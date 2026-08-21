/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';

import {
	Group,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import handleAddGroup from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/handleAddGroup';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

const ROOT_UUID = getUuid();

function field(name: string, parent: Uuid = ROOT_UUID): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent,
		required: false,
		settings: {},
		type: 'text',
		uuid: getUuid(),
	};
}

function group(parent: Uuid = ROOT_UUID): Group {
	return {
		children: new Map(),
		label: {en_US: 'Group'},
		parent,
		type: 'group',
		uuid: getUuid(),
	};
}

function root(...children: StructureChild[]): Structure {
	return {
		children: new Map(children.map((child) => [child.uuid, child])),
		erc: 'root-erc',
		label: {},
		name: 'Root',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'new',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
		workflows: {},
	};
}

describe('handleAddGroup', () => {
	afterEach(() => {
		(openToast as jest.Mock).mockClear();
	});

	it('toasts and does not dispatch when the selection spans more than one parent', () => {
		const dispatch = jest.fn();
		const tab = group();
		const nested = field('nested', tab.uuid);
		const sku = field('sku');

		tab.children.set(nested.uuid, nested);

		handleAddGroup({
			dispatch,
			structure: root(sku, tab),
			uuids: [sku.uuid, nested.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'selected-items-must-be-at-the-same-hierarchy-level',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('dispatches add-group when the shared parent is a group at the root, nesting one level', () => {
		const dispatch = jest.fn();
		const tab = group();
		const sku = field('sku', tab.uuid);

		tab.children.set(sku.uuid, sku);

		handleAddGroup({
			dispatch,
			structure: root(tab),
			uuids: [sku.uuid],
		});

		expect(dispatch).toHaveBeenCalledWith({
			parent: tab.uuid,
			type: 'add-group',
			uuids: [sku.uuid],
		});
		expect(openToast).not.toHaveBeenCalled();
	});

	it('toasts and does not dispatch when the shared parent is a nested group', () => {
		const dispatch = jest.fn();
		const tab = group();
		const panel = group(tab.uuid);
		const width = field('width', panel.uuid);

		panel.children.set(width.uuid, width);
		tab.children.set(panel.uuid, panel);

		handleAddGroup({
			dispatch,
			structure: root(tab),
			uuids: [width.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-cannot-be-created-inside-a-nested-group',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});

	it('dispatches add-group and does not toast for a valid selection', () => {
		const dispatch = jest.fn();
		const sku = field('sku');

		handleAddGroup({
			dispatch,
			structure: root(sku),
			uuids: [sku.uuid],
		});

		expect(dispatch).toHaveBeenCalledWith({
			parent: ROOT_UUID,
			type: 'add-group',
			uuids: [sku.uuid],
		});
		expect(openToast).not.toHaveBeenCalled();
	});

	it('dispatches add-group for a root group, pushing it down one level', () => {
		const dispatch = jest.fn();
		const tab = group();

		handleAddGroup({
			dispatch,
			structure: root(tab),
			uuids: [tab.uuid],
		});

		expect(dispatch).toHaveBeenCalledWith({
			parent: ROOT_UUID,
			type: 'add-group',
			uuids: [tab.uuid],
		});
		expect(openToast).not.toHaveBeenCalled();
	});

	it('toasts and does not dispatch when wrapping a root group that already holds a nested group', () => {
		const dispatch = jest.fn();
		const tab = group();
		const panel = group(tab.uuid);

		tab.children.set(panel.uuid, panel);

		handleAddGroup({
			dispatch,
			structure: root(tab),
			uuids: [tab.uuid],
		});

		expect(openToast).toHaveBeenCalledWith({
			message: 'a-group-cannot-be-created-inside-a-nested-group',
			type: 'danger',
		});
		expect(dispatch).not.toHaveBeenCalled();
	});
});

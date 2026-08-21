/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {config} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/config';
import {
	Group,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getGroupItemActions from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getGroupItemActions';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

jest.mock('frontend-js-components-web', () => ({openToast: jest.fn()}));

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/config',
	() => ({config: {isGroupsEnabled: true}})
);

const ROOT_UUID = getUuid();

function field(name: string): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent: ROOT_UUID,
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

function repeatableGroup(): RepeatableGroup {
	return {
		children: new Map(),
		erc: 'group-erc',
		label: {en_US: 'Group'},
		name: 'group',
		parent: ROOT_UUID,
		relationshipERC: '',
		relationshipName: '',
		type: 'repeatable-group',
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

describe('getGroupItemActions', () => {
	afterEach(() => {
		config.isGroupsEnabled = true;
	});

	it('offers no create-group action inside a repeatable group, which has nowhere to render one', () => {
		const repeatable = repeatableGroup();

		const sku = {...field('sku'), parent: repeatable.uuid};

		const structure = root({
			...repeatable,
			children: new Map([[sku.uuid, sku]]),
		});

		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [sku],
				structure,
			})
		).toEqual([]);
	});

	it('offers a single create-group action for a field', () => {
		const sku = field('sku');

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku],
			structure: root(sku),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
		expect(actions[0].symbolLeft).toBe('fieldset');
	});

	it('offers create-group for a group at the root, to push it down a level', () => {
		const tab = group();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [tab],
			structure: root(tab),
		});

		expect(actions).toHaveLength(1);
		expect(actions[0].label).toBe('create-group');
	});

	it('offers create-group when the selection mixes a field and a root group', () => {
		const sku = field('sku');
		const tab = group();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [sku, tab],
			structure: root(sku, tab),
		});

		expect(actions).toHaveLength(1);
	});

	it('offers no action for a group that is already nested', () => {
		const tab = group();
		const panel = group(tab.uuid);

		tab.children.set(panel.uuid, panel);

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [panel],
			structure: root(tab),
		});

		expect(actions).toHaveLength(0);
	});

	it('offers no action for a repeatable group', () => {
		const variants = repeatableGroup();

		const actions = getGroupItemActions({
			dispatch: jest.fn(),
			items: [variants],
			structure: root(variants),
		});

		expect(actions).toHaveLength(0);
	});

	it('offers no action for an empty selection', () => {
		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [],
				structure: root(),
			})
		).toHaveLength(0);
	});

	it('offers no action while the feature flag is off', () => {
		config.isGroupsEnabled = false;

		const sku = field('sku');

		expect(
			getGroupItemActions({
				dispatch: jest.fn(),
				items: [sku],
				structure: root(sku),
			})
		).toHaveLength(0);
	});

	it('dispatches add-group when create-group is clicked', () => {
		const dispatch = jest.fn();
		const sku = field('sku');

		const [createGroup] = getGroupItemActions({
			dispatch,
			items: [sku],
			structure: root(sku),
		});

		createGroup.onClick();

		expect(dispatch).toHaveBeenCalledWith({
			parent: ROOT_UUID,
			type: 'add-group',
			uuids: [sku.uuid],
		});
	});
});

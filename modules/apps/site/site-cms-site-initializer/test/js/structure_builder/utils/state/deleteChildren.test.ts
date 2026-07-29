/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	GroupingContainer,
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {getDefaultField} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import deleteChildren from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/state/deleteChildren';

function createGroupingContainer({
	children = [],
	parent,
}: {
	children?: StructureChild[];
	parent: Uuid;
}): GroupingContainer {
	const uuid = getUuid();

	const container: GroupingContainer = {
		children: new Map(),
		label: {en_US: 'Grouping Container'},
		parent,
		type: 'grouping-container',
		uuid,
	};

	children.forEach((child) => {
		child.parent = uuid;

		container.children.set(child.uuid, child);
	});

	return container;
}

function createRepeatableGroup({
	children = [],
	parent,
}: {
	children?: StructureChild[];
	parent: Uuid;
}): RepeatableGroup {
	const uuid = getUuid();

	const group: RepeatableGroup = {
		children: new Map(),
		erc: 'group-erc',
		label: {en_US: 'Repeatable Group'},
		name: 'RepeatableGroup',
		parent,
		relationshipERC: 'relationship-erc',
		relationshipName: 'relationshipName',
		type: 'repeatable-group',
		uuid,
	};

	children.forEach((child) => {
		child.parent = uuid;

		group.children.set(child.uuid, child);
	});

	return group;
}

function createRoot(children: StructureChild[]): Structure {
	const uuid = getUuid();

	const root: Structure = {
		children: new Map(),
		erc: 'root-erc',
		label: {},
		name: 'Root',
		path: '',
		slug: '',
		spaces: 'all',
		status: 'new',
		system: false,
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid,
		workflows: {},
	};

	children.forEach((child) => {
		child.parent = uuid;

		root.children.set(child.uuid, child);
	});

	return root;
}

describe('deleteChildren', () => {
	it('keeps an emptied grouping-container and only removes its child', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const container = createGroupingContainer({
			children: [field],
			parent: getUuid(),
		});

		const root = createRoot([container]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [field.uuid],
		});

		const updatedContainer = updatedChildren.get(
			container.uuid
		) as GroupingContainer;

		expect(updatedContainer).toBeDefined();
		expect(updatedContainer.type).toBe('grouping-container');
		expect(updatedContainer.children.size).toBe(0);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(container.uuid)).toBe(false);
	});

	it('removes an emptied repeatable-group along with its child', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const group = createRepeatableGroup({
			children: [field],
			parent: getUuid(),
		});

		const root = createRoot([group]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [field.uuid],
		});

		expect(updatedChildren.has(group.uuid)).toBe(false);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(group.uuid)).toBe(true);
	});

	it('removes a grouping-container and all of its descendants when deleted by its own uuid', () => {
		const field = getDefaultField({parent: getUuid(), type: 'text'});

		const nestedField = getDefaultField({
			parent: getUuid(),
			type: 'integer',
		});

		const nestedContainer = createGroupingContainer({
			children: [nestedField],
			parent: getUuid(),
		});

		const container = createGroupingContainer({
			children: [field, nestedContainer],
			parent: getUuid(),
		});

		const root = createRoot([container]);

		const {deletedChildrenUuids, updatedChildren} = deleteChildren({
			root,
			uuids: [container.uuid],
		});

		expect(updatedChildren.has(container.uuid)).toBe(false);
		expect(deletedChildrenUuids.has(container.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(field.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(nestedContainer.uuid)).toBe(true);
		expect(deletedChildrenUuids.has(nestedField.uuid)).toBe(true);
	});
});

/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import buildLocalizedValue from '../../../common/utils/buildLocalizedValue';
import {
	RepeatableGroup,
	Structure,
	StructureChild,
} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import getRandomId from '../getRandomId';
import getRandomName from '../getRandomName';
import isContainer, {Container} from '../isContainer';
import sortChildren from './sortChildren';

export default function addRepeatableGroup({
	groupChildren,
	groupParent,
	groupUuid,
	root,
}: {
	groupChildren: StructureChild[];
	groupParent: Uuid;
	groupUuid: Uuid;
	root: Structure | Container;
}): Structure['children'] | Container['children'] {
	const children = new Map();

	// Iterate over children

	for (const child of root.children.values()) {

		// Don't insert the child if it belongs to the new group

		if (groupChildren.some(({uuid}) => uuid === child.uuid)) {
			continue;
		}

		// Insert the child. If it's a container, build it with recursive call

		if (isContainer(child)) {
			const container = {
				...child,
				children: addRepeatableGroup({
					groupChildren,
					groupParent,
					groupUuid,
					root: child,
				}),
			};

			children.set(container.uuid, container);
		}
		else {
			children.set(child.uuid, child);
		}
	}

	// Add new group if this is the correct parent

	if (root.uuid === groupParent) {
		const group: RepeatableGroup = {
			children: new Map(
				groupChildren.map((child) => [
					child.uuid,
					{...child, parent: groupUuid},
				])
			),
			erc: getRandomId(),
			label: buildLocalizedValue('repeatable-group'),
			name: getRandomName({capitalize: true}),
			parent: groupParent,
			relationshipERC: getRandomId(),
			relationshipName: getRandomName(),
			type: 'repeatable-group',
			uuid: groupUuid,
		};

		children.set(group.uuid, group);
	}

	return sortChildren(children);
}

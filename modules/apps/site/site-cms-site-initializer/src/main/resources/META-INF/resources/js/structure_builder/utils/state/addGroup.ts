/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import buildLocalizedValue from '../../../common/utils/buildLocalizedValue';
import {Group, Structure, StructureChild} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import isContainer, {Container} from '../isContainer';
import sortChildren from './sortChildren';

// Groups are presentation only: unlike a repeatable group they create no child
// object definition and no relationship, and the nesting is serialized to the
// object layout on save.

export default function addGroup({
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

	for (const child of root.children.values()) {
		if (groupChildren.some(({uuid}) => uuid === child.uuid)) {
			continue;
		}

		if (isContainer(child)) {
			const container = {
				...child,
				children: addGroup({
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

	if (root.uuid === groupParent) {
		const group: Group = {
			children: new Map(
				groupChildren.map((child) => [
					child.uuid,
					{...child, parent: groupUuid},
				])
			),
			label: buildLocalizedValue('group'),
			parent: groupParent,
			type: 'group',
			uuid: groupUuid,
		};

		children.set(group.uuid, group);
	}

	return sortChildren(children);
}

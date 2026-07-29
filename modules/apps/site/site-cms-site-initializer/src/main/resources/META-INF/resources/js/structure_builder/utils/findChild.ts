/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ReferencedStructure,
	Structure,
	StructureChild,
} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import isContainer, {Container} from './isContainer';

export default function findChild({
	root,
	uuid,
}: {
	root: Container | ReferencedStructure | Structure;
	uuid: Uuid;
}): StructureChild | null {
	for (const child of root.children.values()) {
		if (child.uuid === uuid) {
			return child;
		}

		if (child.type === 'referenced-structure' || isContainer(child)) {
			const found = findChild({root: child, uuid});

			if (found) {
				return found;
			}
		}
	}

	return null;
}

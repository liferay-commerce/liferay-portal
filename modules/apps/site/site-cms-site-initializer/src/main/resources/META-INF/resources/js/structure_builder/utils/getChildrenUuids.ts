/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ReferencedStructure, Structure} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import isContainer, {Container} from './isContainer';

export function getChildrenUuids({
	root,
	uuids = new Set(),
}: {
	root: Container | ReferencedStructure | Structure;
	uuids?: Set<Uuid>;
}) {
	for (const child of root.children.values()) {
		uuids.add(child.uuid);

		if (child.type === 'referenced-structure' || isContainer(child)) {
			getChildrenUuids({root: child, uuids});
		}
	}

	return uuids;
}

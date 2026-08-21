/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Structure} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findChild from './findChild';

// Depth specifies how a group renders: 1 (at the root) is a tab, 2 (inside a
// group) is a panel. Nothing deeper is allowed.

export const MAXIMUM_GROUP_DEPTH = 2;

export default function getGroupDepth({
	structure,
	uuid,
}: {
	structure: Structure;
	uuid: Uuid;
}): number {
	let depth = 0;

	let currentUuid = uuid;

	while (currentUuid && currentUuid !== structure.uuid) {
		const child = findChild({root: structure, uuid: currentUuid});

		if (!child) {
			break;
		}

		if (child.type === 'group') {
			depth++;
		}

		currentUuid = child.parent;
	}

	return depth;
}

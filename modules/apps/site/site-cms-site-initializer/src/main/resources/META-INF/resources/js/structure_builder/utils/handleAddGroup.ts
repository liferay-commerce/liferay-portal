/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {Dispatch} from 'react';

import {Action} from '../contexts/StateContext';
import {Structure, StructureChild} from '../types/Structure';
import {Uuid} from '../types/Uuid';
import findChild from './findChild';
import getGroupDepth, {MAXIMUM_GROUP_DEPTH} from './getGroupDepth';

export default function handleAddGroup({
	dispatch,
	structure,
	uuids,
}: {
	dispatch: Dispatch<Action>;
	structure: Structure;
	uuids: Uuid[];
}) {
	const items = uuids.map((uuid) => findChild({root: structure, uuid})!);

	if (new Set(items.map((item) => item.parent)).size > 1) {
		openToast({
			message: Liferay.Language.get(
				'selected-items-must-be-at-the-same-hierarchy-level'
			),
			type: 'danger',
		});

		return;
	}

	const parent = items[0].parent;

	const depth =
		getGroupDepth({structure, uuid: parent}) +
		1 +
		Math.max(...items.map(_getGroupLevels));

	if (depth > MAXIMUM_GROUP_DEPTH) {
		openToast({
			message: Liferay.Language.get(
				'a-group-cannot-be-created-inside-a-nested-group'
			),
			type: 'danger',
		});

		return;
	}

	dispatch({parent, type: 'add-group', uuids});
}

function _getGroupLevels(item: StructureChild): number {
	if (item.type !== 'group') {
		return 0;
	}

	return (
		1 +
		Math.max(0, ...Array.from(item.children.values()).map(_getGroupLevels))
	);
}

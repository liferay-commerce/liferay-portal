/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Dispatch} from 'react';

import {config} from '../config';
import {Action} from '../contexts/StateContext';
import {Structure, StructureChild} from '../types/Structure';
import getGroupDepth from './getGroupDepth';
import handleAddGroup from './handleAddGroup';
import isField from './isField';
import isInsideRepeatableGroup from './isInsideRepeatableGroup';

export type GroupItemAction = {
	label: string;
	onClick: () => void;
	symbolLeft?: string;
};

export default function getGroupItemActions({
	dispatch,
	items,
	structure,
}: {
	dispatch: Dispatch<Action>;
	items: StructureChild[];
	structure: Structure;
}): GroupItemAction[] {
	if (!config.isGroupsEnabled || !items.length) {
		return [];
	}

	const groupable = items.every(
		(item) =>
			!isInsideRepeatableGroup({structure, uuid: item.parent}) &&
			(isField(item) ||
				(item.type === 'group' &&
					getGroupDepth({structure, uuid: item.uuid}) === 1))
	);

	if (!groupable) {
		return [];
	}

	return [
		{
			label: Liferay.Language.get('create-group'),
			onClick: () =>
				handleAddGroup({
					dispatch,
					structure,
					uuids: items.map((item) => item.uuid),
				}),
			symbolLeft: 'fieldset',
		},
	];
}

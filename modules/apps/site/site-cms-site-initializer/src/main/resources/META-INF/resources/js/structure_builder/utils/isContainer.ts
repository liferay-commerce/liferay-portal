/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, RepeatableGroup, StructureChild} from '../types/Structure';

export type Container = Group | RepeatableGroup;

export default function isContainer(child: StructureChild): child is Container {
	return child.type === 'group' || child.type === 'repeatable-group';
}

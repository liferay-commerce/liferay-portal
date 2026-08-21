/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {config} from '../config';
import {Structure} from '../types/Structure';
import persistObjectLayout from './persistObjectLayout';

export default async function persistStructureData(
	structure: Structure
): Promise<boolean> {
	if (!config.isGroupsEnabled) {
		return false;
	}

	const {error} = await persistObjectLayout({
		erc: structure.erc,
		structure,
	});

	return error;
}

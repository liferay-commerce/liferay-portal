/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import structureBuilderRegistry from '../contributors/registry';
import {Structure} from '../types/Structure';

export default async function persistStructureData(
	structure: Structure
): Promise<boolean> {
	const structureBuilderProvider = structureBuilderRegistry.getProvider(
		structure.type
	);

	if (!structureBuilderProvider?.persist) {
		return false;
	}

	const {error} = await structureBuilderProvider.persist({
		erc: structure.erc,
		structure,
	});

	return error;
}

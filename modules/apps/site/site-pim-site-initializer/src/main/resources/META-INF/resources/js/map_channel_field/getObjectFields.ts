/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {IObjectField, IPIMObjectDefinition} from './types';

export default function getObjectFields(
	allStructuresObjectFields: IObjectField[],
	pimObjectDefinitions: IPIMObjectDefinition[],
	source: string
): IObjectField[] {
	if (!source) {
		return allStructuresObjectFields;
	}

	const pimObjectDefinition = pimObjectDefinitions.find(
		({name}) => name === source
	);

	if (!pimObjectDefinition) {
		return [];
	}

	return pimObjectDefinition.objectFields;
}

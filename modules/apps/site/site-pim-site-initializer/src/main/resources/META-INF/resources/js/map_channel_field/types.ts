/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export const MAPPING_TYPE_ATTRIBUTE = 'attribute';

export const MAPPING_TYPE_FIXED_VALUE = 'fixedValue';

export interface IMapping {
	attribute: string;
	source: string;
	type?: string;
	value?: string;
}

export interface IObjectField {
	label: string;
	name: string;
}

export interface IPIMObjectDefinition {
	label: string;
	name: string;
	objectFields: IObjectField[];
}

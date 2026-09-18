/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getObjectFields from '../../../src/main/resources/META-INF/resources/js/map_channel_field/getObjectFields';

const ALL_STRUCTURES_OBJECT_FIELDS = [
	{label: 'Code', name: 'code'},
	{label: 'Name', name: 'name'},
];

const PIM_OBJECT_DEFINITIONS = [
	{
		label: 'Base SKU',
		name: 'PIMBaseSku',
		objectFields: [{label: 'Code', name: 'code'}],
	},
];

describe('getObjectFields', () => {
	it('returns every structure field when no source is selected', () => {
		expect(
			getObjectFields(
				ALL_STRUCTURES_OBJECT_FIELDS,
				PIM_OBJECT_DEFINITIONS,
				''
			)
		).toEqual(ALL_STRUCTURES_OBJECT_FIELDS);
	});

	it('returns only the fields of the selected structure', () => {
		expect(
			getObjectFields(
				ALL_STRUCTURES_OBJECT_FIELDS,
				PIM_OBJECT_DEFINITIONS,
				'PIMBaseSku'
			)
		).toEqual([{label: 'Code', name: 'code'}]);
	});

	it('returns nothing when the selected structure is gone', () => {
		expect(
			getObjectFields(
				ALL_STRUCTURES_OBJECT_FIELDS,
				PIM_OBJECT_DEFINITIONS,
				'PIMApparelSku'
			)
		).toEqual([]);
	});
});

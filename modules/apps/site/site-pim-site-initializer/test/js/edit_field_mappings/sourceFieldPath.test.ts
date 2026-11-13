/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	getRepeatableGroups,
	joinSourceFieldPath,
	parseSourceFieldPath,
	resolveSourceFieldPath,
} from '../../../src/main/resources/META-INF/resources/js/edit_field_mappings/sourceFieldPath';

const PIM_SHIRT_CLASS_NAME = 'com.liferay.object.model.ObjectDefinition#S1Z3';

const SIZES = {
	label: 'Sizes',
	name: 'r_sizes',
	objectFields: [{label: 'Width', name: 'width'}],
};

const IMAGES = {
	label: 'Images',
	name: 'r_images',
	objectFields: [{label: 'Caption', name: 'caption'}],
	repeatableGroups: [SIZES],
};

const OBJECT_DEFINITIONS = [
	{
		className: 'com.liferay.object.model.ObjectDefinition#P4R4',
		label: 'PIM Basic Product',
		objectFields: [{label: 'SKU', name: 'sku'}],
	},
	{
		className: PIM_SHIRT_CLASS_NAME,
		label: 'PIM Shirt',
		objectFields: [{label: 'Title', name: 'title'}],
		repeatableGroups: [IMAGES],
	},
];

describe('sourceFieldPath', () => {
	it('parses and joins a source field path', () => {
		expect(parseSourceFieldPath('')).toEqual([]);
		expect(parseSourceFieldPath('sku')).toEqual(['sku']);
		expect(parseSourceFieldPath('r_images/r_sizes/width')).toEqual([
			'r_images',
			'r_sizes',
			'width',
		]);
		expect(joinSourceFieldPath(['r_images', 'caption'])).toBe(
			'r_images/caption'
		);
	});

	it('merges the repeatable groups that share a name across structures', () => {
		expect(
			getRepeatableGroups(
				[
					...OBJECT_DEFINITIONS,
					{
						className:
							'com.liferay.object.model.ObjectDefinition#P4N7',
						label: 'PIM Pants',
						objectFields: [{label: 'Title', name: 'title'}],
						repeatableGroups: [
							{
								label: 'Images',
								name: 'r_images',
								objectFields: [
									{label: 'Caption', name: 'caption'},
									{label: 'Alt Text', name: 'altText'},
								],
							},
						],
					},
				],
				''
			)
		).toEqual([
			{
				label: 'Images',
				name: 'r_images',
				objectFields: [
					{label: 'Caption', name: 'caption'},
					{label: 'Alt Text', name: 'altText'},
				],
				repeatableGroups: [{...SIZES, repeatableGroups: []}],
			},
		]);
	});

	it('returns the repeatable groups of the selected structure', () => {
		expect(
			getRepeatableGroups(OBJECT_DEFINITIONS, PIM_SHIRT_CLASS_NAME)
		).toEqual([IMAGES]);
		expect(
			getRepeatableGroups(
				OBJECT_DEFINITIONS,
				'com.liferay.object.model.ObjectDefinition#P4R4'
			)
		).toEqual([]);
	});

	it('resolves a source field path against the structures', () => {
		expect(resolveSourceFieldPath(OBJECT_DEFINITIONS, '', 'sku')).toEqual({
			complete: true,
			repeatableGroups: [],
		});
		expect(
			resolveSourceFieldPath(
				OBJECT_DEFINITIONS,
				'',
				'r_images/r_sizes/width'
			)
		).toMatchObject({
			complete: true,
			repeatableGroups: [{name: 'r_images'}, {name: 'r_sizes'}],
		});
		expect(
			resolveSourceFieldPath(OBJECT_DEFINITIONS, '', 'r_images')
		).toMatchObject({
			complete: false,
			repeatableGroups: [{name: 'r_images'}],
		});
		expect(
			resolveSourceFieldPath(OBJECT_DEFINITIONS, '', 'r_images/sku')
		).toMatchObject({
			complete: false,
			repeatableGroups: [{name: 'r_images'}],
		});
		expect(
			resolveSourceFieldPath(
				OBJECT_DEFINITIONS,
				'com.liferay.object.model.ObjectDefinition#P4R4',
				'r_images/caption'
			)
		).toEqual({complete: false, repeatableGroups: []});
		expect(resolveSourceFieldPath(OBJECT_DEFINITIONS, '', '')).toEqual({
			complete: false,
			repeatableGroups: [],
		});
	});
});

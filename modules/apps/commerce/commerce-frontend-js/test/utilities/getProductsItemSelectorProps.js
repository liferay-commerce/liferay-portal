/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getProductsItemSelectorProps from '../../src/main/resources/META-INF/resources/utilities/getProductsItemSelectorProps';

const STATUS_FILTER = 'statusCode/any(x:(x eq 0))';

function getAPIURL(options) {
	const {apiURL} = getProductsItemSelectorProps({
		portletNamespace: 'ns',
		...options,
	});

	return new URL(apiURL);
}

describe('getProductsItemSelectorProps', () => {
	describe('apiURL', () => {
		it('lists the approved products with their catalog', () => {
			const url = getAPIURL();

			expect(url.pathname).toBe(
				'/o/headless-commerce-admin-catalog/v1.0/products'
			);
			expect(url.searchParams.get('filter')).toBe(STATUS_FILTER);
			expect(url.searchParams.get('nestedFields')).toBe('catalog');
		});

		it('filters by channel', () => {
			const url = getAPIURL({channelId: 42});

			expect(url.searchParams.get('filter')).toBe(
				`${STATUS_FILTER} and (productChannelFilter eq false or channelId/any(x:(x eq 42)))`
			);
		});

		it('excludes products and product types', () => {
			const url = getAPIURL({
				excludedProductIds: [1, 2],
				excludedProductTypes: ['grouped'],
			});

			expect(url.searchParams.get('filter')).toBe(
				`${STATUS_FILTER} and not (productId in (1,2)) and productType ne 'grouped'`
			);
		});
	});

	it('selects products by ID', () => {
		const {fdsProps, locator, multiSelect, title} =
			getProductsItemSelectorProps({portletNamespace: 'ns'});

		expect(fdsProps.configInURLBehavior).toBe('OFF');
		expect(fdsProps.id).toBe('nsproductsItemSelector');
		expect(
			fdsProps.views[0].schema.fields.map((field) => field.fieldName)
		).toEqual([
			'thumbnail',
			['name', 'LANG'],
			'skuFormatted',
			'catalog.name',
			'workflowStatusInfo',
		]);
		expect(locator.id).toBe('id');
		expect(locator.value).toBe('id');
		expect(multiSelect).toBe(true);
		expect(title).toBe('select-x');
	});

	it('supports a single selection with a custom title', () => {
		const {multiSelect, title} = getProductsItemSelectorProps({
			multiSelect: false,
			portletNamespace: 'ns',
			title: 'Add Products',
		});

		expect(multiSelect).toBe(false);
		expect(title).toBe('Add Products');
	});
});

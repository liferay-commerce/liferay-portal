/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {sub} from 'frontend-js-web';

const PRODUCTS_API_URL = '/o/headless-commerce-admin-catalog/v1.0/products';

const STATUS_FILTER = 'statusCode/any(x:(x eq 0))';

function buildAPIURL(channelId, excludedProductIds, excludedProductTypes) {
	const url = new URL(PRODUCTS_API_URL, window.location.origin);

	url.searchParams.set('nestedFields', 'catalog');

	const filters = [STATUS_FILTER];

	if (channelId) {
		filters.push(
			`(productChannelFilter eq false or channelId/any(x:(x eq ${channelId})))`
		);
	}

	if (excludedProductIds.length) {
		filters.push(`not (productId in (${excludedProductIds.join(',')}))`);
	}

	excludedProductTypes.forEach((excludedProductType) =>
		filters.push(`productType ne '${excludedProductType}'`)
	);

	url.searchParams.set('filter', filters.join(' and '));

	return url.toString();
}

export default function getProductsItemSelectorProps({
	channelId,
	excludedProductIds = [],
	excludedProductTypes = [],
	multiSelect = true,
	portletNamespace,
	title = sub(
		Liferay.Language.get('select-x'),
		Liferay.Language.get('product')
	),
}) {
	return {
		apiURL: buildAPIURL(
			channelId,
			excludedProductIds,
			excludedProductTypes
		),
		fdsProps: {
			configInURLBehavior: 'OFF',
			id: `${portletNamespace}productsItemSelector`,
			pagination: {
				deltas: [{label: 20}, {label: 40}, {label: 60}],
				initialDelta: 20,
			},
			views: [
				{
					contentRenderer: 'table',
					label: '',
					name: 'table',
					schema: {
						fields: [
							{
								contentRenderer: 'image',
								fieldName: 'thumbnail',
								label: Liferay.Language.get('image'),
							},
							{
								fieldName: ['name', 'LANG'],
								label: Liferay.Language.get('name'),
							},
							{
								fieldName: 'skuFormatted',
								label: Liferay.Language.get('sku'),
							},
							{
								fieldName: 'catalog.name',
								label: Liferay.Language.get('catalog'),
							},
							{
								contentRenderer: 'status',
								fieldName: 'workflowStatusInfo',
								label: Liferay.Language.get('status'),
							},
						],
					},
				},
			],
		},
		itemTypeLabel: Liferay.Language.get('product'),
		locator: {
			id: 'id',
			label: `name.${Liferay.ThemeDisplay.getLanguageId()}`,
			value: 'id',
		},
		multiSelect,
		title,
	};
}

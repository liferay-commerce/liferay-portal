/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-Licnse-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../fixtures/commercePagesTest';
import {loginTest} from '../../../fixtures/loginTest';
import {getRandomInt} from '../../../utils/util';

export const test = mergeTests(apiHelpersTest, commercePagesTest, loginTest);

test('products with option type of datetime displays the correct format', async ({
	apiHelpers,
	commerceLayoutsPage,
	commerceProductDetailsPage,
}) => {
	const pageLabel = 'Product Details Page';

	await commerceLayoutsPage.goToPages();
	await commerceLayoutsPage.createWidgetPage(pageLabel);
	await commerceProductDetailsPage.goToPage();
	await commerceProductDetailsPage.addProductDetailsWidget();

	const site = await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
		'guest'
	);

	const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const productName = 'Product' + getRandomInt();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {
			en_US: productName,
		},
	});

	const option = await apiHelpers.headlessCommerceAdminCatalog.postOption();

	const productOption =
		await apiHelpers.headlessCommerceAdminCatalog.postProductOption(
			product.productId,
			true,
			'select',
			'key' + getRandomInt(),
			'ProductOptionName' + getRandomInt(),
			option.id,
			1,
			true,
			true
		);

	const productOptionValue =
		await apiHelpers.headlessCommerceAdminCatalog.postProductOptionValues(
			productOption.items[0].id,
			1,
			'days',
			'key' + getRandomInt(),
			'ProductOptionValueName' + getRandomInt(),
			'2024-01-01T00:00:00.000Z',
			true,
			1
		);

	await commerceProductDetailsPage.goToProductPage(productName);

	await expect(
		commerceProductDetailsPage.page
			.locator('select, options')
			.filter({hasText: 'Jan 1, 2024'})
	).toBeVisible();

	await Promise.all([
		apiHelpers.headlessCommerceAdminChannel.deleteChannel(channel.id),
	]);

	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		product.productId
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteProductOptionValue(
		productOptionValue.id
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteProductOption(
		productOption.items[0].id
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteOption(option.id);

	await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(catalog.id);

	await commerceLayoutsPage.goToPages();
	await commerceProductDetailsPage.deleteProductDetailsPage();
});

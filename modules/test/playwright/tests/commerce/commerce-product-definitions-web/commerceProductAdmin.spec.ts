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

test('products can be configured to have the option type of datetime', async ({
	apiHelpers,
	commerceProductsAdminPage,
}) => {
	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const productName = 'Product' + getRandomInt();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {
			en_US: productName,
		},
	});

	const optionName = 'OptionName' + getRandomInt();

	const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		'OptionKey' + getRandomInt(),
		optionName
	);

	await commerceProductsAdminPage.goto();
	await commerceProductsAdminPage.gotoProductDetails(productName);
	await commerceProductsAdminPage.gotoProductOptions();
	await commerceProductsAdminPage.addDateTimeProductOption(optionName);

	const productOptionValueKey = 'key' + getRandomInt();

	await commerceProductsAdminPage.addDateTimeProductOptionValue(
		productOptionValueKey
	);

	await commerceProductsAdminPage.reloadPage();
	await commerceProductsAdminPage.gotoProductOption(optionName);

	await expect(
		commerceProductsAdminPage.contentFrameLocator.getByText(
			productOptionValueKey
		)
	).toBeVisible();

	await commerceProductsAdminPage.goto();

	await Promise.all([
		await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
			product.productId
		),
	]);

	await apiHelpers.headlessCommerceAdminCatalog.deleteOption(option.id);

	await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(catalog.id);
});

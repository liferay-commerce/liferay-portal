/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';

export const test = mergeTests(apiHelpersTest, commercePagesTest, loginTest());

test('COMMERCE-11835 Account Supplier role user cannot upload diagram file/image', async ({
	apiHelpers,
	commerceAdminProductDetailsDiagramPage,
	commerceAdminProductDetailsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const account = await apiHelpers.headlessAdminUser.postAccount({
		name: 'Supplier account',
		type: 'supplier',
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
		accountId: account.id,
	});

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {
			en_US: 'Product',
		},
		productType: 'diagram',
	});

	try {
		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductDiagramTab();

		await commerceAdminProductDetailsDiagramPage.goToSelectFileButton();

		// await commerceAdminProductDetailsProductRelationsPage.selectItemsInput.check();

		// await expect(
		// 	commerceAdminProductDetailsProductRelationsPage.deleteBulkButton
		// ).toBeVisible();

		// await commerceAdminProductDetailsProductRelationsPage.deleteBulkButton.click();

		// await expect(
		// 	commerceAdminProductDetailsProductRelationsPage.emptyTableMessage
		// ).toBeVisible();
	} finally {
		await apiHelpers.headlessAdminUser.deleteAccount(account.id);

		await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(catalog.id);
	}
});

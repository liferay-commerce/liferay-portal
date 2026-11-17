/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {createCategories} from '../../../../helpers/CreateCategories';
import getRandomString from '../../../../utils/getRandomString';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test('LPD-3381 Error message is shown when saving an existing override category display page entry without selecting a valid category', async ({
	apiHelpers,
	commerceAdminChannelDetailsCategoryDisplayPagesPage,
	commerceAdminChannelDetailsPage,
	commerceAdminChannelsPage,
}) => {
	await apiHelpers.headlessAdminSite.postSite({
		name: 'Minium',
		templateKey: 'minium-initializer',
		templateType: 'site-initializer',
	});

	const pageName = 'Placed Orders';

	const channels =
		await apiHelpers.headlessCommerceAdminChannel.getChannelsPage(
			'Minium Portal'
		);

	apiHelpers.data.push({id: channels.items[0].id, type: 'channel'});

	const catalogs =
		await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage('Minium');

	apiHelpers.data.push({id: catalogs.items[0].id, type: 'catalog'});

	const products =
		await apiHelpers.headlessCommerceAdminCatalog.getProductsPage(50, '');

	for (let i = 0; i < products.totalCount; i++) {
		if (products.items[i].catalogId === catalogs.items[0].id) {
			apiHelpers.data.push({
				id: products.items[i].productId,
				type: 'product',
			});
		}
	}
	const options = await apiHelpers.headlessCommerceAdminCatalog.getOptions();

	for (let i = 0; i < options.totalCount; i++) {
		apiHelpers.data.push({
			id: options.items[i].id,
			type: 'option',
		});
	}

	const optionCategories =
		await apiHelpers.headlessCommerceAdminCatalog.getOptionCategories();

	for (let i = 0; i < optionCategories.totalCount; i++) {
		apiHelpers.data.push({
			id: optionCategories.items[i].id,
			type: 'optionCategory',
		});
	}

	const specifications =
		await apiHelpers.headlessCommerceAdminCatalog.getSpecifications();

	for (let i = 0; i < specifications.totalCount; i++) {
		apiHelpers.data.push({
			id: specifications.items[i].id,
			type: 'specification',
		});
	}

	const warehouses =
		await apiHelpers.headlessCommerceAdminInventoryApiHelper.getWarehousesPage();

	for (let i = 0; i < warehouses.totalCount; i++) {
		apiHelpers.data.push({
			id: warehouses.items[i].id,
			type: 'warehouse',
		});
	}

	await commerceAdminChannelsPage.goto();

	await (
		await commerceAdminChannelsPage.channelsTableRowLink(
			channels.items[0].name
		)
	).click();

	await commerceAdminChannelDetailsPage.goToCategoryDisplayPages();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.addDisplayLayoutButton.click();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
		'Choose'
	);

	await expect(
		commerceAdminChannelDetailsCategoryDisplayPagesPage.frameSearchBar
	).toBeVisible();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.selectCategoryDisplayPage(
		pageName
	);

	await expect(
		await commerceAdminChannelDetailsCategoryDisplayPagesPage.categoryDisplayPageLabel(
			pageName
		)
	).toBeVisible();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
		'Save'
	);

	await expect(
		commerceAdminChannelDetailsCategoryDisplayPagesPage.errorMessageSelectCategory
	).toBeVisible();
});

test(
	'Category display page rules pick the category from the new item selector',
	{tag: '@LPD-110420'},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsCategoryDisplayPagesPage,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
	}) => {
		const site = await apiHelpers.headlessAdminSite.postSite({
			name: getRandomString(),
		});

		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
			groupId: site.id,
			title: getRandomString(),
		});

		const vocabularyName = getRandomString();

		const [category] = await createCategories({
			apiHelpers,
			categoryNames: [{name: getRandomString()}],
			siteId: site.id,
			vocabularyName,
		});

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await commerceAdminChannelDetailsPage.goToCategoryDisplayPages();

		await commerceAdminChannelDetailsCategoryDisplayPagesPage.addDisplayLayoutButton.click();

		await commerceAdminChannelDetailsCategoryDisplayPagesPage.selectCategory(
			vocabularyName,
			category.name
		);

		await expect(
			await commerceAdminChannelDetailsCategoryDisplayPagesPage.categoryLabel(
				category.name
			)
		).toBeVisible();

		await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
			'Choose'
		);

		await commerceAdminChannelDetailsCategoryDisplayPagesPage.selectCategoryDisplayPage(
			layout.nameCurrentValue
		);

		await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
			'Save'
		);

		await expect(
			commerceAdminChannelDetailsCategoryDisplayPagesPage.successMessage
		).toBeVisible();

		await commerceAdminChannelDetailsPage.page.reload();

		await commerceAdminChannelDetailsPage.goToCategoryDisplayPages();

		await expect(
			await commerceAdminChannelDetailsCategoryDisplayPagesPage.categoryNameRowTableLink(
				category.name
			)
		).toBeVisible();
	}
);

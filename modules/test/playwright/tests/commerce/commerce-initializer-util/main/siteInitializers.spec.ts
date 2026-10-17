/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {miniumSetUp, speedwellSetUp} from '../../utils/commerce';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

const specifications = [
	{key: 'dimensions', optionCategoryKey: 'dimensions', title: 'Dimensions'},
	{key: 'fit', optionCategoryKey: 'fit', title: 'Fit'},
	{key: 'height', optionCategoryKey: 'dimensions', title: 'Height'},
	{key: 'length', optionCategoryKey: 'dimensions', title: 'Length'},
	{key: 'material', optionCategoryKey: 'material', title: 'Material'},
	{key: 'quantity', optionCategoryKey: 'quantity', title: 'Quantity'},
	{key: 'warranty', optionCategoryKey: 'warranty', title: 'Warranty'},
	{key: 'weight', optionCategoryKey: 'dimensions', title: 'Weight'},
	{key: 'width', optionCategoryKey: 'dimensions', title: 'Width'},
];

async function expectInitializerContent(
	apiHelpers: DataApiHelpers,
	{catalog, channel, site}
) {
	await test.step('Verify the channel is linked to the site and uses US dollars', async () => {
		expect(channel).toMatchObject({
			currencyCode: 'USD',
			name: `${site.name} Portal`,
			siteGroupId: site.id,
			type: 'site',
		});
	});

	await test.step('Verify the catalog holds 50 products priced in US dollars', async () => {
		expect(catalog).toMatchObject({
			currencyCode: 'USD',
			defaultLanguageId: 'en_US',
			name: site.name,
		});

		const productsPage =
			await apiHelpers.headlessCommerceAdminCatalog.getProducts(
				new URLSearchParams({
					filter: `catalogId eq ${catalog.id}`,
					pageSize: '1',
				})
			);

		expect(productsPage.totalCount).toBe(50);
	});

	await test.step('Verify the Package Quantity option is created', async () => {
		const optionsPage =
			await apiHelpers.headlessCommerceAdminCatalog.getOptions(
				new URLSearchParams({search: 'package-quantity'})
			);

		const option = optionsPage.items.find(
			(item) => item.key === 'package-quantity'
		);

		expect(option.name).toMatchObject({en_US: 'Package Quantity'});
	});

	await test.step('Verify the specifications are created in their groups', async () => {
		const specificationsPage =
			await apiHelpers.headlessCommerceAdminCatalog.getSpecifications(
				new URLSearchParams({pageSize: '100'})
			);

		for (const {key, optionCategoryKey, title} of specifications) {
			const specification = specificationsPage.items.find(
				(item) => item.key === key
			);

			expect(specification).toMatchObject({
				optionCategory: {key: optionCategoryKey},
				title: {en_US: title},
			});
		}
	});
}

test(
	'The Minium initializer creates a B2B storefront with its catalog',
	{tag: '@LPD-108862'},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		commerceThemeMiniumCatalogPage,
		page,
	}) => {
		const {catalog, channel, site} = await miniumSetUp(apiHelpers);

		await expectInitializerContent(apiHelpers, {catalog, channel, site});

		await test.step('Verify the channel site type is B2B', async () => {
			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await expect(
				commerceAdminChannelsPage.commerceSiteType
			).toHaveValue('1');
		});

		await test.step('Verify the catalog renders with the Minium theme', async () => {
			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(page.locator('#minium')).toBeVisible();
			await expect(
				page.getByText('50 Products Available', {exact: true})
			).toBeVisible();
			await expect(
				commerceThemeMiniumCatalogPage.productCard('U-Joint')
			).toBeVisible();
		});
	}
);

test(
	'The Speedwell initializer creates a B2C storefront with its catalog',
	{tag: '@LPD-108862'},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		commerceThemeMiniumCatalogPage,
		page,
	}) => {
		const {catalog, channel, site} = await speedwellSetUp(apiHelpers);

		await expectInitializerContent(apiHelpers, {catalog, channel, site});

		await test.step('Verify the channel site type is B2C', async () => {
			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await expect(
				commerceAdminChannelsPage.commerceSiteType
			).toHaveValue('0');
		});

		await test.step('Verify the catalog renders with the Speedwell theme', async () => {
			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(page.locator('#speedwell')).toBeVisible();
			await expect(
				page.getByText('50 Products Available', {exact: true})
			).toBeVisible();
			await expect(
				commerceThemeMiniumCatalogPage.productCard('U-Joint')
			).toBeVisible();
		});
	}
);

/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../../../fixtures/applicationsMenuPageTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageViewModePagesTest} from '../../../../fixtures/pageViewModePagesTest';
import getRandomString from '../../../../utils/getRandomString';

export const test = mergeTests(
	applicationsMenuPageTest,
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	pageViewModePagesTest
);

test('LPD-30188 Product publisher tag filters can be added and removed', async ({
	apiHelpers,
	page,
	productPublisherPage,
	site,
	widgetPagePage,
}) => {
	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		title: getRandomString(),
	});

	await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product1 = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: getRandomString()},
		tags: ['tag1'],
	});
	const product2 = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: getRandomString()},
		tags: ['tag2'],
	});

	await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyURL}`);

	await widgetPagePage.addPortlet('Product Publisher');

	await expect(
		await productPublisherPage.productLink(product1.name.en_US)
	).toBeVisible();
	await expect(
		await productPublisherPage.productLink(product2.name.en_US)
	).toBeVisible();

	await productPublisherPage.addProductPublisherTagFilter('tag1');

	await page.goto(`/web/${site.name}`);

	await expect(
		await productPublisherPage.productLink(product1.name.en_US)
	).toBeVisible();
	await expect(
		await productPublisherPage.productLink(product2.name.en_US)
	).toHaveCount(0);

	await productPublisherPage.removeProductPublisherTagFilter('tag1');

	await page.goto(`/web/${site.name}`);

	await expect(
		await productPublisherPage.productLink(product1.name.en_US)
	).toBeVisible();
	await expect(
		await productPublisherPage.productLink(product2.name.en_US)
	).toBeVisible();
});

test(
	'Product names are properly escaped and appear correctly',
	{tag: ['@LPD-3300', '@LPD-58881']},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		page,
		productPublisherPage,
		site,
		widgetPagePage,
	}) => {
		const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
			groupId: site.id,
			title: getRandomString(),
		});

		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		await commerceAdminChannelsPage.changeCommerceChannelSiteType(
			channel.name,
			'B2B'
		);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const productName1 = 'B "&" B';
		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: {en_US: productName1},
		});

		const productName2 =
			'"></option><img src=x onerror=alert(document.location)>';
		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: {en_US: productName2},
		});

		await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyURL}`);

		await widgetPagePage.addPortlet('Product Publisher');

		await expect(
			await productPublisherPage.productLink(productName1)
		).toBeVisible();
		await expect(
			await productPublisherPage.productLink(productName2)
		).toBeVisible();
	}
);

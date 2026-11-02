/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {createReadStream} from 'fs';
import path from 'node:path';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {globalMenuPagesTest} from '../../../../fixtures/globalMenuPagesTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageViewModePagesTest} from '../../../../fixtures/pageViewModePagesTest';
import getRandomString from '../../../../utils/getRandomString';
import performLogin, {
	performLogout,
	performUserSwitch,
} from '../../../../utils/performLogin';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';
import {templatesPageTest} from '../../../template-web/main/fixtures/templatesPageTest';
import {createAccountWithBuyerUser} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	globalMenuPagesTest,
	isolatedSiteTest,
	loginTest(),
	templatesPageTest,
	pageViewModePagesTest
);

test(
	'Buyer adds, limits and removes products in the comparison bar and the comparison table',
	{
		tag: [
			'@COMMERCE-5911',
			'@COMMERCE-5912',
			'@COMMERCE-5914',
			'@COMMERCE-5915',
			'@COMMERCE-5916',
			'@LPD-108792',
		],
	},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		page,
		productComparisonPage,
		site,
	}) => {
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

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			getRandomString(),
			'Color'
		);

		const imageIds = {};
		const productURLs = {};

		for (const productName of ['Alpha', 'Beta', 'Delta', 'Gamma']) {
			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: productName},
					productOptions: [
						{
							fieldType: 'select',
							key: option.key,
							name: {en_US: 'Color'},
							optionId: option.id,
							priority: 1,
							productOptionValues: [
								{
									key: 'black',
									name: {en_US: `Black ${productName}`},
									priority: 1,
								},
								{
									key: 'white',
									name: {en_US: `White ${productName}`},
									priority: 2,
								},
							],
						},
					],
				});

			const document = await apiHelpers.headlessDelivery.postDocument(
				site.id,
				createReadStream(
					path.join(__dirname, '/dependencies/liferay.png')
				),
				{
					fileName: getRandomString(),
					title: getRandomString(),
				}
			);

			apiHelpers.data.push({id: document.id, type: 'document'});

			const image =
				await apiHelpers.headlessCommerceAdminCatalog.postImage(
					product.productId,
					document.id
				);

			imageIds[productName] = String(image.id);
			productURLs[productName] =
				`/web/${site.name}/p/${product.urls.en_US}`;
		}

		const catalogLayout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {productsLimit: '3'},
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentMiniPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPPublisherPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentMiniPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPContentPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		const compareLayout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentMiniPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {productsLimit: '3'},
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performUserSwitch(page, buyerUser.alternateName);

		await test.step('Select products from the product cards up to the comparison bar limit', async () => {
			await page.goto(
				`/web/${site.name}/${catalogLayout.friendlyUrlPath}`
			);

			await productComparisonPage
				.productCardCompareCheckbox('Alpha')
				.check();
			await productComparisonPage
				.productCardCompareCheckbox('Beta')
				.check();
			await productComparisonPage
				.productCardCompareCheckbox('Gamma')
				.check();

			await expect(
				productComparisonPage.compareBarActiveItems
			).toHaveCount(3);
			await expect(productComparisonPage.compareBarItems).toHaveCount(3);

			await expect(
				productComparisonPage.compareBarItemThumbnail(imageIds['Alpha'])
			).toBeVisible();
			await expect(
				productComparisonPage.compareBarItemThumbnail(imageIds['Beta'])
			).toBeVisible();
			await expect(
				productComparisonPage.compareBarItemThumbnail(imageIds['Gamma'])
			).toBeVisible();

			await expect(
				productComparisonPage.productCardCompareCheckbox('Delta')
			).toBeDisabled();
		});

		await test.step('Select a product from its product details page', async () => {
			await page.goto(productURLs['Delta']);

			await productComparisonPage.compareCheckbox.check();

			await expect(
				productComparisonPage.compareBarActiveItems
			).toHaveCount(4);
			await expect(
				productComparisonPage.compareBarItemThumbnail(imageIds['Delta'])
			).toBeVisible();
		});

		await test.step('Compare only the products allowed by the comparison table limit', async () => {
			await productComparisonPage.compareBarCompareLink.click();

			await expect(page).toHaveURL(
				new RegExp(compareLayout.friendlyUrlPath)
			);
			await expect(
				productComparisonPage.comparisonTableRowCells('Color')
			).toHaveText([
				'Color',
				'Black Alpha, White Alpha',
				'Black Beta, White Beta',
				'Black Gamma, White Gamma',
			]);
		});

		await test.step('Deselect a product from its product details page', async () => {
			await page.goto(productURLs['Gamma']);

			await productComparisonPage.compareCheckbox.uncheck();

			await expect(
				productComparisonPage.compareBarActiveItems
			).toHaveCount(3);
			await expect(
				productComparisonPage.compareBarItemThumbnail(imageIds['Gamma'])
			).toBeHidden();

			await productComparisonPage.compareBarCompareLink.click();

			await expect(
				productComparisonPage.comparisonTableRowCells('Color')
			).toHaveText([
				'Color',
				'Black Alpha, White Alpha',
				'Black Beta, White Beta',
				'Black Delta, White Delta',
			]);
		});
	}
);

test(
	'CPContentHelper is available in product comparison widget templates',
	{tag: '@LPD-46669'},
	async ({apiHelpers, page, site, templatesPage}) => {
		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		await templatesPage.gotoWidgetTemplates(site.friendlyUrlPath);

		const productComparisonBarWidgetTemplateName = getRandomString();

		await templatesPage.createWidgetTemplate(
			productComparisonBarWidgetTemplateName,
			'Product Comparison Bar Template'
		);
		await templatesPage.editTemplate(
			productComparisonBarWidgetTemplateName
		);
		await templatesPage.importInformationTemplate(
			__dirname,
			'product_comparison_template.ftl'
		);

		const productComparisonBarWidgetTemplateKey =
			await templatesPage.getTemplateKey();

		await templatesPage.saveTemplate(
			productComparisonBarWidgetTemplateName
		);
		await templatesPage.gotoWidgetTemplates(site.friendlyUrlPath);

		const productComparisonTableWidgetTemplateName = getRandomString();

		await templatesPage.createWidgetTemplate(
			productComparisonTableWidgetTemplateName,
			'Product Comparison Table Template'
		);
		await templatesPage.editTemplate(
			productComparisonTableWidgetTemplateName
		);
		await templatesPage.importInformationTemplate(
			__dirname,
			'product_comparison_template.ftl'
		);

		const productComparisonTableWidgetTemplateKey =
			await templatesPage.getTemplateKey();

		await templatesPage.saveTemplate(
			productComparisonTableWidgetTemplateName
		);

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						displayStyle:
							'ddmTemplate_' +
							productComparisonBarWidgetTemplateKey,
						displayStyleGroupId: String(site.id),
						selectionStyle: 'adt',
					},
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentMiniPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						displayStyle:
							'ddmTemplate_' +
							productComparisonTableWidgetTemplateKey,
						displayStyleGroupId: String(site.id),
						selectionStyle: 'adt',
					},
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);

		await expect(page.getByText('TEST', {exact: true})).toHaveCount(2);
	}
);

test(
	'Product Compare is removed upon logout and login',
	{tag: ['@LPD-37427', '@LPD-60912']},
	async ({apiHelpers, globalMenuPage, page, productComparisonPage}) => {
		const site = await apiHelpers.headlessAdminSite.postSite({
			name: getRandomString(),
		});

		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: {en_US: 'Product'},
		});

		await globalMenuPage.goToSite(site.name);

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCompareContentMiniPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPPublisherPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);

		await globalMenuPage.goToSite(site.name);

		await page.getByRole('checkbox', {disabled: false}).first().click();

		await expect(productComparisonPage.compareBar).toHaveCount(1);

		await page.reload();

		await expect(productComparisonPage.compareBar).toHaveCount(1);

		await performLogout(page);
		await performLogin(page, 'test');

		await globalMenuPage.goToSite(site.name);

		await expect(productComparisonPage.compareBar).toHaveCount(0);
	}
);

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../../fixtures/displayPageTemplatesPagesTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../../fixtures/pageEditorPagesTest';
import getRandomString from '../../../../utils/getRandomString';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	displayPageTemplatesPagesTest,
	isolatedSiteTest,
	loginTest(),
	pageEditorPagesTest
);

test(
	'Pick the product, the page and the display page template of a product display page',
	{tag: '@LPD-110416'},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsProductDisplayPagesPage,
		commerceAdminChannelsPage,
		site,
	}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		const layoutName = getRandomString();

		await apiHelpers.jsonWebServicesLayout.addLayout({
			groupId: site.id,
			title: layoutName,
		});

		const className =
			await apiHelpers.jsonWebServicesClassName.fetchClassName(
				'com.liferay.commerce.product.model.CPDefinition'
			);

		const displayPageTemplateName = getRandomString();

		await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.addDisplayPageLayoutPageTemplateEntry(
			{
				classNameId: className.classNameId,
				groupId: site.id,
				name: displayPageTemplateName,
			}
		);

		// Open the side panel

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.goToProductDisplayPages();

		await commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutButton.click();

		// Pick the display page template

		await commerceAdminChannelDetailsProductDisplayPagesPage.chooseDisplayPage(
			'Display Page Template'
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProductDisplayPageFrame
			.getByText(displayPageTemplateName, {exact: true})
			.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.displayPageName
		).toHaveText(displayPageTemplateName);

		// Pick the page

		await commerceAdminChannelDetailsProductDisplayPagesPage.chooseDisplayPage(
			'Layout'
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProductDisplayPageFrame
			.getByText(layoutName, {exact: true})
			.click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.doneButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.displayPageName
		).toContainText(layoutName);

		// Pick the product

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProduct(
			product.name['en_US']
		);

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutFrame.getByText(
				product.name['en_US'],
				{exact: true}
			)
		).toBeVisible();

		// Save

		await commerceAdminChannelDetailsProductDisplayPagesPage.saveButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.successMessage
		).toBeVisible();
	}
);

test(
	'Add, edit and delete a product display page override',
	{tag: ['@COMMERCE-7842', '@COMMERCE-7951', '@COMMERCE-9433']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsProductDisplayPagesPage,
		commerceAdminChannelsPage,
		site,
	}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});
		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		const layout1 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			false
		);
		const layout2 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			false
		);

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.goToProductDisplayPages();

		// Save without a page

		await commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutButton.click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProduct(
			product1.name['en_US']
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.saveButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutFrame.getByText(
				'Please select a valid layout.'
			)
		).toBeVisible();

		await commerceAdminChannelDetailsProductDisplayPagesPage.closeButton.click();

		// Save without a product

		await commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutButton.click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectLayout(
			layout1.nameCurrentValue
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.saveButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutFrame.getByText(
				'Please select a valid product.'
			)
		).toBeVisible();

		await commerceAdminChannelDetailsProductDisplayPagesPage.closeButton.click();

		// Add the override

		await commerceAdminChannelDetailsProductDisplayPagesPage.addDisplayLayoutButton.click();

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectLayout(
			layout1.nameCurrentValue
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProduct(
			product1.name['en_US']
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.saveButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.successMessage
		).toBeVisible();

		await commerceAdminChannelDetailsProductDisplayPagesPage.closeButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.tableRow(
				product1.name['en_US']
			)
		).toContainText(layout1.nameCurrentValue);

		// Edit the override

		await commerceAdminChannelDetailsProductDisplayPagesPage.clickTableRowAction(
			product1.name['en_US'],
			'Edit'
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectLayout(
			layout2.nameCurrentValue
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.selectProduct(
			product2.name['en_US']
		);

		await commerceAdminChannelDetailsProductDisplayPagesPage.saveButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.successMessage
		).toBeVisible();

		await commerceAdminChannelDetailsProductDisplayPagesPage.closeButton.click();

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.tableRow(
				product1.name['en_US']
			)
		).toHaveCount(0);
		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.tableRow(
				product2.name['en_US']
			)
		).toContainText(layout2.nameCurrentValue);

		// Delete the override

		await commerceAdminChannelDetailsProductDisplayPagesPage.clickTableRowAction(
			product2.name['en_US'],
			'Delete'
		);

		await expect(
			commerceAdminChannelDetailsProductDisplayPagesPage.tableRow(
				product2.name['en_US']
			)
		).toHaveCount(0);
	}
);

test(
	'Default and override product display pages route to public and private pages',
	{
		tag: [
			'@COMMERCE-7838',
			'@COMMERCE-7839',
			'@COMMERCE-7840',
			'@COMMERCE-7841',
			'@COMMERCE-7842',
		],
	},
	async ({apiHelpers, page, site}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});
		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});
		const product3 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		const publicLayout1 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			false
		);
		const publicLayout2 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			false
		);
		const privateLayout1 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			true
		);
		const privateLayout2 = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			true
		);

		// Override a private default page with a public and a private page

		await apiHelpers.headlessCommerceAdminChannel.postDefaultProductDisplayPage(
			channel.id,
			{pageUuid: privateLayout1.uuid}
		);

		await apiHelpers.headlessCommerceAdminChannel.postProductDisplayPage(
			channel.id,
			{pageUuid: publicLayout1.uuid, productId: product1.id}
		);
		await apiHelpers.headlessCommerceAdminChannel.postProductDisplayPage(
			channel.id,
			{pageUuid: privateLayout2.uuid, productId: product2.id}
		);

		await _assertProductDisplayPage(page, site, product1, publicLayout1);
		await _assertProductDisplayPage(page, site, product2, privateLayout2);
		await _assertProductDisplayPage(page, site, product3, privateLayout1);

		// Override a public default page with a public and a private page

		await apiHelpers.headlessCommerceAdminChannel.postDefaultProductDisplayPage(
			channel.id,
			{pageUuid: publicLayout2.uuid}
		);

		await _assertProductDisplayPage(page, site, product1, publicLayout1);
		await _assertProductDisplayPage(page, site, product2, privateLayout2);
		await _assertProductDisplayPage(page, site, product3, publicLayout2);
	}
);

test(
	'A product display page template override wins over the default',
	{tag: '@COMMERCE-9433'},
	async ({
		apiHelpers,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		site,
	}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});
		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});
		const product3 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		const layout = await _addProductDetailsLayout(
			apiHelpers,
			site.id,
			false
		);

		const displayPageTemplate1 = await _addProductDisplayPageTemplate(
			apiHelpers,
			displayPageTemplatesPage,
			pageEditorPage,
			site,
			'Heading'
		);
		const displayPageTemplate2 = await _addProductDisplayPageTemplate(
			apiHelpers,
			displayPageTemplatesPage,
			pageEditorPage,
			site,
			'Paragraph'
		);

		const displayPageTemplate1Text = page.getByText('Heading Example');
		const displayPageTemplate2Text = page.getByText(
			'A paragraph is a self-contained unit'
		);

		await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.markAsDefaultDisplayPageLayoutPageTemplateEntry(
			{
				layoutPageTemplateEntryId:
					displayPageTemplate2.layoutPageTemplateEntryId,
			}
		);

		// Override the default display page template

		await apiHelpers.headlessCommerceAdminChannel.postProductDisplayPage(
			channel.id,
			{
				pageTemplateUuid: displayPageTemplate1.uuid,
				productId: product1.id,
			}
		);
		await apiHelpers.headlessCommerceAdminChannel.postProductDisplayPage(
			channel.id,
			{pageUuid: layout.uuid, productId: product3.id}
		);

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product1.urls['en_US']}`
		);

		await expect(displayPageTemplate1Text).toBeVisible();

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product2.urls['en_US']}`
		);

		await expect(displayPageTemplate2Text).toBeVisible();

		await _assertProductDisplayPage(page, site, product3, layout);

		await expect(displayPageTemplate2Text).toHaveCount(0);

		// Set a default product display page

		await apiHelpers.headlessCommerceAdminChannel.postDefaultProductDisplayPage(
			channel.id,
			{pageUuid: layout.uuid}
		);

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product1.urls['en_US']}`
		);

		await expect(displayPageTemplate1Text).toBeVisible();
	}
);

async function _addProductDetailsLayout(
	apiHelpers,
	groupId: string,
	privateLayout: boolean
) {
	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId,
		privateLayout: String(privateLayout),
		title: getRandomString(),
		typeSettings:
			'layout-template-id=1_column\ncolumn-1=com_liferay_commerce_product_content_web_internal_portlet_CPContentPortlet\n',
	});

	return {...layout, privateLayout};
}

async function _addProductDisplayPageTemplate(
	apiHelpers,
	displayPageTemplatesPage,
	pageEditorPage,
	site,
	fragmentName: string
) {
	const className = await apiHelpers.jsonWebServicesClassName.fetchClassName(
		'com.liferay.commerce.product.model.CPDefinition'
	);

	const name = getRandomString();

	const displayPageTemplate =
		await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.addDisplayPageLayoutPageTemplateEntry(
			{
				classNameId: className.classNameId,
				groupId: site.id,
				name,
			}
		);

	apiHelpers.data.push({
		id: displayPageTemplate.layoutPageTemplateEntryId,
		type: 'layoutPageTemplateEntry',
	});

	await displayPageTemplatesPage.goto(site.friendlyUrlPath);

	await displayPageTemplatesPage.editTemplate(name);

	await pageEditorPage.addFragment('Basic Components', fragmentName);

	await displayPageTemplatesPage.publishTemplate();

	return displayPageTemplate;
}

async function _assertProductDisplayPage(page: Page, site, product, layout) {
	await page.goto(
		`/${layout.privateLayout ? 'group' : 'web'}${site.friendlyUrlPath}/p/${product.urls['en_US']}`
	);

	await expect(page).toHaveTitle(new RegExp(layout.nameCurrentValue));
	await expect(
		page.getByText(product.name['en_US'], {exact: true}).first()
	).toBeVisible();
}

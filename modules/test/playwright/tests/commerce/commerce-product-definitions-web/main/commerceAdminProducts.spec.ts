/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {serverAdministrationPageTest} from '../../../../fixtures/serverAdministrationPageTest';
import {userPersonalBarPagesTest} from '../../../../fixtures/userPersonalBarPagesTest';
import {EActions} from '../../../../helpers/ServerAdministrationHelper';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import {userData} from '../../../../utils/performLogin';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	serverAdministrationPageTest,
	userPersonalBarPagesTest
);

test(
	'Add a SKU',
	{tag: '@COMMERCE-6021'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			'BLACKSKU'
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPurchasableToggle.check();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();
	}
);

test(
	'Back button works as expected',
	{tag: '@LPD-43791'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
		site,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.backLink.click();

		await expect(
			commerceAdminProductPage.productsTableRowLink(product.name['en_US'])
		).toBeVisible();

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await page.goto(
			`${page.url()}&_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_backURL=${site.friendlyUrlPath}`
		);

		await commerceAdminProductDetailsPage.backLink.click();

		await expect(
			commerceAdminProductPage.productsTableRowLink(product.name['en_US'])
		).toHaveCount(0);
	}
);

test(
	'Add a SKU with subscriptions',
	{tag: ['@COMMERCE-6024', '@COMMERCE-11150', '@LPD-108844']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
				name: 'Master',
			});

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Simple T-Shirt'},
				productType: 'simple',
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddButton
		).toHaveCount(1);

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			'BLACKSKU'
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();

		await (
			await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(false)
		).click();

		await expect(
			await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(false)
		).toBeHidden();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink('BLACKSKU')
			.click();

		await commerceAdminProductDetailsSkusPage.goToSkuTab('Subscriptions');

		const overrideToggle =
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByLabel(
				'Override Subscription Settings'
			);

		await overrideToggle.check();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Payment Subscription'
			)
		).toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Delivery Subscription'
			)
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage
			.sidePanelSubscriptionEnableToggle('Delivery Subscription')
			.click();
		await commerceAdminProductDetailsSkusPage
			.sidePanelSubscriptionSelect(
				'Delivery Subscription',
				'Subscription Type'
			)
			.selectOption('Year');
		await commerceAdminProductDetailsSkusPage
			.sidePanelSubscriptionSelect('Delivery Subscription', 'Mode')
			.selectOption('Exact Day of Year');
		await commerceAdminProductDetailsSkusPage
			.sidePanelSubscriptionSelect('Delivery Subscription', 'Month')
			.selectOption('March');
		await commerceAdminProductDetailsSkusPage
			.sidePanelSubscriptionInput('Delivery Subscription', 'Day')
			.fill('5');

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Success:Your request completed successfully.'
			)
		).toBeVisible();

		await (
			await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(false)
		).click();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink('BLACKSKU')
			.click();

		await commerceAdminProductDetailsSkusPage.goToSkuTab('Subscriptions');

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSelect(
				'Delivery Subscription',
				'Subscription Type'
			)
		).toHaveValue('yearly');
		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSelect(
				'Delivery Subscription',
				'Mode'
			)
		).toHaveValue('1');
		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSelect(
				'Delivery Subscription',
				'Month'
			)
		).toHaveValue('2');
		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelSubscriptionInput(
				'Delivery Subscription',
				'Day'
			)
		).toHaveValue('5');
		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelSubscriptionInput(
				'Delivery Subscription',
				'Subscription Length'
			)
		).toHaveValue('1');

		await overrideToggle.uncheck();

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Success:Your request completed successfully.'
			)
		).toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Payment Subscription'
			)
		).not.toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Delivery Subscription'
			)
		).not.toBeVisible();
	}
);

test(
	'Currency changes based on price lists',
	{tag: '@LPD-52938'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const productSkus = await apiHelpers.headlessCommerceAdminCatalog
			.getProduct(product.productId)
			.then((product) => {
				return product.skus;
			});

		const currencies =
			await apiHelpers.headlessCommerceAdminCatalog.getCurrenciesPage('');

		const currencyEUR = currencies.items.find(
			(item) => item.name['en_US'] === 'Euro'
		);
		const currencyUSD = currencies.items.find(
			(item) => item.name['en_US'] === 'US Dollar'
		);

		const priceListEUR =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: currencyEUR.code,
				name: 'EUR-pl',
				type: 'price-list',
			});
		const priceListUSD =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: currencyUSD.code,
				name: 'USD-pl',
				type: 'price-list',
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(`${productSkus[0].sku}`)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();
		await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
			priceListEUR.name
		);

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceAddModal.getByText(
				'EUR',
				{exact: true}
			)
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
			priceListUSD.name
		);

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceAddModal.getByText(
				'USD',
				{exact: true}
			)
		).toBeVisible();
	}
);

test(
	'Add price entries from several price lists to a SKU',
	{tag: ['@COMMERCE-11084', '@LPD-110416']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const productDetails =
			await apiHelpers.headlessCommerceAdminCatalog.getProduct(
				product.productId
			);

		const priceList1 =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: 'USD',
				name: getRandomString(),
				type: 'price-list',
			});
		const priceList2 =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: 'USD',
				name: getRandomString(),
				type: 'price-list',
			});

		await test.step('Open the add price dialog', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(productDetails.skus[0].sku)
				.click();
			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
			await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();
		});

		await test.step('Add a price entry for each price list', async () => {
			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(0)
				.selectOption(priceList1.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(0)
				.fill('10');

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddEntryButton.click();

			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(1)
				.selectOption(priceList2.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(1)
				.fill('20');

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddButton.click();
		});

		await test.step('Check the dialog closes and the list refreshes', async () => {
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput.first()
			).toBeHidden();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList1.name
				)
			).toContainText('$ 10.00');
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList2.name
				)
			).toContainText('$ 20.00');
		});
	}
);

test(
	'The add price dialog shows the unit of measure fields of the SKU and adds an entry for each one',
	{tag: ['@COMMERCE-12289', '@COMMERCE-12290', '@LPD-108844']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const {catalog, priceList1, priceList2, product, sku} =
			await test.step('Create a product with a SKU priced 50 and two price lists', async () => {
				const catalog =
					await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

				const product =
					await apiHelpers.headlessCommerceAdminCatalog.postProduct({
						catalogId: catalog.id,
						skus: [
							{
								cost: 0,
								price: 50,
								published: true,
								purchasable: true,
								sku: `SKU-${getRandomInt()}`,
							},
						],
					});

				const productDetails =
					await apiHelpers.headlessCommerceAdminCatalog.getProduct(
						product.productId
					);

				const priceList1 =
					await apiHelpers.headlessCommerceAdminPricing.postPriceList(
						{
							catalogId: catalog.id,
							currencyCode: 'USD',
							name: getRandomString(),
							type: 'price-list',
						}
					);
				const priceList2 =
					await apiHelpers.headlessCommerceAdminPricing.postPriceList(
						{
							catalogId: catalog.id,
							currencyCode: 'EUR',
							name: getRandomString(),
							type: 'price-list',
						}
					);

				return {
					catalog,
					priceList1,
					priceList2,
					product,
					sku: productDetails.skus[0],
				};
			});

		await test.step('Without units of measure the dialog shows the SKU price and no unit of measure field', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku.sku)
				.click();
			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRowLink(
					`${catalog.name} Base Price List`
				)
			).toBeVisible();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList1.name
				)
			).toHaveCount(0);
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList2.name
				)
			).toHaveCount(0);

			await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceListSelect
			).toBeVisible();
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
			).toHaveCount(0);
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
			).toHaveValue('50');

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalCancelButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
			).toBeHidden();
		});

		await test.step('Add two units of measure to the SKU', async () => {
			await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
				sku.id,
				{
					basePrice: 1,
					key: 'uom1',
					name: {en_US: 'uom1'},
					primary: true,
					priority: 1,
				}
			);
			await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
				sku.id,
				{
					basePrice: 2,
					key: 'uom2',
					name: {en_US: 'uom2'},
					primary: false,
					priority: 2,
				}
			);
		});

		await test.step('With units of measure the dialog preselects the first one and its price', async () => {
			await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
			).toHaveValue('uom1');
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
			).toHaveValue('1');
		});

		await test.step('Add an entry for each unit of measure on a different price list', async () => {
			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(0)
				.selectOption(priceList1.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
				.nth(0)
				.selectOption('uom1');
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(0)
				.fill('30');

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddEntryButton.click();

			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(1)
				.selectOption(priceList2.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
				.nth(1)
				.selectOption('uom2');
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(1)
				.fill('90');

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput.first()
			).toBeHidden();
		});

		await test.step('Check the entries show their unit of measure and the price in the price list currency', async () => {
			const priceList1Row =
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList1.name
				);

			await expect(priceList1Row).toHaveCount(1);
			await expect(priceList1Row).toContainText('uom1');
			await expect(priceList1Row).toContainText('$ 30.00');

			const priceList2Row =
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList2.name
				);

			await expect(priceList2Row).toHaveCount(1);
			await expect(priceList2Row).toContainText('uom2');
			await expect(priceList2Row).toContainText('€ 90.00');
		});
	}
);

test(
	'Delete a SKU from the SKUs table',
	{tag: ['@COMMERCE-6023', '@LPD-108844']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const skuCodes = [`SKU-${getRandomInt()}`, `SKU-${getRandomInt()}`];

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				skus: skuCodes.map((skuCode) => ({
					cost: 0,
					price: 10,
					published: true,
					purchasable: true,
					sku: skuCode,
				})),
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowActionsButton(skuCodes[0])
			.click();
		await commerceAdminProductDetailsSkusPage.menuItem('Delete').click();

		await waitForAlert(page);

		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink(skuCodes[0])
		).toHaveCount(0);
		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink(skuCodes[1])
		).toBeVisible();
	}
);

test(
	'Edit the external reference code of a SKU and reject one already used by another SKU',
	{tag: ['@COMMERCE-9891', '@LPD-108844']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const skuCodes = [`SKU-${getRandomInt()}`, `SKU-${getRandomInt()}`];

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				skus: skuCodes.map((skuCode) => ({
					cost: 0,
					price: 10,
					published: true,
					purchasable: true,
					sku: skuCode,
				})),
			});

		const externalReferenceCode = getRandomString();

		await test.step('Edit the external reference code of a SKU', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(skuCodes[0])
				.click();

			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput.fill(
				externalReferenceCode
			);

			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await (
				await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(
					false
				)
			).click();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(skuCodes[0])
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput
			).toHaveValue(externalReferenceCode);
		});

		await test.step('The same external reference code is rejected on another SKU', async () => {
			await (
				await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(
					false
				)
			).click();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(skuCodes[1])
				.click();

			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput.fill(
				externalReferenceCode
			);

			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame,
				'Please enter a unique external reference code.',
				{type: 'danger'}
			);

			const productDetails =
				await apiHelpers.headlessCommerceAdminCatalog.getProduct(
					product.productId
				);

			const sku = productDetails.skus.find(
				(sku) => sku.sku === skuCodes[1]
			);

			expect(sku.externalReferenceCode).not.toBe(externalReferenceCode);
		});
	}
);

test(
	'Edit and delete the price entries and the tier prices of a SKU from the Price tab',
	{tag: ['@COMMERCE-12182', '@COMMERCE-12218', '@LPD-108844']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				skus: [
					{
						cost: 0,
						price: 10,
						published: true,
						purchasable: true,
						sku: `SKU-${getRandomInt()}`,
					},
				],
			});

		const productDetails =
			await apiHelpers.headlessCommerceAdminCatalog.getProduct(
				product.productId
			);

		const sku = productDetails.skus[0];

		const priceList =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: 'EUR',
				name: getRandomString(),
				type: 'price-list',
			});

		await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
			price: 15,
			priceListId: priceList.id,
			skuId: sku.id,
		});

		const basePriceListName = `${catalog.name} Base Price List`;

		await test.step('Edit the price of the base price list entry', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku.sku)
				.click();

			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');

			await commerceAdminProductDetailsSkusPage.openPriceEntry(
				basePriceListName
			);

			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice.fill(
				'25'
			);

			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice
			).toHaveValue('25.00');
		});

		await test.step('Adding a tier price keeps the bulk pricing setting', async () => {
			await commerceAdminProductDetailsSkusPage.sidePanelNestedBulkPricingRadio.check();

			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await commerceAdminProductDetailsSkusPage.addPriceTier('2', '20');

			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedBulkPricingRadio
			).toBeChecked();
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'2'
				)
			).toContainText('$ 20.00');
		});

		await test.step('Adding a tier price keeps the tiered pricing setting', async () => {
			await commerceAdminProductDetailsSkusPage.sidePanelNestedTieredPricingRadio.check();

			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await commerceAdminProductDetailsSkusPage.addPriceTier('5', '15');

			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedTieredPricingRadio
			).toBeChecked();
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'5'
				)
			).toContainText('$ 15.00');
		});

		await test.step('Edit a tier price', async () => {
			await commerceAdminProductDetailsSkusPage
				.sidePanelNestedPriceTierTableRowActionsButton('2')
				.click();
			await commerceAdminProductDetailsSkusPage
				.sidePanelNestedMenuItem('Edit')
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.priceTierModalPriceInput
			).toHaveValue('20.00');

			await commerceAdminProductDetailsSkusPage.priceTierModalPriceInput.fill(
				'18'
			);

			await commerceAdminProductDetailsSkusPage.priceTierModalSubmitButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.priceTierModalPriceInput
			).toBeHidden();

			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'2'
				)
			).toContainText('$ 18.00');
		});

		await test.step('Delete a tier price', async () => {
			await commerceAdminProductDetailsSkusPage
				.sidePanelNestedPriceTierTableRowActionsButton('5')
				.click();
			await commerceAdminProductDetailsSkusPage
				.sidePanelNestedMenuItem('Delete')
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'5'
				)
			).toHaveCount(0);

			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'5'
				)
			).toHaveCount(0);
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceTierTableRow(
					'2'
				)
			).toContainText('$ 18.00');
		});

		await test.step('The price entry shows the currency of its price list', async () => {
			await commerceAdminProductDetailsSkusPage.reopenPriceEntry(
				priceList.name
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame.getByText(
					'EUR',
					{exact: true}
				)
			).toBeVisible();

			await (
				await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(
					true
				)
			).click();
		});

		await test.step('Delete a price entry from the Price tab', async () => {
			await clickAndExpectToBeVisible({
				target: commerceAdminProductDetailsSkusPage.skuUOMActionMenuItem(
					'Delete'
				),
				trigger:
					commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRowActionsButton(
						priceList.name
					),
			});

			await commerceAdminProductDetailsSkusPage
				.skuUOMActionMenuItem('Delete')
				.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					priceList.name
				)
			).toHaveCount(0);
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTableRow(
					basePriceListName
				)
			).toContainText('$ 25.00');
		});
	}
);

test(
	'Show the subscription length unit and save the subscription settings of a product',
	{
		tag: [
			'@COMMERCE-6085',
			'@COMMERCE-9744',
			'@COMMERCE-9798',
			'@COMMERCE-9806',
			'@COMMERCE-11150',
			'@LPD-108844',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});
		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const subscriptionNames = [
			'Payment Subscription',
			'Delivery Subscription',
		];

		await test.step('Enabling the subscriptions shows the default settings', async () => {
			await commerceAdminProductPage.gotoProduct(product1.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSubscription();

			for (const subscriptionName of subscriptionNames) {
				await commerceAdminProductDetailsPage
					.subscriptionEnableToggle(subscriptionName)
					.click();

				await expect(
					commerceAdminProductDetailsPage.subscriptionSelect(
						subscriptionName,
						'Subscription Type'
					)
				).toHaveValue('daily');
				await expect(
					commerceAdminProductDetailsPage.subscriptionInput(
						subscriptionName,
						'Subscription Length'
					)
				).toHaveValue('1');
				await expect(
					commerceAdminProductDetailsPage.subscriptionLengthUnit(
						subscriptionName
					)
				).toHaveText('Day');
			}
		});

		await test.step('Changing the subscription type updates the subscription length unit', async () => {
			for (const subscriptionName of subscriptionNames) {
				for (const subscriptionType of ['Week', 'Month', 'Year']) {
					await commerceAdminProductDetailsPage
						.subscriptionSelect(
							subscriptionName,
							'Subscription Type'
						)
						.selectOption(subscriptionType);

					await expect(
						commerceAdminProductDetailsPage.subscriptionLengthUnit(
							subscriptionName
						)
					).toHaveText(subscriptionType);
				}
			}
		});

		await test.step('The saved subscription length shows the singular or plural unit', async () => {
			for (const savedSubscriptions of [
				[
					{
						length: '1',
						name: 'Payment Subscription',
						type: 'Week',
						unit: 'Week',
					},
					{
						length: '2',
						name: 'Delivery Subscription',
						type: 'Month',
						unit: 'Months',
					},
				],
				[
					{
						length: '2',
						name: 'Payment Subscription',
						type: 'Year',
						unit: 'Years',
					},
					{
						length: '1',
						name: 'Delivery Subscription',
						type: 'Day',
						unit: 'Day',
					},
				],
			]) {
				for (const {length, name, type} of savedSubscriptions) {
					await commerceAdminProductDetailsPage.setSubscription(
						name,
						type,
						length
					);
				}

				await commerceAdminProductDetailsPage.publishLink.click();

				await waitForAlert(page);

				for (const {name, unit} of savedSubscriptions) {
					await expect(
						commerceAdminProductDetailsPage.subscriptionLengthUnit(
							name
						)
					).toHaveText(unit);
				}
			}
		});

		const deliverySubscriptionSettings = {
			inputValues: {'Day': '5', 'Subscription Length': '5'},
			selectValues: {
				'Mode': '1',
				'Month': '2',
				'Subscription Type': 'yearly',
			},
		};

		await test.step('The exact day of year of the delivery subscription is saved', async () => {
			await commerceAdminProductPage.gotoProduct(product2.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSubscription();

			await commerceAdminProductDetailsPage
				.subscriptionEnableToggle('Delivery Subscription')
				.click();

			await commerceAdminProductDetailsPage.setSubscription(
				'Delivery Subscription',
				'Year',
				'5'
			);

			await commerceAdminProductDetailsPage
				.subscriptionSelect('Delivery Subscription', 'Mode')
				.selectOption('Exact Day of Year');
			await commerceAdminProductDetailsPage
				.subscriptionSelect('Delivery Subscription', 'Month')
				.selectOption('March');
			await commerceAdminProductDetailsPage
				.subscriptionInput('Delivery Subscription', 'Day')
				.fill('5');

			await commerceAdminProductDetailsPage.publishLink.click();

			await waitForAlert(page);

			await commerceAdminProductDetailsPage.assertSubscriptionSettings(
				'Delivery Subscription',
				deliverySubscriptionSettings
			);
		});

		await test.step('Saving a yearly payment subscription keeps both subscription settings', async () => {
			await commerceAdminProductDetailsPage
				.subscriptionEnableToggle('Payment Subscription')
				.click();

			await expect(
				commerceAdminProductDetailsPage.subscriptionSelect(
					'Payment Subscription',
					'Subscription Type'
				)
			).toHaveValue('daily');

			await commerceAdminProductDetailsPage.setSubscription(
				'Payment Subscription',
				'Year',
				'1'
			);

			await commerceAdminProductDetailsPage.publishLink.click();

			await waitForAlert(page);

			await expect(
				commerceAdminProductDetailsPage.subscriptionSelect(
					'Payment Subscription',
					'Subscription Type'
				)
			).toHaveValue('yearly');
			await expect(
				commerceAdminProductDetailsPage.subscriptionSelect(
					'Payment Subscription',
					'Mode'
				)
			).toHaveValue('0');

			await commerceAdminProductDetailsPage.assertSubscriptionSettings(
				'Delivery Subscription',
				deliverySubscriptionSettings
			);
		});
	}
);

test(
	'Product name should be created for both user language and instance language',
	{tag: '@LPD-64086'},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
		const user =
			await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
				'test@liferay.com'
			);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		userData[user.alternateName] = {
			name: user.givenName,
			password: 'test',
			surname: user.familyName,
		};

		const role =
			await apiHelpers.headlessAdminUser.getRoleByName('Administrator');

		await apiHelpers.headlessAdminUser.assignUserToRole(
			role.externalReferenceCode,
			user.id
		);

		await commerceAdminProductPage.goto();

		await apiHelpers.headlessAdminUser.patchUserAccount(user, {
			languageId: 'es_ES',
		});

		await page.reload();

		const translateLink = page.getByRole('link', {
			name: 'Mostrar la página en español (España).',
		});

		const inEnglish = await translateLink.isVisible();

		if (inEnglish) {
			await translateLink.click();
		}

		await commerceAdminProductPage.addButton.click();
		await commerceAdminProductPage.menuItemProductType('Simple').click();

		await expect(
			commerceAdminProductPage.modalFrameLocator.getByText(
				'Crear nuevo producto'
			)
		).toBeVisible();

		const productName = getRandomString();

		await commerceAdminProductPage.modalFrameLocator
			.getByLabel('Nombre Requerido')
			.fill(productName);
		await commerceAdminProductPage.modalFrameLocator
			.getByPlaceholder('Escriba aquí')
			.fill(catalog.name);
		await commerceAdminProductPage.modalMenuItem(catalog.name).click();
		await commerceAdminProductPage.modalFrameLocator
			.getByRole('button', {
				exact: true,
				name: 'Enviar',
			})
			.click();

		await expect(page.getByText(productName)).toBeVisible();

		const product = (
			await apiHelpers.headlessCommerceAdminCatalog.getProducts(
				new URLSearchParams({
					filter: `name eq '${productName}'`,
				})
			)
		).items[0];

		const productNameUS = product.name['en_US'];
		const productNameES = product.name['es_ES'];

		expect(productNameUS).toBe(productName);
		expect(productNameES).toBe(productName);

		await apiHelpers.headlessAdminUser.patchUserAccount(user, {
			languageId: 'en_US',
		});
	}
);

test(
	'Creating product with pending status in headless triggers workflow notification',
	{tag: ['@LPD-73496']},
	async ({
		apiHelpers,
		commerceAdminProductPage,
		page,
		userPersonalBarPage,
	}) => {
		await userPersonalBarPage.goToProcessBuilderConfigurationTab();
		await userPersonalBarPage.enableSingleApproverWorkflowProduct();

		try {
			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					productStatus: 1,
				});

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await expect(page.getByText('Assigned to:')).toBeVisible();
			await expect(userPersonalBarPage.notificationBadge).toBeVisible();
		}
		finally {
			await userPersonalBarPage.disableSingleApproverWorkflowProduct();
		}
	}
);

test(
	'Add SKU with duplicate values and confirm warning message shows',
	{tag: '@LPD-90070'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product2.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			product1.skus[0].sku.toUpperCase()
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPurchasableToggle.check();

		page.once('dialog', async (dialog) => {
			expect(dialog.message()).toContain('The SKU is already in use');

			await dialog.accept();
		});

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();
	}
);

test(
	'Product fields default to the catalog default language',
	{tag: ['@COMMERCE-9532', '@LPD-103842']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
				defaultLanguageId: 'es_ES',
				name: getRandomString(),
			});

		const productName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: {en_US: productName},
		});

		await commerceAdminProductPage.gotoProduct(productName);

		await expect(
			commerceAdminProductDetailsPage.nameInputLocaleSelector
		).toHaveText('es-ES');
	}
);

test(
	'Cannot publish a product with empty or zero order quantities',
	{tag: ['@COMMERCE-10173', '@LPD-107429']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsConfigurationPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
	}) => {
		const defaultOrderQuantities = {
			'Maximum Order Quantity': '10000.0',
			'Minimum Order Quantity': '1.0',
			'Multiple Order Quantity': '1.0',
		};

		await test.step('Open the configuration of a new product', async () => {
			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
				});

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductConfiguration();

			for (const [
				orderQuantityLabel,
				orderQuantityValue,
			] of Object.entries(defaultOrderQuantities)) {
				await expect(
					commerceAdminProductDetailsConfigurationPage.orderQuantityInput(
						orderQuantityLabel
					)
				).toHaveValue(orderQuantityValue);
			}
		});

		await test.step('Publishing empty order quantities shows the required errors', async () => {
			for (const orderQuantityLabel of Object.keys(
				defaultOrderQuantities
			)) {
				await commerceAdminProductDetailsConfigurationPage
					.orderQuantityInput(orderQuantityLabel)
					.clear();
			}

			await commerceAdminProductDetailsConfigurationPage.publishLink.click();

			await expect(
				commerceAdminProductDetailsConfigurationPage.orderQuantityInput(
					'Minimum Order Quantity'
				)
			).toBeFocused();

			for (const orderQuantityLabel of Object.keys(
				defaultOrderQuantities
			)) {
				await expect(
					commerceAdminProductDetailsConfigurationPage.orderQuantityErrorMessage(
						orderQuantityLabel
					)
				).toHaveText(`The ${orderQuantityLabel} field is required.`);
			}
		});

		await test.step('Publishing zero order quantities shows the minimum value errors', async () => {
			for (const orderQuantityLabel of Object.keys(
				defaultOrderQuantities
			)) {
				await commerceAdminProductDetailsConfigurationPage
					.orderQuantityInput(orderQuantityLabel)
					.fill('0');
			}

			await commerceAdminProductDetailsConfigurationPage.publishLink.click();

			await expect(
				commerceAdminProductDetailsConfigurationPage.orderQuantityInput(
					'Minimum Order Quantity'
				)
			).toBeFocused();

			for (const orderQuantityLabel of Object.keys(
				defaultOrderQuantities
			)) {
				await expect(
					commerceAdminProductDetailsConfigurationPage.orderQuantityErrorMessage(
						orderQuantityLabel
					)
				).toHaveText(
					'Please enter a value greater than or equal to 0.000001.'
				);
			}
		});

		await test.step('The order quantities are not saved', async () => {
			await page.reload();

			for (const [
				orderQuantityLabel,
				orderQuantityValue,
			] of Object.entries(defaultOrderQuantities)) {
				await expect(
					commerceAdminProductDetailsConfigurationPage.orderQuantityInput(
						orderQuantityLabel
					)
				).toHaveValue(orderQuantityValue);
			}
		});
	}
);

test(
	'Update the inventory of a SKU with a zero reserved quantity',
	{tag: '@LPD-113063'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		serverAdministrationPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const sku1 = getRandomString();
		const sku2 = getRandomString();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				skus: [
					{
						cost: 0,
						price: 0,
						published: true,
						purchasable: true,
						sku: sku1,
					},
					{
						cost: 0,
						price: 0,
						published: true,
						purchasable: true,
						sku: sku2,
					},
				],
			});

		const skuUnitOfMeasure =
			await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
				product.skus.find((sku) => sku.sku === sku2).id,
				{
					precision: 7,
					primary: true,
				}
			);

		const warehouse =
			await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehouses(
				{
					active: true,
					latitude: getRandomInt(),
					longitude: getRandomInt(),
					warehouseItems: [
						{
							quantity: 5,
							sku: sku1,
						},
						{
							quantity: 5,
							sku: sku2,
							unitOfMeasureKey: skuUnitOfMeasure.key,
						},
					],
				}
			);

		await test.step('Update the inventory of a SKU after its reserved quantity is reloaded from the database', async () => {
			await serverAdministrationPage.goto();

			await serverAdministrationPage.executeAction(
				EActions.CLEAR_DATABASE_CACHE
			);

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku1)
				.click();

			await commerceAdminProductDetailsSkusPage.goToSkuTab('Inventory');

			await commerceAdminProductDetailsSkusPage.addWarehouseQuantity(
				'10',
				warehouse.name['en_US']
			);

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				await commerceAdminProductDetailsSkusPage.inventoryTableRowAction(
					warehouse.name['en_US']
				)
			).toHaveValue('10');
		});

		await test.step('Update the inventory of a SKU whose unit of measure has a precision greater than six', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku2)
				.click();

			await commerceAdminProductDetailsSkusPage.goToSkuTab('Inventory');

			await commerceAdminProductDetailsSkusPage.addWarehouseQuantity(
				'10',
				warehouse.name['en_US']
			);

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				await commerceAdminProductDetailsSkusPage.inventoryTableRowAction(
					warehouse.name['en_US']
				)
			).toHaveValue('10.0000000');
		});
	}
);

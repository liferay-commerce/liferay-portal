/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {CommerceMiniCartPage} from '../../../../pages/commerce/commerceMiniCartPage';
import {
	performLoginViaApi,
	performLogout,
} from '../../../../utils/performLogin';
import {
	createAccountWithBuyerUser,
	miniumSetUp,
	patchUnitOfMeasureWarehouseItems,
	setUpStockedUnitOfMeasures,
} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

let catalog: {id: number; name: string};
let setupData: Array<{id: number | string; type: string}>;
let site: Site;

test.beforeAll(async ({browser}) => {
	const page = await browser.newPage();

	await performLoginViaApi({page, screenName: 'test'});

	const apiHelpers = new DataApiHelpers(page);

	const miniumResult = await miniumSetUp(apiHelpers);

	catalog = miniumResult.catalog;
	site = miniumResult.site;

	setupData = [...apiHelpers.data];

	await page.close();
});

test.afterAll(async ({browser}) => {
	const page = await browser.newPage();

	await performLoginViaApi({page, screenName: 'test'});

	const apiHelpers = new DataApiHelpers(page);

	apiHelpers.setData(setupData);

	await apiHelpers.clearData();

	await page.close();
});

test.afterEach(async ({page}) => {
	await performLoginViaApi({page, screenName: 'test'});
});

async function quickAddAndEdit(
	commerceMiniCartPage: CommerceMiniCartPage,
	page: Page,
	skuName: string
) {
	await page.goto(`/web${site.friendlyUrlPath}/catalog`, {
		waitUntil: 'networkidle',
	});

	await commerceMiniCartPage.quickAddToCart(skuName);

	await commerceMiniCartPage.open();

	await commerceMiniCartPage
		.miniCartItemActionsButton(
			commerceMiniCartPage.miniCartItemForSku(skuName)
		)
		.click();

	await commerceMiniCartPage.editMenuItem.click();

	await expect(commerceMiniCartPage.miniCartEditItemPanel).toBeVisible();
}

async function setUpThreeUnitsOfMeasure(
	apiHelpers: DataApiHelpers,
	stockedUnitOfMeasureKeys: string[]
) {
	const {product, sku, unitsOfMeasure} = await setUpStockedUnitOfMeasures(
		apiHelpers,
		catalog.id,
		{
			productConfiguration: {
				allowBackOrder: false,
				minOrderQuantity: 0.01,
				multipleOrderQuantity: 0.01,
			},
			unitsOfMeasure: [
				{
					active: false,
					basePrice: 1,
					incrementalOrderQuantity: 1,
					key: 'uom1',
					name: {en_US: 'UOM1'},
					primary: true,
					priority: 1,
				},
				{
					basePrice: 2,
					incrementalOrderQuantity: 0.6,
					key: 'uom2',
					name: {en_US: 'UOM2'},
					priority: 2,
				},
				{
					basePrice: 3,
					incrementalOrderQuantity: 2,
					key: 'uom3',
					name: {en_US: 'UOM3'},
					priority: 3,
				},
			],
			warehouseQuantities: [['Italy', 0]],
		}
	);

	await patchUnitOfMeasureWarehouseItems(apiHelpers, {
		skuName: sku.sku,
		unitOfMeasureKeys: stockedUnitOfMeasureKeys,
		warehouseQuantities: [['Italy', 10]],
	});

	return {product, sku, unitsOfMeasure};
}

test(
	'Every pricing type of a unit of measure resolves in the mini cart edit panel',
	{tag: ['@COMMERCE-12619', '@LPD-107107']},
	async ({apiHelpers, commerceMiniCartPage, page}) => {
		const {sku, unitsOfMeasure} = await setUpStockedUnitOfMeasures(
			apiHelpers,
			catalog.id,
			{
				productConfiguration: {
					allowBackOrder: true,
					minOrderQuantity: 0.01,
					multipleOrderQuantity: 0.01,
				},
				unitsOfMeasure: [
					{
						basePrice: 45,
						incrementalOrderQuantity: 1.5,
						key: 'uom1',
						name: {en_US: 'UOM1'},
						primary: true,
						priority: 1,
					},
					{
						basePrice: 15.5,
						incrementalOrderQuantity: 0.6,
						key: 'uom2',
						name: {en_US: 'UOM2'},
						priority: 2,
					},
					{
						basePrice: 80,
						incrementalOrderQuantity: 2,
						key: 'uom3',
						name: {en_US: 'UOM3'},
						priority: 3,
						promoPrice: 72,
					},
				],
				warehouseQuantities: [['Italy', 100]],
			}
		);

		await test.step('Give the second unit of measure a tier price and the first one a discount', async () => {
			const basePriceList =
				await apiHelpers.headlessCommerceAdminPricing.getBasePriceList(
					catalog.id
				);

			const priceEntries =
				await apiHelpers.headlessCommerceAdminPricing.getPriceListEntries(
					basePriceList.items[0].id
				);

			const unitOfMeasureTwoPriceEntry = priceEntries.items.find(
				(priceEntry: {skuId: number; unitOfMeasureKey: string}) =>
					priceEntry.skuId === sku.id &&
					priceEntry.unitOfMeasureKey === 'uom2'
			);

			await apiHelpers.headlessCommerceAdminPricing.postTierPrice(
				unitOfMeasureTwoPriceEntry.priceEntryId,
				{
					minimumQuantity: 6,
					price: 12,
					unitOfMeasureKey: 'uom2',
				}
			);

			const discount =
				await apiHelpers.headlessCommerceAdminPricing.postDiscount({
					active: true,
					level: 'L1',
					percentageLevel1: 10,
					target: 'skus',
					usePercentage: false,
				});

			await apiHelpers.headlessCommerceAdminPricing.postDiscountSku(
				discount.id,
				{skuId: sku.id, unitOfMeasureKey: 'uom1'}
			);
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(`/web${site.friendlyUrlPath}/catalog`, {
			waitUntil: 'networkidle',
		});

		await commerceMiniCartPage.quickAddToCart(sku.sku);

		await test.step('Open the edit panel of the cart item', async () => {
			await commerceMiniCartPage.open();

			await commerceMiniCartPage
				.miniCartItemActionsButton(
					commerceMiniCartPage.miniCartItemForSku(sku.sku)
				)
				.click();

			await commerceMiniCartPage.editMenuItem.click();

			await expect(
				commerceMiniCartPage.miniCartEditItemPanel
			).toBeVisible();
		});

		const selectUnitOfMeasure = async (
			unitOfMeasureKey: string,
			quantity: string
		) => {
			await commerceMiniCartPage.miniCartUnitOfMeasureSelector.selectOption(
				unitOfMeasureKey
			);

			await commerceMiniCartPage.miniCartEditItemQuantitySelector.fill(
				quantity
			);
		};

		await test.step('A tier price applies above its minimum quantity', async () => {
			await selectUnitOfMeasure('uom2', '3');

			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('List Price')
			).toHaveText('$ 15.50');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice(
					'Price as Configured'
				)
			).toHaveText('$ 77.50');

			await commerceMiniCartPage.miniCartEditItemQuantitySelector.fill(
				'6'
			);

			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('List Price')
			).toHaveText('$ 14.92');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice(
					'Price as Configured'
				)
			).toHaveText('$ 149.17');
		});

		await test.step('A discount applies to the unit of measure it targets', async () => {
			await selectUnitOfMeasure('uom1', '3');

			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('List Price')
			).toHaveText('$ 45.00');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('Discount')
			).toHaveText('-22.22%');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice(
					'Price as Configured'
				)
			).toHaveText('$ 70.00');
		});

		await test.step('A promotion price applies to the unit of measure that carries it', async () => {
			await selectUnitOfMeasure('uom3', '6');

			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('List Price')
			).toHaveText('$ 80.00');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice('Promo Price')
			).toHaveText('$ 72.00');
			await expect(
				commerceMiniCartPage.miniCartEditItemPrice(
					'Price as Configured'
				)
			).toHaveText('$ 216.00');
		});

		expect(unitsOfMeasure).toHaveLength(3);
	}
);

test(
	'A unit of measure without stock cannot be applied from the mini cart edit panel',
	{tag: ['@COMMERCE-12618', '@LPD-107107']},
	async ({apiHelpers, commerceMiniCartPage, page}) => {
		const {sku} = await setUpThreeUnitsOfMeasure(apiHelpers, ['uom2']);

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await quickAddAndEdit(commerceMiniCartPage, page, sku.sku);

		await test.step('The stockless unit of measure is offered in the selector', async () => {
			await expect(
				commerceMiniCartPage.miniCartUnitOfMeasureSelector.locator(
					'option'
				)
			).toHaveText(['UOM2', 'UOM3']);

			await commerceMiniCartPage.miniCartUnitOfMeasureSelector.selectOption(
				'uom3'
			);

			await commerceMiniCartPage.miniCartEditItemQuantitySelector.fill(
				'6'
			);

			await expect(commerceMiniCartPage.miniCartSaveButton).toBeEnabled();
		});

		await test.step('Saving does not move the cart item onto it', async () => {
			await commerceMiniCartPage.miniCartSaveButton.click();

			await expect(
				commerceMiniCartPage.miniCartEditItemPanel
			).toBeHidden();

			const cartItem = commerceMiniCartPage.miniCartItemForSku(sku.sku);

			await expect(
				commerceMiniCartPage.miniCartItemUnitOfMeasure(cartItem)
			).toContainText('uom2');
			await expect(
				commerceMiniCartPage.miniCartItemListPrice(cartItem)
			).toHaveText('$ 2.00');
		});
	}
);

test(
	'The mini cart edit panel price table lists every active unit of measure and tier price',
	{tag: ['@COMMERCE-12620', '@LPD-107107']},
	async ({apiHelpers, commerceMiniCartPage, page, productDetailsPage}) => {
		const {sku} = await setUpThreeUnitsOfMeasure(apiHelpers, [
			'uom2',
			'uom3',
		]);

		await test.step('Give the third unit of measure a tier price', async () => {
			const basePriceList =
				await apiHelpers.headlessCommerceAdminPricing.getBasePriceList(
					catalog.id
				);

			const priceEntries =
				await apiHelpers.headlessCommerceAdminPricing.getPriceListEntries(
					basePriceList.items[0].id
				);

			const unitOfMeasureThreePriceEntry = priceEntries.items.find(
				(priceEntry: {skuId: number; unitOfMeasureKey: string}) =>
					priceEntry.skuId === sku.id &&
					priceEntry.unitOfMeasureKey === 'uom3'
			);

			await apiHelpers.headlessCommerceAdminPricing.postTierPrice(
				unitOfMeasureThreePriceEntry.priceEntryId,
				{minimumQuantity: 6, price: 2, unitOfMeasureKey: 'uom3'}
			);
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await quickAddAndEdit(commerceMiniCartPage, page, sku.sku);

		await expect(
			commerceMiniCartPage.unitOfMeasureTableLabel
		).toBeVisible();
		await expect(productDetailsPage.uomPriceTableRows).toHaveCount(3);
		await expect(productDetailsPage.uomPriceTableRowCells(0)).toHaveText([
			'UOM2',
			'uom2',
			'0.6',
			'$ 2.00',
		]);
		await expect(productDetailsPage.uomPriceTableRowCells(1)).toHaveText([
			'UOM3',
			'uom3',
			'2',
			'$ 3.00',
		]);
		await expect(productDetailsPage.uomPriceTableRowCells(2)).toHaveText([
			'UOM3',
			'uom3',
			'6',
			'$ 2.00',
		]);
		await expect(
			productDetailsPage.uomPriceTable.getByText('UOM1')
		).toBeHidden();
	}
);

test(
	'The mini cart edit panel validates the quantity against the multiple of the selected unit of measure',
	{tag: ['@COMMERCE-12617', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceMiniCartPage,
		commerceThemeMiniumCatalogPage,
		page,
	}) => {
		const {sku} = await setUpThreeUnitsOfMeasure(apiHelpers, [
			'uom2',
			'uom3',
		]);

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await quickAddAndEdit(commerceMiniCartPage, page, sku.sku);

		await expect(
			commerceMiniCartPage.miniCartUnitOfMeasureSelector
		).toHaveValue('uom2');

		for (const [quantity, valid] of [
			['2', false],
			['1', false],
			['0.6', true],
		] as Array<[string, boolean]>) {
			await commerceMiniCartPage.miniCartEditItemQuantitySelector.focus();
			await commerceMiniCartPage.miniCartEditItemQuantitySelector.fill(
				quantity
			);

			const multipleMessage =
				commerceThemeMiniumCatalogPage.popOverMessage(
					'Quantity must be a multiple of 0.6'
				);

			await expect(multipleMessage).toBeVisible();

			if (valid) {
				await expect(multipleMessage).not.toHaveClass(/text-danger/);
				await expect(
					commerceMiniCartPage.miniCartSaveButton
				).toBeEnabled();
			}
			else {
				await expect(multipleMessage).toHaveClass('text-danger');
				await expect(
					commerceMiniCartPage.miniCartSaveButton
				).toBeDisabled();
			}
		}
	}
);

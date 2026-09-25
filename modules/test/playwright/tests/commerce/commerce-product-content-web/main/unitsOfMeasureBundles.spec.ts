/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {ProductDetailsPage} from '../../../../pages/commerce/commerce-product-content-web/productDetailsPage';
import {CommerceMiniCartPage} from '../../../../pages/commerce/commerceMiniCartPage';
import getRandomString from '../../../../utils/getRandomString';
import performLogin, {
	performLoginViaApi,
	performLogout,
} from '../../../../utils/performLogin';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {
	createAccountWithBuyerUser,
	miniumSetUp,
	randomToken,
	setUpStockedUnitOfMeasures,
} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

let catalog: {id: number; name: string};
let channel: {id: number; name: string; siteGroupId: number};
let setupData: Array<{id: number | string; type: string}>;
let site: Site;

test.beforeAll(async ({browser}) => {
	const page = await browser.newPage();

	await performLoginViaApi({page, screenName: 'test'});

	const apiHelpers = new DataApiHelpers(page);

	const miniumResult = await miniumSetUp(apiHelpers);

	catalog = miniumResult.catalog;
	channel = miniumResult.channel;
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

async function addBundleToCart(
	{
		commerceMiniCartPage,
		productDetailsPage,
	}: {
		commerceMiniCartPage: CommerceMiniCartPage;
		productDetailsPage: ProductDetailsPage;
	},
	optionLabel: string,
	expectedItemCount: number
) {
	await commerceMiniCartPage.close();

	await productDetailsPage.selectOptionContaining(optionLabel, 'Color');

	await productDetailsPage.productDetailAddToCartButton.click();

	await commerceMiniCartPage.open();

	await expect(commerceMiniCartPage.miniCartButton).toHaveAttribute(
		'data-badge-count',
		String(expectedItemCount)
	);
}

async function setUpLinkedUnitOfMeasureBundle(
	apiHelpers: DataApiHelpers,
	{
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		deltaPrices = [],
		linkOptionValues = true,
		linkedQuantities,
		priceType = 'dynamic',
		unitsOfMeasure,
	}
) {
	const {product, sku} = await setUpStockedUnitOfMeasures(
		apiHelpers,
		catalog.id,
		{
			productConfiguration: {
				allowBackOrder: true,
				minOrderQuantity: 0.1,
				multipleOrderQuantity: 0.1,
			},
			unitsOfMeasure,
		}
	);

	const bundleName = `Bundle${randomToken()}`;
	const optionKey = getRandomString();

	const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		optionKey,
		'Color'
	);

	const bundle = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: bundleName},
		productConfiguration: {allowBackOrder: true},
		productOptions: [
			{
				fieldType: 'select',
				key: optionKey,
				name: {en_US: 'Color'},
				optionId: option.id,
				priceType,
				priority: 1,
				productOptionValues: [
					{key: 'blue', name: {en_US: 'Blue'}, priority: 1},
					{key: 'white', name: {en_US: 'White'}, priority: 2},
				],
				required: true,
				skuContributor: true,
			},
		],
	});

	await commerceAdminProductPage.gotoProduct(bundleName);

	await commerceAdminProductDetailsPage.goToProductOptions();

	await commerceAdminProductDetailsProductOptionsPage.openOption('Color');

	await expect(
		commerceAdminProductDetailsProductOptionsPage.optionValueRow('Blue')
	).toBeVisible();

	if (!linkOptionValues) {
		return {bundle, bundleName, product, sku};
	}

	for (const [index, optionValueName] of ['Blue', 'White'].entries()) {
		await commerceAdminProductDetailsProductOptionsPage.editOptionValue(
			optionValueName,
			{
				...(deltaPrices[index] && {deltaPrice: deltaPrices[index]}),
				quantity: linkedQuantities[index],
				sku: sku.sku,
				unitOfMeasureKey: unitsOfMeasure[index % unitsOfMeasure.length].key,
			}
		);
	}

	await commerceAdminProductDetailsProductOptionsPage.closeOption();

	await commerceAdminProductPage.generateSkus();

	return {bundle, bundleName, product, sku};
}

test(
	'A dynamic price bundle prices each option value from its linked SKU unit of measure',
	{tag: ['@COMMERCE-12545', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}) => {
		const {bundleName, product} = await setUpLinkedUnitOfMeasureBundle(
			apiHelpers,
			{
				commerceAdminProductDetailsPage,
				commerceAdminProductDetailsProductOptionsPage,
				commerceAdminProductPage,
				linkedQuantities: ['1', '1'],
				unitsOfMeasure: [
					{
						basePrice: 30,
						key: 'UOM1KEY',
						name: {en_US: 'UOM1'},
						primary: true,
						priority: 1,
					},
					{
						basePrice: 30,
						key: 'UOM2KEY',
						name: {en_US: 'UOM2'},
						priority: 2,
						promoPrice: 20,
					},
				],
			}
		);

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(`/web${site.friendlyUrlPath}/p/${bundleName}`, {
			waitUntil: 'networkidle',
		});

		const pages = {commerceMiniCartPage, productDetailsPage};


		await test.step('The option selector carries the price difference between the linked units of measure', async () => {
			await expect(
				productDetailsPage.optionSelector('Color').locator('option')
			).toHaveText(['Choose an Option', 'Blue', 'White - $ 10.00']);
		});

		await test.step('Each option value takes the price of the unit of measure it is linked to', async () => {
			await commerceMiniCartPage.close();

			await productDetailsPage.selectOptionContaining('Blue', 'Color');

			await expect(productDetailsPage.productDetailListPrice).toHaveText(
				'$ 30.00'
			);

			await addBundleToCart(pages, 'Blue', 1);

			await commerceMiniCartPage.close();

			await productDetailsPage.selectOptionContaining('White', 'Color');

			await expect(
				productDetailsPage.productDetailInactivePrice
			).toHaveText('$ 30.00');
			await expect(productDetailsPage.productDetailPromoPrice).toHaveText(
				'$ 20.00'
			);

			await addBundleToCart(pages, 'White', 2);
		});

		await test.step('The mini cart carries the linked SKU unit of measure of each bundled item', async () => {
			await commerceMiniCartPage.open();

			for (const [skuName, unitOfMeasureKey] of [
				['BLUE', 'UOM1KEY'],
				['WHITE', 'UOM2KEY'],
			]) {
				await expect(async () => {
					await commerceMiniCartPage.showItemOptions(skuName);

					await expect(
						commerceMiniCartPage
							.miniCartItem(skuName)
							.getByLabel(
								`1 × ${product.name['en_US']} ${unitOfMeasureKey}`
							)
					).toBeVisible({timeout: 5000});
				}).toPass({timeout: 30000});
			}

			await expect(
				commerceMiniCartPage.miniCartItemListPrice(
					commerceMiniCartPage.miniCartItem('BLUE')
				)
			).toHaveText('$ 30.00');
			await expect(
				commerceMiniCartPage.miniCartItemPromoPrice(
					commerceMiniCartPage.miniCartItem('WHITE')
				)
			).toHaveText('$ 20.00');
		});
	}
);

test(
	'A dynamic price bundle converts the price of a decimal unit of measure quantity',
	{tag: ['@COMMERCE-12546', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}) => {
		const {bundleName, product} = await setUpLinkedUnitOfMeasureBundle(
			apiHelpers,
			{
				commerceAdminProductDetailsPage,
				commerceAdminProductDetailsProductOptionsPage,
				commerceAdminProductPage,
				linkedQuantities: ['0.6', '1.5'],
				unitsOfMeasure: [
					{
						basePrice: 30,
						incrementalOrderQuantity: 0.6,
						key: 'UOM1KEY',
						name: {en_US: 'UOM1'},
						primary: true,
						priority: 1,
					},
					{
						basePrice: 50,
						incrementalOrderQuantity: 1.5,
						key: 'UOM2KEY',
						name: {en_US: 'UOM2'},
						priority: 2,
						promoPrice: 40,
					},
				],
			}
		);

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(`/web${site.friendlyUrlPath}/p/${bundleName}`, {
			waitUntil: 'networkidle',
		});

		const pages = {commerceMiniCartPage, productDetailsPage};


		await test.step('The decimal linked quantities convert each option value to one bundle unit', async () => {
			await expect(
				productDetailsPage.optionSelector('Color').locator('option')
			).toHaveText(['Choose an Option', 'Blue', 'White + $ 10.00']);

			await commerceMiniCartPage.close();

			await productDetailsPage.selectOptionContaining('Blue', 'Color');

			await expect(productDetailsPage.productDetailListPrice).toHaveText(
				'$ 30.00'
			);

			await addBundleToCart(pages, 'Blue', 1);

			await commerceMiniCartPage.close();

			await productDetailsPage.selectOptionContaining('White', 'Color');

			await expect(
				productDetailsPage.productDetailInactivePrice
			).toHaveText('$ 50.00');
			await expect(productDetailsPage.productDetailPromoPrice).toHaveText(
				'$ 40.00'
			);

			await addBundleToCart(pages, 'White', 2);
		});

		await test.step('The mini cart keeps the decimal linked quantity and the converted price', async () => {
			await commerceMiniCartPage.open();

			for (const [skuName, linkedQuantity, unitOfMeasureKey] of [
				['BLUE', '0.6', 'UOM1KEY'],
				['WHITE', '1.5', 'UOM2KEY'],
			]) {
				await expect(async () => {
					await commerceMiniCartPage.showItemOptions(skuName);

					await expect(
						commerceMiniCartPage
							.miniCartItem(skuName)
							.getByLabel(
								`${linkedQuantity} × ${product.name['en_US']} ${unitOfMeasureKey}`
							)
					).toBeVisible({timeout: 5000});
				}).toPass({timeout: 30000});
			}

			await expect(
				commerceMiniCartPage.miniCartItemListPrice(
					commerceMiniCartPage.miniCartItem('BLUE')
				)
			).toHaveText('$ 30.00');
			await expect(
				commerceMiniCartPage.miniCartItemPromoPrice(
					commerceMiniCartPage.miniCartItem('WHITE')
				)
			).toHaveText('$ 40.00');
		});
	}
);

test(
	'Normal and quick add to cart merge onto the same bundled order item',
	{tag: ['@COMMERCE-12547', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}) => {
		const {bundleName} = await setUpLinkedUnitOfMeasureBundle(apiHelpers, {
			commerceAdminProductDetailsPage,
			commerceAdminProductDetailsProductOptionsPage,
			commerceAdminProductPage,
			linkedQuantities: ['1', '1'],
			unitsOfMeasure: [
				{
					basePrice: 30,
					incrementalOrderQuantity: 0.6,
					key: 'UOM1KEY',
					name: {en_US: 'UOM1'},
					primary: true,
					priority: 1,
				},
				{
					basePrice: 50,
					incrementalOrderQuantity: 1.5,
					key: 'UOM2KEY',
					name: {en_US: 'UOM2'},
					priority: 2,
					promoPrice: 40,
				},
			],
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(`/web${site.friendlyUrlPath}/p/${bundleName}`, {
			waitUntil: 'networkidle',
		});

		const pages = {commerceMiniCartPage, productDetailsPage};


		await test.step('Adding the same option value twice through the product details page merges it', async () => {
			await expect(
				productDetailsPage.optionSelector('Color').locator('option')
			).toHaveText(['Choose an Option', 'Blue', 'White - $ 23.33']);

			await addBundleToCart(pages, 'Blue', 1);
			await addBundleToCart(pages, 'White', 2);
			await addBundleToCart(pages, 'Blue', 2);

			await expect(
				commerceMiniCartPage.miniCartItemQuantitySelector('BLUE')
			).toHaveValue('2');
			await expect(
				commerceMiniCartPage.miniCartItemQuantitySelector('WHITE')
			).toHaveValue('1');
		});

		await test.step('Quick adding the other option value merges onto its existing order item', async () => {
			await commerceMiniCartPage.quickAddToCart('WHITE');

			await commerceMiniCartPage.open();

			await expect(commerceMiniCartPage.miniCartButton).toHaveAttribute(
				'data-badge-count',
				'2'
			);

			await expect(
				commerceMiniCartPage.miniCartItemQuantitySelector('BLUE')
			).toHaveValue('2');
			await expect(
				commerceMiniCartPage.miniCartItemQuantitySelector('WHITE')
			).toHaveValue('2');
			await expect(
				commerceMiniCartPage.miniCartItemListPrice(
					commerceMiniCartPage.miniCartItem('BLUE')
				)
			).toHaveText('$ 30.00');
			await expect(
				commerceMiniCartPage.miniCartItemPromoPrice(
					commerceMiniCartPage.miniCartItem('WHITE')
				)
			).toHaveText('$ 26.66');
		});
	}
);

test(
	'The same SKU unit of measure can back two option values only with different quantities',
	{tag: ['@COMMERCE-12544', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}) => {
		const {bundle, bundleName, product, sku} =
			await setUpLinkedUnitOfMeasureBundle(apiHelpers, {
				commerceAdminProductDetailsPage,
				commerceAdminProductDetailsProductOptionsPage,
				commerceAdminProductPage,
				linkOptionValues: false,
				linkedQuantities: [],
				priceType: 'static',
				unitsOfMeasure: [
					{
						basePrice: 1,
						key: 'UOM1KEY',
						name: {en_US: 'UOM1'},
						primary: true,
						priority: 1,
					},
				],
			});

		await commerceAdminProductDetailsProductOptionsPage.editOptionValue(
			'Blue',
			{
				deltaPrice: '40',
				quantity: '1',
				sku: sku.sku,
				unitOfMeasureKey: 'UOM1KEY',
			}
		);

		await test.step('The same SKU unit of measure at the same quantity is rejected', async () => {
			await commerceAdminProductDetailsProductOptionsPage.openOptionValue(
				'White'
			);

			await commerceAdminProductDetailsProductOptionsPage.searchSku(
				sku.sku
			);

			await commerceAdminProductDetailsProductOptionsPage
				.optionValueSkuDropdownItem(`${sku.sku} - UOM1KEY`)
				.click();

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueQuantityInput
			).toBeEnabled();

			await commerceAdminProductDetailsProductOptionsPage.optionValueQuantityInput.fill(
				'1'
			);

			await commerceAdminProductDetailsProductOptionsPage.optionValueSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionValueSidePanelFrame,
				'Please enter a valid quantity.',
				{type: 'danger'}
			);

			await commerceAdminProductDetailsProductOptionsPage.closeOptionValue();
		});

		await test.step('The same SKU unit of measure at a different quantity is accepted', async () => {
			await commerceAdminProductDetailsProductOptionsPage.editOptionValue(
				'White',
				{
					deltaPrice: '20',
					quantity: '2',
					sku: sku.sku,
					unitOfMeasureKey: 'UOM1KEY',
				}
			);

			await commerceAdminProductDetailsProductOptionsPage.closeOption();

			await commerceAdminProductPage.generateSkus();
		});

		const basePriceList =
			await apiHelpers.headlessCommerceAdminPricing.getBasePriceListId(
				catalog.id
			);

		const bundleSkus =
			await apiHelpers.headlessCommerceAdminCatalog.getProduct(
				bundle.productId
			);

		for (const [skuName, price] of [
			['BLUE', 30],
			['WHITE', 40],
		] as Array<[string, number]>) {
			await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
				price,
				priceListId: basePriceList.items[0].id,
				skuId: bundleSkus.skus.find(
					(bundleSku: {sku: string}) => bundleSku.sku === skuName
				).id,
			});
		}

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(`/web${site.friendlyUrlPath}/p/${bundleName}`, {
			waitUntil: 'networkidle',
		});

		const pages = {commerceMiniCartPage, productDetailsPage};

		await test.step('Each option value carries its own linked quantity into the cart', async () => {
			await addBundleToCart(pages, 'Blue', 1);
			await addBundleToCart(pages, 'White', 2);

			for (const [skuName, linkedQuantity] of [
				['BLUE', '1'],
				['WHITE', '2'],
			]) {
				await expect(async () => {
					await commerceMiniCartPage.showItemOptions(skuName);

					await expect(
						commerceMiniCartPage
							.miniCartItem(skuName)
							.getByLabel(
								`${linkedQuantity} × ${product.name['en_US']} UOM1KEY`
							)
					).toBeVisible({timeout: 5000});
				}).toPass({timeout: 30000});
			}

			await expect(
				commerceMiniCartPage.miniCartItemListPrice(
					commerceMiniCartPage.miniCartItem('BLUE')
				)
			).toHaveText('$ 70.00');
			await expect(
				commerceMiniCartPage.miniCartItemListPrice(
					commerceMiniCartPage.miniCartItem('WHITE')
				)
			).toHaveText('$ 60.00');
		});
	}
);

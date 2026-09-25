/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Page, expect, mergeTests} from '@playwright/test';

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
import {
	createAccountWithBuyerUser,
	miniumSetUp,
	patchUnitOfMeasureWarehouseItems,
	randomToken,
	setUpStockedUnitOfMeasures,
	tableCellByColumnName,
	unitOfMeasurePriceLabel,
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

type TOrderItem = {
	productName: string;
	quantity: string;
	skuName: string;
	unitOfMeasureKey: string;
};

async function expectOrderItemUnitsOfMeasure(
	page: Page,
	orderItems: TOrderItem[]
) {
	for (const {quantity, skuName, unitOfMeasureKey} of orderItems) {
		const rowText = unitOfMeasureKey ? [skuName, unitOfMeasureKey] : skuName;

		await expect(
			await tableCellByColumnName(page, rowText, 'Quantity')
		).toHaveText(quantity);
		await expect(
			await tableCellByColumnName(page, rowText, 'UOM')
		).toHaveText(unitOfMeasureKey);
	}
}

async function setUpOrderWithUnitsOfMeasure(
	apiHelpers: DataApiHelpers,
	{
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}: {
		commerceMiniCartPage: CommerceMiniCartPage;
		page: Page;
		productDetailsPage: ProductDetailsPage;
	}
) {
	const twoUnitsProduct = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
		productConfiguration: {
			allowBackOrder: true,
			multipleOrderQuantity: 0.1,
		},
		unitsOfMeasure: [
			{
				basePrice: 1,
				key: 'uom1',
				name: {en_US: 'UOM1'},
				precision: 1,
				primary: true,
				priority: 1,
			},
			{
				basePrice: 2,
				key: 'uom2',
				name: {en_US: 'UOM2'},
				precision: 1,
				priority: 2,
			},
		],
		warehouseQuantities: [['Italy', 100]],
	});

	const decimalProduct = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
		productConfiguration: {
			allowBackOrder: true,
			multipleOrderQuantity: 0.1,
		},
		unitsOfMeasure: [
			{
				basePrice: 3,
				incrementalOrderQuantity: 0.6,
				key: 'uom3',
				name: {en_US: 'UOM3'},
				precision: 1,
				primary: true,
			},
		],
		warehouseQuantities: [['Italy', 100]],
	});

	const plainProduct = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
		productConfiguration: {allowBackOrder: true},
		unitsOfMeasure: [],
		warehouseQuantities: [['Italy', 100]],
	});

	const {account, buyerUser} = await createAccountWithBuyerUser(
		apiHelpers,
		site.id
	);

	const orderItems: TOrderItem[] = [
		{
			productName: twoUnitsProduct.product.name['en_US'],
			quantity: '2',
			skuName: twoUnitsProduct.sku.sku,
			unitOfMeasureKey: 'uom1',
		},
		{
			productName: twoUnitsProduct.product.name['en_US'],
			quantity: '3',
			skuName: twoUnitsProduct.sku.sku,
			unitOfMeasureKey: 'uom2',
		},
		{
			productName: decimalProduct.product.name['en_US'],
			quantity: '1.2',
			skuName: decimalProduct.sku.sku,
			unitOfMeasureKey: 'uom3',
		},
		{
			productName: plainProduct.product.name['en_US'],
			quantity: '1',
			skuName: plainProduct.sku.sku,
			unitOfMeasureKey: '',
		},
	];

	const productUrls = {
		[twoUnitsProduct.product.name['en_US']]:
			twoUnitsProduct.product.urls['en_US'],
		[decimalProduct.product.name['en_US']]:
			decimalProduct.product.urls['en_US'],
		[plainProduct.product.name['en_US']]:
			plainProduct.product.urls['en_US'],
	};

	await performLogout(page);
	await performLoginViaApi({page, screenName: buyerUser.alternateName});

	for (const {productName, quantity, unitOfMeasureKey} of orderItems) {
		await commerceMiniCartPage.close();

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${productUrls[productName]}`,
			{waitUntil: 'networkidle'}
		);

		if (
			unitOfMeasureKey &&
			(await productDetailsPage.unitOfMeasureSelect
				.locator('option')
				.count()) > 1
		) {
			await productDetailsPage.unitOfMeasureSelect.selectOption(
				unitOfMeasureKey
			);
		}

		await productDetailsPage.productDetailQuantitySelector.fill(quantity);

		await expect(
			productDetailsPage.productDetailAddToCartButton
		).toBeEnabled();

		await productDetailsPage.productDetailAddToCartButton.click();

		await commerceMiniCartPage.open();
	}

	await expect(commerceMiniCartPage.miniCartButton).toHaveAttribute(
		'data-badge-count',
		String(orderItems.length)
	);

	return {account, buyerUser, decimalProduct, orderItems};
}

test(
	'The discount SKU finder exposes one entry per unit of measure of a SKU',
	{tag: ['@COMMERCE-12424', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminDiscountDetailsPage,
		commerceAdminDiscountsPage,
	}) => {
		const skuName = `SKU${randomToken()}`;
		const productName = `Product${randomToken()}`;

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				active: true,
				catalogId: catalog.id,
				name: {en_US: productName},
				skus: [
					{
						cost: 0,
						price: 10,
						published: true,
						purchasable: true,
						sku: skuName,
					},
				],
			});

		const sku = product.skus[0];

		const discount =
			await apiHelpers.headlessCommerceAdminPricing.postDiscount({
				active: true,
				level: 'L1',
				percentageLevel1: 50,
				target: 'skus',
				title: `Test Discount ${getRandomString()}`,
				usePercentage: true,
			});

		await commerceAdminDiscountsPage.goto();

		await commerceAdminDiscountsPage.discountLink(discount.title).click();

		const findSkuInput =
			commerceAdminDiscountDetailsPage.relationFindInput('Find a SKU');

		const searchSku = async (expectedRowCount: number) => {
			await expect(async () => {
				await findSkuInput.click({timeout: 5000});
				await findSkuInput.fill('', {timeout: 5000});
				await findSkuInput.fill(skuName, {timeout: 5000});

				await expect(
					commerceAdminDiscountDetailsPage.skuFinderRows
				).toHaveCount(expectedRowCount, {timeout: 5000});
			}).toPass({timeout: 30000});
		};

		await test.step('A SKU without units of measure is found once, with an empty UOM', async () => {
			await searchSku(1);

			await expect(
				commerceAdminDiscountDetailsPage.skuFinderRowSku(0)
			).toHaveText(skuName);
			await expect(
				commerceAdminDiscountDetailsPage.skuFinderRowUnitOfMeasureKey(0)
			).toHaveText('');
		});

		await test.step('Selecting it adds a single SKU row whose UOM column is empty', async () => {
			await commerceAdminDiscountDetailsPage
				.skuFinderRowSelectButton(0)
				.click();

			await commerceAdminDiscountDetailsPage.closeSkuFinder();

			await expect(
				commerceAdminDiscountDetailsPage.skuTableRows
			).toHaveCount(1);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowSku(0)
			).toHaveText(skuName);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowProductName(0)
			).toHaveText(productName);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowUnitOfMeasureKey(0)
			).toHaveText('');

			await commerceAdminDiscountDetailsPage
				.eligibilityRowActions(skuName)
				.click();
			await commerceAdminDiscountDetailsPage.eligibilityRowRemoveMenuItem.click();

			await expect(
				commerceAdminDiscountDetailsPage.skuTableRows
			).toHaveCount(0);
		});

		await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
			sku.id,
			{
				basePrice: 10,
				key: 'uomKey1',
				name: {en_US: 'uomName1'},
			}
		);

		await test.step('Adding a unit of measure carries its key into the finder and into the selected SKU', async () => {
			await commerceAdminDiscountsPage.goto();

			await commerceAdminDiscountsPage
				.discountLink(discount.title)
				.click();

			await searchSku(1);

			await expect(
				commerceAdminDiscountDetailsPage.skuFinderRowUnitOfMeasureKey(0)
			).toHaveText('uomKey1');

			await commerceAdminDiscountDetailsPage
				.skuFinderRowSelectButton(0)
				.click();

			await commerceAdminDiscountDetailsPage.closeSkuFinder();

			await expect(
				commerceAdminDiscountDetailsPage.skuTableRows
			).toHaveCount(1);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowSku(0)
			).toHaveText(skuName);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowUnitOfMeasureKey(0)
			).toHaveText('uomKey1');
		});

		await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
			sku.id,
			{
				basePrice: 10,
				key: 'uomKey2',
				name: {en_US: 'uomName2'},
			}
		);

		await test.step('A second unit of measure makes the SKU findable and selectable once per combination', async () => {
			await commerceAdminDiscountsPage.goto();

			await commerceAdminDiscountsPage
				.discountLink(discount.title)
				.click();

			await searchSku(2);

			await expect(
				commerceAdminDiscountDetailsPage.skuFinderRowUnitOfMeasureKey(0)
			).toHaveText('uomKey1');
			await expect(
				commerceAdminDiscountDetailsPage.skuFinderRowUnitOfMeasureKey(1)
			).toHaveText('uomKey2');

			await commerceAdminDiscountDetailsPage
				.skuFinderRowSelectButton(1)
				.click();

			await commerceAdminDiscountDetailsPage.closeSkuFinder();

			await expect(
				commerceAdminDiscountDetailsPage.skuTableRows
			).toHaveCount(2);
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowUnitOfMeasureKey(0)
			).toHaveText('uomKey1');
			await expect(
				commerceAdminDiscountDetailsPage.skuTableRowUnitOfMeasureKey(1)
			).toHaveText('uomKey2');
		});
	}
);

test(
	'A buyer can add to cart only the units of measure of a multi UOM SKU that carry stock',
	{tag: ['@COMMERCE-12374', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceMiniCartPage,
		commerceThemeMiniumCatalogPage,
		page,
		productDetailsPage,
	}) => {
		const {product, sku, unitsOfMeasure} = await setUpStockedUnitOfMeasures(
			apiHelpers,
			catalog.id,
			{
				price: 20,
				productConfiguration: {
					allowBackOrder: false,
					displayAvailability: true,
					displayStockQuantity: true,
					multipleOrderQuantity: 0.6,
				},
				warehouseQuantities: [['Italy', 120]],
				unitsOfMeasure: [
					{
						basePrice: 20,
						incrementalOrderQuantity: 0.6,
						key: 'UOM1KEY',
						name: {en_US: 'UOM1'},
						priority: 1,
					},
					{
						basePrice: 20,
						incrementalOrderQuantity: 0.6,
						key: 'UOM2KEY',
						name: {en_US: 'UOM2'},
						priority: 2,
						promoPrice: 15,
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

		await test.step('The product card offers the variants instead of adding to cart', async () => {
			await page.goto(`/web${site.friendlyUrlPath}/catalog`, {
				waitUntil: 'networkidle',
			});

			await commerceThemeMiniumCatalogPage.search(product.name['en_US']);

			await expect(
				commerceThemeMiniumCatalogPage.productCardViewAllVariantsButton(
					product.name['en_US']
				)
			).toBeVisible();
			await expect(
				commerceThemeMiniumCatalogPage.productCardAddToCartButton(
					product.name['en_US']
				)
			).toHaveCount(0);
		});

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product.urls['en_US']}`,
			{waitUntil: 'networkidle'}
		);

		await test.step('The first unit of measure is available with the stock inherited from the SKU', async () => {
			await expect(
				productDetailsPage.unitOfMeasureSelectedOption
			).toHaveText('UOM1');
			await expect(
				productDetailsPage.productDetailAvailabilityLabel
			).toHaveText('Available');
			await expect(productDetailsPage.inStockQuantity).toHaveText(
				'120 in Stock'
			);
			await expect(productDetailsPage.productDetailListPrice).toHaveText(
				unitOfMeasurePriceLabel(unitsOfMeasure[0], 20)
			);
		});

		await test.step('The second unit of measure is unavailable and cannot be added to the cart', async () => {
			await productDetailsPage.unitOfMeasureSelect.selectOption('UOM2');

			await expect(
				productDetailsPage.productDetailAvailabilityLabel
			).toHaveText('Unavailable');
			await expect(productDetailsPage.inStockQuantity).toHaveText(
				'0 in Stock'
			);
			await expect(productDetailsPage.productDetailPromoPrice).toHaveText(
				'$ 15.00'
			);
			await expect(
				productDetailsPage.productDetailInactivePrice
			).toHaveText(unitOfMeasurePriceLabel(unitsOfMeasure[1], 20));
			await expect(
				productDetailsPage.productDetailAddToCartButton
			).toBeDisabled();
		});

		await test.step('Adding the stocked unit of measure twice sums into a single cart item', async () => {
			await productDetailsPage.unitOfMeasureSelect.selectOption('UOM1');

			const cartItem = commerceMiniCartPage.miniCartItemForSku(sku.sku);

			for (const expectedQuantity of ['3', '6']) {
				await commerceMiniCartPage.close();

				await productDetailsPage.productDetailQuantitySelector.fill('3');

				await expect(
					productDetailsPage.productDetailAddToCartButton
				).toBeEnabled();

				await productDetailsPage.productDetailAddToCartButton.click();

				await commerceMiniCartPage.open();

				await expect(
					commerceThemeMiniumCatalogPage.quantitySelector(cartItem)
				).toHaveValue(expectedQuantity);
			}

			await expect(
				commerceMiniCartPage.miniCartItemUnitOfMeasure(cartItem)
			).toContainText('UOM1KEY');
			await expect(
				commerceMiniCartPage.miniCartItemListPrice(cartItem)
			).toHaveText('$ 20.00');
			await expect(commerceMiniCartPage.miniCartTotalPrice).toContainText(
				'$ 200.00'
			);
		});
	}
);

test(
	'The product details unit of measure selector lists only the active units of measure, by priority',
	{tag: ['@COMMERCE-12393', '@LPD-107107']},
	async ({apiHelpers, page, productDetailsPage}) => {
		const {product, unitsOfMeasure} = await setUpStockedUnitOfMeasures(
			apiHelpers,
			catalog.id,
			{
				unitsOfMeasure: [
					{
						basePrice: 10,
						key: 'uom4',
						name: {en_US: 'UOM4'},
						priority: 4,
					},
					{
						basePrice: 25,
						key: 'uom1',
						name: {en_US: 'UOM1'},
						priority: 1,
					},
					{
						active: false,
						basePrice: 15,
						key: 'uom2',
						name: {en_US: 'UOM2'},
						priority: 2,
					},
					{
						basePrice: 20,
						key: 'uom3',
						name: {en_US: 'UOM3'},
						priority: 3,
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

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product.urls['en_US']}`,
			{waitUntil: 'networkidle'}
		);

		await expect(
			productDetailsPage.unitOfMeasureSelect.locator('option')
		).toHaveText(['UOM1', 'UOM3', 'UOM4']);

		for (const [index, key] of ['uom1', 'uom3', 'uom4'].entries()) {
			await expect(
				productDetailsPage.unitOfMeasureSelect.locator('option').nth(index)
			).toHaveAttribute('value', key);
		}

		await expect(productDetailsPage.unitOfMeasureSelect).toHaveValue('uom1');
		await expect(productDetailsPage.productDetailListPrice).toHaveText(
			unitOfMeasurePriceLabel(
				unitsOfMeasure.find((unitOfMeasure) => unitOfMeasure.key === 'uom1'),
				25
			)
		);
	}
);

test(
	'A unit of measure with no stock cannot reach the cart when back orders are disabled',
	{tag: ['@COMMERCE-12551', '@LPD-107107']},
	async ({apiHelpers, commerceMiniCartPage, page, productDetailsPage}) => {
		const {product} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			productConfiguration: {
				allowBackOrder: false,
				displayAvailability: true,
				displayStockQuantity: true,
			},
			warehouseQuantities: [['Italy', 10]],
			unitsOfMeasure: [
				{basePrice: 1, key: 'uomKey1', name: {en_US: 'uomName1'}},
				{basePrice: 2, key: 'uomKey2', name: {en_US: 'uomName2'}},
			],
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product.urls['en_US']}`,
			{waitUntil: 'networkidle'}
		);

		await productDetailsPage.unitOfMeasureSelect.selectOption('uomKey2');

		await expect(productDetailsPage.inStockQuantity).toHaveText(
			'0 in Stock'
		);
		await expect(
			productDetailsPage.productDetailAvailabilityLabel
		).toHaveText('Unavailable');
		await expect(
			productDetailsPage.productDetailAddToCartButton
		).toBeDisabled();

		await productDetailsPage.productDetailAddToCartButton.click({
			force: true,
		});

		await expect(commerceMiniCartPage.miniCartButton).toHaveAttribute(
			'data-badge-count',
			'0'
		);
	}
);

test(
	'The quantity selector popover follows the multiple of the selected unit of measure',
	{tag: ['@COMMERCE-12394', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceThemeMiniumCatalogPage,
		page,
		productDetailsPage,
	}) => {
		const {product} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			price: 20,
			productConfiguration: {
				allowBackOrder: true,
				multipleOrderQuantity: 0.01,
			},
			unitsOfMeasure: [
				{
					basePrice: 20,
					incrementalOrderQuantity: 0.444,
					key: 'UOM1KEY',
					name: {en_US: 'UOM1'},
					precision: 3,
					priority: 1,
				},
				{
					basePrice: 20,
					incrementalOrderQuantity: 0.25,
					key: 'UOM2KEY',
					name: {en_US: 'UOM2'},
					precision: 2,
					priority: 2,
				},
			],
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${product.urls['en_US']}`,
			{waitUntil: 'networkidle'}
		);

		const expectMultipleMessage = async (
			multiple: string,
			satisfied: boolean
		) => {
			const message = commerceThemeMiniumCatalogPage.popOverMessage(
				`Quantity must be a multiple of ${multiple}`
			);

			await expect(message).toBeVisible();

			if (satisfied) {
				await expect(message).not.toHaveClass(/text-danger/);
			}
			else {
				await expect(message).toHaveClass('text-danger');
			}
		};

		const fillQuantity = async (quantity: string) => {
			await productDetailsPage.productDetailQuantitySelector.focus();
			await productDetailsPage.productDetailQuantitySelector.fill(
				quantity
			);
		};

		await test.step('The first unit of measure steps by the base multiple converted to its increment', async () => {
			await fillQuantity('0.0555');

			await expectMultipleMessage('2.22', false);

			await fillQuantity('2.22');

			await expectMultipleMessage('2.22', true);
		});

		await test.step('Switching unit of measure recomputes the multiple', async () => {
			await productDetailsPage.unitOfMeasureSelect.selectOption(
				'UOM2KEY'
			);

			await fillQuantity('0.888');

			await expectMultipleMessage('0.25', false);
		});
	}
);

test(
	'The SKU details panel carries no pricing section once units of measure own the prices',
	{tag: ['@COMMERCE-12461', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const {product, sku} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			unitsOfMeasure: [],
		});

		const expectNoPricingFields = async (container: FrameLocator) => {
			await expect(
				container.getByRole('heading', {exact: true, name: 'Pricing'})
			).toHaveCount(0);

			for (const pricingField of [
				'Base Price',
				'Promotion Price',
				'Cost',
			]) {
				await expect(
					container.getByLabel(pricingField, {exact: true})
				).toHaveCount(0);
			}

			await expect(
				container.getByLabel('Price on Application', {exact: true})
			).toHaveCount(0);
		};

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await test.step('An existing SKU shows no pricing fields', async () => {
			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku.sku)
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuFieldName
			).toBeVisible();

			await expectNoPricingFields(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);
		});

		await test.step('A new SKU shows no pricing fields either', async () => {
			await (
				await commerceAdminProductDetailsSkusPage.closeSidePanelFrame(
					false
				)
			).click();

			await commerceAdminProductDetailsSkusPage.skuAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuAddModalSkuInput
			).toBeVisible();

			await expectNoPricingFields(
				commerceAdminProductDetailsSkusPage.skuAddModal
			);
		});
	}
);

test(
	'The Add Unit of Measure modal defaults its base price to the primary unit of measure price',
	{tag: ['@COMMERCE-12456', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const {product, sku} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			unitsOfMeasure: [
				{
					basePrice: 56,
					key: 'uomKey1',
					name: {en_US: 'uomName1'},
					primary: true,
				},
			],
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(sku.sku)
			.click();

		await commerceAdminProductDetailsSkusPage.goToSkuUOM();

		const expectDefaultBasePrice = async () => {
			await expect(async () => {
				await commerceAdminProductDetailsSkusPage.addSkuUOMButton.click(
					{timeout: 5000}
				);

				await expect(
					commerceAdminProductDetailsSkusPage.skuUOMModal.getByRole(
						'heading',
						{name: 'Add Unit of Measure'}
					)
				).toBeVisible({timeout: 5000});
			}).toPass({timeout: 30000});

			await expect(
				commerceAdminProductDetailsSkusPage.skuUOMModalField(
					'Base Price'
				)
			).toHaveValue('56.00');
		};

		await test.step('The modal opens with the primary unit of measure base price', async () => {
			await expectDefaultBasePrice();
		});

		await test.step('A second unit of measure does not take over the default', async () => {
			await commerceAdminProductDetailsSkusPage.addUOMEntry({
				skipModalOpening: true,
				uomKey: 'uom2',
				uomName: 'UOM2',
			});

			await expect(
				commerceAdminProductDetailsSkusPage.uomTableRowLink('UOM2')
			).toBeVisible();

			await expectDefaultBasePrice();
		});
	}
);

test(
	'A SKU without units of measure keeps its default prices and an empty UOM column',
	{tag: ['@COMMERCE-12342', '@LPD-107107']},
	async ({
		commerceAdminPriceListDetailsPage,
		commerceAdminPriceListsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		commerceAdminPromotionsPage,
		page,
	}) => {
		await test.step('The SKU Price tab shows the seeded price list and promotion prices with no UOM', async () => {
			await commerceAdminProductPage.gotoProduct('ABS Sensor');

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink('MIN93015')
				.click();

			await commerceAdminProductDetailsSkusPage.skuTab('Price').click();

			for (const [entryName, price] of [
				[`${catalog.name} Base Price List`, '$ 50.00'],
				[`${catalog.name} Base Promotion`, '$ 0.00'],
			]) {
				await expect(
					commerceAdminProductDetailsSkusPage.sidePanelPriceTableRowUnitOfMeasure(
						entryName
					)
				).toHaveText('');
				await expect(
					commerceAdminProductDetailsSkusPage.sidePanelPriceTableRowUnitPrice(
						entryName
					)
				).toHaveText(price);
			}
		});

		await test.step('The price list entry shows the default base price and price list price with no UOM', async () => {
			await commerceAdminPriceListsPage.goto();

			await (
				await commerceAdminPriceListsPage.tableRowLink({
					colIndex: 0,
					rowValue: `${catalog.name} Base Price List`,
				})
			).click();

			await commerceAdminPriceListDetailsPage.entriesTab.click();

			await commerceAdminPriceListDetailsPage.searchByValue('MIN93015');

			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'UOM')
			).toHaveText('');
			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'Base Price')
			).toHaveText('$ 50.00');
			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'Price List Price')
			).toHaveText('$ 50.00');
		});

		await test.step('The promotion entry shows the default promotion prices with no UOM', async () => {
			await commerceAdminPromotionsPage.goto();

			await (
				await commerceAdminPromotionsPage.tableRowLink({
					colIndex: 0,
					rowValue: `${catalog.name} Base Promotion`,
				})
			).click();

			await commerceAdminPriceListDetailsPage.entriesTab.click();

			await commerceAdminPriceListDetailsPage.searchByValue('MIN93015');

			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'UOM')
			).toHaveText('');
			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'Base Promotion Price')
			).toHaveText('$ 0.00');
			await expect(
				await tableCellByColumnName(page, 'MIN93015', 'Promotion Price')
			).toHaveText('$ 0.00');
		});
	}
);

test(
	'The inventory admin lists one line per SKU and unit of measure combination',
	{tag: ['@COMMERCE-12324', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminInventoryItemPage,
		commerceAdminInventoryPage,
		page,
	}) => {
		const {sku} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			unitsOfMeasure: [],
			warehouseQuantities: [
				['Italy', 20],
				['United States - Northeast', 60],
				['United States - Southwest', 60],
			],
		});

		const expectInventoryRow = async (
			rowText: string,
			quantities: {[columnName: string]: string}
		) => {
			for (const [columnName, value] of Object.entries(quantities)) {
				await expect(
					await tableCellByColumnName(page, rowText, columnName)
				).toHaveText(value);
			}
		};

		await test.step('Without units of measure the SKU has a single line with an empty UOM', async () => {
			await commerceAdminInventoryPage.goto();

			await commerceAdminInventoryPage.searchByValue(sku.sku);

			await expect(
				page.getByRole('row').filter({hasText: sku.sku})
			).toHaveCount(1);

			await expectInventoryRow(sku.sku, {
				Available: '140',
				Incoming: '0',
				'On Hand': '140',
				'On Order': '0',
				UOM: '',
			});
		});

		for (const index of [1, 2]) {
			await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
				sku.id,
				{
					basePrice: index,
					key: `uomKey${index}`,
					name: {en_US: `uomName${index}`},
				}
			);
		}

		await test.step('The first unit of measure inherits the SKU quantities and the second starts empty', async () => {
			await commerceAdminInventoryPage.goto();

			await commerceAdminInventoryPage.searchByValue(sku.sku);

			await expect(
				page.getByRole('row').filter({hasText: sku.sku})
			).toHaveCount(2);

			await expectInventoryRow('uomKey1', {
				Available: '140',
				Incoming: '0',
				'On Hand': '140',
				'On Order': '0',
			});

			await expectInventoryRow('uomKey2', {
				Available: '0',
				Incoming: '0',
				'On Hand': '0',
				'On Order': '0',
			});
		});

		await test.step('Each combination carries its own warehouse inventory', async () => {
			await commerceAdminInventoryPage
				.inventoryRowSkuLink(sku.sku, 'uomKey1')
				.click();
			await expect(
				commerceAdminInventoryItemPage.heading(`${sku.sku} uomKey1`)
			).toBeVisible();

			await expect(
				commerceAdminInventoryItemPage.warehouseRowOnHandCell('Italy')
			).toHaveText('20');

			await commerceAdminInventoryItemPage
				.warehouseRowActionsButton('Italy')
				.click();
			await commerceAdminInventoryItemPage.editMenuItem.click();

			await commerceAdminInventoryItemPage.sidePanelQuantityInput.fill(
				'10'
			);
			await commerceAdminInventoryItemPage.sidePanelSaveButton.click();

			await expect(
				commerceAdminInventoryItemPage.warehouseRowOnHandCell('Italy')
			).toHaveText('10');
		});
	}
);

test(
	'The SKU inventory tab splits the warehouses per unit of measure and falls back on deletion',
	{tag: ['@COMMERCE-12324', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const warehouseNames = [
			'Italy',
			'United States - Northeast',
			'United States - Southwest',
		];

		const {product, sku} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			unitsOfMeasure: [],
			warehouseQuantities: [
				['Italy', 20],
				['United States - Northeast', 60],
				['United States - Southwest', 60],
			],
		});

		const goToSkuInventory = async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku.sku)
				.click();

			await commerceAdminProductDetailsSkusPage
				.skuTab('Inventory')
				.click();
		};

		await test.step('Without units of measure each warehouse has one row and an empty UOM', async () => {
			await goToSkuInventory();

			await expect(
				commerceAdminProductDetailsSkusPage.inventoryTableRows
			).toHaveCount(3);

			for (const [index, warehouseName] of warehouseNames.entries()) {
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowUnitOfMeasureCell(warehouseName)
				).toHaveText('');
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName)
				).toHaveValue(index === 0 ? '20' : '60');
			}
		});

		const unitsOfMeasure = [];

		for (const index of [1, 2]) {
			unitsOfMeasure.push(
				await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
					sku.id,
					{
						basePrice: index * 10,
						key: `uomKey${index}`,
						name: {en_US: `uomName${index}`},
						precision: 0,
					}
				)
			);
		}

		await test.step('The first unit of measure inherits the warehouse quantities and the second starts at zero', async () => {
			await goToSkuInventory();

			await expect(
				commerceAdminProductDetailsSkusPage.inventoryTableRows
			).toHaveCount(6);

			for (const [index, warehouseName] of warehouseNames.entries()) {
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName, 'uomKey1')
				).toHaveValue(index === 0 ? '20' : '60');
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName, 'uomKey2')
				).toHaveValue('0');
			}
		});

		await test.step('Each combination carries its own warehouse inventory', async () => {
			await patchUnitOfMeasureWarehouseItems(apiHelpers, {
				skuName: sku.sku,
				unitOfMeasureKeys: ['uomKey2'],
				warehouseQuantities: warehouseNames.map(
					(warehouseName, index): [string, number] => [
						warehouseName,
						index * 2 + 2,
					]
				),
			});

			await goToSkuInventory();

			for (const [index, warehouseName] of warehouseNames.entries()) {
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName, 'uomKey1')
				).toHaveValue(index === 0 ? '20' : '60');
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName, 'uomKey2')
				).toHaveValue(String(index * 2 + 2));
			}
		});

		await test.step('Deleting every unit of measure leaves the last one quantities as the SKU default', async () => {
			for (const unitOfMeasure of unitsOfMeasure) {
				await apiHelpers.headlessCommerceAdminCatalog.deleteSkuUnitOfMeasure(
					unitOfMeasure.id
				);
			}

			await goToSkuInventory();

			await expect(
				commerceAdminProductDetailsSkusPage.inventoryTableRows
			).toHaveCount(3);

			for (const [index, warehouseName] of warehouseNames.entries()) {
				await expect(
					commerceAdminProductDetailsSkusPage.inventoryTableRowUnitOfMeasureCell(warehouseName)
				).toHaveText('');
				await expect(commerceAdminProductDetailsSkusPage.inventoryTableRowQuantityInput(warehouseName)).toHaveValue(
					String(index * 2 + 2)
				);
			}
		});
	}
);

test(
	'A buyer sees the unit of measure and quantity of every order item through checkout',
	{tag: ['@COMMERCE-12405', '@LPD-107107']},
	async ({
		apiHelpers,
		checkoutPage,
		commerceMiniCartPage,
		page,
		pendingOrdersPage,
		productDetailsPage,
	}) => {
		const {orderItems} = await setUpOrderWithUnitsOfMeasure(apiHelpers, {
			commerceMiniCartPage,
			page,
			productDetailsPage,
		});

		await test.step('The pending order lists the unit of measure and quantity of each item', async () => {
			await page.goto(`/web${site.friendlyUrlPath}/pending-orders`, {
				waitUntil: 'networkidle',
			});

			await pendingOrdersPage.viewButton.click();

			await expectOrderItemUnitsOfMeasure(page, orderItems);
		});

		await test.step('The checkout order summary keeps them', async () => {
			await pendingOrdersPage.checkoutButton.click();

			await expect(checkoutPage.activeCheckoutStep).toContainText(
				'Shipping Address'
			);

			await checkoutPage.addAddress({
				city: 'Test City',
				countryLabel: 'United States',
				name: 'Test Address',
				regionLabel: 'Alabama',
				street: 'Test Street',
				zip: '12345',
			});

			await checkoutPage.performCheckoutUntilStep('Order Summary');

			for (const {
				productName,
				quantity,
				unitOfMeasureKey,
			} of orderItems) {
				await expect(
					checkoutPage.orderSummaryRowQuantity(
						productName,
						unitOfMeasureKey
					)
				).toHaveText(quantity);
				await expect(
					checkoutPage.orderSummaryRowUnitOfMeasure(
						productName,
						unitOfMeasureKey
					)
				).toHaveText(unitOfMeasureKey);
			}

			await checkoutPage.continueButton.click();

			await expect(checkoutPage.orderConfirmationContainer).toBeVisible();
		});

		await test.step('The placed order keeps them', async () => {
			await page.goto(`/web${site.friendlyUrlPath}/placed-orders`, {
				waitUntil: 'networkidle',
			});

			await pendingOrdersPage.viewButton.click();

			await expectOrderItemUnitsOfMeasure(page, orderItems);
		});
	}
);

test(
	'An order manager sees the unit of measure and quantity of every order item and follows a precision change',
	{tag: ['@COMMERCE-12531', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminOrdersPage,
		commerceMiniCartPage,
		page,
		productDetailsPage,
	}) => {
		const {account, decimalProduct, orderItems} =
			await setUpOrderWithUnitsOfMeasure(apiHelpers, {
				commerceMiniCartPage,
				page,
				productDetailsPage,
			});

		await performLogout(page);
		await performLoginViaApi({page, screenName: 'test'});

		const goToOrder = async () => {
			await commerceAdminOrdersPage.goto();

			await commerceAdminOrdersPage.menuActionButton(account.name).click();
			await commerceAdminOrdersPage.menuItemAction('View').click();
		};

		await test.step('The admin order lists the unit of measure and quantity of each item', async () => {
			await goToOrder();

			await expectOrderItemUnitsOfMeasure(page, orderItems);
		});

		await test.step('Raising the precision of a unit of measure leaves the quantity already captured on the order item', async () => {
			await apiHelpers.headlessCommerceAdminCatalog.patchSkuUnitOfMeasure(
				decimalProduct.unitsOfMeasure[0].id,
				{precision: 3}
			);

			expect(
				(
					await apiHelpers.headlessCommerceAdminCatalog.getSkuUnitOfMeasure(
						decimalProduct.unitsOfMeasure[0].id
					)
				).precision
			).toBe(3);

			await goToOrder();

			await expect(
				await tableCellByColumnName(
					page,
					[decimalProduct.sku.sku, 'uom3'],
					'Quantity'
				)
			).toHaveText('1.2');
		});
	}
);

test(
	'A unit of measure takes over the SKU price list and promotion prices',
	{tag: ['@COMMERCE-12338', '@LPD-107107']},
	async ({
		apiHelpers,
		commerceAdminPriceListDetailsPage,
		commerceAdminPriceListsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const {product, sku} = await setUpStockedUnitOfMeasures(apiHelpers, catalog.id, {
			price: 100,
			unitsOfMeasure: [],
		});

		const basePromoPriceList =
			await apiHelpers.headlessCommerceAdminPricing.getBasePromoPriceList(
				catalog.id
			);

		await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
			price: 90,
			priceListId: basePromoPriceList.items[0].id,
			skuId: sku.id,
		});

		await test.step('The Add Unit of Measure modal defaults to the SKU price list and promotion prices', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(sku.sku)
				.click();

			await commerceAdminProductDetailsSkusPage.goToSkuUOM();

			await commerceAdminProductDetailsSkusPage.addSkuUOMButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuUOMModalField(
					'Base Price'
				)
			).toHaveValue('100.00');
			await expect(
				commerceAdminProductDetailsSkusPage.skuUOMModalField(
					'Base Promotion Price'
				)
			).toHaveValue('90.00');
		});

		await test.step('Creating the unit of measure rewrites the base price on the SKU Price tab', async () => {
			await commerceAdminProductDetailsSkusPage
				.skuUOMModalField('Unit of Measure Required')
				.fill('Crate');
			await commerceAdminProductDetailsSkusPage
				.skuUOMModalField('Key Required')
				.fill('cr');

			for (const [label, price] of [
				['Base Price', '75'],
				['Base Promotion Price', '70'],
			]) {
				const input =
					commerceAdminProductDetailsSkusPage.skuUOMModalField(label);

				await input.fill(price);
				await input.blur();

				await expect(input).toHaveValue(price);
			}

			await commerceAdminProductDetailsSkusPage.skuUOMModalAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.uomTableRowLink('Crate')
			).toBeVisible();

			await expect(async () => {
				await commerceAdminProductDetailsSkusPage
					.skuTab('Price')
					.click({timeout: 5000});

				await expect(
					commerceAdminProductDetailsSkusPage.sidePanelPriceTableRowUnitPrice(
						`${catalog.name} Base Price List`
					)
				).toHaveText('$ 75.00', {timeout: 5000});
			}).toPass({timeout: 30000});
		});

		await test.step('The price list entry carries the unit of measure base price and key', async () => {
			await commerceAdminPriceListsPage.goto();

			await (
				await commerceAdminPriceListsPage.tableRowLink({
					colIndex: 0,
					rowValue: `${catalog.name} Base Price List`,
				})
			).click();

			await commerceAdminPriceListDetailsPage.entriesTab.click();

			await commerceAdminPriceListDetailsPage.searchByValue(sku.sku);

			await expect(
				await tableCellByColumnName(page, sku.sku, 'UOM')
			).toHaveText('cr');
			await expect(
				await tableCellByColumnName(page, sku.sku, 'Base Price')
			).toHaveText('$ 75.00');
		});

		await test.step('The promotion price list carries the unit of measure promotion price', async () => {
			const promoPriceEntries = (
				await apiHelpers.headlessCommerceAdminPricing.getPriceListEntries(
					basePromoPriceList.items[0].id
				)
			).items.filter(
				(priceEntry: {skuId: number}) => priceEntry.skuId === sku.id
			);

			expect(
				promoPriceEntries.map(
					(priceEntry: {price: number}) => priceEntry.price
				)
			).toContain(70);

			for (const priceEntry of promoPriceEntries) {
				expect(priceEntry.unitOfMeasureKey).toBe('cr');
			}
		});
	}
);

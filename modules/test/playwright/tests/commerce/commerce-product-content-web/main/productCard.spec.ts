/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {usersAndOrganizationsPagesTest} from '../../../../fixtures/usersAndOrganizationsPagesTest';
import getRandomString from '../../../../utils/getRandomString';
import performLogin, {
	performLoginViaApi,
	performLogout,
} from '../../../../utils/performLogin';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';
import {classicCommerceSetUp, miniumSetUp} from '../../utils/commerce';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	loginTest(),
	usersAndOrganizationsPagesTest
);

test(
	'COMMERCE-5864. Verify buyer can view the product card informations correctly',
	{tag: ['@LPD-56323']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		commerceThemeClassicCatalogPage,
		page,
		productDetailsPage,
	}) => {
		test.setTimeout(180000);

		let catalog;
		let channel;
		let product1;
		let product2;
		let product3;
		let product4;
		let site;

		await test.step('Initialize Commerce Classic Site', async () => {
			const {
				catalog: catalogSetup,
				channel: channelSetup,
				site: siteSetup,
			} = await classicCommerceSetUp(apiHelpers, getRandomString());

			catalog = catalogSetup;
			channel = channelSetup;
			site = siteSetup;
		});

		await test.step('Create an Account and a Buyer user', async () => {
			const account = await apiHelpers.headlessAdminUser.postAccount({
				name: getRandomString(),
				type: 'business',
			});

			const user =
				await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
					'demo.unprivileged@liferay.com'
				);
			const rolesResponse =
				await apiHelpers.headlessAdminUser.getAccountRoles(account.id);

			const accountRoleBuyer = rolesResponse?.items?.filter((role) => {
				return role.name === 'Buyer';
			});

			await apiHelpers.headlessAdminUser.assignAccountRoles(
				account.externalReferenceCode,
				accountRoleBuyer[0].id,
				user.emailAddress
			);
			const siteRole =
				await apiHelpers.headlessAdminUser.getRoleByName('Site Member');
			await apiHelpers.headlessAdminUser.assignUserToSite(
				siteRole.id,
				site.id,
				user.id
			);
			await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
				account.id,
				[user.emailAddress]
			);
		});

		await test.step('Create a simple product', async () => {
			product1 =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: 'Product1'},
				});
		});

		await test.step('Create a promo price list and discount for products', async () => {
			product2 = (
				await apiHelpers.headlessCommerceAdminCatalog.getProducts(
					new URLSearchParams({
						filter: `name eq 'Transmission Cooler Line Assembly'`,
					})
				)
			).items[0];

			product3 = (
				await apiHelpers.headlessCommerceAdminCatalog.getProducts(
					new URLSearchParams({
						filter: `name eq 'Torque Converters'`,
					})
				)
			).items[0];

			product4 = (
				await apiHelpers.headlessCommerceAdminCatalog.getProducts(
					new URLSearchParams({
						filter: `name eq 'Brake Fluid'`,
					})
				)
			).items[0];

			await apiHelpers.headlessCommerceAdminPricing.postDiscount({
				discountProducts: [
					{
						productId: product3.productId,
					},
				],
				percentageLevel1: 10,
				usePercentage: true,
			});

			const basePriceListId =
				await apiHelpers.headlessCommerceAdminPricing.getBasePriceListId(
					catalog.id
				);

			await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
				price: 20,
				priceListId: basePriceListId.items[0].id,
				skuId: product4.skus[0].id,
			});

			await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
				price: 40,
				priceListId: basePriceListId.items[0].id,
				skuId: product4.skus[1].id,
			});

			await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
				price: 60,
				priceListId: basePriceListId.items[0].id,
				skuId: product4.skus[2].id,
			});

			const basePromoPriceListId =
				await apiHelpers.headlessCommerceAdminPricing.getBasePromoPriceListId(
					catalog.id
				);

			await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
				price: 10,
				priceListId: basePromoPriceListId.items[0].id,
				skuId: product2.skus[0].id,
			});
		});

		await test.step('USD is the default currency for the channel', async () => {
			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await expect(
				commerceAdminChannelDetailsPage.channelCurrencySelect
			).toHaveValue('USD');
		});

		await test.step('As a Buyer, when the product card image is clicked, the user is redirected to the product page', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'demo.unprivileged'});

			await page.goto(`/web/${site.name}`);

			await commerceThemeClassicCatalogPage
				.productCardImage(product1.name['en_US'])
				.click();

			await expect(page).toHaveURL(/.*product1/);

			await expect(
				await productDetailsPage.priceField('$ 0.00')
			).toBeVisible();
		});

		await test.step('Change the default channel currency', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'test'});

			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await commerceAdminChannelDetailsPage.changeChannelDefaultCurrency(
				'EUR'
			);
		});

		await test.step('As a Buyer, currency in product card is changed', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'demo.unprivileged'});

			await page.goto(`/web/${site.name}`);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product1.name['en_US'],
					'€ 0.00'
				)
			).toBeVisible();
		});

		await test.step('Reset the default channel currency', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'test'});

			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await commerceAdminChannelDetailsPage.changeChannelDefaultCurrency(
				'USD'
			);
		});

		await test.step('Product card price is visible and the add to cart button is enabled', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'demo.unprivileged'});

			await page.goto(`/web/${site.name}`);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product1.name['en_US'],
					'$ 0.00'
				)
			).toBeVisible();
			await expect(
				commerceThemeClassicCatalogPage.productCardAddToCartButton(
					product1.name['en_US']
				)
			).toBeEnabled();
		});

		await test.step('Also promo and discount prices are visible in the product card', async () => {
			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product2.name['en_US'],
					'$ 15.00'
				)
			).toHaveClass(/price-value-inactive/);
			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product2.name['en_US'],
					'$ 10.00'
				)
			).toHaveClass(/price-value-promo/);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product3.name['en_US'],
					'$ 34.00'
				)
			).toHaveClass(/price-value-inactive/);
			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product3.name['en_US'],
					'$ 30.60'
				)
			).toHaveClass(/price-value-final/);

			await commerceThemeClassicCatalogPage.selectSorting(
				'Name Ascending'
			);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product4.name['en_US'],
					'From $ 20.00'
				)
			).toBeVisible();

			await expect(
				commerceThemeClassicCatalogPage.productCardSku(
					product4.name['en_US'],
					product4.skus[0].sku
				)
			).not.toBeVisible();

			await expect(
				commerceThemeClassicCatalogPage.productCardAddToCartButton(
					product4.name['en_US']
				)
			).not.toBeVisible();
		});

		await test.step('AllowBackOrder is disabled', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'test'});

			await apiHelpers.headlessCommerceAdminCatalog.patchProduct(
				product1.productId,
				{
					name: {en_US: product1.name['en_US']},
					productConfiguration: {
						allowBackOrder: false,
					},
				}
			);
		});

		await test.step('As a Buyer, the add to cart button in the product card is disabled', async () => {
			await performLogout(page);
			await performLogin(page, 'demo.unprivileged');

			await page.goto(`/web/${site.name}`);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product1.name['en_US'],
					'$ 0.00'
				)
			).toBeVisible();

			await expect(
				commerceThemeClassicCatalogPage.productCardAddToCartButton(
					product1.name['en_US']
				)
			).not.toBeEnabled();
		});

		await test.step('Product sku purchasable is disabled', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'test'});

			await apiHelpers.headlessCommerceAdminCatalog.patchProduct(
				product1.productId,
				{
					name: {en_US: product1.name['en_US']},
					productConfiguration: {
						allowBackOrder: true,
					},
					skus: [
						{
							purchasable: false,
						},
					],
				}
			);
		});

		await test.step('As a Buyer, the add to cart button in the product card is disabled', async () => {
			await performLogout(page);
			await performLogin(page, 'demo.unprivileged');

			await page.goto(`/web/${site.name}`);

			await expect(
				commerceThemeClassicCatalogPage.productCardPrice(
					product1.name['en_US'],
					'$ 0.00'
				)
			).toBeVisible();

			await expect(
				commerceThemeClassicCatalogPage.productCardAddToCartButton(
					product1.name['en_US']
				)
			).not.toBeEnabled();
		});
	}
);

test('COMMERCE-6193. As a buyer, I want the first selectable quantity of a product to be the minimum multiple quantity if Minimum Order Quantity is higher than Multiple Order Quantity', async ({
	apiHelpers,
	commerceThemeMiniumCatalogPage,
	page,
}) => {
	const {site} = await miniumSetUp(apiHelpers);

	const account = await apiHelpers.headlessAdminUser.postAccount({
		name: getRandomString(),
		type: 'business',
	});

	const user =
		await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
			'demo.unprivileged@liferay.com'
		);
	await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
		account.id,
		['demo.unprivileged@liferay.com']
	);

	const companyId = await page.evaluate(() => {
		return Liferay.ThemeDisplay.getCompanyId();
	});

	const role = await apiHelpers.headlessAdminUser.postRole({
		name: 'Buyer ' + getRandomString(),
		rolePermissions: [
			{
				actionIds: ['MANAGE_ADDRESSES', 'VIEW_ADDRESSES'],
				primaryKey: '0',
				resourceName: 'com.liferay.account.model.AccountEntry',
				scope: 3,
			},
			{
				actionIds: ['VIEW'],
				primaryKey: companyId,
				resourceName: 'com.liferay.commerce.model.CommerceOrderType',
				scope: 1,
			},
			{
				actionIds: [
					'ADD_COMMERCE_ORDER',
					'CHECKOUT_OPEN_COMMERCE_ORDERS',
					'MANAGE_COMMERCE_ORDER_DELIVERY_TERMS',
					'MANAGE_COMMERCE_ORDER_PAYMENT_METHODS',
					'MANAGE_COMMERCE_ORDER_PAYMENT_TERMS',
					'MANAGE_COMMERCE_ORDER_SHIPPING_OPTIONS',
					'VIEW_BILLING_ADDRESS',
					'VIEW_COMMERCE_ORDERS',
					'VIEW_OPEN_COMMERCE_ORDERS',
				],
				primaryKey: '0',
				resourceName: 'com.liferay.commerce.order',
				scope: 3,
			},
		],
	});

	await apiHelpers.headlessAdminUser.postRoleUserAccountAssociation(
		role.id,
		user.id
	);

	apiHelpers.data.push({
		id: `${role.id}_${user.id}`,
		type: 'roleUserAccountAssociation',
	});

	await apiHelpers.jsonWebServicesUser.addGroupUsers(site.id, [user.id]);

	const product = (
		await apiHelpers.headlessCommerceAdminCatalog.getProducts(
			new URLSearchParams({
				filter: `name eq 'U-Joint'`,
			})
		)
	).items[0];

	const productName = product.name['en_US'];

	await apiHelpers.headlessCommerceAdminCatalog.patchProduct(
		product.productId,
		{
			name: {en_US: productName},
			productConfiguration: {
				minOrderQuantity: 6,
				multipleOrderQuantity: 5,
			},
		}
	);

	const patchedProduct = (
		await apiHelpers.headlessCommerceAdminCatalog.getProducts(
			new URLSearchParams({
				filter: `name eq 'U-Joint'`,
				nestedFields: `skus,productConfiguration`,
			})
		)
	).items[0];

	const multipleQuantity = commerceThemeMiniumCatalogPage.getMultipleQuantity(
		0,
		patchedProduct.productConfiguration.multipleOrderQuantity
	);
	const minQuantity = commerceThemeMiniumCatalogPage.getProductMinQuantity(
		patchedProduct.productConfiguration.minOrderQuantity,
		multipleQuantity
	);
	const maxQuantity = commerceThemeMiniumCatalogPage.getProductMaxQuantity(
		patchedProduct.productConfiguration.maxOrderQuantity,
		multipleQuantity
	);

	await performLogout(page);
	await performLogin(page, user.alternateName);

	await page.goto(`/web/${site.name}`);

	let minQuantityNotSatisfied;
	let multipleQuantityNotSatisfied;
	let maxQuantityNotSatisfied;

	for (const quantitySelectorActualQuantity of [5, 20]) {
		await commerceThemeMiniumCatalogPage
			.quantitySelector(
				commerceThemeMiniumCatalogPage.productCard(productName)
			)
			.fill(`${quantitySelectorActualQuantity}`);

		await expect(
			commerceThemeMiniumCatalogPage.quantitySelector(
				commerceThemeMiniumCatalogPage.productCard(productName)
			)
		).toHaveValue(`${quantitySelectorActualQuantity}`);

		maxQuantityNotSatisfied = quantitySelectorActualQuantity > maxQuantity;
		minQuantityNotSatisfied = quantitySelectorActualQuantity < minQuantity;
		multipleQuantityNotSatisfied = !Number.isInteger(
			quantitySelectorActualQuantity / multipleQuantity
		);

		await commerceThemeMiniumCatalogPage.checkQuantitiesInPopOverMessages(
			maxQuantity,
			minQuantity,
			multipleQuantity,
			maxQuantityNotSatisfied,
			minQuantityNotSatisfied,
			multipleQuantityNotSatisfied
		);
	}
});

test(
	'Add and remove products from the wish list via the product card',
	{tag: ['@COMMERCE-5866', '@COMMERCE-5867', '@LPD-96522']},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		commerceThemeClassicCatalogPage,
		commerceWishListPage,
		page,
	}) => {
		test.setTimeout(180000);

		const product1Name = 'Wish List Product One ' + getRandomString();
		const product2Name = 'Wish List Product Two ' + getRandomString();

		let catalogLayout;
		let channel;
		let site;
		let wishListLayout;

		await test.step('Create a commerce site and build the storefront pages', async () => {
			site = await apiHelpers.headlessAdminSite.postSite({
				name: getRandomString(),
			});

			catalogLayout = await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition([
					getWidgetDefinition({
						id: getRandomString(),
						widgetName:
							'com_liferay_commerce_product_content_web_internal_portlet_CPPublisherPortlet',
					}),
				]),
				siteId: site.id,
				title: getRandomString(),
			});

			wishListLayout = await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition([
					getWidgetDefinition({
						id: getRandomString(),
						widgetName:
							'com_liferay_commerce_wish_list_web_internal_portlet_CommerceWishListContentPortlet',
					}),
				]),
				siteId: site.id,
				title: getRandomString(),
			});
		});

		await test.step('Create a channel, catalog and products', async () => {
			channel = await apiHelpers.headlessCommerceAdminChannel.postChannel(
				{
					siteGroupId: site.id,
				}
			);

			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: product1Name},
			});
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: product2Name},
			});
		});

		await test.step('Set the channel site type to B2B', async () => {
			await commerceAdminChannelsPage.changeCommerceChannelSiteType(
				channel.name,
				'B2B'
			);
		});

		await test.step('Create a buyer for the account', async () => {
			const account = await apiHelpers.headlessAdminUser.postAccount({
				name: getRandomString(),
				type: 'business',
			});

			const user =
				await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
					'demo.unprivileged@liferay.com'
				);
			const rolesResponse =
				await apiHelpers.headlessAdminUser.getAccountRoles(account.id);

			const accountRoleBuyer = rolesResponse?.items?.filter((role) => {
				return role.name === 'Buyer';
			});

			await apiHelpers.headlessAdminUser.assignAccountRoles(
				account.externalReferenceCode,
				accountRoleBuyer[0].id,
				user.emailAddress
			);
			const siteRole =
				await apiHelpers.headlessAdminUser.getRoleByName('Site Member');
			await apiHelpers.headlessAdminUser.assignUserToSite(
				siteRole.id,
				site.id,
				user.id
			);
			await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
				account.id,
				[user.emailAddress]
			);
		});

		await test.step('As a buyer, add two products to the wish list from their cards', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'demo.unprivileged'});

			await page.goto(
				`/web/${site.name}/${catalogLayout.friendlyUrlPath}`
			);

			await commerceThemeClassicCatalogPage
				.productCardAddToWishListButton(product1Name)
				.click();
			await commerceThemeClassicCatalogPage
				.productCardAddToWishListButton(product2Name)
				.click();
		});

		await test.step('Both products appear in the wish list', async () => {
			await page.goto(
				`/web/${site.name}/${wishListLayout.friendlyUrlPath}`
			);

			await expect(
				commerceWishListPage.wishListItem(product1Name)
			).toBeVisible();
			await expect(
				commerceWishListPage.wishListItem(product2Name)
			).toBeVisible();
		});

		await test.step('Removing a product from the wish list drops only that product', async () => {
			await commerceWishListPage
				.wishListItemDeleteButton(product1Name)
				.click();

			await expect(
				commerceWishListPage.wishListItem(product1Name)
			).not.toBeVisible();
			await expect(
				commerceWishListPage.wishListItem(product2Name)
			).toBeVisible();
		});
	}
);

test(
	'View product availability on the product card',
	{tag: ['@COMMERCE-5871', '@COMMERCE-5872', '@LPD-96522']},
	async ({
		apiHelpers,
		commerceAdminChannelsPage,
		commerceThemeClassicCatalogPage,
		page,
	}) => {
		test.setTimeout(180000);

		const availableProductName = 'Available Product ' + getRandomString();
		const unavailableProductName =
			'Unavailable Product ' + getRandomString();

		let catalogLayout;
		let channel;
		let site;

		await test.step('Create a commerce site and a catalog page', async () => {
			site = await apiHelpers.headlessAdminSite.postSite({
				name: getRandomString(),
			});

			catalogLayout = await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition([
					getWidgetDefinition({
						id: getRandomString(),
						widgetName:
							'com_liferay_commerce_product_content_web_internal_portlet_CPPublisherPortlet',
					}),
				]),
				siteId: site.id,
				title: getRandomString(),
			});
		});

		await test.step('Create a channel, catalog and two products with availability display', async () => {
			channel = await apiHelpers.headlessCommerceAdminChannel.postChannel(
				{
					siteGroupId: site.id,
				}
			);

			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const availableProduct =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: availableProductName},
				});
			await apiHelpers.headlessCommerceAdminCatalog.patchProduct(
				String(availableProduct.productId),
				{
					name: {en_US: availableProductName},
					productConfiguration: {
						displayAvailability: true,
					},
				}
			);

			const unavailableProduct =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: unavailableProductName},
				});
			await apiHelpers.headlessCommerceAdminCatalog.patchProduct(
				String(unavailableProduct.productId),
				{
					name: {en_US: unavailableProductName},
					productConfiguration: {
						allowBackOrder: false,
						displayAvailability: true,
					},
				}
			);

			const warehouse =
				await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehouses(
					{active: true, latitude: 40, longitude: -74}
				);

			await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehousesChannels(
				warehouse.id,
				channel.id
			);

			await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehousesWarehouseItems(
				warehouse.id,
				{quantity: 100, sku: availableProduct.skus[0].sku}
			);
		});

		await test.step('Set the channel site type to B2B', async () => {
			await commerceAdminChannelsPage.changeCommerceChannelSiteType(
				channel.name,
				'B2B'
			);
		});

		await test.step('Create a buyer for the account', async () => {
			const account = await apiHelpers.headlessAdminUser.postAccount({
				name: getRandomString(),
				type: 'business',
			});

			const user =
				await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
					'demo.unprivileged@liferay.com'
				);
			const rolesResponse =
				await apiHelpers.headlessAdminUser.getAccountRoles(account.id);

			const accountRoleBuyer = rolesResponse?.items?.filter((role) => {
				return role.name === 'Buyer';
			});

			await apiHelpers.headlessAdminUser.assignAccountRoles(
				account.externalReferenceCode,
				accountRoleBuyer[0].id,
				user.emailAddress
			);
			const siteRole =
				await apiHelpers.headlessAdminUser.getRoleByName('Site Member');
			await apiHelpers.headlessAdminUser.assignUserToSite(
				siteRole.id,
				site.id,
				user.id
			);
			await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
				account.id,
				[user.emailAddress]
			);
		});

		await test.step('As a buyer, each product card shows its availability', async () => {
			await performLogout(page);
			await performLoginViaApi({page, screenName: 'demo.unprivileged'});

			await page.goto(
				`/web/${site.name}/${catalogLayout.friendlyUrlPath}`
			);

			await expect(
				commerceThemeClassicCatalogPage.productCardAvailabilityLabel(
					availableProductName
				)
			).toHaveText('Available');
			await expect(
				commerceThemeClassicCatalogPage.productCardAvailabilityLabel(
					unavailableProductName
				)
			).toHaveText('Unavailable');
		});
	}
);

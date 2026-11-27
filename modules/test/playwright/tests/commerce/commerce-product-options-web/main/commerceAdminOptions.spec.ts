/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getRandomString from '../../../../utils/getRandomString';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test('LPD-26249 Configure options and Product options', async ({
	apiHelpers,
	commerceAdminProductDetailsPage,
	commerceAdminProductDetailsProductOptionsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const option1 = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		'color',
		'Color',
		1
	);

	const option2 = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		'size',
		'Size',
		2
	);

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: 'Simple T-Shirt'},
		productOptions: [
			{
				fieldType: 'select',
				key: 'color',
				name: {
					en_US: 'Color',
				},
				optionId: option1.id,
				priority: 1,
				productOptionValues: [
					{
						key: 'black',
						name: {
							en_US: 'Black',
						},
						priority: 1,
					},
					{
						key: 'white',
						name: {
							en_US: 'White',
						},
						priority: 2,
					},
				],
				skuContributor: true,
			},
			{
				fieldType: 'select',
				key: 'size',
				name: {
					en_US: 'Size',
				},
				optionId: option2.id,
				priority: 2,
				productOptionValues: [
					{
						key: 'xs',
						name: {
							en_US: 'XS',
						},
						priority: 1,
					},
					{
						key: 'xl',
						name: {
							en_US: 'XL',
						},
						priority: 2,
					},
				],
				skuContributor: true,
			},
		],
	});

	await commerceAdminProductPage.gotoProduct(product.name['en_US']);

	await commerceAdminProductDetailsPage.goToProductOptions();

	await expect(
		(
			await commerceAdminProductDetailsProductOptionsPage.tableRow(
				0,
				option1.name['en_US'],
				true
			)
		).row
	).toBeVisible();
});

test('LPD-45740 Product options can be added from product admins', async ({
	apiHelpers,
	commerceAdminProductDetailsPage,
	commerceAdminProductDetailsProductOptionsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: 'Simple T-Shirt'},
	});

	await commerceAdminProductPage.gotoProduct(product.name['en_US']);

	await commerceAdminProductDetailsPage.goToProductOptions();

	const optionName = getRandomString();

	await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.fill(
		optionName
	);

	await commerceAdminProductDetailsProductOptionsPage.createNewOptionsButton.click();

	await waitForAlert(page, 'Success:Option Created');

	await page.reload();

	await expect(
		(
			await commerceAdminProductDetailsProductOptionsPage.tableRow(
				0,
				optionName,
				true
			)
		).row
	).toBeVisible();
});

test(
	'Product options can be deleted from product admins',
	{tag: ['@LPD-45740']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		page,
	}) => {
		await page.goto('/');

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			'color',
			'Color',
			1
		);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Simple T-Shirt'},
				productOptions: [
					{
						fieldType: 'select',
						key: 'color',
						name: {
							en_US: 'Color',
						},
						optionId: option.id,
						priority: 1,
						productOptionValues: [
							{
								key: 'black',
								name: {
									en_US: 'Black',
								},
								priority: 1,
							},
							{
								key: 'white',
								name: {
									en_US: 'White',
								},
								priority: 2,
							},
						],
						skuContributor: true,
					},
				],
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductOptions();

		await expect(
			(
				await commerceAdminProductDetailsProductOptionsPage.tableRow(
					0,
					option.name['en_US'],
					true
				)
			).row
		).toBeVisible();

		await commerceAdminProductDetailsProductOptionsPage
			.optionActionsButton(option.name['en_US'])
			.click();

		await commerceAdminProductDetailsProductOptionsPage.deleteMenuItem.click();

		await waitForAlert(page);
	}
);

test(
	'Change the price type of a bundle option and link its values to SKUs of other catalogs',
	{
		tag: [
			'@COMMERCE-6018',
			'@COMMERCE-6020',
			'@COMMERCE-10798',
			'@LPD-108805',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		page,
	}) => {
		await page.goto('/');

		const bundleCatalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();
		const otherCatalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const bundleCatalogProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: bundleCatalog.id,
				name: {en_US: getRandomString()},
			});
		const otherCatalogProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: otherCatalog.id,
				name: {en_US: getRandomString()},
			});

		const optionKey = getRandomString();

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			optionKey,
			'Color'
		);

		const bundleName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: bundleCatalog.id,
			name: {en_US: bundleName},
			productOptions: [
				{
					fieldType: 'select',
					key: optionKey,
					name: {en_US: 'Color'},
					optionId: option.id,
					priceType: '',
					priority: 1,
					productOptionValues: [
						{
							key: 'black',
							name: {en_US: 'Black'},
							priority: 1,
						},
					],
					required: true,
					skuContributor: true,
				},
			],
		});

		await commerceAdminProductPage.gotoProduct(bundleName);

		await commerceAdminProductDetailsPage.goToProductOptions();

		await commerceAdminProductDetailsProductOptionsPage.openOption('Color');

		await test.step('Link the option value to a SKU of another catalog with a dynamic price type', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'dynamic'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await commerceAdminProductDetailsProductOptionsPage.editOptionValue(
				'Black',
				{
					quantity: '1',
					sku: otherCatalogProduct.skus[0].sku,
				}
			);
		});

		await test.step('Link the option value to a SKU of the same catalog with a static price type', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'static'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await commerceAdminProductDetailsProductOptionsPage.editOptionValue(
				'Black',
				{
					deltaPrice: '60',
					quantity: '1',
					sku: bundleCatalogProduct.skus[0].sku,
				}
			);
		});

		await test.step('Change the price type after deleting the option value', async () => {
			await commerceAdminProductDetailsProductOptionsPage
				.optionValueRowActionsButton('Black')
				.click();
			await commerceAdminProductDetailsProductOptionsPage
				.optionValueRowMenuItem('Delete')
				.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueRow(
					'Black'
				)
			).toHaveCount(0);

			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'dynamic'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame.locator(
					'.alert-danger'
				)
			).toHaveCount(0);

			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'static'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await commerceAdminProductDetailsProductOptionsPage.closeOption();

			await commerceAdminProductDetailsProductOptionsPage.openOption(
				'Color'
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect
			).toHaveValue('static');
		});
	}
);

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getGlobalSiteId from '../../../../utils/getGlobalSiteId';
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

test(
	'Configure an option with values defined externally by categories',
	{
		tag: [
			'@COMMERCE-12093',
			'@COMMERCE-12110',
			'@COMMERCE-12137',
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

		const site = await apiHelpers.headlessAdminSite.postSite({
			name: getRandomString(),
		});

		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: Number(site.id),
		});

		const vocabulary =
			await apiHelpers.headlessAdminTaxonomy.postSiteTaxonomyVocabulary({
				name: getRandomString(),
				siteId: await getGlobalSiteId(apiHelpers),
			});

		const category1 =
			await apiHelpers.headlessAdminTaxonomy.postTaxonomyVocabularyTaxonomyCategory(
				{name: getRandomString(), vocabularyId: vocabulary.id}
			);
		const category2 =
			await apiHelpers.headlessAdminTaxonomy.postTaxonomyVocabularyTaxonomyCategory(
				{name: getRandomString(), vocabularyId: vocabulary.id}
			);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();
		const otherCatalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const category1ProductName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			categories: [{id: category1.id, name: category1.name}],
			name: {en_US: category1ProductName},
		});

		const category2ProductName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			categories: [{id: category2.id, name: category2.name}],
			name: {en_US: category2ProductName},
		});

		const otherCatalogProductName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: otherCatalog.id,
			name: {en_US: otherCatalogProductName},
		});

		const optionKey = getRandomString();
		const optionName = getRandomString();

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			optionKey,
			optionName
		);

		const productName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			categories: [{id: category1.id, name: category1.name}],
			name: {en_US: productName},
			productOptions: [
				{
					fieldType: 'select',
					key: optionKey,
					name: {en_US: optionName},
					optionId: option.id,
					priority: 1,
					productOptionValues: ['value1', 'value2', 'value3'].map(
						(value, index) => ({
							key: value,
							name: {en_US: value},
							priority: index + 1,
						})
					),
				},
			],
		});

		await commerceAdminProductPage.gotoProduct(productName);

		await commerceAdminProductDetailsPage.goToProductOptions();

		await commerceAdminProductDetailsProductOptionsPage.openOption(
			optionName
		);

		const optionSidePanelFrame =
			commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame;

		await test.step('Reject a static price type for externally defined values', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionDefineExternallyToggle.check();
			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'static'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(
				optionSidePanelFrame,
				'Price type can only be dynamic for externally defined product options.',
				{type: 'danger'}
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionDefineExternallyToggle
			).not.toBeChecked();
		});

		await test.step('Define the option values externally by categories', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionDefineExternallyToggle.check();
			await commerceAdminProductDetailsProductOptionsPage.optionPriceTypeSelect.selectOption(
				'dynamic'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(optionSidePanelFrame);

			await commerceAdminProductDetailsProductOptionsPage.optionCollectionProviderSelect.selectOption(
				{label: 'SKUs by Categories'}
			);

			await expect(async () => {
				await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click(
					{timeout: 5000}
				);

				await waitForAlert(
					optionSidePanelFrame,
					'Success:Your request completed successfully.',
					{timeout: 5000}
				);
			}).toPass();
		});

		await test.step('List the products of the selected category', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionCategoryInput.fill(
				category2.name
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionCategoryMenuItem(
					category2.name
				)
			).toBeVisible();

			await commerceAdminProductDetailsProductOptionsPage.optionCategoryInput.fill(
				category1.name
			);
			await commerceAdminProductDetailsProductOptionsPage
				.optionCategoryMenuItem(category1.name)
				.click();
			await commerceAdminProductDetailsProductOptionsPage.optionCategoryInput.blur();
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(optionSidePanelFrame);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueRow(
					category1ProductName
				)
			).toBeVisible();

			for (const name of [
				category2ProductName,
				otherCatalogProductName,
				productName,
			]) {
				await expect(
					commerceAdminProductDetailsProductOptionsPage.optionValueRow(
						name
					)
				).toHaveCount(0);
			}
		});

		await test.step('Restore the manual option values', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionDefineExternallyToggle.uncheck();
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(optionSidePanelFrame);

			for (const value of ['value1', 'value2', 'value3']) {
				await expect(
					commerceAdminProductDetailsProductOptionsPage.optionValueRow(
						value
					)
				).toBeVisible();
			}
		});

		await test.step('Define the option values externally again', async () => {
			await commerceAdminProductDetailsProductOptionsPage.optionDefineExternallyToggle.check();
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(optionSidePanelFrame);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueRow(
					category1ProductName
				)
			).toBeVisible();
			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueRow(
					'value1'
				)
			).toHaveCount(0);
		});

		await test.step('List the products of every catalog without a category', async () => {
			await commerceAdminProductDetailsProductOptionsPage
				.optionCategoryRemoveButton(category1.name)
				.click();
			await commerceAdminProductDetailsProductOptionsPage.optionCategoryInput.press(
				'Tab'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

			await waitForAlert(optionSidePanelFrame);

			for (const name of [
				category1ProductName,
				category2ProductName,
				otherCatalogProductName,
			]) {
				await commerceAdminProductDetailsProductOptionsPage.optionValueSearchInput.fill(
					name
				);
				await commerceAdminProductDetailsProductOptionsPage.optionValueSearchInput.press(
					'Enter'
				);

				await expect(
					commerceAdminProductDetailsProductOptionsPage.optionValueRow(
						name
					)
				).toBeVisible();
			}
		});
	}
);

test(
	'Create options of every field type from the product options and find them in the Options admin',
	{tag: ['@COMMERCE-6017', '@COMMERCE-6269', '@LPD-40568', '@LPD-108805']},
	async ({
		apiHelpers,
		commerceAdminOptionsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		page,
	}) => {
		await page.goto('/');

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductOptions();

		const optionNames: string[] = [];

		try {
			await test.step('Create an option of every field type', async () => {
				for (const fieldType of [
					'Text',
					'Single Selection',
					'Multiple Selection',
					'Date',
					'Numeric',
					'Boolean',
				]) {
					const optionName = getRandomString();

					optionNames.push(optionName);

					await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.fill(
						optionName
					);
					await commerceAdminProductDetailsProductOptionsPage.createNewOptionsButton.click();

					await waitForAlert(page, 'Success:Option Created');

					await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.press(
						'Escape'
					);

					await expect(
						commerceAdminProductDetailsProductOptionsPage.optionRow(
							optionName
						)
					).toContainText('Select from List');

					await commerceAdminProductDetailsProductOptionsPage.openOption(
						optionName
					);

					await commerceAdminProductDetailsProductOptionsPage.optionFieldTypeSelect.selectOption(
						{label: fieldType}
					);
					await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

					await waitForAlert(
						commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
					);

					await commerceAdminProductDetailsProductOptionsPage.optionSidePanelCancelButton.click();

					await expect(
						commerceAdminProductDetailsProductOptionsPage.optionRow(
							optionName
						)
					).toContainText(fieldType);
				}
			});

			await test.step('Find the options in the Options admin', async () => {
				await commerceAdminOptionsPage.goto();

				for (const optionName of optionNames) {
					await expect(
						commerceAdminOptionsPage.optionRow(optionName)
					).toBeVisible();
				}
			});
		}
		finally {
			for (const optionName of optionNames) {
				const options =
					await apiHelpers.headlessCommerceAdminCatalog.getOptions(
						optionName
					);

				for (const option of options.items) {
					apiHelpers.data.unshift({id: option.id, type: 'option'});
				}
			}
		}
	}
);

test(
	'Create and delete options from the Options admin',
	{tag: ['@COMMERCE-6280', '@COMMERCE-7821', '@LPD-14865', '@LPD-108805']},
	async ({apiHelpers, commerceAdminOptionsPage, page}) => {
		const selectOptionName = getRandomString();
		const textOptionName = getRandomString();

		try {
			await page.goto('/');

			await test.step('Create a text option with a description', async () => {
				await commerceAdminOptionsPage.goto();

				await commerceAdminOptionsPage.addOption({
					fieldType: 'Text',
					key: getRandomString(),
					name: textOptionName,
				});

				const description = getRandomString();

				await commerceAdminOptionsPage.descriptionInput.fill(
					description
				);
				await commerceAdminOptionsPage.saveButton.click();

				await waitForAlert(page);

				await commerceAdminOptionsPage.goto();

				await commerceAdminOptionsPage
					.optionRowActionsButton(textOptionName)
					.click();
				await commerceAdminOptionsPage.rowMenuItem('Edit').click();

				await expect(commerceAdminOptionsPage.nameInput).toHaveValue(
					textOptionName
				);
				await expect(
					commerceAdminOptionsPage.descriptionInput
				).toHaveValue(description);
			});

			await test.step('Create a select from list option with a value', async () => {
				await commerceAdminOptionsPage.goto();

				await commerceAdminOptionsPage.addOption({
					fieldType: 'Select from List',
					key: getRandomString(),
					name: selectOptionName,
				});

				await commerceAdminOptionsPage.addOptionValue({
					key: getRandomString(),
					name: 'Black',
				});

				await commerceAdminOptionsPage.optionValueLink('Black').click();

				await expect(
					commerceAdminOptionsPage.optionValueSidePanelTitle
				).toHaveText('Edit Black');
			});

			await test.step('Delete the options', async () => {
				await commerceAdminOptionsPage.goto();

				await commerceAdminOptionsPage.deleteOption(selectOptionName);
				await commerceAdminOptionsPage.deleteOption(textOptionName);
			});
		}
		finally {
			for (const optionName of [selectOptionName, textOptionName]) {
				const options =
					await apiHelpers.headlessCommerceAdminCatalog.getOptions(
						optionName
					);

				for (const option of options.items) {
					await apiHelpers.headlessCommerceAdminCatalog.deleteOption(
						option.id
					);
				}
			}
		}
	}
);

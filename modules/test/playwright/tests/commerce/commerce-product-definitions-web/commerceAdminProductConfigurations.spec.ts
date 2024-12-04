/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../../fixtures/applicationsMenuPageTest';
import {commercePagesTest} from '../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {waitForAlert} from '../../../utils/waitForAlert';

export const test = mergeTests(
	applicationsMenuPageTest,
	dataApiHelpersTest,
	commercePagesTest,
	featureFlagsTest({
		'LPD-10889': true,
	}),
	loginTest()
);

test('LPD-42555 Verify configuration list table appears', async ({
	applicationsMenuPage,
	commerceAdminProductConfigurationListsPage,
}) => {
	await applicationsMenuPage.goToCommerceProductConfigurationLists(false);

	await expect(
		commerceAdminProductConfigurationListsPage.table
	).toBeVisible();
});

test('LPD-43013 Configuration Entry form in side panel', async ({
	apiHelpers,
	applicationsMenuPage,
	commerceAdminProductConfigurationEntriesPage,
	commerceAdminProductConfigurationEntryPage,
	commerceAdminProductConfigurationListPage,
	commerceAdminProductConfigurationListsPage,
	page,
}) => {
	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
	});

	const configurationLists =
		await apiHelpers.headlessCommerceAdminCatalog.getProductConfigurationListsPage();

	expect(configurationLists.items?.length).toBeGreaterThan(0);

	const configurationList = configurationLists.items.find((item) =>
		item.name.includes(catalog.name)
	);

	expect(configurationList).not.toBeNull();

	const configurationEntry =
		await apiHelpers.headlessCommerceAdminCatalog.postProductConfiguration(
			configurationList.id,
			{
				entityExternalReferenceCode: product.externalReferenceCode,
				entityId: product.id,
			}
		);

	await applicationsMenuPage.goToCommerceProductConfigurationLists();

	await (
		await commerceAdminProductConfigurationListsPage.tableRowLink({
			colIndex: 0,
			rowValue: configurationList.name,
		})
	).click();

	await commerceAdminProductConfigurationListPage.entriesMenuItem.click();

	await expect(
		commerceAdminProductConfigurationEntriesPage.table
	).toBeVisible();

	await (
		await commerceAdminProductConfigurationEntriesPage.tableRowLink({
			colIndex: 0,
			rowValue: configurationEntry.id,
		})
	).click();

	await expect(
		commerceAdminProductConfigurationEntryPage.sidePanelTitle
	).toBeVisible();

	await commerceAdminProductConfigurationEntryPage.allowedOrderQuantitiesInput.fill(
		'1,2'
	);
	await commerceAdminProductConfigurationEntryPage.backOrdersInput.click();
	await commerceAdminProductConfigurationEntryPage.commerceAvailabilityEstimateIdInput.selectOption(
		'5-7 days'
	);
	await commerceAdminProductConfigurationEntryPage.CPDefinitionInventoryEngineInput.selectOption(
		'default'
	);
	await commerceAdminProductConfigurationEntryPage.CPTaxCategoryIdInput.selectOption(
		{label: 'Normal Product'}
	);
	await commerceAdminProductConfigurationEntryPage.depthInput.fill('2');
	await commerceAdminProductConfigurationEntryPage.displayAvailabilityInput.click();
	await commerceAdminProductConfigurationEntryPage.displayStockQuantityInput.click();
	await commerceAdminProductConfigurationEntryPage.freeShippingInput.click();
	await commerceAdminProductConfigurationEntryPage.heightInput.fill('3');
	await commerceAdminProductConfigurationEntryPage.lowStockActivityInput.selectOption(
		'default'
	);
	await commerceAdminProductConfigurationEntryPage.maxOrderQuantityInput.fill(
		'400'
	);
	await commerceAdminProductConfigurationEntryPage.minOrderQuantityInput.fill(
		'5'
	);
	await commerceAdminProductConfigurationEntryPage.minStockQuantityInput.fill(
		'6'
	);
	await commerceAdminProductConfigurationEntryPage.multipleOrderQuantityInput.fill(
		'7'
	);
	await commerceAdminProductConfigurationEntryPage.purchasableInput.click();
	await commerceAdminProductConfigurationEntryPage.shippableInput.click();

	await expect(
		commerceAdminProductConfigurationEntryPage.freeShippingInput
	).toBeHidden();
	await expect(
		commerceAdminProductConfigurationEntryPage.shipSeparatelyInput
	).toBeHidden();
	await expect(
		commerceAdminProductConfigurationEntryPage.depthInput
	).toBeHidden();
	await expect(
		commerceAdminProductConfigurationEntryPage.heightInput
	).toBeHidden();
	await expect(
		commerceAdminProductConfigurationEntryPage.weightInput
	).toBeHidden();
	await expect(
		commerceAdminProductConfigurationEntryPage.widthInput
	).toBeHidden();

	await commerceAdminProductConfigurationEntryPage.shippableInput.click();

	await commerceAdminProductConfigurationEntryPage.shipSeparatelyInput.click();
	await commerceAdminProductConfigurationEntryPage.taxExemptInput.click();
	await commerceAdminProductConfigurationEntryPage.visibleInput.click();
	await commerceAdminProductConfigurationEntryPage.weightInput.fill('8');
	await commerceAdminProductConfigurationEntryPage.widthInput.fill('9');

	await commerceAdminProductConfigurationEntryPage.saveButton.click();

	await waitForAlert(page);

	await (
		await commerceAdminProductConfigurationEntriesPage.tableRowLink({
			colIndex: 0,
			rowValue: configurationEntry.id,
		})
	).click();

	await expect(
		commerceAdminProductConfigurationEntryPage.sidePanelTitle
	).toBeVisible();
	await expect(
		commerceAdminProductConfigurationEntryPage.allowedOrderQuantitiesInput
	).toHaveValue('1,2');
	await expect(
		commerceAdminProductConfigurationEntryPage.backOrdersInput
	).toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.commerceAvailabilityEstimateIdInput.locator(
			'option[selected]'
		)
	).toHaveText('5-7 days');
	await expect(
		commerceAdminProductConfigurationEntryPage.CPDefinitionInventoryEngineInput
	).toHaveValue('default');
	await expect(
		commerceAdminProductConfigurationEntryPage.CPTaxCategoryIdInput.locator(
			'option[selected]'
		)
	).toHaveText('Normal Product');
	await expect(
		commerceAdminProductConfigurationEntryPage.depthInput
	).toHaveValue('2.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.displayAvailabilityInput
	).toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.displayStockQuantityInput
	).toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.freeShippingInput
	).not.toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.heightInput
	).toHaveValue('3.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.lowStockActivityInput
	).toHaveValue('default');
	await expect(
		commerceAdminProductConfigurationEntryPage.maxOrderQuantityInput
	).toHaveValue('400.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.minOrderQuantityInput
	).toHaveValue('5.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.minStockQuantityInput
	).toHaveValue('6.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.multipleOrderQuantityInput
	).toHaveValue('7.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.purchasableInput
	).not.toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.shippableInput
	).toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.shipSeparatelyInput
	).not.toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.taxExemptInput
	).toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.visibleInput
	).not.toBeChecked();
	await expect(
		commerceAdminProductConfigurationEntryPage.weightInput
	).toHaveValue('8.0');
	await expect(
		commerceAdminProductConfigurationEntryPage.widthInput
	).toHaveValue('9.0');
});

test('LPD-43013 Configuration Entry form in side panel for virtual products', async ({
	apiHelpers,
	applicationsMenuPage,
	commerceAdminProductConfigurationEntriesPage,
	commerceAdminProductConfigurationEntryPage,
	commerceAdminProductConfigurationListPage,
	commerceAdminProductConfigurationListsPage,
	page,
}) => {
	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		productType: 'virtual',
	});

	const configurationLists =
		await apiHelpers.headlessCommerceAdminCatalog.getProductConfigurationListsPage();

	expect(configurationLists.items?.length).toBeGreaterThan(0);

	const configurationList = configurationLists.items.find((item) =>
		item.name.includes(catalog.name)
	);

	expect(configurationList).not.toBeNull();

	const configurationEntry =
		await apiHelpers.headlessCommerceAdminCatalog.postProductConfiguration(
			configurationList.id,
			{
				entityExternalReferenceCode: product.externalReferenceCode,
				entityId: product.id,
			}
		);

	await applicationsMenuPage.goToCommerceProductConfigurationLists();

	await (
		await commerceAdminProductConfigurationListsPage.tableRowLink({
			colIndex: 0,
			rowValue: configurationList.name,
		})
	).click();

	await commerceAdminProductConfigurationListPage.entriesMenuItem.click();

	await expect(
		commerceAdminProductConfigurationEntriesPage.table
	).toBeVisible();

	await (
		await commerceAdminProductConfigurationEntriesPage.tableRowLink({
			colIndex: 0,
			rowValue: configurationEntry.id,
		})
	).click();

	await expect(
		commerceAdminProductConfigurationEntryPage.sidePanelTitle
	).toBeVisible();

	await commerceAdminProductConfigurationEntryPage.allowedOrderQuantitiesInput.fill(
		'1,2'
	);
	await commerceAdminProductConfigurationEntryPage.backOrdersInput.click();

	await expect(
		commerceAdminProductConfigurationEntryPage.shippableInput
	).toBeDisabled();

	await commerceAdminProductConfigurationEntryPage.saveButton.click();

	await waitForAlert(page);
});

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {CommerceDNDTablePage} from '../commerceDNDTablePage';

export class CommerceAdminProductDetailsProductOptionsPage extends CommerceDNDTablePage {
	readonly addOptionsSearch: Locator;
	readonly createNewOptionsButton: Locator;
	readonly deleteMenuItem: Locator;
	readonly optionActionsButton: (optionName: string) => Locator;
	readonly optionCategoryInput: Locator;
	readonly optionCategoryMenuItem: (categoryName: string) => Locator;
	readonly optionCategoryRemoveButton: (categoryName: string) => Locator;
	readonly optionCollectionProviderSelect: Locator;
	readonly optionDefineExternallyToggle: Locator;
	readonly optionFieldTypeSelect: Locator;
	readonly optionLink: (optionName: string) => Locator;
	readonly optionPriceTypeSelect: Locator;
	readonly optionRow: (optionName: string) => Locator;
	readonly optionSaveButton: Locator;
	readonly optionSidePanelCancelButton: Locator;
	readonly optionSidePanelFrame: FrameLocator;
	readonly optionValueDefaultToggle: Locator;
	readonly optionValueDeltaPriceInput: Locator;
	readonly optionValueLink: (optionValueName: string) => Locator;
	readonly optionValueQuantityInput: Locator;
	readonly optionValueRow: (optionValueName: string) => Locator;
	readonly optionValueRowActionsButton: (optionValueName: string) => Locator;
	readonly optionValueRowMenuItem: (action: string) => Locator;
	readonly optionValueSaveButton: Locator;
	readonly optionValueSearchInput: Locator;
	readonly optionValueSidePanel: Locator;
	readonly optionValueSidePanelCloseButton: Locator;
	readonly optionValueSidePanelFrame: FrameLocator;
	readonly optionValueSkuDropdownItem: (label: string) => Locator;
	readonly optionValueSkuDropdownItems: Locator;
	readonly optionValueSkuInput: Locator;
	readonly page: Page;
	readonly visibleSidePanels: Locator;

	constructor(page: Page) {
		super(
			page,
			'#_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_fm .fds table'
		);

		const sidePanel = '.fds-side-panel.is-visible';
		const sidePanelIframe = `${sidePanel} iframe`;

		this.addOptionsSearch = page.getByPlaceholder(
			'Find or create an option'
		);
		this.createNewOptionsButton = page.getByRole('button', {
			name: 'Create New',
		});
		this.deleteMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Delete',
		});
		this.optionActionsButton = (optionName: string) =>
			page
				.locator('[data-testid="visualization-mode-table"]')
				.getByRole('button', {
					exact: true,
					name: `${optionName} Actions`,
				});
		this.optionLink = (optionName: string) =>
			page.getByRole('link', {exact: true, name: optionName});
		this.optionRow = (optionName: string) =>
			page
				.locator('[data-testid="visualization-mode-table"]')
				.getByRole('row')
				.filter({has: this.optionLink(optionName)});
		this.optionSidePanelFrame = page.frameLocator(sidePanelIframe);
		this.optionCategoryInput =
			this.optionSidePanelFrame.getByLabel('Category');
		this.optionCategoryMenuItem = (categoryName: string) =>
			this.optionSidePanelFrame.getByRole('option', {
				exact: true,
				name: categoryName,
			});
		this.optionCategoryRemoveButton = (categoryName: string) =>
			this.optionSidePanelFrame.getByRole('button', {
				exact: true,
				name: `Remove ${categoryName}`,
			});
		this.optionCollectionProviderSelect =
			this.optionSidePanelFrame.getByLabel('Collection Provider');
		this.optionDefineExternallyToggle =
			this.optionSidePanelFrame.getByLabel('Define Externally');
		this.optionFieldTypeSelect =
			this.optionSidePanelFrame.getByLabel('Field Type');
		this.optionPriceTypeSelect =
			this.optionSidePanelFrame.getByLabel('Price Type');
		this.optionSaveButton = this.optionSidePanelFrame.getByRole('button', {
			exact: true,
			name: 'Save',
		});
		this.optionValueSidePanelFrame =
			this.optionSidePanelFrame.frameLocator(sidePanelIframe);
		this.optionSidePanelCancelButton = this.optionSidePanelFrame.getByRole(
			'button',
			{
				exact: true,
				name: 'Cancel',
			}
		);
		this.optionValueDefaultToggle =
			this.optionValueSidePanelFrame.getByLabel('Default', {exact: true});
		this.optionValueDeltaPriceInput =
			this.optionValueSidePanelFrame.getByLabel('Delta Price');
		this.optionValueLink = (optionValueName: string) =>
			this.optionSidePanelFrame.getByRole('link', {
				exact: true,
				name: optionValueName,
			});
		this.optionValueQuantityInput = this.optionValueSidePanelFrame.locator(
			'input[name$="_quantity"]'
		);
		this.optionValueRow = (optionValueName: string) =>
			this.optionSidePanelFrame
				.getByRole('row')
				.filter({hasText: optionValueName});
		this.optionValueRowActionsButton = (optionValueName: string) =>
			this.optionValueRow(optionValueName).getByRole('button', {
				exact: true,
				name: `${optionValueName} Actions`,
			});
		this.optionValueRowMenuItem = (action: string) =>
			this.optionSidePanelFrame.getByRole('menuitem', {
				exact: true,
				name: action,
			});
		this.optionValueSaveButton = this.optionValueSidePanelFrame.getByRole(
			'button',
			{
				exact: true,
				name: 'Save',
			}
		);
		this.optionValueSearchInput = this.optionSidePanelFrame.getByRole(
			'searchbox',
			{exact: true, name: 'Search'}
		);
		this.optionValueSidePanel =
			this.optionSidePanelFrame.locator('.fds-side-panel');
		this.optionValueSidePanelCloseButton =
			this.optionValueSidePanelFrame.locator('.side-panel-iframe-close');
		this.optionValueSkuDropdownItem = (label: string) =>
			this.optionValueSidePanelFrame
				.locator('.autocomplete-dropdown-menu')
				.getByText(label, {exact: true});
		this.optionValueSkuDropdownItems =
			this.optionValueSidePanelFrame.locator(
				'.autocomplete-dropdown-menu li'
			);
		this.optionValueSkuInput = this.optionValueSidePanelFrame.locator(
			'#autocomplete-root input[type="text"]'
		);
		this.page = page;
		this.visibleSidePanels = page.locator(sidePanel);
	}

	async closeOption() {
		await this.page.keyboard.press('Escape');

		await expect(this.visibleSidePanels).toHaveCount(0);
	}

	async closeOptionValue() {
		if (await this.optionValueSidePanel.isVisible()) {
			await this.optionValueSidePanelCloseButton.click();
		}

		await expect(this.optionValueSidePanel).toBeHidden();
	}

	async editOptionValue(
		optionValueName: string,
		{
			deltaPrice,
			quantity,
			sku,
			unitOfMeasureKey,
		}: {
			deltaPrice?: string;
			quantity: string;
			sku: string;
			unitOfMeasureKey?: string;
		}
	) {
		await this.openOptionValue(optionValueName);

		if (deltaPrice) {
			await this.optionValueDeltaPriceInput.fill(deltaPrice);
		}

		await this.searchSku(sku);

		const dropdownItemLabel = unitOfMeasureKey
			? `${sku} - ${unitOfMeasureKey}`
			: sku;

		await this.optionValueSkuDropdownItem(dropdownItemLabel).click();

		await expect(this.optionValueQuantityInput).toBeEnabled();

		await this.optionValueQuantityInput.fill(quantity);
		await this.optionValueSaveButton.click();

		await expect(this.optionValueRow(optionValueName)).toContainText(
			dropdownItemLabel
		);

		await this.closeOptionValue();
	}

	async openOption(optionName: string) {
		await this.optionLink(optionName).click();

		await expect(this.optionSidePanelCancelButton).toBeVisible();
	}

	async openOptionValue(optionValueName: string) {
		await this.optionValueLink(optionValueName).click();

		await expect(this.optionValueSkuInput).toBeVisible();
	}

	async searchSku(sku: string) {
		await this.optionValueSkuInput.evaluate((element) =>
			element.scrollIntoView({block: 'center'})
		);

		await this.optionValueSkuInput.fill(sku);

		await expect(this.optionValueSkuDropdownItems.first()).toBeVisible();
	}

	async setOptionValueDefault(optionValueName: string) {
		await this.optionValueRowActionsButton(optionValueName).click();
		await this.optionValueRowMenuItem('Toggle Default').click();

		await expect(
			this.optionValueRow(optionValueName).locator('.cell-preselected')
		).toHaveText('Yes');
	}
}

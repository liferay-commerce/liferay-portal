/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {
	CommerceDNDTablePage,
	searchTableRowByValue,
} from '../commerceDNDTablePage';

export class CommerceAdminProductDetailsSkusPage extends CommerceDNDTablePage {
	readonly addSkuUOMButton: Locator;
	readonly closeSidePanelFrame: (isNestedFrame: boolean) => Promise<Locator>;
	readonly incrementalOrderQuantity: Locator;
	readonly inventoryTableRow: (
		colPosition: number,
		value: number | string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly inventoryTableRowAction: (pageName: string) => Promise<Locator>;
	readonly neverExpireCheckbox: Locator;
	readonly page: Page;
	readonly priceTierModal: FrameLocator;
	readonly priceTierModalPriceInput: Locator;
	readonly priceTierModalQuantityInput: Locator;
	readonly priceTierModalSubmitButton: Locator;
	readonly pricinQuantity: Locator;
	readonly sidePanelDetailsSkuExternalReferenceCodeInput: Locator;
	readonly sidePanelDetailsSkuFieldName: Locator;
	readonly sidePanelDetailsSkuPublishButton: Locator;
	readonly sidePanelFrame: FrameLocator;
	readonly sidePanelInventoryTable: Locator;
	readonly sidePanelNestedAddPriceTierButton: Locator;
	readonly sidePanelNestedBulkPricingRadio: Locator;
	readonly sidePanelNestedFrame: FrameLocator;
	readonly sidePanelNestedMenuItem: (menuItemName: string) => Locator;
	readonly sidePanelNestedPriceListPrice: Locator;
	readonly sidePanelNestedPriceTierTableRow: (quantity: string) => Locator;
	readonly sidePanelNestedPriceTierTableRowActionsButton: (
		quantity: string
	) => Locator;
	readonly sidePanelNestedSaveButton: Locator;
	readonly sidePanelNestedTieredPricingRadio: Locator;
	readonly sidePanelSaveAsDraftButton: Locator;
	readonly sidePanelSaveButton: Locator;
	readonly sidePanelSkuInventoryQuantity: Locator;
	readonly sidePanelSkuPriceTableRow: (priceListName: string) => Locator;
	readonly sidePanelSkuPriceTableRowActionsButton: (
		priceListName: string
	) => Locator;
	readonly sidePanelSkuPriceTableRowLink: (priceListName: string) => Locator;
	readonly sidePanelSubscriptionEnableToggle: (
		subscriptionName: string
	) => Locator;
	readonly sidePanelSubscriptionInput: (
		subscriptionName: string,
		inputLabel: string
	) => Locator;
	readonly sidePanelSubscriptionSection: (
		subscriptionName: string
	) => Locator;
	readonly sidePanelSubscriptionSelect: (
		subscriptionName: string,
		selectLabel: string
	) => Locator;
	readonly skuAddButton: Locator;
	readonly skuAddModal: FrameLocator;
	readonly skuAddModalSkuInput: Locator;
	readonly skuAddModalSkuPublishButton: Locator;
	readonly skuAddModalSkuPurchasableToggle: Locator;
	readonly skuAddModalSuccessMessage: Locator;
	readonly skuPriceAddButton: Locator;
	readonly skuPriceAddModal: FrameLocator;
	readonly skuPriceAddModalAddButton: Locator;
	readonly skuPriceAddModalAddEntryButton: Locator;
	readonly skuPriceAddModalCancelButton: Locator;
	readonly skuPriceAddModalUnitOfMeasureSelect: Locator;
	readonly skuPriceAddModalUnitPriceInput: Locator;
	readonly skuPriceFrame: FrameLocator;
	readonly skuPriceListSelect: Locator;
	readonly skuTab: (tabName: string) => Locator;
	readonly skusLink: Locator;
	readonly skusTableRowActionsButton: (skuName: string) => Locator;
	readonly skusTableRowLink: (skuName: string) => Locator;
	readonly skuUOMFrame: FrameLocator;
	readonly skuUOMModal: FrameLocator;
	readonly skuUOMTab: Locator;
	readonly skuUOMTable: Locator;
	readonly skuUOMTableRow: (
		colPosition: number,
		value: number | string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly skuUOMTableRowAction: (skuUOMName: string) => Promise<Locator>;
	readonly skuUOMActionMenuItem: (actionName: string) => Locator;
	readonly skuUOMFrameCancelButton: Locator;
	readonly uomTableRowLink: (uomName: string) => Locator;

	constructor(page: Page) {
		super(
			page,
			'#_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_fm .fds table'
		);
		this.sidePanelFrame = page.frameLocator('.is-visible iframe');
		this.addSkuUOMButton = this.sidePanelFrame
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.closeSidePanelFrame = async (isNestedFrame: boolean) => {
			if (isNestedFrame) {
				return this.sidePanelNestedFrame
					.locator('.side-panel-iframe-header')
					.getByRole('button');
			}

			return this.sidePanelFrame
				.locator('.side-panel-iframe-header')
				.getByRole('button');
		};
		this.inventoryTableRow = async (
			colPosition: number,
			value: number | string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.sidePanelInventoryTable,
				colPosition,
				String(value),
				strictEqual
			);
		};
		this.incrementalOrderQuantity = page
			.frameLocator('iframe')
			.first()
			.frameLocator('iframe')
			.getByLabel('Base Unit Quantity');
		this.inventoryTableRowAction = async (warehouseName: string) => {
			const inventoriesTableRow = await this.inventoryTableRow(
				0,
				warehouseName,
				true
			);

			if (inventoriesTableRow && inventoriesTableRow.column) {
				return inventoriesTableRow.row.getByRole('textbox', {
					name: 'commerce-inventory-warehouse-',
				});
			}

			throw new Error(
				`Cannot locate table row with name ${warehouseName}`
			);
		};
		this.neverExpireCheckbox =
			this.sidePanelFrame.getByLabel('Never Expire');
		this.page = page;
		this.priceTierModal = page.getByRole('dialog').frameLocator('iframe');
		this.priceTierModalPriceInput = this.priceTierModal.getByLabel(
			'Tier Price Required'
		);
		this.priceTierModalQuantityInput =
			this.priceTierModal.getByLabel('Quantity Required');
		this.priceTierModalSubmitButton = this.priceTierModal.getByRole(
			'button',
			{exact: true, name: 'Submit'}
		);
		this.pricinQuantity = page
			.frameLocator('iframe')
			.first()
			.frameLocator('iframe')
			.getByLabel('Pricing Quantity');
		this.sidePanelDetailsSkuExternalReferenceCodeInput =
			this.sidePanelFrame.getByLabel('External Reference Code', {
				exact: true,
			});
		this.sidePanelDetailsSkuFieldName =
			this.sidePanelFrame.getByLabel('SKU Required');
		this.sidePanelDetailsSkuPublishButton = this.sidePanelFrame.getByRole(
			'button',
			{exact: true, name: 'Publish'}
		);
		this.sidePanelInventoryTable = this.sidePanelFrame.locator(
			'#p_p_id_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_ table'
		);
		this.sidePanelNestedFrame =
			this.sidePanelFrame.frameLocator('.is-visible iframe');
		this.sidePanelNestedAddPriceTierButton = this.sidePanelNestedFrame
			.getByRole('button', {exact: true, name: 'Add New Price Tier'})
			.first();
		this.sidePanelNestedBulkPricingRadio =
			this.sidePanelNestedFrame.getByRole('radio', {
				exact: true,
				name: 'Bulk Pricing',
			});
		this.sidePanelNestedMenuItem = (menuItemName: string) =>
			this.sidePanelNestedFrame.getByRole('menuitem', {
				exact: true,
				name: menuItemName,
			});
		this.sidePanelNestedPriceListPrice =
			this.sidePanelNestedFrame.getByLabel('Price List');
		this.sidePanelNestedPriceTierTableRow = (quantity: string) =>
			this.sidePanelNestedFrame.getByRole('row').filter({
				has: this.sidePanelNestedPriceTierTableRowActionsButton(
					quantity
				),
			});
		this.sidePanelNestedPriceTierTableRowActionsButton = (
			quantity: string
		) =>
			this.sidePanelNestedFrame.getByRole('button', {
				exact: true,
				name: `${quantity} Actions`,
			});
		this.sidePanelNestedSaveButton = this.sidePanelNestedFrame.getByRole(
			'button',
			{exact: true, name: 'Save'}
		);
		this.sidePanelNestedTieredPricingRadio =
			this.sidePanelNestedFrame.getByRole('radio', {
				exact: true,
				name: 'Tiered Pricing',
			});
		this.sidePanelSaveAsDraftButton = this.sidePanelFrame.getByRole(
			'button',
			{exact: true, name: 'Save as Draft'}
		);
		this.sidePanelSaveButton = this.sidePanelFrame.getByRole('button', {
			exact: true,
			name: 'Save',
		});
		this.sidePanelSkuInventoryQuantity = this.sidePanelFrame.getByRole(
			'textbox',
			{name: 'commerce-inventory-warehouse-'}
		);
		this.sidePanelSkuPriceTableRow = (priceListName: string) =>
			this.sidePanelFrame.getByRole('row').filter({
				has: this.sidePanelSkuPriceTableRowLink(priceListName),
			});
		this.sidePanelSkuPriceTableRowActionsButton = (priceListName: string) =>
			this.sidePanelFrame.getByRole('button', {
				exact: true,
				name: `${priceListName} Actions`,
			});
		this.sidePanelSkuPriceTableRowLink = (priceListName: string) =>
			this.sidePanelFrame.getByRole('link', {
				exact: true,
				name: priceListName,
			});
		this.sidePanelSubscriptionEnableToggle = (subscriptionName: string) =>
			this.sidePanelSubscriptionSection(subscriptionName).getByText(
				'Enable',
				{exact: true}
			);
		this.sidePanelSubscriptionInput = (
			subscriptionName: string,
			inputLabel: string
		) =>
			this.sidePanelSubscriptionSection(subscriptionName).getByRole(
				'textbox',
				{exact: true, name: inputLabel}
			);
		this.sidePanelSubscriptionSection = (subscriptionName: string) =>
			this.sidePanelFrame
				.locator('.card .card')
				.filter({hasText: subscriptionName});
		this.sidePanelSubscriptionSelect = (
			subscriptionName: string,
			selectLabel: string
		) =>
			this.sidePanelSubscriptionSection(subscriptionName).getByRole(
				'combobox',
				{exact: true, name: selectLabel}
			);
		this.skusLink = page.getByRole('link', {
			exact: true,
			name: 'SKUs',
		});
		this.skuAddButton = page.locator(
			'[data-testid="fdsCreationActionButton"]'
		);
		this.skuAddModal = page.frameLocator('iframe').first();
		this.skuAddModalSkuInput = this.skuAddModal.getByLabel('SKU Required');
		this.skuAddModalSkuPublishButton = this.skuAddModal.getByRole(
			'button',
			{
				name: 'Publish',
			}
		);
		this.skuAddModalSkuPurchasableToggle = this.skuAddModal.getByLabel(
			'Purchasable',
			{exact: true}
		);
		this.skuAddModalSuccessMessage = this.skuAddModal.getByText(
			'Success:Your request completed successfully.'
		);
		this.skuPriceFrame = page.frameLocator('iframe').first();
		this.skuPriceAddButton = this.skuPriceFrame.locator(
			'[data-testid="fdsCreationActionButton"]'
		);
		this.skuPriceAddModal = this.skuPriceFrame.frameLocator(
			'iframe[title="Add Price"]'
		);
		this.skuPriceAddModalAddButton = this.skuPriceFrame.getByRole(
			'button',
			{exact: true, name: 'Add'}
		);
		this.skuPriceAddModalAddEntryButton = this.skuPriceAddModal.getByRole(
			'button',
			{exact: true, name: 'Add Entry'}
		);
		this.skuPriceAddModalCancelButton = this.skuPriceFrame.getByRole(
			'button',
			{exact: true, name: 'Cancel'}
		);
		this.skuPriceAddModalUnitOfMeasureSelect =
			this.skuPriceAddModal.getByLabel('Unit of Measure');
		this.skuPriceAddModalUnitPriceInput =
			this.skuPriceAddModal.getByLabel('Unit Price');
		this.skuPriceListSelect =
			this.skuPriceAddModal.getByLabel('Price List');
		this.skuTab = (skuTab) =>
			this.skuPriceFrame.getByRole('link', {
				exact: true,
				name: skuTab,
			});
		this.skusTableRowActionsButton = (sku: string) =>
			this.table
				.getByRole('row')
				.filter({has: this.skusTableRowLink(sku)})
				.getByRole('button', {name: 'Actions'});
		this.skusTableRowLink = (sku: string) =>
			page.getByRole('link', {exact: true, name: sku});
		this.skuUOMFrame = page.frameLocator('iframe').first();
		this.skuUOMModal = page.frameLocator('iframe >> nth=1');
		this.skuUOMTab = this.sidePanelFrame.getByRole('link', {
			name: 'Units of Measure',
		});
		this.skuUOMTable = this.sidePanelFrame.locator('.fds table');
		this.skuUOMTableRow = async (
			colPosition: number,
			value: number | string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.skuUOMTable,
				colPosition,
				String(value),
				strictEqual
			);
		};
		this.skuUOMTableRowAction = async (skuUOMName: string) => {
			const skuUOMTableRow = await this.skuUOMTableRow(
				1,
				skuUOMName,
				true
			);

			if (skuUOMTableRow && skuUOMTableRow.column) {
				return skuUOMTableRow.row.getByRole('button', {
					name: 'Actions',
				});
			}
			throw new Error(
				`Cannot locate sku UOM row with name ${skuUOMName}`
			);
		};
		this.skuUOMActionMenuItem = (actionName: string) =>
			this.sidePanelFrame.getByRole('menuitem', {
				exact: true,
				name: actionName,
			});

		this.skuUOMFrameCancelButton = this.sidePanelFrame
			.frameLocator('iframe')
			.getByRole('button', {name: 'Cancel'});
		this.uomTableRowLink = (uom: string) =>
			this.sidePanelFrame.getByRole('link', {exact: true, name: uom});
	}

	async addPriceTier(quantity: string, price: string) {
		await this.sidePanelNestedAddPriceTierButton.click();

		await this.priceTierModalQuantityInput.fill(quantity);
		await this.priceTierModalPriceInput.fill(price);

		await this.priceTierModalSubmitButton.click();

		await expect(
			this.sidePanelNestedPriceTierTableRow(quantity)
		).toContainText(`$ ${price}.00`);
	}

	async addUOMEntry({
		baseUnitQuantity = '1',
		decimalAllowed = '0',
		skipModalOpening = false,
		uomKey,
		uomName,
	}) {
		if (!skipModalOpening) {
			await this.addSkuUOMButton.click();
		}

		await expect(
			this.skuUOMModal.getByRole('heading', {name: 'Add Unit of Measure'})
		).toBeVisible();

		await expect(
			this.skuUOMModal.getByLabel('Unit of Measure Required')
		).toBeVisible();

		await this.skuUOMModal
			.getByLabel('Unit of Measure Required')
			.fill(uomName);

		await this.skuUOMModal.getByLabel('Key Required').fill(uomKey);

		await this.skuUOMModal
			.getByLabel('Decimal Allowed (Precision)')
			.fill(decimalAllowed);

		await this.skuUOMModal
			.getByLabel('Base Unit Quantity Required')
			.fill(baseUnitQuantity);

		await this.skuUOMModal.getByRole('button', {name: 'Add'}).click();
	}

	async addWarehouseQuantity(quantity: string, warehouseName: string) {
		await (
			await this.inventoryTableRowAction(warehouseName)
		).fill(quantity);

		await (
			await this.inventoryTableRow(0, warehouseName, true)
		).row
			.getByRole('button', {
				exact: true,
				name: 'Save',
			})
			.click();
	}

	async goToSkuUOM() {
		await this.skuUOMTab.click();
	}

	async goToSkuTab(skuTab: string) {
		await this.skuTab(skuTab).click();
	}

	async openPriceEntry(priceListName: string) {
		await this.sidePanelSkuPriceTableRowLink(priceListName).click();

		await expect(this.sidePanelNestedSaveButton).toBeVisible();
	}

	async reopenPriceEntry(priceListName: string) {
		await (await this.closeSidePanelFrame(true)).click();

		await this.openPriceEntry(priceListName);
	}
}

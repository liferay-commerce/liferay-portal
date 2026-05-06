/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page} from '@playwright/test';

import {GlobalMenuPage} from '../../product-navigation-applications-menu/GlobalMenuPage';
import {CommerceDNDTablePage} from '../commerceDNDTablePage';

export class CommerceAdminInventoryPage extends CommerceDNDTablePage {
	readonly addButton: Locator;
	readonly backLink: Locator;
	readonly changeLogLink: Locator;
	readonly commerceInventoryTableActions: (sku: string) => Promise<Locator>;
	readonly deleteItemMenuItem: Locator;
	readonly globalMenuPage: GlobalMenuPage;
	readonly modalFrameLocator: FrameLocator;
	readonly modalQuantityField: Locator;
	readonly modalSkuField: Locator;
	readonly modalSubmitButton: Locator;
	readonly modalWarehouseSelect: Locator;
	readonly sidePanelFrameLocator: FrameLocator;
	readonly sidePanelSafetyStockField: Locator;
	readonly sidePanelSaveButton: Locator;
	readonly warehouseBreakdownTableCell: (
		warehouseName: string,
		colPosition: number
	) => Promise<Locator>;

	readonly page: Page;

	constructor(page: Page) {
		super(
			page,
			'#p_p_id_com_liferay_commerce_inventory_web_internal_portlet_CommerceInventoryPortlet_ .fds table'
		);
		this.globalMenuPage = new GlobalMenuPage(page);
		this.addButton = page
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.backLink = page.locator('span[title="Back"]');
		this.changeLogLink = page.getByRole('link', {name: 'Changelog'});
		this.commerceInventoryTableActions = async (sku: string) => {
			const itemsTableRow = await this.tableRow(0, sku, true);

			if (itemsTableRow && itemsTableRow.column) {
				return itemsTableRow.row.getByRole('button', {
					exact: true,
					name: `${sku} Actions`,
				});
			}

			throw new Error(`Cannot locate inventory row with value ${sku}`);
		};
		this.deleteItemMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Delete',
		});
		this.modalFrameLocator = page.frameLocator('.fds-modal-body iframe');
		this.modalSkuField = this.modalFrameLocator.getByLabel('SKU Required');
		this.modalWarehouseSelect =
			this.modalFrameLocator.getByLabel('Warehouse Required');
		this.modalQuantityField =
			this.modalFrameLocator.getByLabel('Quantity Required');
		this.modalSubmitButton = this.modalFrameLocator.getByRole('button', {
			exact: true,
			name: 'Submit',
		});
		this.sidePanelFrameLocator = page.frameLocator('.is-visible iframe');
		this.sidePanelSafetyStockField = this.sidePanelFrameLocator.getByLabel(
			'Safety Stock Quantity'
		);
		this.sidePanelSaveButton = this.sidePanelFrameLocator.getByRole(
			'button',
			{exact: true, name: 'Save'}
		);
		this.warehouseBreakdownTableCell = async (
			warehouseName: string,
			colPosition: number
		) => {
			const breakdownRow = await this.tableRow(0, warehouseName, true);

			return breakdownRow.row.locator('td').nth(colPosition);
		};

		this.page = page;
	}

	async goto() {
		await this.globalMenuPage.goToCommerce('Inventory');
	}

	async createInventoryItem({
		quantity,
		sku,
		warehouseName,
	}: {
		quantity: number;
		sku: string;
		warehouseName: string;
	}) {
		await this.addButton.click();
		await this.modalSkuField.fill(sku);
		await this.modalWarehouseSelect.selectOption({label: warehouseName});
		await this.modalQuantityField.fill(String(quantity));
		await this.modalSubmitButton.click();
	}
}

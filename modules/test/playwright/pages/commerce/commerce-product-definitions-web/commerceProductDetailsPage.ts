/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {CommerceLayoutsPage} from '../commerceLayoutsPage';

export class CommerceProductDetailsPage {
	readonly addProductDetailsLabel: Locator;
	readonly addWidgetButton: Locator;
	readonly layoutsPage: CommerceLayoutsPage;
	readonly page: Page;
	readonly pageLabel: Locator;
	readonly searchFormInput: Locator;
	readonly selectProductDetailsPageInput: Locator;
	readonly select: Locator;

	constructor(page: Page) {
		this.addProductDetailsLabel = page
			.getByTestId('addPanelTabItem')
			.filter({hasText: /^Product Details$/})
			.getByRole('button', {exact: true, name: 'Add Content'});
		this.addWidgetButton = page.getByTestId('add');
		this.layoutsPage = new CommerceLayoutsPage(page);
		this.page = page;
		this.pageLabel = page
			.getByTestId('layoutHref')
			.getByLabel('Product Details Page');
		this.searchFormInput = page.getByRole('textbox', {
			name: 'Search Form',
		});
		this.selectProductDetailsPageInput = page
			.getByTestId('selectLayout')
			.getByLabel('Select Product Details Page');
	}

	async addProductDetailsWidget() {
		await this.addWidgetButton.click();
		await this.searchFormInput.click();
		await this.searchFormInput.fill('Product Details');
		await this.addProductDetailsLabel.click();
	}

	async deleteProductDetailsPage() {
		await this.selectProductDetailsPageInput.click();
		await this.layoutsPage.deletePageButton.click();
		await this.layoutsPage.deleteLayoutModal.waitFor({
			state: 'attached',
		});
		await Promise.all([
			this.layoutsPage.deleteLayoutModal.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp
						.url()
						.includes(
							'p_p_id=com_liferay_layout_admin_web_portlet_GroupPagesPortlet'
						)
			),
		]);
	}

	async goto() {
		await this.layoutsPage.goto();
	}

	async goToPage() {
		await this.layoutsPage.goToPages();
		await Promise.all([
			this.pageLabel.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp.url().includes('product-details-page')
			),
		]);
	}

	async goToProductPage(friendlyURL: string) {
		await this.page.goto(`/p/${friendlyURL}`);
		await this.page.waitForLoadState();
	}

	async reloadPage() {
		await this.page.reload();
	}
}

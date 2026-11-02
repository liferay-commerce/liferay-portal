/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {CommerceLayoutsPage} from '../commerce-order-content-web/commerceLayoutsPage';

export class ProductComparisonPage {
	readonly compareBar: Locator;
	readonly compareBarActiveItems: Locator;
	readonly compareBarCompareLink: Locator;
	readonly compareBarItemThumbnail: (imageId: string) => Locator;
	readonly compareBarItems: Locator;
	readonly compareCheckbox: Locator;
	readonly comparisonTableRowCells: (label: string) => Locator;
	readonly layoutsPage: CommerceLayoutsPage;
	readonly page: Page;
	readonly productCardCompareCheckbox: (productName: string) => Locator;

	constructor(page: Page) {
		this.compareBar = page
			.locator('.mini-compare.active')
			.filter({hasText: 'Compare'});
		this.compareBarActiveItems = this.compareBar.locator(
			'.mini-compare-item.active'
		);
		this.compareBarCompareLink = this.compareBar.getByRole('link', {
			exact: true,
			name: 'Compare',
		});
		this.compareBarItemThumbnail = (imageId: string) =>
			this.compareBar.locator(
				`.mini-compare-item.active .mini-compare-thumbnail[style*="images/${imageId}"]`
			);
		this.compareBarItems = this.compareBar.locator('.mini-compare-item');
		this.compareCheckbox = page.getByRole('checkbox', {
			exact: true,
			name: 'Compare',
		});
		this.comparisonTableRowCells = (label: string) =>
			page
				.locator('.products-comparison-table')
				.getByRole('row')
				.filter({
					has: page.getByRole('cell', {exact: true, name: label}),
				})
				.getByRole('cell');
		this.layoutsPage = new CommerceLayoutsPage(page);
		this.page = page;
		this.productCardCompareCheckbox = (productName: string) =>
			page
				.locator('.product-card')
				.filter({has: page.getByTitle(productName, {exact: true})})
				.getByRole('checkbox', {exact: true, name: 'Compare'});
	}

	async addProductComparisonBarWidget() {
		await this.layoutsPage.addWidgetToPage('Product Comparison Bar');
	}
}

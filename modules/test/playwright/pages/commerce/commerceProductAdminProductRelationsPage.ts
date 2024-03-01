/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {CommerceAdminProductPage} from './commerceAdminProductPage';

export class CommerceProductAdminProductRelationsPage {
	readonly addProductRelationHeading: (
		productName: string
	) => Promise<Locator>;
	readonly CommerceAdminProductPage: CommerceAdminProductPage;
	readonly creationMenuNewButton: Locator;
	readonly page: Page;
	readonly productRelationsLink: Locator;
	readonly spareProductMenuButton: Locator;

	constructor(page: Page) {
		this.addProductRelationHeading = async (productName: string) => {
			return page.getByRole('heading', {
				name: 'Add New Product to ' + productName,
			});
		};
		this.CommerceAdminProductPage = new CommerceAdminProductPage(page);
		this.creationMenuNewButton = page
			.getByTestId('creationMenuNewButton')
			.nth(0);
		this.page = page;
		this.productRelationsLink = page.getByRole('link', {
			exact: true,
			name: 'Product Relations',
		});
		this.spareProductMenuButton = page.getByRole('menuitem', {
			exact: true,
			name: 'Add Spare Product',
		});
	}

	async addSpareProductRelation() {
		await this.goto();
		await this.creationMenuNewButton.click();
		await this.spareProductMenuButton.click();
	}

	async goto() {
		await Promise.all([
			this.productRelationsLink.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp
						.url()
						.includes(
							'screenNavigationCategoryKey=product-relations'
						)
			),
		]);
	}
}

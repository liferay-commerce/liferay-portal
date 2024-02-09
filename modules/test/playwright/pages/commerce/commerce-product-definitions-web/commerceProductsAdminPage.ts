/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page} from '@playwright/test';

import {ApplicationsMenuPage} from '../../product-navigation-applications-menu/ApplicationsMenuPage';

export class CommerceProductsAdminPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;
	readonly contentFrameLocator: FrameLocator;
	readonly contentFrameDateTimeInput: Locator;
	readonly optionSearchInput: Locator;
	readonly page: Page;
	readonly pageTitle: Locator;
	readonly searchInput: Locator;
	readonly selectButton: Locator;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.contentFrameLocator = page.frameLocator('iframe');
		this.optionSearchInput = page.getByPlaceholder(
			'Find or create an option'
		);
		this.page = page;
		this.pageTitle = page.getByTestId('headerTitle');
		this.searchInput = page.getByPlaceholder('Search');
		this.selectButton = page.getByRole('button', {name: 'Select'});
		this.contentFrameDateTimeInput =
			this.contentFrameLocator.getByLabel('Date Time');
	}

	async gotoProductDetails(name: string) {
		await this.searchInput.click();
		await this.searchInput.fill(name);
		await this.page.getByRole('button', {name: 'Search'}).click();
		await this.page.getByRole('link', {name}).click();
	}

	async gotoProductOptions() {
		await this.page.getByRole('link', {name: 'Options'}).click();

		await this.page.waitForLoadState();
	}

	async gotoProductOption(optionName: string) {
		await this.page.getByRole('link', {name: optionName}).click();
	}

	async addDateTimeProductOption(optionName: string) {
		await this.optionSearchInput.click();
		await this.optionSearchInput.fill(optionName);
		await this.selectButton.click();
		await this.selectButton.press('Escape');
		await this.gotoProductOption(optionName);
		await this.contentFrameLocator.getByLabel('Date Time').check();
		await this.contentFrameLocator.getByLabel('SKU Contributor').check();

		await Promise.all([
			this.contentFrameLocator
				.getByRole('button', {name: 'Save'})
				.click(),
			this.page.waitForResponse((resp) => resp.status() === 200),
		]);
	}

	async addDateTimeProductOptionValue(key: string) {
		await this.contentFrameLocator.getByLabel('Add Value').click();
		await this.contentFrameLocator.nth(1).getByLabel('Key').click();
		await this.contentFrameLocator.nth(1).getByLabel('Key').fill(key);
		await this.contentFrameLocator
			.nth(1)
			.getByLabel('Duration', {exact: true})
			.click();
		await this.contentFrameLocator
			.nth(1)
			.getByLabel('Duration', {exact: true})
			.fill('2');
		await this.contentFrameLocator
			.nth(1)
			.getByLabel('Duration Type')
			.selectOption('hours');

		await Promise.all([
			this.contentFrameLocator
				.nth(1)
				.getByRole('button', {name: 'Submit'})
				.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp
						.url()
						.includes(
							'p_p_id=com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet'
						)
			),
		]);
	}

	async goto() {
		await this.applicationsMenuPage.goToProducts();

		await this.page.waitForLoadState();
	}

	async reloadPage() {
		await this.page.reload();

		await this.page.waitForLoadState();
	}
}

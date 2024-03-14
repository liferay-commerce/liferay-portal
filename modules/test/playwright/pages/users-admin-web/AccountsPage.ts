/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export class AccountsPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;
	readonly creationMenuNewButton: Locator;
	readonly searchInput: Locator;
	readonly page: Page;
	readonly accountsTableRowLink: (accountName: string) => Locator;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.creationMenuNewButton = page.getByLabel('New', {exact: true});
		this.searchInput = page.getByPlaceholder('Search', {
			exact: true,
		});
		this.accountsTableRowLink = (accountName: string) =>
			page.getByRole('link', {exact: true, name: accountName});
	}

	async goto() {
		await this.applicationsMenuPage.goToAccounts();
	}

	async gotoAccount(accountName: string) {
		await this.goto();
		await this.searchInput.fill(accountName);
		await this.searchInput.press('Enter');
		await this.accountsTableRowLink(accountName).click();
	}
}

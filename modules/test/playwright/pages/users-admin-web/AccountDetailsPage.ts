/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../utils/clickAndExpectToBeVisible';

export class AccountDetailsPage {
	readonly usersTab: Locator;
	readonly addressesTab: Locator;
	readonly accountEntryLink: Locator;

	readonly page: Page;

	constructor(page: Page) {
		this.usersTab = page.getByRole('link', {
			name: 'Users',
		});

		this.addressesTab = page.getByRole('link', {
			name: 'Addresses',
		});
		this.accountEntryLink = page.getByLabel('Show Actions');

		this.page = page;
	}

	async goToAddressesTab() {
		await this.addressesTab.click();
	}

	async goToUsersTab() {
		await this.usersTab.click();
	}

	async editUserEntry() {
		clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('menuitem', {name: 'Assign Roles'}),
			trigger: this.accountEntryLink,
		});
	}
}

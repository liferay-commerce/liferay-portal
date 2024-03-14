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
	readonly assignRolesModal: Locator;
	readonly doneButton: Locator;
	readonly page: Page;
	readonly accountsUserRolesTableRow: (accountName: string) => Locator;

	constructor(page: Page) {
		this.usersTab = page.getByRole('link', {
			name: 'Users',
		});

		this.addressesTab = page.getByRole('link', {
			name: 'Addresses',
		});
		this.accountEntryLink = page.getByLabel('Show Actions');

		this.assignRolesModal = page.locator('.modal-content');

		this.doneButton = page.getByRole('button', {name: 'Done'});

		this.accountsUserRolesTableRow = (roleName: string) =>
			page
				.frameLocator('iframe[title="Assign Roles"]')
				.getByLabel(roleName);

		this.page = page;
	}

	async goToAddressesTab() {
		await this.addressesTab.click();
	}

	async goToUsersTab() {
		await this.usersTab.click();
	}

	async goToAssignRoles(roleName) {
		clickAndExpectToBeVisible({
			autoClick: true,
			target: this.page.getByRole('menuitem', {name: 'Assign Roles'}),
			timeout: 200,
			trigger: this.accountEntryLink,
		});

		await this.assignRolesModal.isVisible();
		await this.accountsUserRolesTableRow(roleName).check();
		await this.doneButton.click();
	}
}

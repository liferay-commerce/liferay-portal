/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {searchTableRowByValue} from './AccountsPage';

export class AccountUsersPage {
	readonly assignUserMenuItem: Locator;
	readonly newButton: Locator;
	readonly page: Page;
	readonly removeButton: Locator;
	readonly searchButton: Locator;
	readonly searchInput: Locator;
	readonly userActions: (name: string) => Promise<Locator>;
	readonly userName: (name: string) => Locator;
	readonly userCheckBox: (name: string) => Promise<Locator>;
	readonly usersTable: Locator;
	readonly usersTableCell: (name: string) => Locator;
	readonly usersTableRow: (
		colPosition: number,
		value: string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;

	constructor(page: Page) {
		this.assignUserMenuItem = page.getByRole('menuitem', {
			name: 'Assign Users',
		});
		this.newButton = page
			.getByTestId('creationMenuNewButton')
			.getByText('New');
		this.page = page;
		this.removeButton = page
			.getByRole('button', {name: 'Remove'})
			.or(page.getByRole('menuitem', {name: 'Remove'}));
		this.searchButton = page.getByLabel('Search for', {exact: true});
		this.searchInput = page.getByPlaceholder('Search for', {exact: true});
		this.userActions = async (name: string) => {
			const usersTableRow = await this.usersTableRow(1, name);

			if (usersTableRow && usersTableRow.row) {
				return usersTableRow.row.getByLabel('Show Actions');
			}
		};
		this.userCheckBox = async (name: string) => {
			const usersTableRow = await this.usersTableRow(1, name);

			if (usersTableRow && usersTableRow.row) {
				return usersTableRow.row.getByRole('checkbox');
			}
		};
		this.userName = (name: string) => {
			return this.page.getByText(name, {exact: true});
		};
		this.usersTable = page.locator(
			'#_com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet_accountUsersSearchContainer'
		);
		this.usersTableCell = (name: string) => {
			return this.page.getByRole('cell', {
				exact: true,
				name: `${name}`,
			});
		};
		this.usersTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.usersTable,
				colPosition,
				value,
				strictEqual
			);
		};
	}

	async roleName(name: string): Promise<Locator> {
		return this.page.getByText(name, {exact: true});
	}
}

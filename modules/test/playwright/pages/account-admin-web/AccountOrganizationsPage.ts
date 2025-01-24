/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {searchTableRowByValue} from './AccountsPage';

export class AccountOrganizationsPage {
	readonly newButton: Locator;
	readonly organizationName: (organizationName: string) => Locator;
	readonly organizationCheckBox: (
		organizationName: string
	) => Promise<Locator>;
	readonly organizationRemoveButton: (
		organizationName: string
	) => Promise<Locator>;
	readonly organizationsTable: Locator;
	readonly organizationsTableCell: (organizationName: string) => Locator;
	readonly organizationsTableRow: (
		colPosition: number,
		value: string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly page: Page;
	readonly removeButton: Locator;
	readonly searchButton: Locator;
	readonly searchInput: Locator;
	readonly selectAllItemsCheckbox: Locator;

	constructor(page: Page) {
		this.newButton = page
			.getByTestId('creationMenuNewButton')
			.getByText('New');
		this.organizationCheckBox = async (organizationName: string) => {
			const organizationsTableRow = await this.organizationsTableRow(
				1,
				organizationName
			);

			if (organizationsTableRow && organizationsTableRow.row) {
				return organizationsTableRow.row.getByRole('checkbox');
			}
		};
		this.organizationRemoveButton = async (organizationName: string) => {
			const organizationsTableRow = await this.organizationsTableRow(
				1,
				organizationName
			);

			if (organizationsTableRow && organizationsTableRow.row) {
				return organizationsTableRow.row.getByRole('link', {
					name: 'Remove',
				});
			}
		};
		this.organizationName = (organizationName: string) => {
			return this.page.getByText(organizationName, {exact: true});
		};
		this.organizationsTable = page.locator(
			'#_com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet_accountOrganizationsSearchContainer'
		);
		this.organizationsTableCell = (organizationName: string) => {
			return this.page.getByRole('cell', {
				exact: true,
				name: `${organizationName}`,
			});
		};
		this.organizationsTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.organizationsTable,
				colPosition,
				value,
				strictEqual
			);
		};
		this.page = page;
		this.removeButton = page.getByRole('button', {name: 'Remove'});
		this.searchButton = page.getByLabel('Search for', {exact: true});
		this.searchInput = page.getByPlaceholder('Search for', {exact: true});
		this.selectAllItemsCheckbox = page.getByLabel(
			'Select All Items on the Page'
		);
	}
}

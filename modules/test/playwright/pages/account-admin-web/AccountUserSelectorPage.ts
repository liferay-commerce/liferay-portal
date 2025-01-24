/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../utils/waitForAlert';
import {searchTableRowByValue} from './AccountsPage';

export class AccountUserSelectorPage {
	readonly assignButton: Locator;
	readonly userCheckBox: (name: string) => Promise<Locator>;
	readonly userFrame: FrameLocator;
	readonly userTable: Locator;
	readonly userTableCell: (name: string) => Locator;
	readonly userTableRow: (
		colPosition: number,
		value: string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly page: Page;
	readonly searchButton: Locator;
	readonly searchInput: Locator;

	constructor(page: Page) {
		this.assignButton = page.getByRole('button', {
			exact: true,
			name: 'Assign',
		});
		this.userCheckBox = async (name: string) => {
			const userTableRow = await this.userTableRow(1, name);

			if (userTableRow && userTableRow.row) {
				return userTableRow.row.getByRole('checkbox');
			}
		};
		this.userFrame = page.frameLocator('iframe[id="modalIframe"]');
		this.userTable = this.userFrame.locator(
			'#_com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet_selectAccountUser'
		);
		this.userTableCell = (organizationName: string) => {
			return this.userFrame
				.getByRole('cell', {
					exact: true,
					name: `${organizationName}`,
				})
				.first();
		};
		this.userTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.userTable,
				colPosition,
				value,
				strictEqual
			);
		};
		this.page = page;
		this.searchButton = this.userFrame.getByLabel('Search for', {
			exact: true,
		});
		this.searchInput = this.userFrame.getByPlaceholder('Search for', {
			exact: true,
		});
	}

	async assignUsers(names: string[]) {
		await expect(this.searchInput).toBeEditable();

		for (const name of names) {
			await (await this.userCheckBox(name)).check();
		}
		await this.assignButton.click();

		await waitForAlert(this.page);
	}
}

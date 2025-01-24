/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page} from '@playwright/test';

import {waitForAlert} from '../../utils/waitForAlert';
import {searchTableRowByValue} from './AccountsPage';

export class AccountOrganizationSelectorPage {
	readonly assignButton: Locator;
	readonly organizationCheckBox: (
		organizationName: string
	) => Promise<Locator>;
	readonly organizationFrame: FrameLocator;
	readonly organizationTable: Locator;
	readonly organizationsTableCell: (organizationName: string) => Locator;
	readonly organizationTableRow: (
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
		this.organizationCheckBox = async (organizationName: string) => {
			const organizationTableRow = await this.organizationTableRow(
				1,
				organizationName
			);

			if (organizationTableRow && organizationTableRow.row) {
				return organizationTableRow.row.getByRole('checkbox');
			}
		};
		this.organizationFrame = page.frameLocator('iframe[id="modalIframe"]');
		this.organizationTable = this.organizationFrame.locator(
			'#_com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet_organizationsSearchContainer'
		);
		this.organizationsTableCell = (organizationName: string) => {
			return this.organizationFrame
				.getByRole('cell', {
					exact: true,
					name: `${organizationName}`,
				})
				.first();
		};
		this.organizationTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.organizationTable,
				colPosition,
				value,
				strictEqual
			);
		};
		this.page = page;
		this.searchButton = this.organizationFrame.getByLabel('Search for', {
			exact: true,
		});
		this.searchInput = this.organizationFrame.getByPlaceholder(
			'Search for',
			{
				exact: true,
			}
		);
	}

	async assignOrganizations(organizationNames: string[]) {
		for (const organizationName of organizationNames) {
			await (await this.organizationCheckBox(organizationName)).check();
		}
		await this.assignButton.click();

		await waitForAlert(this.page);
	}
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {clickAndExpectToBeHidden} from '../../../utils/clickAndExpectToBeHidden';

const PORTLET_NAMESPACE =
	'_com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet_';

export class CommerceAccountAddressEligibilityPage {
	readonly allChannelsRadio: Locator;
	readonly channelRemoveButton: (channelName: string) => Locator;
	readonly channelSelectButton: (channelName: string) => Locator;
	readonly findChannelInput: Locator;
	readonly page: Page;
	readonly specificChannelsRadio: Locator;

	constructor(page: Page) {
		this.allChannelsRadio = page.getByRole('radio', {
			exact: true,
			name: 'All Channels',
		});
		this.channelRemoveButton = (channelName: string) =>
			page
				.getByRole('row')
				.filter({hasText: channelName})
				.getByRole('button', {exact: true, name: 'Remove'});
		this.channelSelectButton = (channelName: string) =>
			page
				.getByRole('row')
				.filter({hasText: channelName})
				.getByRole('button', {exact: true, name: 'Select'});
		this.findChannelInput = page.getByRole('textbox', {
			exact: true,
			name: 'Find a Channel',
		});
		this.page = page;
		this.specificChannelsRadio = page.getByRole('radio', {
			exact: true,
			name: 'Specific Channels',
		});
	}

	async addChannel(channelName: string) {
		await this.specificChannelsRadio.check();

		await this.findChannelInput.fill(channelName);

		await this.channelSelectButton(channelName).click();

		await expect(this.channelRemoveButton(channelName)).toBeVisible();
	}

	async goto(accountId: number, addressId: number) {
		await this.page.goto(
			`/group/control_panel/manage?p_p_id=com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet&${PORTLET_NAMESPACE}accountEntryAddressId=${addressId}&${PORTLET_NAMESPACE}accountEntryId=${accountId}&${PORTLET_NAMESPACE}mvcRenderCommandName=%2Faccount_admin%2Fedit_account_entry_address&${PORTLET_NAMESPACE}screenNavigationCategoryKey=qualifiers`
		);
	}

	async removeChannel(channelName: string) {
		await clickAndExpectToBeHidden({
			target: this.channelRemoveButton(channelName),
			trigger: this.channelRemoveButton(channelName),
		});
	}
}

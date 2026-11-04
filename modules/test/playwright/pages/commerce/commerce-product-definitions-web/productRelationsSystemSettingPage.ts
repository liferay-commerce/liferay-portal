/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';
import {SystemSettingsPage} from '../../configuration-admin-web/SystemSettingsPage';

export class ProductRelationsSystemSettingPage {
	readonly actionsButton: (type: string) => Locator;
	readonly deleteMenuItem: Locator;
	readonly page: Page;
	readonly systemSettingsPage: SystemSettingsPage;
	readonly typeInput: Locator;

	constructor(page: Page) {
		this.actionsButton = (type: string) =>
			page
				.getByRole('row')
				.filter({
					has: page.getByRole('link', {exact: true, name: type}),
				})
				.getByRole('button', {name: 'Actions'});
		this.deleteMenuItem = page
			.getByRole('menu')
			.getByRole('link', {exact: true, name: 'Delete'});
		this.page = page;
		this.systemSettingsPage = new SystemSettingsPage(page);
		this.typeInput = page.getByRole('textbox', {exact: true, name: 'Type'});
	}

	async addType(type: string) {
		await this.systemSettingsPage.goToConfiguration(
			'com.liferay.commerce.product.configuration.CPDefinitionLinkTypeConfiguration'
		);

		await this.typeInput.fill(type);

		await this.systemSettingsPage.saveAndWaitForAlert();
	}

	async deleteType(type: string) {
		await this.systemSettingsPage.goToSystemSetting(
			'Catalog',
			'Product Relations'
		);

		this.page.once('dialog', (dialog) => dialog.accept());

		await expect(async () => {
			await this.actionsButton(type).click();

			await expect(this.deleteMenuItem).toBeVisible({timeout: 500});
		}).toPass({timeout: 5000});

		await this.deleteMenuItem.click();

		await waitForAlert(this.page);
	}
}

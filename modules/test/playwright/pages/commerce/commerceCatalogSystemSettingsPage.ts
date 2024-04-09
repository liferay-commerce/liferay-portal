/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {SystemSettingsPage} from '../configuration-admin-web/SystemSettingsPage';

export class CommerceCatalogSystemSettingsPage {
	readonly page: Page;
	readonly editConfigurationSubmitButton: Locator;
	readonly enabledButton: Locator;
	readonly systemSettingsPage: SystemSettingsPage;

	constructor(page: Page) {
		this.enabledButton = page.getByLabel('enabled');
		this.systemSettingsPage = new SystemSettingsPage(page);
		this.editConfigurationSubmitButton = page.getByTestId(
			'submitConfiguration'
		);
	}

	async toggleProductVersioning() {
		await this.systemSettingsPage.goToSystemSetting(
			'Catalog',
			'Product Versioning'
		);
		await this.enabledButton.click();
		await Promise.all([
			this.editConfigurationSubmitButton.click(),
			this.systemSettingsPage.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp
						.url()
						.includes(
							'com.liferay.commerce.product.configuration.CProductVersionConfiguration'
						)
			),
		]);
	}
}

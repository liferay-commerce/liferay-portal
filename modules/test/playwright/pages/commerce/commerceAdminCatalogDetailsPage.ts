/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {Page} from '@playwright/test';

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export class CommerceAdminCatalogDetailsPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;

	// readonly selectFileButton: Locator;

	readonly page: Page;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);

		// this.selectFileButton = page.getByLabel('Select File', {exact: true});

		this.page = page;
	}

	async goto() {
		await this.applicationsMenuPage.goToCommerceCatalogs();
	}

	// async goToProductDiagramTab() {
	// 	await this.productDiagramTab.click();
	// }
	// }
}

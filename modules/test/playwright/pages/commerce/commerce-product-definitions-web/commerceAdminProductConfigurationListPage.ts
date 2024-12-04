/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {CommerceDNDTablePage} from '../commerceDNDTablePage';

export class CommerceAdminProductConfigurationListPage {
	readonly detailsMenuItem: Locator;
	readonly entriesMenuItem: Locator;
	readonly page: Page;

	constructor(page: Page) {
		this.detailsMenuItem = page.getByRole('link', {
			name: 'Details',
		});
		this.entriesMenuItem = page.getByRole('link', {name: 'Entries'});
		this.page = page;
	}
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {test} from '@playwright/test';

import {AccountDetailsPage} from '../pages/users-admin-web/AccountDetailsPage';
import {AccountsPage} from '../pages/users-admin-web/AccountsPage';

const accountsPagesTest = test.extend<{
	accountDetailsPage: AccountDetailsPage;
	accountsPage: AccountsPage;
}>({
	accountDetailsPage: async ({page}, use) => {
		await use(new AccountDetailsPage(page));
	},
	accountsPage: async ({page}, use) => {
		await use(new AccountsPage(page));
	},
});

export {accountsPagesTest};

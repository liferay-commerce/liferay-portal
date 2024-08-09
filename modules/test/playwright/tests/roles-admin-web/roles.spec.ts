/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {rolesPagesTest} from '../../fixtures/rolesPagesTest';

export const test = mergeTests(loginTest(), rolesPagesTest);

test('LPD-32861 Portlet permissions searchable with no name', async ({
	roleDefinePermissionsPage,
	rolePage,
	rolesPage,
}) => {
	await rolesPage.goto();

	await rolesPage.userLink.click();
	await rolePage.definePermissionsLink.click();
	await roleDefinePermissionsPage.searchInput.click();
	await roleDefinePermissionsPage.searchInput.fill('jsp');

	await expect(
		roleDefinePermissionsPage.menuItem('Applications')
	).toBeVisible();
	await expect(roleDefinePermissionsPage.menuItem('jsp')).toBeVisible();
});

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.util;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalServiceUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

/**
 * @author Fabio Monaco
 */
public class CommerceRoleUtil {

	public static long[] getRoleIds(
		long accountEntryId, PermissionChecker permissionChecker) {

		if (permissionChecker == null) {
			return new long[0];
		}

		AccountEntry accountEntry =
			AccountEntryLocalServiceUtil.fetchAccountEntry(accountEntryId);

		long groupId = 0;

		if (accountEntry != null) {
			groupId = accountEntry.getAccountEntryGroupId();
		}

		return permissionChecker.getRoleIds(
			permissionChecker.getUserId(), groupId);
	}

}
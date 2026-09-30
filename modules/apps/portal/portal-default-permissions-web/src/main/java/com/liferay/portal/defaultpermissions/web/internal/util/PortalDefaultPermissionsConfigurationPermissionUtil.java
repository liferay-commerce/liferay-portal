/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.web.internal.util;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.permission.GroupPermissionUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.Objects;

/**
 * @author Balazs Breier
 */
public class PortalDefaultPermissionsConfigurationPermissionUtil {

	public static void check(String scope, ThemeDisplay themeDisplay)
		throws PortalException {

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (Objects.equals(
				scope, ExtendedObjectClassDefinition.Scope.GROUP.toString())) {

			GroupPermissionUtil.check(
				permissionChecker, themeDisplay.getSiteGroup(),
				ActionKeys.PERMISSIONS);

			return;
		}

		if (!permissionChecker.isCompanyAdmin(themeDisplay.getCompanyId())) {
			throw new PrincipalException.MustBeCompanyAdmin(
				permissionChecker.getUserId());
		}
	}

}
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.permission;

import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Marco Leo
 */
@ProviderType
public interface CommerceProductViewPermission {

	public void check(
			PermissionChecker permissionChecker, long commerceAccountId,
			long cpDefinitionId)
		throws PortalException;

	public void check(
			PermissionChecker permissionChecker, long commerceAccountId,
			long commerceChannelGroupId, long cpDefinitionId)
		throws PortalException;

	public boolean contains(
			PermissionChecker permissionChecker, long commerceAccountId,
			long cpDefinitionId)
		throws PortalException;

	public boolean contains(
			PermissionChecker permissionChecker, long commerceAccountId,
			long commerceChannelGroupId, long cpDefinitionId)
		throws PortalException;

	public boolean isRoleEnabled(
			PermissionChecker permissionChecker, long commerceAccountId,
			CPDefinition cpDefinition)
		throws PortalException;

}
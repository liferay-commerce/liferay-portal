/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import java.util.Map;

/**
 * @author Stefano Motta
 */
public interface PortalDefaultPermissionsConfiguration {

	public Map<String, Map<String, String[]>> getDefaultPermissions(
		long companyId, long groupId);

	public Map<String, String[]> getDefaultPermissions(
		long companyId, long groupId, String className);

	public ExtendedObjectClassDefinition.Scope getScope();

	public void setDefaultPermissions(
		long primaryKey, Map<String, Map<String, String[]>> defaultPermissions);

}
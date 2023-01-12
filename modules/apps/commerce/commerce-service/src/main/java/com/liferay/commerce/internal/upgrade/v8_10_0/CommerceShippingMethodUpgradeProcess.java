/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.internal.upgrade.v8_10_0;

import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.service.permission.ModelPermissionsFactory;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Arrays;

/**
 * @author Crescenzo Rega
 */
public class CommerceShippingMethodUpgradeProcess extends UpgradeProcess {

	public CommerceShippingMethodUpgradeProcess(
		ResourceActionLocalService resourceActionLocalService,
		ResourceLocalService resourceLocalService) {

		_resourceActionLocalService = resourceActionLocalService;
		_resourceLocalService = resourceLocalService;
	}

	@Override
	public void doUpgrade() throws Exception {
		_resourceActionLocalService.checkResourceActions(
			CommerceShippingMethod.class.getName(),
			Arrays.asList(_OWNER_PERMISSIONS), true);

		String selectCommerceShippingMethodSQL =
			"select commerceShippingMethodId, groupId, companyId, userId " +
				"from CommerceShippingMethod";

		try (Statement s = connection.createStatement();
			ResultSet resultSet = s.executeQuery(
				selectCommerceShippingMethodSQL)) {

			while (resultSet.next()) {
				ModelPermissions modelPermissions =
					ModelPermissionsFactory.create(
						new String[0], new String[0]);

				modelPermissions.addRolePermissions(
					RoleConstants.OWNER, _OWNER_PERMISSIONS);
				modelPermissions.addRolePermissions(
					RoleConstants.GUEST, "VIEW");
				modelPermissions.addRolePermissions(RoleConstants.USER, "VIEW");

				long commerceShippingMethodId = resultSet.getLong(
					"commerceShippingMethodId");
				long groupId = resultSet.getLong("groupId");
				long companyId = resultSet.getLong("companyId");
				long userId = resultSet.getLong("userId");

				_resourceLocalService.addModelResources(
					companyId, groupId, userId,
					CommerceShippingMethod.class.getName(),
					commerceShippingMethodId, modelPermissions);
			}
		}
	}

	private static final String[] _OWNER_PERMISSIONS = {
		"DELETE", "PERMISSIONS", "UPDATE", "VIEW"
	};

	private final ResourceActionLocalService _resourceActionLocalService;
	private final ResourceLocalService _resourceLocalService;

}
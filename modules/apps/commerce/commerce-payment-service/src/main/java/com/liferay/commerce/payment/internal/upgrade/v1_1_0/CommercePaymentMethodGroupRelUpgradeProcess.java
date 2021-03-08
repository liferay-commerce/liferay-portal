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

package com.liferay.commerce.payment.internal.upgrade.v1_1_0;

import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.service.permission.ModelPermissionsFactory;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Arrays;

/**
 * @author Riccardo Alberti
 */
public class CommercePaymentMethodGroupRelUpgradeProcess
	extends UpgradeProcess {

	public CommercePaymentMethodGroupRelUpgradeProcess(
		ResourceActionLocalService resourceActionLocalService,
		ResourceLocalService resourceLocalService) {

		_resourceActionLocalService = resourceActionLocalService;
		_resourceLocalService = resourceLocalService;
	}

	@Override
	public void doUpgrade() throws Exception {
		_resourceActionLocalService.checkResourceActions(
			CommercePaymentMethodGroupRel.class.getName(),
			Arrays.asList(_OWNER_PERMISSIONS), true);

		String selectCommercePaymentMethodGroupRelSQL =
			"select companyId, groupId, userId, CPaymentMethodGroupRelId " +
				"from CommercePaymentMethodGroupRel";

		try (Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(
				selectCommercePaymentMethodGroupRelSQL)) {

			while (rs.next()) {
				long companyId = rs.getLong("companyId");

				long groupId = rs.getLong("groupId");

				long userId = rs.getLong("userId");

				long commercePaymentMethodGroupRelId = rs.getLong(
					"CPaymentMethodGroupRelId");

				ModelPermissions modelPermissions =
					ModelPermissionsFactory.create(
						new String[0], new String[0]);

				modelPermissions.addRolePermissions(
					RoleConstants.OWNER, _OWNER_PERMISSIONS);

				modelPermissions.addRolePermissions(
					RoleConstants.GUEST, "VIEW");

				modelPermissions.addRolePermissions(RoleConstants.USER, "VIEW");

				_resourceLocalService.addModelResources(
					companyId, groupId, userId,
					CommercePaymentMethodGroupRel.class.getName(),
					commercePaymentMethodGroupRelId, modelPermissions);
			}
		}
	}

	private static final String[] _OWNER_PERMISSIONS = {
		"DELETE", "PERMISSIONS", "UPDATE", "VIEW"
	};

	private final ResourceActionLocalService _resourceActionLocalService;
	private final ResourceLocalService _resourceLocalService;

}
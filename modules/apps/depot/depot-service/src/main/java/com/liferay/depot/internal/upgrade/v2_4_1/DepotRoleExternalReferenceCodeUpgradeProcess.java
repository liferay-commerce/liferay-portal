/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.internal.upgrade.v2_4_1;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.util.Objects;

/**
 * @author Stefano Motta
 */
public class DepotRoleExternalReferenceCodeUpgradeProcess
	extends UpgradeProcess {

	public DepotRoleExternalReferenceCodeUpgradeProcess(
		CompanyLocalService companyLocalService,
		RoleLocalService roleLocalService) {

		_companyLocalService = companyLocalService;
		_roleLocalService = roleLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(
			companyId -> {
				for (String name : _ROLE_NAMES) {
					_upgradeRole(companyId, name);
				}
			});
	}

	private void _upgradeRole(long companyId, String name) {
		Role role = _roleLocalService.fetchRole(companyId, name);

		if (role == null) {
			return;
		}

		String existingExternalReferenceCode = role.getExternalReferenceCode();
		String externalReferenceCode =
			RoleConstants.toSystemRoleExternalReferenceCode(name);

		if (Objects.equals(
				existingExternalReferenceCode, externalReferenceCode)) {

			return;
		}

		if (!Objects.equals(existingExternalReferenceCode, role.getUuid())) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to assign the external reference code \"",
						externalReferenceCode, "\" to the \"", name,
						"\" role in company ", companyId,
						" because it was customized"));
			}

			return;
		}

		Role existingRole = _roleLocalService.fetchRoleByExternalReferenceCode(
			externalReferenceCode, companyId);

		if (existingRole != null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to assign the external reference code \"",
						externalReferenceCode, "\" to the \"", name,
						"\" role in company ", companyId,
						" because another role already uses it"));
			}

			return;
		}

		role.setExternalReferenceCode(externalReferenceCode);

		_roleLocalService.updateRole(role);
	}

	private static final String[] _ROLE_NAMES = ArrayUtil.append(
		DepotRolesConstants.DEPOT_ROLE_NAMES,
		DepotRolesConstants.DESIGN_LIBRARY_ROLE_NAMES,
		DepotRolesConstants.PROJECT_ROLE_NAMES);

	private static final Log _log = LogFactoryUtil.getLog(
		DepotRoleExternalReferenceCodeUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final RoleLocalService _roleLocalService;

}
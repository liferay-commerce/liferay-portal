/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.internal.upgrade.v2_4_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@RunWith(Arquillian.class)
public class DepotRoleExternalReferenceCodeUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() throws Exception {
		_roleLocalService.deleteRole(_role);

		_updateRole(
			RoleConstants.toSystemRoleExternalReferenceCode(
				DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER),
			DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER);
		_updateRole(
			RoleConstants.toSystemRoleExternalReferenceCode(
				DepotRolesConstants.ASSET_LIBRARY_OWNER),
			DepotRolesConstants.ASSET_LIBRARY_OWNER);
	}

	@Test
	public void testUpgrade() throws Exception {
		Role assetLibraryContentReviewerRole = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(),
			DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER);

		assetLibraryContentReviewerRole = _updateRole(
			assetLibraryContentReviewerRole.getUuid(),
			DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER);

		Role assetLibraryMemberRole = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(),
			DepotRolesConstants.ASSET_LIBRARY_MEMBER);

		assetLibraryMemberRole = _updateRole(
			assetLibraryMemberRole.getUuid(),
			DepotRolesConstants.ASSET_LIBRARY_MEMBER);

		String externalReferenceCode = RandomTestUtil.randomString();

		Role assetLibraryOwnerRole = _updateRole(
			externalReferenceCode, DepotRolesConstants.ASSET_LIBRARY_OWNER);

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_role = _updateRole(
			RoleConstants.toSystemRoleExternalReferenceCode(
				DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER),
			_role.getName());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME, LoggerTestUtil.WARN)) {

			_upgrade();

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 2, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				StringBundler.concat(
					"Unable to assign the external reference code \"",
					RoleConstants.toSystemRoleExternalReferenceCode(
						DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER),
					"\" to the \"",
					DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER,
					"\" role in company ", TestPropsValues.getCompanyId(),
					" because another role already uses it"),
				logEntry.getMessage());

			logEntry = logEntries.get(1);

			Assert.assertEquals(
				StringBundler.concat(
					"Unable to assign the external reference code \"",
					RoleConstants.toSystemRoleExternalReferenceCode(
						DepotRolesConstants.ASSET_LIBRARY_OWNER),
					"\" to the \"", DepotRolesConstants.ASSET_LIBRARY_OWNER,
					"\" role in company ", TestPropsValues.getCompanyId(),
					" because it was customized"),
				logEntry.getMessage());

			Role assetLibraryAdministratorRole = _roleLocalService.getRole(
				TestPropsValues.getCompanyId(),
				DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);

			Assert.assertEquals(
				RoleConstants.toSystemRoleExternalReferenceCode(
					DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR),
				assetLibraryAdministratorRole.getExternalReferenceCode());

			assetLibraryContentReviewerRole = _roleLocalService.getRole(
				assetLibraryContentReviewerRole.getRoleId());

			Assert.assertEquals(
				assetLibraryContentReviewerRole.getUuid(),
				assetLibraryContentReviewerRole.getExternalReferenceCode());

			assetLibraryMemberRole = _roleLocalService.getRole(
				assetLibraryMemberRole.getRoleId());

			Assert.assertEquals(
				RoleConstants.toSystemRoleExternalReferenceCode(
					DepotRolesConstants.ASSET_LIBRARY_MEMBER),
				assetLibraryMemberRole.getExternalReferenceCode());

			assetLibraryOwnerRole = _roleLocalService.getRole(
				assetLibraryOwnerRole.getRoleId());

			Assert.assertEquals(
				externalReferenceCode,
				assetLibraryOwnerRole.getExternalReferenceCode());
		}
	}

	private Role _updateRole(String externalReferenceCode, String name)
		throws Exception {

		Role role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), name);

		role.setExternalReferenceCode(externalReferenceCode);

		return _roleLocalService.updateRole(role);
	}

	private void _upgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private static final String _CLASS_NAME =
		"com.liferay.depot.internal.upgrade.v2_4_1." +
			"DepotRoleExternalReferenceCodeUpgradeProcess";

	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.depot.internal.upgrade.registry.DepotServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.internal.service;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.RoleSubtypeException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserGroupRoleService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lianne Louie
 */
public class DepotUserGroupRoleServiceWrapperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAddUserGroupRoles() throws Exception {
		DepotUserGroupRoleServiceWrapper depotUserGroupRoleServiceWrapper =
			new DepotUserGroupRoleServiceWrapper();

		UserGroupRoleService userGroupRoleService = Mockito.mock(
			UserGroupRoleService.class);

		depotUserGroupRoleServiceWrapper.setWrappedService(
			userGroupRoleService);

		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockDepotEntry(depotEntryLocalServiceUtilMockedStatic);

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, false);

			long designLibraryRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic,
				DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY);

			long[] designLibraryRoleIds = {designLibraryRoleId};

			depotUserGroupRoleServiceWrapper.addUserGroupRoles(
				_USER_ID, _GROUP_ID, designLibraryRoleIds);

			Mockito.verify(
				userGroupRoleService
			).addUserGroupRoles(
				_USER_ID, _GROUP_ID, designLibraryRoleIds
			);

			long[] userIds = {_USER_ID};

			depotUserGroupRoleServiceWrapper.addUserGroupRoles(
				userIds, _GROUP_ID, designLibraryRoleId);

			Mockito.verify(
				userGroupRoleService
			).addUserGroupRoles(
				userIds, _GROUP_ID, designLibraryRoleId
			);

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);

			long projectRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic,
				DepotRolesConstants.SUBTYPE_PROJECT);

			long[] projectRoleIds = {projectRoleId};

			depotUserGroupRoleServiceWrapper.addUserGroupRoles(
				_USER_ID, _GROUP_ID, projectRoleIds);

			Mockito.verify(
				userGroupRoleService
			).addUserGroupRoles(
				_USER_ID, _GROUP_ID, projectRoleIds
			);

			depotUserGroupRoleServiceWrapper.addUserGroupRoles(
				userIds, _GROUP_ID, projectRoleId);

			Mockito.verify(
				userGroupRoleService
			).addUserGroupRoles(
				userIds, _GROUP_ID, projectRoleId
			);

			Mockito.clearInvocations(userGroupRoleService);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> depotUserGroupRoleServiceWrapper.addUserGroupRoles(
					_USER_ID, _GROUP_ID, designLibraryRoleIds));
			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> depotUserGroupRoleServiceWrapper.addUserGroupRoles(
					userIds, _GROUP_ID, designLibraryRoleId));

			Mockito.verifyNoInteractions(userGroupRoleService);
		}
	}

	@Test
	public void testUpdateUserGroupRoles() throws Exception {
		DepotUserGroupRoleServiceWrapper depotUserGroupRoleServiceWrapper =
			new DepotUserGroupRoleServiceWrapper();

		UserGroupRoleService userGroupRoleService = Mockito.mock(
			UserGroupRoleService.class);

		depotUserGroupRoleServiceWrapper.setWrappedService(
			userGroupRoleService);

		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockDepotEntry(depotEntryLocalServiceUtilMockedStatic);

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, false);

			long[] designLibraryRoleIds = {
				_mockRole(
					roleLocalServiceUtilMockedStatic,
					DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY)
			};
			long[] projectRoleIds = {
				_mockRole(
					roleLocalServiceUtilMockedStatic,
					DepotRolesConstants.SUBTYPE_PROJECT)
			};

			depotUserGroupRoleServiceWrapper.updateUserGroupRoles(
				_USER_ID, _GROUP_ID, designLibraryRoleIds, projectRoleIds);

			Mockito.verify(
				userGroupRoleService
			).updateUserGroupRoles(
				_USER_ID, _GROUP_ID, designLibraryRoleIds, projectRoleIds
			);

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);

			depotUserGroupRoleServiceWrapper.updateUserGroupRoles(
				_USER_ID, _GROUP_ID, projectRoleIds, designLibraryRoleIds);

			Mockito.verify(
				userGroupRoleService
			).updateUserGroupRoles(
				_USER_ID, _GROUP_ID, projectRoleIds, designLibraryRoleIds
			);

			Mockito.clearInvocations(userGroupRoleService);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> depotUserGroupRoleServiceWrapper.updateUserGroupRoles(
					_USER_ID, _GROUP_ID, designLibraryRoleIds, projectRoleIds));

			Mockito.verifyNoInteractions(userGroupRoleService);
		}
	}

	private void _mockDepotEntry(
		MockedStatic<DepotEntryLocalServiceUtil> mockedStatic) {

		DepotEntry depotEntry = Mockito.mock(DepotEntry.class);

		Mockito.when(
			depotEntry.getType()
		).thenReturn(
			DepotConstants.TYPE_PROJECT
		);

		mockedStatic.when(
			() -> DepotEntryLocalServiceUtil.fetchGroupDepotEntry(_GROUP_ID)
		).thenReturn(
			depotEntry
		);
	}

	private void _mockFeatureFlag(
		MockedStatic<FeatureFlagManagerUtil> mockedStatic, boolean enabled) {

		mockedStatic.when(
			() -> FeatureFlagManagerUtil.isEnabled(
				Mockito.anyLong(), Mockito.eq("LPD-96750"))
		).thenReturn(
			enabled
		);
	}

	private long _mockRole(
		MockedStatic<RoleLocalServiceUtil> mockedStatic, String subtype) {

		long roleId = RandomTestUtil.randomLong();

		Role role = Mockito.mock(Role.class);

		Mockito.when(
			role.getSubtype()
		).thenReturn(
			subtype
		);

		Mockito.when(
			role.getType()
		).thenReturn(
			RoleConstants.TYPE_DEPOT
		);

		mockedStatic.when(
			() -> RoleLocalServiceUtil.getRole(roleId)
		).thenReturn(
			role
		);

		return roleId;
	}

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private static final long _USER_ID = RandomTestUtil.randomLong();

}
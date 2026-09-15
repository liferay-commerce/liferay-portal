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
import com.liferay.portal.kernel.service.UserGroupGroupRoleService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Lianne Louie
 */
public class DepotUserGroupGroupRoleServiceWrapperTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_userGroupGroupRoleService = Mockito.mock(
			UserGroupGroupRoleService.class);

		_depotUserGroupGroupRoleServiceWrapper =
			new DepotUserGroupGroupRoleServiceWrapper();

		_depotUserGroupGroupRoleServiceWrapper.setWrappedService(
			_userGroupGroupRoleService);

		_depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
			DepotEntryLocalServiceUtil.class);
		_featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
			FeatureFlagManagerUtil.class);
		_roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
			RoleLocalServiceUtil.class);

		_featureFlagManagerUtilMockedStatic.when(
			() -> FeatureFlagManagerUtil.isEnabled(
				Mockito.anyLong(), Mockito.eq("LPD-96750"))
		).thenReturn(
			true
		);

		DepotEntry depotEntry = Mockito.mock(DepotEntry.class);

		Mockito.when(
			depotEntry.getType()
		).thenReturn(
			DepotConstants.TYPE_PROJECT
		);

		_depotEntryLocalServiceUtilMockedStatic.when(
			() -> DepotEntryLocalServiceUtil.fetchGroupDepotEntry(_GROUP_ID)
		).thenReturn(
			depotEntry
		);

		_designLibraryRoleId = _mockRole(
			DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY);
		_projectRoleId = _mockRole(DepotRolesConstants.SUBTYPE_PROJECT);
	}

	@After
	public void tearDown() {
		_depotEntryLocalServiceUtilMockedStatic.close();
		_featureFlagManagerUtilMockedStatic.close();
		_roleLocalServiceUtilMockedStatic.close();
	}

	@Test
	public void testAddUserGroupGroupRoles() throws Exception {
		long[] roleIds = {_projectRoleId};

		_depotUserGroupGroupRoleServiceWrapper.addUserGroupGroupRoles(
			_USER_GROUP_ID, _GROUP_ID, roleIds);

		Mockito.verify(
			_userGroupGroupRoleService
		).addUserGroupGroupRoles(
			_USER_GROUP_ID, _GROUP_ID, roleIds
		);
	}

	@Test
	public void testAddUserGroupGroupRolesWhenRoleSubtypeIsForeign()
		throws Exception {

		long[] roleIds = {_designLibraryRoleId};

		Assert.assertThrows(
			RoleSubtypeException.class,
			() -> _depotUserGroupGroupRoleServiceWrapper.addUserGroupGroupRoles(
				_USER_GROUP_ID, _GROUP_ID, roleIds));

		Mockito.verify(
			_userGroupGroupRoleService, Mockito.never()
		).addUserGroupGroupRoles(
			_USER_GROUP_ID, _GROUP_ID, roleIds
		);
	}

	@Test
	public void testAddUserGroupGroupRolesWithUserGroupIds() throws Exception {
		long[] userGroupIds = {_USER_GROUP_ID};

		_depotUserGroupGroupRoleServiceWrapper.addUserGroupGroupRoles(
			userGroupIds, _GROUP_ID, _projectRoleId);

		Mockito.verify(
			_userGroupGroupRoleService
		).addUserGroupGroupRoles(
			userGroupIds, _GROUP_ID, _projectRoleId
		);
	}

	@Test
	public void testAddUserGroupGroupRolesWithUserGroupIdsWhenRoleSubtypeIsForeign()
		throws Exception {

		long[] userGroupIds = {_USER_GROUP_ID};

		Assert.assertThrows(
			RoleSubtypeException.class,
			() -> _depotUserGroupGroupRoleServiceWrapper.addUserGroupGroupRoles(
				userGroupIds, _GROUP_ID, _designLibraryRoleId));

		Mockito.verify(
			_userGroupGroupRoleService, Mockito.never()
		).addUserGroupGroupRoles(
			userGroupIds, _GROUP_ID, _designLibraryRoleId
		);
	}

	private long _mockRole(String subtype) {
		long roleId = RandomTestUtil.randomLong();

		Role role = Mockito.mock(Role.class);

		Mockito.when(
			role.getRoleId()
		).thenReturn(
			roleId
		);

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

		_roleLocalServiceUtilMockedStatic.when(
			() -> RoleLocalServiceUtil.getRole(roleId)
		).thenReturn(
			role
		);

		return roleId;
	}

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private static final long _USER_GROUP_ID = RandomTestUtil.randomLong();

	private MockedStatic<DepotEntryLocalServiceUtil>
		_depotEntryLocalServiceUtilMockedStatic;
	private DepotUserGroupGroupRoleServiceWrapper
		_depotUserGroupGroupRoleServiceWrapper;
	private long _designLibraryRoleId;
	private MockedStatic<FeatureFlagManagerUtil>
		_featureFlagManagerUtilMockedStatic;
	private long _projectRoleId;
	private MockedStatic<RoleLocalServiceUtil>
		_roleLocalServiceUtilMockedStatic;
	private UserGroupGroupRoleService _userGroupGroupRoleService;

}
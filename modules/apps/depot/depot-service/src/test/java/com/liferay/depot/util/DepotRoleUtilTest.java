/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.util;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.RoleSubtypeException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Stefano Motta
 */
public class DepotRoleUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testFilter() {
		Role role1 = _mockRole(DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY);
		Role role2 = _mockRole(DepotRolesConstants.SUBTYPE_PROJECT);
		Role role3 = _mockRole(DepotRolesConstants.SUBTYPE_SPACE);
		Role role4 = _mockRole(null);
		Role role5 = _mockRole("");

		try (MockedStatic<FeatureFlagManagerUtil> mockedStatic =
				Mockito.mockStatic(FeatureFlagManagerUtil.class)) {

			mockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					Mockito.anyLong(), Mockito.eq("LPD-96750"))
			).thenReturn(
				false
			);

			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					_mockDepotEntry(DepotConstants.TYPE_PROJECT),
					Arrays.asList(role1, role2, role3, role4, role5)));
			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_SPACE));

			mockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					Mockito.anyLong(), Mockito.eq("LPD-96750"))
			).thenReturn(
				true
			);

			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					(DepotEntry)null,
					Arrays.asList(role1, role2, role3, role4, role5)));
			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					_mockDepotEntry(DepotConstants.TYPE_ASSET_LIBRARY),
					Arrays.asList(role1, role2, role3, role4, role5)));
			Assert.assertEquals(
				Arrays.asList(role1),
				DepotRoleUtil.filter(
					_mockDepotEntry(DepotConstants.TYPE_DESIGN_LIBRARY),
					Arrays.asList(role1, role2, role3, role4, role5)));
			Assert.assertEquals(
				Arrays.asList(role2),
				DepotRoleUtil.filter(
					_mockDepotEntry(DepotConstants.TYPE_PROJECT),
					Arrays.asList(role1, role2, role3, role4, role5)));
			Assert.assertEquals(
				Arrays.asList(role3, role4, role5),
				DepotRoleUtil.filter(
					_mockDepotEntry(DepotConstants.TYPE_SPACE),
					Arrays.asList(role1, role2, role3, role4, role5)));
		}
	}

	@Test
	public void testFilterBySubtype() {
		Role role1 = _mockRole(DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY);
		Role role2 = _mockRole(DepotRolesConstants.SUBTYPE_PROJECT);
		Role role3 = _mockRole(DepotRolesConstants.SUBTYPE_SPACE);
		Role role4 = _mockRole(null);
		Role role5 = _mockRole("");

		try (MockedStatic<FeatureFlagManagerUtil> mockedStatic =
				Mockito.mockStatic(FeatureFlagManagerUtil.class)) {

			mockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					Mockito.anyLong(), Mockito.eq("LPD-96750"))
			).thenReturn(
				false
			);

			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_PROJECT));
			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_SPACE));

			mockedStatic.when(
				() -> FeatureFlagManagerUtil.isEnabled(
					Mockito.anyLong(), Mockito.eq("LPD-96750"))
			).thenReturn(
				true
			);

			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5), null));
			Assert.assertEquals(
				Arrays.asList(role1, role2, role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5), ""));
			Assert.assertEquals(
				Arrays.asList(role1),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY));
			Assert.assertEquals(
				Arrays.asList(role2),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_PROJECT));
			Assert.assertEquals(
				Arrays.asList(role3, role4, role5),
				DepotRoleUtil.filter(
					Arrays.asList(role1, role2, role3, role4, role5),
					DepotRolesConstants.SUBTYPE_SPACE));
		}
	}

	@Test
	public void testValidateWhenDepotEntryTypeIsAssetLibrary()
		throws Exception {

		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);
			_mockDepotEntry(
				depotEntryLocalServiceUtilMockedStatic, _ASSET_LIBRARY_GROUP_ID,
				DepotConstants.TYPE_ASSET_LIBRARY);

			DepotRoleUtil.validate(
				_ASSET_LIBRARY_GROUP_ID,
				new long[] {
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT,
						DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY),
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT,
						DepotRolesConstants.SUBTYPE_PROJECT),
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT,
						DepotRolesConstants.SUBTYPE_SPACE),
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT, null)
				});
		}
	}

	@Test
	public void testValidateWhenDepotEntryTypeIsDesignLibrary()
		throws Exception {

		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);
			_mockDepotEntry(
				depotEntryLocalServiceUtilMockedStatic,
				_DESIGN_LIBRARY_GROUP_ID, DepotConstants.TYPE_DESIGN_LIBRARY);

			DepotRoleUtil.validate(
				_DESIGN_LIBRARY_GROUP_ID,
				new long[] {
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT,
						DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY)
				});

			long projectRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic, RoleConstants.TYPE_DEPOT,
				DepotRolesConstants.SUBTYPE_PROJECT);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> DepotRoleUtil.validate(
					_DESIGN_LIBRARY_GROUP_ID, new long[] {projectRoleId}));

			long untaggedRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic, RoleConstants.TYPE_DEPOT,
				null);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> DepotRoleUtil.validate(
					_DESIGN_LIBRARY_GROUP_ID, new long[] {untaggedRoleId}));
		}
	}

	@Test
	public void testValidateWhenDepotEntryTypeIsProject() throws Exception {
		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);
			_mockDepotEntry(
				depotEntryLocalServiceUtilMockedStatic, _PROJECT_GROUP_ID,
				DepotConstants.TYPE_PROJECT);

			long projectRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic, RoleConstants.TYPE_DEPOT,
				DepotRolesConstants.SUBTYPE_PROJECT);

			DepotRoleUtil.validate(
				_PROJECT_GROUP_ID, new long[] {projectRoleId});

			long designLibraryRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic, RoleConstants.TYPE_DEPOT,
				DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> DepotRoleUtil.validate(
					_PROJECT_GROUP_ID, new long[] {designLibraryRoleId}));

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> DepotRoleUtil.validate(
					_PROJECT_GROUP_ID,
					new long[] {projectRoleId, designLibraryRoleId}));
		}
	}

	@Test
	public void testValidateWhenDepotEntryTypeIsSpace() throws Exception {
		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);
			_mockDepotEntry(
				depotEntryLocalServiceUtilMockedStatic, _SPACE_GROUP_ID,
				DepotConstants.TYPE_SPACE);

			DepotRoleUtil.validate(
				_SPACE_GROUP_ID,
				new long[] {
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT,
						DepotRolesConstants.SUBTYPE_SPACE),
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_DEPOT, null)
				});

			long projectRoleId = _mockRole(
				roleLocalServiceUtilMockedStatic, RoleConstants.TYPE_DEPOT,
				DepotRolesConstants.SUBTYPE_PROJECT);

			Assert.assertThrows(
				RoleSubtypeException.class,
				() -> DepotRoleUtil.validate(
					_SPACE_GROUP_ID, new long[] {projectRoleId}));
		}
	}

	@Test
	public void testValidateWhenFeatureFlagIsDisabled() throws Exception {
		try (MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, false);

			DepotRoleUtil.validate(
				RandomTestUtil.randomLong(),
				new long[] {RandomTestUtil.randomLong()});
		}
	}

	@Test
	public void testValidateWhenGroupIsNotDepotEntry() throws Exception {
		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);

			DepotRoleUtil.validate(
				_SITE_GROUP_ID, new long[] {RandomTestUtil.randomLong()});
		}
	}

	@Test
	public void testValidateWhenRoleTypeIsNotDepot() throws Exception {
		try (MockedStatic<DepotEntryLocalServiceUtil>
				depotEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					DepotEntryLocalServiceUtil.class);
			MockedStatic<FeatureFlagManagerUtil>
				featureFlagManagerUtilMockedStatic = Mockito.mockStatic(
					FeatureFlagManagerUtil.class);
			MockedStatic<RoleLocalServiceUtil>
				roleLocalServiceUtilMockedStatic = Mockito.mockStatic(
					RoleLocalServiceUtil.class)) {

			_mockFeatureFlag(featureFlagManagerUtilMockedStatic, true);
			_mockDepotEntry(
				depotEntryLocalServiceUtilMockedStatic, _PROJECT_GROUP_ID,
				DepotConstants.TYPE_PROJECT);

			DepotRoleUtil.validate(
				_PROJECT_GROUP_ID,
				new long[] {
					_mockRole(
						roleLocalServiceUtilMockedStatic,
						RoleConstants.TYPE_SITE,
						DepotRolesConstants.SUBTYPE_DESIGN_LIBRARY)
				});
		}
	}

	private DepotEntry _mockDepotEntry(int depotType) {
		DepotEntry depotEntry = Mockito.mock(DepotEntry.class);

		Mockito.when(
			depotEntry.getType()
		).thenReturn(
			depotType
		);

		return depotEntry;
	}

	private void _mockDepotEntry(
		MockedStatic<DepotEntryLocalServiceUtil> mockedStatic, long groupId,
		int depotType) {

		DepotEntry depotEntry = _mockDepotEntry(depotType);

		mockedStatic.when(
			() -> DepotEntryLocalServiceUtil.fetchGroupDepotEntry(groupId)
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
		MockedStatic<RoleLocalServiceUtil> mockedStatic, int type,
		String subtype) {

		long roleId = RandomTestUtil.randomLong();

		Role role = _mockRole(subtype);

		Mockito.when(
			role.getRoleId()
		).thenReturn(
			roleId
		);

		Mockito.when(
			role.getType()
		).thenReturn(
			type
		);

		mockedStatic.when(
			() -> RoleLocalServiceUtil.getRole(roleId)
		).thenReturn(
			role
		);

		return roleId;
	}

	private Role _mockRole(String subtype) {
		Role role = Mockito.mock(Role.class);

		Mockito.when(
			role.getSubtype()
		).thenReturn(
			subtype
		);

		return role;
	}

	private static final long _ASSET_LIBRARY_GROUP_ID =
		RandomTestUtil.randomLong();

	private static final long _DESIGN_LIBRARY_GROUP_ID =
		RandomTestUtil.randomLong();

	private static final long _PROJECT_GROUP_ID = RandomTestUtil.randomLong();

	private static final long _SITE_GROUP_ID = RandomTestUtil.randomLong();

	private static final long _SPACE_GROUP_ID = RandomTestUtil.randomLong();

}
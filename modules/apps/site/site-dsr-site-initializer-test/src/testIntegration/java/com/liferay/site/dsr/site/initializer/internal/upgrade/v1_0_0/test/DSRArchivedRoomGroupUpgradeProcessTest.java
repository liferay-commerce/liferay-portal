/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.upgrade.v1_0_0.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalServiceUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.site.dsr.site.initializer.test.util.DSRTestUtil;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matyas Wollner
 */
@FeatureFlags(
	featureFlags = {@FeatureFlag("LPD-66359"), @FeatureFlag("LPD-82960")}
)
@RunWith(Arquillian.class)
public class DSRArchivedRoomGroupUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		DSRTestUtil.getOrAddGroup();

		_accountEntry = AccountEntryLocalServiceUtil.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			"business", 1, ServiceContextTestUtil.getServiceContext());
	}

	@Test
	@TestInfo("LPD-97751")
	public void testUpgradeWithActiveRoom() throws Exception {
		long userId = TestPropsValues.getUserId();

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				getObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", TestPropsValues.getCompanyId());

		ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(
			0, userId, objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"name",
				StringUtil.toLowerCase("A" + RandomTestUtil.randomString())
			).put(
				"r_accountToDSRRooms_accountEntryId",
				_accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext());

		Group group = GroupLocalServiceUtil.fetchGroup(
			TestPropsValues.getCompanyId(),
			ClassNameLocalServiceUtil.getClassNameId(
				objectDefinition.getClassName()),
			objectEntry.getObjectEntryId());

		_runUpgrade();

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertTrue(group.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(group));
	}

	@Test
	@TestInfo("LPD-97751")
	public void testUpgradeWithArchivedRoom() throws Exception {
		long userId = TestPropsValues.getUserId();

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				getObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", TestPropsValues.getCompanyId());

		ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(
			0, userId, objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"name",
				StringUtil.toLowerCase("A" + RandomTestUtil.randomString())
			).put(
				"r_accountToDSRRooms_accountEntryId",
				_accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext());

		Group group = GroupLocalServiceUtil.fetchGroup(
			TestPropsValues.getCompanyId(),
			ClassNameLocalServiceUtil.getClassNameId(
				objectDefinition.getClassName()),
			objectEntry.getObjectEntryId());

		ObjectEntryLocalServiceUtil.partialUpdateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"roomStatus", WorkflowConstants.STATUS_INACTIVE
			).build(),
			ServiceContextTestUtil.getServiceContext());

		Group activeGroup = _groupLocalService.updateGroup(
			group.getGroupId(), group.getParentGroupId(), group.getNameMap(),
			group.getDescriptionMap(), group.getType(), group.getTypeSettings(),
			group.isManualMembership(), group.getMembershipRestriction(),
			group.getFriendlyURL(), group.isInheritContent(), true,
			ServiceContextTestUtil.getServiceContext());

		Assert.assertTrue(activeGroup.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(activeGroup));

		_runUpgrade();

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertFalse(group.isActive());
		Assert.assertTrue(_groupLocalService.isMaintenanceMode(group));
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private static final String _CLASS_NAME =
		"com.liferay.site.dsr.site.initializer.internal.upgrade.v1_0_0." +
			"DSRArchivedRoomGroupUpgradeProcess";

	private AccountEntry _accountEntry;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.site.dsr.site.initializer.internal.upgrade.registry.DSRSiteInitializerUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}
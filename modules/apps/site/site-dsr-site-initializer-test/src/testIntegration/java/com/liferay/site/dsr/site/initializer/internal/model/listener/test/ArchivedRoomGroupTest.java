/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.model.listener.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.GroupService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
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
public class ArchivedRoomGroupTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		DSRTestUtil.getOrAddGroup();

		_accountEntry = _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			"business", 1, ServiceContextTestUtil.getServiceContext());
		_objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", TestPropsValues.getCompanyId());
		_user = UserTestUtil.addUser();
	}

	@Test
	public void testArchiveRoom() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry();

		Group group = _getGroup(objectEntry);

		Assert.assertTrue(group.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(group));
		Assert.assertTrue(_hasUserSitesGroup(group));

		_updateRoomStatus(objectEntry, WorkflowConstants.STATUS_INACTIVE);

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertFalse(group.isActive());
		Assert.assertTrue(_groupLocalService.isMaintenanceMode(group));
		Assert.assertFalse(_hasUserSitesGroup(group));
	}

	@Test
	public void testRestoreRoom() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry();

		Group group = _getGroup(objectEntry);

		_updateRoomStatus(objectEntry, WorkflowConstants.STATUS_INACTIVE);

		_updateRoomStatus(objectEntry, WorkflowConstants.STATUS_APPROVED);

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertTrue(group.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(group));
		Assert.assertTrue(_hasUserSitesGroup(group));
	}

	private ObjectEntry _addObjectEntry() throws Exception {
		return _objectEntryLocalService.addObjectEntry(
			0, _user.getUserId(), _objectDefinition.getObjectDefinitionId(), 0,
			null,
			HashMapBuilder.<String, Serializable>put(
				"name",
				StringUtil.toLowerCase("A" + RandomTestUtil.randomString())
			).put(
				"r_accountToDSRRooms_accountEntryId",
				_accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private Group _getGroup(ObjectEntry objectEntry) throws Exception {
		return _groupLocalService.fetchGroup(
			TestPropsValues.getCompanyId(),
			_classNameLocalService.getClassNameId(
				_objectDefinition.getClassName()),
			objectEntry.getObjectEntryId());
	}

	private boolean _hasUserSitesGroup(Group group) throws Exception {
		if (ListUtil.exists(
				_groupService.getUserSitesGroups(
					_user.getUserId(), new String[] {Group.class.getName()},
					QueryUtil.ALL_POS),
				curGroup -> curGroup.getGroupId() == group.getGroupId())) {

			return true;
		}

		return false;
	}

	private void _updateRoomStatus(ObjectEntry objectEntry, int roomStatus)
		throws Exception {

		_objectEntryLocalService.partialUpdateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"roomStatus", roomStatus
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private AccountEntry _accountEntry;

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private GroupService _groupService;

	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private User _user;

}
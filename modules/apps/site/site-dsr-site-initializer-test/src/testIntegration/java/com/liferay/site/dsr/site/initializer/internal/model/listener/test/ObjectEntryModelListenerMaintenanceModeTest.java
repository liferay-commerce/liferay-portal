/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.model.listener.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.GroupService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.dsr.site.initializer.test.util.DSRTestUtil;

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
public class ObjectEntryModelListenerMaintenanceModeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		DSRTestUtil.getOrAddGroup();

		_accountEntry = DSRTestUtil.addAccountEntry();
		_user = UserTestUtil.addUser();
	}

	@Test
	@TestInfo("LPD-97751")
	public void testOnAfterUpdateWithArchivedRoom() throws Exception {
		ObjectEntry objectEntry = DSRTestUtil.addRoom(
			_accountEntry, _user.getUserId());

		Group group = DSRTestUtil.getRoomGroup(objectEntry);

		Assert.assertTrue(group.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(group));
		Assert.assertTrue(_hasUserSitesGroup(group));

		DSRTestUtil.updateRoomStatus(
			objectEntry, WorkflowConstants.STATUS_INACTIVE);

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertFalse(group.isActive());
		Assert.assertTrue(_groupLocalService.isMaintenanceMode(group));
		Assert.assertFalse(_hasUserSitesGroup(group));
	}

	@Test
	@TestInfo("LPD-97751")
	public void testOnAfterUpdateWithRestoredRoom() throws Exception {
		ObjectEntry objectEntry = DSRTestUtil.addRoom(
			_accountEntry, _user.getUserId());

		Group group = DSRTestUtil.getRoomGroup(objectEntry);

		DSRTestUtil.updateRoomStatus(
			objectEntry, WorkflowConstants.STATUS_INACTIVE);

		DSRTestUtil.updateRoomStatus(
			objectEntry, WorkflowConstants.STATUS_APPROVED);

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertTrue(group.isActive());
		Assert.assertFalse(_groupLocalService.isMaintenanceMode(group));
		Assert.assertTrue(_hasUserSitesGroup(group));
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

	private AccountEntry _accountEntry;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private GroupService _groupService;

	private User _user;

}
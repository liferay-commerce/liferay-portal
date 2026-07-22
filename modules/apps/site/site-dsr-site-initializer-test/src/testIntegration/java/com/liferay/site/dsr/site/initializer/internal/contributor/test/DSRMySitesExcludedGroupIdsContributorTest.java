/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.contributor.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.contributor.MySitesExcludedGroupIdsContributor;
import com.liferay.site.dsr.site.initializer.test.util.DSRTestUtil;

import java.io.Serializable;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@FeatureFlag("LPD-66359")
@RunWith(Arquillian.class)
public class DSRMySitesExcludedGroupIdsContributorTest {

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
	public void testGetExcludedGroupIds() throws Exception {
		Group group1 = _fetchGroup(
			_addObjectEntry(WorkflowConstants.STATUS_APPROVED));
		Group group2 = _fetchGroup(
			_addObjectEntry(WorkflowConstants.STATUS_INACTIVE));
		Group group3 = _addGroup();

		List<Long> excludedGroupIds =
			_mySitesExcludedGroupIdsContributor.getExcludedGroupIds(
				TestPropsValues.getCompanyId(), _user.getUserId());

		Assert.assertFalse(excludedGroupIds.contains(group1.getGroupId()));
		Assert.assertTrue(excludedGroupIds.contains(group2.getGroupId()));
		Assert.assertFalse(excludedGroupIds.contains(group3.getGroupId()));
	}

	private Group _addGroup() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_userLocalService.addGroupUsers(
			group.getGroupId(), new long[] {_user.getUserId()});

		return group;
	}

	private ObjectEntry _addObjectEntry(int roomStatus) throws Exception {
		return _objectEntryLocalService.addObjectEntry(
			0, _user.getUserId(), _objectDefinition.getObjectDefinitionId(), 0,
			null,
			HashMapBuilder.<String, Serializable>put(
				"name", "A" + RandomTestUtil.randomString()
			).put(
				"r_accountToDSRRooms_accountEntryId",
				_accountEntry.getAccountEntryId()
			).put(
				"roomStatus", roomStatus
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private Group _fetchGroup(ObjectEntry objectEntry) throws Exception {
		return _groupLocalService.fetchGroup(
			TestPropsValues.getCompanyId(),
			_classNameLocalService.getClassNameId(
				_objectDefinition.getClassName()),
			objectEntry.getObjectEntryId());
	}

	private AccountEntry _accountEntry;

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject(
		filter = "component.name=com.liferay.site.dsr.site.initializer.internal.contributor.DSRMySitesExcludedGroupIdsContributor"
	)
	private MySitesExcludedGroupIdsContributor
		_mySitesExcludedGroupIdsContributor;

	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
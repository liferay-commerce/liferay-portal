/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.model.listener.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.dsr.site.initializer.test.util.DSRTestUtil;
import com.liferay.site.dsr.site.initializer.util.DSRRoomUtil;

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
	featureFlags = {
		@FeatureFlag("LPD-66359"),
		@FeatureFlag(enable = false, value = "LPD-82960")
	}
)
@RunWith(Arquillian.class)
public class ObjectEntryModelListenerWithoutMaintenanceModeTest {

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
	}

	@Test
	@TestInfo("LPD-97751")
	public void testOnBeforeUpdateWithArchivedRoom() throws Exception {
		ObjectEntry objectEntry = DSRTestUtil.addRoom(
			_accountEntry, TestPropsValues.getUserId());

		Group group = DSRTestUtil.getRoomGroup(objectEntry);

		Assert.assertTrue(group.isActive());

		try {
			DSRTestUtil.updateRoomStatus(
				objectEntry, WorkflowConstants.STATUS_INACTIVE);

			Assert.fail();
		}
		catch (ModelListenerException modelListenerException) {
			Throwable throwable = modelListenerException.getCause();

			Assert.assertEquals(
				UnsupportedOperationException.class, throwable.getClass());

			String message = throwable.getMessage();

			Assert.assertTrue(message.contains("LPD-82960"));
		}

		Assert.assertFalse(
			DSRRoomUtil.isArchived(
				_objectEntryLocalService.getObjectEntry(
					objectEntry.getObjectEntryId())));

		group = _groupLocalService.getGroup(group.getGroupId());

		Assert.assertTrue(group.isActive());
	}

	private AccountEntry _accountEntry;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}
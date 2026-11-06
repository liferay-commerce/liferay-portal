/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.notification.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.notification.model.CommerceNotificationQueueEntry;
import com.liferay.commerce.notification.model.CommerceNotificationTemplate;
import com.liferay.commerce.notification.service.CommerceNotificationQueueEntryLocalService;
import com.liferay.commerce.notification.test.util.CommerceNotificationTestUtil;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class EditCommerceNotificationQueueEntryMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"A notification queue entry can be deleted"
		).given(
			"A channel with a notification queue entry"
		).when(
			"The notification queue entry is deleted"
		).then(
			"The notification queue entry no longer exists"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		CommerceNotificationTemplate commerceNotificationTemplate =
			CommerceNotificationTestUtil.addNotificationTemplate(
				RandomTestUtil.randomString(),
				CommerceOrderConstants.ORDER_NOTIFICATION_PLACED,
				ServiceContextTestUtil.getServiceContext(
					commerceChannel.getGroupId()));

		CommerceNotificationQueueEntry commerceNotificationQueueEntry =
			_commerceNotificationQueueEntryLocalService.
				addCommerceNotificationQueueEntry(
					TestPropsValues.getUserId(), commerceChannel.getGroupId(),
					RandomTestUtil.randomString(), RandomTestUtil.randomLong(),
					commerceNotificationTemplate.
						getCommerceNotificationTemplateId(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(), null, null,
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomDouble());

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.DELETE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceNotificationQueueEntryId",
			String.valueOf(
				commerceNotificationQueueEntry.
					getCommerceNotificationQueueEntryId()));

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Assert.assertNull(
			_commerceNotificationQueueEntryLocalService.
				fetchCommerceNotificationQueueEntry(
					commerceNotificationQueueEntry.
						getCommerceNotificationQueueEntryId()));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	@Inject
	private CommerceNotificationQueueEntryLocalService
		_commerceNotificationQueueEntryLocalService;

	@Inject(
		filter = "mvc.command.name=/commerce_channels/edit_commerce_notification_queue_entry",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
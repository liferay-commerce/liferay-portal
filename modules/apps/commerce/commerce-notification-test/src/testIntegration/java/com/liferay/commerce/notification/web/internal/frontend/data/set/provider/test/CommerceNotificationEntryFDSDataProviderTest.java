/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.notification.web.internal.frontend.data.set.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.notification.model.CommerceNotificationQueueEntry;
import com.liferay.commerce.notification.model.CommerceNotificationTemplate;
import com.liferay.commerce.notification.service.CommerceNotificationQueueEntryLocalService;
import com.liferay.commerce.notification.test.util.CommerceNotificationTestUtil;
import com.liferay.commerce.notification.type.CommerceNotificationType;
import com.liferay.commerce.notification.type.CommerceNotificationTypeRegistry;
import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.FDSDataProviderRegistry;
import com.liferay.frontend.data.set.provider.search.FDSKeywordsFactory;
import com.liferay.frontend.data.set.provider.search.FDSKeywordsFactoryRegistry;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class CommerceNotificationEntryFDSDataProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testGetItems() throws Exception {
		frutillaRule.scenario(
			"The notification queue of a channel lists its entries"
		).given(
			"A channel with a notification queue entry of an order placed " +
				"notification template"
		).when(
			"The notification queue of the channel is listed"
		).then(
			"The entry is listed with its sender, its recipient and the " +
				"notification type"
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

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter(
			"commerceChannelId",
			String.valueOf(commerceChannel.getCommerceChannelId()));

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLocale(LocaleUtil.US);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		FDSDataProvider<?> fdsDataProvider =
			_fdsDataProviderRegistry.getFDSDataProvider(_FDS_NAME);

		FDSKeywordsFactory fdsKeywordsFactory =
			_fdsKeywordsFactoryRegistry.getFDSKeywordsFactory(_FDS_NAME);

		List<?> items = fdsDataProvider.getItems(
			fdsKeywordsFactory.create(mockHttpServletRequest),
			new FDSPagination() {

				@Override
				public int getEndPosition() {
					return 2;
				}

				@Override
				public int getPage() {
					return 1;
				}

				@Override
				public int getPageSize() {
					return 2;
				}

				@Override
				public int getStartPosition() {
					return 0;
				}

			},
			mockHttpServletRequest, null);

		Assert.assertEquals(items.toString(), 1, items.size());

		Object item = items.get(0);

		Assert.assertEquals(
			commerceNotificationQueueEntry.getFromName(),
			BeanPropertiesUtil.getString(item, "from"));
		Assert.assertEquals(
			commerceNotificationQueueEntry.getToName(),
			BeanPropertiesUtil.getString(item, "to"));

		CommerceNotificationType commerceNotificationType =
			_commerceNotificationTypeRegistry.getCommerceNotificationType(
				CommerceOrderConstants.ORDER_NOTIFICATION_PLACED);

		Assert.assertEquals(
			commerceNotificationType.getLabel(LocaleUtil.US),
			BeanPropertiesUtil.getString(item, "type"));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private static final String _FDS_NAME =
		CPPortletKeys.COMMERCE_CHANNELS + "-notificationEntries";

	@Inject
	private CommerceNotificationQueueEntryLocalService
		_commerceNotificationQueueEntryLocalService;

	@Inject
	private CommerceNotificationTypeRegistry _commerceNotificationTypeRegistry;

	@Inject
	private FDSDataProviderRegistry _fdsDataProviderRegistry;

	@Inject
	private FDSKeywordsFactoryRegistry _fdsKeywordsFactoryRegistry;

}
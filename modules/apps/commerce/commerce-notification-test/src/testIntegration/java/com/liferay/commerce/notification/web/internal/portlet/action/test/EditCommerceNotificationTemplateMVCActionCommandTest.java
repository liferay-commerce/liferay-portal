/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.notification.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.notification.model.CommerceNotificationTemplate;
import com.liferay.commerce.notification.service.CommerceNotificationTemplateLocalService;
import com.liferay.commerce.notification.service.CommerceNotificationTemplateService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
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

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class EditCommerceNotificationTemplateMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"A notification template can be added, edited and deleted in a " +
				"channel"
		).given(
			"A channel"
		).when(
			"A notification template with a recipient longer than 75 " +
				"characters is added, then edited and then deleted"
		).then(
			"The channel holds the added template, then the edited values, " +
				"and finally no template"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		String name = RandomTestUtil.randomString();
		String to = RandomTestUtil.randomString(100);

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			_getMockLiferayPortletActionRequest(
				Constants.ADD, name, to,
				CommerceOrderConstants.ORDER_NOTIFICATION_PLACED);

		mockLiferayPortletActionRequest.setParameter(
			"commerceChannelId",
			String.valueOf(commerceChannel.getCommerceChannelId()));

		_processAction(mockLiferayPortletActionRequest);

		List<CommerceNotificationTemplate> commerceNotificationTemplates =
			_commerceNotificationTemplateService.
				getCommerceNotificationTemplates(
					commerceChannel.getGroupId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);

		Assert.assertEquals(
			commerceNotificationTemplates.toString(), 1,
			commerceNotificationTemplates.size());

		CommerceNotificationTemplate commerceNotificationTemplate =
			commerceNotificationTemplates.get(0);

		_assertCommerceNotificationTemplate(
			commerceNotificationTemplate.getCommerceNotificationTemplateId(),
			name, to, CommerceOrderConstants.ORDER_NOTIFICATION_PLACED);

		name = RandomTestUtil.randomString();
		to = RandomTestUtil.randomString(100);

		mockLiferayPortletActionRequest = _getMockLiferayPortletActionRequest(
			Constants.UPDATE, name, to,
			CommerceOrderConstants.ORDER_NOTIFICATION_COMPLETED);

		mockLiferayPortletActionRequest.setParameter(
			"commerceNotificationTemplateId",
			String.valueOf(
				commerceNotificationTemplate.
					getCommerceNotificationTemplateId()));

		_processAction(mockLiferayPortletActionRequest);

		_assertCommerceNotificationTemplate(
			commerceNotificationTemplate.getCommerceNotificationTemplateId(),
			name, to, CommerceOrderConstants.ORDER_NOTIFICATION_COMPLETED);

		mockLiferayPortletActionRequest = new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.DELETE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceNotificationTemplateId",
			String.valueOf(
				commerceNotificationTemplate.
					getCommerceNotificationTemplateId()));

		_processAction(mockLiferayPortletActionRequest);

		Assert.assertNull(
			_commerceNotificationTemplateLocalService.
				fetchCommerceNotificationTemplate(
					commerceNotificationTemplate.
						getCommerceNotificationTemplateId()));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _assertCommerceNotificationTemplate(
			long commerceNotificationTemplateId, String name, String to,
			String type)
		throws Exception {

		CommerceNotificationTemplate commerceNotificationTemplate =
			_commerceNotificationTemplateLocalService.
				getCommerceNotificationTemplate(commerceNotificationTemplateId);

		Assert.assertEquals(name, commerceNotificationTemplate.getName());
		Assert.assertEquals(to, commerceNotificationTemplate.getTo());
		Assert.assertEquals(type, commerceNotificationTemplate.getType());
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			String cmd, String name, String to, String type)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setUser(TestPropsValues.getUser());

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockLiferayPortletActionRequest.setParameter(Constants.CMD, cmd);
		mockLiferayPortletActionRequest.setParameter(
			"from", RandomTestUtil.randomString());
		mockLiferayPortletActionRequest.setParameter("name", name);
		mockLiferayPortletActionRequest.setParameter("to", to);
		mockLiferayPortletActionRequest.setParameter("type", type);

		return mockLiferayPortletActionRequest;
	}

	private void _processAction(
			MockLiferayPortletActionRequest mockLiferayPortletActionRequest)
		throws Exception {

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());
	}

	@Inject
	private CommerceNotificationTemplateLocalService
		_commerceNotificationTemplateLocalService;

	@Inject
	private CommerceNotificationTemplateService
		_commerceNotificationTemplateService;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(
		filter = "mvc.command.name=/commerce_channels/edit_commerce_notification_template",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
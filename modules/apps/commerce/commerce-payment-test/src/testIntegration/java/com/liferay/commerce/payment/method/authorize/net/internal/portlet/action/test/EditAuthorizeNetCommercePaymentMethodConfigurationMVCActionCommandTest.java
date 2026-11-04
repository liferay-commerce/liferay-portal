/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.method.authorize.net.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.portlet.MockPortletSession;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
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
public class
	EditAuthorizeNetCommercePaymentMethodConfigurationMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"The Authorize.Net payment method configuration is saved in the " +
				"channel settings"
		).given(
			"A channel"
		).when(
			"The configuration is saved"
		).then(
			"The channel settings hold the saved values"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		String apiLoginId = RandomTestUtil.randomString();
		String requireCaptcha = String.valueOf(RandomTestUtil.randomBoolean());
		String requireCardCodeVerification = String.valueOf(
			RandomTestUtil.randomBoolean());
		String showBankAccount = String.valueOf(RandomTestUtil.randomBoolean());
		String showCreditCard = String.valueOf(RandomTestUtil.randomBoolean());
		String showStoreName = String.valueOf(RandomTestUtil.randomBoolean());
		String signatureKey = RandomTestUtil.randomString();
		String transactionKey = RandomTestUtil.randomString();

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.UPDATE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceChannelId",
			String.valueOf(commerceChannel.getCommerceChannelId()));
		mockLiferayPortletActionRequest.setParameter(
			"settings--apiLoginId--", apiLoginId);
		mockLiferayPortletActionRequest.setParameter(
			"settings--environment--", "sandbox");
		mockLiferayPortletActionRequest.setParameter(
			"settings--requireCaptcha--", requireCaptcha);
		mockLiferayPortletActionRequest.setParameter(
			"settings--requireCardCodeVerification--",
			requireCardCodeVerification);
		mockLiferayPortletActionRequest.setParameter(
			"settings--showBankAccount--", showBankAccount);
		mockLiferayPortletActionRequest.setParameter(
			"settings--showCreditCard--", showCreditCard);
		mockLiferayPortletActionRequest.setParameter(
			"settings--showStoreName--", showStoreName);
		mockLiferayPortletActionRequest.setParameter(
			"settings--signatureKey--", signatureKey);
		mockLiferayPortletActionRequest.setParameter(
			"settings--transactionKey--", transactionKey);
		mockLiferayPortletActionRequest.setPortletSession(
			new MockPortletSession());

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Settings settings = FallbackKeysSettingsUtil.getSettings(
			new GroupServiceSettingsLocator(
				commerceChannel.getGroupId(),
				"com.liferay.commerce.payment.method.authorize.net"));

		Assert.assertEquals(apiLoginId, settings.getValue("apiLoginId", null));
		Assert.assertEquals("sandbox", settings.getValue("environment", null));
		Assert.assertEquals(
			requireCaptcha, settings.getValue("requireCaptcha", null));
		Assert.assertEquals(
			requireCardCodeVerification,
			settings.getValue("requireCardCodeVerification", null));
		Assert.assertEquals(
			showBankAccount, settings.getValue("showBankAccount", null));
		Assert.assertEquals(
			showCreditCard, settings.getValue("showCreditCard", null));
		Assert.assertEquals(
			showStoreName, settings.getValue("showStoreName", null));
		Assert.assertEquals(
			signatureKey, settings.getValue("signatureKey", null));
		Assert.assertEquals(
			transactionKey, settings.getValue("transactionKey", null));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	@Inject(
		filter = "mvc.command.name=/commerce_payment_methods/edit_authorize_net_commerce_payment_method_configuration",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
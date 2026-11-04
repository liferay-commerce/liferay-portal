/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.method.paypal.internal.portlet.action.test;

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
import com.liferay.portal.kernel.util.Portal;
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
public class EditPayPalCommercePaymentMethodConfigurationMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"The PayPal payment method configuration is saved in the channel " +
				"settings"
		).given(
			"A channel"
		).when(
			"The configuration is saved, and then saved again with the " +
				"masked client secret"
		).then(
			"The channel settings hold the saved values, and the masked " +
				"client secret does not replace the stored one"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		String clientId = RandomTestUtil.randomString();
		String clientSecret = RandomTestUtil.randomString();
		String paymentAttempts = String.valueOf(RandomTestUtil.randomInt());
		String requestDetailsOption = RandomTestUtil.randomString();

		_processAction(
			commerceChannel, clientId, clientSecret, "sandbox", paymentAttempts,
			requestDetailsOption);

		_assertSettings(
			commerceChannel, clientId, clientSecret, "sandbox", paymentAttempts,
			requestDetailsOption);

		clientId = RandomTestUtil.randomString();
		paymentAttempts = String.valueOf(RandomTestUtil.randomInt());
		requestDetailsOption = RandomTestUtil.randomString();

		_processAction(
			commerceChannel, clientId, Portal.TEMP_OBFUSCATION_VALUE, "live",
			paymentAttempts, requestDetailsOption);

		_assertSettings(
			commerceChannel, clientId, clientSecret, "live", paymentAttempts,
			requestDetailsOption);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _assertSettings(
			CommerceChannel commerceChannel, String clientId,
			String clientSecret, String mode, String paymentAttempts,
			String requestDetailsOption)
		throws Exception {

		Settings settings = FallbackKeysSettingsUtil.getSettings(
			new GroupServiceSettingsLocator(
				commerceChannel.getGroupId(),
				"com.liferay.commerce.payment.engine.method.paypal"));

		Assert.assertEquals(clientId, settings.getValue("clientId", null));
		Assert.assertEquals(
			clientSecret, settings.getValue("clientSecret", null));
		Assert.assertEquals(mode, settings.getValue("mode", null));
		Assert.assertEquals(
			paymentAttempts,
			settings.getValue("paymentAttemptsMaxCount", null));
		Assert.assertEquals(
			requestDetailsOption, settings.getValue("requestDetails", null));
	}

	private void _processAction(
			CommerceChannel commerceChannel, String clientId,
			String clientSecret, String mode, String paymentAttempts,
			String requestDetailsOption)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.UPDATE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceChannelId",
			String.valueOf(commerceChannel.getCommerceChannelId()));
		mockLiferayPortletActionRequest.setParameter(
			"settings--clientId--", clientId);
		mockLiferayPortletActionRequest.setParameter(
			"settings--clientSecret--", clientSecret);
		mockLiferayPortletActionRequest.setParameter("settings--mode--", mode);
		mockLiferayPortletActionRequest.setParameter(
			"settings--paymentAttempts--", paymentAttempts);
		mockLiferayPortletActionRequest.setParameter(
			"settings--request--", requestDetailsOption);
		mockLiferayPortletActionRequest.setPortletSession(
			new MockPortletSession());

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());
	}

	@Inject(
		filter = "mvc.command.name=/commerce_payment_methods/edit_paypal_commerce_payment_method_configuration",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
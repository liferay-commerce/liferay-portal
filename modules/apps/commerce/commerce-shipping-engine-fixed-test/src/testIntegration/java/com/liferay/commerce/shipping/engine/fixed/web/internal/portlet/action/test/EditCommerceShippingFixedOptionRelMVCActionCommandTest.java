/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shipping.engine.fixed.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOptionRel;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionRelLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

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
public class EditCommerceShippingFixedOptionRelMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"Deleting a shipping option setting removes it"
		).given(
			"A shipping option with a setting"
		).when(
			"The setting is deleted"
		).then(
			"The setting no longer exists"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		CommerceShippingMethod commerceShippingMethod =
			CommerceTestUtil.addCommerceShippingMethod(
				TestPropsValues.getUserId(), commerceChannel.getGroupId());

		CommerceShippingFixedOption commerceShippingFixedOption =
			CommerceTestUtil.addCommerceShippingFixedOption(
				commerceShippingMethod);

		CommerceShippingFixedOptionRel commerceShippingFixedOptionRel =
			_commerceShippingFixedOptionRelLocalService.
				addCommerceShippingFixedOptionRel(
					TestPropsValues.getUserId(),
					commerceShippingMethod.getGroupId(), 0,
					commerceShippingFixedOption.
						getCommerceShippingFixedOptionId(),
					commerceShippingMethod.getCommerceShippingMethodId(), 0, 0,
					BigDecimal.valueOf(RandomTestUtil.randomDouble()),
					RandomTestUtil.randomDouble(),
					BigDecimal.valueOf(RandomTestUtil.randomDouble()),
					RandomTestUtil.randomDouble(),
					RandomTestUtil.randomDouble(),
					RandomTestUtil.randomString());

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.DELETE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceShippingFixedOptionRelId",
			String.valueOf(
				commerceShippingFixedOptionRel.
					getCommerceShippingFixedOptionRelId()));

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Assert.assertNull(
			_commerceShippingFixedOptionRelLocalService.
				fetchCommerceShippingFixedOptionRel(
					commerceShippingFixedOptionRel.
						getCommerceShippingFixedOptionRelId()));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	@Inject
	private CommerceShippingFixedOptionRelLocalService
		_commerceShippingFixedOptionRelLocalService;

	@Inject(
		filter = "mvc.command.name=/commerce_shipping_methods/edit_commerce_shipping_fixed_option_rel",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
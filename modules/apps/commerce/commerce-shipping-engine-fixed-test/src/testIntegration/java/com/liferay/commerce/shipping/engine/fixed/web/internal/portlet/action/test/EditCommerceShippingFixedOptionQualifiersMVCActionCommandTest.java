/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shipping.engine.fixed.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOptionQualifier;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionQualifierLocalService;
import com.liferay.commerce.term.model.CommerceTermEntry;
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
public class EditCommerceShippingFixedOptionQualifiersMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"The eligibility of a shipping option can be reverted"
		).given(
			"A shipping option eligible for a term and for an order type"
		).when(
			"The term eligibility is set to none and then the order type " +
				"eligibility is set to all"
		).then(
			"Only the term qualifiers are removed, and then the order type " +
				"qualifiers are removed"
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

		_commerceShippingFixedOptionQualifierLocalService.
			addCommerceShippingFixedOptionQualifier(
				TestPropsValues.getUserId(), CommerceOrderType.class.getName(),
				RandomTestUtil.randomLong(),
				commerceShippingFixedOption.getCommerceShippingFixedOptionId());
		_commerceShippingFixedOptionQualifierLocalService.
			addCommerceShippingFixedOptionQualifier(
				TestPropsValues.getUserId(), CommerceTermEntry.class.getName(),
				RandomTestUtil.randomLong(),
				commerceShippingFixedOption.getCommerceShippingFixedOptionId());

		_processAction(
			commerceShippingFixedOption, "termEntryQualifiers", "none");

		_assertCommerceShippingFixedOptionQualifiers(
			CommerceOrderType.class.getName(), commerceShippingFixedOption, 1);
		_assertCommerceShippingFixedOptionQualifiers(
			CommerceTermEntry.class.getName(), commerceShippingFixedOption, 0);

		_processAction(
			commerceShippingFixedOption, "orderTypeQualifiers", "all");

		_assertCommerceShippingFixedOptionQualifiers(
			CommerceOrderType.class.getName(), commerceShippingFixedOption, 0);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _assertCommerceShippingFixedOptionQualifiers(
		String className,
		CommerceShippingFixedOption commerceShippingFixedOption,
		int expectedSize) {

		List<CommerceShippingFixedOptionQualifier>
			commerceShippingFixedOptionQualifiers =
				_commerceShippingFixedOptionQualifierLocalService.
					getCommerceShippingFixedOptionQualifiers(
						className,
						commerceShippingFixedOption.
							getCommerceShippingFixedOptionId());

		Assert.assertEquals(
			commerceShippingFixedOptionQualifiers.toString(), expectedSize,
			commerceShippingFixedOptionQualifiers.size());
	}

	private void _processAction(
			CommerceShippingFixedOption commerceShippingFixedOption,
			String name, String value)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.UPDATE);
		mockLiferayPortletActionRequest.setParameter(
			"commerceShippingFixedOptionId",
			String.valueOf(
				commerceShippingFixedOption.
					getCommerceShippingFixedOptionId()));
		mockLiferayPortletActionRequest.setParameter(name, value);

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());
	}

	@Inject
	private CommerceShippingFixedOptionQualifierLocalService
		_commerceShippingFixedOptionQualifierLocalService;

	@Inject(
		filter = "mvc.command.name=/commerce_shipping_methods/edit_commerce_shipping_fixed_option_qualifiers",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
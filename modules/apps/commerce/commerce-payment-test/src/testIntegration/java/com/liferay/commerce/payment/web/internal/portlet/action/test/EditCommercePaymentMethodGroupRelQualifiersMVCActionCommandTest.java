/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRelQualifier;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelQualifierLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
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
public class EditCommercePaymentMethodGroupRelQualifiersMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testProcessAction() throws Exception {
		frutillaRule.scenario(
			"The eligibility of a payment method can be reverted"
		).given(
			"A payment method eligible for a term and for an order type"
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

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			CommerceTestUtil.addCommercePaymentMethodGroupRel(
				TestPropsValues.getUserId(), commerceChannel.getGroupId());

		_commercePaymentMethodGroupRelQualifierLocalService.
			addCommercePaymentMethodGroupRelQualifier(
				TestPropsValues.getUserId(), CommerceOrderType.class.getName(),
				RandomTestUtil.randomLong(),
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId());
		_commercePaymentMethodGroupRelQualifierLocalService.
			addCommercePaymentMethodGroupRelQualifier(
				TestPropsValues.getUserId(), CommerceTermEntry.class.getName(),
				RandomTestUtil.randomLong(),
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId());

		_processAction(
			commercePaymentMethodGroupRel, "termEntryQualifiers", "none");

		_assertCommercePaymentMethodGroupRelQualifiers(
			CommerceOrderType.class.getName(), commercePaymentMethodGroupRel,
			1);
		_assertCommercePaymentMethodGroupRelQualifiers(
			CommerceTermEntry.class.getName(), commercePaymentMethodGroupRel,
			0);

		_processAction(
			commercePaymentMethodGroupRel, "orderTypeQualifiers", "all");

		_assertCommercePaymentMethodGroupRelQualifiers(
			CommerceOrderType.class.getName(), commercePaymentMethodGroupRel,
			0);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _assertCommercePaymentMethodGroupRelQualifiers(
		String className,
		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel,
		int expectedSize) {

		List<CommercePaymentMethodGroupRelQualifier>
			commercePaymentMethodGroupRelQualifiers =
				_commercePaymentMethodGroupRelQualifierLocalService.
					getCommercePaymentMethodGroupRelQualifiers(
						className,
						commercePaymentMethodGroupRel.
							getCommercePaymentMethodGroupRelId());

		Assert.assertEquals(
			commercePaymentMethodGroupRelQualifiers.toString(), expectedSize,
			commercePaymentMethodGroupRelQualifiers.size());
	}

	private void _processAction(
			CommercePaymentMethodGroupRel commercePaymentMethodGroupRel,
			String name, String value)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			Constants.CMD, Constants.UPDATE);
		mockLiferayPortletActionRequest.setParameter(
			"commercePaymentMethodGroupRelId",
			String.valueOf(
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId()));
		mockLiferayPortletActionRequest.setParameter(name, value);

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());
	}

	@Inject
	private CommercePaymentMethodGroupRelQualifierLocalService
		_commercePaymentMethodGroupRelQualifierLocalService;

	@Inject(
		filter = "mvc.command.name=/commerce_payment_methods/edit_commerce_payment_method_group_rel_qualifiers",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

}
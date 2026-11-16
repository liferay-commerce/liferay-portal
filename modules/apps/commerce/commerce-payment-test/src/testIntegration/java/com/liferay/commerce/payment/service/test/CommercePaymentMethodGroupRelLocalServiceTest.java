/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Locale;
import java.util.Map;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class CommercePaymentMethodGroupRelLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());
	}

	@Test
	public void testAddCommercePaymentMethodGroupRel() throws Exception {
		frutillaRule.scenario(
			"Adding a payment method to a channel saves its details"
		).given(
			"A channel"
		).when(
			"A payment method is added with a name, a description, a " +
				"priority and an active status"
		).then(
			"The payment method of the channel holds the saved details"
		);

		Map<Locale, String> descriptionMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> nameMap = RandomTestUtil.randomLocaleStringMap();
		String paymentIntegrationKey = RandomTestUtil.randomString();
		double priority = RandomTestUtil.randomDouble();

		_commercePaymentMethodGroupRelLocalService.
			addCommercePaymentMethodGroupRel(
				TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
				nameMap, descriptionMap, true, null, paymentIntegrationKey,
				priority, null);

		_assertCommercePaymentMethodGroupRel(
			true, descriptionMap, nameMap, paymentIntegrationKey, priority);
	}

	@Test
	public void testUpdateCommercePaymentMethodGroupRel() throws Exception {
		frutillaRule.scenario(
			"Editing a payment method of a channel saves its details"
		).given(
			"A channel with an active payment method"
		).when(
			"The name, the description, the priority and the active status " +
				"of the payment method are edited"
		).then(
			"The payment method of the channel holds the edited details"
		);

		String paymentIntegrationKey = RandomTestUtil.randomString();

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				addCommercePaymentMethodGroupRel(
					TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomLocaleStringMap(), true, null,
					paymentIntegrationKey, RandomTestUtil.randomDouble(), null);

		Map<Locale, String> descriptionMap =
			RandomTestUtil.randomLocaleStringMap();
		Map<Locale, String> nameMap = RandomTestUtil.randomLocaleStringMap();
		double priority = RandomTestUtil.randomDouble();

		_commercePaymentMethodGroupRelLocalService.
			updateCommercePaymentMethodGroupRel(
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId(),
				nameMap, descriptionMap, null, priority, false);

		_assertCommercePaymentMethodGroupRel(
			false, descriptionMap, nameMap, paymentIntegrationKey, priority);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _assertCommercePaymentMethodGroupRel(
			boolean active, Map<Locale, String> descriptionMap,
			Map<Locale, String> nameMap, String paymentIntegrationKey,
			double priority)
		throws Exception {

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				getCommercePaymentMethodGroupRel(
					_commerceChannel.getGroupId(), paymentIntegrationKey);

		Assert.assertEquals(active, commercePaymentMethodGroupRel.isActive());
		Assert.assertEquals(
			descriptionMap, commercePaymentMethodGroupRel.getDescriptionMap());
		Assert.assertEquals(
			nameMap, commercePaymentMethodGroupRel.getNameMap());
		Assert.assertEquals(
			priority, commercePaymentMethodGroupRel.getPriority(), 0);
	}

	private CommerceChannel _commerceChannel;

	@Inject
	private CommercePaymentMethodGroupRelLocalService
		_commercePaymentMethodGroupRelLocalService;

}
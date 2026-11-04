/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.payment.exception.DuplicateCommercePaymentMethodGroupRelQualifierException;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelQualifierLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.term.model.CommerceTermEntry;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
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
public class CommercePaymentMethodGroupRelQualifierLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testAddCommercePaymentMethodGroupRelQualifier()
		throws Exception {

		frutillaRule.scenario(
			"An eligibility element cannot be linked twice to the same " +
				"payment method"
		).given(
			"A payment method linked to a term and to an order type"
		).when(
			"The same term and the same order type are linked again"
		).then(
			"A duplicate qualifier exception is thrown"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			CommerceTestUtil.addCommercePaymentMethodGroupRel(
				TestPropsValues.getUserId(), commerceChannel.getGroupId());

		_testAddCommercePaymentMethodGroupRelQualifier(
			CommerceOrderType.class.getName(), commercePaymentMethodGroupRel);
		_testAddCommercePaymentMethodGroupRelQualifier(
			CommerceTermEntry.class.getName(), commercePaymentMethodGroupRel);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _testAddCommercePaymentMethodGroupRelQualifier(
			String className,
			CommercePaymentMethodGroupRel commercePaymentMethodGroupRel)
		throws Exception {

		long classPK = RandomTestUtil.randomLong();

		_commercePaymentMethodGroupRelQualifierLocalService.
			addCommercePaymentMethodGroupRelQualifier(
				TestPropsValues.getUserId(), className, classPK,
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId());

		try {
			_commercePaymentMethodGroupRelQualifierLocalService.
				addCommercePaymentMethodGroupRelQualifier(
					TestPropsValues.getUserId(), className, classPK,
					commercePaymentMethodGroupRel.
						getCommercePaymentMethodGroupRelId());

			Assert.fail();
		}
		catch (DuplicateCommercePaymentMethodGroupRelQualifierException
					duplicateCommercePaymentMethodGroupRelQualifierException) {
		}
	}

	@Inject
	private CommercePaymentMethodGroupRelQualifierLocalService
		_commercePaymentMethodGroupRelQualifierLocalService;

}
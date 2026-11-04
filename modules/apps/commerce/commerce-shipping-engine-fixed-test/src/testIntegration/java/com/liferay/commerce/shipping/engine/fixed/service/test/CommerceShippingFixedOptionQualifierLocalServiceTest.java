/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shipping.engine.fixed.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.shipping.engine.fixed.exception.DuplicateCommerceShippingFixedOptionQualifierException;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionQualifierLocalService;
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
public class CommerceShippingFixedOptionQualifierLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testAddCommerceShippingFixedOptionQualifier() throws Exception {
		frutillaRule.scenario(
			"An eligibility element cannot be linked twice to the same " +
				"shipping option"
		).given(
			"A shipping option linked to a term and to an order type"
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

		CommerceShippingMethod commerceShippingMethod =
			CommerceTestUtil.addCommerceShippingMethod(
				TestPropsValues.getUserId(), commerceChannel.getGroupId());

		CommerceShippingFixedOption commerceShippingFixedOption =
			CommerceTestUtil.addCommerceShippingFixedOption(
				commerceShippingMethod);

		_testAddCommerceShippingFixedOptionQualifier(
			CommerceOrderType.class.getName(), commerceShippingFixedOption);
		_testAddCommerceShippingFixedOptionQualifier(
			CommerceTermEntry.class.getName(), commerceShippingFixedOption);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private void _testAddCommerceShippingFixedOptionQualifier(
			String className,
			CommerceShippingFixedOption commerceShippingFixedOption)
		throws Exception {

		long classPK = RandomTestUtil.randomLong();

		_commerceShippingFixedOptionQualifierLocalService.
			addCommerceShippingFixedOptionQualifier(
				TestPropsValues.getUserId(), className, classPK,
				commerceShippingFixedOption.getCommerceShippingFixedOptionId());

		try {
			_commerceShippingFixedOptionQualifierLocalService.
				addCommerceShippingFixedOptionQualifier(
					TestPropsValues.getUserId(), className, classPK,
					commerceShippingFixedOption.
						getCommerceShippingFixedOptionId());

			Assert.fail();
		}
		catch (DuplicateCommerceShippingFixedOptionQualifierException
					duplicateCommerceShippingFixedOptionQualifierException) {
		}
	}

	@Inject
	private CommerceShippingFixedOptionQualifierLocalService
		_commerceShippingFixedOptionQualifierLocalService;

}
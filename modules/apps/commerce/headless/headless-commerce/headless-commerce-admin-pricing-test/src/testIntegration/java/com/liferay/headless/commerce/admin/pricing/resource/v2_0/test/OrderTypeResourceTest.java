/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.commerce.admin.pricing.resource.v2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountOrderTypeRel;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountOrderTypeRelLocalService;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.model.CommercePriceListOrderTypeRel;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListOrderTypeRelLocalService;
import com.liferay.commerce.service.CommerceOrderTypeLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.OrderType;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class OrderTypeResourceTest extends BaseOrderTypeResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, 0, true);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId());

		_commerceDiscount =
			_commerceDiscountLocalService.addOrUpdateCommerceDiscount(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(),
				CommerceDiscountConstants.TARGET_PRODUCTS, false, null, false,
				BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO,
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED, 0, true, 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commerceOrderType =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomBoolean(), 1, 1, 2022, 12, 0,
				RandomTestUtil.nextInt(), 0, 0, 0, 0, 0, true, serviceContext);

		_commerceDiscountOrderTypeRel =
			_commerceDiscountOrderTypeRelLocalService.
				addCommerceDiscountOrderTypeRel(
					_user.getUserId(),
					_commerceDiscount.getCommerceDiscountId(),
					_commerceOrderType.getCommerceOrderTypeId(),
					RandomTestUtil.nextInt(), serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commercePriceListOrderTypeRel =
			_commercePriceListOrderTypeRelLocalService.
				addCommercePriceListOrderTypeRel(
					_user.getUserId(),
					_commercePriceList.getCommercePriceListId(),
					_commerceOrderType.getCommerceOrderTypeId(),
					RandomTestUtil.nextInt(), serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected OrderType randomOrderType() throws Exception {
		return new OrderType() {
			{
				id = _commerceOrderType.getCommerceOrderTypeId();
				name = LanguageUtils.getLanguageIdMap(
					_commerceOrderType.getNameMap());
			}
		};
	}

	@Override
	protected OrderType testGetDiscountOrderTypeOrderType_addOrderType()
		throws Exception {

		return randomOrderType();
	}

	@Override
	protected Long testGetDiscountOrderTypeOrderType_getDiscountOrderTypeId()
		throws Exception {

		return _commerceDiscountOrderTypeRel.
			getCommerceDiscountOrderTypeRelId();
	}

	@Override
	protected OrderType testGetPriceListOrderTypeOrderType_addOrderType()
		throws Exception {

		return randomOrderType();
	}

	@Override
	protected Long testGetPriceListOrderTypeOrderType_getPriceListOrderTypeId()
		throws Exception {

		return _commercePriceListOrderTypeRel.
			getCommercePriceListOrderTypeRelId();
	}

	@Override
	protected Long
			testGraphQLGetDiscountOrderTypeOrderType_getDiscountOrderTypeId()
		throws Exception {

		return _commerceDiscountOrderTypeRel.
			getCommerceDiscountOrderTypeRelId();
	}

	@Override
	protected Long
			testGraphQLGetPriceListOrderTypeOrderType_getPriceListOrderTypeId()
		throws Exception {

		return _commercePriceListOrderTypeRel.
			getCommercePriceListOrderTypeRelId();
	}

	@Override
	protected OrderType testGraphQLOrderType_addOrderType() throws Exception {
		return randomOrderType();
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommerceDiscountOrderTypeRelLocalService
		_commerceDiscountOrderTypeRelLocalService;

	@Inject
	private static CommerceOrderTypeLocalService _commerceOrderTypeLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@Inject
	private static CommercePriceListOrderTypeRelLocalService
		_commercePriceListOrderTypeRelLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private CommerceDiscountOrderTypeRel _commerceDiscountOrderTypeRel;

	@DeleteAfterTestRun
	private CommerceOrderType _commerceOrderType;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceListOrderTypeRel _commercePriceListOrderTypeRel;

	@DeleteAfterTestRun
	private User _user;

}
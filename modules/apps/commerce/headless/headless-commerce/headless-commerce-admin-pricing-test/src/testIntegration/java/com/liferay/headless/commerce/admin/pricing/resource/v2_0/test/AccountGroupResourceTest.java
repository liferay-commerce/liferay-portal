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

import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountCommerceAccountGroupRel;
import com.liferay.commerce.discount.service.CommerceDiscountCommerceAccountGroupRelLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.model.CommercePriceListCommerceAccountGroupRel;
import com.liferay.commerce.price.list.service.CommercePriceListCommerceAccountGroupRelLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.AccountGroup;
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
public class AccountGroupResourceTest extends BaseAccountGroupResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_accountGroup = _accountGroupLocalService.addAccountGroup(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString());
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

		_commerceDiscountAccountGroupRel =
			_commerceDiscountCommerceAccountGroupRelLocalService.
				addCommerceDiscountCommerceAccountGroupRel(
					_user.getUserId(),
					_commerceDiscount.getCommerceDiscountId(),
					_accountGroup.getAccountGroupId(), serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commercePriceListAccountGroupRel =
			_commercePriceListCommerceAccountGroupRelLocalService.
				addCommercePriceListCommerceAccountGroupRel(
					_user.getUserId(),
					_commercePriceList.getCommercePriceListId(),
					_accountGroup.getAccountGroupId(), RandomTestUtil.nextInt(),
					serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected AccountGroup randomAccountGroup() throws Exception {
		return new AccountGroup() {
			{
				id = _accountGroup.getAccountGroupId();
				name = _accountGroup.getName();
			}
		};
	}

	@Override
	protected AccountGroup
			testGetDiscountAccountGroupAccountGroup_addAccountGroup()
		throws Exception {

		return randomAccountGroup();
	}

	@Override
	protected Long
			testGetDiscountAccountGroupAccountGroup_getDiscountAccountGroupId()
		throws Exception {

		return _commerceDiscountAccountGroupRel.
			getCommerceDiscountCommerceAccountGroupRelId();
	}

	@Override
	protected AccountGroup
			testGetPriceListAccountGroupAccountGroup_addAccountGroup()
		throws Exception {

		return randomAccountGroup();
	}

	@Override
	protected Long
			testGetPriceListAccountGroupAccountGroup_getPriceListAccountGroupId()
		throws Exception {

		return _commercePriceListAccountGroupRel.
			getCommercePriceListCommerceAccountGroupRelId();
	}

	@Override
	protected AccountGroup testGraphQLAccountGroup_addAccountGroup()
		throws Exception {

		return randomAccountGroup();
	}

	@Override
	protected Long
			testGraphQLGetDiscountAccountGroupAccountGroup_getDiscountAccountGroupId()
		throws Exception {

		return _commerceDiscountAccountGroupRel.
			getCommerceDiscountCommerceAccountGroupRelId();
	}

	@Override
	protected Long
			testGraphQLGetPriceListAccountGroupAccountGroup_getPriceListAccountGroupId()
		throws Exception {

		return _commercePriceListAccountGroupRel.
			getCommercePriceListCommerceAccountGroupRelId();
	}

	@Inject
	private static AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountCommerceAccountGroupRelLocalService
		_commerceDiscountCommerceAccountGroupRelLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommercePriceListCommerceAccountGroupRelLocalService
		_commercePriceListCommerceAccountGroupRelLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private com.liferay.account.model.AccountGroup _accountGroup;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private CommerceDiscountCommerceAccountGroupRel
		_commerceDiscountAccountGroupRel;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceListCommerceAccountGroupRel
		_commercePriceListAccountGroupRel;

	@DeleteAfterTestRun
	private User _user;

}
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

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountAccountRel;
import com.liferay.commerce.discount.service.CommerceDiscountAccountRelLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.model.CommercePriceListAccountRel;
import com.liferay.commerce.price.list.service.CommercePriceListAccountRelLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.Account;
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
public class AccountResourceTest extends BaseAccountResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId());

		_accountEntry = _accountEntryLocalService.addOrUpdateAccountEntry(
			RandomTestUtil.randomString(), _user.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 10, serviceContext);

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, 0, true);

		_commerceDiscount =
			_commerceDiscountLocalService.addOrUpdateCommerceDiscount(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(),
				CommerceDiscountConstants.TARGET_PRODUCTS, false, null, false,
				BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO,
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED, 0, true, 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commerceDiscountAccountRel =
			_commerceDiscountAccountRelLocalService.
				addCommerceDiscountAccountRel(
					_user.getUserId(),
					_commerceDiscount.getCommerceDiscountId(),
					_accountEntry.getAccountEntryId(), serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commercePriceListAccountRel =
			_commercePriceListAccountRelLocalService.
				addCommercePriceListAccountRel(
					_user.getUserId(),
					_commercePriceList.getCommercePriceListId(),
					_accountEntry.getAccountEntryId(), RandomTestUtil.nextInt(),
					serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected Account randomAccount() throws Exception {
		return new Account() {
			{
				id = _accountEntry.getAccountEntryId();
				logoId = _accountEntry.getLogoId();
				name = _accountEntry.getName();
			}
		};
	}

	@Override
	protected Account testGetDiscountAccountAccount_addAccount()
		throws Exception {

		return randomAccount();
	}

	@Override
	protected Long testGetDiscountAccountAccount_getDiscountAccountId()
		throws Exception {

		return _commerceDiscountAccountRel.getCommerceDiscountAccountRelId();
	}

	@Override
	protected Account testGetPriceListAccountAccount_addAccount()
		throws Exception {

		return randomAccount();
	}

	@Override
	protected Long testGetPriceListAccountAccount_getPriceListAccountId()
		throws Exception {

		return _commercePriceListAccountRel.getCommercePriceListAccountRelId();
	}

	@Override
	protected Account testGraphQLAccount_addAccount() throws Exception {
		return randomAccount();
	}

	@Override
	protected Long testGraphQLGetDiscountAccountAccount_getDiscountAccountId()
		throws Exception {

		return _commerceDiscountAccountRel.getCommerceDiscountAccountRelId();
	}

	@Override
	protected Account testGraphQLGetPriceListAccountAccount_addAccount()
		throws Exception {

		return randomAccount();
	}

	@Override
	protected Long testGraphQLGetPriceListAccountAccount_getPriceListAccountId()
		throws Exception {

		return _commercePriceListAccountRel.getCommercePriceListAccountRelId();
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountAccountRelLocalService
		_commerceDiscountAccountRelLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommercePriceListAccountRelLocalService
		_commercePriceListAccountRelLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private CommerceDiscountAccountRel _commerceDiscountAccountRel;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceListAccountRel _commercePriceListAccountRel;

	@DeleteAfterTestRun
	private User _user;

}
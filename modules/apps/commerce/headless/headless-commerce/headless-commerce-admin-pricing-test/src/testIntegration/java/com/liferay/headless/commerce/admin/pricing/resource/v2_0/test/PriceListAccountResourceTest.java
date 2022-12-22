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
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceListAccount;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class PriceListAccountResourceTest
	extends BasePriceListAccountResourceTestCase {

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

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);
	}

	@Override
	@Test
	public void testDeletePriceListAccount() throws Exception {
		PriceListAccount priceListAccount =
			priceListAccountResource.postPriceListIdPriceListAccount(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListAccount());

		assertHttpResponseStatusCode(
			204,
			priceListAccountResource.deletePriceListAccountHttpResponse(
				priceListAccount.getPriceListAccountId()));

		assertHttpResponseStatusCode(
			404,
			priceListAccountResource.deletePriceListAccountHttpResponse(
				priceListAccount.getPriceListAccountId()));

		assertHttpResponseStatusCode(
			404,
			priceListAccountResource.deletePriceListAccountHttpResponse(
				priceListAccount.getPriceListAccountId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceListAccount() throws Exception {
		PriceListAccount priceListAccount =
			priceListAccountResource.postPriceListIdPriceListAccount(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListAccount());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceListAccount",
						HashMapBuilder.<String, Object>put(
							"priceListAccountId",
							priceListAccount.getPriceListAccountId()
						).build())),
				"JSONObject/data", "Object/deletePriceListAccount"));
	}

	@Override
	protected Collection<EntityField> getEntityFields() throws Exception {
		try {
			return super.getEntityFields();
		}
		catch (NullPointerException nullPointerException) {
			Map<String, EntityField> entityFieldsMap = new HashMap<>();

			return entityFieldsMap.values();
		}
	}

	@Override
	protected PriceListAccount randomPriceListAccount() throws Exception {
		AccountEntry accountEntry =
			_accountEntryLocalService.addOrUpdateAccountEntry(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, RandomTestUtil.randomString() + "@liferay.com", null,
				null, AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 10,
				_serviceContext);

		_accountEntries.add(accountEntry);

		return new PriceListAccount() {
			{
				accountExternalReferenceCode =
					accountEntry.getExternalReferenceCode();
				accountId = accountEntry.getAccountEntryId();
				order = RandomTestUtil.randomInt();
				priceListAccountId = RandomTestUtil.randomLong();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
			}
		};
	}

	@Override
	protected PriceListAccount
			testGetPriceListByExternalReferenceCodePriceListAccountsPage_addPriceListAccount(
				String externalReferenceCode, PriceListAccount priceListAccount)
		throws Exception {

		return priceListAccountResource.
			postPriceListByExternalReferenceCodePriceListAccount(
				externalReferenceCode, priceListAccount);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceListAccountsPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceListAccount
			testGetPriceListIdPriceListAccountsPage_addPriceListAccount(
				Long id, PriceListAccount priceListAccount)
		throws Exception {

		return priceListAccountResource.postPriceListIdPriceListAccount(
			id, priceListAccount);
	}

	@Override
	protected Long testGetPriceListIdPriceListAccountsPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceListAccount
			testPostPriceListByExternalReferenceCodePriceListAccount_addPriceListAccount(
				PriceListAccount priceListAccount)
		throws Exception {

		return priceListAccountResource.
			postPriceListByExternalReferenceCodePriceListAccount(
				_commercePriceList.getExternalReferenceCode(),
				priceListAccount);
	}

	@Override
	protected PriceListAccount
			testPostPriceListIdPriceListAccount_addPriceListAccount(
				PriceListAccount priceListAccount)
		throws Exception {

		return priceListAccountResource.postPriceListIdPriceListAccount(
			_commercePriceList.getCommercePriceListId(), priceListAccount);
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private final List<AccountEntry> _accountEntries = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
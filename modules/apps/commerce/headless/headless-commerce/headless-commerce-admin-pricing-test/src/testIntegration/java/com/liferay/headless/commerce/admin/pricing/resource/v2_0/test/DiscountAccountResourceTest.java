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
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountAccount;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collection;
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
public class DiscountAccountResourceTest
	extends BaseDiscountAccountResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
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
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);
	}

	@Override
	@Test
	public void testDeleteDiscountAccount() throws Exception {
		DiscountAccount discountAccount =
			discountAccountResource.postDiscountIdDiscountAccount(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountAccount());

		assertHttpResponseStatusCode(
			204,
			discountAccountResource.deleteDiscountAccountHttpResponse(
				discountAccount.getDiscountAccountId()));

		assertHttpResponseStatusCode(
			404,
			discountAccountResource.deleteDiscountAccountHttpResponse(
				discountAccount.getDiscountAccountId()));

		assertHttpResponseStatusCode(
			404,
			discountAccountResource.deleteDiscountAccountHttpResponse(
				discountAccount.getDiscountAccountId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountAccount() throws Exception {
		DiscountAccount discountAccount =
			discountAccountResource.postDiscountIdDiscountAccount(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountAccount());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountAccount",
						HashMapBuilder.<String, Object>put(
							"discountAccountId",
							discountAccount.getDiscountAccountId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountAccount"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"accountId", "discountExternalReferenceCode", "discountId"
		};
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
	protected DiscountAccount randomDiscountAccount() throws Exception {
		AccountEntry accountEntry =
			_accountEntryLocalService.addOrUpdateAccountEntry(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, RandomTestUtil.randomString() + "@liferay.com", null,
				null, AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 10,
				_serviceContext);

		_accountEntries.add(accountEntry);

		return new DiscountAccount() {
			{
				accountExternalReferenceCode =
					accountEntry.getExternalReferenceCode();
				accountId = accountEntry.getAccountEntryId();
				discountAccountId = RandomTestUtil.randomLong();
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
			}
		};
	}

	@Override
	protected DiscountAccount
			testGetDiscountByExternalReferenceCodeDiscountAccountsPage_addDiscountAccount(
				String externalReferenceCode, DiscountAccount discountAccount)
		throws Exception {

		return discountAccountResource.
			postDiscountByExternalReferenceCodeDiscountAccount(
				externalReferenceCode, discountAccount);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountAccountsPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountAccount
			testGetDiscountIdDiscountAccountsPage_addDiscountAccount(
				Long id, DiscountAccount discountAccount)
		throws Exception {

		return discountAccountResource.postDiscountIdDiscountAccount(
			id, discountAccount);
	}

	@Override
	protected Long testGetDiscountIdDiscountAccountsPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountAccount
			testPostDiscountByExternalReferenceCodeDiscountAccount_addDiscountAccount(
				DiscountAccount discountAccount)
		throws Exception {

		return discountAccountResource.
			postDiscountByExternalReferenceCodeDiscountAccount(
				_commerceDiscount.getExternalReferenceCode(), discountAccount);
	}

	@Override
	protected DiscountAccount
			testPostDiscountIdDiscountAccount_addDiscountAccount(
				DiscountAccount discountAccount)
		throws Exception {

		return discountAccountResource.postDiscountIdDiscountAccount(
			_commerceDiscount.getCommerceDiscountId(), discountAccount);
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@DeleteAfterTestRun
	private final List<AccountEntry> _accountEntries = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
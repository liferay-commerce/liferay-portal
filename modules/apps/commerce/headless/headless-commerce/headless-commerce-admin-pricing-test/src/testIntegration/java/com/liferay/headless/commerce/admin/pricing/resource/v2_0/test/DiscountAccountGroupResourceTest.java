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

import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountAccountGroup;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
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
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class DiscountAccountGroupResourceTest
	extends BaseDiscountAccountGroupResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_commerceDiscount =
			_commerceDiscountLocalService.addOrUpdateCommerceDiscount(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(),
				CommerceDiscountConstants.TARGET_PRODUCTS, false, null, false,
				BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO,
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED, 0, true, 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true,
				ServiceContextTestUtil.getServiceContext(
					testCompany.getCompanyId(), testGroup.getGroupId(),
					_user.getUserId()));
	}

	@Override
	@Test
	public void testDeleteDiscountAccountGroup() throws Exception {
		DiscountAccountGroup discountAccountGroup =
			discountAccountGroupResource.postDiscountIdDiscountAccountGroup(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountAccountGroup());

		assertHttpResponseStatusCode(
			204,
			discountAccountGroupResource.deleteDiscountAccountGroupHttpResponse(
				discountAccountGroup.getDiscountAccountGroupId()));

		assertHttpResponseStatusCode(
			404,
			discountAccountGroupResource.deleteDiscountAccountGroupHttpResponse(
				discountAccountGroup.getDiscountAccountGroupId()));

		assertHttpResponseStatusCode(
			404,
			discountAccountGroupResource.deleteDiscountAccountGroupHttpResponse(
				discountAccountGroup.getDiscountAccountGroupId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountAccountGroup() throws Exception {
		DiscountAccountGroup discountAccountGroup =
			discountAccountGroupResource.postDiscountIdDiscountAccountGroup(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountAccountGroup());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountAccountGroup",
						HashMapBuilder.<String, Object>put(
							"discountAccountGroupId",
							discountAccountGroup.getDiscountAccountGroupId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountAccountGroup"));
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
	protected DiscountAccountGroup randomDiscountAccountGroup()
		throws Exception {

		AccountGroup localAccountGroup =
			_accountGroupLocalService.addAccountGroup(
				_user.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString());

		_accountGroups.add(localAccountGroup);

		return new DiscountAccountGroup() {
			{
				accountGroupExternalReferenceCode =
					localAccountGroup.getExternalReferenceCode();
				accountGroupId = localAccountGroup.getAccountGroupId();
				discountAccountGroupId = RandomTestUtil.randomLong();
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
			}
		};
	}

	@Override
	protected DiscountAccountGroup
			testGetDiscountByExternalReferenceCodeDiscountAccountGroupsPage_addDiscountAccountGroup(
				String externalReferenceCode,
				DiscountAccountGroup discountAccountGroup)
		throws Exception {

		return discountAccountGroupResource.
			postDiscountByExternalReferenceCodeDiscountAccountGroup(
				externalReferenceCode, discountAccountGroup);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountAccountGroupsPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountAccountGroup
			testGetDiscountIdDiscountAccountGroupsPage_addDiscountAccountGroup(
				Long id, DiscountAccountGroup discountAccountGroup)
		throws Exception {

		return discountAccountGroupResource.postDiscountIdDiscountAccountGroup(
			id, discountAccountGroup);
	}

	@Override
	protected Long testGetDiscountIdDiscountAccountGroupsPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountAccountGroup
			testPostDiscountByExternalReferenceCodeDiscountAccountGroup_addDiscountAccountGroup(
				DiscountAccountGroup discountAccountGroup)
		throws Exception {

		return discountAccountGroupResource.
			postDiscountByExternalReferenceCodeDiscountAccountGroup(
				_commerceDiscount.getExternalReferenceCode(),
				discountAccountGroup);
	}

	@Override
	protected DiscountAccountGroup
			testPostDiscountIdDiscountAccountGroup_addDiscountAccountGroup(
				DiscountAccountGroup discountAccountGroup)
		throws Exception {

		return discountAccountGroupResource.postDiscountIdDiscountAccountGroup(
			_commerceDiscount.getCommerceDiscountId(), discountAccountGroup);
	}

	@Inject
	private static AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@DeleteAfterTestRun
	private final List<AccountGroup> _accountGroups = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private User _user;

}
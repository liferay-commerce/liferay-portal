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
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.service.CommerceOrderTypeLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountOrderType;
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
public class DiscountOrderTypeResourceTest
	extends BaseDiscountOrderTypeResourceTestCase {

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
	public void testDeleteDiscountOrderType() throws Exception {
		DiscountOrderType discountOrderType =
			discountOrderTypeResource.postDiscountIdDiscountOrderType(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountOrderType());

		assertHttpResponseStatusCode(
			204,
			discountOrderTypeResource.deleteDiscountOrderTypeHttpResponse(
				discountOrderType.getDiscountOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			discountOrderTypeResource.deleteDiscountOrderTypeHttpResponse(
				discountOrderType.getDiscountOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			discountOrderTypeResource.deleteDiscountOrderTypeHttpResponse(
				discountOrderType.getDiscountOrderTypeId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountOrderType() throws Exception {
		DiscountOrderType discountOrderType =
			discountOrderTypeResource.postDiscountIdDiscountOrderType(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountOrderType());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountOrderType",
						HashMapBuilder.<String, Object>put(
							"discountOrderTypeId",
							discountOrderType.getDiscountOrderTypeId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountOrderType"));
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
	protected DiscountOrderType randomDiscountOrderType() throws Exception {
		CommerceOrderType localOrderType =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true, 1, 1, 2022, 12, 0,
				RandomTestUtil.nextInt(), 0, 0, 0, 0, 0, true, _serviceContext);

		_commerceOrderTypes.add(localOrderType);

		return new DiscountOrderType() {
			{
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
				discountOrderTypeId = RandomTestUtil.randomLong();
				orderTypeExternalReferenceCode =
					localOrderType.getExternalReferenceCode();
				orderTypeId = localOrderType.getCommerceOrderTypeId();
				priority = RandomTestUtil.randomInt();
			}
		};
	}

	@Override
	protected DiscountOrderType
			testGetDiscountByExternalReferenceCodeDiscountOrderTypesPage_addDiscountOrderType(
				String externalReferenceCode,
				DiscountOrderType discountOrderType)
		throws Exception {

		return discountOrderTypeResource.
			postDiscountByExternalReferenceCodeDiscountOrderType(
				externalReferenceCode, discountOrderType);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountOrderTypesPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountOrderType
			testGetDiscountIdDiscountOrderTypesPage_addDiscountOrderType(
				Long id, DiscountOrderType discountOrderType)
		throws Exception {

		return discountOrderTypeResource.postDiscountIdDiscountOrderType(
			id, discountOrderType);
	}

	@Override
	protected Long testGetDiscountIdDiscountOrderTypesPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountOrderType
			testPostDiscountByExternalReferenceCodeDiscountOrderType_addDiscountOrderType(
				DiscountOrderType discountOrderType)
		throws Exception {

		return discountOrderTypeResource.
			postDiscountByExternalReferenceCodeDiscountOrderType(
				_commerceDiscount.getExternalReferenceCode(),
				discountOrderType);
	}

	@Override
	protected DiscountOrderType
			testPostDiscountIdDiscountOrderType_addDiscountOrderType(
				DiscountOrderType discountOrderType)
		throws Exception {

		return discountOrderTypeResource.postDiscountIdDiscountOrderType(
			_commerceDiscount.getCommerceDiscountId(), discountOrderType);
	}

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommerceOrderTypeLocalService _commerceOrderTypeLocalService;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private final List<CommerceOrderType> _commerceOrderTypes =
		new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
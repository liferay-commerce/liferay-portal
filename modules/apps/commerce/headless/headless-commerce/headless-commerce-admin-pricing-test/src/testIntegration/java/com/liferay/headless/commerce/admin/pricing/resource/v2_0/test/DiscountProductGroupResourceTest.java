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
import com.liferay.commerce.pricing.model.CommercePricingClass;
import com.liferay.commerce.pricing.service.CommercePricingClassLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountProductGroup;
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
public class DiscountProductGroupResourceTest
	extends BaseDiscountProductGroupResourceTestCase {

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
	public void testDeleteDiscountProductGroup() throws Exception {
		DiscountProductGroup discountProductGroup =
			discountProductGroupResource.postDiscountIdDiscountProductGroup(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountProductGroup());

		assertHttpResponseStatusCode(
			204,
			discountProductGroupResource.deleteDiscountProductGroupHttpResponse(
				discountProductGroup.getDiscountProductGroupId()));

		assertHttpResponseStatusCode(
			404,
			discountProductGroupResource.deleteDiscountProductGroupHttpResponse(
				discountProductGroup.getDiscountProductGroupId()));

		assertHttpResponseStatusCode(
			404,
			discountProductGroupResource.deleteDiscountProductGroupHttpResponse(
				discountProductGroup.getDiscountProductGroupId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountProductGroup() throws Exception {
		DiscountProductGroup discountProductGroup =
			discountProductGroupResource.postDiscountIdDiscountProductGroup(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountProductGroup());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountProductGroup",
						HashMapBuilder.<String, Object>put(
							"discountProductGroupId",
							discountProductGroup.getDiscountProductGroupId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountProductGroup"));
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
	protected DiscountProductGroup randomDiscountProductGroup()
		throws Exception {

		CommercePricingClass commercePricingClass =
			_commercePricingClassLocalService.addCommercePricingClass(
				_user.getUserId(), RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), _serviceContext);

		_commercePricingClasses.add(commercePricingClass);

		return new DiscountProductGroup() {
			{
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
				discountProductGroupId = RandomTestUtil.randomLong();
				productGroupExternalReferenceCode =
					commercePricingClass.getExternalReferenceCode();
				productGroupId =
					commercePricingClass.getCommercePricingClassId();
			}
		};
	}

	@Override
	protected DiscountProductGroup
			testGetDiscountByExternalReferenceCodeDiscountProductGroupsPage_addDiscountProductGroup(
				String externalReferenceCode,
				DiscountProductGroup discountProductGroup)
		throws Exception {

		return discountProductGroupResource.
			postDiscountByExternalReferenceCodeDiscountProductGroup(
				externalReferenceCode, discountProductGroup);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountProductGroupsPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountProductGroup
			testGetDiscountIdDiscountProductGroupsPage_addDiscountProductGroup(
				Long id, DiscountProductGroup discountProductGroup)
		throws Exception {

		return discountProductGroupResource.postDiscountIdDiscountProductGroup(
			id, discountProductGroup);
	}

	@Override
	protected Long testGetDiscountIdDiscountProductGroupsPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountProductGroup
			testPostDiscountByExternalReferenceCodeDiscountProductGroup_addDiscountProductGroup(
				DiscountProductGroup discountProductGroup)
		throws Exception {

		return discountProductGroupResource.
			postDiscountByExternalReferenceCodeDiscountProductGroup(
				_commerceDiscount.getExternalReferenceCode(),
				discountProductGroup);
	}

	@Override
	protected DiscountProductGroup
			testPostDiscountIdDiscountProductGroup_addDiscountProductGroup(
				DiscountProductGroup discountProductGroup)
		throws Exception {

		return discountProductGroupResource.postDiscountIdDiscountProductGroup(
			_commerceDiscount.getCommerceDiscountId(), discountProductGroup);
	}

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommercePricingClassLocalService
		_commercePricingClassLocalService;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private final List<CommercePricingClass> _commercePricingClasses =
		new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
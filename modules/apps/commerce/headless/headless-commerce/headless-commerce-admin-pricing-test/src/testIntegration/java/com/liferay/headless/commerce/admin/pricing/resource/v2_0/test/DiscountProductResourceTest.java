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
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountProduct;
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
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class DiscountProductResourceTest
	extends BaseDiscountProductResourceTestCase {

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
	public void testDeleteDiscountProduct() throws Exception {
		DiscountProduct discountProduct =
			discountProductResource.postDiscountIdDiscountProduct(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountProduct());

		assertHttpResponseStatusCode(
			204,
			discountProductResource.deleteDiscountProductHttpResponse(
				discountProduct.getDiscountProductId()));

		assertHttpResponseStatusCode(
			404,
			discountProductResource.deleteDiscountProductHttpResponse(
				discountProduct.getDiscountProductId()));

		assertHttpResponseStatusCode(
			404,
			discountProductResource.deleteDiscountProductHttpResponse(
				discountProduct.getDiscountProductId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountProduct() throws Exception {
		DiscountProduct discountProduct =
			discountProductResource.postDiscountIdDiscountProduct(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountProduct());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountProduct",
						HashMapBuilder.<String, Object>put(
							"discountProductId",
							discountProduct.getDiscountProductId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountProduct"));
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
	protected DiscountProduct randomDiscountProduct() throws Exception {
		CPInstance cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpInstances.add(cpInstance);

		CPDefinition commerceDefinition = cpInstance.getCPDefinition();

		_cpDefinitions.add(commerceDefinition);

		CProduct commerceProduct = commerceDefinition.getCProduct();

		return new DiscountProduct() {
			{
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
				discountProductId = RandomTestUtil.randomLong();
				productExternalReferenceCode =
					commerceProduct.getExternalReferenceCode();
				productId = commerceProduct.getCProductId();
			}
		};
	}

	@Override
	protected DiscountProduct
			testGetDiscountByExternalReferenceCodeDiscountProductsPage_addDiscountProduct(
				String externalReferenceCode, DiscountProduct discountProduct)
		throws Exception {

		return discountProductResource.
			postDiscountByExternalReferenceCodeDiscountProduct(
				externalReferenceCode, discountProduct);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountProductsPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountProduct
			testGetDiscountIdDiscountProductsPage_addDiscountProduct(
				Long id, DiscountProduct discountProduct)
		throws Exception {

		return discountProductResource.postDiscountIdDiscountProduct(
			id, discountProduct);
	}

	@Override
	protected Long testGetDiscountIdDiscountProductsPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountProduct
			testPostDiscountByExternalReferenceCodeDiscountProduct_addDiscountProduct(
				DiscountProduct discountProduct)
		throws Exception {

		return discountProductResource.
			postDiscountByExternalReferenceCodeDiscountProduct(
				_commerceDiscount.getExternalReferenceCode(), discountProduct);
	}

	@Override
	protected DiscountProduct
			testPostDiscountIdDiscountProduct_addDiscountProduct(
				DiscountProduct discountProduct)
		throws Exception {

		return discountProductResource.postDiscountIdDiscountProduct(
			_commerceDiscount.getCommerceDiscountId(), discountProduct);
	}

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private final List<CPDefinition> _cpDefinitions = new ArrayList<>();

	@DeleteAfterTestRun
	private final List<CPInstance> _cpInstances = new ArrayList<>();

	@DeleteAfterTestRun
	private User _user;

}
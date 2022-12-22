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
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountSku;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class DiscountSkuResourceTest extends BaseDiscountSkuResourceTestCase {

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
	public void testDeleteDiscountSku() throws Exception {
		DiscountSku discountSku = discountSkuResource.postDiscountIdDiscountSku(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountSku());

		assertHttpResponseStatusCode(
			204,
			discountSkuResource.deleteDiscountSkuHttpResponse(
				discountSku.getDiscountSkuId()));

		assertHttpResponseStatusCode(
			404,
			discountSkuResource.deleteDiscountSkuHttpResponse(
				discountSku.getDiscountSkuId()));

		assertHttpResponseStatusCode(
			404,
			discountSkuResource.deleteDiscountSkuHttpResponse(
				discountSku.getDiscountSkuId()));
	}

	@Ignore
	@Override
	@Test
	public void testGetDiscountByExternalReferenceCodeDiscountSkusPage()
		throws Exception {

		super.testGetDiscountByExternalReferenceCodeDiscountSkusPage();
	}

	@Ignore
	@Override
	@Test
	public void testGetDiscountByExternalReferenceCodeDiscountSkusPageWithPagination()
		throws Exception {

		super.
			testGetDiscountByExternalReferenceCodeDiscountSkusPageWithPagination();
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountSku() throws Exception {
		DiscountSku discountSku = discountSkuResource.postDiscountIdDiscountSku(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountSku());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountSku",
						HashMapBuilder.<String, Object>put(
							"discountSkuId", discountSku.getDiscountSkuId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountSku"));
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
	protected DiscountSku randomDiscountSku() throws Exception {
		CPInstance cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpInstances.add(cpInstance);

		CPDefinition commerceDefinition = cpInstance.getCPDefinition();

		_cpDefinitions.add(commerceDefinition);

		CProduct commerceProduct = commerceDefinition.getCProduct();

		return new DiscountSku() {
			{
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
				discountSkuId = RandomTestUtil.randomLong();
				productId = commerceProduct.getCProductId();
				skuExternalReferenceCode =
					commerceProduct.getExternalReferenceCode();
				skuId = cpInstance.getCPInstanceId();
			}
		};
	}

	@Override
	protected DiscountSku
			testGetDiscountByExternalReferenceCodeDiscountSkusPage_addDiscountSku(
				String externalReferenceCode, DiscountSku discountSku)
		throws Exception {

		return discountSkuResource.
			postDiscountByExternalReferenceCodeDiscountSku(
				externalReferenceCode, discountSku);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountSkusPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountSku testGetDiscountIdDiscountSkusPage_addDiscountSku(
			Long id, DiscountSku discountSku)
		throws Exception {

		return discountSkuResource.postDiscountIdDiscountSku(id, discountSku);
	}

	@Override
	protected Long testGetDiscountIdDiscountSkusPage_getId() throws Exception {
		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountSku
			testPostDiscountByExternalReferenceCodeDiscountSku_addDiscountSku(
				DiscountSku discountSku)
		throws Exception {

		return discountSkuResource.
			postDiscountByExternalReferenceCodeDiscountSku(
				_commerceDiscount.getExternalReferenceCode(), discountSku);
	}

	@Override
	protected DiscountSku testPostDiscountIdDiscountSku_addDiscountSku(
			DiscountSku discountSku)
		throws Exception {

		return discountSkuResource.postDiscountIdDiscountSku(
			_commerceDiscount.getCommerceDiscountId(), discountSku);
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
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
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountCategory;
import com.liferay.petra.string.StringPool;
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
public class DiscountCategoryResourceTest
	extends BaseDiscountCategoryResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_assetVocabulary = _assetVocabularyLocalService.addVocabulary(
			_user.getUserId(), testGroup.getGroupId(),
			RandomTestUtil.randomString(), _serviceContext);

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
	public void testDeleteDiscountCategory() throws Exception {
		DiscountCategory discountCategory =
			discountCategoryResource.postDiscountIdDiscountCategory(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountCategory());

		assertHttpResponseStatusCode(
			204,
			discountCategoryResource.deleteDiscountCategoryHttpResponse(
				discountCategory.getDiscountCategoryId()));

		assertHttpResponseStatusCode(
			404,
			discountCategoryResource.deleteDiscountCategoryHttpResponse(
				discountCategory.getDiscountCategoryId()));

		assertHttpResponseStatusCode(
			404,
			discountCategoryResource.deleteDiscountCategoryHttpResponse(
				discountCategory.getDiscountCategoryId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteDiscountCategory() throws Exception {
		DiscountCategory discountCategory =
			discountCategoryResource.postDiscountIdDiscountCategory(
				_commerceDiscount.getCommerceDiscountId(),
				randomDiscountCategory());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteDiscountCategory",
						HashMapBuilder.<String, Object>put(
							"discountCategoryId",
							discountCategory.getDiscountCategoryId()
						).build())),
				"JSONObject/data", "Object/deleteDiscountCategory"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"categoryId", "discountExternalReferenceCode", "discountId"
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
	protected DiscountCategory randomDiscountCategory() throws Exception {
		AssetCategory assetCategory = _assetCategoryLocalService.addCategory(
			_user.getUserId(), testGroup.getGroupId(),
			RandomTestUtil.randomString(), _assetVocabulary.getVocabularyId(),
			_serviceContext);

		_assetCategories.add(assetCategory);

		return new DiscountCategory() {
			{
				categoryExternalReferenceCode = StringPool.BLANK;
				categoryId = assetCategory.getCategoryId();
				discountCategoryId = RandomTestUtil.randomLong();
				discountExternalReferenceCode =
					_commerceDiscount.getExternalReferenceCode();
				discountId = _commerceDiscount.getCommerceDiscountId();
			}
		};
	}

	@Override
	protected DiscountCategory
			testGetDiscountByExternalReferenceCodeDiscountCategoriesPage_addDiscountCategory(
				String externalReferenceCode, DiscountCategory discountCategory)
		throws Exception {

		return discountCategoryResource.
			postDiscountByExternalReferenceCodeDiscountCategory(
				externalReferenceCode, discountCategory);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountCategoriesPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountCategory
			testGetDiscountIdDiscountCategoriesPage_addDiscountCategory(
				Long id, DiscountCategory discountCategory)
		throws Exception {

		return discountCategoryResource.postDiscountIdDiscountCategory(
			id, discountCategory);
	}

	@Override
	protected Long testGetDiscountIdDiscountCategoriesPage_getId()
		throws Exception {

		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountCategory
			testPostDiscountByExternalReferenceCodeDiscountCategory_addDiscountCategory(
				DiscountCategory discountCategory)
		throws Exception {

		return discountCategoryResource.
			postDiscountByExternalReferenceCodeDiscountCategory(
				_commerceDiscount.getExternalReferenceCode(), discountCategory);
	}

	@Override
	protected DiscountCategory
			testPostDiscountIdDiscountCategory_addDiscountCategory(
				DiscountCategory discountCategory)
		throws Exception {

		return discountCategoryResource.postDiscountIdDiscountCategory(
			_commerceDiscount.getCommerceDiscountId(), discountCategory);
	}

	@Inject
	private static AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private static AssetVocabularyLocalService _assetVocabularyLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@DeleteAfterTestRun
	private final List<AssetCategory> _assetCategories = new ArrayList<>();

	@DeleteAfterTestRun
	private AssetVocabulary _assetVocabulary;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
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
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.pricing.constants.CommercePriceModifierConstants;
import com.liferay.commerce.pricing.model.CommercePriceModifier;
import com.liferay.commerce.pricing.service.CommercePriceModifierLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceModifierCategory;
import com.liferay.petra.string.StringPool;
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
public class PriceModifierCategoryResourceTest
	extends BasePriceModifierCategoryResourceTestCase {

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

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, 0, true);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);

		_commercePriceModifier =
			_commercePriceModifierLocalService.addCommercePriceModifier(
				testGroup.getGroupId(), RandomTestUtil.randomString(),
				_commercePriceList.getCommercePriceListId(),
				CommercePriceModifierConstants.MODIFIER_TYPE_FIXED_AMOUNT,
				BigDecimal.TEN, RandomTestUtil.randomDouble(), true, 1, 1, 2022,
				12, 0, 0, 0, 0, 0, 0, true, _serviceContext);
	}

	@Override
	@Test
	public void testDeletePriceModifierCategory() throws Exception {
		PriceModifierCategory priceModifierCategory =
			priceModifierCategoryResource.
				postPriceModifierIdPriceModifierCategory(
					_commercePriceModifier.getCommercePriceModifierId(),
					randomPriceModifierCategory());

		assertHttpResponseStatusCode(
			204,
			priceModifierCategoryResource.
				deletePriceModifierCategoryHttpResponse(
					priceModifierCategory.getPriceModifierCategoryId()));

		assertHttpResponseStatusCode(
			404,
			priceModifierCategoryResource.
				deletePriceModifierCategoryHttpResponse(
					priceModifierCategory.getPriceModifierCategoryId()));

		assertHttpResponseStatusCode(
			404,
			priceModifierCategoryResource.
				deletePriceModifierCategoryHttpResponse(
					priceModifierCategory.getPriceModifierCategoryId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceModifierCategory() throws Exception {
		PriceModifierCategory priceModifierCategory =
			priceModifierCategoryResource.
				postPriceModifierIdPriceModifierCategory(
					_commercePriceModifier.getCommercePriceModifierId(),
					randomPriceModifierCategory());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceModifierCategory",
						HashMapBuilder.<String, Object>put(
							"priceModifierCategoryId",
							priceModifierCategory.getPriceModifierCategoryId()
						).build())),
				"JSONObject/data", "Object/deletePriceModifierCategory"));
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
	protected PriceModifierCategory randomPriceModifierCategory()
		throws Exception {

		AssetCategory assetCategory = _assetCategoryLocalService.addCategory(
			_user.getUserId(), testGroup.getGroupId(),
			RandomTestUtil.randomString(), _assetVocabulary.getVocabularyId(),
			_serviceContext);

		_assetCategories.add(assetCategory);

		return new PriceModifierCategory() {
			{
				categoryExternalReferenceCode = StringPool.BLANK;
				categoryId = assetCategory.getCategoryId();
				priceModifierCategoryId = RandomTestUtil.randomLong();
				priceModifierExternalReferenceCode =
					_commercePriceModifier.getExternalReferenceCode();
				priceModifierId =
					_commercePriceModifier.getCommercePriceModifierId();
			}
		};
	}

	@Override
	protected PriceModifierCategory
			testGetPriceModifierByExternalReferenceCodePriceModifierCategoriesPage_addPriceModifierCategory(
				String externalReferenceCode,
				PriceModifierCategory priceModifierCategory)
		throws Exception {

		return priceModifierCategoryResource.
			postPriceModifierByExternalReferenceCodePriceModifierCategory(
				externalReferenceCode, priceModifierCategory);
	}

	@Override
	protected String
			testGetPriceModifierByExternalReferenceCodePriceModifierCategoriesPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceModifier.getExternalReferenceCode();
	}

	@Override
	protected PriceModifierCategory
			testGetPriceModifierIdPriceModifierCategoriesPage_addPriceModifierCategory(
				Long id, PriceModifierCategory priceModifierCategory)
		throws Exception {

		return priceModifierCategoryResource.
			postPriceModifierIdPriceModifierCategory(id, priceModifierCategory);
	}

	@Override
	protected Long testGetPriceModifierIdPriceModifierCategoriesPage_getId()
		throws Exception {

		return _commercePriceModifier.getCommercePriceModifierId();
	}

	@Override
	protected PriceModifierCategory
			testPostPriceModifierByExternalReferenceCodePriceModifierCategory_addPriceModifierCategory(
				PriceModifierCategory priceModifierCategory)
		throws Exception {

		return priceModifierCategoryResource.
			postPriceModifierByExternalReferenceCodePriceModifierCategory(
				_commercePriceModifier.getExternalReferenceCode(),
				priceModifierCategory);
	}

	@Override
	protected PriceModifierCategory
			testPostPriceModifierIdPriceModifierCategory_addPriceModifierCategory(
				PriceModifierCategory priceModifierCategory)
		throws Exception {

		return priceModifierCategoryResource.
			postPriceModifierIdPriceModifierCategory(
				_commercePriceModifier.getCommercePriceModifierId(),
				priceModifierCategory);
	}

	@Inject
	private static AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private static AssetVocabularyLocalService _assetVocabularyLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@Inject
	private static CommercePriceModifierLocalService
		_commercePriceModifierLocalService;

	@DeleteAfterTestRun
	private final List<AssetCategory> _assetCategories = new ArrayList<>();

	@DeleteAfterTestRun
	private AssetVocabulary _assetVocabulary;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceModifier _commercePriceModifier;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
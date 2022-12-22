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
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountRel;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountRelLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.pricing.constants.CommercePriceModifierConstants;
import com.liferay.commerce.pricing.model.CommercePriceModifier;
import com.liferay.commerce.pricing.model.CommercePriceModifierRel;
import com.liferay.commerce.pricing.service.CommercePriceModifierLocalService;
import com.liferay.commerce.pricing.service.CommercePriceModifierRelLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.Category;
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
public class CategoryResourceTest extends BaseCategoryResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId());

		_assetVocabulary = _assetVocabularyLocalService.addVocabulary(
			_user.getUserId(), testGroup.getGroupId(),
			RandomTestUtil.randomString(), serviceContext);

		_assetCategory = _assetCategoryLocalService.addCategory(
			_user.getUserId(), testGroup.getGroupId(),
			RandomTestUtil.randomString(), _assetVocabulary.getVocabularyId(),
			serviceContext);

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

		_commerceDiscountRel =
			_commerceDiscountRelLocalService.addCommerceDiscountRel(
				_commerceDiscount.getCommerceDiscountId(),
				Category.class.getName(), _assetCategory.getCategoryId(),
				serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commercePriceModifier =
			_commercePriceModifierLocalService.addCommercePriceModifier(
				testGroup.getGroupId(), RandomTestUtil.randomString(),
				_commercePriceList.getCommercePriceListId(),
				CommercePriceModifierConstants.MODIFIER_TYPE_FIXED_AMOUNT,
				BigDecimal.TEN, RandomTestUtil.nextDouble(),
				RandomTestUtil.randomBoolean(), 1, 1, 2022, 12, 0, 0, 0, 0, 0,
				0, true, serviceContext);

		_commercePriceModifierRel =
			_commercePriceModifierRelLocalService.addCommercePriceModifierRel(
				_commercePriceModifier.getCommercePriceModifierId(),
				AssetCategory.class.getName(), _assetCategory.getCategoryId(),
				serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name", "path"};
	}

	@Override
	protected Category randomCategory() throws Exception {
		return new Category() {
			{
				id = _assetCategory.getCategoryId();
				name = _assetCategory.getName();
				path = _assetCategory.getPath(LocaleUtil.getSiteDefault());
				vocabulary = _assetVocabulary.getName();
			}
		};
	}

	@Override
	protected Category testGetDiscountCategoryCategory_addCategory()
		throws Exception {

		return randomCategory();
	}

	@Override
	protected Long testGetDiscountCategoryCategory_getDiscountCategoryId()
		throws Exception {

		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Category testGetPriceModifierCategoryCategory_addCategory()
		throws Exception {

		return randomCategory();
	}

	@Override
	protected Long
			testGetPriceModifierCategoryCategory_getPriceModifierCategoryId()
		throws Exception {

		return _commercePriceModifierRel.getCommercePriceModifierRelId();
	}

	@Override
	protected Category testGraphQLCategory_addCategory() throws Exception {
		return randomCategory();
	}

	@Override
	protected Long
			testGraphQLGetDiscountCategoryCategory_getDiscountCategoryId()
		throws Exception {

		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Long
			testGraphQLGetPriceModifierCategoryCategory_getPriceModifierCategoryId()
		throws Exception {

		return _commercePriceModifierRel.getCommercePriceModifierRelId();
	}

	@Inject
	private static AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private static AssetVocabularyLocalService _assetVocabularyLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommerceDiscountRelLocalService
		_commerceDiscountRelLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@Inject
	private static CommercePriceModifierLocalService
		_commercePriceModifierLocalService;

	@Inject
	private static CommercePriceModifierRelLocalService
		_commercePriceModifierRelLocalService;

	@DeleteAfterTestRun
	private AssetCategory _assetCategory;

	@DeleteAfterTestRun
	private AssetVocabulary _assetVocabulary;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private CommerceDiscountRel _commerceDiscountRel;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceModifier _commercePriceModifier;

	@DeleteAfterTestRun
	private CommercePriceModifierRel _commercePriceModifierRel;

	@DeleteAfterTestRun
	private User _user;

}
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
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountRel;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountRelLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceEntry;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceEntryLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.Sku;
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
public class SkuResourceTest extends BaseSkuResourceTestCase {

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

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
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
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpDefinition = _cpInstance.getCPDefinition();

		_commerceDiscountRel =
			_commerceDiscountRelLocalService.addCommerceDiscountRel(
				_commerceDiscount.getCommerceDiscountId(),
				CPInstance.class.getName(), _cpInstance.getCPInstanceId(),
				serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				_cpDefinition.getCProductId(), _cpInstance.getCPInstanceUuid(),
				_commercePriceList.getCommercePriceListId(), BigDecimal.TEN,
				BigDecimal.ONE, serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"basePrice", "basePromoPrice", "name"};
	}

	@Override
	protected Sku randomSku() throws Exception {
		CommercePriceEntry commerceBasePriceListPriceEntry =
			_commercePriceEntryLocalService.getInstanceBaseCommercePriceEntry(
				_cpInstance.getCPInstanceUuid(),
				CommercePriceListConstants.TYPE_PRICE_LIST);

		CommercePriceEntry commerceBasePromotionPriceEntry =
			_commercePriceEntryLocalService.getInstanceBaseCommercePriceEntry(
				_cpInstance.getCPInstanceUuid(),
				CommercePriceListConstants.TYPE_PROMOTION);

		return new Sku() {
			{
				id = _cpInstance.getCPInstanceId();
				name = _cpInstance.getSku();

				setBasePrice(
					() -> {
						if (commerceBasePriceListPriceEntry == null) {
							return 0D;
						}

						BigDecimal price =
							commerceBasePriceListPriceEntry.getPrice();

						return price.doubleValue();
					});

				setBasePromoPrice(
					() -> {
						if (commerceBasePromotionPriceEntry == null) {
							return 0D;
						}

						BigDecimal price =
							commerceBasePromotionPriceEntry.getPromoPrice();

						return price.doubleValue();
					});
			}
		};
	}

	@Override
	protected Sku testGetDiscountSkuSku_addSku() throws Exception {
		return randomSku();
	}

	@Override
	protected Long testGetDiscountSkuSku_getDiscountSkuId() throws Exception {
		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Sku testGetPriceEntryIdSku_addSku() throws Exception {
		return randomSku();
	}

	@Override
	protected Long testGetPriceEntryIdSku_getPriceEntryId() throws Exception {
		return _commercePriceEntry.getCommercePriceEntryId();
	}

	@Override
	protected Long testGraphQLGetDiscountSkuSku_getDiscountSkuId()
		throws Exception {

		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Long testGraphQLGetPriceEntryIdSku_getPriceEntryId()
		throws Exception {

		return _commercePriceEntry.getCommercePriceEntryId();
	}

	@Override
	protected Sku testGraphQLSku_addSku() throws Exception {
		return randomSku();
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommerceDiscountRelLocalService
		_commerceDiscountRelLocalService;

	@Inject
	private static CommercePriceEntryLocalService
		_commercePriceEntryLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private CommerceDiscountRel _commerceDiscountRel;

	@DeleteAfterTestRun
	private CommercePriceEntry _commercePriceEntry;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	@DeleteAfterTestRun
	private User _user;

}
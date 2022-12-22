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
import com.liferay.commerce.pricing.constants.CommercePriceModifierConstants;
import com.liferay.commerce.pricing.model.CommercePriceModifier;
import com.liferay.commerce.pricing.model.CommercePriceModifierRel;
import com.liferay.commerce.pricing.service.CommercePriceModifierLocalService;
import com.liferay.commerce.pricing.service.CommercePriceModifierRelLocalService;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.Product;
import com.liferay.headless.commerce.core.util.LanguageUtils;
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
public class ProductResourceTest extends BaseProductResourceTestCase {

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

		_cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpDefinition = _cpInstance.getCPDefinition();

		_commerceDiscountRel =
			_commerceDiscountRelLocalService.addCommerceDiscountRel(
				_commerceDiscount.getCommerceDiscountId(),
				CProduct.class.getName(), _cpDefinition.getCPDefinitionId(),
				_serviceContext);

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);

		_commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				_cpDefinition.getCProductId(), _cpInstance.getCPInstanceUuid(),
				_commercePriceList.getCommercePriceListId(), BigDecimal.TEN,
				BigDecimal.ONE, _serviceContext);

		_commercePriceModifier =
			_commercePriceModifierLocalService.addCommercePriceModifier(
				testGroup.getGroupId(), RandomTestUtil.randomString(),
				_commercePriceList.getCommercePriceListId(),
				CommercePriceModifierConstants.MODIFIER_TYPE_FIXED_AMOUNT,
				BigDecimal.TEN, RandomTestUtil.randomDouble(), true, 1, 1, 2022,
				12, 0, 0, 0, 0, 0, 0, true, _serviceContext);

		_commercePriceModifierRel =
			_commercePriceModifierRelLocalService.addCommercePriceModifierRel(
				_commercePriceModifier.getCommercePriceModifierId(),
				CProduct.class.getName(), _cpDefinition.getCPDefinitionId(),
				_serviceContext);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name", "sku"};
	}

	@Override
	protected Product randomProduct() throws Exception {
		return new Product() {
			{
				id = RandomTestUtil.randomLong();
				name = LanguageUtils.getLanguageIdMap(
					_cpDefinition.getNameMap());
				sku = _cpInstance.getSku();
				thumbnail = RandomTestUtil.randomString();
			}
		};
	}

	@Override
	protected Product testGetDiscountProductProduct_addProduct()
		throws Exception {

		return randomProduct();
	}

	@Override
	protected Long testGetDiscountProductProduct_getDiscountProductId()
		throws Exception {

		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Product testGetPriceEntryIdProduct_addProduct() throws Exception {
		return randomProduct();
	}

	@Override
	protected Long testGetPriceEntryIdProduct_getPriceEntryId()
		throws Exception {

		return _commercePriceEntry.getCommercePriceEntryId();
	}

	@Override
	protected Product testGetPriceModifierProductProduct_addProduct()
		throws Exception {

		return randomProduct();
	}

	@Override
	protected Long
			testGetPriceModifierProductProduct_getPriceModifierProductId()
		throws Exception {

		return _commercePriceModifierRel.getCommercePriceModifierRelId();
	}

	@Override
	protected Long testGraphQLGetDiscountProductProduct_getDiscountProductId()
		throws Exception {

		return _commerceDiscountRel.getCommerceDiscountRelId();
	}

	@Override
	protected Long testGraphQLGetPriceEntryIdProduct_getPriceEntryId()
		throws Exception {

		return _commercePriceEntry.getCommercePriceEntryId();
	}

	@Override
	protected Long
			testGraphQLGetPriceModifierProductProduct_getPriceModifierProductId()
		throws Exception {

		return _commercePriceModifierRel.getCommercePriceModifierRelId();
	}

	@Override
	protected Product testGraphQLProduct_addProduct() throws Exception {
		return randomProduct();
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

	@Inject
	private static CommercePriceModifierLocalService
		_commercePriceModifierLocalService;

	@Inject
	private static CommercePriceModifierRelLocalService
		_commercePriceModifierRelLocalService;

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
	private CommercePriceModifier _commercePriceModifier;

	@DeleteAfterTestRun
	private CommercePriceModifierRel _commercePriceModifierRel;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
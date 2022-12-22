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
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceListDiscount;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class PriceListDiscountResourceTest
	extends BasePriceListDiscountResourceTestCase {

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

		_commercePriceList =
			_commercePriceListLocalService.addCommercePriceList(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				_user.getUserId(), _commerceCurrency.getCommerceCurrencyId(),
				RandomTestUtil.randomBoolean(),
				CommercePriceListConstants.TYPE_PRICE_LIST, 0, false,
				RandomTestUtil.randomString(), RandomTestUtil.randomDouble(), 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);
	}

	@Override
	@Test
	public void testDeletePriceListDiscount() throws Exception {
		PriceListDiscount priceListDiscount =
			priceListDiscountResource.postPriceListIdPriceListDiscount(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListDiscount());

		assertHttpResponseStatusCode(
			204,
			priceListDiscountResource.deletePriceListDiscountHttpResponse(
				priceListDiscount.getPriceListDiscountId()));

		assertHttpResponseStatusCode(
			404,
			priceListDiscountResource.deletePriceListDiscountHttpResponse(
				priceListDiscount.getPriceListDiscountId()));

		assertHttpResponseStatusCode(
			404,
			priceListDiscountResource.deletePriceListDiscountHttpResponse(
				priceListDiscount.getPriceListDiscountId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceListDiscount() throws Exception {
		PriceListDiscount priceListDiscount =
			priceListDiscountResource.postPriceListIdPriceListDiscount(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListDiscount());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceListDiscount",
						HashMapBuilder.<String, Object>put(
							"priceListDiscountId",
							priceListDiscount.getPriceListDiscountId()
						).build())),
				"JSONObject/data", "Object/deletePriceListDiscount"));
	}

	@Override
	protected PriceListDiscount randomPriceListDiscount() throws Exception {
		CommerceDiscount commerceDiscount =
			_commerceDiscountLocalService.addOrUpdateCommerceDiscount(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(),
				CommerceDiscountConstants.TARGET_PRODUCTS, false, null, false,
				BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO,
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED, 0, true, 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, _serviceContext);

		_commerceDiscounts.add(commerceDiscount);

		return new PriceListDiscount() {
			{
				discountExternalReferenceCode =
					commerceDiscount.getExternalReferenceCode();
				discountId = commerceDiscount.getCommerceDiscountId();
				discountName = RandomTestUtil.randomString();
				order = RandomTestUtil.randomInt();
				priceListDiscountId = RandomTestUtil.randomLong();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
			}
		};
	}

	@Override
	protected PriceListDiscount
			testGetPriceListByExternalReferenceCodePriceListDiscountsPage_addPriceListDiscount(
				String externalReferenceCode,
				PriceListDiscount priceListDiscount)
		throws Exception {

		return priceListDiscountResource.
			postPriceListByExternalReferenceCodePriceListDiscount(
				externalReferenceCode, priceListDiscount);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceListDiscountsPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceListDiscount
			testGetPriceListIdPriceListDiscountsPage_addPriceListDiscount(
				Long id, PriceListDiscount priceListDiscount)
		throws Exception {

		return priceListDiscountResource.postPriceListIdPriceListDiscount(
			id, priceListDiscount);
	}

	@Override
	protected Long testGetPriceListIdPriceListDiscountsPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceListDiscount
			testPostPriceListByExternalReferenceCodePriceListDiscount_addPriceListDiscount(
				PriceListDiscount priceListDiscount)
		throws Exception {

		return priceListDiscountResource.
			postPriceListByExternalReferenceCodePriceListDiscount(
				_commercePriceList.getExternalReferenceCode(),
				priceListDiscount);
	}

	@Override
	protected PriceListDiscount
			testPostPriceListIdPriceListDiscount_addPriceListDiscount(
				PriceListDiscount priceListDiscount)
		throws Exception {

		return priceListDiscountResource.postPriceListIdPriceListDiscount(
			_commercePriceList.getCommercePriceListId(), priceListDiscount);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private final List<CommerceDiscount> _commerceDiscounts = new ArrayList<>();

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
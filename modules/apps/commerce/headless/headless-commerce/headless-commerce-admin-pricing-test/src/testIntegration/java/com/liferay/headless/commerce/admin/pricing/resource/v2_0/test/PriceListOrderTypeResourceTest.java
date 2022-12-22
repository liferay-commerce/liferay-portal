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
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.service.CommerceOrderTypeLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceListOrderType;
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
public class PriceListOrderTypeResourceTest
	extends BasePriceListOrderTypeResourceTestCase {

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
	public void testDeletePriceListOrderType() throws Exception {
		PriceListOrderType priceListOrderType =
			priceListOrderTypeResource.postPriceListIdPriceListOrderType(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListOrderType());

		assertHttpResponseStatusCode(
			204,
			priceListOrderTypeResource.deletePriceListOrderTypeHttpResponse(
				priceListOrderType.getPriceListOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			priceListOrderTypeResource.deletePriceListOrderTypeHttpResponse(
				priceListOrderType.getPriceListOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			priceListOrderTypeResource.deletePriceListOrderTypeHttpResponse(
				priceListOrderType.getPriceListOrderTypeId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceListOrderType() throws Exception {
		PriceListOrderType priceListOrderType =
			priceListOrderTypeResource.postPriceListIdPriceListOrderType(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListOrderType());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceListOrderType",
						HashMapBuilder.<String, Object>put(
							"priceListOrderTypeId",
							priceListOrderType.getPriceListOrderTypeId()
						).build())),
				"JSONObject/data", "Object/deletePriceListOrderType"));
	}

	@Override
	protected PriceListOrderType randomPriceListOrderType() throws Exception {
		CommerceOrderType commerceOrderType =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true, 1, 1, 2022, 12, 0,
				RandomTestUtil.nextInt(), 0, 0, 0, 0, 0, true, _serviceContext);

		_commerceOrderTypes.add(commerceOrderType);

		return new PriceListOrderType() {
			{
				orderTypeExternalReferenceCode =
					commerceOrderType.getExternalReferenceCode();
				orderTypeId = commerceOrderType.getCommerceOrderTypeId();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
				priceListOrderTypeId = RandomTestUtil.randomLong();
				priority = RandomTestUtil.randomInt();
			}
		};
	}

	@Override
	protected PriceListOrderType
			testGetPriceListByExternalReferenceCodePriceListOrderTypesPage_addPriceListOrderType(
				String externalReferenceCode,
				PriceListOrderType priceListOrderType)
		throws Exception {

		return priceListOrderTypeResource.
			postPriceListByExternalReferenceCodePriceListOrderType(
				externalReferenceCode, priceListOrderType);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceListOrderTypesPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceListOrderType
			testGetPriceListIdPriceListOrderTypesPage_addPriceListOrderType(
				Long id, PriceListOrderType priceListOrderType)
		throws Exception {

		return priceListOrderTypeResource.postPriceListIdPriceListOrderType(
			id, priceListOrderType);
	}

	@Override
	protected Long testGetPriceListIdPriceListOrderTypesPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceListOrderType
			testPostPriceListByExternalReferenceCodePriceListOrderType_addPriceListOrderType(
				PriceListOrderType priceListOrderType)
		throws Exception {

		return priceListOrderTypeResource.
			postPriceListByExternalReferenceCodePriceListOrderType(
				_commercePriceList.getExternalReferenceCode(),
				priceListOrderType);
	}

	@Override
	protected PriceListOrderType
			testPostPriceListIdPriceListOrderType_addPriceListOrderType(
				PriceListOrderType priceListOrderType)
		throws Exception {

		return priceListOrderTypeResource.postPriceListIdPriceListOrderType(
			_commercePriceList.getCommercePriceListId(), priceListOrderType);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceOrderTypeLocalService _commerceOrderTypeLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private final List<CommerceOrderType> _commerceOrderTypes =
		new ArrayList<>();

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
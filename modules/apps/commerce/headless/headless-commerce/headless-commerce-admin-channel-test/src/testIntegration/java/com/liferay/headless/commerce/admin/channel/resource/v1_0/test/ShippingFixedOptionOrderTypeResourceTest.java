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

package com.liferay.headless.commerce.admin.channel.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceOrderTypeLocalService;
import com.liferay.commerce.service.CommerceShippingMethodLocalService;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionLocalService;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.ShippingFixedOptionOrderType;
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
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class ShippingFixedOptionOrderTypeResourceTest
	extends BaseShippingFixedOptionOrderTypeResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, RandomTestUtil.nextDouble(), true);

		_commerceChannel = _commerceChannelLocalService.addCommerceChannel(
			RandomTestUtil.randomString(), testGroup.getGroupId(),
			RandomTestUtil.randomString(),
			CommerceChannelConstants.CHANNEL_TYPE_SITE, null,
			_commerceCurrency.getCode(), _serviceContext);

		_commerceShippingMethod =
			_commerceShippingMethodLocalService.addCommerceShippingMethod(
				_user.getUserId(), _commerceChannel.getGroupId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true,
				RandomTestUtil.randomString(), null,
				RandomTestUtil.nextDouble(), null);

		_commerceShippingFixedOption =
			_commerceShippingFixedOptionLocalService.
				addCommerceShippingFixedOption(
					_user.getUserId(), _commerceChannel.getGroupId(),
					_commerceShippingMethod.getCommerceShippingMethodId(),
					BigDecimal.TEN, RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.nextDouble());
	}

	@Override
	@Test
	public void testDeleteShippingFixedOptionOrderType() throws Exception {
		ShippingFixedOptionOrderType shippingFixedOptionOrderType =
			shippingFixedOptionOrderTypeResource.
				postShippingFixedOptionIdShippingFixedOptionOrderType(
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId(),
					randomShippingFixedOptionOrderType());

		assertHttpResponseStatusCode(
			204,
			shippingFixedOptionOrderTypeResource.
				deleteShippingFixedOptionOrderTypeHttpResponse(
					shippingFixedOptionOrderType.
						getShippingFixedOptionOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			shippingFixedOptionOrderTypeResource.
				deleteShippingFixedOptionOrderTypeHttpResponse(
					shippingFixedOptionOrderType.
						getShippingFixedOptionOrderTypeId()));

		assertHttpResponseStatusCode(
			404,
			shippingFixedOptionOrderTypeResource.
				deleteShippingFixedOptionOrderTypeHttpResponse(
					shippingFixedOptionOrderType.
						getShippingFixedOptionOrderTypeId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteShippingFixedOptionOrderType()
		throws Exception {

		ShippingFixedOptionOrderType shippingFixedOptionOrderType =
			shippingFixedOptionOrderTypeResource.
				postShippingFixedOptionIdShippingFixedOptionOrderType(
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId(),
					randomShippingFixedOptionOrderType());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteShippingFixedOptionOrderType",
						HashMapBuilder.<String, Object>put(
							"shippingFixedOptionOrderTypeId",
							shippingFixedOptionOrderType.
								getShippingFixedOptionOrderTypeId()
						).build())),
				"JSONObject/data",
				"Object/deleteShippingFixedOptionOrderType"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"orderTypeExternalReferenceCode", "orderTypeId",
			"shippingFixedOptionId"
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
	protected ShippingFixedOptionOrderType randomShippingFixedOptionOrderType()
		throws Exception {

		CommerceOrderType commerceOrderType =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true, 1, 1, 2022, 12, 0,
				RandomTestUtil.nextInt(), 0, 0, 0, 0, 0, true, _serviceContext);

		_commerceOrderTypes.add(commerceOrderType);

		return new ShippingFixedOptionOrderType() {
			{
				orderTypeExternalReferenceCode =
					commerceOrderType.getExternalReferenceCode();
				orderTypeId = commerceOrderType.getCommerceOrderTypeId();
				priority = RandomTestUtil.randomInt();
				shippingFixedOptionId =
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId();
				shippingFixedOptionOrderTypeId = RandomTestUtil.randomLong();
			}
		};
	}

	@Override
	protected ShippingFixedOptionOrderType
			testGetShippingFixedOptionIdShippingFixedOptionOrderTypesPage_addShippingFixedOptionOrderType(
				Long id,
				ShippingFixedOptionOrderType shippingFixedOptionOrderType)
		throws Exception {

		return shippingFixedOptionOrderTypeResource.
			postShippingFixedOptionIdShippingFixedOptionOrderType(
				id, shippingFixedOptionOrderType);
	}

	@Override
	protected Long
			testGetShippingFixedOptionIdShippingFixedOptionOrderTypesPage_getId()
		throws Exception {

		return _commerceShippingFixedOption.getCommerceShippingFixedOptionId();
	}

	@Override
	protected ShippingFixedOptionOrderType
			testPostShippingFixedOptionIdShippingFixedOptionOrderType_addShippingFixedOptionOrderType(
				ShippingFixedOptionOrderType shippingFixedOptionOrderType)
		throws Exception {

		return shippingFixedOptionOrderTypeResource.
			postShippingFixedOptionIdShippingFixedOptionOrderType(
				_commerceShippingFixedOption.getCommerceShippingFixedOptionId(),
				shippingFixedOptionOrderType);
	}

	@Inject
	private static CommerceChannelLocalService _commerceChannelLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceOrderTypeLocalService _commerceOrderTypeLocalService;

	@Inject
	private static CommerceShippingFixedOptionLocalService
		_commerceShippingFixedOptionLocalService;

	@Inject
	private static CommerceShippingMethodLocalService
		_commerceShippingMethodLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private final List<CommerceOrderType> _commerceOrderTypes =
		new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceShippingFixedOption _commerceShippingFixedOption;

	@DeleteAfterTestRun
	private CommerceShippingMethod _commerceShippingMethod;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
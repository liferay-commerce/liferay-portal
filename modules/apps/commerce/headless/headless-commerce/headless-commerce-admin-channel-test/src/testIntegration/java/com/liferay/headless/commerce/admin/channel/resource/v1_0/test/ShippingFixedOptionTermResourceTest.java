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
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceShippingMethodLocalService;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionLocalService;
import com.liferay.commerce.term.constants.CommerceTermEntryConstants;
import com.liferay.commerce.term.model.CommerceTermEntry;
import com.liferay.commerce.term.service.CommerceTermEntryLocalService;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.ShippingFixedOptionTerm;
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
public class ShippingFixedOptionTermResourceTest
	extends BaseShippingFixedOptionTermResourceTestCase {

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
	public void testDeleteShippingFixedOptionTerm() throws Exception {
		ShippingFixedOptionTerm shippingFixedOptionTerm =
			shippingFixedOptionTermResource.
				postShippingFixedOptionIdShippingFixedOptionTerm(
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId(),
					randomShippingFixedOptionTerm());

		assertHttpResponseStatusCode(
			204,
			shippingFixedOptionTermResource.
				deleteShippingFixedOptionTermHttpResponse(
					shippingFixedOptionTerm.getShippingFixedOptionTermId()));

		assertHttpResponseStatusCode(
			404,
			shippingFixedOptionTermResource.
				deleteShippingFixedOptionTermHttpResponse(
					shippingFixedOptionTerm.getShippingFixedOptionTermId()));

		assertHttpResponseStatusCode(
			404,
			shippingFixedOptionTermResource.
				deleteShippingFixedOptionTermHttpResponse(
					shippingFixedOptionTerm.getShippingFixedOptionTermId()));
	}

	@Override
	@Test
	public void testGraphQLDeleteShippingFixedOptionTerm() throws Exception {
		ShippingFixedOptionTerm shippingFixedOptionTerm =
			shippingFixedOptionTermResource.
				postShippingFixedOptionIdShippingFixedOptionTerm(
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId(),
					randomShippingFixedOptionTerm());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteShippingFixedOptionTerm",
						HashMapBuilder.<String, Object>put(
							"shippingFixedOptionTermId",
							shippingFixedOptionTerm.
								getShippingFixedOptionTermId()
						).build())),
				"JSONObject/data", "Object/deleteShippingFixedOptionTerm"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"shippingFixedOptionId", "termExternalReferenceCode", "termId"
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
	protected ShippingFixedOptionTerm randomShippingFixedOptionTerm()
		throws Exception {

		CommerceTermEntry commerceTermEntry =
			_commerceTermEntryLocalService.addCommerceTermEntry(
				RandomTestUtil.randomString(), _user.getUserId(), true,
				RandomTestUtil.randomLocaleStringMap(), 1, 1, 2022, 12, 0, 0, 0,
				0, 0, 0, true, RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomString(), RandomTestUtil.nextDouble(),
				CommerceTermEntryConstants.TYPE_DELIVERY_TERMS,
				RandomTestUtil.randomString(), _serviceContext);

		_commerceTermEntries.add(commerceTermEntry);

		return new ShippingFixedOptionTerm() {
			{
				shippingFixedOptionId =
					_commerceShippingFixedOption.
						getCommerceShippingFixedOptionId();
				shippingFixedOptionTermId = RandomTestUtil.randomLong();
				termExternalReferenceCode =
					commerceTermEntry.getExternalReferenceCode();
				termId = commerceTermEntry.getCommerceTermEntryId();
			}
		};
	}

	@Override
	protected ShippingFixedOptionTerm
			testGetShippingFixedOptionIdShippingFixedOptionTermsPage_addShippingFixedOptionTerm(
				Long id, ShippingFixedOptionTerm shippingFixedOptionTerm)
		throws Exception {

		return shippingFixedOptionTermResource.
			postShippingFixedOptionIdShippingFixedOptionTerm(
				id, shippingFixedOptionTerm);
	}

	@Override
	protected Long
			testGetShippingFixedOptionIdShippingFixedOptionTermsPage_getId()
		throws Exception {

		return _commerceShippingFixedOption.getCommerceShippingFixedOptionId();
	}

	@Override
	protected ShippingFixedOptionTerm
			testPostShippingFixedOptionIdShippingFixedOptionTerm_addShippingFixedOptionTerm(
				ShippingFixedOptionTerm shippingFixedOptionTerm)
		throws Exception {

		return shippingFixedOptionTermResource.
			postShippingFixedOptionIdShippingFixedOptionTerm(
				_commerceShippingFixedOption.getCommerceShippingFixedOptionId(),
				shippingFixedOptionTerm);
	}

	@Inject
	private static CommerceChannelLocalService _commerceChannelLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceShippingFixedOptionLocalService
		_commerceShippingFixedOptionLocalService;

	@Inject
	private static CommerceShippingMethodLocalService
		_commerceShippingMethodLocalService;

	@Inject
	private static CommerceTermEntryLocalService _commerceTermEntryLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceShippingFixedOption _commerceShippingFixedOption;

	@DeleteAfterTestRun
	private CommerceShippingMethod _commerceShippingMethod;

	@DeleteAfterTestRun
	private final List<CommerceTermEntry> _commerceTermEntries =
		new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
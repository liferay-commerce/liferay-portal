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
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceListChannel;
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
public class PriceListChannelResourceTest
	extends BasePriceListChannelResourceTestCase {

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
	public void testDeletePriceListChannel() throws Exception {
		PriceListChannel priceListChannel =
			priceListChannelResource.postPriceListIdPriceListChannel(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListChannel());

		assertHttpResponseStatusCode(
			204,
			priceListChannelResource.deletePriceListChannelHttpResponse(
				priceListChannel.getPriceListChannelId()));

		assertHttpResponseStatusCode(
			404,
			priceListChannelResource.deletePriceListChannelHttpResponse(
				priceListChannel.getPriceListChannelId()));

		assertHttpResponseStatusCode(
			404,
			priceListChannelResource.deletePriceListChannelHttpResponse(
				priceListChannel.getPriceListChannelId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceListChannel() throws Exception {
		PriceListChannel priceListChannel =
			priceListChannelResource.postPriceListIdPriceListChannel(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListChannel());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceListChannel",
						HashMapBuilder.<String, Object>put(
							"priceListChannelId",
							priceListChannel.getPriceListChannelId()
						).build())),
				"JSONObject/data", "Object/deletePriceListChannel"));
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
	protected PriceListChannel randomPriceListChannel() throws Exception {
		CommerceChannel commerceChannel =
			_commerceChannelLocalService.addCommerceChannel(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				RandomTestUtil.randomString(),
				CommerceChannelConstants.CHANNEL_TYPE_SITE, null,
				RandomTestUtil.randomString(), _serviceContext);

		_commerceChannels.add(commerceChannel);

		return new PriceListChannel() {
			{
				channelExternalReferenceCode =
					commerceChannel.getExternalReferenceCode();
				channelId = commerceChannel.getCommerceChannelId();
				order = RandomTestUtil.randomInt();
				priceListChannelId = RandomTestUtil.randomLong();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
			}
		};
	}

	@Override
	protected PriceListChannel
			testGetPriceListByExternalReferenceCodePriceListChannelsPage_addPriceListChannel(
				String externalReferenceCode, PriceListChannel priceListChannel)
		throws Exception {

		return priceListChannelResource.
			postPriceListByExternalReferenceCodePriceListChannel(
				externalReferenceCode, priceListChannel);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceListChannelsPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceListChannel
			testGetPriceListIdPriceListChannelsPage_addPriceListChannel(
				Long id, PriceListChannel priceListChannel)
		throws Exception {

		return priceListChannelResource.postPriceListIdPriceListChannel(
			id, priceListChannel);
	}

	@Override
	protected Long testGetPriceListIdPriceListChannelsPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceListChannel
			testPostPriceListByExternalReferenceCodePriceListChannel_addPriceListChannel(
				PriceListChannel priceListChannel)
		throws Exception {

		return priceListChannelResource.
			postPriceListByExternalReferenceCodePriceListChannel(
				_commercePriceList.getExternalReferenceCode(),
				priceListChannel);
	}

	@Override
	protected PriceListChannel
			testPostPriceListIdPriceListChannel_addPriceListChannel(
				PriceListChannel priceListChannel)
		throws Exception {

		return priceListChannelResource.postPriceListIdPriceListChannel(
			_commercePriceList.getCommercePriceListId(), priceListChannel);
	}

	@Inject
	private static CommerceChannelLocalService _commerceChannelLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private final List<CommerceChannel> _commerceChannels = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
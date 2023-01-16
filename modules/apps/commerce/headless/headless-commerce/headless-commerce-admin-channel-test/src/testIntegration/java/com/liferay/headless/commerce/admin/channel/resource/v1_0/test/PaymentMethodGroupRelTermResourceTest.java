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
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.term.constants.CommerceTermEntryConstants;
import com.liferay.commerce.term.model.CommerceTermEntry;
import com.liferay.commerce.term.service.CommerceTermEntryLocalService;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.PaymentMethodGroupRelTerm;
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
public class PaymentMethodGroupRelTermResourceTest
	extends BasePaymentMethodGroupRelTermResourceTestCase {

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

		_commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				addCommercePaymentMethodGroupRel(
					_user.getUserId(), _commerceChannel.getGroupId(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomLocaleStringMap(), null,
					RandomTestUtil.randomString(), RandomTestUtil.nextDouble(),
					true);
	}

	@Override
	@Test
	public void testDeletePaymentMethodGroupRelTerm() throws Exception {
		PaymentMethodGroupRelTerm paymentMethodGroupRelTerm =
			paymentMethodGroupRelTermResource.
				postPaymentMethodGroupRelIdPaymentMethodGroupRelTerm(
					_commercePaymentMethodGroupRel.
						getCommercePaymentMethodGroupRelId(),
					randomPaymentMethodGroupRelTerm());

		assertHttpResponseStatusCode(
			204,
			paymentMethodGroupRelTermResource.
				deletePaymentMethodGroupRelTermHttpResponse(
					paymentMethodGroupRelTerm.
						getPaymentMethodGroupRelTermId()));

		assertHttpResponseStatusCode(
			404,
			paymentMethodGroupRelTermResource.
				deletePaymentMethodGroupRelTermHttpResponse(
					paymentMethodGroupRelTerm.
						getPaymentMethodGroupRelTermId()));
		assertHttpResponseStatusCode(
			404,
			paymentMethodGroupRelTermResource.
				deletePaymentMethodGroupRelTermHttpResponse(
					paymentMethodGroupRelTerm.
						getPaymentMethodGroupRelTermId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePaymentMethodGroupRelTerm() throws Exception {
		PaymentMethodGroupRelTerm paymentMethodGroupRelTerm =
			paymentMethodGroupRelTermResource.
				postPaymentMethodGroupRelIdPaymentMethodGroupRelTerm(
					_commercePaymentMethodGroupRel.
						getCommercePaymentMethodGroupRelId(),
					randomPaymentMethodGroupRelTerm());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePaymentMethodGroupRelTerm",
						HashMapBuilder.<String, Object>put(
							"paymentMethodGroupRelTermId",
							paymentMethodGroupRelTerm.
								getPaymentMethodGroupRelTermId()
						).build())),
				"JSONObject/data", "Object/deletePaymentMethodGroupRelTerm"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"paymentMethodGroupRelId", "termExternalReferenceCode", "termId"
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
	protected PaymentMethodGroupRelTerm randomPaymentMethodGroupRelTerm()
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

		return new PaymentMethodGroupRelTerm() {
			{
				paymentMethodGroupRelId =
					_commercePaymentMethodGroupRel.
						getCommercePaymentMethodGroupRelId();
				paymentMethodGroupRelTermId = RandomTestUtil.randomLong();
				termExternalReferenceCode =
					commerceTermEntry.getExternalReferenceCode();
				termId = commerceTermEntry.getCommerceTermEntryId();
			}
		};
	}

	@Override
	protected PaymentMethodGroupRelTerm
			testGetPaymentMethodGroupRelIdPaymentMethodGroupRelTermsPage_addPaymentMethodGroupRelTerm(
				Long id, PaymentMethodGroupRelTerm paymentMethodGroupRelTerm)
		throws Exception {

		return paymentMethodGroupRelTermResource.
			postPaymentMethodGroupRelIdPaymentMethodGroupRelTerm(
				id, paymentMethodGroupRelTerm);
	}

	@Override
	protected Long
			testGetPaymentMethodGroupRelIdPaymentMethodGroupRelTermsPage_getId()
		throws Exception {

		return _commercePaymentMethodGroupRel.
			getCommercePaymentMethodGroupRelId();
	}

	@Override
	protected PaymentMethodGroupRelTerm
			testPostPaymentMethodGroupRelIdPaymentMethodGroupRelTerm_addPaymentMethodGroupRelTerm(
				PaymentMethodGroupRelTerm paymentMethodGroupRelTerm)
		throws Exception {

		return paymentMethodGroupRelTermResource.
			postPaymentMethodGroupRelIdPaymentMethodGroupRelTerm(
				_commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId(),
				paymentMethodGroupRelTerm);
	}

	@Inject
	private static CommerceChannelLocalService _commerceChannelLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePaymentMethodGroupRelLocalService
		_commercePaymentMethodGroupRelLocalService;

	@Inject
	private static CommerceTermEntryLocalService _commerceTermEntryLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePaymentMethodGroupRel _commercePaymentMethodGroupRel;

	@DeleteAfterTestRun
	private final List<CommerceTermEntry> _commerceTermEntries =
		new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
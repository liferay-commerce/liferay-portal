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

import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceListAccountGroup;
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
public class PriceListAccountGroupResourceTest
	extends BasePriceListAccountGroupResourceTestCase {

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
	}

	@Override
	@Test
	public void testDeletePriceListAccountGroup() throws Exception {
		PriceListAccountGroup priceListAccountGroup =
			priceListAccountGroupResource.postPriceListIdPriceListAccountGroup(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListAccountGroup());

		assertHttpResponseStatusCode(
			204,
			priceListAccountGroupResource.
				deletePriceListAccountGroupHttpResponse(
					priceListAccountGroup.getPriceListAccountGroupId()));

		assertHttpResponseStatusCode(
			404,
			priceListAccountGroupResource.
				deletePriceListAccountGroupHttpResponse(
					priceListAccountGroup.getPriceListAccountGroupId()));

		assertHttpResponseStatusCode(
			404,
			priceListAccountGroupResource.
				deletePriceListAccountGroupHttpResponse(
					priceListAccountGroup.getPriceListAccountGroupId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceListAccountGroup() throws Exception {
		PriceListAccountGroup priceListAccountGroup =
			priceListAccountGroupResource.postPriceListIdPriceListAccountGroup(
				_commercePriceList.getCommercePriceListId(),
				randomPriceListAccountGroup());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceListAccountGroup",
						HashMapBuilder.<String, Object>put(
							"priceListAccountGroupId",
							priceListAccountGroup.getPriceListAccountGroupId()
						).build())),
				"JSONObject/data", "Object/deletePriceListAccountGroup"));
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
	protected PriceListAccountGroup randomPriceListAccountGroup()
		throws Exception {

		AccountGroup localAccountGroup =
			_accountGroupLocalService.addAccountGroup(
				_user.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString());

		_accountGroups.add(localAccountGroup);

		return new PriceListAccountGroup() {
			{
				accountGroupExternalReferenceCode =
					localAccountGroup.getExternalReferenceCode();
				accountGroupId = localAccountGroup.getAccountGroupId();
				order = RandomTestUtil.randomInt();
				priceListAccountGroupId = RandomTestUtil.randomLong();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
			}
		};
	}

	@Override
	protected PriceListAccountGroup
			testGetPriceListByExternalReferenceCodePriceListAccountGroupsPage_addPriceListAccountGroup(
				String externalReferenceCode,
				PriceListAccountGroup priceListAccountGroup)
		throws Exception {

		return priceListAccountGroupResource.
			postPriceListByExternalReferenceCodePriceListAccountGroup(
				externalReferenceCode, priceListAccountGroup);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceListAccountGroupsPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceListAccountGroup
			testGetPriceListIdPriceListAccountGroupsPage_addPriceListAccountGroup(
				Long id, PriceListAccountGroup priceListAccountGroup)
		throws Exception {

		return priceListAccountGroupResource.
			postPriceListIdPriceListAccountGroup(id, priceListAccountGroup);
	}

	@Override
	protected Long testGetPriceListIdPriceListAccountGroupsPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceListAccountGroup
			testPostPriceListByExternalReferenceCodePriceListAccountGroup_addPriceListAccountGroup(
				PriceListAccountGroup priceListAccountGroup)
		throws Exception {

		return priceListAccountGroupResource.
			postPriceListByExternalReferenceCodePriceListAccountGroup(
				_commercePriceList.getExternalReferenceCode(),
				priceListAccountGroup);
	}

	@Override
	protected PriceListAccountGroup
			testPostPriceListIdPriceListAccountGroup_addPriceListAccountGroup(
				PriceListAccountGroup priceListAccountGroup)
		throws Exception {

		return priceListAccountGroupResource.
			postPriceListIdPriceListAccountGroup(
				_commercePriceList.getCommercePriceListId(),
				priceListAccountGroup);
	}

	@Inject
	private static AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private final List<AccountGroup> _accountGroups = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
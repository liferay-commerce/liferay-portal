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
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceList;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
public class PriceListResourceTest extends BasePriceListResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId()));

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, 0, true);

		_commercePriceListLocalService.deleteCommercePriceLists(
			testCompany.getCompanyId());
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"catalogBasePriceList", "catalogId", "catalogName", "currencyCode",
			"name", "netPrice", "priority"
		};
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return new String[] {"createDate", "type"};
	}

	@Override
	protected PriceList randomPriceList() throws Exception {
		return new PriceList() {
			{
				active = RandomTestUtil.randomBoolean();
				author = StringUtil.toLowerCase(RandomTestUtil.randomString());
				catalogBasePriceList = false;
				catalogId = _commerceCatalog.getCommerceCatalogId();
				catalogName = _commerceCatalog.getName();
				createDate = RandomTestUtil.nextDate();
				currencyCode = _commerceCurrency.getCode();
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				netPrice = RandomTestUtil.randomBoolean();
				neverExpire = true;
				priority = RandomTestUtil.randomDouble();
				type = Type.PRICE_LIST;
			}
		};
	}

	@Override
	protected PriceList testDeletePriceList_addPriceList() throws Exception {
		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList
			testDeletePriceListByExternalReferenceCode_addPriceList()
		throws Exception {

		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testGetPriceList_addPriceList() throws Exception {
		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testGetPriceListByExternalReferenceCode_addPriceList()
		throws Exception {

		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testGetPriceListsPage_addPriceList(PriceList priceList)
		throws Exception {

		return priceListResource.postPriceList(priceList);
	}

	@Override
	protected PriceList testGraphQLPriceList_addPriceList() throws Exception {
		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testPatchPriceList_addPriceList() throws Exception {
		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testPatchPriceListByExternalReferenceCode_addPriceList()
		throws Exception {

		return priceListResource.postPriceList(randomPriceList());
	}

	@Override
	protected PriceList testPostPriceList_addPriceList(PriceList priceList)
		throws Exception {

		return priceListResource.postPriceList(priceList);
	}

	@Inject
	private static CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private User _user;

}
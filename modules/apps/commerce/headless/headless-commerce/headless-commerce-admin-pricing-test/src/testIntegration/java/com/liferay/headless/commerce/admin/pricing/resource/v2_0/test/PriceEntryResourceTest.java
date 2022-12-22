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
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceEntry;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Page;
import com.liferay.headless.commerce.admin.pricing.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.pricing.client.serdes.v2_0.PriceEntrySerDes;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.Arrays;
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
public class PriceEntryResourceTest extends BasePriceEntryResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
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
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true, serviceContext);

		_cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpDefinition = _cpInstance.getCPDefinition();
	}

	@Override
	@Test
	public void testDeletePriceEntry() throws Exception {
		PriceEntry priceEntry = priceEntryResource.postPriceListIdPriceEntry(
			_commercePriceList.getCommercePriceListId(), randomPriceEntry());

		assertHttpResponseStatusCode(
			204,
			priceEntryResource.deletePriceEntryHttpResponse(
				priceEntry.getPriceEntryId()));

		assertHttpResponseStatusCode(
			404,
			priceEntryResource.deletePriceEntryHttpResponse(
				priceEntry.getPriceEntryId()));

		assertHttpResponseStatusCode(
			404,
			priceEntryResource.deletePriceEntryHttpResponse(
				priceEntry.getPriceEntryId()));
	}

	@Override
	@Test
	public void testDeletePriceEntryByExternalReferenceCode() throws Exception {
		PriceEntry priceEntry =
			priceEntryResource.postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(),
				randomPriceEntry());

		assertHttpResponseStatusCode(
			204,
			priceEntryResource.
				deletePriceEntryByExternalReferenceCodeHttpResponse(
					priceEntry.getExternalReferenceCode()));

		assertHttpResponseStatusCode(
			404,
			priceEntryResource.
				deletePriceEntryByExternalReferenceCodeHttpResponse(
					priceEntry.getExternalReferenceCode()));

		assertHttpResponseStatusCode(
			404,
			priceEntryResource.
				deletePriceEntryByExternalReferenceCodeHttpResponse(
					priceEntry.getExternalReferenceCode()));
	}

	@Override
	@Test
	public void testGetPriceEntry() throws Exception {
		Long id = _commercePriceList.getCommercePriceListId();

		Page<PriceEntry> page =
			priceEntryResource.getPriceListIdPriceEntriesPage(
				id, null, null, Pagination.of(1, 10), null);

		Assert.assertEquals(0, page.getTotalCount());

		PriceEntry priceEntry1 = priceEntryResource.postPriceListIdPriceEntry(
			_commercePriceList.getCommercePriceListId(), randomPriceEntry());

		PriceEntry priceEntry2 = priceEntryResource.postPriceListIdPriceEntry(
			_commercePriceList.getCommercePriceListId(), randomPriceEntry());

		page = priceEntryResource.getPriceListIdPriceEntriesPage(
			id, null, null, Pagination.of(1, 10), null);

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(priceEntry1, priceEntry2),
			(List<PriceEntry>)page.getItems());
		assertValid(page);
	}

	@Override
	@Test
	public void testGetPriceEntryByExternalReferenceCode() throws Exception {
		String externalReferenceCode =
			_commercePriceList.getExternalReferenceCode();

		Page<PriceEntry> page =
			priceEntryResource.
				getPriceListByExternalReferenceCodePriceEntriesPage(
					externalReferenceCode, null, null, Pagination.of(1, 10),
					null);

		Assert.assertEquals(0, page.getTotalCount());

		PriceEntry priceEntry1 =
			priceEntryResource.postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(),
				randomPriceEntry());

		PriceEntry priceEntry2 =
			priceEntryResource.postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(),
				randomPriceEntry());

		page =
			priceEntryResource.
				getPriceListByExternalReferenceCodePriceEntriesPage(
					externalReferenceCode, null, null, Pagination.of(1, 10),
					null);

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(priceEntry1, priceEntry2),
			(List<PriceEntry>)page.getItems());
		assertValid(page);
	}

	@Override
	@Test
	public void testGraphQLDeletePriceEntry() throws Exception {
		PriceEntry priceEntry = priceEntryResource.postPriceListIdPriceEntry(
			_commercePriceList.getCommercePriceListId(), randomPriceEntry());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceEntry",
						HashMapBuilder.<String, Object>put(
							"priceEntryId", priceEntry::getPriceEntryId
						).build())),
				"JSONObject/data", "Object/deletePriceEntry"));
	}

	@Override
	@Test
	public void testGraphQLGetPriceEntryByExternalReferenceCode()
		throws Exception {

		PriceEntry priceEntry =
			priceEntryResource.postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(),
				randomPriceEntry());

		String externalReferenceCode =
			"\"" + priceEntry.getExternalReferenceCode() + "\"";

		Assert.assertTrue(
			equals(
				priceEntry,
				PriceEntrySerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"priceEntryByExternalReferenceCode",
								HashMapBuilder.<String, Object>put(
									"externalReferenceCode",
									externalReferenceCode
								).build(),
								getGraphQLFields())),
						"JSONObject/data",
						"Object/priceEntryByExternalReferenceCode"))));
	}

	@Override
	@Test
	public void testGraphQLGetPriceEntryByExternalReferenceCodeNotFound()
		throws Exception {

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"priceEntryByExternalReferenceCode",
						HashMapBuilder.<String, Object>put(
							"externalReferenceCode",
							() -> "\"" + RandomTestUtil.randomString() + "\""
						).build(),
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	@Override
	@Test
	public void testPatchPriceEntry() throws Exception {
		PriceEntry postPriceEntry =
			priceEntryResource.postPriceListIdPriceEntry(
				_commercePriceList.getCommercePriceListId(),
				randomPriceEntry());

		PriceEntry randomPatchPriceEntry = randomPatchPriceEntry();

		priceEntryResource.patchPriceEntry(
			postPriceEntry.getPriceEntryId(), randomPatchPriceEntry);

		PriceEntry expectedPriceEntry = postPriceEntry.clone();

		BeanTestUtil.copyProperties(randomPatchPriceEntry, expectedPriceEntry);

		PriceEntry getPriceEntry = priceEntryResource.getPriceEntry(
			postPriceEntry.getPriceEntryId());

		assertEquals(expectedPriceEntry, getPriceEntry);
		assertValid(getPriceEntry);
	}

	@Override
	@Test
	public void testPatchPriceEntryByExternalReferenceCode() throws Exception {
		PriceEntry postPriceEntry =
			priceEntryResource.postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(),
				randomPriceEntry());

		PriceEntry randomPatchPriceEntry = randomPatchPriceEntry();

		priceEntryResource.patchPriceEntryByExternalReferenceCode(
			postPriceEntry.getExternalReferenceCode(), randomPatchPriceEntry);

		PriceEntry expectedPriceEntry = postPriceEntry.clone();

		BeanTestUtil.copyProperties(randomPatchPriceEntry, expectedPriceEntry);

		PriceEntry getPriceEntry =
			priceEntryResource.getPriceEntryByExternalReferenceCode(
				postPriceEntry.getExternalReferenceCode());

		assertEquals(expectedPriceEntry, getPriceEntry);
		assertValid(getPriceEntry);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"priceListId", "skuExternalReferenceCode", "skuId"
		};
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return new String[] {
			"priceListExternalReferenceCode", "skuExternalReferenceCode"
		};
	}

	@Override
	protected PriceEntry randomPriceEntry() throws Exception {
		return new PriceEntry() {
			{
				active = RandomTestUtil.randomBoolean();
				bulkPricing = RandomTestUtil.randomBoolean();
				discountDiscovery = RandomTestUtil.randomBoolean();
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				hasTierPrice = RandomTestUtil.randomBoolean();
				neverExpire = true;
				price = RandomTestUtil.randomDouble();
				priceEntryId = RandomTestUtil.randomLong();
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
				skuExternalReferenceCode =
					_cpInstance.getExternalReferenceCode();
				skuId = _cpInstance.getCPInstanceId();
			}
		};
	}

	@Override
	protected PriceEntry
			testGetPriceListByExternalReferenceCodePriceEntriesPage_addPriceEntry(
				String externalReferenceCode, PriceEntry priceEntry)
		throws Exception {

		return priceEntryResource.
			postPriceListByExternalReferenceCodePriceEntry(
				externalReferenceCode, priceEntry);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceEntriesPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceEntry testGetPriceListIdPriceEntriesPage_addPriceEntry(
			Long id, PriceEntry priceEntry)
		throws Exception {

		return priceEntryResource.postPriceListIdPriceEntry(id, priceEntry);
	}

	@Override
	protected Long testGetPriceListIdPriceEntriesPage_getId() throws Exception {
		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceEntry
			testPostPriceListByExternalReferenceCodePriceEntry_addPriceEntry(
				PriceEntry priceEntry)
		throws Exception {

		return priceEntryResource.
			postPriceListByExternalReferenceCodePriceEntry(
				_commercePriceList.getExternalReferenceCode(), priceEntry);
	}

	@Override
	protected PriceEntry testPostPriceListIdPriceEntry_addPriceEntry(
			PriceEntry priceEntry)
		throws Exception {

		return priceEntryResource.postPriceListIdPriceEntry(
			_commercePriceList.getCommercePriceListId(), priceEntry);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	@DeleteAfterTestRun
	private User _user;

}
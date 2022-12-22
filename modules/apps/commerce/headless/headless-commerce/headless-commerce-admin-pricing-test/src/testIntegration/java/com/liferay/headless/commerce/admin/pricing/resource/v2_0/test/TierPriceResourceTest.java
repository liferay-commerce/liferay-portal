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
import com.liferay.commerce.price.list.model.CommercePriceEntry;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceEntryLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListLocalService;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.TierPrice;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class TierPriceResourceTest extends BaseTierPriceResourceTestCase {

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

		_cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpDefinition = _cpInstance.getCPDefinition();

		_commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				_cpDefinition.getCProductId(), _cpInstance.getCPInstanceUuid(),
				_commercePriceList.getCommercePriceListId(), BigDecimal.TEN,
				BigDecimal.ONE, _serviceContext);
	}

	@Override
	@Test
	public void testPatchTierPrice() throws Exception {
		TierPrice postTierPrice = tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());

		TierPrice randomPatchTierPrice = randomPatchTierPrice();

		tierPriceResource.patchTierPrice(
			postTierPrice.getId(), randomPatchTierPrice);

		TierPrice expectedTierPrice = postTierPrice.clone();

		BeanTestUtil.copyProperties(randomPatchTierPrice, expectedTierPrice);

		TierPrice getTierPrice = tierPriceResource.getTierPrice(
			postTierPrice.getId());

		assertEquals(expectedTierPrice, getTierPrice);
		assertValid(getTierPrice);
	}

	@Override
	@Test
	public void testPatchTierPriceByExternalReferenceCode() throws Exception {
		TierPrice postTierPrice =
			tierPriceResource.postPriceEntryByExternalReferenceCodeTierPrice(
				_commercePriceEntry.getExternalReferenceCode(),
				randomTierPrice());

		TierPrice randomPatchTierPrice = randomPatchTierPrice();

		tierPriceResource.patchTierPriceByExternalReferenceCode(
			postTierPrice.getExternalReferenceCode(), randomPatchTierPrice);

		TierPrice expectedTierPrice = postTierPrice.clone();

		BeanTestUtil.copyProperties(randomPatchTierPrice, expectedTierPrice);

		TierPrice getTierPrice =
			tierPriceResource.getTierPriceByExternalReferenceCode(
				postTierPrice.getExternalReferenceCode());

		assertEquals(expectedTierPrice, getTierPrice);
		assertValid(getTierPrice);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"discountDiscovery", "minimumQuantity", "price",
			"priceEntryExternalReferenceCode", "priceEntryId"
		};
	}

	@Override
	protected TierPrice randomTierPrice() throws Exception {
		return new TierPrice() {
			{
				active = true;
				discountDiscovery = RandomTestUtil.randomBoolean();
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				minimumQuantity = RandomTestUtil.randomInt();
				neverExpire = true;
				price = RandomTestUtil.randomDouble();
				priceEntryExternalReferenceCode =
					_commercePriceEntry.getExternalReferenceCode();
				priceEntryId = _commercePriceEntry.getCommercePriceEntryId();
				priceFormatted = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
			}
		};
	}

	@Override
	protected TierPrice testDeleteTierPrice_addTierPrice() throws Exception {
		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());
	}

	@Override
	protected TierPrice
			testDeleteTierPriceByExternalReferenceCode_addTierPrice()
		throws Exception {

		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());
	}

	@Override
	protected TierPrice
			testGetPriceEntryByExternalReferenceCodeTierPricesPage_addTierPrice(
				String externalReferenceCode, TierPrice tierPrice)
		throws Exception {

		return tierPriceResource.postPriceEntryByExternalReferenceCodeTierPrice(
			externalReferenceCode, tierPrice);
	}

	@Override
	protected String
			testGetPriceEntryByExternalReferenceCodeTierPricesPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceEntry.getExternalReferenceCode();
	}

	@Override
	protected TierPrice testGetPriceEntryIdTierPricesPage_addTierPrice(
			Long priceEntryId, TierPrice tierPrice)
		throws Exception {

		return tierPriceResource.postPriceEntryIdTierPrice(
			priceEntryId, tierPrice);
	}

	@Override
	protected Long testGetPriceEntryIdTierPricesPage_getPriceEntryId()
		throws Exception {

		return _commercePriceEntry.getCommercePriceEntryId();
	}

	@Override
	protected TierPrice testGetTierPrice_addTierPrice() throws Exception {
		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());
	}

	@Override
	protected TierPrice testGetTierPriceByExternalReferenceCode_addTierPrice()
		throws Exception {

		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());
	}

	@Override
	protected TierPrice testGraphQLTierPrice_addTierPrice() throws Exception {
		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), randomTierPrice());
	}

	@Override
	protected TierPrice
			testPostPriceEntryByExternalReferenceCodeTierPrice_addTierPrice(
				TierPrice tierPrice)
		throws Exception {

		return tierPriceResource.postPriceEntryByExternalReferenceCodeTierPrice(
			_commercePriceEntry.getExternalReferenceCode(), tierPrice);
	}

	@Override
	protected TierPrice testPostPriceEntryIdTierPrice_addTierPrice(
			TierPrice tierPrice)
		throws Exception {

		return tierPriceResource.postPriceEntryIdTierPrice(
			_commercePriceEntry.getCommercePriceEntryId(), tierPrice);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceEntryLocalService
		_commercePriceEntryLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceEntry _commercePriceEntry;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
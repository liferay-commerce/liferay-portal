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
import com.liferay.commerce.pricing.constants.CommercePriceModifierConstants;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceModifier;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class PriceModifierResourceTest
	extends BasePriceModifierResourceTestCase {

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
	public void testPatchPriceModifier() throws Exception {
		PriceModifier postPriceModifier =
			priceModifierResource.postPriceListIdPriceModifier(
				_commercePriceList.getCommercePriceListId(),
				randomPatchPriceModifier());

		PriceModifier randomPatchPriceModifier = randomPatchPriceModifier();

		priceModifierResource.patchPriceModifier(
			postPriceModifier.getId(), randomPatchPriceModifier);

		PriceModifier expectedPriceModifier = postPriceModifier.clone();

		BeanTestUtil.copyProperties(
			randomPatchPriceModifier, expectedPriceModifier);

		PriceModifier getPriceModifier = priceModifierResource.getPriceModifier(
			postPriceModifier.getId());

		assertEquals(expectedPriceModifier, getPriceModifier);
		assertValid(getPriceModifier);
	}

	@Override
	@Test
	public void testPatchPriceModifierByExternalReferenceCode()
		throws Exception {

		PriceModifier postPriceModifier =
			priceModifierResource.
				postPriceListByExternalReferenceCodePriceModifier(
					_commercePriceList.getExternalReferenceCode(),
					randomPatchPriceModifier());

		PriceModifier randomPatchPriceModifier = randomPatchPriceModifier();

		priceModifierResource.patchPriceModifierByExternalReferenceCode(
			postPriceModifier.getExternalReferenceCode(),
			randomPatchPriceModifier);

		PriceModifier expectedPriceModifier = postPriceModifier.clone();

		BeanTestUtil.copyProperties(
			randomPatchPriceModifier, expectedPriceModifier);

		PriceModifier getPriceModifier =
			priceModifierResource.getPriceModifierByExternalReferenceCode(
				postPriceModifier.getExternalReferenceCode());

		assertEquals(expectedPriceModifier, getPriceModifier);
		assertValid(getPriceModifier);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"modifierAmount", "modifierType", "priceListExternalReferenceCode",
			"priceListId", "priority", "target", "title"
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
	protected PriceModifier randomPriceModifier() throws Exception {
		return new PriceModifier() {
			{
				active = true;
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				modifierAmount = BigDecimal.TEN;
				modifierType =
					CommercePriceModifierConstants.MODIFIER_TYPE_FIXED_AMOUNT;
				neverExpire = true;
				priceListExternalReferenceCode =
					_commercePriceList.getExternalReferenceCode();
				priceListId = _commercePriceList.getCommercePriceListId();
				priority = RandomTestUtil.randomDouble();
				target = CommercePriceModifierConstants.TARGET_PRODUCTS;
				title = StringUtil.toLowerCase(RandomTestUtil.randomString());
			}
		};
	}

	@Override
	protected PriceModifier testDeletePriceModifier_addPriceModifier()
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), randomPriceModifier());
	}

	@Override
	protected PriceModifier
			testDeletePriceModifierByExternalReferenceCode_addPriceModifier()
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), randomPriceModifier());
	}

	@Override
	protected PriceModifier
			testGetPriceListByExternalReferenceCodePriceModifiersPage_addPriceModifier(
				String externalReferenceCode, PriceModifier priceModifier)
		throws Exception {

		return priceModifierResource.
			postPriceListByExternalReferenceCodePriceModifier(
				externalReferenceCode, priceModifier);
	}

	@Override
	protected String
			testGetPriceListByExternalReferenceCodePriceModifiersPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceList.getExternalReferenceCode();
	}

	@Override
	protected PriceModifier
			testGetPriceListIdPriceModifiersPage_addPriceModifier(
				Long id, PriceModifier priceModifier)
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			id, priceModifier);
	}

	@Override
	protected Long testGetPriceListIdPriceModifiersPage_getId()
		throws Exception {

		return _commercePriceList.getCommercePriceListId();
	}

	@Override
	protected PriceModifier testGetPriceModifier_addPriceModifier()
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), randomPriceModifier());
	}

	@Override
	protected PriceModifier
			testGetPriceModifierByExternalReferenceCode_addPriceModifier()
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), randomPriceModifier());
	}

	@Override
	protected PriceModifier testGraphQLPriceModifier_addPriceModifier()
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), randomPriceModifier());
	}

	@Override
	protected PriceModifier
			testPostPriceListByExternalReferenceCodePriceModifier_addPriceModifier(
				PriceModifier priceModifier)
		throws Exception {

		return priceModifierResource.
			postPriceListByExternalReferenceCodePriceModifier(
				testGetPriceListByExternalReferenceCodePriceModifiersPage_getExternalReferenceCode(),
				priceModifier);
	}

	@Override
	protected PriceModifier testPostPriceListIdPriceModifier_addPriceModifier(
			PriceModifier priceModifier)
		throws Exception {

		return priceModifierResource.postPriceListIdPriceModifier(
			_commercePriceList.getCommercePriceListId(), priceModifier);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
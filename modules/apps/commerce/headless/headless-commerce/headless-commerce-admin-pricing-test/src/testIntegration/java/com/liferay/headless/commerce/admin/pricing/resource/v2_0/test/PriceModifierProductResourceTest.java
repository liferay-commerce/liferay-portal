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
import com.liferay.commerce.pricing.model.CommercePriceModifier;
import com.liferay.commerce.pricing.service.CommercePriceModifierLocalService;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.PriceModifierProduct;
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
public class PriceModifierProductResourceTest
	extends BasePriceModifierProductResourceTestCase {

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

		_commercePriceModifier =
			_commercePriceModifierLocalService.addCommercePriceModifier(
				testGroup.getGroupId(), RandomTestUtil.randomString(),
				_commercePriceList.getCommercePriceListId(),
				CommercePriceModifierConstants.MODIFIER_TYPE_FIXED_AMOUNT,
				BigDecimal.TEN, RandomTestUtil.randomDouble(), true, 1, 1, 2022,
				12, 0, 0, 0, 0, 0, 0, true, _serviceContext);
	}

	@Override
	@Test
	public void testDeletePriceModifierProduct() throws Exception {
		PriceModifierProduct priceModifierProduct =
			priceModifierProductResource.
				postPriceModifierIdPriceModifierProduct(
					_commercePriceModifier.getCommercePriceModifierId(),
					randomPriceModifierProduct());

		assertHttpResponseStatusCode(
			204,
			priceModifierProductResource.deletePriceModifierProductHttpResponse(
				priceModifierProduct.getPriceModifierProductId()));

		assertHttpResponseStatusCode(
			404,
			priceModifierProductResource.deletePriceModifierProductHttpResponse(
				priceModifierProduct.getPriceModifierProductId()));

		assertHttpResponseStatusCode(
			404,
			priceModifierProductResource.deletePriceModifierProductHttpResponse(
				priceModifierProduct.getPriceModifierProductId()));
	}

	@Override
	@Test
	public void testGraphQLDeletePriceModifierProduct() throws Exception {
		PriceModifierProduct priceModifierProduct =
			priceModifierProductResource.
				postPriceModifierIdPriceModifierProduct(
					_commercePriceModifier.getCommercePriceModifierId(),
					randomPriceModifierProduct());

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deletePriceModifierProduct",
						HashMapBuilder.<String, Object>put(
							"priceModifierProductId",
							priceModifierProduct.getPriceModifierProductId()
						).build())),
				"JSONObject/data", "Object/deletePriceModifierProduct"));
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
	protected PriceModifierProduct randomPriceModifierProduct()
		throws Exception {

		CPInstance cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			testGroup.getGroupId(), BigDecimal.TEN);

		_cpInstances.add(cpInstance);

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		_cpDefinitions.add(cpDefinition);

		CProduct cProduct = cpDefinition.getCProduct();

		return new PriceModifierProduct() {
			{
				priceModifierExternalReferenceCode =
					_commercePriceModifier.getExternalReferenceCode();
				priceModifierId =
					_commercePriceModifier.getCommercePriceModifierId();
				priceModifierProductId = RandomTestUtil.randomLong();
				productExternalReferenceCode =
					cProduct.getExternalReferenceCode();
				productId = cProduct.getCProductId();
			}
		};
	}

	@Override
	protected PriceModifierProduct
			testGetPriceModifierByExternalReferenceCodePriceModifierProductsPage_addPriceModifierProduct(
				String externalReferenceCode,
				PriceModifierProduct priceModifierProduct)
		throws Exception {

		return priceModifierProductResource.
			postPriceModifierByExternalReferenceCodePriceModifierProduct(
				externalReferenceCode, priceModifierProduct);
	}

	@Override
	protected String
			testGetPriceModifierByExternalReferenceCodePriceModifierProductsPage_getExternalReferenceCode()
		throws Exception {

		return _commercePriceModifier.getExternalReferenceCode();
	}

	@Override
	protected PriceModifierProduct
			testGetPriceModifierIdPriceModifierProductsPage_addPriceModifierProduct(
				Long id, PriceModifierProduct priceModifierProduct)
		throws Exception {

		return priceModifierProductResource.
			postPriceModifierIdPriceModifierProduct(id, priceModifierProduct);
	}

	@Override
	protected Long testGetPriceModifierIdPriceModifierProductsPage_getId()
		throws Exception {

		return _commercePriceModifier.getCommercePriceModifierId();
	}

	@Override
	protected PriceModifierProduct
			testPostPriceModifierByExternalReferenceCodePriceModifierProduct_addPriceModifierProduct(
				PriceModifierProduct priceModifierProduct)
		throws Exception {

		return priceModifierProductResource.
			postPriceModifierByExternalReferenceCodePriceModifierProduct(
				_commercePriceModifier.getExternalReferenceCode(),
				priceModifierProduct);
	}

	@Override
	protected PriceModifierProduct
			testPostPriceModifierIdPriceModifierProduct_addPriceModifierProduct(
				PriceModifierProduct priceModifierProduct)
		throws Exception {

		return priceModifierProductResource.
			postPriceModifierIdPriceModifierProduct(
				_commercePriceModifier.getCommercePriceModifierId(),
				priceModifierProduct);
	}

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommercePriceListLocalService _commercePriceListLocalService;

	@Inject
	private static CommercePriceModifierLocalService
		_commercePriceModifierLocalService;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommercePriceList _commercePriceList;

	@DeleteAfterTestRun
	private CommercePriceModifier _commercePriceModifier;

	@DeleteAfterTestRun
	private final List<CPDefinition> _cpDefinitions = new ArrayList<>();

	@DeleteAfterTestRun
	private final List<CPInstance> _cpInstances = new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
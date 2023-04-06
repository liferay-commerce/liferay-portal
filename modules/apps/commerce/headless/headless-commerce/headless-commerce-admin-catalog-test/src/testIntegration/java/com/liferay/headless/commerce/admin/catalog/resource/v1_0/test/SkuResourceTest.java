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

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CPOptionValue;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CPOptionLocalService;
import com.liferay.commerce.product.service.CPOptionValueLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductVirtualSettings;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.SkuOption;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.SkuResource;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class SkuResourceTest extends BaseSkuResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "virtual", true, false);

		_cProduct = _cpDefinition.getCProduct();

		User user = UserTestUtil.addUser(testCompany);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				user.getUserId());

		_cpOption = _cpOptionLocalService.addCPOption(
			RandomTestUtil.randomString(), user.getUserId(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), "select", false, false,
			false, RandomTestUtil.randomString(), serviceContext);

		_cpOptionValue = _cpOptionValueLocalService.addCPOptionValue(
			_cpOption.getCPOptionId(), RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.nextDouble(), RandomTestUtil.randomString(),
			serviceContext);

		_cpDefinitionOptionRel =
			_cpDefinitionOptionRelLocalService.addCPDefinitionOptionRel(
				_cpDefinition.getCPDefinitionId(), _cpOption.getCPOptionId(),
				serviceContext);

		_cpDefinitionOptionValueRels =
			_cpDefinitionOptionRel.getCPDefinitionOptionValueRels();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeleteSku() throws Exception {
		super.testGraphQLDeleteSku();
	}

	@Test
	public void testPostProductIdSkuWithOptionId() throws Exception {
		super.testPostProductIdSku();

		SkuResource skuResource = SkuResource.builder(
		).authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			_cpDefinitionOptionValueRels.get(0);

		Sku postSku = skuResource.postProductIdSku(
			_cpDefinition.getCProductId(),
			_randomSkuWithSkuOptions(
				null, _cpDefinitionOptionRel.getCPDefinitionOptionRelId(),
				cpDefinitionOptionValueRel.getCPDefinitionOptionValueRelId(),
				null));

		SkuOption[] skuOptions = postSku.getSkuOptions();

		Assert.assertTrue((skuOptions != null) && (skuOptions.length == 1));

		SkuOption skuOption = skuOptions[0];

		Assert.assertEquals(skuOption.getKey(), _cpOption.getKey());
		Assert.assertEquals(
			(long)skuOption.getOptionId(),
			_cpDefinitionOptionRel.getCPDefinitionOptionRelId());
		Assert.assertEquals(
			(long)skuOption.getOptionValueId(),
			cpDefinitionOptionValueRel.getCPDefinitionOptionValueRelId());
		Assert.assertEquals(skuOption.getValue(), _cpOptionValue.getKey());
	}

	@Test
	public void testPostProductIdSkuWithOptionIdKey() throws Exception {
		super.testPostProductIdSku();

		SkuResource skuResource = SkuResource.builder(
		).authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			_cpDefinitionOptionValueRels.get(0);

		Sku postSku = skuResource.postProductIdSku(
			_cpDefinition.getCProductId(),
			_randomSkuWithSkuOptions(
				String.valueOf(
					_cpDefinitionOptionRel.getCPDefinitionOptionRelId()),
				null, null,
				String.valueOf(
					cpDefinitionOptionValueRel.
						getCPDefinitionOptionValueRelId())));

		SkuOption[] skuOptions = postSku.getSkuOptions();

		Assert.assertTrue((skuOptions != null) && (skuOptions.length == 1));

		SkuOption skuOption = skuOptions[0];

		Assert.assertEquals(skuOption.getKey(), _cpOption.getKey());
		Assert.assertEquals(
			(long)skuOption.getOptionId(),
			_cpDefinitionOptionRel.getCPDefinitionOptionRelId());
		Assert.assertEquals(
			(long)skuOption.getOptionValueId(),
			cpDefinitionOptionValueRel.getCPDefinitionOptionValueRelId());
		Assert.assertEquals(skuOption.getValue(), _cpOptionValue.getKey());
	}

	@Test
	public void testPostProductIdSkuWithOptionKey() throws Exception {
		super.testPostProductIdSku();

		SkuResource skuResource = SkuResource.builder(
		).authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			_cpDefinitionOptionValueRels.get(0);

		Sku postSku = skuResource.postProductIdSku(
			_cpDefinition.getCProductId(),
			_randomSkuWithSkuOptions(
				_cpOption.getKey(), null, null, _cpOptionValue.getKey()));

		SkuOption[] skuOptions = postSku.getSkuOptions();

		Assert.assertTrue((skuOptions != null) && (skuOptions.length == 1));

		SkuOption skuOption = skuOptions[0];

		Assert.assertEquals(skuOption.getKey(), _cpOption.getKey());
		Assert.assertEquals(
			(long)skuOption.getOptionId(),
			_cpDefinitionOptionRel.getCPDefinitionOptionRelId());
		Assert.assertEquals(
			(long)skuOption.getOptionValueId(),
			cpDefinitionOptionValueRel.getCPDefinitionOptionValueRelId());
		Assert.assertEquals(skuOption.getValue(), _cpOptionValue.getKey());
	}

	@Test
	public void testPostProductIdSkuWithVirtualSettings() throws Exception {
		super.testPostProductIdSku();

		SkuResource skuResource = SkuResource.builder(
		).authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).parameters(
			"nestedFields", "virtualSettings"
		).build();

		ProductVirtualSettings randomProductVirtualSettings =
			new ProductVirtualSettings() {
				{
					activationStatus = 0;
					duration = RandomTestUtil.nextLong();
					maxUsages = RandomTestUtil.nextInt();
					override = true;
					sampleUrl = "https://liferay.com";
					termsOfUseRequired = false;
					url = "https://liferay.com";
					useSample = true;
				}
			};

		Sku randomSku = randomSku();

		randomSku.setVirtualSettings(randomProductVirtualSettings);

		Sku postSku = skuResource.postProductIdSku(
			_cpDefinition.getCProductId(), randomSku);

		ProductVirtualSettings postProductVirtualSettings =
			postSku.getVirtualSettings();

		Assert.assertNotNull(postProductVirtualSettings);
		Assert.assertEquals(
			postProductVirtualSettings.getActivationStatus(),
			randomProductVirtualSettings.getActivationStatus());
		Assert.assertEquals(
			postProductVirtualSettings.getDuration(),
			randomProductVirtualSettings.getDuration());
		Assert.assertEquals(
			postProductVirtualSettings.getMaxUsages(),
			randomProductVirtualSettings.getMaxUsages());
		Assert.assertEquals(
			postProductVirtualSettings.getOverride(),
			randomProductVirtualSettings.getOverride());
		Assert.assertEquals(
			postProductVirtualSettings.getSampleUrl(),
			randomProductVirtualSettings.getSampleUrl());
		Assert.assertEquals(
			postProductVirtualSettings.getTermsOfUseRequired(),
			randomProductVirtualSettings.getTermsOfUseRequired());
		Assert.assertEquals(
			postProductVirtualSettings.getUrl(),
			randomProductVirtualSettings.getUrl());
		Assert.assertEquals(
			postProductVirtualSettings.getUseSample(),
			randomProductVirtualSettings.getUseSample());
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"depth", "discontinued", "gtin", "height", "manufacturerPartNumber",
			"published", "purchasable", "sku", "unspsc", "weight", "width"
		};
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return new String[] {"catalogId"};
	}

	@Override
	protected Sku randomSku() throws Exception {
		return new Sku() {
			{
				depth = RandomTestUtil.randomDouble();
				discontinued = false;
				discontinuedDate = RandomTestUtil.nextDate();
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				gtin = StringUtil.toLowerCase(RandomTestUtil.randomString());
				height = RandomTestUtil.randomDouble();
				inventoryLevel = RandomTestUtil.randomInt();
				manufacturerPartNumber = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				neverExpire = true;
				published = true;
				purchasable = true;
				sku = StringUtil.toLowerCase(RandomTestUtil.randomString());
				unspsc = StringUtil.toLowerCase(RandomTestUtil.randomString());
				weight = RandomTestUtil.randomDouble();
				width = RandomTestUtil.randomDouble();
			}
		};
	}

	@Override
	protected Sku testDeleteSku_addSku() throws Exception {
		return skuResource.postProductIdSku(
			_cProduct.getCProductId(), randomSku());
	}

	@Override
	protected Sku testDeleteSkuByExternalReferenceCode_addSku()
		throws Exception {

		return skuResource.postProductByExternalReferenceCodeSku(
			_cProduct.getExternalReferenceCode(), randomSku());
	}

	@Override
	protected Sku testGetProductByExternalReferenceCodeSkusPage_addSku(
			String externalReferenceCode, Sku sku)
		throws Exception {

		return skuResource.postProductByExternalReferenceCodeSku(
			externalReferenceCode, sku);
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodeSkusPage_getExternalReferenceCode()
		throws Exception {

		return _cProduct.getExternalReferenceCode();
	}

	@Override
	protected Sku testGetProductIdSkusPage_addSku(Long id, Sku sku)
		throws Exception {

		return skuResource.postProductIdSku(id, sku);
	}

	@Override
	protected Long testGetProductIdSkusPage_getId() throws Exception {
		return _cProduct.getCProductId();
	}

	@Override
	protected Sku testGetSku_addSku() throws Exception {
		return skuResource.postProductIdSku(
			_cProduct.getCProductId(), randomSku());
	}

	@Override
	protected Sku testGetSkuByExternalReferenceCode_addSku() throws Exception {
		return skuResource.postProductByExternalReferenceCodeSku(
			_cProduct.getExternalReferenceCode(), randomSku());
	}

	@Override
	protected Sku testGetSkusPage_addSku(Sku sku) throws Exception {
		return skuResource.postProductIdSku(_cProduct.getCProductId(), sku);
	}

	@Override
	protected Sku testGraphQLSku_addSku() throws Exception {
		return skuResource.postProductIdSku(
			_cProduct.getCProductId(), randomSku());
	}

	@Override
	protected Sku testPatchSku_addSku() throws Exception {
		return skuResource.postProductIdSku(
			_cProduct.getCProductId(), randomSku());
	}

	@Override
	protected Sku testPatchSkuByExternalReferenceCode_addSku()
		throws Exception {

		return skuResource.postProductByExternalReferenceCodeSku(
			_cProduct.getExternalReferenceCode(), randomSku());
	}

	@Override
	protected Sku testPostProductByExternalReferenceCodeSku_addSku(Sku sku)
		throws Exception {

		return skuResource.postProductByExternalReferenceCodeSku(
			_cProduct.getExternalReferenceCode(), sku);
	}

	@Override
	protected Sku testPostProductIdSku_addSku(Sku sku) throws Exception {
		return skuResource.postProductIdSku(_cProduct.getCProductId(), sku);
	}

	private Sku _randomSkuWithSkuOptions(
			String optionKey, Long optionKeyId, Long optionValueKeyId,
			String optionValue)
		throws Exception {

		Sku sku = randomSku();

		sku.setSkuOptions(
			() -> new SkuOption[] {
				new SkuOption() {
					{
						key = optionKey;
						optionId = optionKeyId;
						optionValueId = optionValueKeyId;
						value = optionValue;
					}
				}
			});

		return sku;
	}

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private CPDefinitionOptionRel _cpDefinitionOptionRel;

	@Inject
	private CPDefinitionOptionRelLocalService
		_cpDefinitionOptionRelLocalService;

	private List<CPDefinitionOptionValueRel> _cpDefinitionOptionValueRels =
		new ArrayList<>();

	@Inject
	private CPInstanceLocalService _cpInstanceLocalService;

	private CPOption _cpOption;

	@Inject
	private CPOptionLocalService _cpOptionLocalService;

	private CPOptionValue _cpOptionValue;

	@Inject
	private CPOptionValueLocalService _cpOptionValueLocalService;

	private CProduct _cProduct;

}
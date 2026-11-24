/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.price.list.constants.CommercePriceListConstants;
import com.liferay.commerce.price.list.model.CommercePriceEntry;
import com.liferay.commerce.price.list.model.CommercePriceList;
import com.liferay.commerce.price.list.service.CommercePriceEntryLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListAccountRelLocalService;
import com.liferay.commerce.price.list.service.CommercePriceListLocalServiceUtil;
import com.liferay.commerce.product.configuration.CProductVersionConfiguration;
import com.liferay.commerce.product.model.CPConfigurationEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPConfigurationEntryLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.commerce.test.util.price.list.CommercePriceEntryTestUtil;
import com.liferay.commerce.test.util.price.list.CommercePriceListTestUtil;
import com.liferay.commerce.test.util.price.list.CommerceTierPriceEntryTestUtil;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Availability;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.DDMOption;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Price;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Sku;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.SkuOption;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.SkuUnitOfMeasure;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.TierPrice;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Page;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.delivery.catalog.client.problem.Problem;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class SkuResourceTest extends BaseSkuResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), RandomTestUtil.randomString());

		_cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", false, false);

		_cpDefinitionOptionRel = CPTestUtil.addCPDefinitionOptionRel(
			testGroup.getGroupId(), _cpDefinition.getCPDefinitionId(), true,
			10);
	}

	@Override
	@Test
	public void testGetChannelProductSku() throws Exception {
		super.testGetChannelProductSku();

		_testGetChannelProductSkuAllowMultiplePriceEntriesInTheSamePriceList();
		_testGetChannelProductSkuAllowMultiplePriceEntriesInTheSamePromotion();
		_testGetChannelProductSkuWithAnotherCPDefinition();
		_testGetChannelProductSkuWithCPDefinitionVersions();
		_testGetChannelProductSkuWithCurrencyCode();
		_testGetChannelProductSkuWithDraftCPInstance();
		_testGetChannelProductSkuWithUnitOfMeasurePrice();
	}

	@Override
	@Test
	public void testGetChannelProductSkusPage() throws Exception {
		super.testGetChannelProductSkusPage();

		_testGetChannelProductSkusPageWithPriceListAccountRel();
		_testGetChannelProductSkusPageWithUnitOfMeasure();
		_testGetChannelProductSkusPageWithUnitOfMeasurePrice();
	}

	@Override
	@Test
	public void testPostChannelProductSku() throws Exception {
		Problem.ProblemException problemException = Assert.assertThrows(
			Problem.ProblemException.class, super::testPostChannelProductSku);

		Problem problem = problemException.getProblem();

		Assert.assertEquals("BAD_REQUEST", problem.getStatus());
	}

	@Override
	@Test
	public void testPostChannelProductSkuBySkuOption() throws Exception {
		super.testPostChannelProductSkuBySkuOption();

		_testPostChannelProductSkuBySkuOptionWithBackOrder();
	}

	@Override
	protected Sku
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuByExternalReferenceCodeSkuExternalReferenceCode_addSku()
		throws Exception {

		return _addCPInstance(randomSku());
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuByExternalReferenceCodeSkuExternalReferenceCode_getChannelExternalReferenceCode()
		throws Exception {

		return _commerceChannel.getExternalReferenceCode();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuByExternalReferenceCodeSkuExternalReferenceCode_getProductExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected Sku
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkusPage_addSku(
				String channelExternalReferenceCode,
				String productExternalReferenceCode, Sku sku)
		throws Exception {

		return _addCPInstance(sku);
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkusPage_getChannelExternalReferenceCode()
		throws Exception {

		return _commerceChannel.getExternalReferenceCode();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkusPage_getProductExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected Sku testGetChannelProductSku_addSku() throws Exception {
		return _addCPInstance(randomSku());
	}

	@Override
	protected Long testGetChannelProductSku_getChannelId() throws Exception {
		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGetChannelProductSku_getProductId(Sku sku)
		throws Exception {

		return sku.getProductId();
	}

	@Override
	protected Sku testGetChannelProductSkusPage_addSku(
			Long channelId, Long productId, Sku sku)
		throws Exception {

		return _addCPInstance(sku);
	}

	@Override
	protected Long testGetChannelProductSkusPage_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGetChannelProductSkusPage_getProductId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected String
			testGraphQLGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuByExternalReferenceCodeSkuExternalReferenceCode_getChannelExternalReferenceCode()
		throws Exception {

		return _commerceChannel.getExternalReferenceCode();
	}

	@Override
	protected String
			testGraphQLGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuByExternalReferenceCodeSkuExternalReferenceCode_getProductExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected Long testGraphQLGetChannelProductSku_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGraphQLGetChannelProductSku_getProductId(Sku sku)
		throws Exception {

		return sku.getProductId();
	}

	@Override
	protected Sku testGraphQLSku_addSku() throws Exception {
		return _addCPInstance(randomSku());
	}

	@Override
	protected Sku
			testPostChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSku_addSku(
				Sku sku)
		throws Exception {

		return _addCPInstance(sku);
	}

	@Override
	protected Sku
			testPostChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeSkuBySkuOption_addSku(
				Sku sku)
		throws Exception {

		return _addCPInstance(sku);
	}

	@Override
	protected Sku testPostChannelProductSku_addSku(Sku sku) throws Exception {
		return skuResource.postChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			_cpDefinition.getCProductId(), null, null, new DDMOption[0]);
	}

	@Override
	protected Sku testPostChannelProductSkuBySkuOption_addSku(Sku sku)
		throws Exception {

		return _addCPInstance(sku);
	}

	private Sku _addCPInstance(Sku sku) throws Exception {
		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpDefinitionOptionRel.getCPDefinitionOptionValueRels();

		CPInstance cpInstance = _cpInstanceLocalService.addCPInstance(
			RandomTestUtil.randomString(), _cpDefinition.getCPDefinitionId(),
			_cpDefinition.getGroupId(), sku.getSku(), sku.getGtin(),
			sku.getManufacturerPartNumber(), sku.getPurchasable(),
			HashMapBuilder.put(
				_cpDefinitionOptionRel.getCPDefinitionOptionRelId(),
				() -> {
					CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
						cpDefinitionOptionValueRels.get(_cpInstances.size());

					return Collections.singletonList(
						cpDefinitionOptionValueRel.
							getCPDefinitionOptionValueRelId());
				}
			).build(),
			sku.getWidth(), sku.getHeight(), sku.getDepth(), sku.getWeight(),
			BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
			sku.getPublished(), 1, 1, 2022, 12, 0, 0, 0, 0, 0, 0, true, false,
			false, 1, null, null, 0, false, 1, null, null, 0,
			RandomTestUtil.randomString(), false, null, 0, 0, 0, 0,
			_serviceContext);

		_cpInstances.add(cpInstance);

		return new Sku() {
			{
				depth = cpInstance.getDepth();
				displayDate = cpInstance.getDisplayDate();
				expirationDate = cpInstance.getExpirationDate();
				externalReferenceCode = cpInstance.getExternalReferenceCode();
				gtin = cpInstance.getGtin();
				height = cpInstance.getHeight();
				id = cpInstance.getCPInstanceId();
				manufacturerPartNumber = cpInstance.getManufacturerPartNumber();
				maxOrderQuantity = BigDecimal.ZERO;
				minOrderQuantity = BigDecimal.ZERO;
				neverExpire = true;
				productId = _cpDefinition.getCProductId();
				published = cpInstance.isPublished();
				purchasable = cpInstance.isPurchasable();
				sku = cpInstance.getSku();
				weight = cpInstance.getWeight();
				width = cpInstance.getWidth();
			}
		};
	}

	private void _addCPInstanceUnitOfMeasure(Sku sku, boolean active)
		throws Exception {

		_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
			_user.getUserId(), sku.getId(), active, BigDecimal.ONE,
			RandomTestUtil.randomString(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomInt(0, 5), BigDecimal.ONE, true,
			RandomTestUtil.nextDouble(), BigDecimal.ONE, sku.getSku());
	}

	private void _assertNotFound(ThrowingRunnable throwingRunnable) {
		Problem.ProblemException problemException = Assert.assertThrows(
			Problem.ProblemException.class, throwingRunnable);

		Problem problem = problemException.getProblem();

		Assert.assertEquals("NOT_FOUND", problem.getStatus());
	}

	private void _testGetChannelProductSkuAllowMultiplePriceEntriesInTheSamePriceList()
		throws Exception {

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				testCompany.getCompanyId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			testGroup.getCompanyId(), testGroup.getGroupId(), _user.getUserId(),
			commerceCurrency.getCode());

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		CommercePriceList commercePriceList =
			CommercePriceListTestUtil.addCommercePriceList(
				commerceCatalog.getGroupId(), false,
				CommercePriceListConstants.TYPE_PRICE_LIST, 1.0);

		Calendar calendar = new GregorianCalendar();

		CommercePriceEntry commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				RandomTestUtil.randomString(), cpDefinition.getCProductId(),
				cpInstance.getCPInstanceUuid(),
				commercePriceList.getCommercePriceListId(), true, null, null,
				null, null, calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), false, BigDecimal.ONE, false,
				BigDecimal.ONE, StringPool.BLANK, _serviceContext);

		_serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		_commercePriceEntryLocalService.addCommercePriceEntry(
			RandomTestUtil.randomString(), cpDefinition.getCProductId(),
			cpInstance.getCPInstanceUuid(),
			commercePriceList.getCommercePriceListId(), true, null, null, null,
			null, calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR),
			calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
			calendar.get(Calendar.YEAR) - 1, calendar.get(Calendar.HOUR),
			calendar.get(Calendar.MINUTE), false, BigDecimal.TEN, false,
			BigDecimal.TEN, StringPool.BLANK, _serviceContext);

		Sku channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(), -1L,
			null);

		Price price = channelProductSku.getPrice();

		BigDecimal commercePriceEntryPrice = commercePriceEntry.getPrice();

		Assert.assertTrue(
			Objects.equals(
				price.getPrice(), commercePriceEntryPrice.doubleValue()));
	}

	private void _testGetChannelProductSkuAllowMultiplePriceEntriesInTheSamePromotion()
		throws Exception {

		_serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				testCompany.getCompanyId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			testGroup.getCompanyId(), testGroup.getGroupId(), _user.getUserId(),
			commerceCurrency.getCode());

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		CommercePriceList catalogBaseCommercePriceList =
			CommercePriceListLocalServiceUtil.
				getCatalogBaseCommercePriceListByType(
					commerceCatalog.getGroupId(),
					CommercePriceListConstants.TYPE_PRICE_LIST);

		Calendar calendar = new GregorianCalendar();

		_commercePriceEntryLocalService.addCommercePriceEntry(
			RandomTestUtil.randomString(), cpDefinition.getCProductId(),
			cpInstance.getCPInstanceUuid(),
			catalogBaseCommercePriceList.getCommercePriceListId(), true, null,
			null, null, null, calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR),
			calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
			calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.HOUR),
			calendar.get(Calendar.MINUTE), false, new BigDecimal(100), false,
			BigDecimal.ONE, StringPool.BLANK, _serviceContext);

		CommercePriceList commercePriceList =
			CommercePriceListTestUtil.addCommercePriceList(
				commerceCatalog.getGroupId(), false,
				CommercePriceListConstants.TYPE_PROMOTION, 1.0);

		CommercePriceEntry commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				RandomTestUtil.randomString(), cpDefinition.getCProductId(),
				cpInstance.getCPInstanceUuid(),
				commercePriceList.getCommercePriceListId(), true, null, null,
				null, null, calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), false, BigDecimal.ONE, false,
				BigDecimal.ONE, StringPool.BLANK, _serviceContext);

		_serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		_commercePriceEntryLocalService.addCommercePriceEntry(
			RandomTestUtil.randomString(), cpDefinition.getCProductId(),
			cpInstance.getCPInstanceUuid(),
			commercePriceList.getCommercePriceListId(), true, null, null, null,
			null, calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR),
			calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
			calendar.get(Calendar.YEAR) - 1, calendar.get(Calendar.HOUR),
			calendar.get(Calendar.MINUTE), false, BigDecimal.TEN, false,
			BigDecimal.TEN, StringPool.BLANK, _serviceContext);

		Sku channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(), -1L,
			null);

		Price price = channelProductSku.getPrice();

		BigDecimal commercePriceEntryPrice = commercePriceEntry.getPrice();

		Assert.assertTrue(
			Objects.equals(
				price.getPromoPrice(), commercePriceEntryPrice.doubleValue()));
	}

	private void _testGetChannelProductSkuWithAnotherCPDefinition()
		throws Exception {

		CommerceCatalog commerceCatalog = _cpDefinition.getCommerceCatalog();

		CPInstance cpInstance1 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition1 = cpInstance1.getCPDefinition();

		Sku sku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition1.getCProductId(), cpInstance1.getCPInstanceId(), -1L,
			null);

		Assert.assertEquals(cpInstance1.getSku(), sku.getSku());

		CPInstance cpInstance2 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		_assertNotFound(
			() -> skuResource.getChannelProductSku(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition1.getCProductId(), cpInstance2.getCPInstanceId(),
				-1L, null));
	}

	private void _testGetChannelProductSkuWithCPDefinitionVersions()
		throws Exception {

		CommerceCatalog commerceCatalog = _cpDefinition.getCommerceCatalog();

		CPInstance cpInstance1 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition1 = cpInstance1.getCPDefinition();

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						testCompany.getCompanyId(),
						CProductVersionConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).put(
							"versionThreshold", 2
						).build())) {

			CPDefinition cpDefinition2 =
				_cpDefinitionLocalService.copyCPDefinition(
					cpDefinition1.getCPDefinitionId());

			CPInstance cpInstance2 = _cpInstanceLocalService.getCPInstance(
				cpDefinition2.getCPDefinitionId(), cpInstance1.getSku());

			_assertNotFound(
				() -> skuResource.getChannelProductSku(
					_commerceChannel.getCommerceChannelId(),
					cpDefinition1.getCProductId(),
					cpInstance2.getCPInstanceId(), -1L, null));

			_cpDefinitionLocalService.updateStatus(
				_user.getUserId(), cpDefinition2.getCPDefinitionId(),
				WorkflowConstants.STATUS_APPROVED, _serviceContext,
				Collections.emptyMap());

			Sku sku = skuResource.getChannelProductSku(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition1.getCProductId(), cpInstance1.getCPInstanceId(),
				-1L, null);

			Assert.assertEquals(cpInstance1.getSku(), sku.getSku());

			sku = skuResource.getChannelProductSku(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition1.getCProductId(), cpInstance2.getCPInstanceId(),
				-1L, null);

			Assert.assertEquals(cpInstance2.getSku(), sku.getSku());
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private void _testGetChannelProductSkuWithCurrencyCode() throws Exception {
		_serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				testCompany.getCompanyId(), RandomTestUtil.randomString());

		commerceCurrency.setRate(BigDecimal.valueOf(0.5));

		commerceCurrency = _commerceCurrencyLocalService.updateCommerceCurrency(
			commerceCurrency);

		Long channelId = testGetChannelProductSkusPage_getChannelId();
		Long productId = testGetChannelProductSkusPage_getProductId();

		Sku sku = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		CommerceCatalog commerceCatalog = _cpDefinition.getCommerceCatalog();

		CommercePriceList commercePriceList =
			CommercePriceListTestUtil.addCommercePriceList(
				commerceCatalog.getGroupId(), false, "price-list",
				RandomTestUtil.nextDouble());

		CPInstance cpInstance = _cpInstanceLocalService.getCPInstance(
			sku.getId());

		CommercePriceEntry commercePriceEntry =
			CommercePriceEntryTestUtil.addCommercePriceEntry(
				null, productId, cpInstance.getCPInstanceUuid(),
				commercePriceList.getCommercePriceListId(), BigDecimal.TEN);

		CommerceTierPriceEntryTestUtil.addCommerceTierPriceEntry(
			commercePriceEntry.getCommercePriceEntryId(), 1, 10, 1, null);

		Sku channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(), productId,
			cpInstance.getCPInstanceId(), -1L, null);

		Price price = channelProductSku.getPrice();

		Assert.assertEquals(10, price.getPrice(), 0);

		TierPrice[] tierPrices = channelProductSku.getTierPrices();

		Assert.assertEquals(10, tierPrices[0].getPrice(), 0);

		channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(), productId,
			cpInstance.getCPInstanceId(), -1L, commerceCurrency.getCode());

		price = channelProductSku.getPrice();

		BigDecimal convertedPrice = BigDecimal.valueOf(10);

		convertedPrice = convertedPrice.multiply(commerceCurrency.getRate());

		Assert.assertEquals(convertedPrice.doubleValue(), price.getPrice(), 0);

		tierPrices = channelProductSku.getTierPrices();

		Assert.assertEquals(
			convertedPrice.doubleValue(), tierPrices[0].getPrice(), 0);
	}

	private void _testGetChannelProductSkuWithDraftCPInstance()
		throws Exception {

		CommerceCatalog commerceCatalog = _cpDefinition.getCommerceCatalog();

		CPInstance cpInstance1 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition1 = cpInstance1.getCPDefinition();

		Sku sku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition1.getCProductId(), cpInstance1.getCPInstanceId(), -1L,
			null);

		Assert.assertEquals(cpInstance1.getSku(), sku.getSku());

		CPInstance cpInstance2 = CPTestUtil.addCPDefinitionCPInstance(
			cpDefinition1.getCPDefinitionId(), null);

		_cpInstanceLocalService.updateStatus(
			_user.getUserId(), cpInstance2.getCPInstanceId(),
			WorkflowConstants.STATUS_DRAFT);

		_assertNotFound(
			() -> skuResource.getChannelProductSku(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition1.getCProductId(), cpInstance2.getCPInstanceId(),
				-1L, null));
	}

	private void _testGetChannelProductSkuWithUnitOfMeasurePrice()
		throws Exception {

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				testCompany.getCompanyId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			testGroup.getCompanyId(), testGroup.getGroupId(), _user.getUserId(),
			commerceCurrency.getCode());

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId(), BigDecimal.TEN);

		String unitOfMeasureKey = RandomTestUtil.randomString();

		CPTestUtil.addCPInstanceUnitOfMeasure(
			testGroup.getGroupId(), cpInstance.getCPInstanceId(),
			unitOfMeasureKey, BigDecimal.ONE, cpInstance.getSku());

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		Sku channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(), -1L,
			null);

		Price price = channelProductSku.getPrice();

		Assert.assertNotNull(price.getPricingQuantityPriceFormatted());

		CommercePriceList catalogBaseCommercePriceList =
			CommercePriceListLocalServiceUtil.
				getCatalogBaseCommercePriceListByType(
					commerceCatalog.getGroupId(),
					CommercePriceListConstants.TYPE_PRICE_LIST);

		CommercePriceEntry commercePriceEntry =
			_commercePriceEntryLocalService.fetchCommercePriceEntry(
				catalogBaseCommercePriceList.getCommercePriceListId(),
				cpInstance.getCPInstanceUuid(), unitOfMeasureKey);

		commercePriceEntry.setPricingQuantity(null);

		commercePriceEntry =
			_commercePriceEntryLocalService.updateCommercePriceEntry(
				commercePriceEntry);

		channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(), -1L,
			null);

		Price updatedPrice = channelProductSku.getPrice();

		Assert.assertTrue(
			BigDecimalUtil.eq(
				commercePriceEntry.getPrice(),
				BigDecimal.valueOf(updatedPrice.getPrice())));
		Assert.assertEquals(
			price.getPricingQuantityPriceFormatted(),
			updatedPrice.getPricingQuantityPriceFormatted());
	}

	private void _testGetChannelProductSkusPageWithPriceListAccountRel()
		throws Exception {

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				testCompany.getCompanyId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			testGroup.getCompanyId(), testGroup.getGroupId(), _user.getUserId(),
			commerceCurrency.getCode());

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		Sku channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(), -1L,
			null);

		_addCPInstanceUnitOfMeasure(channelProductSku, true);

		Price price = channelProductSku.getPrice();

		Assert.assertTrue(
			Objects.equals(price.getPrice(), BigDecimal.ZERO.doubleValue()));

		CommercePriceList commercePriceList =
			CommercePriceListTestUtil.addCommercePriceList(
				commerceCatalog.getGroupId(), false,
				CommercePriceListConstants.TYPE_PRICE_LIST, 0.0);

		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, _user.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null,
			RandomTestUtil.randomString(), "business", 1, _serviceContext);

		_commercePriceListAccountRelLocalService.addCommercePriceListAccountRel(
			_user.getUserId(), commercePriceList.getCommercePriceListId(),
			accountEntry.getAccountEntryId(), 0, _serviceContext);

		Calendar calendar = new GregorianCalendar();

		CommercePriceEntry commercePriceEntry =
			_commercePriceEntryLocalService.addCommercePriceEntry(
				RandomTestUtil.randomString(), cpDefinition.getCProductId(),
				cpInstance.getCPInstanceUuid(),
				commercePriceList.getCommercePriceListId(), true, null, null,
				null, null, calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.HOUR),
				calendar.get(Calendar.MINUTE), false, BigDecimal.TEN, false,
				BigDecimal.TEN, StringPool.BLANK, _serviceContext);

		channelProductSku = skuResource.getChannelProductSku(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), cpInstance.getCPInstanceId(),
			accountEntry.getAccountEntryId(), null);

		price = channelProductSku.getPrice();

		BigDecimal commercePriceEntryPrice = commercePriceEntry.getPrice();

		Assert.assertTrue(
			Objects.equals(
				price.getPrice(), commercePriceEntryPrice.doubleValue()));
	}

	private void _testGetChannelProductSkusPageWithUnitOfMeasure()
		throws Exception {

		Long channelId = testGetChannelProductSkusPage_getChannelId();
		Long productId = testGetChannelProductSkusPage_getProductId();

		Sku sku1 = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		_addCPInstanceUnitOfMeasure(sku1, true);
		_addCPInstanceUnitOfMeasure(sku1, true);
		_addCPInstanceUnitOfMeasure(sku1, false);

		Sku sku2 = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		_addCPInstanceUnitOfMeasure(sku2, true);

		Sku sku3 = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		Page<Sku> page = skuResource.getChannelProductSkusPage(
			channelId, productId, null, null, Pagination.of(1, 10));

		for (Sku sku : page.getItems()) {
			SkuUnitOfMeasure[] skuUnitOfMeasures = sku.getSkuUnitOfMeasures();

			if (Objects.equals(sku.getId(), sku1.getId())) {
				Assert.assertEquals(
					Arrays.toString(skuUnitOfMeasures), 2,
					skuUnitOfMeasures.length);
			}

			if (Objects.equals(sku.getId(), sku2.getId())) {
				Assert.assertEquals(
					Arrays.toString(skuUnitOfMeasures), 1,
					skuUnitOfMeasures.length);
			}

			if (!Objects.equals(sku.getId(), sku3.getId())) {
				continue;
			}

			Assert.assertEquals(
				Arrays.toString(skuUnitOfMeasures), 0,
				skuUnitOfMeasures.length);
		}
	}

	private void _testGetChannelProductSkusPageWithUnitOfMeasurePrice()
		throws Exception {

		Long channelId = testGetChannelProductSkusPage_getChannelId();
		Long productId = testGetChannelProductSkusPage_getProductId();

		Sku sku1 = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		CommerceCatalog commerceCatalog = _cpDefinition.getCommerceCatalog();

		CommercePriceList commercePriceList =
			CommercePriceListTestUtil.addCommercePriceList(
				commerceCatalog.getGroupId(), false, "price-list",
				RandomTestUtil.nextDouble());

		CPInstance cpInstance = _cpInstanceLocalService.getCPInstance(
			sku1.getId());

		CommercePriceEntry commercePriceEntry =
			CommercePriceEntryTestUtil.addCommercePriceEntry(
				null, productId, cpInstance.getCPInstanceUuid(),
				commercePriceList.getCommercePriceListId(), BigDecimal.TEN);

		CommerceTierPriceEntryTestUtil.addCommerceTierPriceEntry(
			commercePriceEntry.getCommercePriceEntryId(), 10, 10, 1, null);
		CommerceTierPriceEntryTestUtil.addCommerceTierPriceEntry(
			commercePriceEntry.getCommercePriceEntryId(), 12, 12, 1, null);

		_addCPInstanceUnitOfMeasure(sku1, true);

		Sku sku2 = testGetChannelProductSkusPage_addSku(
			channelId, productId, randomSku());

		_addCPInstanceUnitOfMeasure(sku2, true);

		Page<Sku> page = skuResource.getChannelProductSkusPage(
			channelId, productId, null, null, Pagination.of(1, 10));

		for (Sku sku : page.getItems()) {
			SkuUnitOfMeasure[] skuUnitOfMeasures = sku.getSkuUnitOfMeasures();

			if (Objects.equals(sku.getId(), sku1.getId())) {
				Assert.assertEquals(
					Arrays.toString(skuUnitOfMeasures), 1,
					skuUnitOfMeasures.length);

				SkuUnitOfMeasure skuUnitOfMeasure = skuUnitOfMeasures[0];

				Price skuUnitOfMeasurePrice = skuUnitOfMeasure.getPrice();

				Assert.assertTrue(
					BigDecimalUtil.eq(
						commercePriceEntry.getPrice(),
						BigDecimal.valueOf(skuUnitOfMeasurePrice.getPrice())));
				Assert.assertNotNull(
					skuUnitOfMeasurePrice.getPricingQuantityPriceFormatted());

				TierPrice[] tierPrices = skuUnitOfMeasure.getTierPrices();

				Assert.assertEquals(
					Arrays.toString(tierPrices), 2, tierPrices.length);
			}

			if (!Objects.equals(sku.getId(), sku2.getId())) {
				continue;
			}

			Assert.assertEquals(
				Arrays.toString(skuUnitOfMeasures), 1,
				skuUnitOfMeasures.length);

			SkuUnitOfMeasure skuUnitOfMeasure = skuUnitOfMeasures[0];

			Assert.assertNull(skuUnitOfMeasure.getPrice());
			Assert.assertNull(skuUnitOfMeasure.getTierPrices());
		}
	}

	private void _testPostChannelProductSkuBySkuOptionWithBackOrder()
		throws Exception {

		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpDefinitionOptionRel.getCPDefinitionOptionValueRels();

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			cpDefinitionOptionValueRels.get(_cpInstances.size());

		Sku sku = _addCPInstance(randomSku());

		CPConfigurationEntry cpConfigurationEntry =
			_cpDefinition.fetchMasterCPConfigurationEntry();

		cpConfigurationEntry.setBackOrders(false);
		cpConfigurationEntry.setDisplayStockQuantity(true);

		cpConfigurationEntry =
			_cpConfigurationEntryLocalService.updateCPConfigurationEntry(
				cpConfigurationEntry);

		SkuOption[] skuOptions = {
			new SkuOption() {
				{
					skuOptionKey = _cpDefinitionOptionRel.getKey();
					skuOptionValueKey = cpDefinitionOptionValueRel.getKey();
				}
			}
		};

		Sku postSku = skuResource.postChannelProductSkuBySkuOption(
			_commerceChannel.getCommerceChannelId(),
			_cpDefinition.getCProductId(), null, null, null, null, skuOptions);

		Assert.assertEquals(sku.getId(), postSku.getId());
		Assert.assertFalse(postSku.getBackOrderAllowed());

		Availability availability = postSku.getAvailability();

		Assert.assertTrue(
			BigDecimalUtil.isZero(availability.getStockQuantity()));

		cpConfigurationEntry.setBackOrders(true);

		_cpConfigurationEntryLocalService.updateCPConfigurationEntry(
			cpConfigurationEntry);

		postSku = skuResource.postChannelProductSkuBySkuOption(
			_commerceChannel.getCommerceChannelId(),
			_cpDefinition.getCProductId(), null, null, null, null, skuOptions);

		Assert.assertTrue(postSku.getBackOrderAllowed());
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private CommercePriceEntryLocalService _commercePriceEntryLocalService;

	@Inject
	private CommercePriceListAccountRelLocalService
		_commercePriceListAccountRelLocalService;

	@Inject
	private CPConfigurationEntryLocalService _cpConfigurationEntryLocalService;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@DeleteAfterTestRun
	private CPDefinitionOptionRel _cpDefinitionOptionRel;

	@Inject
	private CPInstanceLocalService _cpInstanceLocalService;

	@Inject
	private CPInstanceUnitOfMeasureLocalService
		_cpInstanceUnitOfMeasureLocalService;

	@DeleteAfterTestRun
	private final List<CPInstance> _cpInstances = new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
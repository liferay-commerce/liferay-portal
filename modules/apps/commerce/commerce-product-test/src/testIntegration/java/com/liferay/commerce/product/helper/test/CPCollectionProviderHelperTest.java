/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.helper.test;

import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.helper.CPCollectionProviderHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalService;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceAccountGroupTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michele Vigilante
 */
@RunWith(Arquillian.class)
public class CPCollectionProviderHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), serviceContext);

		Group group = GroupTestUtil.addGroup();

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), null);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testGetCPDefinitionOptionValueRelsExcludesCPDefinitionOfCPDefinitionOptionRel()
		throws Exception {

		frutillaRule.scenario(
			"Get the values of an externally defined product option"
		).given(
			"Two products of the same category"
		).when(
			"The option values of one product are defined by the category"
		).then(
			"Only the other product is a value"
		);

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		AssetCategory assetCategory = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinition1.getCPDefinitionId(),
			cpDefinition2.getCPDefinitionId());

		CPDefinitionOptionRel cpDefinitionOptionRel = _addCPDefinitionOptionRel(
			cpDefinition1, assetCategory.getCategoryId());

		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpCollectionProviderHelper.getCPDefinitionOptionValueRels(
				_commerceChannel.getCompanyId(),
				_commerceChannel.getSiteGroupId(), cpDefinitionOptionRel,
				StringPool.BLANK, null);

		Assert.assertEquals(
			cpDefinitionOptionValueRels.toString(), 1,
			cpDefinitionOptionValueRels.size());

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			cpDefinitionOptionValueRels.get(0);

		Assert.assertEquals(
			cpDefinition2.getCProductId(),
			cpDefinitionOptionValueRel.getCProductId());
	}

	@Test
	public void testGetCPDefinitionOptionValueRelsExcludesCPDefinitionWithAccountGroupFilter()
		throws Exception {

		frutillaRule.scenario(
			"Get the values of an externally defined product option"
		).given(
			"Two products of the category, one with the account group filter"
		).when(
			"Another product's option values are defined by the category"
		).then(
			"Only the product without the account group filter is a value"
		);

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		AccountGroup accountGroup =
			CommerceAccountGroupTestUtil.addAccountGroup(
				_commerceCatalog.getGroupId());

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
			cpDefinition2.getCPDefinitionId());

		cpDefinition2.setAccountGroupFilterEnabled(true);

		cpDefinition2 = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition2);

		AssetCategory assetCategory = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinition1.getCPDefinitionId(),
			cpDefinition2.getCPDefinitionId());

		CPDefinition cpDefinition3 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		CPDefinitionOptionRel cpDefinitionOptionRel = _addCPDefinitionOptionRel(
			cpDefinition3, assetCategory.getCategoryId());

		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpCollectionProviderHelper.getCPDefinitionOptionValueRels(
				_commerceChannel.getCompanyId(),
				_commerceChannel.getSiteGroupId(), cpDefinitionOptionRel,
				StringPool.BLANK, null);

		Assert.assertEquals(
			cpDefinitionOptionValueRels.toString(), 1,
			cpDefinitionOptionValueRels.size());

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			cpDefinitionOptionValueRels.get(0);

		Assert.assertEquals(
			cpDefinition1.getCProductId(),
			cpDefinitionOptionValueRel.getCProductId());
	}

	@Test
	public void testGetCPDefinitionOptionValueRelsExcludesCPDefinitionWithoutApprovedCPInstances()
		throws Exception {

		frutillaRule.scenario(
			"Get the values of an externally defined product option"
		).given(
			"Two products of the category, one without SKUs"
		).when(
			"Another product's option values are defined by the category"
		).then(
			"Only the product with SKUs is a value"
		);

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		_cpInstanceLocalService.deleteCPInstances(
			cpDefinition2.getCPDefinitionId());

		AssetCategory assetCategory = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinition1.getCPDefinitionId(),
			cpDefinition2.getCPDefinitionId());

		CPDefinition cpDefinition3 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		CPDefinitionOptionRel cpDefinitionOptionRel = _addCPDefinitionOptionRel(
			cpDefinition3, assetCategory.getCategoryId());

		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpCollectionProviderHelper.getCPDefinitionOptionValueRels(
				_commerceChannel.getCompanyId(),
				_commerceChannel.getSiteGroupId(), cpDefinitionOptionRel,
				StringPool.BLANK, null);

		Assert.assertEquals(
			cpDefinitionOptionValueRels.toString(), 1,
			cpDefinitionOptionValueRels.size());

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			cpDefinitionOptionValueRels.get(0);

		Assert.assertEquals(
			cpDefinition1.getCProductId(),
			cpDefinitionOptionValueRel.getCProductId());
	}

	@Test
	public void testGetCPDefinitionOptionValueRelsWithMultipleAssetCategories()
		throws Exception {

		frutillaRule.scenario(
			"Get the values of an option defined by multiple categories"
		).given(
			"Two products, each of a different category"
		).when(
			"Another product's option values are defined by both categories"
		).then(
			"Both products are values"
		).and(
			"The product removed from its category is no longer a value"
		);

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		AssetCategory assetCategory1 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinition1.getCPDefinitionId());
		AssetCategory assetCategory2 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinition2.getCPDefinitionId());

		CPDefinition cpDefinition3 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		CPDefinitionOptionRel cpDefinitionOptionRel = _addCPDefinitionOptionRel(
			cpDefinition3, assetCategory1.getCategoryId(),
			assetCategory2.getCategoryId());

		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels =
			_cpCollectionProviderHelper.getCPDefinitionOptionValueRels(
				_commerceChannel.getCompanyId(),
				_commerceChannel.getSiteGroupId(), cpDefinitionOptionRel,
				StringPool.BLANK, null);

		List<Long> cProductIds = TransformUtil.transform(
			cpDefinitionOptionValueRels,
			CPDefinitionOptionValueRel::getCProductId);

		Assert.assertEquals(cProductIds.toString(), 2, cProductIds.size());
		Assert.assertTrue(cProductIds.contains(cpDefinition1.getCProductId()));
		Assert.assertTrue(cProductIds.contains(cpDefinition2.getCProductId()));

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_commerceCatalog.getGroupId());

		serviceContext.setAssetCategoryIds(new long[0]);

		_cpDefinitionLocalService.updateCPDefinitionCategorization(
			cpDefinition2.getCPDefinitionId(), serviceContext);

		cpDefinitionOptionValueRels =
			_cpCollectionProviderHelper.getCPDefinitionOptionValueRels(
				_commerceChannel.getCompanyId(),
				_commerceChannel.getSiteGroupId(), cpDefinitionOptionRel,
				StringPool.BLANK, null);

		Assert.assertEquals(
			cpDefinitionOptionValueRels.toString(), 1,
			cpDefinitionOptionValueRels.size());

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			cpDefinitionOptionValueRels.get(0);

		Assert.assertEquals(
			cpDefinition1.getCProductId(),
			cpDefinitionOptionValueRel.getCProductId());
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private CPDefinitionOptionRel _addCPDefinitionOptionRel(
			CPDefinition cpDefinition, long... assetCategoryIds)
		throws Exception {

		CPOption cpOption = CPTestUtil.addCPOption(
			_commerceCatalog.getGroupId(), false);

		return _cpDefinitionOptionRelLocalService.addCPDefinitionOptionRel(
			null, cpDefinition.getCPDefinitionId(), cpOption.getCPOptionId(),
			cpOption.getNameMap(), cpOption.getDescriptionMap(),
			cpOption.getCommerceOptionTypeKey(), _INFO_ITEM_SERVICE_KEY,
			RandomTestUtil.randomDouble(), true, false, false, false, false,
			CPConstants.PRODUCT_OPTION_PRICE_TYPE_DYNAMIC,
			UnicodePropertiesBuilder.put(
				"categoryIds", StringUtil.merge(assetCategoryIds)
			).buildString(),
			ServiceContextTestUtil.getServiceContext(
				_commerceCatalog.getGroupId()));
	}

	private static final String _INFO_ITEM_SERVICE_KEY =
		"com.liferay.commerce.product.content.web.internal.info.collection." +
			"provider." +
				"CPDefinitionOptionValueRelRelatedInfoItemCollectionProvider";

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CommerceChannel _commerceChannel;

	@Inject
	private CPCollectionProviderHelper _cpCollectionProviderHelper;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionOptionRelLocalService
		_cpDefinitionOptionRelLocalService;

	@Inject
	private CPInstanceLocalService _cpInstanceLocalService;

}
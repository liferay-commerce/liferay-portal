/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.helper.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.catalog.CPCatalogEntry;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.content.helper.CPCompareContentHelper;
import com.liferay.commerce.product.data.source.CPDataSourceResult;
import com.liferay.commerce.product.helper.CPDefinitionHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CPSpecificationOption;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionValueRelLocalService;
import com.liferay.commerce.product.service.CPDefinitionSpecificationOptionValueLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.frutilla.FrutillaRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class CPCompareContentHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.toLanguageId(LocaleUtil.US),
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId()));

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
	}

	@After
	public void tearDown() throws Exception {
		_cpDefinitionLocalService.deleteCPDefinition(_cpDefinition);
	}

	@Test
	public void testGetCPDefinitionOptionRelNames() throws Exception {
		frutillaRule.scenario(
			"Get the names of the product options to compare"
		).given(
			"A product with an option whose type has values and an option " +
				"whose type does not"
		).when(
			"The names of the product options are retrieved"
		).then(
			"Only the name of the option whose type has values is returned"
		);

		CPOption selectCPOption = CPTestUtil.addCPOption(
			_commerceCatalog.getGroupId(),
			CPConstants.PRODUCT_OPTION_SELECT_KEY, false);

		_cpOptions.add(selectCPOption);

		CPDefinitionOptionRel cpDefinitionOptionRel =
			CPTestUtil.addCPDefinitionOptionRel(
				_commerceCatalog.getGroupId(),
				_cpDefinition.getCPDefinitionId(),
				selectCPOption.getCPOptionId());

		CPOption textCPOption = CPTestUtil.addCPOption(
			_commerceCatalog.getGroupId(), CPConstants.PRODUCT_OPTION_TEXT_KEY,
			false);

		_cpOptions.add(textCPOption);

		CPTestUtil.addCPDefinitionOptionRel(
			_commerceCatalog.getGroupId(), _cpDefinition.getCPDefinitionId(),
			textCPOption.getCPOptionId());

		Assert.assertEquals(
			Collections.singleton(
				cpDefinitionOptionRel.getName(LocaleUtil.getDefault())),
			_cpCompareContentHelper.getCPDefinitionOptionRelNames(
				new CPDataSourceResult(
					Collections.singletonList(_getCPCatalogEntry()), 1),
				LocaleUtil.getDefault()));
	}

	@Test
	public void testGetCPDefinitionOptionValueRels() throws Exception {
		frutillaRule.scenario(
			"Get the values of a product option to compare"
		).given(
			"A product with an option that has two values"
		).when(
			"The values of the product option are retrieved by its name"
		).then(
			"The names of the values are returned in order of priority"
		).and(
			"Nothing is returned for an option that the product does not have"
		);

		CPOption cpOption = CPTestUtil.addCPOption(
			_commerceCatalog.getGroupId(),
			CPConstants.PRODUCT_OPTION_SELECT_KEY, false);

		_cpOptions.add(cpOption);

		CPDefinitionOptionRel cpDefinitionOptionRel =
			CPTestUtil.addCPDefinitionOptionRel(
				_commerceCatalog.getGroupId(),
				_cpDefinition.getCPDefinitionId(), cpOption.getCPOptionId());

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel1 =
			_addCPDefinitionOptionValueRel(
				cpDefinitionOptionRel.getCPDefinitionOptionRelId(), 2);
		CPDefinitionOptionValueRel cpDefinitionOptionValueRel2 =
			_addCPDefinitionOptionValueRel(
				cpDefinitionOptionRel.getCPDefinitionOptionRelId(), 1);

		CPCatalogEntry cpCatalogEntry = _getCPCatalogEntry();

		Assert.assertEquals(
			StringBundler.concat(
				cpDefinitionOptionValueRel2.getName(LocaleUtil.getDefault()),
				StringPool.COMMA_AND_SPACE,
				cpDefinitionOptionValueRel1.getName(LocaleUtil.getDefault())),
			_cpCompareContentHelper.getCPDefinitionOptionValueRels(
				cpCatalogEntry,
				cpDefinitionOptionRel.getName(LocaleUtil.getDefault()),
				LocaleUtil.getDefault()));
		Assert.assertEquals(
			StringPool.BLANK,
			_cpCompareContentHelper.getCPDefinitionOptionValueRels(
				cpCatalogEntry, RandomTestUtil.randomString(),
				LocaleUtil.getDefault()));
	}

	@Test
	public void testGetCPDefinitionSpecificationOptionValue() throws Exception {
		frutillaRule.scenario(
			"Get the value of a product specification to compare"
		).given(
			"A product with a specification value"
		).when(
			"The value of the product specification is retrieved"
		).then(
			"The specification value is returned"
		);

		_cpSpecificationOption = CPTestUtil.addCPSpecificationOption(
			_commerceCatalog.getGroupId(), false);

		Map<Locale, String> valueMap = RandomTestUtil.randomLocaleStringMap();

		_cpDefinitionSpecificationOptionValueLocalService.
			addCPDefinitionSpecificationOptionValue(
				null, _cpDefinition.getCPDefinitionId(),
				_cpSpecificationOption.getCPSpecificationOptionId(), 0,
				RandomTestUtil.randomDouble(), valueMap, true,
				ServiceContextTestUtil.getServiceContext(
					_commerceCatalog.getGroupId()));

		Assert.assertEquals(
			valueMap.get(LocaleUtil.getDefault()),
			_cpCompareContentHelper.getCPDefinitionSpecificationOptionValue(
				_cpDefinition.getCPDefinitionId(),
				_cpSpecificationOption.getCPSpecificationOptionId(),
				LocaleUtil.getDefault()));
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	private CPDefinitionOptionValueRel _addCPDefinitionOptionValueRel(
			long cpDefinitionOptionRelId, double priority)
		throws Exception {

		return _cpDefinitionOptionValueRelLocalService.
			addCPDefinitionOptionValueRel(
				null, cpDefinitionOptionRelId, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), priority,
				ServiceContextTestUtil.getServiceContext(
					_commerceCatalog.getGroupId()));
	}

	private CPCatalogEntry _getCPCatalogEntry() throws Exception {
		return _cpDefinitionHelper.getCPCatalogEntry(
			0, _commerceCatalog.getGroupId(), _cpDefinition.getCPDefinitionId(),
			LocaleUtil.getDefault(), false);
	}

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CPCompareContentHelper _cpCompareContentHelper;

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionHelper _cpDefinitionHelper;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionOptionValueRelLocalService
		_cpDefinitionOptionValueRelLocalService;

	@Inject
	private CPDefinitionSpecificationOptionValueLocalService
		_cpDefinitionSpecificationOptionValueLocalService;

	@DeleteAfterTestRun
	private final List<CPOption> _cpOptions = new ArrayList<>();

	@DeleteAfterTestRun
	private CPSpecificationOption _cpSpecificationOption;

}
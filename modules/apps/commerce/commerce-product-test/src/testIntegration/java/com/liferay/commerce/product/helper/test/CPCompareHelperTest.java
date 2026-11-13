/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.helper.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.helper.CPCompareHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
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

import java.util.Collections;

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
public class CPCompareHelperTest {

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
	public void testGetCPDefinitionIds() throws Exception {
		frutillaRule.scenario(
			"Get the products to compare from the compare cookie"
		).given(
			"A compare cookie with the ID of a product that does not exist " +
				"and the ID of a product"
		).when(
			"The IDs of the products to compare are retrieved from the cookie"
		).then(
			"Only the ID of the product is returned"
		);

		Assert.assertEquals(
			Collections.singletonList(_cpDefinition.getCPDefinitionId()),
			_cpCompareHelper.getCPDefinitionIds(
				_commerceCatalog.getGroupId(), 0,
				StringBundler.concat(
					RandomTestUtil.randomLong(), StringPool.COLON,
					_cpDefinition.getCPDefinitionId()),
				false));
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CPCompareHelper _cpCompareHelper;

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

}
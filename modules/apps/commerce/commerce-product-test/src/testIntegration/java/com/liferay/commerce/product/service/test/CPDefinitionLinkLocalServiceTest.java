/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.constants.CPDefinitionLinkTypeConstants;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionLink;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLinkLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

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
public class CPDefinitionLinkLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(
				TestPropsValues.getCompanyId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), commerceCurrency.getCode());

		_cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
		_cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
	}

	@Test
	public void testAddCPDefinitionLinkByCProductId() throws Exception {
		_cpDefinitionLinkLocalService.addCPDefinitionLinkByCProductId(
			_cpDefinition1.getCPDefinitionId(), _cpDefinition2.getCProductId(),
			1, 1, 2023, 1, 1, 2, 2, 2023, 2, 2, true, 0,
			CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE,
			ServiceContextTestUtil.getServiceContext());

		Assert.assertNotNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition1.getCPDefinitionId(),
				_cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE));
		Assert.assertNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition2.getCPDefinitionId(),
				_cpDefinition1.getCProductId(),
				CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE));

		_cpDefinitionLinkLocalService.addCPDefinitionLinkByCProductId(
			_cpDefinition1.getCPDefinitionId(), _cpDefinition2.getCProductId(),
			1, 1, 2023, 1, 1, 2, 2, 2023, 2, 2, true, 0,
			CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE,
			ServiceContextTestUtil.getServiceContext());

		Assert.assertNotNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition1.getCPDefinitionId(),
				_cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));
		Assert.assertNotNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition2.getCPDefinitionId(),
				_cpDefinition1.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));
	}

	@Test
	public void testDeleteCPDefinitionLink() throws Exception {
		CPDefinitionLink cpDefinitionLink =
			_cpDefinitionLinkLocalService.addCPDefinitionLinkByCProductId(
				_cpDefinition1.getCPDefinitionId(),
				_cpDefinition2.getCProductId(), 1, 1, 2023, 1, 1, 2, 2, 2023, 2,
				2, true, 0,
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE,
				ServiceContextTestUtil.getServiceContext());

		_cpDefinitionLinkLocalService.deleteCPDefinitionLink(cpDefinitionLink);

		Assert.assertNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition1.getCPDefinitionId(),
				_cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));
		Assert.assertNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				_cpDefinition2.getCPDefinitionId(),
				_cpDefinition1.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));
	}

	private CPDefinition _cpDefinition1;
	private CPDefinition _cpDefinition2;

	@Inject
	private CPDefinitionLinkLocalService _cpDefinitionLinkLocalService;

}
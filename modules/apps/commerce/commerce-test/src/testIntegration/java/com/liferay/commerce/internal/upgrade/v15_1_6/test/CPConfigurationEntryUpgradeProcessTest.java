/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.upgrade.v15_1_6.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.model.CPDefinitionInventory;
import com.liferay.commerce.product.constants.CPConfigurationEntrySettingConstants;
import com.liferay.commerce.product.model.CPConfigurationEntry;
import com.liferay.commerce.product.model.CPConfigurationEntrySetting;
import com.liferay.commerce.product.model.CPConfigurationList;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPConfigurationEntryLocalService;
import com.liferay.commerce.product.service.CPConfigurationEntrySettingLocalService;
import com.liferay.commerce.product.service.CPConfigurationListLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.service.CPDefinitionInventoryLocalService;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import java.math.BigDecimal;

import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lianne Louie
 */
@RunWith(Arquillian.class)
public class CPConfigurationEntryUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testUpgrade() throws Exception {
		CommerceCatalog commerceCatalog =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext());

		CPConfigurationList masterCPConfigurationList =
			_cpConfigurationListLocalService.getMasterCPConfigurationList(
				commerceCatalog.getGroupId());

		CPConfigurationList cpConfigurationList =
			_cpConfigurationListLocalService.addCPConfigurationList(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				commerceCatalog.getGroupId(),
				masterCPConfigurationList.getCPConfigurationListId(), false,
				RandomTestUtil.randomString(), 2, 1, 1, 2024, 0, 0, 0, 0, 0, 0,
				0, true, ServiceContextTestUtil.getServiceContext());

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		cpDefinition1.setUserId(RandomTestUtil.nextLong());

		cpDefinition1 = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition1);

		CPDefinitionInventory cpDefinitionInventory =
			_cpDefinitionInventoryLocalService.
				fetchCPDefinitionInventoryByCPDefinitionId(
					cpDefinition1.getCPDefinitionId());

		cpDefinitionInventory.setDisplayAvailability(true);
		cpDefinitionInventory.setMaxOrderQuantity(
			BigDecimal.valueOf(RandomTestUtil.randomInt()));

		cpDefinitionInventory =
			_cpDefinitionInventoryLocalService.updateCPDefinitionInventory(
				cpDefinitionInventory);

		long classNameId = _portal.getClassNameId(CPDefinition.class);

		_cpConfigurationEntryLocalService.deleteCPConfigurationEntries(
			classNameId, cpDefinition1.getCPDefinitionId(), true);

		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		_runUpgrade();

		EntityCacheUtil.clearCache();

		CPConfigurationEntry cpConfigurationEntry =
			_cpConfigurationEntryLocalService.fetchCPConfigurationEntry(
				classNameId, cpDefinition1.getCPDefinitionId(),
				masterCPConfigurationList.getCPConfigurationListId());

		Assert.assertTrue(cpConfigurationEntry.isDisplayAvailability());
		Assert.assertTrue(
			BigDecimalUtil.eq(
				cpDefinitionInventory.getMaxOrderQuantity(),
				cpConfigurationEntry.getMaxOrderQuantity()));
		Assert.assertEquals(
			_userLocalService.getGuestUserId(TestPropsValues.getCompanyId()),
			cpConfigurationEntry.getUserId());

		CPConfigurationEntrySetting cpConfigurationEntrySetting =
			_cpConfigurationEntrySettingLocalService.
				fetchCPConfigurationEntrySetting(
					cpConfigurationEntry.getCPConfigurationEntryId(),
					CPConfigurationEntrySettingConstants.TYPE_INDEX_IDS);

		Assert.assertEquals(
			String.valueOf(cpConfigurationList.getCPConfigurationListId()),
			cpConfigurationEntrySetting.getValue());

		List<CPConfigurationEntry> cpConfigurationEntries =
			_cpConfigurationEntryLocalService.getCPConfigurationEntries(
				classNameId, cpDefinition2.getCPDefinitionId());

		Assert.assertEquals(
			cpConfigurationEntries.toString(), 1,
			cpConfigurationEntries.size());
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private static final String _CLASS_NAME =
		"com.liferay.commerce.internal.upgrade.v15_1_6." +
			"CPConfigurationEntryUpgradeProcess";

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CPConfigurationEntryLocalService _cpConfigurationEntryLocalService;

	@Inject
	private CPConfigurationEntrySettingLocalService
		_cpConfigurationEntrySettingLocalService;

	@Inject
	private CPConfigurationListLocalService _cpConfigurationListLocalService;

	@Inject
	private CPDefinitionInventoryLocalService
		_cpDefinitionInventoryLocalService;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private Portal _portal;

	@Inject(
		filter = "(&(component.name=com.liferay.commerce.internal.upgrade.registry.CommerceServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

	@Inject
	private UserLocalService _userLocalService;

}
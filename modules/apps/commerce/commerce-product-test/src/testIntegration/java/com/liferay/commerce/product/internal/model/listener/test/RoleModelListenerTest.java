/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.model.listener.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountRole;
import com.liferay.account.service.AccountRoleLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class RoleModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testOnBeforeRemove() throws Exception {
		Group group = GroupTestUtil.addGroup();

		CommerceCatalog commerceCatalog =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		AccountRole accountRole = _accountRoleLocalService.addAccountRole(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), null, null);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), accountRole.getRoleId());

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), role.getRoleId());

		_accountRoleLocalService.deleteAccountRole(accountRole);

		Assert.assertNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					cpDefinition.getCPDefinitionId(), Role.class.getName(),
					accountRole.getRoleId()));
		Assert.assertNotNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					cpDefinition.getCPDefinitionId(), Role.class.getName(),
					role.getRoleId()));
	}

	@Inject
	private AccountRoleLocalService _accountRoleLocalService;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

}
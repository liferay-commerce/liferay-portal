/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.product.service.CPInstanceService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class CPInstanceServiceTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		CommerceCatalog commerceCatalog1 =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId()));

		_cpInstance1 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog1.getGroupId());

		CommerceCatalog commerceCatalog2 =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId()));

		_cpInstance2 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog2.getGroupId());

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_resourcePermissionLocalService.setResourcePermissions(
			TestPropsValues.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(commerceCatalog1.getCommerceCatalogId()),
			role.getRoleId(), new String[] {ActionKeys.UPDATE});

		_user = UserTestUtil.addUser();

		_userLocalService.addRoleUser(role.getRoleId(), _user);
	}

	@Test
	public void testAddOrUpdateCPInstance() throws Exception {
		String externalReferenceCode = RandomTestUtil.randomString();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user)) {

			String sku = RandomTestUtil.randomString();

			CPInstance cpInstance = _addOrUpdateCPInstance(
				_cpInstance1.getCPDefinitionId(),
				_cpInstance1.getExternalReferenceCode(),
				_cpInstance1.getGroupId(), sku);

			Assert.assertEquals(sku, cpInstance.getSku());

			Assert.assertThrows(
				PrincipalException.MustHavePermission.class,
				() -> _addOrUpdateCPInstance(
					_cpInstance1.getCPDefinitionId(),
					_cpInstance2.getExternalReferenceCode(),
					_cpInstance1.getGroupId(), RandomTestUtil.randomString()));
			Assert.assertThrows(
				PrincipalException.MustHavePermission.class,
				() -> _addOrUpdateCPInstance(
					_cpInstance2.getCPDefinitionId(), externalReferenceCode,
					_cpInstance1.getGroupId(), RandomTestUtil.randomString()));
		}

		Assert.assertNull(
			_cpInstanceLocalService.fetchCPInstanceByExternalReferenceCode(
				externalReferenceCode, TestPropsValues.getCompanyId()));

		CPInstance cpInstance = _cpInstanceLocalService.getCPInstance(
			_cpInstance2.getCPInstanceId());

		Assert.assertEquals(_cpInstance2.getSku(), cpInstance.getSku());
	}

	private CPInstance _addOrUpdateCPInstance(
			long cpDefinitionId, String externalReferenceCode, long groupId,
			String sku)
		throws Exception {

		return _cpInstanceService.addOrUpdateCPInstance(
			externalReferenceCode, cpDefinitionId, groupId, sku, null, null,
			true, null, 0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
			BigDecimal.ZERO, true, 0, 1, 2026, 0, 0, 0, 0, 0, 0, 0, true, false,
			false, 1, null, null, 0, false, 1, null, null, 0, null, false, null,
			0, 0, 0, 0, ServiceContextTestUtil.getServiceContext(groupId));
	}

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CPInstance _cpInstance1;
	private CPInstance _cpInstance2;

	@Inject
	private CPInstanceLocalService _cpInstanceLocalService;

	@Inject
	private CPInstanceService _cpInstanceService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
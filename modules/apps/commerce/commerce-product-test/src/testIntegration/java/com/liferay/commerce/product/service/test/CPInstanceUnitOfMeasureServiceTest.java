/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CPInstanceUnitOfMeasure;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureLocalService;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureService;
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
public class CPInstanceUnitOfMeasureServiceTest {

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

		CPInstance cpInstance1 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog1.getGroupId());

		_cpInstanceUnitOfMeasure1 = CPTestUtil.addCPInstanceUnitOfMeasure(
			commerceCatalog1.getGroupId(), cpInstance1.getCPInstanceId(),
			RandomTestUtil.randomString(), BigDecimal.ONE,
			cpInstance1.getSku());

		CommerceCatalog commerceCatalog2 =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId()));

		CPInstance cpInstance2 = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog2.getGroupId());

		_cpInstanceUnitOfMeasure2 = CPTestUtil.addCPInstanceUnitOfMeasure(
			commerceCatalog2.getGroupId(), cpInstance2.getCPInstanceId(),
			RandomTestUtil.randomString(), BigDecimal.ONE,
			cpInstance2.getSku());

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
	public void testUpdateCPInstanceUnitOfMeasure() throws Exception {
		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user)) {

			String key = RandomTestUtil.randomString();

			CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure =
				_updateCPInstanceUnitOfMeasure(
					_cpInstanceUnitOfMeasure1.getCPInstanceId(),
					_cpInstanceUnitOfMeasure1.getCPInstanceUnitOfMeasureId(),
					key, _cpInstanceUnitOfMeasure1.getSku());

			Assert.assertEquals(key, cpInstanceUnitOfMeasure.getKey());

			Assert.assertThrows(
				PrincipalException.MustHavePermission.class,
				() -> _updateCPInstanceUnitOfMeasure(
					_cpInstanceUnitOfMeasure1.getCPInstanceId(),
					_cpInstanceUnitOfMeasure2.getCPInstanceUnitOfMeasureId(),
					RandomTestUtil.randomString(),
					_cpInstanceUnitOfMeasure1.getSku()));
		}

		CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.getCPInstanceUnitOfMeasure(
				_cpInstanceUnitOfMeasure2.getCPInstanceUnitOfMeasureId());

		Assert.assertEquals(
			_cpInstanceUnitOfMeasure2.getKey(),
			cpInstanceUnitOfMeasure.getKey());
	}

	private CPInstanceUnitOfMeasure _updateCPInstanceUnitOfMeasure(
			long cpInstanceId, long cpInstanceUnitOfMeasureId, String key,
			String sku)
		throws Exception {

		return _cpInstanceUnitOfMeasureService.updateCPInstanceUnitOfMeasure(
			cpInstanceUnitOfMeasureId, cpInstanceId, true, BigDecimal.ONE, key,
			RandomTestUtil.randomLocaleStringMap(), 0, BigDecimal.ONE, true,
			0.0, BigDecimal.ONE, sku);
	}

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CPInstanceUnitOfMeasure _cpInstanceUnitOfMeasure1;
	private CPInstanceUnitOfMeasure _cpInstanceUnitOfMeasure2;

	@Inject
	private CPInstanceUnitOfMeasureLocalService
		_cpInstanceUnitOfMeasureLocalService;

	@Inject
	private CPInstanceUnitOfMeasureService _cpInstanceUnitOfMeasureService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
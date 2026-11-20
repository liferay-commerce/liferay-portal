/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.definitions.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.portlet.MockPortletSession;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
public class EditCProductExternalReferenceCodeMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_cpDefinition = _addCPDefinition();

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_user = UserTestUtil.addUser();

		_userLocalService.addRoleUser(_role.getRoleId(), _user);
	}

	@Test
	public void testProcessAction() throws Exception {
		_addResourcePermission(ActionKeys.VIEW);

		CProduct cProduct = _cpDefinition.getCProduct();

		String externalReferenceCode = cProduct.getExternalReferenceCode();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user)) {

			Assert.assertTrue(
				SessionErrors.contains(
					_processAction(RandomTestUtil.randomString()),
					PrincipalException.MustHavePermission.class));
		}

		cProduct = _cpDefinition.getCProduct();

		Assert.assertEquals(
			externalReferenceCode, cProduct.getExternalReferenceCode());

		_testProcessActionWithUpdatePermissionOnAnotherCatalog();

		_addResourcePermission(ActionKeys.UPDATE);

		externalReferenceCode = RandomTestUtil.randomString();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user)) {

			Assert.assertTrue(
				SessionErrors.isEmpty(_processAction(externalReferenceCode)));
		}

		cProduct = _cpDefinition.getCProduct();

		Assert.assertEquals(
			externalReferenceCode, cProduct.getExternalReferenceCode());
	}

	private CPDefinition _addCPDefinition() throws Exception {
		CommerceCatalog commerceCatalog =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId()));

		return CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
	}

	private void _addResourcePermission(String actionId) throws Exception {
		_resourcePermissionLocalService.addResourcePermission(
			TestPropsValues.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()), _role.getRoleId(),
			actionId);
	}

	private MockLiferayPortletActionRequest _processAction(
			String externalReferenceCode)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setParameter(
			"cProductId", String.valueOf(_cpDefinition.getCProductId()));
		mockLiferayPortletActionRequest.setParameter(
			"externalReferenceCode", externalReferenceCode);
		mockLiferayPortletActionRequest.setPortletSession(
			new MockPortletSession());

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		return mockLiferayPortletActionRequest;
	}

	private void _testProcessActionWithUpdatePermissionOnAnotherCatalog()
		throws Exception {

		CPDefinition cpDefinition = _addCPDefinition();

		CommerceCatalog commerceCatalog = cpDefinition.getCommerceCatalog();

		_resourcePermissionLocalService.setResourcePermissions(
			TestPropsValues.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(),
			new String[] {ActionKeys.UPDATE, ActionKeys.VIEW});

		CProduct cProduct = _cpDefinition.getCProduct();

		String externalReferenceCode = cProduct.getExternalReferenceCode();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user)) {

			Assert.assertTrue(
				SessionErrors.contains(
					_processAction(RandomTestUtil.randomString()),
					PrincipalException.MustHavePermission.class));
		}

		cProduct = _cpDefinition.getCProduct();

		Assert.assertEquals(
			externalReferenceCode, cProduct.getExternalReferenceCode());
	}

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CPDefinition _cpDefinition;

	@Inject(
		filter = "mvc.command.name=/cp_definitions/edit_c_product_external_reference_code",
		type = MVCActionCommand.class
	)
	private MVCActionCommand _mvcActionCommand;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
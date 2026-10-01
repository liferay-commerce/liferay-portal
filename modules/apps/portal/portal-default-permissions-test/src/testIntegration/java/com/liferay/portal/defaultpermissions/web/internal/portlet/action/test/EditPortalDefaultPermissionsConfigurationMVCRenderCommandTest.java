/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.defaultpermissions.configuration.manager.PortalDefaultPermissionsConfigurationManager;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.constants.MVCRenderConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.portlet.MockPortletSession;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Balazs Breier
 */
@RunWith(Arquillian.class)
public class EditPortalDefaultPermissionsConfigurationMVCRenderCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_user = UserTestUtil.addUser();

		_userLocalService.addRoleUsers(
			_role.getRoleId(), new long[] {_user.getUserId()});
	}

	@Test
	public void testRender() throws Exception {
		_assertRenderFails(
			PrincipalException.MustBeCompanyAdmin.class,
			ExtendedObjectClassDefinition.Scope.COMPANY.getValue(), _user);
		_assertRender(
			ExtendedObjectClassDefinition.Scope.COMPANY.getValue(),
			TestPropsValues.getUser());

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), Group.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_group.getGroupId()), _role.getRoleId(),
			new String[] {ActionKeys.UPDATE});

		_assertRenderFails(
			PrincipalException.MustHavePermission.class,
			ExtendedObjectClassDefinition.Scope.GROUP.getValue(), _user);

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), Group.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_group.getGroupId()), _role.getRoleId(),
			new String[] {ActionKeys.PERMISSIONS});

		_assertRender(
			ExtendedObjectClassDefinition.Scope.GROUP.getValue(), _user);
	}

	private void _assertRender(String scope, User user) throws Exception {
		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			_getMockLiferayPortletRenderRequest(scope, user);

		Assert.assertEquals(
			MVCRenderConstants.MVC_PATH_VALUE_SKIP_DISPATCH,
			_mvcRenderCommand.render(
				mockLiferayPortletRenderRequest,
				new MockLiferayPortletRenderResponse()));
		Assert.assertTrue(
			SessionErrors.isEmpty(mockLiferayPortletRenderRequest));

		PortalDefaultPermissionsConfigurationManager
			portalDefaultPermissionsConfigurationManager =
				(PortalDefaultPermissionsConfigurationManager)
					mockLiferayPortletRenderRequest.getAttribute(
						"PORTAL_DEFAULT_PERMISSIONS_CONFIGURATION_MANAGER");

		Assert.assertEquals(
			scope, portalDefaultPermissionsConfigurationManager.getScope());
	}

	private void _assertRenderFails(
			Class<? extends PrincipalException> clazz, String scope, User user)
		throws Exception {

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			_getMockLiferayPortletRenderRequest(scope, user);

		Assert.assertEquals(
			"/error.jsp",
			_mvcRenderCommand.render(
				mockLiferayPortletRenderRequest,
				new MockLiferayPortletRenderResponse()));
		Assert.assertTrue(
			SessionErrors.contains(mockLiferayPortletRenderRequest, clazz));
	}

	private MockLiferayPortletRenderRequest _getMockLiferayPortletRenderRequest(
			String scope, User user)
		throws Exception {

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.addParameter("scope", scope);
		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(user));
		mockLiferayPortletRenderRequest.setPortletSession(
			new MockPortletSession());

		return mockLiferayPortletRenderRequest;
	}

	private ThemeDisplay _getThemeDisplay(User user) throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(_group.getCompanyId()));
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(user);

		return themeDisplay;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	private Group _group;

	@Inject(
		filter = "mvc.command.name=/configuration/edit_portal_default_permissions_configuration"
	)
	private MVCRenderCommand _mvcRenderCommand;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
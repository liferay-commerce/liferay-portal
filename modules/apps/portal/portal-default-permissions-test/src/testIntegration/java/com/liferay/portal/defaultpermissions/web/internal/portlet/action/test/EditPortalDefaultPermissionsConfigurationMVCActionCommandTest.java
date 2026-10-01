/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.defaultpermissions.configuration.PortalDefaultPermissionsCompanyConfiguration;
import com.liferay.portal.defaultpermissions.configuration.PortalDefaultPermissionsGroupConfiguration;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.portlet.MockActionParameters;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.portlet.MockPortletSession;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.portlet.ActionParameters;
import jakarta.portlet.PortletException;

import java.util.Dictionary;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Balazs Breier
 */
@RunWith(Arquillian.class)
public class EditPortalDefaultPermissionsConfigurationMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		Configuration configuration = _getConfiguration(
			PortalDefaultPermissionsCompanyConfiguration.class,
			ExtendedObjectClassDefinition.Scope.COMPANY,
			TestPropsValues.getCompanyId());

		if (configuration != null) {
			_originalProperties = configuration.getProperties();
		}

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);
		_user = UserTestUtil.addUser();

		_userLocalService.addRoleUsers(
			_role.getRoleId(), new long[] {_user.getUserId()});
	}

	@After
	public void tearDown() throws Exception {
		if (_originalProperties == null) {
			_configurationProvider.deleteCompanyConfiguration(
				PortalDefaultPermissionsCompanyConfiguration.class,
				TestPropsValues.getCompanyId());
		}
		else {
			Configuration configuration = _getConfiguration(
				PortalDefaultPermissionsCompanyConfiguration.class,
				ExtendedObjectClassDefinition.Scope.COMPANY,
				TestPropsValues.getCompanyId());

			configuration.update(_originalProperties);
		}

		_configurationProvider.deleteGroupConfiguration(
			PortalDefaultPermissionsGroupConfiguration.class,
			_group.getCompanyId(), _group.getGroupId());
	}

	@Test
	public void testProcessAction() throws Exception {
		_assertProcessActionFails(
			PrincipalException.MustBeCompanyAdmin.class,
			ExtendedObjectClassDefinition.Scope.COMPANY.getValue(), _user);

		Assert.assertNull(
			_getActionIds(
				PortalDefaultPermissionsCompanyConfiguration.class,
				ExtendedObjectClassDefinition.Scope.COMPANY,
				_group.getCompanyId()));

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), Group.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_group.getGroupId()), _role.getRoleId(),
			new String[] {ActionKeys.PERMISSIONS, ActionKeys.UPDATE});

		_assertProcessActionFails(
			PrincipalException.MustBeCompanyAdmin.class,
			ExtendedObjectClassDefinition.Scope.COMPANY.getValue(), _user);

		Assert.assertNull(
			_getActionIds(
				PortalDefaultPermissionsCompanyConfiguration.class,
				ExtendedObjectClassDefinition.Scope.COMPANY,
				_group.getCompanyId()));

		_mvcActionCommand.processAction(
			_getMockLiferayPortletActionRequest(
				ExtendedObjectClassDefinition.Scope.COMPANY.getValue(),
				TestPropsValues.getUser()),
			new MockLiferayPortletActionResponse());

		Assert.assertArrayEquals(
			new String[] {ActionKeys.VIEW},
			_getActionIds(
				PortalDefaultPermissionsCompanyConfiguration.class,
				ExtendedObjectClassDefinition.Scope.COMPANY,
				_group.getCompanyId()));

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), Group.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_group.getGroupId()), _role.getRoleId(),
			new String[] {ActionKeys.UPDATE});

		_assertProcessActionFails(
			PrincipalException.MustHavePermission.class,
			ExtendedObjectClassDefinition.Scope.GROUP.getValue(), _user);

		Assert.assertNull(
			_getActionIds(
				PortalDefaultPermissionsGroupConfiguration.class,
				ExtendedObjectClassDefinition.Scope.GROUP,
				_group.getGroupId()));

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), Group.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_group.getGroupId()), _role.getRoleId(),
			new String[] {ActionKeys.PERMISSIONS});

		_mvcActionCommand.processAction(
			_getMockLiferayPortletActionRequest(
				ExtendedObjectClassDefinition.Scope.GROUP.getValue(), _user),
			new MockLiferayPortletActionResponse());

		Assert.assertArrayEquals(
			new String[] {ActionKeys.VIEW},
			_getActionIds(
				PortalDefaultPermissionsGroupConfiguration.class,
				ExtendedObjectClassDefinition.Scope.GROUP,
				_group.getGroupId()));
	}

	private void _assertProcessActionFails(
			Class<? extends PrincipalException> clazz, String scope, User user)
		throws Exception {

		try {
			_mvcActionCommand.processAction(
				_getMockLiferayPortletActionRequest(scope, user),
				new MockLiferayPortletActionResponse());

			Assert.fail();
		}
		catch (PortletException portletException) {
			Throwable throwable = portletException.getCause();

			Assert.assertTrue(clazz.isInstance(throwable));
		}
	}

	private String[] _getActionIds(
			Class<?> clazz, ExtendedObjectClassDefinition.Scope scope,
			long scopePK)
		throws Exception {

		Configuration configuration = _getConfiguration(clazz, scope, scopePK);

		if (configuration == null) {
			return null;
		}

		Dictionary<String, Object> properties = configuration.getProperties();

		String defaultPermissions = GetterUtil.getString(
			properties.get("defaultPermissions"));

		if (Validator.isNull(defaultPermissions)) {
			return null;
		}

		JSONObject jsonObject1 = _jsonFactory.createJSONObject(
			defaultPermissions);

		JSONObject jsonObject2 = jsonObject1.getJSONObject(
			Layout.class.getName());

		if (jsonObject2 == null) {
			return null;
		}

		JSONArray jsonArray = jsonObject2.getJSONArray(_role.getName());

		if (jsonArray == null) {
			return null;
		}

		return JSONUtil.toStringArray(jsonArray);
	}

	private Configuration _getConfiguration(
			Class<?> clazz, ExtendedObjectClassDefinition.Scope scope,
			long scopePK)
		throws Exception {

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			StringBundler.concat(
				"(&(", ConfigurationAdmin.SERVICE_FACTORYPID, StringPool.EQUAL,
				clazz.getName(), ".scoped)(", scope.getPropertyKey(),
				StringPool.EQUAL, scopePK, "))"));

		if (ArrayUtil.isEmpty(configurations)) {
			return null;
		}

		return configurations[0];
	}

	private MockLiferayPortletActionRequest _getMockLiferayPortletActionRequest(
			String scope, User user)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest() {

				@Override
				public ActionParameters getActionParameters() {
					return new MockActionParameters() {
						{
							parameters = getParameterMap();
						}
					};
				}

			};

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay(user));
		mockLiferayPortletActionRequest.setParameter(
			_role.getRoleId() + "_ACTION_" + ActionKeys.VIEW,
			Boolean.TRUE.toString());
		mockLiferayPortletActionRequest.setParameter(
			"modelResource", Layout.class.getName());
		mockLiferayPortletActionRequest.setParameter(
			"rolesSearchContainerPrimaryKeys",
			String.valueOf(_role.getRoleId()));
		mockLiferayPortletActionRequest.setParameter("scope", scope);
		mockLiferayPortletActionRequest.setPortletSession(
			new MockPortletSession());

		return mockLiferayPortletActionRequest;
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

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	@Inject
	private ConfigurationProvider _configurationProvider;

	private Group _group;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject(
		filter = "mvc.command.name=/configuration/edit_portal_default_permissions_configuration"
	)
	private MVCActionCommand _mvcActionCommand;

	private Dictionary<String, Object> _originalProperties;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
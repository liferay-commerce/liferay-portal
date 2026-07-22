/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.servlet.PortletServlet;
import com.liferay.portal.kernel.test.portlet.MockPortletRequest;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.contributor.MySitesExcludedGroupIdsContributor;
import com.liferay.site.manager.RecentGroupManager;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Adolfo Pérez
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class RecentGroupManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_addCMSGroup();

		_group1 = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addTypeContentLayout(_group1);

		_group2 = GroupTestUtil.addGroup();

		LayoutTestUtil.addTypeContentLayout(_group2);

		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			MySitesExcludedGroupIdsContributor.class,
			(companyId, userId) -> Collections.singletonList(
				_group2.getGroupId()),
			null);
	}

	@After
	public void tearDown() throws Exception {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	@Test
	public void testAddRecentGroup() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			_getMockHttpServletRequest();

		_recentGroupManager.addRecentGroup(
			mockHttpServletRequest,
			_groupLocalService.getGroup(
				TestPropsValues.getCompanyId(), GroupConstants.CMS));

		Assert.assertEquals(
			Collections.emptyList(),
			_recentGroupManager.getRecentGroups(mockHttpServletRequest));

		_recentGroupManager.addRecentGroup(mockHttpServletRequest, _group1);

		Assert.assertEquals(
			Collections.singletonList(_group1),
			_recentGroupManager.getRecentGroups(mockHttpServletRequest));

		_recentGroupManager.addRecentGroup(mockHttpServletRequest, _group2);

		List<Group> recentGroups = _recentGroupManager.getRecentGroups(
			mockHttpServletRequest);

		Assert.assertTrue(recentGroups.contains(_group1));
		Assert.assertFalse(recentGroups.contains(_group2));
	}

	private void _addCMSGroup() throws Exception {

		// These tests require the instance to be created with the feature
		// flag LPD-17564 enabled. On CI, feature flags are enabled on
		// demand for each test, but not during instance initialization.
		// Until the feature flag LPD-17564 is removed, we need an explicit CMS
		// group creation.

		Group group = _groupLocalService.fetchGroup(
			TestPropsValues.getCompanyId(), GroupConstants.CMS);

		if (group != null) {
			return;
		}

		Role role = _roleLocalService.fetchRole(
			TestPropsValues.getCompanyId(), RoleConstants.SITE_MEMBER);

		if (role == null) {
			_roleLocalService.addRole(
				null, TestPropsValues.getUserId(), null, 0,
				RoleConstants.SITE_MEMBER, null, null,
				RoleConstants.TYPE_REGULAR, null, null);
		}

		GroupTestUtil.addGroup(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			GroupConstants.DEFAULT_PARENT_GROUP_ID, GroupConstants.CMS);
	}

	private MockHttpServletRequest _getMockHttpServletRequest()
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockPortletRequest mockPortletRequest = new MockPortletRequest();

		mockPortletRequest.setAttribute(
			PortletServlet.PORTLET_SERVLET_REQUEST, mockHttpServletRequest);

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_REQUEST, mockPortletRequest);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLayout(_layout);
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));
		themeDisplay.setPlid(_layout.getPlid());
		themeDisplay.setSignedIn(true);
		themeDisplay.setSiteGroupId(_group1.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockPortletRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		mockHttpServletRequest.setAttribute(
			WebKeys.USER, TestPropsValues.getUser());

		return mockHttpServletRequest;
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	private Group _group1;
	private Group _group2;

	@Inject
	private GroupLocalService _groupLocalService;

	private Layout _layout;

	@Inject
	private RecentGroupManager _recentGroupManager;

	@Inject
	private RoleLocalService _roleLocalService;

	private ServiceRegistration<MySitesExcludedGroupIdsContributor>
		_serviceRegistration;

}
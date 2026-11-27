/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.web.internal.asset.display.page.portlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.exception.NoSuchCPDefinitionException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CPDisplayLayoutLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.FriendlyURLResolver;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Gianmarco Brunialti Masera
 */
@FeatureFlag("LPD-109588")
@RunWith(Arquillian.class)
public class CPDefinitionAssetDisplayPageFriendlyURLResolverTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), commerceCurrency.getCode());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(),
			TestPropsValues.getUserId(), commerceCurrency.getCode());

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.getMainFriendlyURLEntry(
				_portal.getClassNameId(CProduct.class),
				_cpDefinition.getCProductId());

		_friendlyURL =
			_friendlyURLResolver.getURLSeparator() +
				friendlyURLEntry.getUrlTitle();

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(
				_userLocalService.getGuestUser(_group.getCompanyId())));
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testGetActualURLWhenAccountGroupFilterIsEnabled()
		throws Exception {

		_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
			_cpDefinition.getCPDefinitionId(), true);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build())) {

			String actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL, actualURL.startsWith("/c/portal/login?"));

			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()));

			Assert.assertThrows(
				PrincipalException.class, () -> _getActualURL());
		}
	}

	@Test
	public void testGetActualURLWhenAccountGroupFilterIsEnabledAndFeatureFlagIsDisabled()
		throws Exception {

		_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
			_cpDefinition.getCPDefinitionId(), true);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build());
			SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112683")) {

			Assert.assertThrows(
				PrincipalException.class, () -> _getActualURL());
		}
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testGetActualURLWhenChannelFilterIsEnabled() throws Exception {
		_cpDefinitionLocalService.updateCPDefinitionChannelFilter(
			_cpDefinition.getCPDefinitionId(), true);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build())) {

			String actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL, actualURL.startsWith("/c/portal/login?"));

			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()));

			Assert.assertThrows(
				PrincipalException.class, () -> _getActualURL());
		}
	}

	@Test
	public void testGetActualURLWhenGuestBrowsingIsDisabled() throws Exception {
		CommerceTestUtil.runWithGuestBrowsingDisabled(
			_commerceChannel.getGroupId(),
			() -> {
				Assert.assertThrows(
					NoSuchCPDefinitionException.class, () -> _getActualURL());

				try (ConfigurationTemporarySwapper
						configurationTemporarySwapper =
							new ConfigurationTemporarySwapper(
								_PID,
								HashMapDictionaryBuilder.<String, Object>put(
									"promptEnabled", true
								).build())) {

					String actualURL = _getActualURL();

					Assert.assertTrue(
						actualURL, actualURL.startsWith("/c/portal/login?"));
					Assert.assertEquals(
						URLCodec.encodeURL(_getCurrentURL()),
						HttpComponentsUtil.getParameter(
							actualURL, "redirect", false));
				}
			});
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testGetActualURLWhenRoleFilterIsEnabled() throws Exception {
		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), Role.class.getName(),
			role.getRoleId());

		_cpDefinitionLocalService.updateCPDefinitionRoleFilter(
			_cpDefinition.getCPDefinitionId(), true);

		Assert.assertThrows(PrincipalException.class, () -> _getActualURL());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build())) {

			String actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL, actualURL.startsWith("/c/portal/login?"));
			Assert.assertEquals(
				URLCodec.encodeURL(_getCurrentURL()),
				HttpComponentsUtil.getParameter(actualURL, "redirect", false));

			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()));

			Assert.assertThrows(
				PrincipalException.class, () -> _getActualURL());
		}
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testGetActualURLWhenRoleFilterIsEnabledForGuests()
		throws Exception {

		Role role = _roleLocalService.getRole(
			_group.getCompanyId(), RoleConstants.GUEST);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), Role.class.getName(),
			role.getRoleId());

		_cpDefinitionLocalService.updateCPDefinitionRoleFilter(
			_cpDefinition.getCPDefinitionId(), true);

		Layout layout = LayoutTestUtil.addTypePortletLayout(_group);

		_cpDisplayLayoutLocalService.addCPDisplayLayout(
			TestPropsValues.getUserId(), _group.getGroupId(),
			CPDefinition.class, _cpDefinition.getCPDefinitionId(), null,
			layout.getUuid());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build())) {

			String actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL,
				actualURL.startsWith(_portal.getLayoutActualURL(layout, "/c")));

			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()));

			actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL,
				actualURL.startsWith(_portal.getLayoutActualURL(layout, "/c")));
		}
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testGetActualURLWhenRoleFilterIsEnabledForSignedInUsers()
		throws Exception {

		Role role = _roleLocalService.getRole(
			_group.getCompanyId(), RoleConstants.USER);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), Role.class.getName(),
			role.getRoleId());

		_cpDefinitionLocalService.updateCPDefinitionRoleFilter(
			_cpDefinition.getCPDefinitionId(), true);

		Layout layout = LayoutTestUtil.addTypePortletLayout(_group);

		_cpDisplayLayoutLocalService.addCPDisplayLayout(
			TestPropsValues.getUserId(), _group.getGroupId(),
			CPDefinition.class, _cpDefinition.getCPDefinitionId(), null,
			layout.getUuid());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"promptEnabled", true
					).build())) {

			String actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL, actualURL.startsWith("/c/portal/login?"));

			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()));

			actualURL = _getActualURL();

			Assert.assertTrue(
				actualURL,
				actualURL.startsWith(_portal.getLayoutActualURL(layout, "/c")));
		}
	}

	private String _getActualURL() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setRequestURI(_getCurrentURL());

		return _friendlyURLResolver.getActualURL(
			_group.getCompanyId(), _group.getGroupId(), false, "/c",
			_friendlyURL, Collections.emptyMap(),
			HashMapBuilder.<String, Object>put(
				"request", mockHttpServletRequest
			).build());
	}

	private String _getCurrentURL() {
		return "/web" + _group.getFriendlyURL() + _friendlyURL;
	}

	private static final String _PID =
		"com.liferay.login.web.internal.configuration.AuthLoginConfiguration";

	private CommerceChannel _commerceChannel;
	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

	@Inject
	private CPDisplayLayoutLocalService _cpDisplayLayoutLocalService;

	private String _friendlyURL;

	@Inject
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@Inject(
		filter = "component.name=com.liferay.commerce.product.content.web.internal.asset.display.page.portlet.CPDefinitionAssetDisplayPageFriendlyURLResolver"
	)
	private FriendlyURLResolver _friendlyURLResolver;

	private Group _group;

	@Inject
	private Portal _portal;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}
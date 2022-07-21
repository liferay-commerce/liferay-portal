/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.auth;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifier;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierConfiguration;
import com.liferay.portal.kernel.security.auth.verifier.AuthVerifierResult;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.impl.UserImpl;
import com.liferay.portal.security.auth.registry.AuthVerifierRegistry;
import com.liferay.portal.service.impl.UserLocalServiceImpl;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.util.PortalImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Peter Fellwock
 */
public class AuthVerifierPipelineTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_setUpAuthVerifier();
		_setUpAuthVerifierConfiguration();
		_setUpAuthVerifierRegistry();
		_setUpPortalUtil();
		_setUpUserLocalServiceUtil();
	}

	@Test
	public void testVerifyRequest() throws PortalException {
		String contextPath = "";
		String includeURLs = StringBundler.concat(
			_BASE_URL, "/regular/*,", _BASE_URL, "/legacy*");

		String legacyRequestURI = _BASE_URL + "/legacy/Hello";
		String regularRequestURI = _BASE_URL + "/regular/Hello";

		AuthVerifierResult.State expectedState =
			AuthVerifierResult.State.SUCCESS;

		_assertVerifyRequest(
			contextPath, includeURLs, legacyRequestURI, expectedState);
		_assertVerifyRequest(
			contextPath, includeURLs, regularRequestURI, expectedState);
	}

	@Test
	public void testVerifyRequestWithNonmatchingRequestURI()
		throws PortalException {

		String contextPath = "";
		String includeURLs = StringBundler.concat(
			_BASE_URL, "/regular/*,", _BASE_URL, "/legacy*");

		String requestURI = _BASE_URL + "/non/matching";

		AuthVerifierResult.State expectedState =
			AuthVerifierResult.State.UNSUCCESSFUL;

		_assertVerifyRequest(
			contextPath, includeURLs, requestURI, expectedState);
	}

	private void _assertVerifyRequest(
			String contextPath, String includeURLs, String requestURI,
			AuthVerifierResult.State expectedState)
		throws PortalException {

		AuthVerifierResult authVerifierResult = _verifyRequest(
			contextPath, includeURLs, requestURI);

		Assert.assertSame(expectedState, authVerifierResult.getState());
	}

	private void _setUpAuthVerifier() {
		AuthVerifierResult authVerifierResult = new AuthVerifierResult();

		authVerifierResult.setSettings(new HashMap<>());
		authVerifierResult.setState(AuthVerifierResult.State.SUCCESS);

		_authVerifier = (AuthVerifier)ProxyUtil.newProxyInstance(
			AuthVerifier.class.getClassLoader(),
			new Class<?>[] {AuthVerifier.class},
			(proxy, method, args) -> {
				if (Objects.equals(method.getName(), "verify")) {
					return authVerifierResult;
				}

				return null;
			});
	}

	private void _setUpAuthVerifierConfiguration() {
		Class<? extends AuthVerifier> authVerifierClass =
			_authVerifier.getClass();

		_authVerifierConfiguration = new AuthVerifierConfiguration();

		_authVerifierConfiguration.setAuthVerifierClassName(
			authVerifierClass.getName());
	}

	private void _setUpAuthVerifierRegistry() {
		ReflectionTestUtil.setFieldValue(
			AuthVerifierRegistry.class, "_serviceTrackerMap",
			new ServiceTrackerMap<String, AuthVerifier>() {

				@Override
				public void close() {
				}

				@Override
				public boolean containsKey(String key) {
					return false;
				}

				@Override
				public AuthVerifier getService(String key) {
					if (key.equals(
							_authVerifierConfiguration.
								getAuthVerifierClassName())) {

						return _authVerifier;
					}

					return null;
				}

				@Override
				public Set<String> keySet() {
					return null;
				}

				@Override
				public Collection<AuthVerifier> values() {
					return null;
				}

			});
	}

	private void _setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(
			new PortalImpl() {

				@Override
				public long getCompanyId(
					HttpServletRequest httpServletRequest) {

					return 0;
				}

			});
	}

	private void _setUpUserLocalServiceUtil() throws Exception {
		User user = new UserImpl();

		user.setStatus(WorkflowConstants.STATUS_APPROVED);

		ReflectionTestUtil.setFieldValue(
			UserLocalServiceUtil.class, "_service",
			new UserLocalServiceImpl() {

				@Override
				public User fetchUser(long userId) {
					return user;
				}

				@Override
				public long getDefaultUserId(long companyId) {
					return user.getUserId();
				}

			});
	}

	private AuthVerifierResult _verifyRequest(
			String contextPath, String includeURLs, String requestURI)
		throws PortalException {

		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		Dictionary<String, Object> propertyMap = MapUtil.singletonDictionary(
			"urls.includes", includeURLs);

		ServiceRegistration<AuthVerifier> serviceRegistration =
			bundleContext.registerService(
				AuthVerifier.class, _authVerifier, propertyMap);

		try {
			MockHttpServletRequest mockHttpServletRequest =
				new MockHttpServletRequest(new MockServletContext());

			mockHttpServletRequest.setRequestURI(requestURI);

			AccessControlContext accessControlContext =
				new AccessControlContext();

			accessControlContext.setRequest(mockHttpServletRequest);

			Properties properties = new Properties();

			properties.put("urls.includes", includeURLs);

			_authVerifierConfiguration.setProperties(properties);

			AuthVerifierPipeline authVerifierPipeline =
				new AuthVerifierPipeline(
					Collections.singletonList(_authVerifierConfiguration),
					contextPath);

			return authVerifierPipeline.verifyRequest(accessControlContext);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private static final String _BASE_URL = "/TestAuthVerifier";

	private AuthVerifier _authVerifier;
	private AuthVerifierConfiguration _authVerifierConfiguration;

}
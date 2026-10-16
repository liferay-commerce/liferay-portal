/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureSystemConfigurationUtil;
import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.digital.signature.model.DSRecipientViewDefinition;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Brian I. Kim
 */
public class DSRecipientViewDefinitionManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@AfterClass
	public static void tearDownClass() {
		_digitalSignatureConfigurationUtilMockedStatic.close();
		_digitalSignatureSystemConfigurationUtilMockedStatic.close();
	}

	@Before
	public void setUp() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		PermissionChecker permissionChecker = Mockito.mock(
			PermissionChecker.class);

		Mockito.when(
			permissionChecker.isCompanyAdmin(Mockito.anyLong())
		).thenReturn(
			true
		);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);
	}

	@After
	public void tearDown() {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testAddDSRecipientViewDefinition() throws Exception {
		_mockDSHttp(JSONUtil.put("url", "https://docusign.net"));

		Assert.assertEquals(
			"https://docusign.net",
			_dsRecipientViewDefinitionManagerImpl.addDSRecipientViewDefinition(
				1, 2, "envelopeId", new DSRecipientViewDefinition()));

		_mockDSHttp(
			JSONUtil.put(
				"errorCode", "UNKNOWN_ENVELOPE_RECIPIENT"
			).put(
				"message", "The recipient you have identified is not valid."
			));

		try {
			_dsRecipientViewDefinitionManagerImpl.addDSRecipientViewDefinition(
				1, 2, "envelopeId", new DSRecipientViewDefinition());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertEquals(
				"Unable to create a recipient view for envelope envelopeId: " +
					"The recipient you have identified is not valid.",
				portalException.getMessage());
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			Mockito.mock(DigitalSignatureConfiguration.class);

		Mockito.when(
			digitalSignatureConfiguration.environment()
		).thenReturn(
			"production"
		);

		_digitalSignatureConfigurationUtilMockedStatic.when(
			() ->
				DigitalSignatureConfigurationUtil.
					getDigitalSignatureConfiguration(1, 2)
		).thenReturn(
			digitalSignatureConfiguration
		);

		_digitalSignatureSystemConfigurationUtilMockedStatic.when(
			() -> DigitalSignatureSystemConfigurationUtil.getAppOriginURL(
				"production")
		).thenReturn(
			"https://apps.docusign.com"
		);

		User user = Mockito.mock(User.class);

		Mockito.when(
			user.getEmailAddress()
		).thenReturn(
			"test@liferay.com"
		);

		Mockito.when(
			user.getFullName()
		).thenReturn(
			"Test Test"
		);

		UserLocalService userLocalService = Mockito.mock(
			UserLocalService.class);

		Mockito.when(
			userLocalService.getUser(3)
		).thenReturn(
			user
		);

		ReflectionTestUtil.setFieldValue(
			_dsRecipientViewDefinitionManagerImpl, "_userLocalService",
			userLocalService);

		PermissionChecker permissionChecker = Mockito.mock(
			PermissionChecker.class);

		Mockito.when(
			permissionChecker.getUserId()
		).thenReturn(
			3L
		);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		DSHttp dsHttp = _mockDSHttp(
			JSONUtil.put("url", "https://docusign.net"));

		Assert.assertEquals(
			"https://docusign.net",
			_dsRecipientViewDefinitionManagerImpl.addDSRecipientViewDefinition(
				1, 2, 3, "envelopeId", "https://liferay.com"));

		ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(
			JSONObject.class);

		Mockito.verify(
			dsHttp
		).post(
			Mockito.eq(1L), Mockito.eq(2L),
			Mockito.eq("envelopes/envelopeId/views/recipient"),
			argumentCaptor.capture()
		);

		JSONObject jsonObject = argumentCaptor.getValue();

		Assert.assertEquals("3", jsonObject.getString("clientUserId"));
		Assert.assertEquals("test@liferay.com", jsonObject.getString("email"));
		Assert.assertEquals(
			JSONUtil.putAll(
				"https://liferay.com", "https://apps.docusign.com"
			).toString(),
			String.valueOf(jsonObject.getJSONArray("frameAncestors")));
		Assert.assertEquals(
			JSONUtil.putAll(
				"https://apps.docusign.com"
			).toString(),
			String.valueOf(jsonObject.getJSONArray("messageOrigins")));
		Assert.assertEquals(
			"https://apps.docusign.com", jsonObject.getString("returnUrl"));
		Assert.assertEquals("Test Test", jsonObject.getString("userName"));
	}

	private DSHttp _mockDSHttp(JSONObject jsonObject) {
		DSHttp dsHttp = Mockito.mock(DSHttp.class);

		Mockito.when(
			dsHttp.post(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(JSONObject.class))
		).thenReturn(
			jsonObject
		);

		ReflectionTestUtil.setFieldValue(
			_dsRecipientViewDefinitionManagerImpl, "_dsHttp", dsHttp);

		return dsHttp;
	}

	private static final MockedStatic<DigitalSignatureConfigurationUtil>
		_digitalSignatureConfigurationUtilMockedStatic = Mockito.mockStatic(
			DigitalSignatureConfigurationUtil.class);
	private static final MockedStatic<DigitalSignatureSystemConfigurationUtil>
		_digitalSignatureSystemConfigurationUtilMockedStatic =
			Mockito.mockStatic(DigitalSignatureSystemConfigurationUtil.class);

	private final DSRecipientViewDefinitionManagerImpl
		_dsRecipientViewDefinitionManagerImpl =
			new DSRecipientViewDefinitionManagerImpl();
	private PermissionChecker _originalPermissionChecker;

}
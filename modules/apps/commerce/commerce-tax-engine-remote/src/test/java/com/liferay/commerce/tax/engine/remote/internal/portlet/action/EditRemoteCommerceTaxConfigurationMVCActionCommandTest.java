/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.tax.engine.remote.internal.portlet.action;

import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;
import jakarta.portlet.PortletException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Fabio Monaco
 */
public class EditRemoteCommerceTaxConfigurationMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDoProcessAction() throws Exception {
		String taxValueEndpointAuthorizationToken =
			RandomTestUtil.randomString();
		String taxValueEndpointURL =
			"https://" + RandomTestUtil.randomString() + ".com";

		_processAction(taxValueEndpointAuthorizationToken, taxValueEndpointURL);

		Mockito.verify(
			_modifiableSettings
		).setValue(
			"taxValueEndpointAuthorizationToken",
			taxValueEndpointAuthorizationToken
		);

		Mockito.verify(
			_modifiableSettings
		).setValue(
			"taxValueEndpointURL", taxValueEndpointURL
		);

		Mockito.verify(
			_modifiableSettings
		).store();
	}

	@Test
	public void testDoProcessActionWhenTaxValueEndpointURLIsInvalid()
		throws Exception {

		String taxValueEndpointURL = RandomTestUtil.randomString();

		try {
			_processAction(RandomTestUtil.randomString(), taxValueEndpointURL);

			Assert.fail();
		}
		catch (PortletException portletException) {
			Assert.assertEquals(
				"Invalid URL " + taxValueEndpointURL,
				portletException.getMessage());
		}

		Mockito.verify(
			_modifiableSettings, Mockito.never()
		).store();
	}

	private void _processAction(
			String taxValueEndpointAuthorizationToken,
			String taxValueEndpointURL)
		throws Exception {

		Settings settings = Mockito.mock(Settings.class);

		Mockito.when(
			settings.getModifiableSettings()
		).thenReturn(
			_modifiableSettings
		);

		ActionRequest actionRequest = Mockito.mock(ActionRequest.class);

		Mockito.when(
			actionRequest.getParameter(
				"settings--taxValueEndpointAuthorizationToken--")
		).thenReturn(
			taxValueEndpointAuthorizationToken
		);

		Mockito.when(
			actionRequest.getParameter("settings--taxValueEndpointURL--")
		).thenReturn(
			taxValueEndpointURL
		);

		EditRemoteCommerceTaxConfigurationMVCActionCommand
			editRemoteCommerceTaxConfigurationMVCActionCommand =
				new EditRemoteCommerceTaxConfigurationMVCActionCommand();

		ReflectionTestUtil.setFieldValue(
			editRemoteCommerceTaxConfigurationMVCActionCommand, "_portal",
			Mockito.mock(Portal.class));

		try (MockedStatic<FallbackKeysSettingsUtil>
				fallbackKeysSettingsUtilMockedStatic = Mockito.mockStatic(
					FallbackKeysSettingsUtil.class)) {

			fallbackKeysSettingsUtilMockedStatic.when(
				() -> FallbackKeysSettingsUtil.getSettings(
					Mockito.any(SettingsLocator.class))
			).thenReturn(
				settings
			);

			editRemoteCommerceTaxConfigurationMVCActionCommand.doProcessAction(
				actionRequest, Mockito.mock(ActionResponse.class));
		}
	}

	private final ModifiableSettings _modifiableSettings = Mockito.mock(
		ModifiableSettings.class);

}
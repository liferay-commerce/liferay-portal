/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.avalara.tax.engine.fixed.web.internal.frontend.taglib.servlet.taglib;

import com.liferay.commerce.avalara.connector.CommerceAvalaraConnector;
import com.liferay.commerce.avalara.connector.configuration.CommerceAvalaraConnectorConfiguration;
import com.liferay.commerce.avalara.connector.constants.CommerceAvalaraWebKeys;
import com.liferay.commerce.avalara.connector.exception.CommerceAvalaraConnectionException;
import com.liferay.commerce.tax.model.CommerceTaxMethod;
import com.liferay.commerce.tax.service.CommerceTaxMethodService;
import com.liferay.frontend.taglib.servlet.taglib.util.JSPRenderer;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Fabio Monaco
 */
public class CommerceTaxMethodAvalaraScreenNavigationEntryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testRenderWhenAvalaraConnectionIsInvalid() throws Exception {
		CommerceTaxMethodAvalaraScreenNavigationEntry
			commerceTaxMethodAvalaraScreenNavigationEntry =
				new CommerceTaxMethodAvalaraScreenNavigationEntry();

		CommerceTaxMethodService commerceTaxMethodService = Mockito.mock(
			CommerceTaxMethodService.class);

		Mockito.when(
			commerceTaxMethodService.getCommerceTaxMethod(Mockito.anyLong())
		).thenReturn(
			Mockito.mock(CommerceTaxMethod.class)
		);

		ReflectionTestUtil.setFieldValue(
			commerceTaxMethodAvalaraScreenNavigationEntry,
			"_commerceTaxMethodService", commerceTaxMethodService);

		ConfigurationProvider configurationProvider = Mockito.mock(
			ConfigurationProvider.class);

		Mockito.when(
			configurationProvider.getConfiguration(
				Mockito.eq(CommerceAvalaraConnectorConfiguration.class),
				Mockito.any(SettingsLocator.class))
		).thenReturn(
			Mockito.mock(CommerceAvalaraConnectorConfiguration.class)
		);

		ReflectionTestUtil.setFieldValue(
			commerceTaxMethodAvalaraScreenNavigationEntry,
			"_configurationProvider", configurationProvider);

		CommerceAvalaraConnector commerceAvalaraConnector = Mockito.mock(
			CommerceAvalaraConnector.class);

		Mockito.doThrow(
			new CommerceAvalaraConnectionException()
		).when(
			commerceAvalaraConnector
		).verifyConnection(
			Mockito.any(), Mockito.any(), Mockito.any()
		);

		ReflectionTestUtil.setFieldValue(
			commerceTaxMethodAvalaraScreenNavigationEntry,
			"_commerceAvalaraConnector", commerceAvalaraConnector);

		ReflectionTestUtil.setFieldValue(
			commerceTaxMethodAvalaraScreenNavigationEntry, "_jspRenderer",
			Mockito.mock(JSPRenderer.class));

		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		commerceTaxMethodAvalaraScreenNavigationEntry.render(
			httpServletRequest, Mockito.mock(HttpServletResponse.class));

		Mockito.verify(
			commerceAvalaraConnector
		).verifyConnection(
			Mockito.any(), Mockito.any(), Mockito.any()
		);

		Mockito.verify(
			httpServletRequest
		).setAttribute(
			CommerceAvalaraWebKeys.CONNECTION_ESTABLISHED, Boolean.FALSE
		);
	}

}
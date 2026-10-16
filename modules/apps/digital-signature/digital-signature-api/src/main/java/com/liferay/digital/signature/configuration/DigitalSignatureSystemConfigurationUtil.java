/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.configuration;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;

import java.util.Objects;

/**
 * @author Danny Situ
 */
public class DigitalSignatureSystemConfigurationUtil {

	public static String getAccountURL(String environment) {
		DigitalSignatureSystemConfiguration
			digitalSignatureSystemConfiguration =
				_getDigitalSignatureSystemConfiguration();

		if (Objects.equals(environment, "production")) {
			return digitalSignatureSystemConfiguration.productionAccountURL();
		}

		return digitalSignatureSystemConfiguration.sandboxAccountURL();
	}

	public static String getAppOriginURL(String environment) {
		DigitalSignatureSystemConfiguration
			digitalSignatureSystemConfiguration =
				_getDigitalSignatureSystemConfiguration();

		if (Objects.equals(environment, "production")) {
			return digitalSignatureSystemConfiguration.productionAppOriginURL();
		}

		return digitalSignatureSystemConfiguration.sandboxAppOriginURL();
	}

	public static String getJSURL(String environment) {
		DigitalSignatureSystemConfiguration
			digitalSignatureSystemConfiguration =
				_getDigitalSignatureSystemConfiguration();

		if (Objects.equals(environment, "production")) {
			return digitalSignatureSystemConfiguration.productionJSURL();
		}

		return digitalSignatureSystemConfiguration.sandboxJSURL();
	}

	private static DigitalSignatureSystemConfiguration
		_getDigitalSignatureSystemConfiguration() {

		try {
			return ConfigurationProviderUtil.getSystemConfiguration(
				DigitalSignatureSystemConfiguration.class);
		}
		catch (ConfigurationException configurationException) {
			return ReflectionUtil.throwException(configurationException);
		}
	}

}
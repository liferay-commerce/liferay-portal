/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.test.util;

import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.service.CountryLocalServiceUtil;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Andrea Sbarra
 */
public class CountryTestUtil {

	public static String randomA2(long companyId) {
		return StringUtil.toUpperCase(
			RandomTestUtil.randomString(
				2,
				randomString -> {
					Country country = CountryLocalServiceUtil.fetchCountryByA2(
						companyId, StringUtil.toUpperCase(randomString));

					return country == null;
				}));
	}

	public static String randomA3(long companyId) {
		return StringUtil.toUpperCase(
			RandomTestUtil.randomString(
				3,
				randomString -> {
					Country country = CountryLocalServiceUtil.fetchCountryByA3(
						companyId, StringUtil.toUpperCase(randomString));

					return country == null;
				}));
	}

}
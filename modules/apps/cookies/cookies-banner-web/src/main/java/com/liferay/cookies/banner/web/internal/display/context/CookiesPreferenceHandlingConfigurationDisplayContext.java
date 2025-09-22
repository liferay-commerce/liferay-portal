/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.banner.web.internal.display.context;

import com.liferay.cookies.configuration.CookiesConfigurationProvider;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rachael Koestartyo
 */
public class CookiesPreferenceHandlingConfigurationDisplayContext {

	public CookiesPreferenceHandlingConfigurationDisplayContext(
		CookiesConfigurationProvider cookiesConfigurationProvider,
		ExtendedObjectClassDefinition.Scope scope, long scopePK,
		ThemeDisplay themeDisplay) {

		_cookiesConfigurationProvider = cookiesConfigurationProvider;
		_scope = scope;
		_scopePK = scopePK;
		_themeDisplay = themeDisplay;
	}

	public boolean getCookiesPreferenceHandlingDelegatedConsentMode() {
		return _cookiesConfigurationProvider.
			isCookiesPreferenceHandlingDelegatedConsentMode(_scope, _scopePK);
	}

	public boolean getCookiesPreferenceHandlingEnabled() {
		return _cookiesConfigurationProvider.isCookiesPreferenceHandlingEnabled(
			_scope, _scopePK);
	}

	public boolean getCookiesPreferenceHandlingExplicitConsentMode() {
		return _cookiesConfigurationProvider.
			isCookiesPreferenceHandlingExplicitConsentMode(_scope, _scopePK);
	}

	public List<SelectOption> getSelectOptions() {
		List<SelectOption> selectOptions = new ArrayList<>();

		String cookiesPreferenceHandlingCookieManager =
			_cookiesConfigurationProvider.
				getCookiesPreferenceHandlingCookieManager(_scope, _scopePK);

		selectOptions.add(
			new SelectOption(
				LanguageUtil.get(_themeDisplay.getLocale(), "liferay"),
				"liferay",
				cookiesPreferenceHandlingCookieManager.equals("liferay")));

		selectOptions.add(
			new SelectOption(
				LanguageUtil.get(_themeDisplay.getLocale(), "other"), "other",
				cookiesPreferenceHandlingCookieManager.equals("other")));

		return selectOptions;
	}

	private final CookiesConfigurationProvider _cookiesConfigurationProvider;
	private final ExtendedObjectClassDefinition.Scope _scope;
	private final long _scopePK;
	private final ThemeDisplay _themeDisplay;

}
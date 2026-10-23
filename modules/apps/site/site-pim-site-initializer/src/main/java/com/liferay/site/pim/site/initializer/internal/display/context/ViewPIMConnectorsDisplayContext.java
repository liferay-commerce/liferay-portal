/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.pim.site.initializer.internal.util.PIMURLUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Andrea Sbarra
 * @author Stefano Motta
 */
public class ViewPIMConnectorsDisplayContext {

	public ViewPIMConnectorsDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Map<String, String> getContextParams() {
		return HashMapBuilder.put(
			"editConnectorURL",
			URLCodec.encodeURL(
				PIMURLUtil.getEditConnectorURL("{id}", _themeDisplay))
		).put(
			"fieldMappingsURL",
			URLCodec.encodeURL(
				PIMURLUtil.getFieldMappingsURL("{id}", _themeDisplay))
		).put(
			"scheduleURL",
			URLCodec.encodeURL(
				PIMURLUtil.getConnectorScheduleURL("{id}", _themeDisplay))
		).build();
	}

	public CreationMenu getCreationMenu() {
		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					PIMURLUtil.getEditConnectorURL(
						StringPool.BLANK, _themeDisplay));
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "new"));
			}
		).build();
	}

	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			LanguageUtil.get(
				_httpServletRequest,
				"create-a-connector-to-link-the-pim-to-a-shopping-experience")
		).put(
			"title", LanguageUtil.get(_httpServletRequest, "no-connectors-yet")
		).build();
	}

	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}
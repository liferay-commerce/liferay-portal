/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Andrea Sbarra
 * @author Stefano Motta
 */
public class ViewPIMConnectorsDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetContextParams() {
		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		ThemeDisplay themeDisplay = _mockThemeDisplay();

		Mockito.when(
			httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		ViewPIMConnectorsDisplayContext viewPIMConnectorsDisplayContext =
			new ViewPIMConnectorsDisplayContext(httpServletRequest);

		Assert.assertEquals(
			HashMapBuilder.put(
				"editConnectorURL",
				URLCodec.encodeURL(
					StringBundler.concat(
						"/web/cms/edit-connector?backURL=",
						URLCodec.encodeURL(_URL_CURRENT),
						"&objectEntryId={id}"))
			).put(
				"fieldMappingsURL",
				URLCodec.encodeURL(
					StringBundler.concat(
						"/web/cms/field-mappings?backURL=",
						URLCodec.encodeURL(_URL_CURRENT),
						"&objectEntryId={id}"))
			).put(
				"scheduleURL",
				URLCodec.encodeURL(
					StringBundler.concat(
						"/o/pim/connector/schedule?backURL=",
						URLCodec.encodeURL(_URL_CURRENT),
						"&objectEntryId={id}"))
			).build(),
			viewPIMConnectorsDisplayContext.getContextParams());
	}

	@Test
	public void testGetCreationMenu() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Mockito.when(
			language.get(httpServletRequest, "new")
		).thenReturn(
			"New"
		);

		languageUtil.setLanguage(language);

		ThemeDisplay themeDisplay = _mockThemeDisplay();

		Mockito.when(
			httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		ViewPIMConnectorsDisplayContext viewPIMConnectorsDisplayContext =
			new ViewPIMConnectorsDisplayContext(httpServletRequest);

		List<DropdownItem> dropdownItems = ReflectionTestUtil.getFieldValue(
			viewPIMConnectorsDisplayContext.getCreationMenu(),
			"_primaryDropdownItems");

		Assert.assertEquals(dropdownItems.toString(), 1, dropdownItems.size());

		DropdownItem dropdownItem = dropdownItems.get(0);

		Assert.assertEquals(
			"/web/cms/edit-connector?backURL=" +
				URLCodec.encodeURL(_URL_CURRENT),
			dropdownItem.get("href"));
		Assert.assertEquals("New", dropdownItem.get("label"));
	}

	@Test
	public void testGetEmptyState() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		HttpServletRequest httpServletRequest = Mockito.mock(
			HttpServletRequest.class);

		Mockito.when(
			language.get(
				httpServletRequest,
				"create-a-connector-to-link-the-pim-to-a-shopping-experience")
		).thenReturn(
			"Create a connector to link the PIM to a shopping experience."
		);

		Mockito.when(
			language.get(httpServletRequest, "no-connectors-yet")
		).thenReturn(
			"No Connectors Yet"
		);

		languageUtil.setLanguage(language);

		ViewPIMConnectorsDisplayContext viewPIMConnectorsDisplayContext =
			new ViewPIMConnectorsDisplayContext(httpServletRequest);

		Map<String, Object> emptyState =
			viewPIMConnectorsDisplayContext.getEmptyState();

		Assert.assertEquals(
			"Create a connector to link the PIM to a shopping experience.",
			emptyState.get("description"));
		Assert.assertEquals("No Connectors Yet", emptyState.get("title"));
	}

	private ThemeDisplay _mockThemeDisplay() {
		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getFriendlyURL()
		).thenReturn(
			"/cms"
		);

		Mockito.when(
			themeDisplay.getPathFriendlyURLPublic()
		).thenReturn(
			"/web"
		);

		Mockito.when(
			themeDisplay.getScopeGroup()
		).thenReturn(
			group
		);

		Mockito.when(
			themeDisplay.getURLCurrent()
		).thenReturn(
			"/web/cms/connectors"
		);

		return themeDisplay;
	}

	private static final String _URL_CURRENT = "/web/cms/connectors";

}
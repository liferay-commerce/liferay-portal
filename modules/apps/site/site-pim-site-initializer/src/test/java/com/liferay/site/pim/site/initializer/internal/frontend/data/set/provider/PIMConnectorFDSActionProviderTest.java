/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.frontend.data.set.provider;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.pim.site.initializer.internal.frontend.data.set.model.PIMConnectorDisplay;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Tancredi Covioli
 */
public class PIMConnectorFDSActionProviderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_pimConnectorFDSActionProvider, "_language", _language);
		_httpServletRequest = Mockito.mock(HttpServletRequest.class);

		Mockito.when(
			_httpServletRequest.getParameter("editConnectorURL")
		).thenReturn(
			"/web/pim/edit-connector?objectEntryId={id}"
		);

		Mockito.when(
			_httpServletRequest.getParameter("fieldMappingsURL")
		).thenReturn(
			"/web/pim/field-mappings?objectEntryId={id}"
		);

		Mockito.when(
			_httpServletRequest.getParameter("scheduleURL")
		).thenReturn(
			"/o/pim/connector/schedule?objectEntryId={id}"
		);

		Mockito.when(
			_language.get(Mockito.eq(_httpServletRequest), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> invocationOnMock.getArgument(1)
		);
	}

	@Test
	public void testGetDropdownItems() {
		List<DropdownItem> dropdownItems =
			_pimConnectorFDSActionProvider.getDropdownItems(
				_GROUP_ID, _httpServletRequest,
				_getPIMConnectorDisplay(
					HashMapBuilder.<String, Map<String, String>>put(
						"delete", Collections.emptyMap()
					).put(
						"update", Collections.emptyMap()
					).build(),
					true, true));

		Assert.assertEquals(dropdownItems.toString(), 5, dropdownItems.size());

		DropdownItem dropdownItem = dropdownItems.get(0);

		Assert.assertEquals(
			"/web/pim/field-mappings?objectEntryId=" + _OBJECT_ENTRY_ID,
			dropdownItem.get("href"));
		Assert.assertEquals("sheets", dropdownItem.get("icon"));
		Assert.assertEquals("map-fields", dropdownItem.get("label"));
		Assert.assertEquals("fieldMappings", _getDataValue(dropdownItem, "id"));

		dropdownItem = dropdownItems.get(1);

		Assert.assertEquals(
			"/web/pim/edit-connector?objectEntryId=" + _OBJECT_ENTRY_ID,
			dropdownItem.get("href"));
		Assert.assertEquals("pencil", dropdownItem.get("icon"));
		Assert.assertEquals("edit", dropdownItem.get("label"));
		Assert.assertEquals("edit", _getDataValue(dropdownItem, "id"));

		dropdownItem = dropdownItems.get(2);

		Assert.assertEquals(
			"/o/pim/connector/execute?objectEntryId=" + _OBJECT_ENTRY_ID,
			dropdownItem.get("href"));
		Assert.assertEquals("play", dropdownItem.get("icon"));
		Assert.assertEquals("execute", dropdownItem.get("label"));
		Assert.assertEquals("async", dropdownItem.get("target"));
		Assert.assertEquals("execute", _getDataValue(dropdownItem, "id"));
		Assert.assertEquals("post", _getDataValue(dropdownItem, "method"));
		Assert.assertEquals(
			"execution-has-started-successfully-and-will-continue-in-the-" +
				"background",
			_getDataValue(dropdownItem, "successMessage"));

		dropdownItem = dropdownItems.get(3);

		Assert.assertEquals(
			"/o/pim/connector/schedule?objectEntryId=" + _OBJECT_ENTRY_ID,
			dropdownItem.get("href"));
		Assert.assertEquals("date-time", dropdownItem.get("icon"));
		Assert.assertEquals("schedule", dropdownItem.get("label"));
		Assert.assertEquals("schedule", _getDataValue(dropdownItem, "id"));

		dropdownItem = dropdownItems.get(4);

		Assert.assertEquals("text-danger", dropdownItem.get("className"));
		Assert.assertNull(dropdownItem.get("href"));
		Assert.assertEquals("trash", dropdownItem.get("icon"));
		Assert.assertEquals("delete", dropdownItem.get("label"));
		Assert.assertEquals("delete", _getDataValue(dropdownItem, "id"));
	}

	@Test
	public void testGetDropdownItemsWhenMappingIsIncomplete() {
		List<DropdownItem> dropdownItems =
			_pimConnectorFDSActionProvider.getDropdownItems(
				_GROUP_ID, _httpServletRequest,
				_getPIMConnectorDisplay(
					HashMapBuilder.<String, Map<String, String>>put(
						"delete", Collections.emptyMap()
					).put(
						"update", Collections.emptyMap()
					).build(),
					true, false));

		Assert.assertEquals(dropdownItems.toString(), 4, dropdownItems.size());
		Assert.assertEquals(
			"fieldMappings", _getDataValue(dropdownItems.get(0), "id"));
		Assert.assertEquals("edit", _getDataValue(dropdownItems.get(1), "id"));
		Assert.assertEquals(
			"schedule", _getDataValue(dropdownItems.get(2), "id"));
		Assert.assertEquals(
			"delete", _getDataValue(dropdownItems.get(3), "id"));
	}

	@Test
	public void testGetDropdownItemsWithoutPermissions() {
		List<DropdownItem> dropdownItems =
			_pimConnectorFDSActionProvider.getDropdownItems(
				_GROUP_ID, _httpServletRequest,
				_getPIMConnectorDisplay(Collections.emptyMap(), false, true));

		Assert.assertEquals(dropdownItems.toString(), 1, dropdownItems.size());

		DropdownItem dropdownItem = dropdownItems.get(0);

		Assert.assertEquals("schedule", _getDataValue(dropdownItem, "id"));
	}

	private Object _getDataValue(DropdownItem dropdownItem, String key) {
		Map<String, Object> data = (Map<String, Object>)dropdownItem.get(
			"data");

		return data.get(key);
	}

	private PIMConnectorDisplay _getPIMConnectorDisplay(
		Map<String, Map<String, String>> actions, boolean active,
		boolean mappingComplete) {

		return new PIMConnectorDisplay(
			actions, active, null, _OBJECT_ENTRY_ID, "liferay-commerce",
			"never-executed", null, mappingComplete,
			RandomTestUtil.randomString());
	}

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private static final long _OBJECT_ENTRY_ID = RandomTestUtil.randomLong();

	private HttpServletRequest _httpServletRequest;
	private final Language _language = Mockito.mock(Language.class);
	private final PIMConnectorFDSActionProvider _pimConnectorFDSActionProvider =
		new PIMConnectorFDSActionProvider();

}
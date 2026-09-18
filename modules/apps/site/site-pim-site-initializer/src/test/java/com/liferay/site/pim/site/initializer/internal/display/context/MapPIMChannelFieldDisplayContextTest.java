/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.pim.site.initializer.constants.PIMObjectFolderConstants;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

import java.util.Arrays;
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
 * @author Andrea Sbarra
 */
public class MapPIMChannelFieldDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		Mockito.when(
			language.format(
				Mockito.any(HttpServletRequest.class), Mockito.anyString(),
				Mockito.any(Object.class), Mockito.anyBoolean())
		).thenAnswer(
			invocationOnMock -> invocationOnMock.getArgument(2)
		);

		languageUtil.setLanguage(language);

		_httpServletRequest = Mockito.mock(HttpServletRequest.class);

		Mockito.when(
			_httpServletRequest.getParameter("channelField")
		).thenReturn(
			"skus[].sku"
		);

		Mockito.when(
			_httpServletRequest.getParameter("objectEntryId")
		).thenReturn(
			String.valueOf(_OBJECT_ENTRY_ID)
		);

		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getFriendlyURL()
		).thenReturn(
			"/pim"
		);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			themeDisplay.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			themeDisplay.getLocale()
		).thenReturn(
			LocaleUtil.US
		);

		Mockito.when(
			themeDisplay.getPathFriendlyURLPublic()
		).thenReturn(
			"/web"
		);

		Mockito.when(
			themeDisplay.getPathThemeSpritemap()
		).thenReturn(
			"/o/classic-theme/images/clay/icons.svg"
		);

		Mockito.when(
			themeDisplay.getScopeGroup()
		).thenReturn(
			group
		);

		Mockito.when(
			_httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);
	}

	@Test
	public void testGetReactData() throws Exception {
		_mockPIMObjectDefinition();
		_mockPIMConnectorObjectEntry(
			"{\"skus[].sku\": [{\"attribute\": \"code\", \"source\": \"\"}]}");

		Map<String, Object> reactData = _getDisplayContext().getReactData();

		Assert.assertEquals("skus[].sku", reactData.get("channelField"));
		Assert.assertEquals("/o/pim/connectors", reactData.get("apiURL"));
		Assert.assertEquals(
			"/web/pim/field-mapping?objectEntryId=" + _OBJECT_ENTRY_ID,
			reactData.get("backURL"));
		Assert.assertEquals(_OBJECT_ENTRY_ID, reactData.get("objectEntryId"));
		Assert.assertEquals(
			"/o/classic-theme/images/clay/icons.svg",
			reactData.get("spritemap"));
		Assert.assertEquals("skus[].sku", reactData.get("title"));

		JSONArray mappingsJSONArray = (JSONArray)reactData.get("mappings");

		Assert.assertEquals(
			mappingsJSONArray.toString(), 1, mappingsJSONArray.length());

		JSONObject mappingJSONObject = mappingsJSONArray.getJSONObject(0);

		Assert.assertEquals("code", mappingJSONObject.getString("attribute"));
	}

	@Test
	public void testGetReactDataAllStructuresObjectFields() throws Exception {
		_mockPIMObjectDefinition();
		_mockPIMConnectorObjectEntry("{}");

		Map<String, Object> reactData = _getDisplayContext().getReactData();

		JSONArray allStructuresObjectFieldsJSONArray = (JSONArray)reactData.get(
			"allStructuresObjectFields");

		Assert.assertEquals(
			allStructuresObjectFieldsJSONArray.toString(), 2,
			allStructuresObjectFieldsJSONArray.length());

		JSONObject objectFieldJSONObject =
			allStructuresObjectFieldsJSONArray.getJSONObject(0);

		Assert.assertEquals("Code", objectFieldJSONObject.getString("label"));
		Assert.assertEquals("code", objectFieldJSONObject.getString("name"));

		objectFieldJSONObject =
			allStructuresObjectFieldsJSONArray.getJSONObject(1);

		Assert.assertEquals("Name", objectFieldJSONObject.getString("label"));
		Assert.assertEquals("name", objectFieldJSONObject.getString("name"));
	}

	@Test
	public void testGetReactDataPIMObjectDefinitions() throws Exception {
		_mockPIMObjectDefinition();
		_mockPIMConnectorObjectEntry("{}");

		Map<String, Object> reactData = _getDisplayContext().getReactData();

		JSONArray pimObjectDefinitionsJSONArray = (JSONArray)reactData.get(
			"pimObjectDefinitions");

		Assert.assertEquals(
			pimObjectDefinitionsJSONArray.toString(), 1,
			pimObjectDefinitionsJSONArray.length());

		JSONObject pimObjectDefinitionJSONObject =
			pimObjectDefinitionsJSONArray.getJSONObject(0);

		Assert.assertEquals(
			"Base SKU", pimObjectDefinitionJSONObject.getString("label"));
		Assert.assertEquals(
			"PIMBaseSku", pimObjectDefinitionJSONObject.getString("name"));

		JSONArray objectFieldsJSONArray =
			pimObjectDefinitionJSONObject.getJSONArray("objectFields");

		Assert.assertEquals(
			objectFieldsJSONArray.toString(), 2,
			objectFieldsJSONArray.length());
	}

	@Test
	public void testGetReactDataWithMissingObjectEntry() throws Exception {
		_mockPIMObjectDefinition();

		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(_OBJECT_ENTRY_ID)
		).thenReturn(
			null
		);

		Map<String, Object> reactData = _getDisplayContext().getReactData();

		JSONObject fieldMappingJSONObject = (JSONObject)reactData.get(
			"fieldMapping");

		Assert.assertEquals(0, fieldMappingJSONObject.length());
	}

	private MapPIMChannelFieldDisplayContext _getDisplayContext() {
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getRESTContextPath()
		).thenReturn(
			"/pim/connectors"
		);

		return new MapPIMChannelFieldDisplayContext(
			_httpServletRequest, objectDefinition,
			_objectDefinitionLocalService, _objectEntryLocalService,
			_objectFieldLocalService, _objectFolderLocalService);
	}

	private ObjectField _mockObjectField(String label, String name) {
		ObjectField objectField = Mockito.mock(ObjectField.class);

		Mockito.when(
			objectField.getBusinessType()
		).thenReturn(
			ObjectFieldConstants.BUSINESS_TYPE_TEXT
		);

		Mockito.when(
			objectField.getLabel(LocaleUtil.US)
		).thenReturn(
			label
		);

		Mockito.when(
			objectField.getName()
		).thenReturn(
			name
		);

		return objectField;
	}

	private void _mockPIMConnectorObjectEntry(String fieldMapping) {
		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getValues()
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"fieldMapping", fieldMapping
			).build()
		);

		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(_OBJECT_ENTRY_ID)
		).thenReturn(
			objectEntry
		);
	}

	private void _mockPIMObjectDefinition() {
		ObjectFolder objectFolder = Mockito.mock(ObjectFolder.class);

		Mockito.when(
			objectFolder.getObjectFolderId()
		).thenReturn(
			_OBJECT_FOLDER_ID
		);

		Mockito.when(
			_objectFolderLocalService.fetchObjectFolderByExternalReferenceCode(
				PIMObjectFolderConstants.EXTERNAL_REFERENCE_CODE_PRODUCT_TYPES,
				_COMPANY_ID)
		).thenReturn(
			objectFolder
		);

		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getLabel(LocaleUtil.US)
		).thenReturn(
			"Base SKU"
		);

		Mockito.when(
			objectDefinition.getName()
		).thenReturn(
			"PIMBaseSku"
		);

		Mockito.when(
			objectDefinition.getObjectDefinitionId()
		).thenReturn(
			_OBJECT_DEFINITION_ID
		);

		Mockito.when(
			_objectDefinitionLocalService.getObjectFolderObjectDefinitions(
				_OBJECT_FOLDER_ID)
		).thenReturn(
			Collections.singletonList(objectDefinition)
		);

		List<ObjectField> objectFields = Arrays.asList(
			_mockObjectField("Name", "name"), _mockObjectField("Code", "code"));

		Mockito.when(
			_objectFieldLocalService.getObjectFields(_OBJECT_DEFINITION_ID)
		).thenReturn(
			objectFields
		);
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _OBJECT_DEFINITION_ID =
		RandomTestUtil.randomLong();

	private static final long _OBJECT_ENTRY_ID = RandomTestUtil.randomLong();

	private static final long _OBJECT_FOLDER_ID = RandomTestUtil.randomLong();

	private HttpServletRequest _httpServletRequest;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService =
		Mockito.mock(ObjectDefinitionLocalService.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);
	private final ObjectFieldLocalService _objectFieldLocalService =
		Mockito.mock(ObjectFieldLocalService.class);
	private final ObjectFolderLocalService _objectFolderLocalService =
		Mockito.mock(ObjectFolderLocalService.class);

}
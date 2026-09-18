/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.pim.site.initializer.internal.util.PIMFieldMappingUtil;
import com.liferay.site.pim.site.initializer.internal.util.PIMProductTypeUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrea Sbarra
 */
public class MapPIMChannelFieldDisplayContext {

	public MapPIMChannelFieldDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinition objectDefinition,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService,
		ObjectFieldLocalService objectFieldLocalService,
		ObjectFolderLocalService objectFolderLocalService) {

		_httpServletRequest = httpServletRequest;
		_objectDefinition = objectDefinition;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
		_objectFieldLocalService = objectFieldLocalService;
		_objectFolderLocalService = objectFolderLocalService;

		_channelField = ParamUtil.getString(httpServletRequest, "channelField");
		_objectEntryId = ParamUtil.getLong(httpServletRequest, "objectEntryId");
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Map<String, Object> getReactData() throws Exception {
		JSONObject fieldMappingJSONObject = _getFieldMappingJSONObject();

		return HashMapBuilder.<String, Object>put(
			"allStructuresObjectFields",
			_getAllStructuresObjectFieldsJSONArray()
		).put(
			"apiURL", _getAPIURL()
		).put(
			"backURL", _getBackURL()
		).put(
			"channelField", _channelField
		).put(
			"fieldMapping", fieldMappingJSONObject
		).put(
			"mappings",
			PIMFieldMappingUtil.getMappingsJSONArray(
				fieldMappingJSONObject, _channelField)
		).put(
			"objectEntryId", _objectEntryId
		).put(
			"pimObjectDefinitions", _getPIMObjectDefinitionsJSONArray()
		).put(
			"spritemap", _themeDisplay.getPathThemeSpritemap()
		).put(
			"title",
			LanguageUtil.format(
				_httpServletRequest, "edit-x", _channelField, false)
		).build();
	}

	private JSONArray _getAllStructuresObjectFieldsJSONArray()
		throws Exception {

		Map<String, String> labels = new LinkedHashMap<>();

		for (ObjectDefinition objectDefinition : _getObjectDefinitions()) {
			for (ObjectField objectField :
					PIMProductTypeUtil.getObjectFields(
						objectDefinition, _objectFieldLocalService)) {

				labels.putIfAbsent(
					objectField.getName(),
					objectField.getLabel(_themeDisplay.getLocale()));
			}
		}

		List<String> names = ListUtil.fromCollection(labels.keySet());

		names.sort(
			Comparator.comparing(labels::get, String.CASE_INSENSITIVE_ORDER));

		return JSONUtil.toJSONArray(
			names,
			name -> JSONUtil.put(
				"label", labels.get(name)
			).put(
				"name", name
			));
	}

	private String _getAPIURL() {
		if (_objectDefinition == null) {
			return StringPool.BLANK;
		}

		return "/o" + _objectDefinition.getRESTContextPath();
	}

	private String _getBackURL() {
		Group group = _themeDisplay.getScopeGroup();

		return StringBundler.concat(
			_themeDisplay.getPathFriendlyURLPublic(), group.getFriendlyURL(),
			"/field-mapping?objectEntryId=", _objectEntryId);
	}

	private JSONObject _getFieldMappingJSONObject() throws Exception {
		Map<String, Serializable> values = _getValues();

		if (values == null) {
			return JSONFactoryUtil.createJSONObject();
		}

		String fieldMapping = MapUtil.getString(values, "fieldMapping");

		if (Validator.isNull(fieldMapping)) {
			return JSONFactoryUtil.createJSONObject();
		}

		return JSONFactoryUtil.createJSONObject(fieldMapping);
	}

	private List<ObjectDefinition> _getObjectDefinitions() {
		return PIMProductTypeUtil.getObjectDefinitions(
			_themeDisplay.getCompanyId(), _objectDefinitionLocalService,
			_objectFolderLocalService);
	}

	private JSONArray _getPIMObjectDefinitionsJSONArray() throws Exception {
		return JSONUtil.toJSONArray(
			_getObjectDefinitions(),
			objectDefinition -> JSONUtil.put(
				"label", objectDefinition.getLabel(_themeDisplay.getLocale())
			).put(
				"name", objectDefinition.getName()
			).put(
				"objectFields",
				JSONUtil.toJSONArray(
					PIMProductTypeUtil.getObjectFields(
						objectDefinition, _objectFieldLocalService),
					objectField -> JSONUtil.put(
						"label", objectField.getLabel(_themeDisplay.getLocale())
					).put(
						"name", objectField.getName()
					))
			));
	}

	private Map<String, Serializable> _getValues() {
		if (_objectEntryId == 0) {
			return null;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			_objectEntryId);

		if (objectEntry == null) {
			return null;
		}

		return objectEntry.getValues();
	}

	private final String _channelField;
	private final HttpServletRequest _httpServletRequest;
	private final ObjectDefinition _objectDefinition;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final long _objectEntryId;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final ObjectFieldLocalService _objectFieldLocalService;
	private final ObjectFolderLocalService _objectFolderLocalService;
	private final ThemeDisplay _themeDisplay;

}
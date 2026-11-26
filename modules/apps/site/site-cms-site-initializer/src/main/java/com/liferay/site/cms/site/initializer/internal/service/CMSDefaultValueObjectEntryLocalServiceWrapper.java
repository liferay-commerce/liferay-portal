/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.service;

import com.liferay.batch.engine.thread.local.BatchEngineThreadLocal;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFieldSettingConstants;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.field.setting.util.ObjectFieldSettingUtil;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.model.ObjectField;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionTableUtil;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceWrapper;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.cms.site.initializer.util.CMSDefaultValueUtil;

import java.io.Serializable;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(service = ServiceWrapper.class)
public class CMSDefaultValueObjectEntryLocalServiceWrapper
	extends ObjectEntryLocalServiceWrapper {

	@Override
	public ObjectEntry addObjectEntry(
			long groupId, long userId, long objectDefinitionId,
			long objectEntryFolderId, String defaultLanguageId,
			Map<String, Serializable> values, ServiceContext serviceContext)
		throws PortalException {

		if (FeatureFlagManagerUtil.isEnabled(
				CompanyThreadLocal.getCompanyId(), "LPD-112761")) {

			ObjectDefinition objectDefinition =
				CMSDefaultValueUtil.fetchObjectDefinition(
					CompanyThreadLocal.getCompanyId());

			if ((objectDefinition != null) &&
				(objectDefinition.getObjectDefinitionId() ==
					objectDefinitionId)) {

				_validateValues(
					MapUtil.getString(values, "classExternalReferenceCode"),
					MapUtil.getString(values, "className"),
					objectDefinition.getCompanyId(), groupId, values);
			}
			else {
				_putDefaultValues(
					defaultLanguageId, groupId, objectDefinitionId,
					objectEntryFolderId, values);
			}
		}

		return super.addObjectEntry(
			groupId, userId, objectDefinitionId, objectEntryFolderId,
			defaultLanguageId, values, serviceContext);
	}

	@Override
	public ObjectEntry partialUpdateObjectEntry(
			long userId, long objectEntryId, long objectEntryFolderId,
			Map<String, Serializable> values, ServiceContext serviceContext)
		throws PortalException {

		if (FeatureFlagManagerUtil.isEnabled(
				CompanyThreadLocal.getCompanyId(), "LPD-112761")) {

			_validateValues(objectEntryId, values);
		}

		return super.partialUpdateObjectEntry(
			userId, objectEntryId, objectEntryFolderId, values, serviceContext);
	}

	@Override
	public ObjectEntry updateObjectEntry(
			long userId, long objectEntryId, long objectEntryFolderId,
			Map<String, Serializable> values, ServiceContext serviceContext)
		throws PortalException {

		if (FeatureFlagManagerUtil.isEnabled(
				CompanyThreadLocal.getCompanyId(), "LPD-112761")) {

			_validateValues(objectEntryId, values);
		}

		return super.updateObjectEntry(
			userId, objectEntryId, objectEntryFolderId, values, serviceContext);
	}

	private String _getListTypeDefinitionExternalReferenceCode(
		ObjectField objectField) {

		if (objectField.getListTypeDefinitionId() == 0) {
			return null;
		}

		ListTypeDefinition listTypeDefinition =
			_listTypeDefinitionLocalService.fetchListTypeDefinition(
				objectField.getListTypeDefinitionId());

		if (listTypeDefinition == null) {
			return null;
		}

		return listTypeDefinition.getExternalReferenceCode();
	}

	private Serializable _getValue(ObjectField objectField, Object value) {
		if (Objects.equals(
				objectField.getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

			String key = String.valueOf(value);

			if (value instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject)value;

				key = jsonObject.getString("key");
			}

			if (Validator.isNull(key)) {
				return null;
			}

			ListTypeEntry listTypeEntry =
				_listTypeEntryLocalService.fetchListTypeEntry(
					objectField.getListTypeDefinitionId(), key);

			if (listTypeEntry == null) {
				return null;
			}

			return key;
		}

		if (value instanceof JSONArray || value instanceof JSONObject ||
			!(value instanceof Serializable) || !_isValid(objectField, value)) {

			return null;
		}

		return (Serializable)value;
	}

	private boolean _isValid(ObjectField objectField, Object value) {
		if (!_isValid(objectField.getBusinessType(), value)) {
			return false;
		}

		if (!Objects.equals(
				objectField.getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT) &&
			!Objects.equals(
				objectField.getBusinessType(),
				ObjectFieldConstants.BUSINESS_TYPE_TEXT)) {

			return true;
		}

		String valueString = (String)value;

		if (valueString.length() <= GetterUtil.getInteger(
				ObjectFieldSettingUtil.getValue(
					ObjectFieldSettingConstants.NAME_MAX_LENGTH, objectField),
				DynamicObjectDefinitionTableUtil.getMaxLength(
					objectField.getBusinessType()))) {

			return true;
		}

		return false;
	}

	private boolean _isValid(String businessType, Object value) {
		if (Objects.equals(
				businessType, ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN)) {

			return value instanceof Boolean;
		}

		if (Objects.equals(
				businessType, ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

			if (value instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject)value;

				return Validator.isNotNull(jsonObject.getString("key"));
			}

			if ((value instanceof String) &&
				Validator.isNotNull((String)value)) {

				return true;
			}

			return false;
		}

		if (Objects.equals(
				businessType, ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT) ||
			Objects.equals(
				businessType, ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT) ||
			Objects.equals(
				businessType, ObjectFieldConstants.BUSINESS_TYPE_TEXT)) {

			return value instanceof String;
		}

		if (value instanceof Boolean || value instanceof JSONArray ||
			value instanceof JSONObject) {

			return false;
		}

		try {
			String valueString = String.valueOf(value);

			if (Objects.equals(
					businessType, ObjectFieldConstants.BUSINESS_TYPE_DATE) ||
				Objects.equals(
					businessType,
					ObjectFieldConstants.BUSINESS_TYPE_DATE_TIME)) {

				DateUtil.parseDate(
					ObjectFieldUtil.getDateTimePattern(valueString),
					valueString, LocaleUtil.getSiteDefault());
			}
			else if (Objects.equals(
						businessType,
						ObjectFieldConstants.BUSINESS_TYPE_DECIMAL) ||
					 Objects.equals(
						 businessType,
						 ObjectFieldConstants.
							 BUSINESS_TYPE_PRECISION_DECIMAL)) {

				new BigDecimal(valueString);
			}
			else if (Objects.equals(
						businessType,
						ObjectFieldConstants.BUSINESS_TYPE_INTEGER)) {

				Integer.parseInt(valueString);
			}
			else if (Objects.equals(
						businessType,
						ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER)) {

				Long.parseLong(valueString);
			}

			return true;
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return false;
		}
	}

	private void _putDefaultValues(
			String defaultLanguageId, long groupId, long objectDefinitionId,
			long objectEntryFolderId, Map<String, Serializable> values)
		throws PortalException {

		if ((groupId == 0) || BatchEngineThreadLocal.isBatchImportInProcess() ||
			ExportImportThreadLocal.isImportInProcess()) {

			return;
		}

		DepotEntry depotEntry = _depotEntryLocalService.fetchGroupDepotEntry(
			groupId);

		if ((depotEntry == null) ||
			(depotEntry.getType() != DepotConstants.TYPE_SPACE)) {

			return;
		}

		Map<String, JSONObject> defaultValues =
			CMSDefaultValueUtil.getInheritedDefaultValues(
				_filterFactory, groupId, objectEntryFolderId);

		if (defaultValues.isEmpty()) {
			return;
		}

		for (ObjectField objectField :
				_objectFieldLocalService.getObjectFields(objectDefinitionId)) {

			if (objectField.isMetadata() ||
				!ArrayUtil.contains(
					_BUSINESS_TYPES, objectField.getBusinessType()) ||
				(values.get(objectField.getName()) != null) ||
				(objectField.isLocalized() &&
				 (values.get(objectField.getI18nObjectFieldName()) != null))) {

				continue;
			}

			JSONObject jsonObject = defaultValues.get(
				CMSDefaultValueUtil.getObjectFieldKey(
					_getListTypeDefinitionExternalReferenceCode(objectField),
					objectField.getName(), objectField.getBusinessType()));

			if (jsonObject == null) {
				continue;
			}

			_putValue(
				defaultLanguageId, groupId, objectField,
				jsonObject.get("value"), values);
		}
	}

	private void _putValue(
		String defaultLanguageId, long groupId, ObjectField objectField,
		Object value, Map<String, Serializable> values) {

		if (!objectField.isLocalized() || !(value instanceof JSONObject)) {
			Serializable serializable = _getValue(objectField, value);

			if (serializable != null) {
				values.put(objectField.getName(), serializable);
			}

			return;
		}

		JSONObject jsonObject = (JSONObject)value;

		Map<String, Serializable> localizedValues = new HashMap<>();

		for (String languageId : jsonObject.keySet()) {
			if (!_language.isAvailableLocale(groupId, languageId)) {
				continue;
			}

			Serializable serializable = _getValue(
				objectField, jsonObject.get(languageId));

			if (serializable == null) {
				continue;
			}

			localizedValues.put(languageId, serializable);
		}

		if (localizedValues.isEmpty()) {
			return;
		}

		if (Validator.isNull(defaultLanguageId)) {
			defaultLanguageId = LocaleUtil.toLanguageId(
				LocaleUtil.getSiteDefault());
		}

		Serializable defaultValue = localizedValues.get(defaultLanguageId);

		if (defaultValue != null) {
			values.put(objectField.getName(), defaultValue);
		}

		values.put(
			objectField.getI18nObjectFieldName(),
			(Serializable)localizedValues);
	}

	private void _validateDefaultValues(
			Serializable defaultValues, long groupId)
		throws PortalException {

		JSONArray jsonArray = null;

		if (defaultValues instanceof String) {
			try {
				jsonArray = _jsonFactory.createJSONArray((String)defaultValues);
			}
			catch (JSONException jsonException) {
				if (_log.isDebugEnabled()) {
					_log.debug(jsonException);
				}
			}
		}

		if (jsonArray == null) {
			throw new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues");
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			Object item = jsonArray.get(i);

			if (!(item instanceof JSONObject)) {
				throw new ObjectEntryValuesException.InvalidValue(
					"Each default value must be a JSON object",
					"defaultValues");
			}

			JSONObject jsonObject = (JSONObject)item;

			String name = jsonObject.getString("name");
			String type = jsonObject.getString("type");

			if (Validator.isNull(name) || Validator.isNull(type)) {
				throw new ObjectEntryValuesException.InvalidValue(
					"Each default value must have a name and a type",
					"defaultValues");
			}

			if (!ArrayUtil.contains(_BUSINESS_TYPES, type) ||
				jsonObject.isNull("value")) {

				continue;
			}

			if (Objects.equals(
					type, ObjectFieldConstants.BUSINESS_TYPE_PICKLIST) &&
				Validator.isNull(
					jsonObject.getString("listTypeDefinitionERC"))) {

				throw new ObjectEntryValuesException.InvalidValue(
					StringBundler.concat(
						"Default value \"", name,
						"\" must have a list type definition external ",
						"reference code"),
					"defaultValues");
			}

			Object value = jsonObject.get("value");

			if (!(value instanceof JSONObject)) {
				_validateValue(name, type, value);

				continue;
			}

			JSONObject valueJSONObject = (JSONObject)value;

			if (Objects.equals(
					type, ObjectFieldConstants.BUSINESS_TYPE_PICKLIST) &&
				valueJSONObject.has("key")) {

				_validateValue(name, type, valueJSONObject);

				continue;
			}

			for (String languageId : valueJSONObject.keySet()) {
				if (!_language.isAvailableLocale(groupId, languageId)) {
					throw new ObjectEntryValuesException.InvalidLanguageId(
						languageId, "defaultValues");
				}

				if (valueJSONObject.isNull(languageId)) {
					continue;
				}

				_validateValue(name, type, valueJSONObject.get(languageId));
			}
		}
	}

	private void _validateValue(String name, String type, Object value)
		throws PortalException {

		if ((value instanceof String) && Validator.isNull((String)value)) {
			return;
		}

		if (Objects.equals(type, ObjectFieldConstants.BUSINESS_TYPE_PICKLIST) &&
			(value instanceof JSONObject)) {

			JSONObject jsonObject = (JSONObject)value;

			if (Validator.isNull(jsonObject.getString("key"))) {
				return;
			}
		}

		if (!_isValid(type, value)) {
			throw new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Default value \"", name, "\" is not a valid ", type,
					" value"),
				"defaultValues");
		}
	}

	private void _validateValues(
			long objectEntryId, Map<String, Serializable> values)
		throws PortalException {

		ObjectEntry objectEntry = getObjectEntry(objectEntryId);

		ObjectDefinition objectDefinition =
			CMSDefaultValueUtil.fetchObjectDefinition(
				objectEntry.getCompanyId());

		if ((objectDefinition == null) ||
			(objectDefinition.getObjectDefinitionId() !=
				objectEntry.getObjectDefinitionId())) {

			return;
		}

		Map<String, Serializable> objectEntryValues = objectEntry.getValues();

		_validateValues(
			MapUtil.getString(
				values, "classExternalReferenceCode",
				MapUtil.getString(
					objectEntryValues, "classExternalReferenceCode")),
			MapUtil.getString(
				values, "className",
				MapUtil.getString(objectEntryValues, "className")),
			objectEntry.getCompanyId(), objectEntry.getGroupId(), values);
	}

	private void _validateValues(
			String classExternalReferenceCode, String className, long companyId,
			long groupId, Map<String, Serializable> values)
		throws PortalException {

		values.put("treePath", StringPool.BLANK);

		if (Objects.equals(className, DepotEntry.class.getName())) {
			Group group = _groupLocalService.getGroup(groupId);

			if (ExportImportThreadLocal.isImportInProcess()) {
				values.put(
					"classExternalReferenceCode",
					group.getExternalReferenceCode());
			}
			else if (!Objects.equals(
						classExternalReferenceCode,
						group.getExternalReferenceCode())) {

				throw new ObjectEntryValuesException.InvalidValue(
					"Class external reference code must be the external " +
						"reference code of the space",
					"classExternalReferenceCode");
			}
		}
		else if (Objects.equals(className, ObjectEntryFolder.class.getName())) {
			ObjectEntryFolder objectEntryFolder =
				_objectEntryFolderLocalService.
					fetchObjectEntryFolderByExternalReferenceCode(
						classExternalReferenceCode, groupId, companyId);

			if (objectEntryFolder != null) {
				values.put("treePath", objectEntryFolder.getTreePath());
			}
			else if (!BatchEngineThreadLocal.isBatchImportInProcess() &&
					 !ExportImportThreadLocal.isImportInProcess()) {

				throw new ObjectEntryValuesException.InvalidValue(
					StringBundler.concat(
						"No folder exists with external reference code \"",
						classExternalReferenceCode, "\" in the space"),
					"classExternalReferenceCode");
			}
		}
		else {
			throw new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Class name must be ", DepotEntry.class.getName(), " or ",
					ObjectEntryFolder.class.getName()),
				"className");
		}

		Serializable defaultValues = values.get("defaultValues");

		if (defaultValues != null) {
			_validateDefaultValues(defaultValues, groupId);
		}
	}

	private static final String[] _BUSINESS_TYPES = {
		ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN,
		ObjectFieldConstants.BUSINESS_TYPE_DATE,
		ObjectFieldConstants.BUSINESS_TYPE_DATE_TIME,
		ObjectFieldConstants.BUSINESS_TYPE_DECIMAL,
		ObjectFieldConstants.BUSINESS_TYPE_INTEGER,
		ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER,
		ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT,
		ObjectFieldConstants.BUSINESS_TYPE_PICKLIST,
		ObjectFieldConstants.BUSINESS_TYPE_PRECISION_DECIMAL,
		ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT,
		ObjectFieldConstants.BUSINESS_TYPE_TEXT
	};

	private static final Log _log = LogFactoryUtil.getLog(
		CMSDefaultValueObjectEntryLocalServiceWrapper.class);

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private ListTypeDefinitionLocalService _listTypeDefinitionLocalService;

	@Reference
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

}
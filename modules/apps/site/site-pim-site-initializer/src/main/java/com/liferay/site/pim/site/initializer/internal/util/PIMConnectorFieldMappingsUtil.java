/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryLocalServiceUtil;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.service.ObjectFolderLocalServiceUtil;
import com.liferay.object.service.ObjectRelationshipLocalServiceUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.pim.site.initializer.connector.PIMConnector;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorChannelField;
import com.liferay.site.pim.site.initializer.constants.PIMObjectDefinitionConstants;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Stefano Motta
 */
public class PIMConnectorFieldMappingsUtil {

	public static final String TYPE_FIXED_VALUE = "fixedValue";

	public static ListTypeEntry fetchListTypeEntry(
		ObjectDefinition objectDefinition, ObjectEntry objectEntry,
		Map<String, Serializable> values) {

		if (isFixedValue(objectEntry)) {
			return null;
		}

		return fetchListTypeEntry(
			objectDefinition,
			MapUtil.getString(objectEntry.getValues(), "sourceFieldName"),
			values);
	}

	public static ListTypeEntry fetchListTypeEntry(
		ObjectDefinition objectDefinition, String objectFieldName,
		Map<String, Serializable> values) {

		String value = MapUtil.getString(values, objectFieldName);

		if (Validator.isNull(value)) {
			return null;
		}

		ObjectField objectField = ObjectFieldLocalServiceUtil.fetchObjectField(
			objectDefinition.getObjectDefinitionId(), objectFieldName);

		if ((objectField == null) ||
			!objectField.compareBusinessType(
				ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

			return null;
		}

		return ListTypeEntryLocalServiceUtil.fetchListTypeEntry(
			objectField.getListTypeDefinitionId(), value);
	}

	public static List<ObjectEntry> filterObjectEntries(
		ObjectDefinition objectDefinition,
		List<ObjectEntry> pimFieldMappingObjectEntries) {

		if (ListUtil.isEmpty(pimFieldMappingObjectEntries)) {
			return Collections.emptyList();
		}

		List<ObjectEntry> objectEntries = ListUtil.filter(
			pimFieldMappingObjectEntries,
			objectEntry -> Objects.equals(
				objectDefinition.getClassName(),
				MapUtil.getString(objectEntry.getValues(), "sourceClassName")));

		if (!objectEntries.isEmpty()) {
			return objectEntries;
		}

		return ListUtil.filter(
			pimFieldMappingObjectEntries,
			objectEntry -> Validator.isNull(
				MapUtil.getString(objectEntry.getValues(), "sourceClassName")));
	}

	public static String getAPIURL(long companyId) {
		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_CONNECTOR_FIELD_MAPPING,
					companyId);

		if (objectDefinition == null) {
			return StringPool.BLANK;
		}

		return "/o" + objectDefinition.getRESTContextPath();
	}

	public static Object getChannelFieldValue(
			ObjectDefinition objectDefinition,
			PIMConnectorChannelField pimConnectorChannelField,
			Map<String, List<ObjectEntry>> pimFieldMappingObjectEntriesMap,
			Map<String, ObjectEntry> pimFieldMappingRuleObjectEntriesMap,
			ObjectEntry pimProductObjectEntry, Map<String, Serializable> values)
		throws PortalException {

		ObjectEntry pimFieldMappingRuleObjectEntry =
			pimFieldMappingRuleObjectEntriesMap.get(
				pimConnectorChannelField.getName());

		List<String> channelFieldValues = new ArrayList<>();

		for (ObjectEntry pimFieldMappingObjectEntry :
				filterObjectEntries(
					objectDefinition,
					pimFieldMappingObjectEntriesMap.get(
						pimConnectorChannelField.getName()))) {

			for (String value :
					getValues(
						objectDefinition, pimFieldMappingObjectEntry,
						pimProductObjectEntry, values)) {

				if (pimConnectorChannelField.isMultiple()) {
					value = PIMConnectorFieldMappingRulesUtil.applyRules(
						pimFieldMappingRuleObjectEntry, value);
				}

				if (Validator.isNotNull(value)) {
					channelFieldValues.add(value);
				}
				else if (pimConnectorChannelField.isMultiple() &&
						 isRepeatableGroupPath(pimFieldMappingObjectEntry)) {

					channelFieldValues.add(StringPool.BLANK);
				}
			}
		}

		if (channelFieldValues.isEmpty()) {
			return null;
		}

		String type = pimConnectorChannelField.getType();

		if (pimConnectorChannelField.isMultiple()) {
			return JSONUtil.putAll(
				TransformUtil.transformToArray(
					channelFieldValues,
					channelFieldValue -> {
						if (Validator.isNull(channelFieldValue)) {
							return StringPool.BLANK;
						}

						return _toObject(
							pimConnectorChannelField, channelFieldValue);
					},
					Object.class));
		}

		if (Objects.equals(type, ObjectFieldConstants.BUSINESS_TYPE_TEXT)) {
			String value = channelFieldValues.get(0);

			if ((pimFieldMappingRuleObjectEntry == null) ||
				!MapUtil.getBoolean(
					pimFieldMappingRuleObjectEntry.getValues(),
					"useFirstNonemptySource")) {

				value = StringUtil.merge(channelFieldValues, StringPool.SPACE);
			}

			value = PIMConnectorFieldMappingRulesUtil.applyRules(
				pimFieldMappingRuleObjectEntry, value);

			if (Validator.isNull(value)) {
				return null;
			}

			return value;
		}

		return _toObject(pimConnectorChannelField, channelFieldValues.get(0));
	}

	public static String getLabel(
		ObjectDefinition objectDefinition, Locale locale,
		String sourceFieldName) {

		List<String> sourceFieldPath = getSourceFieldPath(sourceFieldName);

		List<ObjectRelationship> objectRelationships = getObjectRelationships(
			objectDefinition, sourceFieldPath);

		if (objectRelationships == null) {
			return StringPool.BLANK;
		}

		List<String> labels = new ArrayList<>();

		ObjectDefinition leafObjectDefinition = objectDefinition;
		long parentObjectFieldId = 0;

		for (ObjectRelationship objectRelationship : objectRelationships) {
			leafObjectDefinition =
				ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(
					objectRelationship.getObjectDefinitionId2());

			labels.add(leafObjectDefinition.getLabel(locale));

			parentObjectFieldId = objectRelationship.getObjectFieldId2();
		}

		ObjectField objectField = ObjectFieldLocalServiceUtil.fetchObjectField(
			leafObjectDefinition.getObjectDefinitionId(),
			sourceFieldPath.get(sourceFieldPath.size() - 1));

		if ((objectField == null) ||
			(!objectRelationships.isEmpty() &&
			 (objectField.getObjectFieldId() == parentObjectFieldId))) {

			return StringPool.BLANK;
		}

		labels.add(objectField.getLabel(locale));

		return StringUtil.merge(labels, _LABEL_SEPARATOR);
	}

	public static String getLabel(
		ObjectDefinition objectDefinition, ObjectEntry objectEntry) {

		return getLabel(
			objectDefinition, LocaleUtil.US,
			MapUtil.getString(objectEntry.getValues(), "sourceFieldName"));
	}

	public static List<ObjectEntry> getObjectEntries(ObjectEntry objectEntry)
		throws PortalException {

		ObjectRelationship objectRelationship =
			ObjectRelationshipLocalServiceUtil.
				fetchObjectRelationshipByExternalReferenceCode(
					"L_PIM_CONNECTOR_TO_PIM_CONNECTOR_FIELD_MAPPINGS",
					objectEntry.getObjectDefinitionId());

		if (objectRelationship == null) {
			return Collections.emptyList();
		}

		return ListUtil.sort(
			ObjectEntryLocalServiceUtil.getOneToManyObjectEntries(
				objectEntry.getGroupId(),
				objectRelationship.getObjectRelationshipId(), null, false,
				objectEntry.getObjectEntryId(), true, null, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null),
			Comparator.comparingInt(
				curObjectEntry -> MapUtil.getInteger(
					curObjectEntry.getValues(), "priority")));
	}

	public static List<ObjectRelationship> getObjectRelationships(
		ObjectDefinition objectDefinition, List<String> sourceFieldPath) {

		if (sourceFieldPath.isEmpty()) {
			return null;
		}

		List<ObjectRelationship> objectRelationships = new ArrayList<>();

		long objectDefinitionId = objectDefinition.getObjectDefinitionId();

		for (String name :
				sourceFieldPath.subList(0, sourceFieldPath.size() - 1)) {

			ObjectRelationship objectRelationship =
				ObjectRelationshipLocalServiceUtil.
					fetchObjectRelationshipByObjectDefinitionId1(
						objectDefinitionId, name);

			if ((objectRelationship == null) ||
				!_isRepeatableGroupObjectRelationship(
					objectDefinition.getCompanyId(), objectRelationship)) {

				return null;
			}

			objectRelationships.add(objectRelationship);

			objectDefinitionId = objectRelationship.getObjectDefinitionId2();
		}

		return objectRelationships;
	}

	public static List<ObjectRelationship>
		getRepeatableGroupObjectRelationships(
			ObjectDefinition objectDefinition) {

		return ListUtil.filter(
			ObjectRelationshipLocalServiceUtil.getObjectRelationships(
				objectDefinition.getObjectDefinitionId(), true),
			objectRelationship -> _isRepeatableGroupObjectRelationship(
				objectDefinition.getCompanyId(), objectRelationship));
	}

	public static List<String> getSourceFieldPath(String sourceFieldName) {
		if (Validator.isNull(sourceFieldName)) {
			return Collections.emptyList();
		}

		List<String> sourceFieldPath = ListUtil.fromArray(
			StringUtil.split(sourceFieldName, StringPool.SLASH));

		if ((sourceFieldPath.size() !=
				(StringUtil.count(sourceFieldName, StringPool.SLASH) + 1)) ||
			ListUtil.exists(sourceFieldPath, Validator::isBlank)) {

			return Collections.emptyList();
		}

		return sourceFieldPath;
	}

	public static String getValue(
		ObjectDefinition objectDefinition, ObjectEntry objectEntry,
		Map<String, Serializable> values) {

		if (isFixedValue(objectEntry)) {
			return MapUtil.getString(objectEntry.getValues(), "value");
		}

		ListTypeEntry listTypeEntry = fetchListTypeEntry(
			objectDefinition, objectEntry, values);

		if (listTypeEntry != null) {
			return listTypeEntry.getName(LocaleUtil.US);
		}

		return MapUtil.getString(
			values,
			MapUtil.getString(objectEntry.getValues(), "sourceFieldName"));
	}

	public static String getValue(
			ObjectDefinition objectDefinition,
			ObjectEntry pimFieldMappingObjectEntry,
			ObjectEntry pimProductObjectEntry, Map<String, Serializable> values)
		throws PortalException {

		return StringUtil.merge(
			ListUtil.filter(
				getValues(
					objectDefinition, pimFieldMappingObjectEntry,
					pimProductObjectEntry, values),
				Validator::isNotNull),
			StringPool.SPACE);
	}

	public static List<String> getValues(
			ObjectDefinition objectDefinition,
			ObjectEntry pimFieldMappingObjectEntry,
			ObjectEntry pimProductObjectEntry, Map<String, Serializable> values)
		throws PortalException {

		if (!isRepeatableGroupPath(pimFieldMappingObjectEntry)) {
			return Collections.singletonList(
				getValue(objectDefinition, pimFieldMappingObjectEntry, values));
		}

		List<String> sourceFieldPath = getSourceFieldPath(
			MapUtil.getString(
				pimFieldMappingObjectEntry.getValues(), "sourceFieldName"));

		List<ObjectRelationship> objectRelationships = getObjectRelationships(
			objectDefinition, sourceFieldPath);

		if ((objectRelationships == null) || (pimProductObjectEntry == null)) {
			return Collections.emptyList();
		}

		ObjectRelationship leafObjectRelationship = objectRelationships.get(
			objectRelationships.size() - 1);

		ObjectDefinition leafObjectDefinition =
			ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(
				leafObjectRelationship.getObjectDefinitionId2());

		String objectFieldName = sourceFieldPath.get(
			sourceFieldPath.size() - 1);

		ObjectField objectField = ObjectFieldLocalServiceUtil.fetchObjectField(
			leafObjectDefinition.getObjectDefinitionId(), objectFieldName);

		if (objectField == null) {
			return Collections.emptyList();
		}

		List<ObjectEntry> objectEntries = Collections.singletonList(
			pimProductObjectEntry);

		for (ObjectRelationship objectRelationship : objectRelationships) {
			List<ObjectEntry> repeatableGroupObjectEntries = new ArrayList<>();

			for (ObjectEntry objectEntry : objectEntries) {
				repeatableGroupObjectEntries.addAll(
					ObjectEntryLocalServiceUtil.getOneToManyObjectEntries(
						objectEntry.getGroupId(),
						objectRelationship.getObjectRelationshipId(), null,
						false, objectEntry.getObjectEntryId(), true, null,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS, null));
			}

			objectEntries = repeatableGroupObjectEntries;
		}

		List<String> objectFieldValues = new ArrayList<>();

		for (ObjectEntry objectEntry : objectEntries) {
			objectFieldValues.add(
				_getValue(
					leafObjectDefinition, objectFieldName,
					ObjectEntryLocalServiceUtil.getValues(objectEntry)));
		}

		return objectFieldValues;
	}

	public static boolean isFixedValue(ObjectEntry objectEntry) {
		return Objects.equals(
			MapUtil.getString(objectEntry.getValues(), "type"),
			TYPE_FIXED_VALUE);
	}

	public static boolean isMappingComplete(
		List<ObjectEntry> objectEntries, PIMConnector pimConnector) {

		if (pimConnector == null) {
			return false;
		}

		Set<String> mappedChannelFieldNames = new HashSet<>();

		for (ObjectEntry objectEntry : objectEntries) {
			if (!_isMapped(objectEntry)) {
				continue;
			}

			mappedChannelFieldNames.add(
				MapUtil.getString(objectEntry.getValues(), "channelFieldName"));
		}

		for (PIMConnectorChannelField pimConnectorChannelField :
				pimConnector.getPIMConnectorChannelFields()) {

			if (pimConnectorChannelField.isRequired() &&
				!mappedChannelFieldNames.contains(
					pimConnectorChannelField.getName())) {

				return false;
			}
		}

		return true;
	}

	public static boolean isMappingComplete(
			ObjectEntry objectEntry, PIMConnector pimConnector)
		throws PortalException {

		if (pimConnector == null) {
			return false;
		}

		if (!ListUtil.exists(
				pimConnector.getPIMConnectorChannelFields(),
				PIMConnectorChannelField::isRequired)) {

			return true;
		}

		return isMappingComplete(getObjectEntries(objectEntry), pimConnector);
	}

	public static boolean isRepeatableGroupPath(ObjectEntry objectEntry) {
		if (isFixedValue(objectEntry)) {
			return false;
		}

		List<String> sourceFieldPath = getSourceFieldPath(
			MapUtil.getString(objectEntry.getValues(), "sourceFieldName"));

		if (sourceFieldPath.size() > 1) {
			return true;
		}

		return false;
	}

	private static String _getValue(
		ObjectDefinition objectDefinition, String objectFieldName,
		Map<String, Serializable> values) {

		ListTypeEntry listTypeEntry = fetchListTypeEntry(
			objectDefinition, objectFieldName, values);

		if (listTypeEntry != null) {
			return listTypeEntry.getName(LocaleUtil.US);
		}

		return MapUtil.getString(values, objectFieldName);
	}

	private static boolean _isMapped(ObjectEntry objectEntry) {
		if (isFixedValue(objectEntry)) {
			return Validator.isNotNull(
				MapUtil.getString(objectEntry.getValues(), "value"));
		}

		return Validator.isNotNull(
			MapUtil.getString(objectEntry.getValues(), "sourceFieldName"));
	}

	private static boolean _isRepeatableGroupObjectRelationship(
		long companyId, ObjectRelationship objectRelationship) {

		if (!objectRelationship.isEdge() ||
			!Objects.equals(
				objectRelationship.getType(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY)) {

			return false;
		}

		ObjectFolder objectFolder =
			ObjectFolderLocalServiceUtil.
				fetchObjectFolderByExternalReferenceCode(
					ObjectFolderConstants.
						EXTERNAL_REFERENCE_CODE_STRUCTURE_REPEATABLE_GROUPS,
					companyId);

		if (objectFolder == null) {
			return false;
		}

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(
				objectRelationship.getObjectDefinitionId2());

		if ((objectDefinition != null) &&
			(objectDefinition.getObjectFolderId() ==
				objectFolder.getObjectFolderId())) {

			return true;
		}

		return false;
	}

	private static Object _toObject(
		PIMConnectorChannelField pimConnectorChannelField, String value) {

		String type = pimConnectorChannelField.getType();

		if (Objects.equals(type, ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN)) {
			return Boolean.valueOf(value);
		}

		try {
			if (Objects.equals(
					type, ObjectFieldConstants.BUSINESS_TYPE_DECIMAL)) {

				return Double.valueOf(value);
			}

			if (Objects.equals(
					type, ObjectFieldConstants.BUSINESS_TYPE_INTEGER) ||
				Objects.equals(
					type, ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER)) {

				return Long.valueOf(value);
			}
		}
		catch (NumberFormatException numberFormatException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Unable to convert ",
						pimConnectorChannelField.getName(), " to ", type),
					numberFormatException);
			}
		}

		return value;
	}

	private static final String _LABEL_SEPARATOR = " › ";

	private static final Log _log = LogFactoryUtil.getLog(
		PIMConnectorFieldMappingsUtil.class);

}
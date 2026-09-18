/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.connector;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FriendlyURLNormalizer;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.pim.site.initializer.connector.PIMConnector;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorField;
import com.liferay.site.pim.site.initializer.constants.PIMConnectorFieldConstants;
import com.liferay.site.pim.site.initializer.constants.PIMObjectDefinitionConstants;
import com.liferay.site.pim.site.initializer.exception.PIMConnectorFieldMappingException;
import com.liferay.site.pim.site.initializer.internal.link.VariantPIMLinkType;
import com.liferay.site.pim.site.initializer.internal.util.PIMFieldMappingUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(service = PIMConnector.class)
public class LiferayCommercePIMConnector implements PIMConnector {

	public static final String KEY = "liferay-commerce";

	@Override
	public String exportProducts(ObjectEntry pimConnectorObjectEntry)
		throws PortalException {

		long companyId = pimConnectorObjectEntry.getCompanyId();

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_BASE_SKU,
					companyId);

		if (objectDefinition == null) {
			throw new PortalException(
				"Unable to get the base SKU object definition of company " +
					companyId);
		}

		PIMFieldMappings pimFieldMappings = _getPIMFieldMappings(
			_getFieldMappingJSONObject(pimConnectorObjectEntry),
			objectDefinition);

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (DepotEntry depotEntry :
				_depotEntryLocalService.getDepotEntries(
					companyId, DepotConstants.TYPE_SPACE)) {

			Map<String, String> clusterKeys = _getVariantPIMLinkClusterKeys(
				companyId, depotEntry.getGroupId());
			Map<String, List<ObjectEntry>> objectEntriesMap =
				new LinkedHashMap<>();

			for (ObjectEntry objectEntry :
					_objectEntryLocalService.getObjectEntries(
						depotEntry.getGroupId(),
						objectDefinition.getObjectDefinitionId(),
						QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

				String externalReferenceCode =
					objectEntry.getExternalReferenceCode();

				List<ObjectEntry> objectEntries =
					objectEntriesMap.computeIfAbsent(
						GetterUtil.getString(
							clusterKeys.get(externalReferenceCode),
							externalReferenceCode),
						key -> new ArrayList<>());

				objectEntries.add(objectEntry);
			}

			for (List<ObjectEntry> objectEntries : objectEntriesMap.values()) {
				jsonArray.put(_toJSONObject(pimFieldMappings, objectEntries));
			}
		}

		return jsonArray.toString();
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName(Locale locale) {
		return LanguageUtil.get(locale, KEY);
	}

	@Override
	public List<PIMConnectorField> getPIMConnectorFields(Locale locale) {
		return TransformUtil.transform(
			_pimConnectorFields,
			pimConnectorField -> new PIMConnectorField(
				LanguageUtil.get(locale, pimConnectorField.getLabel()),
				pimConnectorField.isMultiple(), pimConnectorField.getName(),
				pimConnectorField.isRequired(), pimConnectorField.getType()));
	}

	@Override
	public boolean isActive(long companyId) {
		return true;
	}

	private long _getCatalogId(String value) throws PortalException {
		long catalogId = GetterUtil.getLong(value);

		if (catalogId <= 0) {
			throw new PIMConnectorFieldMappingException(
				StringBundler.concat(
					"Unable to export the products because the catalog ID is ",
					"not a number: ", value));
		}

		return catalogId;
	}

	private JSONObject _getFieldMappingJSONObject(
			ObjectEntry pimConnectorObjectEntry)
		throws PortalException {

		String fieldMapping = MapUtil.getString(
			_objectEntryLocalService.getValues(pimConnectorObjectEntry),
			"fieldMapping");

		if (Validator.isNull(fieldMapping)) {
			return _jsonFactory.createJSONObject();
		}

		return _jsonFactory.createJSONObject(fieldMapping);
	}

	private String _getFirstValue(
		PIMFieldMappings pimFieldMappings, String name,
		Map<String, Serializable> values) {

		List<String> resolvedValues = pimFieldMappings.getValues(name, values);

		if (resolvedValues.isEmpty()) {
			return StringPool.BLANK;
		}

		if ((resolvedValues.size() > 1) && _log.isWarnEnabled()) {
			_log.warn(
				StringBundler.concat(
					"Ignoring ", resolvedValues.size() - 1,
					" extra mappings for the field ", name));
		}

		return resolvedValues.get(0);
	}

	private String _getJoinedValue(
		PIMFieldMappings pimFieldMappings, String name,
		Map<String, Serializable> values) {

		return StringUtil.merge(
			pimFieldMappings.getValues(name, values), StringPool.SPACE);
	}

	private PIMFieldMappings _getPIMFieldMappings(
			JSONObject fieldMappingJSONObject,
			ObjectDefinition objectDefinition)
		throws PortalException {

		PIMFieldMappings pimFieldMappings = new PIMFieldMappings(
			_listTypeEntryLocalService);

		List<String> unmappedNames = new ArrayList<>();

		for (PIMConnectorField pimConnectorField : _pimConnectorFields) {
			String name = pimConnectorField.getName();

			if (!_setPIMFieldMapping(
					fieldMappingJSONObject, name, objectDefinition,
					pimFieldMappings) &&
				pimConnectorField.isRequired()) {

				unmappedNames.add(name);
			}
		}

		if (!unmappedNames.isEmpty()) {
			throw new PIMConnectorFieldMappingException(
				StringBundler.concat(
					"Unable to export the products because the required ",
					"fields are not mapped: ",
					StringUtil.merge(unmappedNames, ", ")));
		}

		return pimFieldMappings;
	}

	private JSONArray _getProductOptionsJSONArray(
		PIMFieldMappings pimFieldMappings,
		List<Map<String, Serializable>> valuesList) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		int priority = 0;

		for (PIMFieldMappingSource pimFieldMappingSource :
				pimFieldMappings.getPIMFieldMappingSources(
					_CHANNEL_FIELD_NAME_PRODUCT_OPTIONS)) {

			JSONArray productOptionValuesJSONArray =
				_jsonFactory.createJSONArray();

			List<String> keys = new ArrayList<>();

			for (Map<String, Serializable> values : valuesList) {
				String value = pimFieldMappingSource.getValue(values);

				if (Validator.isNull(value)) {
					continue;
				}

				String key = _friendlyURLNormalizer.normalize(value);

				if (keys.contains(key)) {
					continue;
				}

				productOptionValuesJSONArray.put(
					JSONUtil.put(
						"key", key
					).put(
						"name", JSONUtil.put("en_US", value)
					).put(
						"priority", keys.size()
					));

				keys.add(key);
			}

			if (productOptionValuesJSONArray.length() > 0) {
				jsonArray.put(
					JSONUtil.put(
						"name",
						JSONUtil.put("en_US", pimFieldMappingSource.getLabel())
					).put(
						"optionExternalReferenceCode",
						_friendlyURLNormalizer.normalize(
							pimFieldMappingSource.getAttributeName())
					).put(
						"priority", priority
					).put(
						"productOptionValues", productOptionValuesJSONArray
					).put(
						"skuContributor", true
					));
			}

			priority++;
		}

		return jsonArray;
	}

	private JSONArray _getProductSpecificationsJSONArray(
		PIMFieldMappings pimFieldMappings, Map<String, Serializable> values) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		int priority = 0;

		for (PIMFieldMappingSource pimFieldMappingSource :
				pimFieldMappings.getPIMFieldMappingSources(
					_CHANNEL_FIELD_NAME_PRODUCT_SPECIFICATIONS)) {

			String value = pimFieldMappingSource.getValue(values);

			if (Validator.isNotNull(value)) {
				jsonArray.put(
					JSONUtil.put(
						"label",
						JSONUtil.put("en_US", pimFieldMappingSource.getLabel())
					).put(
						"priority", priority
					).put(
						"specificationKey",
						_friendlyURLNormalizer.normalize(
							pimFieldMappingSource.getAttributeName())
					).put(
						"value", JSONUtil.put("en_US", value)
					));
			}

			priority++;
		}

		return jsonArray;
	}

	private JSONArray _getSkuOptionsJSONArray(
		PIMFieldMappings pimFieldMappings, Map<String, Serializable> values) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (PIMFieldMappingSource pimFieldMappingSource :
				pimFieldMappings.getPIMFieldMappingSources(
					_CHANNEL_FIELD_NAME_PRODUCT_OPTIONS)) {

			String value = pimFieldMappingSource.getValue(values);

			if (Validator.isNull(value)) {
				continue;
			}

			jsonArray.put(
				JSONUtil.put(
					"key",
					_friendlyURLNormalizer.normalize(
						pimFieldMappingSource.getAttributeName())
				).put(
					"value", _friendlyURLNormalizer.normalize(value)
				));
		}

		return jsonArray;
	}

	private Map<String, String> _getVariantPIMLinkClusterKeys(
			long companyId, long groupId)
		throws PortalException {

		Map<String, String> clusterKeys = new HashMap<>();

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.EXTERNAL_REFERENCE_CODE_LINK,
					companyId);

		if (objectDefinition == null) {
			return clusterKeys;
		}

		for (Map<String, Serializable> values :
				_objectEntryLocalService.getValuesList(
					groupId, companyId, 0,
					objectDefinition.getObjectDefinitionId(),
					_filterFactory.create(
						StringBundler.concat(
							"type eq '", VariantPIMLinkType.TYPE, "'"),
						objectDefinition),
					null, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			String clusterKey = MapUtil.getString(values, "clusterKey");

			if (Validator.isNull(clusterKey)) {
				continue;
			}

			clusterKeys.put(
				MapUtil.getString(values, "sourceClassExternalReferenceCode"),
				clusterKey);
		}

		return clusterKeys;
	}

	private boolean _setPIMFieldMapping(
			JSONObject fieldMappingJSONObject, String name,
			ObjectDefinition objectDefinition,
			PIMFieldMappings pimFieldMappings)
		throws PIMConnectorFieldMappingException {

		boolean mapped = false;

		boolean keyedByAttribute = false;

		if (name.equals(_CHANNEL_FIELD_NAME_PRODUCT_OPTIONS) ||
			name.equals(_CHANNEL_FIELD_NAME_PRODUCT_SPECIFICATIONS)) {

			keyedByAttribute = true;
		}

		List<String> keys = new ArrayList<>();

		JSONArray mappingsJSONArray = PIMFieldMappingUtil.getMappingsJSONArray(
			fieldMappingJSONObject, name);

		for (int i = 0; i < mappingsJSONArray.length(); i++) {
			JSONObject mappingJSONObject = mappingsJSONArray.getJSONObject(i);

			if (PIMFieldMappingUtil.isFixedValue(mappingJSONObject)) {
				String value = mappingJSONObject.getString("value");

				if (Validator.isNull(value)) {
					continue;
				}

				if (keyedByAttribute) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Ignoring the fixed value mapping for the field " +
								name);
					}

					continue;
				}

				pimFieldMappings.addFixedValue(name, value);

				mapped = true;

				continue;
			}

			String source = mappingJSONObject.getString("source");

			if (Validator.isNotNull(source) &&
				!source.equals(objectDefinition.getName())) {

				throw new PIMConnectorFieldMappingException(
					StringBundler.concat(
						"Unable to export the products because the field ",
						name, " is mapped from the structure ", source,
						" instead of ", objectDefinition.getName()));
			}

			String attributeName = mappingJSONObject.getString("attribute");

			if (Validator.isNull(attributeName)) {
				continue;
			}

			ObjectField objectField = _objectFieldLocalService.fetchObjectField(
				objectDefinition.getObjectDefinitionId(), attributeName);

			if (keyedByAttribute) {
				String key = _friendlyURLNormalizer.normalize(attributeName);

				if (keys.contains(key)) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Ignoring the duplicate key ", key,
								" for the field ", name));
					}

					continue;
				}

				keys.add(key);
			}

			pimFieldMappings.addAttributeName(name, attributeName, objectField);

			mapped = true;
		}

		return mapped;
	}

	private JSONObject _toJSONObject(
			PIMFieldMappings pimFieldMappings, List<ObjectEntry> objectEntries)
		throws PortalException {

		ObjectEntry objectEntry = objectEntries.get(0);

		List<Map<String, Serializable>> valuesList =
			TransformUtil.unsafeTransform(
				objectEntries, _objectEntryLocalService::getValues);

		Map<String, Serializable> values = valuesList.get(0);

		JSONArray skusJSONArray = _jsonFactory.createJSONArray();

		for (Map<String, Serializable> skuValues : valuesList) {
			skusJSONArray.put(_toSkuJSONObject(pimFieldMappings, skuValues));
		}

		JSONObject jsonObject = JSONUtil.put(
			"active", true
		).put(
			"catalogId",
			_getCatalogId(
				_getFirstValue(
					pimFieldMappings, _CHANNEL_FIELD_NAME_CATALOG_ID, values))
		).put(
			"externalReferenceCode", objectEntry.getExternalReferenceCode()
		).put(
			"name",
			JSONUtil.put(
				"en_US",
				_getJoinedValue(
					pimFieldMappings, _CHANNEL_FIELD_NAME_NAME, values))
		).put(
			"productType",
			() -> {
				if (MapUtil.getBoolean(values, "virtual")) {
					return "virtual";
				}

				return "simple";
			}
		).put(
			"skus", skusJSONArray
		);

		List<String> tags = pimFieldMappings.getValues(
			_CHANNEL_FIELD_NAME_TAGS, values);

		if (!tags.isEmpty()) {
			jsonObject.put("tags", JSONUtil.putAll(tags.toArray()));
		}

		JSONArray productOptionsJSONArray = _getProductOptionsJSONArray(
			pimFieldMappings, valuesList);

		if (productOptionsJSONArray.length() > 0) {
			jsonObject.put("productOptions", productOptionsJSONArray);
		}

		JSONArray productSpecificationsJSONArray =
			_getProductSpecificationsJSONArray(pimFieldMappings, values);

		if (productSpecificationsJSONArray.length() > 0) {
			jsonObject.put(
				"productSpecifications", productSpecificationsJSONArray);
		}

		String description = _getJoinedValue(
			pimFieldMappings, _CHANNEL_FIELD_NAME_DESCRIPTION, values);

		if (Validator.isNull(description)) {
			return jsonObject;
		}

		return jsonObject.put(
			"description", JSONUtil.put("en_US", description));
	}

	private JSONObject _toSkuJSONObject(
		PIMFieldMappings pimFieldMappings, Map<String, Serializable> values) {

		JSONObject jsonObject = JSONUtil.put(
			"depth", MapUtil.getDouble(values, "depth")
		).put(
			"height", MapUtil.getDouble(values, "height")
		).put(
			"published", true
		).put(
			"purchasable", true
		).put(
			"sku",
			_getJoinedValue(pimFieldMappings, _CHANNEL_FIELD_NAME_SKU, values)
		).put(
			"weight", MapUtil.getDouble(values, "weight")
		).put(
			"width", MapUtil.getDouble(values, "width")
		);

		JSONArray skuOptionsJSONArray = _getSkuOptionsJSONArray(
			pimFieldMappings, values);

		if (skuOptionsJSONArray.length() > 0) {
			jsonObject.put("skuOptions", skuOptionsJSONArray);
		}

		String unitOfMeasureKey = MapUtil.getString(values, "unitOfMeasureKey");

		if (Validator.isNull(unitOfMeasureKey)) {
			return jsonObject;
		}

		int precision = 0;

		if (MapUtil.getBoolean(values, "unitOfMeasureAllowDecimalQuantities")) {
			precision = 2;
		}

		return jsonObject.put(
			"skuUnitOfMeasures",
			JSONUtil.putAll(
				JSONUtil.put(
					"incrementalOrderQuantity", 1
				).put(
					"key", unitOfMeasureKey
				).put(
					"name",
					JSONUtil.put(
						"en_US", MapUtil.getString(values, "unitOfMeasureName"))
				).put(
					"precision", precision
				).put(
					"primary", true
				).put(
					"rate", 1
				)));
	}

	private static final String _CHANNEL_FIELD_NAME_CATALOG_ID = "catalogId";

	private static final String _CHANNEL_FIELD_NAME_DESCRIPTION = "description";

	private static final String _CHANNEL_FIELD_NAME_NAME = "name";

	private static final String _CHANNEL_FIELD_NAME_PRODUCT_OPTIONS =
		"productOptions";

	private static final String _CHANNEL_FIELD_NAME_PRODUCT_SPECIFICATIONS =
		"productSpecifications";

	private static final String _CHANNEL_FIELD_NAME_SKU = "skus[].sku";

	private static final String _CHANNEL_FIELD_NAME_TAGS = "tags";

	private static final Log _log = LogFactoryUtil.getLog(
		LiferayCommercePIMConnector.class);

	private static final List<PIMConnectorField> _pimConnectorFields =
		Arrays.asList(
			new PIMConnectorField(
				"catalog-id", false, _CHANNEL_FIELD_NAME_CATALOG_ID, true,
				PIMConnectorFieldConstants.TYPE_LONG),
			new PIMConnectorField(
				_CHANNEL_FIELD_NAME_DESCRIPTION, false,
				_CHANNEL_FIELD_NAME_DESCRIPTION, false,
				PIMConnectorFieldConstants.TYPE_LOCALIZED_TEXT),
			new PIMConnectorField(
				_CHANNEL_FIELD_NAME_NAME, false, _CHANNEL_FIELD_NAME_NAME, true,
				PIMConnectorFieldConstants.TYPE_LOCALIZED_TEXT),
			new PIMConnectorField(
				"product-options", true, _CHANNEL_FIELD_NAME_PRODUCT_OPTIONS,
				false, PIMConnectorFieldConstants.TYPE_TEXT),
			new PIMConnectorField(
				"product-specifications", true,
				_CHANNEL_FIELD_NAME_PRODUCT_SPECIFICATIONS, false,
				PIMConnectorFieldConstants.TYPE_TEXT),
			new PIMConnectorField(
				"sku", false, _CHANNEL_FIELD_NAME_SKU, true,
				PIMConnectorFieldConstants.TYPE_TEXT),
			new PIMConnectorField(
				_CHANNEL_FIELD_NAME_TAGS, true, _CHANNEL_FIELD_NAME_TAGS, false,
				PIMConnectorFieldConstants.TYPE_TEXT));

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private FriendlyURLNormalizer _friendlyURLNormalizer;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	private static class PIMFieldMappings {

		public PIMFieldMappings(
			ListTypeEntryLocalService listTypeEntryLocalService) {

			_listTypeEntryLocalService = listTypeEntryLocalService;
		}

		public void addAttributeName(
			String name, String attributeName, ObjectField objectField) {

			getPIMFieldMappingSources(
				name
			).add(
				new PIMFieldMappingSource(
					attributeName, _listTypeEntryLocalService, objectField,
					StringPool.BLANK)
			);
		}

		public void addFixedValue(String name, String value) {
			getPIMFieldMappingSources(
				name
			).add(
				new PIMFieldMappingSource(
					StringPool.BLANK, _listTypeEntryLocalService, null, value)
			);
		}

		public List<PIMFieldMappingSource> getPIMFieldMappingSources(
			String name) {

			return _pimFieldMappingSources.computeIfAbsent(
				name, key -> new ArrayList<>());
		}

		public List<String> getValues(
			String name, Map<String, Serializable> values) {

			List<String> resolvedValues = new ArrayList<>();

			for (PIMFieldMappingSource pimFieldMappingSource :
					getPIMFieldMappingSources(name)) {

				String value = pimFieldMappingSource.getValue(values);

				if (Validator.isNotNull(value)) {
					resolvedValues.add(value);
				}
			}

			return resolvedValues;
		}

		private final ListTypeEntryLocalService _listTypeEntryLocalService;
		private final Map<String, List<PIMFieldMappingSource>>
			_pimFieldMappingSources = new HashMap<>();

	}

	private static class PIMFieldMappingSource {

		public PIMFieldMappingSource(
			String attributeName,
			ListTypeEntryLocalService listTypeEntryLocalService,
			ObjectField objectField, String value) {

			_attributeName = attributeName;
			_listTypeEntryLocalService = listTypeEntryLocalService;
			_objectField = objectField;
			_value = value;
		}

		public String getAttributeName() {
			return _attributeName;
		}

		public String getLabel() {
			if (_objectField == null) {
				return StringPool.BLANK;
			}

			return _objectField.getLabel(LocaleUtil.US);
		}

		public String getValue(Map<String, Serializable> values) {
			if (Validator.isNull(_attributeName)) {
				return _value;
			}

			String value = MapUtil.getString(values, _attributeName);

			if ((_objectField == null) || Validator.isNull(value) ||
				!_objectField.compareBusinessType(
					ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)) {

				return value;
			}

			ListTypeEntry listTypeEntry =
				_listTypeEntryLocalService.fetchListTypeEntry(
					_objectField.getListTypeDefinitionId(), value);

			if (listTypeEntry == null) {
				return value;
			}

			return listTypeEntry.getName(LocaleUtil.US);
		}

		private final String _attributeName;
		private final ListTypeEntryLocalService _listTypeEntryLocalService;
		private final ObjectField _objectField;
		private final String _value;

	}

}
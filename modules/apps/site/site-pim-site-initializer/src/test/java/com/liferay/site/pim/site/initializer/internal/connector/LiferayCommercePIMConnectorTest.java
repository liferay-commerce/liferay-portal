/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.connector;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorField;
import com.liferay.site.pim.site.initializer.constants.PIMConnectorFieldConstants;
import com.liferay.site.pim.site.initializer.constants.PIMObjectDefinitionConstants;
import com.liferay.site.pim.site.initializer.exception.PIMConnectorFieldMappingException;

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
public class LiferayCommercePIMConnectorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtil.setFieldValue(
			_liferayCommercePIMConnector, "_depotEntryLocalService",
			_depotEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_liferayCommercePIMConnector, "_filterFactory", _filterFactory);
		ReflectionTestUtil.setFieldValue(
			_liferayCommercePIMConnector, "_jsonFactory", _jsonFactory);
		ReflectionTestUtil.setFieldValue(
			_liferayCommercePIMConnector, "_objectDefinitionLocalService",
			_objectDefinitionLocalService);
		ReflectionTestUtil.setFieldValue(
			_liferayCommercePIMConnector, "_objectEntryLocalService",
			_objectEntryLocalService);
	}

	@Test
	public void testExportProducts() throws Exception {
		_testExportProducts();
		_testExportProductsWithInvalidCatalogId();
		_testExportProductsWithLegacyFieldMapping();
		_testExportProductsWithMissingObjectDefinition();
		_testExportProductsWithMixedNameMappings();
		_testExportProductsWithMultipleCatalogIdMappings();
		_testExportProductsWithMultipleDepotEntries();
		_testExportProductsWithMultipleNameMappings();
		_testExportProductsWithMultipleTags();
		_testExportProductsWithoutTags();
		_testExportProductsWithoutUnitOfMeasure();
		_testExportProductsWithUnmappedDescription();
		_testExportProductsWithUnmappedRequiredFields();
		_testExportProductsWithUnsupportedSource();
		_testExportProductsWithVariantPIMLink();
		_testExportProductsWithVirtualSku();
	}

	@Test
	public void testGetKey() {
		Assert.assertEquals(
			"liferay-commerce", _liferayCommercePIMConnector.getKey());
	}

	@Test
	public void testGetName() {
		_mockLanguage();

		Assert.assertEquals(
			"liferay-commerce",
			_liferayCommercePIMConnector.getName(LocaleUtil.US));
	}

	@Test
	public void testGetPIMConnectorFields() {
		_mockLanguage();

		List<PIMConnectorField> pimConnectorFields =
			_liferayCommercePIMConnector.getPIMConnectorFields(LocaleUtil.US);

		Assert.assertEquals(
			pimConnectorFields.toString(), 5, pimConnectorFields.size());

		Assert.assertEquals(
			Arrays.asList(
				"catalogId", "description", "name", "skus[].sku", "tags"),
			TransformUtil.transform(
				pimConnectorFields, PIMConnectorField::getName));

		PIMConnectorField pimConnectorField = pimConnectorFields.get(0);

		Assert.assertEquals("catalog-id", pimConnectorField.getLabel());
		Assert.assertEquals(
			PIMConnectorFieldConstants.TYPE_LONG, pimConnectorField.getType());
		Assert.assertFalse(pimConnectorField.isMultiple());
		Assert.assertTrue(pimConnectorField.isRequired());

		pimConnectorField = pimConnectorFields.get(1);

		Assert.assertEquals("description", pimConnectorField.getLabel());
		Assert.assertEquals(
			PIMConnectorFieldConstants.TYPE_LOCALIZED_TEXT,
			pimConnectorField.getType());
		Assert.assertFalse(pimConnectorField.isMultiple());
		Assert.assertFalse(pimConnectorField.isRequired());

		pimConnectorField = pimConnectorFields.get(2);

		Assert.assertEquals("name", pimConnectorField.getLabel());
		Assert.assertEquals(
			PIMConnectorFieldConstants.TYPE_LOCALIZED_TEXT,
			pimConnectorField.getType());
		Assert.assertFalse(pimConnectorField.isMultiple());
		Assert.assertTrue(pimConnectorField.isRequired());

		pimConnectorField = pimConnectorFields.get(3);

		Assert.assertEquals("sku", pimConnectorField.getLabel());
		Assert.assertEquals(
			PIMConnectorFieldConstants.TYPE_TEXT, pimConnectorField.getType());
		Assert.assertFalse(pimConnectorField.isMultiple());
		Assert.assertTrue(pimConnectorField.isRequired());

		pimConnectorField = pimConnectorFields.get(4);

		Assert.assertEquals("tags", pimConnectorField.getLabel());
		Assert.assertEquals(
			PIMConnectorFieldConstants.TYPE_TEXT, pimConnectorField.getType());
		Assert.assertTrue(pimConnectorField.isMultiple());
		Assert.assertFalse(pimConnectorField.isRequired());
	}

	@Test
	public void testIsActive() {
		Assert.assertTrue(
			_liferayCommercePIMConnector.isActive(RandomTestUtil.randomLong()));
	}

	private JSONArray _getAttributeJSONArray(String... attributes) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (String attribute : attributes) {
			jsonArray.put(
				JSONUtil.put(
					"attribute", attribute
				).put(
					"source", ""
				).put(
					"type", "attribute"
				));
		}

		return jsonArray;
	}

	private String _getFieldMapping(
		Map<String, String> attributeNames, String catalogId) {

		JSONObject fieldMappingJSONObject = _jsonFactory.createJSONObject();

		if (catalogId != null) {
			fieldMappingJSONObject.put(
				"catalogId",
				JSONUtil.putAll(
					JSONUtil.put(
						"type", "fixedValue"
					).put(
						"value", catalogId
					)));
		}

		for (Map.Entry<String, String> entry : attributeNames.entrySet()) {
			fieldMappingJSONObject.put(
				entry.getKey(),
				JSONUtil.putAll(
					JSONUtil.put(
						"attribute", entry.getValue()
					).put(
						"source", ""
					)));
		}

		return fieldMappingJSONObject.toString();
	}

	private JSONObject _getFieldMappingJSONObject() {
		return JSONUtil.put(
			"catalogId", _getFixedValueJSONArray(_CATALOG_ID)
		).put(
			"name", _getAttributeJSONArray("name")
		).put(
			"skus[].sku", _getAttributeJSONArray("code")
		);
	}

	private JSONArray _getFixedValueJSONArray(String... values) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (String value : values) {
			jsonArray.put(
				JSONUtil.put(
					"type", "fixedValue"
				).put(
					"value", value
				));
		}

		return jsonArray;
	}

	private JSONArray _getJSONArray(ObjectEntry pimConnectorObjectEntry)
		throws Exception {

		return _jsonFactory.createJSONArray(
			_liferayCommercePIMConnector.exportProducts(
				pimConnectorObjectEntry));
	}

	private JSONObject _getJSONObject(ObjectEntry pimConnectorObjectEntry)
		throws Exception {

		JSONArray jsonArray = _getJSONArray(pimConnectorObjectEntry);

		Assert.assertEquals(1, jsonArray.length());

		return jsonArray.getJSONObject(0);
	}

	private JSONObject _getSkuJSONObject(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("skus");

		Assert.assertEquals(1, jsonArray.length());

		return jsonArray.getJSONObject(0);
	}

	private JSONObject _getSkuUnitOfMeasureJSONObject(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray("skuUnitOfMeasures");

		Assert.assertEquals(1, jsonArray.length());

		return jsonArray.getJSONObject(0);
	}

	private void _mockGetObjectEntries(
		long groupId, ObjectDefinition objectDefinition,
		ObjectEntry... objectEntries) {

		Mockito.when(
			_objectEntryLocalService.getObjectEntries(
				groupId, objectDefinition.getObjectDefinitionId(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS)
		).thenReturn(
			Arrays.asList(objectEntries)
		);
	}

	private void _mockLanguage() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		Mockito.when(
			language.get(Mockito.eq(LocaleUtil.US), Mockito.anyString())
		).thenAnswer(
			invocationOnMock -> invocationOnMock.getArgument(1)
		);

		languageUtil.setLanguage(language);
	}

	private ObjectDefinition _mockObjectDefinition() {
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getName()
		).thenReturn(
			"PIMBaseSku"
		);

		Mockito.when(
			objectDefinition.getObjectDefinitionId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Mockito.when(
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_BASE_SKU,
					_COMPANY_ID)
		).thenReturn(
			objectDefinition
		);

		return objectDefinition;
	}

	private ObjectEntry _mockObjectEntry(
			String code, String externalReferenceCode)
		throws Exception {

		return _mockObjectEntry(
			code, externalReferenceCode,
			Collections.<String, Serializable>emptyMap());
	}

	private ObjectEntry _mockObjectEntry(
			String code, String externalReferenceCode,
			Map<String, Serializable> values)
		throws Exception {

		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getExternalReferenceCode()
		).thenReturn(
			externalReferenceCode
		);

		Mockito.when(
			_objectEntryLocalService.getValues(objectEntry)
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"code", code
			).put(
				"depth", 10.5D
			).put(
				"description", code + " description"
			).put(
				"height", 20.5D
			).put(
				"name", code + " name"
			).put(
				"unitOfMeasureAllowDecimalQuantities", true
			).put(
				"unitOfMeasureKey", "box"
			).put(
				"unitOfMeasureName", "Box"
			).put(
				"virtual", false
			).put(
				"weight", 30.5D
			).put(
				"width", 40.5D
			).putAll(
				values
			).build()
		);

		return objectEntry;
	}

	private ObjectEntry _mockPIMConnectorObjectEntry() throws Exception {
		return _mockPIMConnectorObjectEntry(
			_getFieldMapping(
				HashMapBuilder.put(
					"description", "description"
				).put(
					"name", "name"
				).put(
					"skus[].sku", "code"
				).build(),
				_CATALOG_ID));
	}

	private ObjectEntry _mockPIMConnectorObjectEntry(String fieldMapping)
		throws Exception {

		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			_objectEntryLocalService.getValues(objectEntry)
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"fieldMapping", fieldMapping
			).put(
				"key", "liferay-commerce"
			).build()
		);

		return objectEntry;
	}

	private void _mockSpaceDepotEntries(long... groupIds) {
		DepotEntry[] depotEntries = new DepotEntry[groupIds.length];

		for (int i = 0; i < groupIds.length; i++) {
			depotEntries[i] = Mockito.mock(DepotEntry.class);

			Mockito.when(
				depotEntries[i].getGroupId()
			).thenReturn(
				groupIds[i]
			);
		}

		Mockito.when(
			_depotEntryLocalService.getDepotEntries(
				_COMPANY_ID, DepotConstants.TYPE_SPACE)
		).thenReturn(
			Arrays.asList(depotEntries)
		);
	}

	private void _mockVariantPIMLinks(
			String clusterKey, String... externalReferenceCodes)
		throws Exception {

		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);

		Mockito.when(
			objectDefinition.getObjectDefinitionId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		Mockito.when(
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.EXTERNAL_REFERENCE_CODE_LINK,
					_COMPANY_ID)
		).thenReturn(
			objectDefinition
		);

		Predicate predicate = Mockito.mock(Predicate.class);

		Mockito.when(
			_filterFactory.create(
				Mockito.anyString(), Mockito.eq(objectDefinition))
		).thenReturn(
			predicate
		);

		Mockito.when(
			_objectEntryLocalService.getValuesList(
				_GROUP_ID, _COMPANY_ID, 0,
				objectDefinition.getObjectDefinitionId(), predicate, null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)
		).thenReturn(
			TransformUtil.transform(
				Arrays.asList(externalReferenceCodes),
				externalReferenceCode ->
					HashMapBuilder.<String, Serializable>put(
						"clusterKey", clusterKey
					).put(
						"sourceClassExternalReferenceCode",
						externalReferenceCode
					).build())
		);
	}

	private void _testExportProducts() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject jsonObject = _getJSONObject(_mockPIMConnectorObjectEntry());

		Assert.assertTrue(jsonObject.getBoolean("active"));
		Assert.assertEquals(12345, jsonObject.getLong("catalogId"));
		Assert.assertEquals(
			"SKU-1", jsonObject.getString("externalReferenceCode"));
		Assert.assertEquals("simple", jsonObject.getString("productType"));

		JSONObject descriptionJSONObject = jsonObject.getJSONObject(
			"description");

		Assert.assertEquals(
			"SKU-1 description", descriptionJSONObject.getString("en_US"));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals("SKU-1 name", nameJSONObject.getString("en_US"));

		JSONObject skuJSONObject = _getSkuJSONObject(jsonObject);

		Assert.assertEquals(10.5, skuJSONObject.getDouble("depth"), 0);
		Assert.assertEquals(20.5, skuJSONObject.getDouble("height"), 0);
		Assert.assertTrue(skuJSONObject.getBoolean("published"));
		Assert.assertTrue(skuJSONObject.getBoolean("purchasable"));
		Assert.assertEquals("SKU-1", skuJSONObject.getString("sku"));
		Assert.assertEquals(30.5, skuJSONObject.getDouble("weight"), 0);
		Assert.assertEquals(40.5, skuJSONObject.getDouble("width"), 0);

		JSONObject skuUnitOfMeasureJSONObject = _getSkuUnitOfMeasureJSONObject(
			skuJSONObject);

		Assert.assertEquals(
			1, skuUnitOfMeasureJSONObject.getInt("incrementalOrderQuantity"));
		Assert.assertEquals("box", skuUnitOfMeasureJSONObject.getString("key"));
		Assert.assertEquals(2, skuUnitOfMeasureJSONObject.getInt("precision"));
		Assert.assertTrue(skuUnitOfMeasureJSONObject.getBoolean("primary"));
		Assert.assertEquals(1, skuUnitOfMeasureJSONObject.getInt("rate"));

		JSONObject skuUnitOfMeasureNameJSONObject =
			skuUnitOfMeasureJSONObject.getJSONObject("name");

		Assert.assertEquals(
			"Box", skuUnitOfMeasureNameJSONObject.getString("en_US"));
	}

	private void _testExportProductsWithInvalidCatalogId() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		try {
			_liferayCommercePIMConnector.exportProducts(
				_mockPIMConnectorObjectEntry(
					_getFieldMapping(
						HashMapBuilder.put(
							"name", "name"
						).put(
							"skus[].sku", "code"
						).build(),
						"master")));

			Assert.fail();
		}
		catch (PIMConnectorFieldMappingException
					pimConnectorFieldMappingException) {

			Assert.assertEquals(
				"Unable to export the products because the catalog ID is not " +
					"a number: master",
				pimConnectorFieldMappingException.getMessage());
		}
	}

	private void _testExportProductsWithLegacyFieldMapping() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				JSONUtil.put(
					"catalogId",
					JSONUtil.putAll(
						JSONUtil.put(
							"type", "fixedValue"
						).put(
							"value", _CATALOG_ID
						))
				).put(
					"description", "description"
				).put(
					"name", "name"
				).put(
					"skus[].sku", "code"
				).toString()));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals("SKU-1 name", nameJSONObject.getString("en_US"));

		JSONObject skuJSONObject = _getSkuJSONObject(jsonObject);

		Assert.assertEquals("SKU-1", skuJSONObject.getString("sku"));
	}

	private void _testExportProductsWithMissingObjectDefinition()
		throws Exception {

		Mockito.when(
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_BASE_SKU,
					_COMPANY_ID)
		).thenReturn(
			null
		);

		try {
			_liferayCommercePIMConnector.exportProducts(
				_mockPIMConnectorObjectEntry());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertEquals(
				"Unable to get the base SKU object definition of company " +
					_COMPANY_ID,
				portalException.getMessage());
		}
	}

	private void _testExportProductsWithMixedNameMappings() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject fieldMappingJSONObject = _getFieldMappingJSONObject();

		JSONArray mappingsJSONArray = _getAttributeJSONArray("name");

		mappingsJSONArray.put(
			JSONUtil.put(
				"type", "fixedValue"
			).put(
				"value", "Acme"
			));

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				fieldMappingJSONObject.put(
					"name", mappingsJSONArray
				).toString()));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals(
			"SKU-1 name Acme", nameJSONObject.getString("en_US"));
	}

	private void _testExportProductsWithMultipleCatalogIdMappings()
		throws Exception {

		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject fieldMappingJSONObject = _getFieldMappingJSONObject();

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				fieldMappingJSONObject.put(
					"catalogId", _getFixedValueJSONArray(_CATALOG_ID, "99")
				).toString()));

		Assert.assertEquals(12345, jsonObject.getLong("catalogId"));
	}

	private void _testExportProductsWithMultipleDepotEntries()
		throws Exception {

		ObjectDefinition objectDefinition = _mockObjectDefinition();

		_mockGetObjectEntries(
			_GROUP_ID, objectDefinition, _mockObjectEntry("SKU-1", "SKU-1"));
		_mockGetObjectEntries(
			_OTHER_GROUP_ID, objectDefinition,
			_mockObjectEntry("SKU-2", "SKU-2"));

		_mockSpaceDepotEntries(_GROUP_ID, _OTHER_GROUP_ID);

		JSONArray jsonArray = _getJSONArray(_mockPIMConnectorObjectEntry());

		Assert.assertEquals(2, jsonArray.length());

		JSONObject jsonObject1 = jsonArray.getJSONObject(0);

		Assert.assertEquals(
			"SKU-1", jsonObject1.getString("externalReferenceCode"));

		JSONObject jsonObject2 = jsonArray.getJSONObject(1);

		Assert.assertEquals(
			"SKU-2", jsonObject2.getString("externalReferenceCode"));
	}

	private void _testExportProductsWithMultipleNameMappings()
		throws Exception {

		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject fieldMappingJSONObject = _getFieldMappingJSONObject();

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				fieldMappingJSONObject.put(
					"name", _getAttributeJSONArray("name", "code")
				).toString()));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals(
			"SKU-1 name SKU-1", nameJSONObject.getString("en_US"));
	}

	private void _testExportProductsWithMultipleTags() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject fieldMappingJSONObject = _getFieldMappingJSONObject();

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				fieldMappingJSONObject.put(
					"tags", _getAttributeJSONArray("code", "unitOfMeasureKey")
				).toString()));

		JSONArray tagsJSONArray = jsonObject.getJSONArray("tags");

		Assert.assertEquals(
			tagsJSONArray.toString(), 2, tagsJSONArray.length());

		Assert.assertEquals("SKU-1", tagsJSONArray.getString(0));
		Assert.assertEquals("box", tagsJSONArray.getString(1));
	}

	private void _testExportProductsWithoutTags() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject jsonObject = _getJSONObject(_mockPIMConnectorObjectEntry());

		Assert.assertFalse(jsonObject.has("tags"));
	}

	private void _testExportProductsWithoutUnitOfMeasure() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry(
				"SKU-1", "SKU-1",
				HashMapBuilder.<String, Serializable>put(
					"unitOfMeasureKey", ""
				).build()));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject skuJSONObject = _getSkuJSONObject(
			_getJSONObject(_mockPIMConnectorObjectEntry()));

		Assert.assertEquals("SKU-1", skuJSONObject.getString("sku"));
		Assert.assertFalse(skuJSONObject.has("skuUnitOfMeasures"));
	}

	private void _testExportProductsWithUnmappedDescription() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry("SKU-1", "SKU-1"));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject jsonObject = _getJSONObject(
			_mockPIMConnectorObjectEntry(
				_getFieldMapping(
					HashMapBuilder.put(
						"name", "name"
					).put(
						"skus[].sku", "code"
					).build(),
					_CATALOG_ID)));

		Assert.assertFalse(jsonObject.has("description"));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals("SKU-1 name", nameJSONObject.getString("en_US"));
	}

	private void _testExportProductsWithUnmappedRequiredFields()
		throws Exception {

		_mockObjectDefinition();

		try {
			_liferayCommercePIMConnector.exportProducts(
				_mockPIMConnectorObjectEntry("{}"));

			Assert.fail();
		}
		catch (PIMConnectorFieldMappingException
					pimConnectorFieldMappingException) {

			Assert.assertEquals(
				"Unable to export the products because the required fields " +
					"are not mapped: catalogId, name, skus[].sku",
				pimConnectorFieldMappingException.getMessage());
		}
	}

	private void _testExportProductsWithUnsupportedSource() throws Exception {
		_mockObjectDefinition();

		try {
			_liferayCommercePIMConnector.exportProducts(
				_mockPIMConnectorObjectEntry(
					JSONUtil.put(
						"name",
						JSONUtil.putAll(
							JSONUtil.put(
								"attribute", "name"
							).put(
								"source", "PIMApparelSku"
							))
					).toString()));

			Assert.fail();
		}
		catch (PIMConnectorFieldMappingException
					pimConnectorFieldMappingException) {

			Assert.assertEquals(
				"Unable to export the products because the field name is " +
					"mapped from the structure PIMApparelSku instead of " +
						"PIMBaseSku",
				pimConnectorFieldMappingException.getMessage());
		}
	}

	private void _testExportProductsWithVariantPIMLink() throws Exception {
		ObjectDefinition objectDefinition = _mockObjectDefinition();

		_mockGetObjectEntries(
			_GROUP_ID, objectDefinition, _mockObjectEntry("SKU-1", "ERC-1"),
			_mockObjectEntry("SKU-2", "ERC-2"),
			_mockObjectEntry("SKU-3", "ERC-3"));

		_mockSpaceDepotEntries(_GROUP_ID);

		_mockVariantPIMLinks("cluster-1", "ERC-1", "ERC-2");

		JSONArray jsonArray = _getJSONArray(_mockPIMConnectorObjectEntry());

		Assert.assertEquals(2, jsonArray.length());

		JSONObject jsonObject = jsonArray.getJSONObject(0);

		Assert.assertEquals(
			"ERC-1", jsonObject.getString("externalReferenceCode"));
		Assert.assertEquals("simple", jsonObject.getString("productType"));

		JSONObject nameJSONObject = jsonObject.getJSONObject("name");

		Assert.assertEquals("SKU-1 name", nameJSONObject.getString("en_US"));

		JSONArray skusJSONArray = jsonObject.getJSONArray("skus");

		Assert.assertEquals(2, skusJSONArray.length());

		JSONObject skuJSONObject = skusJSONArray.getJSONObject(0);

		Assert.assertEquals("SKU-1", skuJSONObject.getString("sku"));

		skuJSONObject = skusJSONArray.getJSONObject(1);

		Assert.assertEquals("SKU-2", skuJSONObject.getString("sku"));

		jsonObject = jsonArray.getJSONObject(1);

		Assert.assertEquals(
			"ERC-3", jsonObject.getString("externalReferenceCode"));
		Assert.assertEquals("simple", jsonObject.getString("productType"));

		skusJSONArray = jsonObject.getJSONArray("skus");

		Assert.assertEquals(1, skusJSONArray.length());

		skuJSONObject = skusJSONArray.getJSONObject(0);

		Assert.assertEquals("SKU-3", skuJSONObject.getString("sku"));
	}

	private void _testExportProductsWithVirtualSku() throws Exception {
		_mockGetObjectEntries(
			_GROUP_ID, _mockObjectDefinition(),
			_mockObjectEntry(
				"SKU-1", "SKU-1",
				HashMapBuilder.<String, Serializable>put(
					"virtual", true
				).build()));
		_mockSpaceDepotEntries(_GROUP_ID);

		JSONObject jsonObject = _getJSONObject(_mockPIMConnectorObjectEntry());

		Assert.assertEquals("virtual", jsonObject.getString("productType"));
	}

	private static final String _CATALOG_ID = "12345";

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _GROUP_ID = RandomTestUtil.randomLong();

	private static final long _OTHER_GROUP_ID = RandomTestUtil.randomLong();

	private final DepotEntryLocalService _depotEntryLocalService = Mockito.mock(
		DepotEntryLocalService.class);
	private final FilterFactory<Predicate> _filterFactory = Mockito.mock(
		FilterFactory.class);
	private final JSONFactory _jsonFactory = new JSONFactoryImpl();
	private final LiferayCommercePIMConnector _liferayCommercePIMConnector =
		new LiferayCommercePIMConnector();
	private final ObjectDefinitionLocalService _objectDefinitionLocalService =
		Mockito.mock(ObjectDefinitionLocalService.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);

}
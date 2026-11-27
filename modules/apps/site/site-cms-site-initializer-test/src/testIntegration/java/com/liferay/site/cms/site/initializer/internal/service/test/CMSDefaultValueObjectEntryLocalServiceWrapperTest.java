/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.thread.local.BatchEngineThreadLocal;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectDefinitionSettingConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.exception.ObjectValidationRuleEngineException;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.cms.site.initializer.test.util.CMSDefaultValueTestUtil;
import com.liferay.site.cms.site.initializer.util.CMSDefaultValueUtil;

import java.io.Serializable;

import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@FeatureFlag("LPD-112761")
@RunWith(Arquillian.class)
public class CMSDefaultValueObjectEntryLocalServiceWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		CMSDefaultValueTestUtil.processBatchEngineUnits(
			CMSDefaultValueObjectEntryLocalServiceWrapperTest.class);

		_listTypeDefinition =
			_listTypeDefinitionLocalService.addListTypeDefinition(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				false);

		_listTypeEntryLocalService.addListTypeEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			_listTypeDefinition.getListTypeDefinitionId(), "a",
			Collections.singletonMap(LocaleUtil.US, "A"), false);

		_objectDefinition = ObjectDefinitionTestUtil.publishObjectDefinition(
			"A" + RandomTestUtil.randomString(),
			ListUtil.fromArray(
				ObjectFieldUtil.createObjectField(
					_listTypeDefinition.getListTypeDefinitionId(),
					ObjectFieldConstants.BUSINESS_TYPE_PICKLIST, null,
					ObjectFieldConstants.DB_TYPE_STRING, false, false, null,
					"category", "category", false, false),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_TEXT,
					ObjectFieldConstants.DB_TYPE_STRING, "code"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_INTEGER,
					ObjectFieldConstants.DB_TYPE_INTEGER, "count"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_DATE,
					ObjectFieldConstants.DB_TYPE_DATE, "endDate"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN,
					ObjectFieldConstants.DB_TYPE_BOOLEAN, "flag"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_PRECISION_DECIMAL,
					ObjectFieldConstants.DB_TYPE_BIG_DECIMAL, "price"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_DATE,
					ObjectFieldConstants.DB_TYPE_DATE, "publishDate"),
				ObjectFieldUtil.createObjectField(
					ObjectFieldConstants.BUSINESS_TYPE_TEXT,
					ObjectFieldConstants.DB_TYPE_STRING, "text")),
			ObjectDefinitionConstants.SCOPE_DEPOT, TestPropsValues.getUserId());

		_objectDefinitionSettingLocalService.addObjectDefinitionSetting(
			_objectDefinition.getUserId(),
			_objectDefinition.getObjectDefinitionId(),
			ObjectDefinitionSettingConstants.NAME_ACCEPT_ALL_GROUPS,
			StringPool.TRUE);

		_depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		_group = _depotEntry.getGroup();

		ObjectEntryFolder rootObjectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					_group.getGroupId(), _group.getCompanyId());

		_parentObjectEntryFolder = _addObjectEntryFolder(
			_group, rootObjectEntryFolder.getObjectEntryFolderId());

		_objectEntryFolder = _addObjectEntryFolder(
			_group, _parentObjectEntryFolder.getObjectEntryFolderId());

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112761")) {

			CMSDefaultValueUtil.addOrUpdateObjectEntry(
				JSONUtil.putAll(
					_createJSONObject(
						"category", "Picklist", JSONUtil.put("key", "a")
					).put(
						"listTypeDefinitionERC",
						_listTypeDefinition.getExternalReferenceCode()
					),
					_createJSONObject(
						"code", "Text", RandomTestUtil.randomString(300)),
					_createJSONObject(
						"content", "RichText",
						JSONUtil.put(
							"en_US", "Space content"
						).put(
							"it_IT", RandomTestUtil.randomString()
						)),
					_createJSONObject("count", "Integer", 1),
					_createJSONObject(
						"endDate", "Date", RandomTestUtil.randomString()),
					_createJSONObject("flag", "Boolean", true),
					_createJSONObject(
						"price", "PrecisionDecimal",
						RandomTestUtil.randomString()),
					_createJSONObject("publishDate", "Date", "2026-12-01"),
					_createJSONObject("text", "Text", "Space text")),
				_depotEntry, _filterFactory, TestPropsValues.getUserId());
		}

		CMSDefaultValueUtil.addOrUpdateObjectEntry(
			JSONUtil.putAll(
				_createJSONObject("count", "Integer", 2),
				_createJSONObject("flag", "Boolean", null)),
			_filterFactory, _parentObjectEntryFolder,
			TestPropsValues.getUserId());
	}

	@Test
	public void testAddObjectEntry() throws Exception {
		_assertValues(
			"a", StringPool.BLANK, 2, true, "2026-12-01", "Space text",
			_addObjectEntryValues(new HashMap<>()));
		_assertValues(
			"a", StringPool.BLANK, 5, true, "2026-12-01", StringPool.BLANK,
			_addObjectEntryValues(
				HashMapBuilder.<String, Serializable>put(
					"count", 5
				).put(
					"text", StringPool.BLANK
				).build()));

		Map<String, Serializable> values = new HashMap<>();

		values.put("count", null);

		_assertValues(
			"a", StringPool.BLANK, 2, true, "2026-12-01", "Space text",
			_addObjectEntryValues(values));

		try (SafeCloseable safeCloseable =
				BatchEngineThreadLocal.setBatchImportInProcessWithSafeCloseable(
					true)) {

			values = _addObjectEntryValues(new HashMap<>());
		}

		_assertValues(
			StringPool.BLANK, StringPool.BLANK, 0, false, null,
			StringPool.BLANK, values);

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112761")) {

			values = _addObjectEntryValues(new HashMap<>());
		}

		_assertValues(
			StringPool.BLANK, StringPool.BLANK, 0, false, null,
			StringPool.BLANK, values);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_WEB_CONTENT", _group.getCompanyId());

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			_group.getGroupId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			_objectEntryFolder.getObjectEntryFolderId(),
			LocaleUtil.toLanguageId(LocaleUtil.US),
			HashMapBuilder.<String, Serializable>put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", "Title"
				).build()
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		values = objectEntry.getValues();

		Assert.assertEquals(
			"Space content", MapUtil.getString(values, "content"));
		Assert.assertEquals("Title", MapUtil.getString(values, "title"));

		objectDefinition = CMSDefaultValueUtil.fetchObjectDefinition(
			_group.getCompanyId());

		ObjectEntryFolder objectEntryFolder1 = _addObjectEntryFolder(
			_group, _objectEntryFolder.getObjectEntryFolderId());

		objectEntry = _objectEntryLocalService.addObjectEntry(
			_group.getGroupId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode",
				objectEntryFolder1.getExternalReferenceCode()
			).put(
				"className", ObjectEntryFolder.class.getName()
			).put(
				"defaultValues", "[]"
			).put(
				"treePath", "/0/"
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_assertTreePath(objectEntry, objectEntryFolder1.getTreePath());

		try {
			_addCMSDefaultValueObjectEntry(
				_parentObjectEntryFolder.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), "[]");

			Assert.fail();
		}
		catch (ModelListenerException modelListenerException) {
			Assert.assertTrue(
				modelListenerException.getCause() instanceof
					ObjectValidationRuleEngineException);
		}

		ObjectEntryFolder objectEntryFolder2 = _addObjectEntryFolder(
			_group, _objectEntryFolder.getObjectEntryFolderId());

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Class name must be ", DepotEntry.class.getName(), " or ",
					ObjectEntryFolder.class.getName()),
				"className"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				RandomTestUtil.randomString(), "[]"));

		try (SafeCloseable safeCloseable =
				BatchEngineThreadLocal.setBatchImportInProcessWithSafeCloseable(
					true)) {

			_assertFailure(
				new ObjectEntryValuesException.InvalidValue(
					StringBundler.concat(
						"Class name must be ", DepotEntry.class.getName(),
						" or ", ObjectEntryFolder.class.getName()),
					"className"),
				() -> _addCMSDefaultValueObjectEntry(
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(), "[]"));

			objectEntry = _addCMSDefaultValueObjectEntry(
				RandomTestUtil.randomString(),
				ObjectEntryFolder.class.getName(), "[]");
		}

		_assertTreePath(objectEntry, StringPool.BLANK);

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Class external reference code must be the external " +
					"reference code of the space",
				"classExternalReferenceCode"),
			() -> _addCMSDefaultValueObjectEntry(
				RandomTestUtil.randomString(), DepotEntry.class.getName(),
				"[]"));

		String externalReferenceCode = RandomTestUtil.randomString();

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"No folder exists with external reference code \"",
					externalReferenceCode, "\" in the space"),
				"classExternalReferenceCode"),
			() -> _addCMSDefaultValueObjectEntry(
				externalReferenceCode, ObjectEntryFolder.class.getName(),
				"[]"));

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		Group group = depotEntry.getGroup();

		ObjectEntryFolder rootObjectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					group.getGroupId(), group.getCompanyId());

		ObjectEntryFolder objectEntryFolder3 = _addObjectEntryFolder(
			group, rootObjectEntryFolder.getObjectEntryFolderId());

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"No folder exists with external reference code \"",
					objectEntryFolder3.getExternalReferenceCode(),
					"\" in the space"),
				"classExternalReferenceCode"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder3.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), "[]"));

		ExportImportThreadLocal.setPortletImportInProcess(true);

		try {
			objectEntry = _addCMSDefaultValueObjectEntry(
				RandomTestUtil.randomString(),
				ObjectEntryFolder.class.getName(), "[]");
		}
		finally {
			ExportImportThreadLocal.setPortletImportInProcess(false);
		}

		_assertTreePath(objectEntry, StringPool.BLANK);

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), "{"));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(),
				RandomTestUtil.randomString()));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), new ArrayList<>()));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Each default value must be a JSON object", "defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), "[1]"));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Each default value must have a name and a type",
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(),
				JSONUtil.put(
					JSONUtil.put("type", "Text")
				).toString()));

		String name = RandomTestUtil.randomString();

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Default value \"", name,
					"\" must have a list type definition external reference ",
					"code"),
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(),
				JSONUtil.put(
					_createJSONObject(
						name, "Picklist", JSONUtil.put("key", "a"))
				).toString()));

		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Boolean",
			"true");
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Date",
			RandomTestUtil.randomString());
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "DateTime",
			RandomTestUtil.randomString());
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Decimal",
			RandomTestUtil.randomString());
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Integer",
			1.5);
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Integer",
			Long.MAX_VALUE);
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "LongInteger",
			RandomTestUtil.randomString());
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "LongText", 1);
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Picklist",
			true);
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name,
			"PrecisionDecimal", RandomTestUtil.randomString());
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "RichText", 1);
		_assertInvalidDefaultValue(
			objectEntryFolder2.getExternalReferenceCode(), name, "Text",
			JSONUtil.put("en_US", 1));

		Assert.assertNull(
			CMSDefaultValueUtil.fetchObjectEntry(
				objectEntryFolder2.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(), _group.getCompanyId(),
				_filterFactory, _group.getGroupId()));

		JSONObject contentJSONObject = JSONUtil.put(
			"en_US", RandomTestUtil.randomString()
		).put(
			"es_ES", StringPool.BLANK
		).put(
			"it_IT", RandomTestUtil.randomString()
		);

		JSONArray jsonArray = JSONUtil.putAll(
			_createJSONObject(
				"category", "Picklist", JSONUtil.put("key", StringPool.BLANK)
			).put(
				"listTypeDefinitionERC", RandomTestUtil.randomString()
			),
			_createJSONObject("code", "Text", null),
			_createJSONObject("content", "RichText", contentJSONObject),
			_createJSONObject("count", "Integer", 1),
			_createJSONObject("endDate", "Date", "2026-12-01"),
			_createJSONObject("flag", "Boolean", false),
			_createJSONObject(
				"image", "Attachment",
				JSONUtil.put(
					"externalReferenceCode", RandomTestUtil.randomString())),
			_createJSONObject("price", "PrecisionDecimal", "1.5"),
			_createJSONObject("startDate", "DateTime", "2026-12-01T10:00:00Z"),
			_createJSONObject("text", "Text", StringPool.BLANK));

		objectEntry = _addCMSDefaultValueObjectEntry(
			objectEntryFolder2.getExternalReferenceCode(),
			ObjectEntryFolder.class.getName(), jsonArray.toString());

		contentJSONObject.remove("it_IT");

		Assert.assertEquals(
			jsonArray.toString(),
			MapUtil.getString(
				_objectEntryLocalService.getValues(
					objectEntry.getObjectEntryId()),
				"defaultValues"));
	}

	@Test
	public void testPartialUpdateObjectEntry() throws Exception {
		ObjectEntry objectEntry = CMSDefaultValueUtil.fetchObjectEntry(
			_parentObjectEntryFolder.getExternalReferenceCode(),
			ObjectEntryFolder.class.getName(), _group.getCompanyId(),
			_filterFactory, _group.getGroupId());

		_objectEntryLocalService.partialUpdateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"treePath", "/0/"
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_assertTreePath(objectEntry, _parentObjectEntryFolder.getTreePath());

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Class name must be ", DepotEntry.class.getName(), " or ",
					ObjectEntryFolder.class.getName()),
				"className"),
			() -> _partialUpdateObjectEntry(
				"className", objectEntry, RandomTestUtil.randomString()));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues"),
			() -> _partialUpdateObjectEntry("defaultValues", objectEntry, "["));

		String externalReferenceCode = RandomTestUtil.randomString();

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"No folder exists with external reference code \"",
					externalReferenceCode, "\" in the space"),
				"classExternalReferenceCode"),
			() -> _partialUpdateObjectEntry(
				"classExternalReferenceCode", objectEntry,
				externalReferenceCode));

		Assert.assertEquals(
			_parentObjectEntryFolder.getExternalReferenceCode(),
			MapUtil.getString(
				_objectEntryLocalService.getValues(
					objectEntry.getObjectEntryId()),
				"classExternalReferenceCode"));
	}

	@Test
	public void testUpdateObjectEntry() throws Exception {
		ObjectEntry objectEntry = CMSDefaultValueUtil.fetchObjectEntry(
			_group.getExternalReferenceCode(), DepotEntry.class.getName(),
			_group.getCompanyId(), _filterFactory, _group.getGroupId());

		_objectEntryLocalService.updateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", _group.getExternalReferenceCode()
			).put(
				"className", DepotEntry.class.getName()
			).put(
				"defaultValues", "[]"
			).put(
				"treePath", "/0/"
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_assertTreePath(objectEntry, StringPool.BLANK);

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Class external reference code must be the external " +
					"reference code of the space",
				"classExternalReferenceCode"),
			() -> _updateObjectEntry(
				RandomTestUtil.randomString(), "[]", objectEntry));
		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				"Default values must be a JSON array serialized as a string",
				"defaultValues"),
			() -> _updateObjectEntry(
				_group.getExternalReferenceCode(), "{", objectEntry));

		ExportImportThreadLocal.setPortletImportInProcess(true);

		try {
			_updateObjectEntry(
				RandomTestUtil.randomString(), "[]", objectEntry);
		}
		finally {
			ExportImportThreadLocal.setPortletImportInProcess(false);
		}

		Assert.assertEquals(
			_group.getExternalReferenceCode(),
			MapUtil.getString(
				_objectEntryLocalService.getValues(
					objectEntry.getObjectEntryId()),
				"classExternalReferenceCode"));
	}

	private ObjectEntry _addCMSDefaultValueObjectEntry(
			String classExternalReferenceCode, String className,
			Serializable defaultValues)
		throws Exception {

		ObjectDefinition objectDefinition =
			CMSDefaultValueUtil.fetchObjectDefinition(_group.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			_group.getGroupId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", classExternalReferenceCode
			).put(
				"className", className
			).put(
				"defaultValues", defaultValues
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private ObjectEntryFolder _addObjectEntryFolder(
			Group group, long parentObjectEntryFolderId)
		throws Exception {

		return _objectEntryFolderLocalService.addObjectEntryFolder(
			RandomTestUtil.randomString(), group.getGroupId(),
			group.getCreatorUserId(), parentObjectEntryFolderId,
			StringPool.BLANK,
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext());
	}

	private Map<String, Serializable> _addObjectEntryValues(
			Map<String, Serializable> values)
		throws Exception {

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			_group.getGroupId(), TestPropsValues.getUserId(),
			_objectDefinition.getObjectDefinitionId(),
			_objectEntryFolder.getObjectEntryFolderId(),
			LocaleUtil.toLanguageId(LocaleUtil.US), values,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		return objectEntry.getValues();
	}

	private void _assertFailure(
		Exception exception1, UnsafeRunnable<Exception> unsafeRunnable) {

		try {
			unsafeRunnable.run();

			Assert.fail();
		}
		catch (Exception exception2) {
			Assert.assertEquals(exception1.getClass(), exception2.getClass());
			Assert.assertEquals(
				exception1.getMessage(), exception2.getMessage());
		}
	}

	private void _assertInvalidDefaultValue(
		String classExternalReferenceCode, String name, String type,
		Object value) {

		_assertFailure(
			new ObjectEntryValuesException.InvalidValue(
				StringBundler.concat(
					"Default value \"", name, "\" is not a valid ", type,
					" value"),
				"defaultValues"),
			() -> _addCMSDefaultValueObjectEntry(
				classExternalReferenceCode, ObjectEntryFolder.class.getName(),
				JSONUtil.put(
					_createJSONObject(
						name, type, value
					).put(
						"listTypeDefinitionERC", RandomTestUtil.randomString()
					)
				).toString()));
	}

	private void _assertTreePath(ObjectEntry objectEntry, String treePath)
		throws Exception {

		Assert.assertEquals(
			treePath,
			MapUtil.getString(
				_objectEntryLocalService.getValues(
					objectEntry.getObjectEntryId()),
				"treePath"));
	}

	private void _assertValues(
		String category, String code, int count, boolean flag,
		String publishDate, String text, Map<String, Serializable> values) {

		Assert.assertEquals(category, MapUtil.getString(values, "category"));
		Assert.assertEquals(code, MapUtil.getString(values, "code"));
		Assert.assertEquals(count, MapUtil.getInteger(values, "count"));
		Assert.assertNull(values.get("endDate"));
		Assert.assertEquals(flag, MapUtil.getBoolean(values, "flag"));
		Assert.assertEquals(BigDecimal.ZERO, values.get("price"));

		Date date = (Date)values.get("publishDate");

		if (publishDate == null) {
			Assert.assertNull(date);
		}
		else {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			Assert.assertEquals(publishDate, dateFormat.format(date));
		}

		Assert.assertEquals(text, MapUtil.getString(values, "text"));
	}

	private JSONObject _createJSONObject(
		String name, String type, Object value) {

		return JSONUtil.put(
			"name", name
		).put(
			"type", type
		).put(
			"value", value
		);
	}

	private void _partialUpdateObjectEntry(
			String name, ObjectEntry objectEntry, Serializable value)
		throws Exception {

		_objectEntryLocalService.partialUpdateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				name, value
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private void _updateObjectEntry(
			String classExternalReferenceCode, String defaultValues,
			ObjectEntry objectEntry)
		throws Exception {

		_objectEntryLocalService.updateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", classExternalReferenceCode
			).put(
				"className", DepotEntry.class.getName()
			).put(
				"defaultValues", defaultValues
			).build(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private DepotEntry _depotEntry;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	private Group _group;
	private ListTypeDefinition _listTypeDefinition;

	@Inject
	private ListTypeDefinitionLocalService _listTypeDefinitionLocalService;

	@Inject
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectDefinitionSettingLocalService
		_objectDefinitionSettingLocalService;

	private ObjectEntryFolder _objectEntryFolder;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private ObjectEntryFolder _parentObjectEntryFolder;

}
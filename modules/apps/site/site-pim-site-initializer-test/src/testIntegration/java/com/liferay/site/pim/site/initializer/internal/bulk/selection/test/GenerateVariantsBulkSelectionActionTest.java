/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.bulk.selection.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.bulk.selection.BulkSelectionFactory;
import com.liferay.bulk.selection.constants.BulkSelectionActionStatusConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.object.constants.ObjectFieldSettingConstants;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.field.setting.builder.ObjectFieldSettingBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.pim.site.initializer.constants.PIMObjectEntryFolderConstants;
import com.liferay.site.pim.site.initializer.engine.PIMLinkEngine;
import com.liferay.site.pim.site.initializer.link.PIMLinkRelatedEntry;
import com.liferay.site.pim.site.initializer.test.util.PIMProductTypeTestUtil;
import com.liferay.site.pim.site.initializer.test.util.PIMTestUtil;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Balazs Breier
 */
@FeatureFlag("LPD-96666")
@RunWith(Arquillian.class)
public class GenerateVariantsBulkSelectionActionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		PIMTestUtil.getOrAddGroup();

		_bulkActionTaskObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BULK_ACTION_TASK", TestPropsValues.getCompanyId());
		_depotEntry = PIMTestUtil.addSpaceDepotEntry();

		ObjectField objectField =
			PIMProductTypeTestUtil.createPicklistObjectField(
				"color", "Black", "White");

		objectField.setLocalized(true);

		_objectDefinition =
			PIMProductTypeTestUtil.addProductTypeObjectDefinition(
				Arrays.asList(
					objectField,
					new TextObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).name(
						"description"
					).build(),
					PIMProductTypeTestUtil.createPicklistObjectField(
						"size", "M", "XL"),
					new TextObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).name(
						"sku"
					).objectFieldSettings(
						Collections.singletonList(
							new ObjectFieldSettingBuilder(
							).name(
								ObjectFieldSettingConstants.NAME_UNIQUE_VALUES
							).value(
								Boolean.TRUE.toString()
							).build())
					).build(),
					new TextObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).name(
						"title"
					).build()));

		PIMProductTypeTestUtil.addObjectLayout(
			_objectDefinition, "title", "sku", "color", "size", "description");

		_objectEntryFolder =
			_objectEntryFolderLocalService.
				fetchObjectEntryFolderByExternalReferenceCode(
					PIMObjectEntryFolderConstants.
						EXTERNAL_REFERENCE_CODE_PRODUCTS,
					_depotEntry.getGroupId(), TestPropsValues.getCompanyId());
	}

	@Test
	public void testExecute() throws Exception {
		_testExecute();
		_testExecuteWithEmptyOptionValue();
		_testExecuteWithTrashedObjectEntry();
		_testExecuteWithVariantFamily();
		_testExecuteWithoutUpdatePermission();
	}

	private ObjectEntry _addObjectEntry(
			String color, String size, String sku, String title)
		throws Exception {

		return _objectEntryLocalService.addObjectEntry(
			_depotEntry.getGroupId(), TestPropsValues.getUserId(),
			_objectDefinition.getObjectDefinitionId(),
			_objectEntryFolder.getObjectEntryFolderId(), null,
			HashMapBuilder.<String, Serializable>put(
				"color", color
			).put(
				"color_i18n",
				HashMapBuilder.put(
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()), color
				).build()
			).put(
				"description", RandomTestUtil.randomString()
			).put(
				"size", size
			).put(
				"sku", sku
			).put(
				"title", title
			).build(),
			ServiceContextTestUtil.getServiceContext(_depotEntry.getGroupId()));
	}

	private void _assertBulkActionTask(
		int numberOfFailedItems, int numberOfItems, int numberOfSuccessfulItems,
		Map<String, Serializable> values) {

		Assert.assertEquals(
			BulkSelectionActionStatusConstants.COMPLETED,
			values.get("executionStatus"));
		Assert.assertEquals(
			numberOfFailedItems,
			GetterUtil.getInteger(values.get("numberOfFailedItems")));
		Assert.assertEquals(
			numberOfItems, GetterUtil.getInteger(values.get("numberOfItems")));
		Assert.assertEquals(
			numberOfSuccessfulItems,
			GetterUtil.getInteger(values.get("numberOfSuccessfulItems")));
	}

	private Map<String, Serializable> _execute(
			ObjectEntry objectEntry, User user, String... objectFieldNames)
		throws Exception {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(TestPropsValues.getCompanyId());
		serviceContext.setUserId(TestPropsValues.getUserId());

		ObjectEntry bulkActionTaskObjectEntry =
			_objectEntryLocalService.addObjectEntry(
				0, TestPropsValues.getUserId(),
				_bulkActionTaskObjectDefinition.getObjectDefinitionId(), 0,
				LocaleUtil.toLanguageId(LocaleUtil.US),
				HashMapBuilder.<String, Serializable>put(
					"actionName", "GenerateVariantsBulkSelectionAction"
				).put(
					"executionStatus",
					BulkSelectionActionStatusConstants.INITIAL
				).put(
					"numberOfItems", 1
				).put(
					"type", "GenerateVariantsBulkSelectionAction"
				).build(),
				serviceContext);

		_generateVariantsBulkSelectionAction.execute(
			user, new TestBulkSelection(objectEntry),
			HashMapBuilder.<String, Serializable>put(
				"bulkActionTaskId", bulkActionTaskObjectEntry.getObjectEntryId()
			).put(
				"objectFieldNames", objectFieldNames
			).build());

		bulkActionTaskObjectEntry = _objectEntryLocalService.getObjectEntry(
			bulkActionTaskObjectEntry.getObjectEntryId());

		return bulkActionTaskObjectEntry.getValues();
	}

	private List<PIMLinkRelatedEntry> _getPIMLinkRelatedEntries(
			ObjectEntry objectEntry)
		throws Exception {

		return _pimLinkEngine.getPIMLinkRelatedEntries(
			"type eq 'variant'", objectEntry);
	}

	private String _getSingleTaskResultId(Map<String, Serializable> values)
		throws Exception {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray(
			(String)values.get("taskResult"));

		Assert.assertEquals(jsonArray.toString(), 1, jsonArray.length());

		JSONObject jsonObject = jsonArray.getJSONObject(0);

		return jsonObject.getString("id");
	}

	private List<String> _getValues(ObjectEntry objectEntry, String... names)
		throws Exception {

		return ListUtil.sort(
			TransformUtil.transform(
				_getPIMLinkRelatedEntries(objectEntry),
				pimLinkRelatedEntry -> {
					ObjectEntry variantObjectEntry =
						pimLinkRelatedEntry.getObjectEntry();

					Map<String, Serializable> values =
						variantObjectEntry.getValues();

					return StringUtil.merge(
						TransformUtil.transform(
							names, name -> MapUtil.getString(values, name),
							String.class),
						StringPool.SPACE);
				}));
	}

	private void _testExecute() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(
			"Black", "M", "LebronShortsBlackM", "Lebron Shorts");

		_assertBulkActionTask(
			0, 3, 3,
			_execute(objectEntry, TestPropsValues.getUser(), "color", "size"));

		Assert.assertEquals(
			Arrays.asList(
				"Black M LebronShortsBlackM", "Black XL LebronShortsBlackXL",
				"White M LebronShortsWhiteM", "White XL LebronShortsWhiteXL"),
			_getValues(objectEntry, "color", "size", "sku"));

		Map<String, Serializable> values = objectEntry.getValues();

		for (PIMLinkRelatedEntry pimLinkRelatedEntry :
				_getPIMLinkRelatedEntries(objectEntry)) {

			ObjectEntry variantObjectEntry =
				pimLinkRelatedEntry.getObjectEntry();

			if (Objects.equals(objectEntry, variantObjectEntry)) {
				continue;
			}

			Assert.assertFalse(pimLinkRelatedEntry.isPrimary());

			Assert.assertEquals(
				objectEntry.getObjectEntryFolderId(),
				variantObjectEntry.getObjectEntryFolderId());
			Assert.assertEquals(
				WorkflowConstants.STATUS_DRAFT, variantObjectEntry.getStatus());

			Map<String, Serializable> variantValues =
				variantObjectEntry.getValues();

			Assert.assertEquals(
				values.get("description"), variantValues.get("description"));
			Assert.assertEquals(
				values.get("title"), variantValues.get("title"));
		}
	}

	private void _testExecuteWithEmptyOptionValue() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(
			"Black", null, RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		_assertBulkActionTask(
			0, 4, 4,
			_execute(objectEntry, TestPropsValues.getUser(), "color", "size"));
	}

	private void _testExecuteWithTrashedObjectEntry() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(
			"Black", "M", RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		objectEntry = _objectEntryLocalService.moveObjectEntryToTrash(
			TestPropsValues.getUserId(), objectEntry,
			ServiceContextTestUtil.getServiceContext(_depotEntry.getGroupId()));

		Map<String, Serializable> values = _execute(
			objectEntry, TestPropsValues.getUser(), "color", "size");

		_assertBulkActionTask(1, 1, 0, values);

		Assert.assertEquals(
			"NoSuchObjectEntryException", _getSingleTaskResultId(values));
	}

	private void _testExecuteWithVariantFamily() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(
			"Black", "M", "LebronSocksBlackM", "Lebron Socks");

		ObjectEntry variantObjectEntry = _addObjectEntry(
			"White", "M", "LebronSocksBlackXL", "Lebron Socks");

		_pimLinkEngine.addPIMLinks(
			objectEntry, Collections.singletonList(variantObjectEntry),
			"variant");

		_pimLinkEngine.setPrimaryPIMLink(objectEntry, "variant");

		_objectEntryLocalService.moveObjectEntryToTrash(
			TestPropsValues.getUserId(), variantObjectEntry,
			ServiceContextTestUtil.getServiceContext(_depotEntry.getGroupId()));

		_addObjectEntry("Black", "M", "LebronSocksWhiteXL", "Lebron Socks");

		Map<String, Serializable> values = _execute(
			objectEntry, TestPropsValues.getUser(), "color", "size");

		_assertBulkActionTask(1, 3, 2, values);

		Assert.assertEquals(
			"UniqueValueConstraintViolation", _getSingleTaskResultId(values));

		Assert.assertEquals(
			Arrays.asList(
				"LebronSocksBlackM", "LebronSocksBlackXL",
				"LebronSocksBlackXL1"),
			_getValues(objectEntry, "sku"));

		for (PIMLinkRelatedEntry pimLinkRelatedEntry :
				_getPIMLinkRelatedEntries(objectEntry)) {

			Assert.assertEquals(
				Objects.equals(
					objectEntry, pimLinkRelatedEntry.getObjectEntry()),
				pimLinkRelatedEntry.isPrimary());
		}
	}

	private void _testExecuteWithoutUpdatePermission() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(
			"Black", "M", RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		RoleTestUtil.addResourcePermission(
			role, _objectDefinition.getClassName(),
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()), ActionKeys.VIEW);

		User user = UserTestUtil.addUser();

		_userLocalService.addRoleUser(role.getRoleId(), user);

		String name = PrincipalThreadLocal.getName();
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(user));
			PrincipalThreadLocal.setName(user.getUserId());

			Map<String, Serializable> values = _execute(
				objectEntry, user, "color", "size");

			_assertBulkActionTask(3, 3, 0, values);

			Assert.assertEquals(
				"MustHavePermission", _getSingleTaskResultId(values));
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}

		Assert.assertEquals(
			Collections.emptyList(), _getPIMLinkRelatedEntries(objectEntry));
	}

	private ObjectDefinition _bulkActionTaskObjectDefinition;
	private DepotEntry _depotEntry;

	@Inject(
		filter = "component.name=com.liferay.site.pim.site.initializer.internal.bulk.selection.GenerateVariantsBulkSelectionAction"
	)
	private BulkSelectionAction<Object> _generateVariantsBulkSelectionAction;

	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectEntryFolder _objectEntryFolder;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private PIMLinkEngine _pimLinkEngine;

	@Inject
	private UserLocalService _userLocalService;

	private static class TestBulkSelection implements BulkSelection<Object> {

		@Override
		public <E extends PortalException> void forEach(
				UnsafeConsumer<Object, E> unsafeConsumer)
			throws PortalException {

			unsafeConsumer.accept(_object);
		}

		@Override
		public Class<? extends BulkSelectionFactory>
			getBulkSelectionFactoryClass() {

			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.emptyMap();
		}

		@Override
		public long getSize() {
			return 1;
		}

		@Override
		public Serializable serialize() {
			return null;
		}

		@Override
		public BulkSelection<AssetEntry> toAssetEntryBulkSelection() {
			return null;
		}

		private TestBulkSelection(Object object) {
			_object = object;
		}

		private final Object _object;

	}

}
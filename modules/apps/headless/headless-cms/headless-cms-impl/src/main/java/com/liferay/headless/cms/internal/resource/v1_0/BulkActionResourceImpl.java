/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.resource.v1_0;

import com.liferay.headless.batch.engine.dto.v1_0.ImportTask;
import com.liferay.headless.batch.engine.resource.v1_0.ImportTaskResource;
import com.liferay.headless.cms.dto.v1_0.BulkAction;
import com.liferay.headless.cms.dto.v1_0.BulkActionItem;
import com.liferay.headless.cms.dto.v1_0.BulkActionTask;
import com.liferay.headless.cms.dto.v1_0.DeleteBulkAction;
import com.liferay.headless.cms.dto.v1_0.KeywordBulkAction;
import com.liferay.headless.cms.dto.v1_0.TaxonomyCategoryBulkAction;
import com.liferay.headless.cms.internal.odata.entity.v1_0.BulkActionEntityModel;
import com.liferay.headless.cms.resource.v1_0.BulkActionResource;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.resource.v1_0.SearchResultResource;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.validation.ValidationException;

import jakarta.ws.rs.core.MultivaluedMap;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Crescenzo Rega
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/bulk-action.properties",
	scope = ServiceScope.PROTOTYPE, service = BulkActionResource.class
)
public class BulkActionResourceImpl extends BaseBulkActionResourceImpl {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap)
		throws Exception {

		return _entityModel;
	}

	@Override
	public BulkActionTask postBulkAction(
			String search, Filter filter, BulkAction bulkAction)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-17564")) {

			throw new UnsupportedOperationException();
		}

		BulkAction.Type type = bulkAction.getType();

		if (BulkAction.Type.DELETE_BULK_ACTION.equals(type)) {
			return _executeDeleteBulkAction(
				bulkAction,
				_getBulkActionItemsMap(
					bulkAction.getBulkActionItems(), filter, search,
					GetterUtil.getBoolean(bulkAction.getSelectAll())));
		}
		else if (BulkAction.Type.KEYWORD_BULK_ACTION.equals(type)) {
			return _executeKeywordBulkAction(
				bulkAction,
				_getBulkActionItemsMap(
					bulkAction.getBulkActionItems(), filter, search,
					GetterUtil.getBoolean(bulkAction.getSelectAll())));
		}
		else if (BulkAction.Type.TAXONOMY_CATEGORY_BULK_ACTION.equals(type)) {
			return _executeTaxonomyCategoryBulkAction(
				bulkAction,
				_getBulkActionItemsMap(
					bulkAction.getBulkActionItems(), filter, search,
					GetterUtil.getBoolean(bulkAction.getSelectAll())));
		}

		throw new UnsupportedOperationException();
	}

	private BulkActionTask _addBulkActionTask(String type) throws Exception {
		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, contextUser.getUserId(), _getBulkActionTaskObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"actionName", type
			).put(
				"executionStatus", "initial"
			).put(
				"type", type
			).build(),
			new ServiceContext());

		Map<String, Serializable> values = objectEntry.getValues();

		return new BulkActionTask() {
			{
				setActionName(
					() -> GetterUtil.getString(values.get("actionName")));
				setAuthor(objectEntry::getUserName);
				setCreatedDate(objectEntry::getCreateDate);
				setExecuteStatus(
					() -> GetterUtil.getString(values.get("executionStatus")));
				setExternalReferenceCode(objectEntry::getExternalReferenceCode);
				setId(objectEntry::getObjectEntryId);
				setType(() -> GetterUtil.getString(values.get("type")));
			}
		};
	}

	private void _addBulkActionTaskItems(
			BulkActionTask bulkActionTask,
			Map.Entry<String, List<BulkActionItem>> entry,
			ImportTask importTask, String taskItemDelegateName)
		throws Exception {

		for (BulkActionItem bulkActionItem : entry.getValue()) {
			_objectEntryLocalService.addObjectEntry(
				0, contextUser.getUserId(),
				_getBulkActionTaskItemObjectDefinitionId(),
				ObjectEntryFolderConstants.
					PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
				null,
				HashMapBuilder.<String, Serializable>put(
					"classExternalReferenceCode",
					bulkActionItem.getClassExternalReferenceCode()
				).put(
					"classPK", bulkActionItem.getClassPK()
				).put(
					"executionStatus",
					StringUtil.toLowerCase(
						importTask.getExecuteStatusAsString())
				).put(
					"importTaskId", importTask.getId()
				).put(
					"name", bulkActionItem.getName()
				).put(
					"r_bulkActionTaskToBulkActionTaskItems_c_bulkActionTaskId",
					bulkActionTask.getId()
				).put(
					"type",
					(taskItemDelegateName != null) ? taskItemDelegateName :
						"ObjectEntryFolder"
				).build(),
				new ServiceContext());
		}
	}

	private ImportTaskResource _createImportTaskResource() {
		return _importTaskResourceFactory.create(
		).httpServletRequest(
			contextHttpServletRequest
		).httpServletResponse(
			contextHttpServletResponse
		).uriInfo(
			contextUriInfo
		).user(
			contextUser
		).build();
	}

	private BulkActionTask _executeDeleteBulkAction(
			BulkAction bulkAction,
			Map<String, List<BulkActionItem>> bulkActionItemsMap)
		throws Exception {

		if (MapUtil.isEmpty(bulkActionItemsMap)) {
			return new BulkActionTask();
		}

		DeleteBulkAction deleteBulkAction = (DeleteBulkAction)bulkAction;

		BulkActionTask bulkActionTask = _addBulkActionTask(
			deleteBulkAction.getTypeAsString());

		ImportTaskResource importTaskResource = _createImportTaskResource();

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemsMap.entrySet()) {

			String taskItemDelegateName = _getTaskItemDelegateName(
				entry.getKey());

			ImportTask importTask = importTaskResource.deleteImportTaskObject(
				_getClassName(entry.getKey()), null, null, "ON_ERROR_CONTINUE",
				taskItemDelegateName,
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).build()));

			_addBulkActionTaskItems(
				bulkActionTask, entry, importTask, taskItemDelegateName);
		}

		return bulkActionTask;
	}

	private BulkActionTask _executeKeywordBulkAction(
			BulkAction bulkAction,
			Map<String, List<BulkActionItem>> bulkActionItemsMap)
		throws Exception {

		KeywordBulkAction keywordBulkAction = (KeywordBulkAction)bulkAction;

		String[] keywords = keywordBulkAction.getKeywords();

		if (MapUtil.isEmpty(bulkActionItemsMap) ||
			ArrayUtil.isEmpty(keywords)) {

			return new BulkActionTask();
		}

		BulkActionTask bulkActionTask = _addBulkActionTask(
			keywordBulkAction.getTypeAsString());

		ImportTaskResource importTaskResource = _createImportTaskResource();

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemsMap.entrySet()) {

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder",
					entry.getKey())) {

				continue;
			}

			String taskItemDelegateName = _getTaskItemDelegateName(
				entry.getKey());

			ImportTask importTask = _putImportTaskObject(
				entry, importTaskResource,
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"keywords", keywords
					).build()),
				taskItemDelegateName);

			_addBulkActionTaskItems(
				bulkActionTask, entry, importTask, taskItemDelegateName);
		}

		return bulkActionTask;
	}

	private BulkActionTask _executeTaxonomyCategoryBulkAction(
			BulkAction bulkAction,
			Map<String, List<BulkActionItem>> bulkActionItemsMap)
		throws Exception {

		TaxonomyCategoryBulkAction taxonomyCategoryBulkAction =
			(TaxonomyCategoryBulkAction)bulkAction;

		Long[] taxonomyCategoryIds =
			taxonomyCategoryBulkAction.getTaxonomyCategoryIds();

		if (MapUtil.isEmpty(bulkActionItemsMap) ||
			ArrayUtil.isEmpty(taxonomyCategoryIds)) {

			return new BulkActionTask();
		}

		BulkActionTask bulkActionTask = _addBulkActionTask(
			taxonomyCategoryBulkAction.getTypeAsString());

		ImportTaskResource importTaskResource = _createImportTaskResource();

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemsMap.entrySet()) {

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder",
					entry.getKey())) {

				continue;
			}

			String taskItemDelegateName = _getTaskItemDelegateName(
				entry.getKey());

			ImportTask importTask = _putImportTaskObject(
				entry, importTaskResource,
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"taxonomyCategoryIds", taxonomyCategoryIds
					).build()),
				taskItemDelegateName);

			_addBulkActionTaskItems(
				bulkActionTask, entry, importTask, taskItemDelegateName);
		}

		return bulkActionTask;
	}

	private Map<String, List<BulkActionItem>> _getBulkActionItemsMap(
			BulkActionItem[] bulkActionItems, Filter filter, String search,
			boolean selectAll)
		throws Exception {

		Map<String, List<BulkActionItem>> bulkActionItemsMap = new HashMap<>();

		if (selectAll && ArrayUtil.isEmpty(bulkActionItems)) {
			if (filter == null) {
				throw new ValidationException("Filter is null");
			}

			SearchResultResource searchResultResource =
				_searchResultResourceFactory.create(
				).httpServletRequest(
					contextHttpServletRequest
				).httpServletResponse(
					contextHttpServletResponse
				).preferredLocale(
					contextAcceptLanguage.getPreferredLocale()
				).uriInfo(
					contextUriInfo
				).user(
					contextUser
				).build();

			int page = 1;

			Page<SearchResult> searchPage = _getSearchResultPage(
				filter, search, searchResultResource, page, bulkActionItemsMap);

			while (searchPage.hasNext()) {
				page = page + 1;

				searchPage = _getSearchResultPage(
					filter, search, searchResultResource, page,
					bulkActionItemsMap);
			}

			return bulkActionItemsMap;
		}

		if (ArrayUtil.isEmpty(bulkActionItems)) {
			return bulkActionItemsMap;
		}

		for (BulkActionItem bulkActionItem : bulkActionItems) {
			bulkActionItemsMap.computeIfAbsent(
				bulkActionItem.getClassName(), key -> new ArrayList<>()
			).add(
				bulkActionItem
			);
		}

		return bulkActionItemsMap;
	}

	private long _getBulkActionTaskItemObjectDefinitionId() throws Exception {
		if (_bulkActionTaskItemObjectDefinition != null) {
			return _bulkActionTaskItemObjectDefinition.getObjectDefinitionId();
		}

		_bulkActionTaskItemObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_BULK_ACTION_TASK_ITEM", contextCompany.getCompanyId());

		return _bulkActionTaskItemObjectDefinition.getObjectDefinitionId();
	}

	private long _getBulkActionTaskObjectDefinitionId() throws Exception {
		if (_bulkActionTaskObjectDefinition != null) {
			return _bulkActionTaskObjectDefinition.getObjectDefinitionId();
		}

		_bulkActionTaskObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_BULK_ACTION_TASK", contextCompany.getCompanyId());

		return _bulkActionTaskObjectDefinition.getObjectDefinitionId();
	}

	private String _getClassName(String key) {
		if (StringUtil.equals(
				"com.liferay.object.model.ObjectEntryFolder", key)) {

			return "com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
		}

		return "com.liferay.object.rest.dto.v1_0.ObjectEntry";
	}

	private Page<SearchResult> _getSearchResultPage(
			Filter filter, String search,
			SearchResultResource searchResultResource, int page,
			Map<String, List<BulkActionItem>> bulkActionItemsMap)
		throws Exception {

		Page<SearchResult> searchPage = searchResultResource.getSearchPage(
			null, true, null, null, search, filter, Pagination.of(page, 500),
			null);

		for (SearchResult searchResult : searchPage.getItems()) {
			JSONObject jsonObject = _jsonFactory.createJSONObject(
				String.valueOf(searchResult.getEmbedded()));

			bulkActionItemsMap.computeIfAbsent(
				searchResult.getEntryClassName(), key -> new ArrayList<>()
			).add(
				new BulkActionItem() {
					{
						setClassExternalReferenceCode(
							() -> jsonObject.getString(
								"externalReferenceCode"));
						setClassPK(() -> jsonObject.getLong("id"));
					}
				}
			);
		}

		return searchPage;
	}

	private String _getTaskItemDelegateName(String key) throws Exception {
		if (StringUtil.equals(
				"com.liferay.object.model.ObjectEntryFolder", key)) {

			return null;
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinitionByClassName(
				contextCompany.getCompanyId(), key);

		return objectDefinition.getName();
	}

	private ImportTask _putImportTaskObject(
			Map.Entry<String, List<BulkActionItem>> entry,
			ImportTaskResource importTaskResource,
			List<HashMap<String, Object>> object, String taskItemDelegateName)
		throws Exception {

		return importTaskResource.putImportTaskObject(
			_getClassName(entry.getKey()), null, null,
			ImportTask.ImportStrategy.ON_ERROR_CONTINUE.getValue(),
			taskItemDelegateName, "PARTIAL_UPDATE", object);
	}

	private static final EntityModel _entityModel = new BulkActionEntityModel();

	private ObjectDefinition _bulkActionTaskItemObjectDefinition;
	private ObjectDefinition _bulkActionTaskObjectDefinition;

	@Reference
	private ImportTaskResource.Factory _importTaskResourceFactory;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private SearchResultResource.Factory _searchResultResourceFactory;

}
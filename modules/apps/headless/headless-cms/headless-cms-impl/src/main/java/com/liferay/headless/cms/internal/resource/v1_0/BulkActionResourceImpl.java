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
import com.liferay.headless.cms.dto.v1_0.KeywordBulkAction;
import com.liferay.headless.cms.dto.v1_0.MoveBulkAction;
import com.liferay.headless.cms.dto.v1_0.PermissionBulkAction;
import com.liferay.headless.cms.dto.v1_0.TaxonomyCategoryBulkAction;
import com.liferay.headless.cms.internal.odata.entity.v1_0.BulkActionEntityModel;
import com.liferay.headless.cms.resource.v1_0.BulkActionResource;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.resource.v1_0.SearchResultResource;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.permission.Permission;

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

		return _bulkActionEntityModel;
	}

	@Override
	public BulkActionTask postBulkAction(
			String search, Filter filter, BulkAction bulkAction)
		throws Exception {

		Map<String, List<BulkActionItem>> bulkActionItemMap =
			_getBulkActionItemMap(
				bulkAction.getBulkActionItems(), filter, search,
				bulkAction.getSelectAll());

		if (MapUtil.isEmpty(bulkActionItemMap)) {
			throw new UnsupportedOperationException();
		}

		BulkAction.Type type = bulkAction.getType();

		if (BulkAction.Type.TAXONOMY_CATEGORY_BULK_ACTION.equals(type)) {
			TaxonomyCategoryBulkAction taxonomyCategoryBulkAction =
				(TaxonomyCategoryBulkAction)bulkAction;

			return _executeTaxonomyCategoryBulkAction(
				bulkActionItemMap,
				taxonomyCategoryBulkAction.getTaxonomyCategoryIds());
		}

		if (BulkAction.Type.DELETE_BULK_ACTION.equals(type)) {
			return _executeDeleteBulkAction(bulkActionItemMap);
		}

		if (BulkAction.Type.MOVE_BULK_ACTION.equals(type)) {
			MoveBulkAction moveBulkAction = (MoveBulkAction)bulkAction;

			return _executeMoveBulkAction(
				bulkActionItemMap, moveBulkAction.getObjectEntryFolderId());
		}

		if (BulkAction.Type.PERMISSION_BULK_ACTION.equals(type)) {
			PermissionBulkAction permissionBulkAction =
				(PermissionBulkAction)bulkAction;

			return _executePermissionBulkAction(
				bulkActionItemMap, permissionBulkAction.getPermissions());
		}

		if (BulkAction.Type.KEYWORD_BULK_ACTION.equals(type)) {
			KeywordBulkAction keywordBulkAction = (KeywordBulkAction)bulkAction;

			return _executeKeywordBulkAction(
				bulkActionItemMap, keywordBulkAction.getKeywords());
		}

		throw new UnsupportedOperationException();
	}

	private BulkActionTask _addBulkActionTask(
			String actionName, String executeStatus, String type)
		throws Exception {

		// #TODO This method will implement writing inside
		//  the object defined in LPD-60079

		ObjectDefinition bulkActionTaskObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_BULK_ACTION_TASK", contextCompany.getCompanyId());

		if (bulkActionTaskObjectDefinition == null) {
			return null;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, contextUser.getUserId(),
			bulkActionTaskObjectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"actionName", actionName
			).put(
				"executionStatus", executeStatus
			).put(
				"type", type
			).build(),
			new ServiceContext());

		return new BulkActionTask() {
			{
				setActionName(actionName);
				setAuthor(objectEntry.getUserName());
				setCreatedDate(objectEntry.getCreateDate());
				setExternalReferenceCode(
					objectEntry.getExternalReferenceCode());
				setExecuteStatus(executeStatus);
				setId(objectEntry.getObjectEntryId());
				setType(type);
			}
		};
	}

	private void _addBulkActionTaskItem(
			long bulkActionTaskId, String classExternalReferenceCode,
			Long classPK, String executionStatus, long importTaskID,
			String name, String type)
		throws Exception {

		// #TODO This method will implement writing inside
		//  the object defined in LPD-60079

		ObjectDefinition bulkActionTaskItemObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_BULK_ACTION_TASK_ITEM", contextCompany.getCompanyId());

		if (bulkActionTaskItemObjectDefinition == null) {
			return;
		}

		_objectEntryLocalService.addObjectEntry(
			0, contextUser.getUserId(),
			bulkActionTaskItemObjectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			null,
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", classExternalReferenceCode
			).put(
				"classPK", classPK
			).put(
				"executionStatus", executionStatus
			).put(
				"importTaskID", importTaskID
			).put(
				"name", name
			).put(
				"r_bulkActionTaskToBulkActionTaskItems_c_bulkActionTaskId",
				bulkActionTaskId
			).put(
				"type", type
			).build(),
			new ServiceContext());
	}

	private BulkActionTask _executeDeleteBulkAction(
			Map<String, List<BulkActionItem>> bulkActionItemMap)
		throws Exception {

		BulkActionTask bulkActionTask = _addBulkActionTask(
			"DELETE", "STARTED", "DELETE");

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = null;

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder", key)) {

				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
			}
			else {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.
						fetchObjectDefinitionByClassName(
							contextCompany.getCompanyId(), key);

				taskItemDelegateName = objectDefinition.getName();
			}

			ImportTask importTask =
				_getImportTaskResource().deleteImportTaskObject(
					className, null, null, "ON_ERROR_CONTINUE",
					taskItemDelegateName,
					transform(
						entry.getValue(),
						bulkActionItem -> HashMapBuilder.<String, Object>put(
							"id", bulkActionItem.getClassPK()
						).build()));

			for (BulkActionItem bulkActionItem : entry.getValue()) {
				_addBulkActionTaskItem(
					bulkActionTask.getId(),
					bulkActionItem.getClassExternalReferenceCode(),
					bulkActionItem.getClassPK(),
					importTask.getExecuteStatusAsString(), importTask.getId(),
					bulkActionItem.getName(),
					taskItemDelegateName != null ? taskItemDelegateName :
						"ObjectEntryFolder");
			}
		}

		return bulkActionTask;
	}

	private BulkActionTask _executeKeywordBulkAction(
			Map<String, List<BulkActionItem>> bulkActionItemMap,
			String[] keywords)
		throws Exception {

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemMap.entrySet()) {

			String key = entry.getKey();

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder", key)) {

				continue;
			}

			_putImportTaskObject(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				_getImportTaskResource(),
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"keywords", keywords
					).build()),
				key);
		}

		return _addBulkActionTask("KEYWORDS", "STARTED", "KEYWORDS");
	}

	private BulkActionTask _executeMoveBulkAction(
			Map<String, List<BulkActionItem>> bulkActionItemMap,
			long objectEntryFolderId)
		throws Exception {

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemMap.entrySet()) {

			String taskItemDelegateName = entry.getKey();

			if (StringUtil.equals("ObjectEntryFolder", taskItemDelegateName)) {

				// @TODO SISTEMA

				continue;
			}

			_putImportTaskObject(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				_getImportTaskResource(),
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"objectEntryFolderId", objectEntryFolderId
					).build()),
				taskItemDelegateName);
		}

		return _addBulkActionTask("MOVE", "STARTED", "MOVE");
	}

	private BulkActionTask _executePermissionBulkAction(
			Map<String, List<BulkActionItem>> bulkActionItemMap,
			Permission[] permissions)
		throws Exception {

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = null;

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder", key)) {

				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
			}
			else {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.
						fetchObjectDefinitionByClassName(
							contextCompany.getCompanyId(), key);

				taskItemDelegateName = objectDefinition.getName();
			}

			_putImportTaskObject(
				className, _getImportTaskResource(),
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"permissions",
						transformToList(
							permissions,
							permission -> HashMapBuilder.<String, Object>put(
								"actionIds",
								ListUtil.fromArray(permission.getActionIds())
							).put(
								"roleExternalReferenceCode",
								permission.getRoleExternalReferenceCode()
							).put(
								"roleName", permission.getRoleName()
							).put(
								"roleType", permission.getRoleType()
							).build())
					).build()),
				taskItemDelegateName);
		}

		return _addBulkActionTask("PERMISSION", "STARTED", "PERMISSION");
	}

	private BulkActionTask _executeTaxonomyCategoryBulkAction(
			Map<String, List<BulkActionItem>> bulkActionItemMap,
			Long[] taxonomyCategoryIds)
		throws Exception {

		for (Map.Entry<String, List<BulkActionItem>> entry :
				bulkActionItemMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = null;

			if (StringUtil.equals(
					"com.liferay.object.model.ObjectEntryFolder", key)) {

				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
			}
			else {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.
						fetchObjectDefinitionByClassName(
							contextCompany.getCompanyId(), key);

				taskItemDelegateName = objectDefinition.getName();
			}

			_putImportTaskObject(
				className, _getImportTaskResource(),
				transform(
					entry.getValue(),
					bulkActionItem -> HashMapBuilder.<String, Object>put(
						"id", bulkActionItem.getClassPK()
					).put(
						"taxonomyCategoryIds", taxonomyCategoryIds
					).build()),
				taskItemDelegateName);
		}

		return _addBulkActionTask(
			"TAXONOMY_CATEGORY", "STARTED", "TAXONOMY_CATEGORY");
	}

	private Map<String, List<BulkActionItem>> _getBulkActionItemMap(
			BulkActionItem[] bulkActionItems, Filter filter, String search,
			boolean selectAll)
		throws Exception {

		Map<String, List<BulkActionItem>> bulkActionItemMap = new HashMap<>();

		if (selectAll && ArrayUtil.isEmpty(bulkActionItems)) {
			_searchResultResource.setContextAcceptLanguage(
				contextAcceptLanguage);
			_searchResultResource.setContextCompany(contextCompany);
			_searchResultResource.setContextHttpServletResponse(
				contextHttpServletResponse);
			_searchResultResource.setContextHttpServletRequest(
				contextHttpServletRequest);
			_searchResultResource.setContextUser(contextUser);
			_searchResultResource.setContextUriInfo(contextUriInfo);

			// #TODO pagination management is missing

			Page<SearchResult> searchPage = _searchResultResource.getSearchPage(
				null, true, null, null, search, filter, Pagination.of(1, 100),
				null);

			for (SearchResult searchResult : searchPage.getItems()) {
				JSONObject jsonObject = _jsonFactory.createJSONObject(
					String.valueOf(searchResult.getEmbedded()));

				bulkActionItemMap.computeIfAbsent(
					searchResult.getEntryClassName(), key -> new ArrayList<>()
				).add(
					new BulkActionItem() {
						{
							setClassPK(jsonObject.getLong("id"));
							setExternalReferenceCode(
								jsonObject.getString("externalReferenceCode"));
						}
					}
				);
			}

			return bulkActionItemMap;
		}

		for (BulkActionItem bulkActionItem : bulkActionItems) {
			bulkActionItemMap.computeIfAbsent(
				bulkActionItem.getClassName(), key -> new ArrayList<>()
			).add(
				bulkActionItem
			);
		}

		return bulkActionItemMap;
	}

	private ImportTaskResource _getImportTaskResource() {
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

	private Long _putImportTaskObject(
			String className, ImportTaskResource importTaskResource,
			Object object, String taskItemDelegateName)
		throws Exception {

		ImportTask importTask = importTaskResource.putImportTaskObject(
			className, null, null,
			ImportTask.ImportStrategy.ON_ERROR_CONTINUE.getValue(),
			taskItemDelegateName, "PARTIAL_UPDATE", object);

		return importTask.getId();
	}

	private final BulkActionEntityModel _bulkActionEntityModel =
		new BulkActionEntityModel();

	@Reference
	private ImportTaskResource.Factory _importTaskResourceFactory;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private SearchResultResource _searchResultResource;

}
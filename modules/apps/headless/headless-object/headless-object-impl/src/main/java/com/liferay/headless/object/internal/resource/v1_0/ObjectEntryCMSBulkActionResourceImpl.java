/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.object.internal.resource.v1_0;

import com.liferay.headless.batch.engine.dto.v1_0.ImportTask;
import com.liferay.headless.batch.engine.resource.v1_0.ImportTaskResource;
import com.liferay.headless.object.dto.v1_0.BatchEngineJobResponse;
import com.liferay.headless.object.dto.v1_0.ObjectEntryCMSBulkActionRequest;
import com.liferay.headless.object.dto.v1_0.ObjectEntryCMSBulkActionResponse;
import com.liferay.headless.object.internal.odata.entity.v1_0.ObjectEntryCMSBulkActionEntityModel;
import com.liferay.headless.object.resource.v1_0.ObjectEntryCMSBulkActionResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.rest.dto.v1_0.SearchResult;
import com.liferay.portal.search.rest.resource.v1_0.SearchResultResource;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.permission.Permission;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luca Pellizzon
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/object-entry-cms-bulk-action.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ObjectEntryCMSBulkActionResource.class
)
public class ObjectEntryCMSBulkActionResourceImpl
	extends BaseObjectEntryCMSBulkActionResourceImpl
	implements EntityModelResource {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _objectEntryCMSBulkActionEntityModel;
	}

	@Override
	public ObjectEntryCMSBulkActionResponse postObjectEntryCMSBulkAction(
			String bulkActionName, String search, Filter filter,
			ObjectEntryCMSBulkActionRequest objectEntryCMSBulkActionRequest)
		throws Exception {

		Long[] ids = objectEntryCMSBulkActionRequest.getIds();

		if (ArrayUtil.isEmpty(ids)) {
			List<Long> searchResultIDs = new ArrayList<>();

			_searchResultResource.setContextAcceptLanguage(
				contextAcceptLanguage);
			_searchResultResource.setContextCompany(contextCompany);
			_searchResultResource.setContextHttpServletResponse(
				contextHttpServletResponse);
			_searchResultResource.setContextHttpServletRequest(
				contextHttpServletRequest);
			_searchResultResource.setContextUser(contextUser);

			Page<SearchResult> searchPage = _searchResultResource.getSearchPage(
				null, true, null, null, search, filter, Pagination.of(1, 100),
				null);

			for (SearchResult searchResult : searchPage.getItems()) {
				JSONObject jsonObject = _jsonFactory.createJSONObject(
					String.valueOf(searchResult.getEmbedded()));

				searchResultIDs.add(jsonObject.getLong("id"));
			}

			ids = searchResultIDs.toArray(new Long[0]);
		}

		// Organize IDs by their ObjectDefinition

		Map<String, Map<Long, List<Long>>> map = new HashMap<>();

		for (long id : ids) {
			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				id);

			if (objectEntry != null) {
				map.computeIfAbsent(
					"ObjectEntry", key -> new HashMap<>()
				).computeIfAbsent(
					objectEntry.getObjectDefinitionId(),
					objectEntryId -> new ArrayList<>()
				).add(
					id
				);
			}
			else {
				ObjectEntryFolder objectEntryFolder =
					_objectEntryFolderLocalService.fetchObjectEntryFolder(id);

				if (objectEntryFolder != null) {
					map.computeIfAbsent(
						"ObjectEntryFolder", key -> new HashMap<>()
					).computeIfAbsent(
						0L, key -> new ArrayList<>()
					).add(
						id
					);
				}
			}
		}

		// Call the batch endpoint of each ObjectDefinition

		if (bulkActionName.equals("change-status")) {
			return _changeStatus(
				map, objectEntryCMSBulkActionRequest.getStatus());
		}

		if (bulkActionName.equals("keywords")) {
			return _keywords(
				map, objectEntryCMSBulkActionRequest.getKeywords());
		}

		if (bulkActionName.equals("move")) {
			return _move(
				map, objectEntryCMSBulkActionRequest.getObjectEntryFolderId());
		}

		if (bulkActionName.equals("permissions")) {
			return _permissions(
				map, objectEntryCMSBulkActionRequest.getPermissions());
		}

		if (bulkActionName.equals("remove")) {
			return _remove(map);
		}

		if (bulkActionName.equals("taxonomy-categories")) {
			return _taxonomyCategories(
				map, objectEntryCMSBulkActionRequest.getTaxonomyCategoryIds());
		}

		throw new UnsupportedOperationException();
	}

	private ObjectEntryCMSBulkActionResponse _changeStatus(
			Map<String, Map<Long, List<Long>>> entriesIdByObjectDefinition,
			Integer status)
		throws Exception {

		List<BatchEngineJobResponse> results = new ArrayList<>();

		if (entriesIdByObjectDefinition.containsKey("ObjectEntry")) {
			ImportTaskResource importTaskResource = _getImportTaskResource();

			Map<Long, List<Long>> objectEntries =
				entriesIdByObjectDefinition.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entries :
					objectEntries.entrySet()) {

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entries.getKey());

				List<Long> ids = entries.getValue();

				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _putImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry", ids,
					importTaskResource,
					transform(
						ids,
						id -> HashMapBuilder.<String, Object>put(
							"id", id
						).put(
							"status", status
						).build()),
					taskItemDelegateName);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (entriesIdByObjectDefinition.containsKey("ObjectEntryFolder")) {
			throw new UnsupportedOperationException();
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private Long _deleteImportTaskObject(
			String className, ImportTaskResource importTaskResource,
			String taskItemDelegateName, List<Long> ids)
		throws Exception {

		ImportTask importTask = importTaskResource.deleteImportTaskObject(
			className, null, null, "ON_ERROR_CONTINUE", taskItemDelegateName,
			transform(
				ids,
				id -> HashMapBuilder.put(
					"id", id
				).build()));

		return importTask.getId();
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

	private ObjectEntryCMSBulkActionResponse _keywords(
			Map<String, Map<Long, List<Long>>> map, String[] keywords)
		throws Exception {

		List<BatchEngineJobResponse> results = new ArrayList<>();

		if (map.containsKey("ObjectEntry")) {
			ImportTaskResource importTaskResource = _getImportTaskResource();

			Map<Long, List<Long>> objectEntries = map.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entries :
					objectEntries.entrySet()) {

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entries.getKey());

				List<Long> ids = entries.getValue();

				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _putImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry", ids,
					importTaskResource,
					transform(
						ids,
						id -> HashMapBuilder.<String, Object>put(
							"id", id
						).put(
							"keywords", keywords
						).build()),
					taskItemDelegateName);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (map.containsKey("ObjectEntryFolder")) {
			throw new UnsupportedOperationException();
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private ObjectEntryCMSBulkActionResponse _move(
			Map<String, Map<Long, List<Long>>> entriesIdByObjectDefinition,
			Long objectEntryFolderId)
		throws Exception {

		ImportTaskResource importTaskResource = _getImportTaskResource();
		List<BatchEngineJobResponse> results = new ArrayList<>();

		if (entriesIdByObjectDefinition.containsKey("ObjectEntry")) {
			Map<Long, List<Long>> objectEntries =
				entriesIdByObjectDefinition.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entries :
					objectEntries.entrySet()) {

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entries.getKey());

				List<Long> ids = entries.getValue();

				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _putImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry", ids,
					importTaskResource,
					transform(
						ids,
						id -> HashMapBuilder.<String, Object>put(
							"id", id
						).put(
							"objectEntryFolderId", objectEntryFolderId
						).build()),
					taskItemDelegateName);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (entriesIdByObjectDefinition.containsKey("ObjectEntryFolder")) {
			Map<Long, List<Long>> objectEntryFolders =
				entriesIdByObjectDefinition.get("ObjectEntryFolder");

			List<Long> ids = objectEntryFolders.get(0L);

			Long batchId = _putImportTaskObject(
				"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder", ids,
				importTaskResource,
				transform(
					ids,
					id -> HashMapBuilder.<String, Object>put(
						"id", id
					).put(
						"parentObjectEntryFolderId", objectEntryFolderId
					).build()),
				null);

			results.add(
				_toBatchEngineJobResponse(batchId, "ObjectEntryFolder", ids));
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private ObjectEntryCMSBulkActionResponse _permissions(
			Map<String, Map<Long, List<Long>>> entriesIdByObjectDefinition,
			Permission[] permissions)
		throws Exception {

		ImportTaskResource importTaskResource = _getImportTaskResource();
		List<BatchEngineJobResponse> results = new ArrayList<>();

		if (entriesIdByObjectDefinition.containsKey("ObjectEntry")) {
			Map<Long, List<Long>> objectEntries =
				entriesIdByObjectDefinition.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entries :
					objectEntries.entrySet()) {

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entries.getKey());

				List<Long> ids = entries.getValue();

				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _putImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry", ids,
					importTaskResource,
					transform(
						ids,
						id -> HashMapBuilder.<String, Object>put(
							"id", id
						).put(
							"permissions",
							transformToList(
								permissions,
								permission ->
									HashMapBuilder.<String, Object>put(
										"actionIds",
										ListUtil.fromArray(
											permission.getActionIds())
									).put(
										"roleExternalReferenceCode",
										permission.
											getRoleExternalReferenceCode()
									).put(
										"roleName", permission.getRoleName()
									).put(
										"roleType", permission.getRoleType()
									).build())
						).build()),
					taskItemDelegateName);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (entriesIdByObjectDefinition.containsKey("ObjectEntryFolder")) {
			Map<Long, List<Long>> objectEntryFolders =
				entriesIdByObjectDefinition.get("ObjectEntryFolder");

			List<Long> ids = objectEntryFolders.get(0L);

			Long batchId = _putImportTaskObject(
				"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder", ids,
				importTaskResource,
				transform(
					ids,
					id -> HashMapBuilder.<String, Object>put(
						"id", id
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
				null);

			results.add(
				_toBatchEngineJobResponse(batchId, "ObjectEntryFolder", ids));
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private Long _putImportTaskObject(
			String className, List<Long> ids,
			ImportTaskResource importTaskResource, Object object,
			String taskItemDelegateName)
		throws Exception {

		if (ListUtil.isEmpty(ids)) {
			return null;
		}

		ImportTask importTask = importTaskResource.putImportTaskObject(
			className, null, null,
			ImportTask.ImportStrategy.ON_ERROR_CONTINUE.getValue(),
			taskItemDelegateName, "PARTIAL_UPDATE", object);

		return importTask.getId();
	}

	private ObjectEntryCMSBulkActionResponse _remove(
			Map<String, Map<Long, List<Long>>> map)
		throws Exception {

		List<BatchEngineJobResponse> results = new ArrayList<>();
		ImportTaskResource importTaskResource = _getImportTaskResource();

		if (map.containsKey("ObjectEntry")) {
			Map<Long, List<Long>> objectEntries = map.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entry : objectEntries.entrySet()) {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entry.getKey());

				List<Long> ids = entry.getValue();
				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _deleteImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry",
					importTaskResource, taskItemDelegateName, ids);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (map.containsKey("ObjectEntryFolder")) {
			Map<Long, List<Long>> objectEntryFolders = map.get(
				"ObjectEntryFolder");

			List<Long> ids = objectEntryFolders.get(0L);

			Long batchId = _deleteImportTaskObject(
				"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder",
				importTaskResource, null, ids);

			results.add(
				_toBatchEngineJobResponse(batchId, "ObjectEntryFolder", ids));
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private ObjectEntryCMSBulkActionResponse _taxonomyCategories(
			Map<String, Map<Long, List<Long>>> map, Long[] taxonomyCategoryIds)
		throws Exception {

		List<BatchEngineJobResponse> results = new ArrayList<>();

		if (map.containsKey("ObjectEntry")) {
			ImportTaskResource importTaskResource = _getImportTaskResource();

			Map<Long, List<Long>> objectEntries = map.get("ObjectEntry");

			for (Map.Entry<Long, List<Long>> entries :
					objectEntries.entrySet()) {

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						entries.getKey());

				List<Long> ids = entries.getValue();

				String taskItemDelegateName = objectDefinition.getName();

				Long batchId = _putImportTaskObject(
					"com.liferay.object.rest.dto.v1_0.ObjectEntry", ids,
					importTaskResource,
					transform(
						ids,
						id -> HashMapBuilder.<String, Object>put(
							"id", id
						).put(
							"taxonomyCategoryIds", taxonomyCategoryIds
						).build()),
					taskItemDelegateName);

				results.add(
					_toBatchEngineJobResponse(
						batchId, taskItemDelegateName, ids));
			}
		}

		if (map.containsKey("ObjectEntryFolder")) {
			throw new UnsupportedOperationException();
		}

		return new ObjectEntryCMSBulkActionResponse() {
			{
				setBatchEngineJobResponses(
					() -> results.toArray(new BatchEngineJobResponse[0]));
			}
		};
	}

	private BatchEngineJobResponse _toBatchEngineJobResponse(
		Long batchId, String className, List<Long> ids) {

		return new BatchEngineJobResponse() {
			{
				setBatchId(() -> batchId);
				setObjectType(() -> className);
				setProcessedIds(() -> ids.toArray(new Long[0]));
			}
		};
	}

	@Reference
	private ImportTaskResource.Factory _importTaskResourceFactory;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private final ObjectEntryCMSBulkActionEntityModel
		_objectEntryCMSBulkActionEntityModel =
			new ObjectEntryCMSBulkActionEntityModel();

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private SearchResultResource _searchResultResource;

}
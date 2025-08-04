/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.resource.v1_0;

import com.liferay.headless.batch.engine.dto.v1_0.ImportTask;
import com.liferay.headless.batch.engine.resource.v1_0.ImportTaskResource;
import com.liferay.headless.cms.dto.v1_0.BulkActionTask;
import com.liferay.headless.cms.dto.v1_0.CMSEntryDefinition;
import com.liferay.headless.cms.dto.v1_0.CategoryDefinition;
import com.liferay.headless.cms.dto.v1_0.MoveDefinition;
import com.liferay.headless.cms.dto.v1_0.PermissionDefinition;
import com.liferay.headless.cms.dto.v1_0.TagDefinition;
import com.liferay.headless.cms.resource.v1_0.BulkActionResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.rest.resource.v1_0.SearchResultResource;
import com.liferay.portal.vulcan.permission.Permission;

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
	public BulkActionTask deleteBulkAction(
			String search, Filter filter,
			CMSEntryDefinition[] cmsEntryDefinitions)
		throws Exception {

		if (ArrayUtil.isEmpty(cmsEntryDefinitions)) {

			// #TODO selectAll

		}

		Map<String, List<Long>> cmsEntryDefinitionMap =
			_getCMSEntryDefinitionMap(cmsEntryDefinitions);

		for (Map.Entry<String, List<Long>> entry :
				cmsEntryDefinitionMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = key;

			if (StringUtil.equals("ObjectEntryFolder", key)) {
				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
				taskItemDelegateName = null;
			}

			_getImportTaskResource().deleteImportTaskObject(
				className, null, null, "ON_ERROR_CONTINUE",
				taskItemDelegateName,
				transform(
					entry.getValue(),
					id -> HashMapBuilder.put(
						"id", id
					).build()));
		}

		return super.deleteBulkAction(search, filter, cmsEntryDefinitions);
	}

	@Override
	public BulkActionTask postBulkActionCategory(
			String search, Filter filter, CategoryDefinition categoryDefinition)
		throws Exception {

		CMSEntryDefinition[] cmsEntryDefinitions =
			categoryDefinition.getCmsEntryDefinitions();

		if (ArrayUtil.isEmpty(cmsEntryDefinitions)) {

			// #TODO selectAll

		}

		Map<String, List<Long>> cmsEntryDefinitionMap =
			_getCMSEntryDefinitionMap(cmsEntryDefinitions);

		Long[] taxonomyCategoryIds =
			categoryDefinition.getTaxonomyCategoryIds();

		for (Map.Entry<String, List<Long>> entry :
				cmsEntryDefinitionMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = key;

			if (StringUtil.equals("ObjectEntryFolder", key)) {
				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
				taskItemDelegateName = null;
			}

			_putImportTaskObject(
				className, entry.getValue(), _getImportTaskResource(),
				transform(
					entry.getValue(),
					id -> HashMapBuilder.<String, Object>put(
						"id", id
					).put(
						"taxonomyCategoryIds", taxonomyCategoryIds
					).build()),
				taskItemDelegateName);
		}

		return super.postBulkActionCategory(search, filter, categoryDefinition);
	}

	@Override
	public BulkActionTask postBulkActionMove(
			String search, Filter filter, MoveDefinition moveDefinition)
		throws Exception {

		CMSEntryDefinition[] cmsEntryDefinitions =
			moveDefinition.getCmsEntryDefinitions();

		if (ArrayUtil.isEmpty(cmsEntryDefinitions)) {

			// #TODO selectAll

		}

		Map<String, List<Long>> cmsEntryDefinitionMap =
			_getCMSEntryDefinitionMap(cmsEntryDefinitions);

		for (Map.Entry<String, List<Long>> entry :
				cmsEntryDefinitionMap.entrySet()) {

			String key = entry.getKey();

			if (StringUtil.equals("ObjectEntryFolder", key)) {
				continue;
			}

			_putImportTaskObject(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				entry.getValue(), _getImportTaskResource(),
				transform(
					entry.getValue(),
					id -> HashMapBuilder.<String, Object>put(
						"id", id
					).put(
						"objectEntryFolderId",
						moveDefinition.getObjectEntryFolderId()
					).build()),
				key);
		}

		return super.postBulkActionMove(search, filter, moveDefinition);
	}

	@Override
	public BulkActionTask postBulkActionPermission(
			String search, Filter filter,
			PermissionDefinition permissionDefinition)
		throws Exception {

		CMSEntryDefinition[] cmsEntryDefinitions =
			permissionDefinition.getCmsEntryDefinitions();

		if (ArrayUtil.isEmpty(cmsEntryDefinitions)) {

			// #TODO selectAll

		}

		Map<String, List<Long>> cmsEntryDefinitionMap =
			_getCMSEntryDefinitionMap(cmsEntryDefinitions);

		ImportTaskResource importTaskResource = _getImportTaskResource();
		Permission[] permissions = permissionDefinition.getPermissions();

		for (Map.Entry<String, List<Long>> entry :
				cmsEntryDefinitionMap.entrySet()) {

			String key = entry.getKey();

			String className = "com.liferay.object.rest.dto.v1_0.ObjectEntry";
			String taskItemDelegateName = key;

			if (StringUtil.equals("ObjectEntryFolder", key)) {
				className =
					"com.liferay.headless.object.dto.v1_0.ObjectEntryFolder";
				taskItemDelegateName = null;
			}

			_putImportTaskObject(
				className, entry.getValue(), importTaskResource,
				transform(
					entry.getValue(),
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
				taskItemDelegateName);
		}

		return super.postBulkActionPermission(
			search, filter, permissionDefinition);
	}

	@Override
	public BulkActionTask postBulkActionTag(
			String search, Filter filter, TagDefinition tagDefinition)
		throws Exception {

		CMSEntryDefinition[] cmsEntryDefinitions =
			tagDefinition.getCmsEntryDefinitions();

		if (ArrayUtil.isEmpty(cmsEntryDefinitions)) {

			// #TODO selectAll

		}

		Map<String, List<Long>> cmsEntryDefinitionMap =
			_getCMSEntryDefinitionMap(cmsEntryDefinitions);

		String[] keywords = tagDefinition.getKeywords();

		for (Map.Entry<String, List<Long>> entry :
				cmsEntryDefinitionMap.entrySet()) {

			String key = entry.getKey();

			if (StringUtil.equals("ObjectEntryFolder", key)) {
				continue;
			}

			_putImportTaskObject(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				entry.getValue(), _getImportTaskResource(),
				transform(
					entry.getValue(),
					id -> HashMapBuilder.<String, Object>put(
						"id", id
					).put(
						"keywords", keywords
					).build()),
				key);
		}

		return super.postBulkActionTag(search, filter, tagDefinition);
	}

	private Map<String, List<Long>> _getCMSEntryDefinitionMap(
			CMSEntryDefinition[] cmsEntryDefinitions)
		throws Exception {

		Map<String, List<Long>> entriesIdByObjectDefinition = new HashMap<>();

		for (CMSEntryDefinition cmsEntryDefinition : cmsEntryDefinitions) {
			String className = cmsEntryDefinition.getClassName();
			Long id = cmsEntryDefinition.getId();

			if (StringUtil.equals("ObjectEntry", className)) {
				ObjectEntry objectEntry =
					_objectEntryLocalService.fetchObjectEntry(id);

				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.getObjectDefinition(
						objectEntry.getObjectDefinitionId());

				entriesIdByObjectDefinition.computeIfAbsent(
					objectDefinition.getName(),
					objectEntryId -> new ArrayList<>()
				).add(
					id
				);
			}
			else if (StringUtil.equals("ObjectEntryFolder", className)) {
				entriesIdByObjectDefinition.computeIfAbsent(
					"ObjectEntryFolder", key -> new ArrayList<>()
				).add(
					id
				);
			}
		}

		return entriesIdByObjectDefinition;
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
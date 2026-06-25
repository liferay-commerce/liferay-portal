/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.security.permission.resource;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Tancredi Covioli
 */
public class DSRArchivedRoomFolderModelResourcePermission
	implements ModelResourcePermission<Folder> {

	public DSRArchivedRoomFolderModelResourcePermission(
		ModelResourcePermission<Folder> modelResourcePermission,
		DLFolderLocalService dlFolderLocalService,
		GroupLocalService groupLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_modelResourcePermission = modelResourcePermission;
		_dlFolderLocalService = dlFolderLocalService;
		_groupLocalService = groupLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;

		_portletResourcePermission =
			new DSRArchivedRoomFolderPortletResourcePermission(
				modelResourcePermission.getPortletResourcePermission(),
				groupLocalService, objectDefinitionLocalService,
				objectEntryLocalService);
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, Folder folder, String actionId)
		throws PortalException {

		if (!contains(permissionChecker, folder, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, _modelResourcePermission.getModelName(),
				folder.getFolderId(), actionId);
		}
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		if (!contains(permissionChecker, primaryKey, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, _modelResourcePermission.getModelName(),
				primaryKey, actionId);
		}
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, Folder folder, String actionId)
		throws PortalException {

		if (_readOnlyDeniedActionIds.contains(actionId) &&
			_isArchivedRoomReadOnly(permissionChecker, folder.getGroupId())) {

			return false;
		}

		return _modelResourcePermission.contains(
			permissionChecker, folder, actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		if (_readOnlyDeniedActionIds.contains(actionId)) {
			DLFolder dlFolder = _dlFolderLocalService.fetchDLFolder(primaryKey);

			if ((dlFolder != null) &&
				_isArchivedRoomReadOnly(
					permissionChecker, dlFolder.getGroupId())) {

				return false;
			}
		}

		return _modelResourcePermission.contains(
			permissionChecker, primaryKey, actionId);
	}

	@Override
	public String getModelName() {
		return _modelResourcePermission.getModelName();
	}

	@Override
	public PortletResourcePermission getPortletResourcePermission() {
		return _portletResourcePermission;
	}

	private boolean _isArchivedRoomReadOnly(
		PermissionChecker permissionChecker, long groupId) {

		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return false;
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", group.getCompanyId());

		if ((objectDefinition == null) ||
			!Objects.equals(
				group.getClassName(), objectDefinition.getClassName()) ||
			permissionChecker.isCompanyAdmin()) {

			return false;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			group.getClassPK());

		if (objectEntry == null) {
			return false;
		}

		if (MapUtil.getInteger(objectEntry.getValues(), "roomStatus") ==
				WorkflowConstants.STATUS_INACTIVE) {

			return true;
		}

		return false;
	}

	private final DLFolderLocalService _dlFolderLocalService;
	private final GroupLocalService _groupLocalService;
	private final ModelResourcePermission<Folder> _modelResourcePermission;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final PortletResourcePermission _portletResourcePermission;
	private final Set<String> _readOnlyDeniedActionIds = new HashSet<>(
		Arrays.asList(
			ActionKeys.ADD_DOCUMENT, ActionKeys.ADD_FOLDER,
			ActionKeys.ADD_SHORTCUT, ActionKeys.ADD_SUBFOLDER,
			ActionKeys.DELETE, ActionKeys.UPDATE));

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.security.permission.resource;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
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
public class DSRArchivedRoomFolderPortletResourcePermission
	implements PortletResourcePermission {

	public DSRArchivedRoomFolderPortletResourcePermission(
		PortletResourcePermission portletResourcePermission,
		GroupLocalService groupLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_portletResourcePermission = portletResourcePermission;
		_groupLocalService = groupLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, Group group, String actionId)
		throws PrincipalException {

		if (!contains(permissionChecker, group, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, getResourceName(), group.getGroupId(),
				actionId);
		}
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, long groupId, String actionId)
		throws PrincipalException {

		if (!contains(permissionChecker, groupId, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, getResourceName(), groupId, actionId);
		}
	}

	@Override
	public boolean contains(
		PermissionChecker permissionChecker, Group group, String actionId) {

		if (_readOnlyDeniedActionIds.contains(actionId) &&
			_isArchivedRoomReadOnly(permissionChecker, group.getGroupId())) {

			return false;
		}

		return _portletResourcePermission.contains(
			permissionChecker, group, actionId);
	}

	@Override
	public boolean contains(
		PermissionChecker permissionChecker, long groupId, String actionId) {

		if (_readOnlyDeniedActionIds.contains(actionId) &&
			_isArchivedRoomReadOnly(permissionChecker, groupId)) {

			return false;
		}

		return _portletResourcePermission.contains(
			permissionChecker, groupId, actionId);
	}

	@Override
	public String getResourceName() {
		return _portletResourcePermission.getResourceName();
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

	private final GroupLocalService _groupLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final PortletResourcePermission _portletResourcePermission;
	private final Set<String> _readOnlyDeniedActionIds = new HashSet<>(
		Arrays.asList(
			ActionKeys.ADD_DOCUMENT, ActionKeys.ADD_FOLDER,
			ActionKeys.ADD_SHORTCUT, ActionKeys.ADD_SUBFOLDER,
			ActionKeys.DELETE, ActionKeys.UPDATE));

}
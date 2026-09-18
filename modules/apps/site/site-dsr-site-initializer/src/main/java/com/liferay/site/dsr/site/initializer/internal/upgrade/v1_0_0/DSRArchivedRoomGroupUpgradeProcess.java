/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.upgrade.v1_0_0;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.site.dsr.site.initializer.util.DSRRoomUtil;

/**
 * @author Matyas Wollner
 */
public class DSRArchivedRoomGroupUpgradeProcess extends UpgradeProcess {

	public DSRArchivedRoomGroupUpgradeProcess(
		CompanyLocalService companyLocalService,
		GroupLocalService groupLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_companyLocalService = companyLocalService;
		_groupLocalService = groupLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(this::_upgradeCompany);
	}

	private void _upgradeCompany(long companyId) throws PortalException {
		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", companyId);

		if (objectDefinition == null) {
			return;
		}

		for (Group group :
				_groupLocalService.getGroups(
					companyId, objectDefinition.getClassName(),
					GroupConstants.DEFAULT_PARENT_GROUP_ID)) {

			if (!group.isActive()) {
				continue;
			}

			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				group.getClassPK());

			if ((objectEntry == null) || !DSRRoomUtil.isArchived(objectEntry)) {
				continue;
			}

			UnicodeProperties typeSettingsUnicodeProperties =
				group.getTypeSettingsProperties();

			typeSettingsUnicodeProperties.setProperty(
				GroupConstants.TYPE_SETTINGS_KEY_MAINTENANCE_MODE,
				Boolean.TRUE.toString());

			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setCompanyId(companyId);
			serviceContext.setUserId(group.getCreatorUserId());

			_groupLocalService.updateGroup(
				group.getGroupId(), group.getParentGroupId(),
				group.getNameMap(), group.getDescriptionMap(), group.getType(),
				typeSettingsUnicodeProperties.toString(),
				group.isManualMembership(), group.getMembershipRestriction(),
				group.getFriendlyURL(), group.isInheritContent(), false,
				serviceContext);
		}
	}

	private final CompanyLocalService _companyLocalService;
	private final GroupLocalService _groupLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;

}
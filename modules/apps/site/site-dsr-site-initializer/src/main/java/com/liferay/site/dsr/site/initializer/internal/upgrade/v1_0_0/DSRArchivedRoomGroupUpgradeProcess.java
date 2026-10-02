/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.upgrade.v1_0_0;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.ClassNameLocalService;
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
		ClassNameLocalService classNameLocalService,
		CompanyLocalService companyLocalService,
		GroupLocalService groupLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService) {

		_classNameLocalService = classNameLocalService;
		_companyLocalService = companyLocalService;
		_groupLocalService = groupLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(
			companyId -> {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.
						fetchObjectDefinitionByExternalReferenceCode(
							"L_DSR_ROOM", companyId);

				if (objectDefinition == null) {
					return;
				}

				long classNameId = _classNameLocalService.getClassNameId(
					objectDefinition.getClassName());

				for (ObjectEntry objectEntry :
						_objectEntryLocalService.getObjectEntries(
							0, objectDefinition.getObjectDefinitionId(),
							QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

					if (!DSRRoomUtil.isArchived(objectEntry)) {
						continue;
					}

					Group group = _groupLocalService.fetchGroup(
						companyId, classNameId, objectEntry.getObjectEntryId());

					if ((group == null) || !group.isActive()) {
						continue;
					}

					UnicodeProperties unicodeProperties =
						group.getTypeSettingsProperties();

					unicodeProperties.setProperty(
						GroupConstants.TYPE_SETTINGS_KEY_MAINTENANCE_MODE,
						Boolean.TRUE.toString());

					ServiceContext serviceContext = new ServiceContext();

					serviceContext.setCompanyId(companyId);
					serviceContext.setUserId(group.getCreatorUserId());

					_groupLocalService.updateGroup(
						group.getGroupId(), group.getParentGroupId(),
						group.getNameMap(), group.getDescriptionMap(),
						group.getType(), unicodeProperties.toString(),
						group.isManualMembership(),
						group.getMembershipRestriction(),
						group.getFriendlyURL(), group.isInheritContent(), false,
						serviceContext);
				}
			});
	}

	private final ClassNameLocalService _classNameLocalService;
	private final CompanyLocalService _companyLocalService;
	private final GroupLocalService _groupLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;

}
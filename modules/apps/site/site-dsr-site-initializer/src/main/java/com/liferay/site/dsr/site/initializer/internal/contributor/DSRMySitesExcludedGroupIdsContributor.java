/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.contributor;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.site.contributor.MySitesExcludedGroupIdsContributor;
import com.liferay.site.dsr.site.initializer.util.DSRRoomUtil;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(service = MySitesExcludedGroupIdsContributor.class)
public class DSRMySitesExcludedGroupIdsContributor
	implements MySitesExcludedGroupIdsContributor {

	@Override
	public List<Long> getExcludedGroupIds(long companyId, long userId) {
		return TransformUtil.transformToList(
			_userLocalService.getGroupPrimaryKeys(userId),
			groupId -> {
				Group group = _groupLocalService.fetchGroup(groupId);

				if ((group != null) && DSRRoomUtil.isArchived(group)) {
					return group.getGroupId();
				}

				return null;
			});
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private UserLocalService _userLocalService;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.service;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupServiceWrapper;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.site.dsr.site.initializer.util.DSRRoomUtil;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Stefano Motta
 */
@Component(service = ServiceWrapper.class)
public class DSRGroupServiceWrapper extends GroupServiceWrapper {

	@Override
	public List<Group> getUserSitesGroups(
			long userId, String[] classNames, int max)
		throws PortalException {

		List<Group> groups = ListUtil.filter(
			super.getUserSitesGroups(userId, classNames, QueryUtil.ALL_POS),
			group -> !DSRRoomUtil.isArchived(group));

		if ((max != QueryUtil.ALL_POS) && (groups.size() > max)) {
			return groups.subList(0, max);
		}

		return groups;
	}

}
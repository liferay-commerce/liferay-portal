/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.channel.web.internal.portlet;

import com.liferay.commerce.product.constants.CPActionKeys;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.BaseControlPanelEntry;
import com.liferay.portal.kernel.portlet.ControlPanelEntry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(
	property = "javax.portlet.name=" + CPPortletKeys.COMMERCE_CHANNELS,
	service = ControlPanelEntry.class
)
public class CommerceChannelControlPanelEntry extends BaseControlPanelEntry {

	@Override
	public boolean hasAccessPermission(
			PermissionChecker permissionChecker, Group group, Portlet portlet)
		throws Exception {

		if (PortletPermissionUtil.contains(
				permissionChecker, portlet.getPortletId(),
				ActionKeys.ACCESS_IN_CONTROL_PANEL) &&
			_portletResourcePermission.contains(
				permissionChecker, group,
				CPActionKeys.VIEW_COMMERCE_CHANNELS) &&
			super.hasAccessPermission(permissionChecker, group, portlet)) {

			return true;
		}

		return false;
	}

	@Reference(
		target = "(resource.name=" + CPConstants.RESOURCE_NAME_CHANNEL + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}
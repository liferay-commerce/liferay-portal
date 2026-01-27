/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.notification;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.notifications.BaseModelUserNotificationHandler;
import com.liferay.portal.kernel.notifications.UserNotificationHandler;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HtmlUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "jakarta.portlet.name=com.liferay.commerce.model.CommerceOrder",
	service = UserNotificationHandler.class
)
public class CommerceOrderUserNotificationHandler
	extends BaseModelUserNotificationHandler {

	public CommerceOrderUserNotificationHandler() {
		setPortletId(CommerceOrder.class.getName());
	}

	@Override
	public boolean hasPermission(long classPK, User user)
		throws PortalException {

		return _commerceOrderModelResourcePermission.contains(
			_permissionCheckerFactory.create(user), classPK, ActionKeys.VIEW);
	}

	@Override
	public boolean isDeliver(
		long userId, long classNameId, int notificationType, int deliveryType,
		ServiceContext serviceContext) {

		return true;
	}

	@Override
	protected String getBodyContent(JSONObject jsonObject) {
		return HtmlUtil.escape(jsonObject.getString("notificationMessage"));
	}

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceOrder)"
	)
	private ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.servlet.filter;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.dsr.site.initializer.util.DSRRoomUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matyas Wollner
 */
@Component(
	property = {
		"dispatcher=FORWARD", "dispatcher=REQUEST", "servlet-context-name=",
		"servlet-filter-name=DSR Archived Room Filter", "url-pattern=/c/*",
		"url-pattern=/group/*", "url-pattern=/web/*"
	},
	service = Filter.class
)
public class ArchivedRoomFilter extends BaseFilter {

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		Group group = _fetchGroup(httpServletRequest);

		if ((group != null) && !group.isActive() &&
			_groupLocalService.isMaintenanceMode(group) &&
			DSRRoomUtil.isArchived(group)) {

			PermissionChecker permissionChecker = _getPermissionChecker(
				httpServletRequest);

			if ((permissionChecker != null) &&
				!permissionChecker.isGroupAdmin(group.getGroupId())) {

				httpServletResponse.sendRedirect(
					_portal.getPortalURL(httpServletRequest));

				return;
			}
		}

		processFilter(
			ArchivedRoomFilter.class.getName(), httpServletRequest,
			httpServletResponse, filterChain);
	}

	private Group _fetchGroup(HttpServletRequest httpServletRequest) {
		long plid = ParamUtil.getLong(httpServletRequest, "p_l_id");

		if (plid > 0) {
			Layout layout = _layoutLocalService.fetchLayout(plid);

			if (layout == null) {
				return null;
			}

			return _groupLocalService.fetchGroup(layout.getGroupId());
		}

		String groupFriendlyURL = _getGroupFriendlyURL(httpServletRequest);

		if (Validator.isNull(groupFriendlyURL)) {
			return null;
		}

		return _groupLocalService.fetchFriendlyURLGroup(
			CompanyThreadLocal.getCompanyId(), groupFriendlyURL);
	}

	private String _getGroupFriendlyURL(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		String contextPath = httpServletRequest.getContextPath();

		if (!contextPath.equals(StringPool.SLASH) &&
			requestURI.startsWith(contextPath)) {

			requestURI = requestURI.substring(contextPath.length());
		}

		for (String servletMapping : _SERVLET_MAPPINGS) {
			if (!requestURI.startsWith(servletMapping + StringPool.SLASH)) {
				continue;
			}

			String path = requestURI.substring(servletMapping.length());

			int index = path.indexOf(CharPool.SLASH, 1);

			if (index == -1) {
				return path;
			}

			return path.substring(0, index);
		}

		return null;
	}

	private PermissionChecker _getPermissionChecker(
			HttpServletRequest httpServletRequest)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (permissionChecker != null) {
			return permissionChecker;
		}

		User user = _portal.getUser(httpServletRequest);

		if (user == null) {
			user = _userLocalService.fetchGuestUser(
				CompanyThreadLocal.getCompanyId());
		}

		if (user == null) {
			return null;
		}

		return _permissionCheckerFactory.create(user);
	}

	private static final String[] _SERVLET_MAPPINGS = {
		PropsValues.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING,
		PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING
	};

	private static final Log _log = LogFactoryUtil.getLog(
		ArchivedRoomFilter.class);

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}
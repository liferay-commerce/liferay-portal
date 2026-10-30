/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.util;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderHttpHelper;
import com.liferay.commerce.service.CommerceOrderLocalServiceUtil;
import com.liferay.commerce.util.GuestCommerceOrderUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.access.control.AccessControlUtil;
import com.liferay.portal.kernel.security.auth.AccessControlContext;
import com.liferay.portal.kernel.util.PortalUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Matyas Wollner
 */
public class JSONWebServiceCommerceOrderUtil {

	public static void checkGuestCommerceOrder(CommerceOrder commerceOrder)
		throws PortalException {

		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		if (httpServletRequest == null) {
			return;
		}

		GuestCommerceOrderUtil.checkGuestCommerceOrder(
			commerceOrder, _commerceOrderHttpHelperSnapshot.get(),
			httpServletRequest);
	}

	public static void checkGuestCommerceOrder(long commerceOrderId)
		throws PortalException {

		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		if (httpServletRequest == null) {
			return;
		}

		GuestCommerceOrderUtil.checkGuestCommerceOrder(
			CommerceOrderLocalServiceUtil.getCommerceOrder(commerceOrderId),
			_commerceOrderHttpHelperSnapshot.get(), httpServletRequest);
	}

	private static HttpServletRequest _getHttpServletRequest() {
		AccessControlContext accessControlContext =
			AccessControlUtil.getAccessControlContext();

		if (accessControlContext == null) {
			return null;
		}

		HttpServletRequest httpServletRequest =
			accessControlContext.getRequest();

		String jsonWebServicePath =
			PortalUtil.getPathModule() + "/portal/api/jsonws";
		String requestURI = httpServletRequest.getRequestURI();

		if (!requestURI.equals(jsonWebServicePath) &&
			!requestURI.startsWith(jsonWebServicePath + StringPool.SLASH)) {

			return null;
		}

		return httpServletRequest;
	}

	private static final Snapshot<CommerceOrderHttpHelper>
		_commerceOrderHttpHelperSnapshot = new Snapshot<>(
			JSONWebServiceCommerceOrderUtil.class,
			CommerceOrderHttpHelper.class);

}
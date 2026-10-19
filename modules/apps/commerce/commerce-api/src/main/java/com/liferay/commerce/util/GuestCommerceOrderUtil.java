/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.util;

import com.liferay.commerce.exception.NoSuchOrderException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderHttpHelper;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cookies.CookiesManagerUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Objects;

/**
 * @author Gianmarco Brunialti
 */
public class GuestCommerceOrderUtil {

	public static void checkGuestCommerceOrder(
			CommerceOrder commerceOrder,
			CommerceOrderHttpHelper commerceOrderHttpHelper,
			HttpServletRequest httpServletRequest)
		throws PortalException {

		if (!commerceOrder.isGuestOrder()) {
			return;
		}

		if ((httpServletRequest == null) ||
			!Objects.equals(
				commerceOrder.getUuid(),
				_getCommerceOrderUUID(
					commerceOrderHttpHelper.getCookieName(
						commerceOrder.getGroupId()),
					httpServletRequest))) {

			throw new NoSuchOrderException();
		}
	}

	private static String _getCommerceOrderUUID(
		String cookieName, HttpServletRequest httpServletRequest) {

		String commerceOrderUUID = httpServletRequest.getHeader(
			"Commerce-Cart-UUID");

		if (Validator.isNotNull(commerceOrderUUID)) {
			return commerceOrderUUID;
		}

		HttpServletRequest originalHttpServletRequest =
			PortalUtil.getOriginalServletRequest(httpServletRequest);

		HttpSession httpSession = originalHttpServletRequest.getSession(false);

		if (httpSession != null) {
			commerceOrderUUID = (String)httpSession.getAttribute(cookieName);

			if (Validator.isNotNull(commerceOrderUUID)) {
				return commerceOrderUUID;
			}
		}

		return StringUtil.extractFirst(
			CookiesManagerUtil.getCookieValue(
				cookieName, httpServletRequest, true),
			StringPool.PIPE);
	}

}
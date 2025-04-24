/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.content.web.internal.frontend.data.set.url;

import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.constants.CommerceWebKeys;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.frontend.data.set.url.FDSAPIURLResolver;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Fabio Monaco
 */
@Component(
	property = "fds.rest.application.key=/headless-admin-user/v1.0/UserAccount",
	service = FDSAPIURLResolver.class
)
public class UserAccountFDSAPIURLResolver implements FDSAPIURLResolver {

	@Override
	public String getSchema() {
		return "UserAccount";
	}

	@Override
	public String resolve(String baseURL, HttpServletRequest httpServletRequest)
		throws PortalException {

		CommerceContext commerceContext =
			(CommerceContext)httpServletRequest.getAttribute(
				CommerceWebKeys.COMMERCE_CONTEXT);

		AccountEntry accountEntry = commerceContext.getAccountEntry();

		if (accountEntry == null) {
			return StringPool.BLANK;
		}

		String externalReferenceCode = StringPool.BLANK;

		if (baseURL.startsWith("/v1.0/accounts/by-externalReferenceCode")) {
			externalReferenceCode = accountEntry.getExternalReferenceCode();
		}

		return StringUtil.replace(
			baseURL, new String[] {"{account}", "{externalReferenceCode}"},
			new String[] {
				String.valueOf(accountEntry.getName()), externalReferenceCode
			});
	}

}
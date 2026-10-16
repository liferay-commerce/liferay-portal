/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.servlet;

import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.service.CommerceOrderAttachmentService;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	property = {
		"osgi.http.whiteboard.context.path=/commerce-digital-signature",
		"osgi.http.whiteboard.servlet.name=com.liferay.commerce.digital.signature.internal.servlet.DSCommerceOrderSignServlet",
		"osgi.http.whiteboard.servlet.pattern=/commerce-digital-signature/sign/*"
	},
	service = Servlet.class
)
public class DSCommerceOrderSignServlet extends HttpServlet {

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		String backURL = _portal.escapeRedirect(
			ParamUtil.getString(httpServletRequest, "backURL"));

		if (Validator.isNull(backURL)) {
			backURL = _portal.getPortalURL(httpServletRequest);
		}

		String originalName = PrincipalThreadLocal.getName();
		PermissionChecker originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			User user = _portal.getUser(httpServletRequest);

			if ((user == null) || user.isGuestUser()) {
				httpServletResponse.sendRedirect(backURL);

				return;
			}

			PrincipalThreadLocal.setName(user.getUserId());
			PermissionThreadLocal.setPermissionChecker(
				_permissionCheckerFactory.create(user));

			CommerceOrderAttachment commerceOrderAttachment =
				_commerceOrderAttachmentService.getCommerceOrderAttachment(
					ParamUtil.getLong(
						httpServletRequest, "commerceOrderAttachmentId"));

			DSRequest dsRequest = _dsRequestManager.fetchFileEntryDSRequest(
				user.getCompanyId(), commerceOrderAttachment.getFileEntryId());

			if ((dsRequest == null) ||
				!dsRequest.isSignatureRequired(user.getUserId())) {

				httpServletResponse.sendRedirect(backURL);

				return;
			}

			httpServletResponse.sendRedirect(
				_signDSURLProvider.getModalURL(
					user.getCompanyId(), dsRequest.getSiteGroupId(), backURL,
					dsRequest.getDSRequestId()));
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			httpServletResponse.sendRedirect(backURL);
		}
		catch (Exception exception) {
			_log.error(exception);

			httpServletResponse.sendRedirect(backURL);
		}
		finally {
			PrincipalThreadLocal.setName(originalName);
			PermissionThreadLocal.setPermissionChecker(
				originalPermissionChecker);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSCommerceOrderSignServlet.class);

	@Reference
	private CommerceOrderAttachmentService _commerceOrderAttachmentService;

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private Portal _portal;

	@Reference
	private SignDSURLProvider _signDSURLProvider;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.document.library.internal.display.context;

import com.liferay.digital.signature.constants.DigitalSignatureConstants;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.document.library.display.context.DLDisplayContextFactory;
import com.liferay.document.library.display.context.DLEditFileEntryDisplayContext;
import com.liferay.document.library.display.context.DLViewFileVersionDisplayContext;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileShortcut;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Danny Situ
 */
@Component(service = DLDisplayContextFactory.class)
public class SignatureDLDisplayContextFactory
	implements DLDisplayContextFactory {

	@Override
	public DLEditFileEntryDisplayContext getDLEditFileEntryDisplayContext(
		DLEditFileEntryDisplayContext parentDLEditFileEntryDisplayContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse,
		DLFileEntryType dlFileEntryType) {

		return parentDLEditFileEntryDisplayContext;
	}

	@Override
	public DLEditFileEntryDisplayContext getDLEditFileEntryDisplayContext(
		DLEditFileEntryDisplayContext parentDLEditFileEntryDisplayContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, FileEntry fileEntry) {

		return parentDLEditFileEntryDisplayContext;
	}

	@Override
	public DLViewFileVersionDisplayContext getDLViewFileVersionDisplayContext(
		DLViewFileVersionDisplayContext parentDLViewFileVersionDisplayContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, FileShortcut fileShortcut) {

		return parentDLViewFileVersionDisplayContext;
	}

	@Override
	public DLViewFileVersionDisplayContext getDLViewFileVersionDisplayContext(
		DLViewFileVersionDisplayContext parentDLViewFileVersionDisplayContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, FileVersion fileVersion) {

		if (fileVersion == null) {
			return parentDLViewFileVersionDisplayContext;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if ((themeDisplay == null) || !themeDisplay.isSignedIn()) {
			return parentDLViewFileVersionDisplayContext;
		}

		try {
			FileEntry fileEntry = fileVersion.getFileEntry();

			long fileEntryId = fileEntry.getFileEntryId();

			DSRequest dsRequest = _getDSRequest(
				httpServletRequest, themeDisplay.getCompanyId(), fileEntryId);

			if (dsRequest == null) {
				return parentDLViewFileVersionDisplayContext;
			}

			boolean hasUpdatePermission =
				_dlFileEntryModelResourcePermission.contains(
					themeDisplay.getPermissionChecker(), fileEntryId,
					ActionKeys.UPDATE);

			return new SignatureDLViewFileVersionDisplayContext(
				parentDLViewFileVersionDisplayContext, httpServletRequest,
				httpServletResponse, fileVersion, hasUpdatePermission,
				dsRequest, _signDSURLProvider);
		}
		catch (PortalException portalException) {
			throw new SystemException(
				"Unable to create the signature display context for file " +
					"version " + fileVersion,
				portalException);
		}
	}

	private DSRequest _getDSRequest(
		HttpServletRequest httpServletRequest, long companyId,
		long fileEntryId) {

		Map<Long, DSRequest> dsRequests =
			(Map<Long, DSRequest>)httpServletRequest.getAttribute(
				DigitalSignatureConstants.DS_REQUESTS_ATTRIBUTE_NAME);

		if (dsRequests != null) {
			return dsRequests.get(fileEntryId);
		}

		return _dsRequestManager.fetchDSRequest(companyId, fileEntryId);
	}

	@Reference(
		target = "(model.class.name=com.liferay.document.library.kernel.model.DLFileEntry)"
	)
	private ModelResourcePermission<DLFileEntry>
		_dlFileEntryModelResourcePermission;

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference
	private SignDSURLProvider _signDSURLProvider;

}
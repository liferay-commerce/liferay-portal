/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.display.context;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.document.library.display.context.BaseDLDisplayContextFactory;
import com.liferay.document.library.display.context.DLDisplayContextFactory;
import com.liferay.document.library.display.context.DLViewFileVersionDisplayContext;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Danny Situ
 */
@Component(service = DLDisplayContextFactory.class)
public class DigitalSignatureDLDisplayContextFactory
	extends BaseDLDisplayContextFactory {

	@Override
	public DLViewFileVersionDisplayContext getDLViewFileVersionDisplayContext(
		DLViewFileVersionDisplayContext parentDLViewFileVersionDisplayContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, FileVersion fileVersion) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if ((fileVersion == null) || (themeDisplay == null) ||
			!themeDisplay.isSignedIn()) {

			return parentDLViewFileVersionDisplayContext;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				themeDisplay.getCompanyId(), fileVersion.getGroupId());

		if (!digitalSignatureConfiguration.embeddedViewEnabled() ||
			!digitalSignatureConfiguration.enabled()) {

			return parentDLViewFileVersionDisplayContext;
		}

		return new DigitalSignatureDLViewFileVersionDisplayContext(
			_dsRequestManager, fileVersion, httpServletRequest,
			httpServletResponse, parentDLViewFileVersionDisplayContext);
	}

	@Reference
	private DSRequestManager _dsRequestManager;

}
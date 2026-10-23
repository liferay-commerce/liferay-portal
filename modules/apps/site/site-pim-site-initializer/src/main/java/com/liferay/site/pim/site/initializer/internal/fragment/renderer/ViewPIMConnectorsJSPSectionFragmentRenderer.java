/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.fragment.renderer;

import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.site.pim.site.initializer.internal.display.context.ViewPIMConnectorsDisplayContext;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Andrea Sbarra
 */
@Component(service = FragmentRenderer.class)
public class ViewPIMConnectorsJSPSectionFragmentRenderer
	extends BaseJSPSectionFragmentRenderer<ViewPIMConnectorsDisplayContext> {

	@Override
	public String getLabelKey() {
		return "connectors";
	}

	@Override
	protected ViewPIMConnectorsDisplayContext getDisplayContext(
		HttpServletRequest httpServletRequest) {

		return new ViewPIMConnectorsDisplayContext(httpServletRequest);
	}

}
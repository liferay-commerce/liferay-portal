/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.servlet;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.site.pim.site.initializer.connector.PIMConnector;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorRegistry;
import com.liferay.site.pim.site.initializer.exception.PIMConnectorFieldMappingException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = {
		"osgi.http.whiteboard.servlet.name=com.liferay.site.pim.site.initializer.internal.servlet.ExportPIMProductsServlet",
		"osgi.http.whiteboard.servlet.pattern=/pim/export/*",
		"servlet.init.httpMethods=GET"
	},
	service = Servlet.class
)
public class ExportPIMProductsServlet extends HttpServlet {

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException, ServletException {

		try {
			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				ParamUtil.getLong(httpServletRequest, "objectEntryId"));

			if (objectEntry == null) {
				_sendError(
					httpServletResponse, "Unable to get the PIM connector",
					HttpServletResponse.SC_BAD_REQUEST);

				return;
			}

			String key = MapUtil.getString(
				_objectEntryLocalService.getValues(objectEntry), "key");

			PIMConnector pimConnector = _pimConnectorRegistry.getPIMConnector(
				key);

			if (pimConnector == null) {
				_sendError(
					httpServletResponse,
					"Unable to get the PIM connector with key " + key,
					HttpServletResponse.SC_BAD_REQUEST);

				return;
			}

			String products = pimConnector.exportProducts(objectEntry);

			ServletResponseUtil.sendFile(
				httpServletRequest, httpServletResponse, "pim-products.json",
				products.getBytes(StringPool.UTF8),
				ContentTypes.APPLICATION_JSON,
				HttpHeaders.CONTENT_DISPOSITION_ATTACHMENT);
		}
		catch (PIMConnectorFieldMappingException
					pimConnectorFieldMappingException) {

			_sendError(
				httpServletResponse,
				pimConnectorFieldMappingException.getMessage(),
				HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (Exception exception) {
			_log.error(exception);

			_sendError(
				httpServletResponse, "Unable to export the products",
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void _sendError(
			HttpServletResponse httpServletResponse, String message, int status)
		throws IOException {

		httpServletResponse.setContentType(ContentTypes.APPLICATION_JSON);
		httpServletResponse.setStatus(status);

		ServletResponseUtil.write(
			httpServletResponse,
			JSONUtil.put(
				"error", message
			).toString());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExportPIMProductsServlet.class);

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PIMConnectorRegistry _pimConnectorRegistry;

}
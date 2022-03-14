/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.batch.planner.web.internal.portlet.action;

import com.liferay.batch.engine.service.BatchEngineImportTaskErrorLocalService;
import com.liferay.batch.planner.constants.BatchPlannerPortletKeys;
import com.liferay.batch.planner.web.internal.report.BatchEngineImportTaskErrorCSVReport;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matija Petanjek
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + BatchPlannerPortletKeys.BATCH_PLANNER,
		"mvc.command.name=/batch_planner/download_error_report"
	},
	service = MVCResourceCommand.class
)
public class DownloadErrorReportMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		long batchEngineImportTaskId = ParamUtil.getLong(
			resourceRequest, "batchEngineImportTaskId");
		String title = ParamUtil.getString(resourceRequest, "title");

		File file = null;

		try {
			file = _batchEngineImportTaskErrorCSVReport.create(
				_batchEngineImportTaskErrorLocalService.
					getBatchEngineImportTaskErrors(batchEngineImportTaskId));

			title = StringUtil.replace(title, CharPool.SPACE, CharPool.DASH);

			PortletResponseUtil.sendFile(
				resourceRequest, resourceResponse,
				StringUtil.toLowerCase(title) + "-errors.csv",
				new FileInputStream(file), ContentTypes.TEXT_CSV);
		}
		catch (Exception exception) {
			_log.error("Unable to create error report", exception);
		}
		finally {
			if (file != null) {
				FileUtil.delete(file);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DownloadErrorReportMVCResourceCommand.class);

	@Reference
	private BatchEngineImportTaskErrorCSVReport
		_batchEngineImportTaskErrorCSVReport;

	@Reference
	private BatchEngineImportTaskErrorLocalService
		_batchEngineImportTaskErrorLocalService;

}
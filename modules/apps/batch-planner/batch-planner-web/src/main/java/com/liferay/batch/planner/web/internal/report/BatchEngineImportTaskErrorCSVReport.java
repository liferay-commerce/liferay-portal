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

package com.liferay.batch.planner.web.internal.report;

import com.liferay.batch.engine.model.BatchEngineImportTaskError;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Matija Petanjek
 */
@Component(
	immediate = true, service = BatchEngineImportTaskErrorCSVReport.class
)
public class BatchEngineImportTaskErrorCSVReport {

	public File create(
			List<BatchEngineImportTaskError> batchEngineImportTaskErrors)
		throws IOException {

		File file = FileUtil.createTempFile();

		try (BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file)))) {

			bufferedWriter.write(_CSV_REPORT_HEADER);
			bufferedWriter.newLine();

			for (BatchEngineImportTaskError batchEngineImportTaskError :
					batchEngineImportTaskErrors) {

				StringBundler sb = new StringBundler(5);

				sb.append(batchEngineImportTaskError.getItem());
				sb.append(StringPool.COMMA_AND_SPACE);
				sb.append(batchEngineImportTaskError.getItemIndex());
				sb.append(StringPool.COMMA_AND_SPACE);
				sb.append(batchEngineImportTaskError.getMessage());

				bufferedWriter.write(sb.toString());

				bufferedWriter.newLine();
			}
		}

		return file;
	}

	private static final String _CSV_REPORT_HEADER = "item, itemIndex, message";

}
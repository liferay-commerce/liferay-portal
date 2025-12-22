/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.test.util;

import com.liferay.batch.engine.unit.BatchEngineUnitProcessor;
import com.liferay.batch.engine.unit.BatchEngineUnitReader;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.module.service.Snapshot;

import java.io.File;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Stefano Motta
 */
public class BatchEngineTestUtil {

	public static void processBatchEngineUnits(String bundleSymbolicName) {
		BatchEngineUnitProcessor batchEngineUnitProcessor =
			_batchEngineUnitProcessorSnapshot.get();
		BatchEngineUnitReader batchEngineUnitReader =
			_batchEngineUnitReaderSnapshot.get();

		Bundle testBundle = FrameworkUtil.getBundle(BatchEngineTestUtil.class);

		BundleContext bundleContext = testBundle.getBundleContext();

		for (Bundle bundle : bundleContext.getBundles()) {
			if (!Objects.equals(bundle.getSymbolicName(), bundleSymbolicName)) {
				continue;
			}

			Dictionary<String, String> headers = bundle.getHeaders("");

			for (String fileName :
					Collections.list(
						bundle.getEntryPaths(
							headers.get("Liferay-Client-Extension-Batch")))) {

				File file = bundle.getDataFile(
					StringBundler.concat(
						".",
						StringUtil.replace(
							StringUtil.replace(fileName, '/', '.'), '-', '.'),
						".0.processed"));

				if ((file != null) && file.exists()) {
					file.delete();
				}
			}

			CompletableFuture<Void> completableFuture =
				batchEngineUnitProcessor.processBatchEngineUnits(
					batchEngineUnitReader.getBatchEngineUnits(bundle));

			completableFuture.join();
		}
	}

	private static final Snapshot<BatchEngineUnitProcessor>
		_batchEngineUnitProcessorSnapshot = new Snapshot<>(
			BatchEngineTestUtil.class, BatchEngineUnitProcessor.class);
	private static final Snapshot<BatchEngineUnitReader>
		_batchEngineUnitReaderSnapshot = new Snapshot<>(
			BatchEngineTestUtil.class, BatchEngineUnitReader.class);

}
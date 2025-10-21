/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.scheduler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.batch.engine.unit.BatchEngineUnitProcessor;
import com.liferay.batch.engine.unit.BatchEngineUnitReader;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.rest.test.util.ObjectEntryTestUtil;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.site.cms.site.initializer.constants.BulkActionExecutionStatusConstants;

import java.io.File;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Crescenzo Rega
 */
@FeatureFlags(
	featureFlags = {
		@FeatureFlag("LPD-17564"), @FeatureFlag("LPD-21926"),
		@FeatureFlag("LPD-31149"), @FeatureFlag("LPD-32050"),
		@FeatureFlag("LPD-34594"), @FeatureFlag("LPS-179669")
	}
)
@RunWith(Arquillian.class)
@Sync
public class UpdateBulkActionTaskSchedulerJobConfigurationTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		_jobExecutorUnsafeRunnable =
			_schedulerJobConfiguration.getJobExecutorUnsafeRunnable();
	}

	@Before
	public void setUp() throws Exception {
		if (!_isCMSSiteInitialized()) {

			// These tests require the instance to be created with the feature
			// flag LPD-17564 enabled. On CI, feature flags are enabled on
			// demand for each test, but not during instance initialization.
			// Until the feature flag LPD-17564 is removed, run the batch
			// engine unit processor manually so that the object definitions
			// are created.

			Bundle testBundle = FrameworkUtil.getBundle(
				UpdateBulkActionTaskSchedulerJobConfigurationTest.class);

			BundleContext bundleContext = testBundle.getBundleContext();

			for (Bundle bundle : bundleContext.getBundles()) {
				if (!Objects.equals(
						bundle.getSymbolicName(),
						"com.liferay.site.initializer.cms")) {

					continue;
				}

				_deleteFile(bundle, "00.list.type.definition");
				_deleteFile(bundle, "01.object.folder");
				_deleteFile(bundle, "02.object.definition");

				CompletableFuture<Void> completableFuture =
					_batchEngineUnitProcessor.processBatchEngineUnits(
						_batchEngineUnitReader.getBatchEngineUnits(bundle));

				completableFuture.join();
			}
		}

		_cmsBulkActionTaskObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BULK_ACTION_TASK", TestPropsValues.getCompanyId());

		_cmsBulkActionTaskItemObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BULK_ACTION_TASK_ITEM",
					TestPropsValues.getCompanyId());
	}

	@Test
	public void testUpdateObjectEntries() throws Exception {
		ObjectEntry cmsBulkActionTask = ObjectEntryTestUtil.addObjectEntry(
			0, _cmsBulkActionTaskObjectDefinition,
			HashMapBuilder.<String, Serializable>put(
				"actionName", "DeleteBulkAction"
			).put(
				"executionStatus", "initial"
			).put(
				"type", "DeleteBulkAction"
			).build());

		BatchEngineImportTask batchEngineImportTask =
			_batchEngineImportTaskLocalService.addBatchEngineImportTask(
				null, TestPropsValues.getCompanyId(),
				TestPropsValues.getUserId(), 10, null,
				ObjectEntry.class.getName(), new byte[0], "JSON",
				BatchEngineTaskExecuteStatus.FAILED.name(), null,
				BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
				BatchEngineTaskOperation.DELETE.name(), new HashMap<>(), null);

		ObjectEntry cmsBulkActionTaskItem = ObjectEntryTestUtil.addObjectEntry(
			0, _cmsBulkActionTaskItemObjectDefinition,
			HashMapBuilder.<String, Serializable>put(
				"classExternalReferenceCode", RandomTestUtil.randomString(10)
			).put(
				"classPK", RandomTestUtil.randomLong(1, 10)
			).put(
				"executionStatus", "initial"
			).put(
				"importTaskId",
				batchEngineImportTask.getBatchEngineImportTaskId()
			).put(
				"name", RandomTestUtil.randomString(10)
			).put(
				"r_cmsBATaskToCMSBATaskItems_c_cmsBulkActionTaskId",
				cmsBulkActionTask.getObjectEntryId()
			).put(
				"type", RandomTestUtil.randomString(10)
			).build());

		_jobExecutorUnsafeRunnable.run();

		cmsBulkActionTask = _objectEntryLocalService.fetchObjectEntry(
			cmsBulkActionTask.getObjectEntryId());

		Map<String, Serializable> values = cmsBulkActionTask.getValues();

		Assert.assertEquals(
			BulkActionExecutionStatusConstants.COMPLETED,
			values.get("executionStatus"));

		cmsBulkActionTaskItem = _objectEntryLocalService.fetchObjectEntry(
			cmsBulkActionTaskItem.getObjectEntryId());

		values = cmsBulkActionTaskItem.getValues();

		Assert.assertEquals(
			StringUtil.toLowerCase(batchEngineImportTask.getExecuteStatus()),
			values.get("executionStatus"));
	}

	private void _deleteFile(Bundle bundle, String fileName) {
		File file = bundle.getDataFile(
			".com.liferay.site.initializer.cms.internal.batch." + fileName +
				".batch.engine.data.json.0.processed");

		if ((file != null) && file.exists()) {
			file.delete();
		}
	}

	private boolean _isCMSSiteInitialized() throws Exception {
		ObjectFolder objectFolder =
			_objectFolderLocalService.fetchObjectFolderByExternalReferenceCode(
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES,
				TestPropsValues.getCompanyId());

		if (objectFolder != null) {
			return true;
		}

		return false;
	}

	private static UnsafeRunnable<Exception> _jobExecutorUnsafeRunnable;

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.scheduler.UpdateBulkActionTaskSchedulerJobConfiguration"
	)
	private static SchedulerJobConfiguration _schedulerJobConfiguration;

	@Inject
	private BatchEngineImportTaskLocalService
		_batchEngineImportTaskLocalService;

	@Inject
	private BatchEngineUnitProcessor _batchEngineUnitProcessor;

	@Inject
	private BatchEngineUnitReader _batchEngineUnitReader;

	private ObjectDefinition _cmsBulkActionTaskItemObjectDefinition;
	private ObjectDefinition _cmsBulkActionTaskObjectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFolderLocalService _objectFolderLocalService;

}
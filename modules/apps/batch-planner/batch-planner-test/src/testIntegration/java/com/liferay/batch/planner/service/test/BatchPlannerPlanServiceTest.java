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

package com.liferay.batch.planner.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.planner.batch.engine.broker.BatchEngineBroker;
import com.liferay.batch.planner.constants.BatchPlannerPlanConstants;
import com.liferay.batch.planner.exception.BatchPlannerPlanExternalTypeException;
import com.liferay.batch.planner.exception.BatchPlannerPlanInternalClassNameException;
import com.liferay.batch.planner.exception.BatchPlannerPlanNameException;
import com.liferay.batch.planner.exception.DuplicateBatchPlannerPlanException;
import com.liferay.batch.planner.exception.RequiredBatchPlannerPlanException;
import com.liferay.batch.planner.model.BatchPlannerLog;
import com.liferay.batch.planner.model.BatchPlannerPlan;
import com.liferay.batch.planner.service.BatchPlannerLogService;
import com.liferay.batch.planner.service.BatchPlannerMappingService;
import com.liferay.batch.planner.service.BatchPlannerPlanService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Igor Beslic
 * @author Brian Wing Shun Chan
 */
@RunWith(Arquillian.class)
public class BatchPlannerPlanServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testAddBatchPlannerPlan() throws Exception {
		String name = RandomTestUtil.randomString();

		BatchPlannerPlan batchPlannerPlan =
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), name, null, false);

		Assert.assertEquals(
			BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
			batchPlannerPlan.getExternalType());
		Assert.assertEquals(name, batchPlannerPlan.getName());

		try {
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, "", "/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, false);

			Assert.fail();
		}
		catch (BatchPlannerPlanExternalTypeException
					batchPlannerPlanExternalTypeException) {

			String externalTypesString = StringUtil.merge(
				BatchPlannerPlanConstants.EXTERNAL_TYPES, StringPool.COMMA);

			Assert.assertEquals(
				"Batch planner plan external type must be one of following: " +
					externalTypesString,
				batchPlannerPlanExternalTypeException.getMessage());
		}

		try {
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(), null, name, null, false);

			Assert.fail();
		}
		catch (BatchPlannerPlanInternalClassNameException
					batchPlannerPlanInternalClassNameException) {

			Assert.assertNotNull(batchPlannerPlanInternalClassNameException);
		}

		try {
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), "", null, true);

			Assert.fail();
		}
		catch (BatchPlannerPlanNameException batchPlannerPlanNameException) {
			Assert.assertEquals(
				"Batch planner plan name is null for company " +
					TestPropsValues.getCompanyId(),
				batchPlannerPlanNameException.getMessage());
		}

		int maxLength = 75;

		try {
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				RandomTestUtil.randomString(maxLength + 1), null, false);

			Assert.fail();
		}
		catch (BatchPlannerPlanNameException batchPlannerPlanNameException) {
			Assert.assertEquals(
				"Batch planner plan name must not be longer than " + maxLength,
				batchPlannerPlanNameException.getMessage());
		}

		try {
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), name, null, false);

			Assert.fail();
		}
		catch (DuplicateBatchPlannerPlanException
					duplicateBatchPlannerPlanException) {

			Assert.assertEquals(
				StringBundler.concat(
					"Batch planner plan name \"", name,
					"\" already exists for company ",
					TestPropsValues.getCompanyId()),
				duplicateBatchPlannerPlanException.getMessage());
		}

		_batchPlannerPlanService.deleteBatchPlannerPlan(
			batchPlannerPlan.getBatchPlannerPlanId());
	}

	@Test
	public void testSearchBatchPlannerPlan() throws Exception {
		for (int i = 0; i < 60; i++) {
			boolean export = false;

			if ((i % 2) == 0) {
				export = true;
			}

			_submitPlan(
				export, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel",
				RandomTestUtil.randomString());
		}

		_submitPlan(true, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name1");
		_submitPlan(true, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name2");
		_submitPlan(true, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name3");
		_submitPlan(false, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name4");
		_submitPlan(false, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name5");

		BatchPlannerPlan batchPlannerPlan = _submitPlan(
			false, "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", "name6");

		_testSearchExportBatchPlannerLogs(batchPlannerPlan.getCompanyId());
		_testSearchImportBatchPlannerLogs(batchPlannerPlan.getCompanyId());
	}

	@Test
	public void testUpdateBatchPlannerPlan() throws Exception {
		BatchPlannerPlan batchPlannerPlan =
			_batchPlannerPlanService.addBatchPlannerPlan(
				true, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, false);

		try {
			_batchPlannerPlanService.updateBatchPlannerPlan(
				batchPlannerPlan.getBatchPlannerPlanId(),
				batchPlannerPlan.getExternalType(),
				batchPlannerPlan.getInternalClassName(),
				RandomTestUtil.randomString());

			Assert.fail();
		}
		catch (RequiredBatchPlannerPlanException
					requiredBatchPlannerPlanException) {

			Assert.assertEquals(
				"Batch planner plan is not a template",
				requiredBatchPlannerPlanException.getMessage());
		}
	}

	private BatchPlannerPlan _submitPlan(
			boolean export, String internalClassName, String name)
		throws Exception {

		BatchPlannerPlan batchPlannerPlan =
			_batchPlannerPlanService.addBatchPlannerPlan(
				export, BatchPlannerPlanConstants.EXTERNAL_TYPE_CSV,
				"/" + RandomTestUtil.randomString(), internalClassName, name,
				null, false);

		_batchPlannerMappingService.addBatchPlannerMapping(
			batchPlannerPlan.getBatchPlannerPlanId(),
			"name", "String",
			"name", "String",
			StringPool.BLANK);

		_batchEngineBroker.submit(batchPlannerPlan.getBatchPlannerPlanId());

		return batchPlannerPlan;
	}

	private void _testSearchExportBatchPlannerLogs(long companyId)
		throws Exception {

		List<BatchPlannerLog> batchPlannerLogs =
			_batchPlannerLogService.getCompanyBatchPlannerLogs(
				companyId, true, "name", "name3", 0, Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 1, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, true, "internalClassName", "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", 0,
			Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 33, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, true, "name", RandomTestUtil.randomString(), 0,
			Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 0, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, true, "internalClassName", RandomTestUtil.randomString(),
			0, Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 0, batchPlannerLogs.size());
	}

	private void _testSearchImportBatchPlannerLogs(long companyId)
		throws Exception {

		List<BatchPlannerLog> batchPlannerLogs =
			_batchPlannerLogService.getCompanyBatchPlannerLogs(
				companyId, false, "name", "name5", 0, Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 1, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, false, "internalClassName", "com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel", 0,
			Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 33, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, false, "name", RandomTestUtil.randomString(), 0,
			Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 0, batchPlannerLogs.size());

		batchPlannerLogs = _batchPlannerLogService.getCompanyBatchPlannerLogs(
			companyId, false, "internalClassName",
			RandomTestUtil.randomString(), 0, Integer.MAX_VALUE, null);

		Assert.assertEquals(
			batchPlannerLogs.toString(), 0, batchPlannerLogs.size());
	}

	private File _toBatchPlannerFile(
		String externalType, InputStream inputStream)
		throws Exception {

		UUID uuid = UUID.randomUUID();

		File file = FileUtil.createTempFile(uuid.toString(), externalType);

		try {
			Files.copy(inputStream, file.toPath());

			return file;
		}
		catch (IOException ioException) {
			if (file.exists()) {
				file.delete();
			}

			throw ioException;
		}
	}

	@Inject
	private BatchEngineBroker _batchEngineBroker;

	@Inject
	private BatchPlannerMappingService _batchPlannerMappingService;

	@Inject
	private BatchPlannerLogService _batchPlannerLogService;

	@Inject
	private BatchPlannerPlanService _batchPlannerPlanService;

}
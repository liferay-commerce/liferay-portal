/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.scheduler;

import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.model.BatchEngineImportTaskError;
import com.liferay.batch.engine.service.BatchEngineImportTaskService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.TriggerConfiguration;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.site.cms.site.initializer.configuration.BulkActionTaskConfiguration;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(
	configurationPid = "com.liferay.site.cms.site.initializer.configuration.BulkActionTaskConfiguration",
	service = SchedulerJobConfiguration.class
)
public class UpdateBulkActionTaskSchedulerJobConfiguration
	implements SchedulerJobConfiguration {

	@Override
	public UnsafeRunnable<Exception> getJobExecutorUnsafeRunnable() {
		return () -> _companyLocalService.forEachCompanyId(
			this::_updateObjectEntries);
	}

	@Override
	public TriggerConfiguration getTriggerConfiguration() {
		return _triggerConfiguration;
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		BulkActionTaskConfiguration bulkActionTaskConfiguration =
			ConfigurableUtil.createConfigurable(
				BulkActionTaskConfiguration.class, properties);

		_triggerConfiguration = TriggerConfiguration.createTriggerConfiguration(
			bulkActionTaskConfiguration.checkInterval(), TimeUnit.MINUTE);
	}

	private BatchEngineImportTaskError _getBatchEngineImportTaskError(
		List<BatchEngineImportTaskError> batchEngineImportTaskErrors,
		long classPK) {

		List<BatchEngineImportTaskError> filter = ListUtil.filter(
			batchEngineImportTaskErrors,
			batchEngineImportTaskError -> {
				try {
					JSONObject jsonObject = _jsonFactory.createJSONObject(
						batchEngineImportTaskError.getItem());

					return classPK == jsonObject.getLong("id");
				}
				catch (JSONException jsonException) {
					throw new RuntimeException(jsonException);
				}
			});

		if (ListUtil.isNotEmpty(filter)) {
			return filter.getFirst();
		}

		return null;
	}

	private ObjectDefinition _getObjectDefinition(long companyId)
		throws Exception {

		if (_objectDefinition != null) {
			return _objectDefinition;
		}

		_objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_BULK_ACTION_TASK", companyId);

		return _objectDefinition;
	}

	private long _getObjectRelationshipId(ObjectDefinition objectDefinition)
		throws Exception {

		if (_objectRelationship != null) {
			return _objectRelationship.getObjectRelationshipId();
		}

		_objectRelationship =
			_objectRelationshipLocalService.getObjectRelationship(
				objectDefinition.getObjectDefinitionId(),
				"bulkActionTaskToBulkActionTaskItems");

		return _objectRelationship.getObjectRelationshipId();
	}

	private void _updateObjectEntries(long companyId) throws Exception {
		ObjectDefinition objectDefinition = _getObjectDefinition(companyId);

		List<Long> primaryKeys = _objectEntryLocalService.getPrimaryKeys(
			new Long[0], companyId, 0, objectDefinition.getObjectDefinitionId(),
			_filterFactory.create(
				"executionStatus in ('initial','started')", objectDefinition),
			null, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		if (ListUtil.isEmpty(primaryKeys)) {
			return;
		}

		for (Long primaryKey : primaryKeys) {
			int numberOfFailedItems = 0;
			int numberOfSuccessfulItems = 0;
			Date completionDate = null;

			for (ObjectEntry objectEntry :
					_objectEntryLocalService.getOneToManyObjectEntries(
						0, _getObjectRelationshipId(objectDefinition), null,
						primaryKey, true, null, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS, null)) {

				Map<String, Serializable> values = objectEntry.getValues();

				if (MapUtil.isEmpty(values)) {
					continue;
				}

				String executionStatus = GetterUtil.getString(
					values.get("executionStatus"));

				if (executionStatus.equals("completed") ||
					executionStatus.equals("failed")) {

					continue;
				}

				BatchEngineImportTask batchEngineImportTask =
					_batchEngineImportTaskService.getBatchEngineImportTask(
						GetterUtil.getLong(values.get("importTaskID")));

				values.put(
					"executionStatus",
					batchEngineImportTask.getExecuteStatus(
					).toLowerCase());

				long classPK = GetterUtil.getLong(values.get("classPK"));

				if (classPK > 0) {
					BatchEngineImportTaskError batchEngineImportTaskError =
						_getBatchEngineImportTaskError(
							batchEngineImportTask.
								getBatchEngineImportTaskErrors(),
							classPK);

					if (batchEngineImportTaskError != null) {
						values.put(
							"description",
							batchEngineImportTaskError.getMessage());
					}
				}

				ObjectEntry updateObjectEntry =
					_objectEntryLocalService.partialUpdateObjectEntry(
						objectEntry.getUserId(), objectEntry.getObjectEntryId(),
						values, new ServiceContext());

				values = updateObjectEntry.getValues();

				executionStatus = GetterUtil.getString(
					values.get("executionStatus"));

				if (executionStatus.equals("completed") &&
					Validator.isBlank(
						GetterUtil.getString(values.get("description")))) {

					numberOfSuccessfulItems++;
				}
				else if (executionStatus.equals("failed") ||
						 !Validator.isBlank(
							 GetterUtil.getString(values.get("description")))) {

					numberOfFailedItems++;
				}

				if (completionDate == null) {
					completionDate = batchEngineImportTask.getEndTime();
				}
			}

			ObjectEntry objectEntry = _objectEntryLocalService.getObjectEntry(
				primaryKey);

			Map<String, Serializable> values = objectEntry.getValues();

			values.put("completionDate", completionDate);
			values.put("numberOfFailedItems", numberOfFailedItems);
			values.put("numberOfSuccessfulItems", numberOfSuccessfulItems);

			long numberOfItems = GetterUtil.getInteger(
				values.get("numberOfItems"));

			if (numberOfItems ==
					(numberOfSuccessfulItems + numberOfFailedItems)) {

				values.put("executionStatus", "completed");
			}
			else {
				values.put("executionStatus", "started");
			}

			_objectEntryLocalService.partialUpdateObjectEntry(
				objectEntry.getUserId(), objectEntry.getObjectEntryId(), values,
				new ServiceContext());
		}
	}

	@Reference
	private BatchEngineImportTaskService _batchEngineImportTaskService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private JSONFactory _jsonFactory;

	private ObjectDefinition _objectDefinition;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	private ObjectRelationship _objectRelationship;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	private TriggerConfiguration _triggerConfiguration;

}
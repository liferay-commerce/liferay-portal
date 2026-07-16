/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.request;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestManager.class)
public class DSRequestManagerImpl implements DSRequestManager {

	@Override
	public void addDSRequests(
		long companyId, long groupId, long userId, DSEnvelope dsEnvelope,
		long[] fileEntryIds) {

		if (!_isEnabled(companyId, groupId) || (dsEnvelope == null) ||
			(fileEntryIds == null)) {

			return;
		}

		ObjectDefinition requestObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST");
		ObjectDefinition recipientObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_RECIPIENT");

		if ((requestObjectDefinition == null) ||
			(recipientObjectDefinition == null)) {

			return;
		}

		try {
			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return;
			}

			ServiceContext serviceContext = _createServiceContext(
				companyId, groupId, userId);

			String languageId = LocaleUtil.toLanguageId(
				LocaleUtil.getSiteDefault());

			for (long fileEntryId : fileEntryIds) {
				ObjectEntry requestObjectEntry =
					_objectEntryLocalService.addObjectEntry(
						0, userId,
						requestObjectDefinition.getObjectDefinitionId(), 0,
						languageId,
						HashMapBuilder.<String, Serializable>put(
							"emailSubject", dsEnvelope.getEmailSubject()
						).put(
							"fileEntryId", fileEntryId
						).put(
							"providerKey", _PROVIDER_KEY
						).put(
							"providerRequestId", dsEnvelope.getDSEnvelopeId()
						).put(
							"requestStatus",
							_toRequestStatus(dsEnvelope.getStatus())
						).build(),
						serviceContext);

				for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
					_objectEntryLocalService.addObjectEntry(
						0, userId,
						recipientObjectDefinition.getObjectDefinitionId(), 0,
						languageId,
						HashMapBuilder.<String, Serializable>put(
							fieldName, requestObjectEntry.getObjectEntryId()
						).put(
							"emailAddress", dsRecipient.getEmailAddress()
						).put(
							"name", dsRecipient.getName()
						).put(
							"providerRecipientId",
							dsRecipient.getDSRecipientId()
						).put(
							"recipientUserId",
							_getRecipientUserId(
								companyId, dsRecipient.getEmailAddress())
						).put(
							"requestRecipientStatus",
							_toRecipientStatus(dsRecipient.getStatus())
						).build(),
						serviceContext);
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to record the signature request for envelope " +
					dsEnvelope.getDSEnvelopeId(),
				exception);
		}
	}

	@Override
	public void updateDSRequests(
		long companyId, long groupId, String providerRequestId) {

		if (!_isEnabled(companyId, groupId) || Validator.isNull(providerRequestId)) {
			return;
		}

		ObjectDefinition requestObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST");
		ObjectDefinition recipientObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_RECIPIENT");

		if ((requestObjectDefinition == null) ||
			(recipientObjectDefinition == null)) {

			return;
		}

		try {
			DSEnvelope dsEnvelope = _dsEnvelopeManager.getDSEnvelope(
				companyId, groupId, providerRequestId);

			if (dsEnvelope == null) {
				return;
			}

			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return;
			}

			Map<String, String> recipientStatuses = new HashMap<>();

			for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
				recipientStatuses.put(
					dsRecipient.getDSRecipientId(),
					_toRecipientStatus(dsRecipient.getStatus()));
			}

			String requestStatus = _toRequestStatus(dsEnvelope.getStatus());

			for (Map<String, Serializable> requestValues :
					_getObjectEntryValues(
						companyId, requestObjectDefinition,
						"(providerRequestId eq '" + providerRequestId + "')")) {

				long requestId = GetterUtil.getLong(
					requestValues.get(
						requestObjectDefinition.getPKObjectFieldName()));

				_updateRequestStatus(
					companyId, groupId, requestId, requestStatus);

				_updateRecipientStatuses(
					companyId, groupId, recipientObjectDefinition, fieldName,
					requestId, recipientStatuses);
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to sync the signature request for envelope " +
					providerRequestId,
				exception);
		}
	}

	private ServiceContext _createServiceContext(
		long companyId, long groupId, long userId) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(userId);

		return serviceContext;
	}

	private ObjectDefinition _fetchObjectDefinition(
		long companyId, String externalReferenceCode) {

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				externalReferenceCode, companyId);
	}

	private long _getRecipientUserId(long companyId, String emailAddress) {
		if (Validator.isNull(emailAddress)) {
			return 0;
		}

		User user = _userLocalService.fetchUserByEmailAddress(
			companyId, emailAddress);

		if (user == null) {
			return 0;
		}

		return user.getUserId();
	}

	private String _getRelationshipFieldName(
			ObjectDefinition requestObjectDefinition)
		throws Exception {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				requestObjectDefinition.getObjectDefinitionId(),
				"dsRequestToDSRequestRecipients");

		if (objectRelationship == null) {
			return null;
		}

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return objectField.getName();
	}

	private List<Map<String, Serializable>> _getObjectEntryValues(
			long companyId, ObjectDefinition objectDefinition,
			String filterString)
		throws Exception {

		return _objectEntryLocalService.getValuesList(
			0, companyId, objectDefinition.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			_filterFactory.create(filterString, objectDefinition), null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private boolean _isEnabled(long companyId, long groupId) {
		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, groupId);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return false;
		}

		return true;
	}

	private String _toRecipientStatus(String status) {
		status = StringUtil.toLowerCase(GetterUtil.getString(status));

		if (ArrayUtil.contains(_DS_RECIPIENT_STATUSES, status)) {
			return status;
		}

		return "sent";
	}

	private String _toRequestStatus(String status) {
		status = StringUtil.toLowerCase(GetterUtil.getString(status));

		if (ArrayUtil.contains(_DS_ENVELOPE_STATUSES, status)) {
			return status;
		}

		return "sent";
	}

	private void _updateRecipientStatuses(
			long companyId, long groupId,
			ObjectDefinition recipientObjectDefinition, String fieldName,
			long requestId, Map<String, String> recipientStatuses)
		throws Exception {

		for (Map<String, Serializable> recipientValues :
				_getObjectEntryValues(
					companyId, recipientObjectDefinition,
					StringBundler.concat(
						"(", fieldName, " eq '", requestId, "')"))) {

			String recipientStatus = recipientStatuses.get(
				GetterUtil.getString(
					recipientValues.get("providerRecipientId")));

			if (recipientStatus == null) {
				continue;
			}

			long recipientId = GetterUtil.getLong(
				recipientValues.get(
					recipientObjectDefinition.getPKObjectFieldName()));

			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				recipientId);

			if (objectEntry == null) {
				continue;
			}

			_objectEntryLocalService.updateObjectEntry(
				objectEntry.getUserId(), recipientId, 0,
				HashMapBuilder.putAll(
					objectEntry.getValues()
				).put(
					"requestRecipientStatus", recipientStatus
				).build(),
				_createServiceContext(
					companyId, groupId, objectEntry.getUserId()));
		}
	}

	private void _updateRequestStatus(
			long companyId, long groupId, long requestId, String requestStatus)
		throws Exception {

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			requestId);

		if (objectEntry == null) {
			return;
		}

		Serializable completionDate = objectEntry.getValues(
		).get(
			"completionDate"
		);

		if (Objects.equals(requestStatus, "completed")) {
			completionDate = new Date();
		}

		_objectEntryLocalService.updateObjectEntry(
			objectEntry.getUserId(), requestId, 0,
			HashMapBuilder.<String, Serializable>putAll(
				objectEntry.getValues()
			).put(
				"completionDate", completionDate
			).put(
				"requestStatus", requestStatus
			).build(),
			_createServiceContext(companyId, groupId, objectEntry.getUserId()));
	}

	private static final String[] _DS_ENVELOPE_STATUSES = {
		"completed", "created", "declined", "delivered", "sent", "voided"
	};

	private static final String[] _DS_RECIPIENT_STATUSES = {
		"completed", "created", "declined", "delivered", "sent", "signed"
	};

	private static final String _PROVIDER_KEY = "docusign";

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestManagerImpl.class);

	@Reference
	private DSEnvelopeManager _dsEnvelopeManager;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference
	private UserLocalService _userLocalService;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = DSRequestManager.class)
public class DSRequestManagerImpl implements DSRequestManager {

	@Override
	public void addDSRequest(
			long companyId, long groupId, long userId, DSEnvelope dsEnvelope,
			long[] fileEntryIds)
		throws PortalException {

		if (!_isEnabled(companyId, groupId)) {
			return;
		}

		ObjectDefinition documentObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_DOCUMENT");
		ObjectDefinition recipientObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_RECIPIENT");
		ObjectDefinition requestObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST");

		if ((documentObjectDefinition == null) ||
			(recipientObjectDefinition == null) ||
			(requestObjectDefinition == null)) {

			return;
		}

		String documentFieldName = _getRelationshipFieldName(
			requestObjectDefinition, "dsRequestToDSRequestDocuments");
		String recipientFieldName = _getRelationshipFieldName(
			requestObjectDefinition, "dsRequestToDSRequestRecipients");

		if ((documentFieldName == null) || (recipientFieldName == null)) {
			return;
		}

		ServiceContext serviceContext = _getServiceContext(
			companyId, groupId, userId);

		ObjectEntry requestObjectEntry =
			_objectEntryLocalService.addObjectEntry(
				0, userId, requestObjectDefinition.getObjectDefinitionId(),
				ObjectEntryFolderConstants.
					PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
				null,
				HashMapBuilder.<String, Serializable>put(
					"emailSubject", dsEnvelope.getEmailSubject()
				).put(
					"providerKey", "docusign"
				).put(
					"providerRequestId", dsEnvelope.getDSEnvelopeId()
				).put(
					"requestExpirationDate",
					_toDate(dsEnvelope.getExpireLocalDateTime())
				).put(
					"requestStatus", _getRequestStatus(dsEnvelope)
				).build(),
				serviceContext);

		try {
			for (long fileEntryId : fileEntryIds) {
				_objectEntryLocalService.addObjectEntry(
					0, userId, documentObjectDefinition.getObjectDefinitionId(),
					ObjectEntryFolderConstants.
						PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
					null,
					HashMapBuilder.<String, Serializable>put(
						documentFieldName, requestObjectEntry.getObjectEntryId()
					).put(
						"fileEntryId", fileEntryId
					).build(),
					serviceContext);
			}

			for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
				_objectEntryLocalService.addObjectEntry(
					0, userId,
					recipientObjectDefinition.getObjectDefinitionId(),
					ObjectEntryFolderConstants.
						PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
					null,
					HashMapBuilder.<String, Serializable>put(
						recipientFieldName,
						requestObjectEntry.getObjectEntryId()
					).put(
						"emailAddress", dsRecipient.getEmailAddress()
					).put(
						"name", dsRecipient.getName()
					).put(
						"providerRecipientId", dsRecipient.getDSRecipientId()
					).put(
						"r_userToDSRequestRecipients_userId",
						_getRecipientUserId(
							companyId, dsRecipient.getEmailAddress())
					).put(
						"requestRecipientStatus",
						_getRequestRecipientStatus(dsRecipient)
					).put(
						"sentDate", _toDate(dsRecipient.getSentLocalDateTime())
					).build(),
					serviceContext);
			}

			for (long fileEntryId : fileEntryIds) {
				_reindexFileEntry(fileEntryId);
			}
		}
		catch (Exception exception) {
			_objectEntryLocalService.deleteObjectEntry(requestObjectEntry);

			throw exception;
		}
	}

	@Override
	public void updateDSRequest(
		long companyId, long groupId, String providerRequestId) {

		if (!_isEnabled(companyId, groupId) ||
			Validator.isNull(providerRequestId)) {

			return;
		}

		ObjectDefinition documentObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_DOCUMENT");
		ObjectDefinition recipientObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_RECIPIENT");
		ObjectDefinition requestObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST");

		if ((documentObjectDefinition == null) ||
			(recipientObjectDefinition == null) ||
			(requestObjectDefinition == null)) {

			return;
		}

		try {
			String documentFieldName = _getRelationshipFieldName(
				requestObjectDefinition, "dsRequestToDSRequestDocuments");
			String recipientFieldName = _getRelationshipFieldName(
				requestObjectDefinition, "dsRequestToDSRequestRecipients");

			if ((documentFieldName == null) || (recipientFieldName == null)) {
				return;
			}

			DSEnvelope dsEnvelope = _dsEnvelopeManager.getDSEnvelope(
				companyId, groupId, providerRequestId);

			Map<String, DSRecipient> dsRecipientsByDSRecipientId =
				new HashMap<>();

			for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
				dsRecipientsByDSRecipientId.put(
					dsRecipient.getDSRecipientId(), dsRecipient);
			}

			for (Map<String, Serializable> requestValues :
					_getValuesList(
						companyId,
						StringBundler.concat(
							"(providerRequestId eq '", providerRequestId, "')"),
						requestObjectDefinition, null)) {

				long dsRequestId = GetterUtil.getLong(
					requestValues.get(
						requestObjectDefinition.getPKObjectFieldName()));

				_updateRequestStatus(
					companyId, groupId, dsEnvelope, dsRequestId);

				_updateRecipientStatuses(
					companyId, groupId, dsRecipientsByDSRecipientId,
					dsRequestId, recipientObjectDefinition, recipientFieldName);

				_reindexRequestDocuments(
					companyId, documentFieldName, documentObjectDefinition,
					dsRequestId);
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to sync the signature request for envelope " +
					providerRequestId,
				exception);
		}
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
			ObjectDefinition objectDefinition, String relationshipName)
		throws PortalException {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				objectDefinition.getObjectDefinitionId(), relationshipName);

		if (objectRelationship == null) {
			return null;
		}

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return objectField.getName();
	}

	private String _getRequestRecipientStatus(DSRecipient dsRecipient) {
		String status = StringUtil.toLowerCase(dsRecipient.getStatus());

		if (ArrayUtil.contains(_REQUEST_RECIPIENT_STATUSES, status)) {
			return status;
		}

		return "sent";
	}

	private String _getRequestStatus(DSEnvelope dsEnvelope) {
		String status = StringUtil.toLowerCase(dsEnvelope.getStatus());

		if (ArrayUtil.contains(_REQUEST_STATUSES, status)) {
			return status;
		}

		return "sent";
	}

	private ServiceContext _getServiceContext(
		long companyId, long groupId, long userId) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(userId);

		return serviceContext;
	}

	private List<Map<String, Serializable>> _getValuesList(
			long companyId, String filterString,
			ObjectDefinition objectDefinition, Sort[] sorts)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			PermissionThreadLocal.setPermissionChecker(null);

			return _objectEntryLocalService.getValuesList(
				0, companyId, objectDefinition.getUserId(),
				objectDefinition.getObjectDefinitionId(),
				_filterFactory.create(filterString, objectDefinition), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, sorts);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		}
	}

	private boolean _isEnabled(long companyId, long groupId) {
		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, groupId);

		return digitalSignatureConfiguration.enabled();
	}

	private void _reindexFileEntry(long fileEntryId) {
		try {
			Indexer<?> indexer = IndexerRegistryUtil.nullSafeGetIndexer(
				DLFileEntry.class);

			indexer.reindex(DLFileEntry.class.getName(), fileEntryId);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to reindex file entry " + fileEntryId, exception);
		}
	}

	private void _reindexRequestDocuments(
			long companyId, String documentFieldName,
			ObjectDefinition objectDefinition, long dsRequestId)
		throws Exception {

		for (Map<String, Serializable> documentValues :
				_getValuesList(
					companyId,
					StringBundler.concat(
						"(", documentFieldName, " eq '", dsRequestId, "')"),
					objectDefinition, null)) {

			_reindexFileEntry(
				GetterUtil.getLong(documentValues.get("fileEntryId")));
		}
	}

	private Date _toDate(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

	private void _updateRecipientStatuses(
			long companyId, long groupId,
			Map<String, DSRecipient> dsRecipientsByDSRecipientId,
			long dsRequestId, ObjectDefinition objectDefinition,
			String recipientFieldName)
		throws Exception {

		for (Map<String, Serializable> recipientValues :
				_getValuesList(
					companyId,
					StringBundler.concat(
						"(", recipientFieldName, " eq '", dsRequestId, "')"),
					objectDefinition, null)) {

			DSRecipient dsRecipient = dsRecipientsByDSRecipientId.get(
				GetterUtil.getString(
					recipientValues.get("providerRecipientId")));

			if (dsRecipient == null) {
				continue;
			}

			long dsRequestRecipientId = GetterUtil.getLong(
				recipientValues.get(objectDefinition.getPKObjectFieldName()));

			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				dsRequestRecipientId);

			if (objectEntry == null) {
				continue;
			}

			Map<String, Serializable> values =
				HashMapBuilder.<String, Serializable>putAll(
					objectEntry.getValues()
				).put(
					"requestRecipientStatus",
					_getRequestRecipientStatus(dsRecipient)
				).put(
					"requestRecipientStatusDate",
					() -> _toDate(dsRecipient.getStatusLocalDateTime())
				).put(
					"sentDate",
					() -> _toDate(dsRecipient.getSentLocalDateTime())
				).build();

			_objectEntryLocalService.updateObjectEntry(
				objectEntry.getUserId(), dsRequestRecipientId,
				ObjectEntryFolderConstants.
					PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
				values,
				_getServiceContext(
					companyId, groupId, objectEntry.getUserId()));
		}
	}

	private void _updateRequestStatus(
			long companyId, long groupId, DSEnvelope dsEnvelope,
			long dsRequestId)
		throws Exception {

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			dsRequestId);

		if (objectEntry == null) {
			return;
		}

		Map<String, Serializable> values =
			HashMapBuilder.<String, Serializable>putAll(
				objectEntry.getValues()
			).put(
				"requestExpirationDate",
				() -> _toDate(dsEnvelope.getExpireLocalDateTime())
			).put(
				"requestStatus", _getRequestStatus(dsEnvelope)
			).put(
				"requestStatusDate",
				() -> _toDate(dsEnvelope.getStatusChangedLocalDateTime())
			).build();

		_objectEntryLocalService.updateObjectEntry(
			objectEntry.getUserId(), dsRequestId,
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			values,
			_getServiceContext(companyId, groupId, objectEntry.getUserId()));
	}

	private static final String[] _REQUEST_RECIPIENT_STATUSES = {
		"completed", "created", "declined", "sent", "signed"
	};

	private static final String[] _REQUEST_STATUSES = {
		"completed", "created", "declined", "sent", "voided"
	};

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
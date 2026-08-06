/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.action;

import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.model.DSRequestRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(
	property = {
		"jakarta.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
		"jakarta.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN,
		"mvc.command.name=/document_library/get_signature_details"
	},
	service = MVCResourceCommand.class
)
public class GetSignatureDetailsMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long fileEntryId = ParamUtil.getLong(resourceRequest, "fileEntryId");

		_fileEntryModelResourcePermission.check(
			themeDisplay.getPermissionChecker(), fileEntryId, ActionKeys.VIEW);

		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			themeDisplay.getCompanyId(), fileEntryId);

		if (dsRequest == null) {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				_jsonFactory.createJSONObject());

			return;
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put(
				"createDate", _toTime(dsRequest.getCreateDate())
			).put(
				"emailSubject", dsRequest.getEmailSubject()
			).put(
				"expirationDate", _toTime(dsRequest.getExpirationDate())
			).put(
				"providerRequestId", dsRequest.getProviderRequestId()
			).put(
				"recipients",
				JSONUtil.toJSONArray(
					dsRequest.getDSRequestRecipients(),
					this::_toRecipientJSONObject)
			).put(
				"requesterEmailAddress", dsRequest.getRequesterEmailAddress()
			).put(
				"requesterName", dsRequest.getRequesterName()
			).put(
				"requestStatus", dsRequest.getStatus()
			).put(
				"statusDate", _toTime(dsRequest.getStatusDate())
			));
	}

	private JSONObject _toRecipientJSONObject(
		DSRequestRecipient dsRequestRecipient) {

		return JSONUtil.put(
			"emailAddress", dsRequestRecipient.getEmailAddress()
		).put(
			"name", dsRequestRecipient.getName()
		).put(
			"requestRecipientStatus", dsRequestRecipient.getStatus()
		).put(
			"sentDate", _toTime(dsRequestRecipient.getSentDate())
		).put(
			"statusDate", _toTime(dsRequestRecipient.getStatusDate())
		);
	}

	private Long _toTime(Date date) {
		if (date == null) {
			return null;
		}

		return date.getTime();
	}

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference(
		target = "(model.class.name=com.liferay.portal.kernel.repository.model.FileEntry)"
	)
	private ModelResourcePermission<FileEntry>
		_fileEntryModelResourcePermission;

	@Reference
	private JSONFactory _jsonFactory;

}
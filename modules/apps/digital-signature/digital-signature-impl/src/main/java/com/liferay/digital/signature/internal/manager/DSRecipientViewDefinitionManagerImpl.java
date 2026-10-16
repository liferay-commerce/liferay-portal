/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureSystemConfigurationUtil;
import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.digital.signature.manager.DSRecipientViewDefinitionManager;
import com.liferay.digital.signature.model.DSRecipientViewDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Validator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author José Abelenda
 */
@Component(service = DSRecipientViewDefinitionManager.class)
public class DSRecipientViewDefinitionManagerImpl
	implements DSRecipientViewDefinitionManager {

	@Override
	public String addDSRecipientViewDefinition(
			long companyId, long groupId, long userId, String dsEnvelopeId,
			String portalURL)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if ((permissionChecker == null) ||
			((permissionChecker.getUserId() != userId) &&
			 !permissionChecker.isCompanyAdmin(companyId))) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, DSRecipientViewDefinition.class.getName(),
				userId, ActionKeys.VIEW);
		}

		DSRecipientViewDefinition dsRecipientViewDefinition =
			new DSRecipientViewDefinition();

		dsRecipientViewDefinition.setAuthenticationMethod("none");
		dsRecipientViewDefinition.setDSClientUserId(String.valueOf(userId));

		User user = _userLocalService.getUser(userId);

		dsRecipientViewDefinition.setEmailAddress(user.getEmailAddress());

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, groupId);

		String appOriginURL =
			DigitalSignatureSystemConfigurationUtil.getAppOriginURL(
				digitalSignatureConfiguration.environment());

		dsRecipientViewDefinition.setFrameAncestors(
			new String[] {portalURL, appOriginURL});
		dsRecipientViewDefinition.setMessageOrigins(
			new String[] {appOriginURL});
		dsRecipientViewDefinition.setReturnURL(appOriginURL);

		dsRecipientViewDefinition.setUserName(user.getFullName());

		return _addDSRecipientViewDefinition(
			companyId, groupId, dsEnvelopeId, dsRecipientViewDefinition);
	}

	@Override
	public String addDSRecipientViewDefinition(
			long companyId, long groupId, String dsEnvelopeId,
			DSRecipientViewDefinition dsRecipientViewDefinition)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if ((permissionChecker == null) ||
			!permissionChecker.isCompanyAdmin(companyId)) {

			throw new PrincipalException.MustBeCompanyAdmin(permissionChecker);
		}

		return _addDSRecipientViewDefinition(
			companyId, groupId, dsEnvelopeId, dsRecipientViewDefinition);
	}

	private String _addDSRecipientViewDefinition(
			long companyId, long groupId, String dsEnvelopeId,
			DSRecipientViewDefinition dsRecipientViewDefinition)
		throws Exception {

		JSONObject jsonObject = _dsHttp.post(
			companyId, groupId,
			StringBundler.concat(
				"envelopes/", dsEnvelopeId, "/views/recipient"),
			dsRecipientViewDefinition.toJSONObject());

		if (Validator.isNotNull(jsonObject.getString("errorCode"))) {
			throw new PortalException(
				StringBundler.concat(
					"Unable to create a recipient view for envelope ",
					dsEnvelopeId, ": ", jsonObject.getString("message")));
		}

		return jsonObject.getString("url");
	}

	@Reference
	private DSHttp _dsHttp;

	@Reference
	private UserLocalService _userLocalService;

}
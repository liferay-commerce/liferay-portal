/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.util;

import com.liferay.account.service.AccountEntryUserRelLocalServiceUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalServiceUtil;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalServiceUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.constants.DigitalSignatureConstants;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brian I. Kim
 */
public class DSCommerceOrderAttachmentUtil {

	public static List<User> getAccountUsers(CommerceOrder commerceOrder) {
		return TransformUtil.transform(
			AccountEntryUserRelLocalServiceUtil.
				getAccountEntryUserRelsByAccountEntryId(
					commerceOrder.getCommerceAccountId()),
			accountEntryUserRel -> {
				User user = UserLocalServiceUtil.fetchUser(
					accountEntryUserRel.getAccountUserId());

				if ((user == null) || !user.isActive() ||
					(user.getType() != UserConstants.TYPE_REGULAR)) {

					return null;
				}

				return user;
			});
	}

	public static Map<Long, DSRequest> getDSRequests(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!isEnabled(commerceOrder)) {
			return Collections.emptyMap();
		}

		String key =
			DSCommerceOrderAttachmentUtil.class.getName() +
				commerceOrder.getCommerceOrderId();

		Map<Long, DSRequest> dsRequests =
			(Map<Long, DSRequest>)httpServletRequest.getAttribute(key);

		if (dsRequests != null) {
			return dsRequests;
		}

		Map<Long, Long> fileEntryIds = new LinkedHashMap<>();

		for (CommerceOrderAttachment commerceOrderAttachment :
				CommerceOrderAttachmentLocalServiceUtil.
					getCommerceOrderAttachments(
						commerceOrder.getCommerceOrderId(), QueryUtil.ALL_POS,
						QueryUtil.ALL_POS, null)) {

			fileEntryIds.put(
				commerceOrderAttachment.getCommerceOrderAttachmentId(),
				commerceOrderAttachment.getFileEntryId());
		}

		DSRequestManager dsRequestManager = _dsRequestManagerSnapshot.get();

		Map<Long, DSRequest> fileEntryDSRequests =
			dsRequestManager.getDSRequests(
				commerceOrder.getCompanyId(),
				ArrayUtil.toLongArray(fileEntryIds.values()));

		dsRequests = new LinkedHashMap<>();

		for (Map.Entry<Long, Long> entry : fileEntryIds.entrySet()) {
			DSRequest dsRequest = fileEntryDSRequests.get(entry.getValue());

			if (dsRequest != null) {
				dsRequests.put(entry.getKey(), dsRequest);
			}
		}

		httpServletRequest.setAttribute(key, dsRequests);

		return dsRequests;
	}

	public static Map<String, String> getSignatureStatuses(
		Map<Long, DSRequest> dsRequests) {

		Map<String, String> signatureStatuses = new HashMap<>();

		for (Map.Entry<Long, DSRequest> entry : dsRequests.entrySet()) {
			DSRequest dsRequest = entry.getValue();

			signatureStatuses.put(
				String.valueOf(entry.getKey()), dsRequest.getStatus());
		}

		return signatureStatuses;
	}

	public static long getSiteGroupId(CommerceOrder commerceOrder) {
		CommerceChannel commerceChannel =
			CommerceChannelLocalServiceUtil.fetchCommerceChannelByGroupClassPK(
				commerceOrder.getGroupId());

		if (commerceChannel == null) {
			return 0;
		}

		return commerceChannel.getSiteGroupId();
	}

	public static JSONObject getUserJSONObject(User user) {
		return JSONUtil.put(
			"emailAddress", user.getEmailAddress()
		).put(
			"name", user.getFullName()
		).put(
			"userId", user.getUserId()
		);
	}

	public static boolean hasAllowedFileExtension(
			CommerceOrderAttachment commerceOrderAttachment)
		throws PortalException {

		FileEntry fileEntry = DLAppLocalServiceUtil.fetchFileEntry(
			commerceOrderAttachment.getFileEntryId());

		if (fileEntry == null) {
			return false;
		}

		return ArrayUtil.contains(
			DigitalSignatureConstants.ALLOWED_FILE_EXTENSIONS,
			fileEntry.getExtension());
	}

	public static boolean isEnabled(CommerceOrder commerceOrder) {
		if (!FeatureFlagManagerUtil.isEnabled(
				commerceOrder.getCompanyId(), "LPD-69290")) {

			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				commerceOrder.getCompanyId(), getSiteGroupId(commerceOrder));

		if (digitalSignatureConfiguration.embeddedViewEnabled() &&
			digitalSignatureConfiguration.enabled()) {

			return true;
		}

		return false;
	}

	private static final Snapshot<DSRequestManager> _dsRequestManagerSnapshot =
		new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class, DSRequestManager.class);

}
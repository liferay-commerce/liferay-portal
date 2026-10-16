/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager;

import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import java.util.List;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Brian I. Kim
 */
@ProviderType
public interface DSRequestManager {

	public DSRequest addDSRequest(
			long companyId, long groupId, long userId, DSEnvelope dsEnvelope,
			long[] fileEntryIds)
		throws PortalException;

	public DSRequest fetchDSRequest(long companyId, long dsRequestId);

	public DSRequest fetchFileEntryDSRequest(long companyId, long fileEntryId);

	public Map<Long, DSRequest> getDSRequests(
		long companyId, long[] fileEntryIds);

	public List<DSRequest> getFileEntryDSRequests(
		long companyId, long fileEntryId);

	public List<DSRequest> getSiteDSRequests(
		long companyId, long siteGroupId, String search, int start, int end);

	public int getSiteDSRequestsCount(
		long companyId, long siteGroupId, String search);

	public boolean hasPermission(
			PermissionChecker permissionChecker, DSRequest dsRequest,
			String actionId)
		throws PortalException;

	public void sendDSRequestNotifications(long dsRequestId)
		throws PortalException;

	public void sendSignatureReminders(long companyId);

	public void updateDSRequest(long dsRequestId) throws PortalException;

	public void voidDSRequest(long dsRequestId, String reason)
		throws PortalException;

}
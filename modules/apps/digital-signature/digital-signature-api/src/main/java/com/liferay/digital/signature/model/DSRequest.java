/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.model;

import com.liferay.digital.signature.constants.DigitalSignatureConstants;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Brian Kim
 * @author Danny Situ
 */
public class DSRequest implements Serializable {

	public DSRequest(
		Date createDate, List<DSRequestRecipient> dsRequestRecipients,
		String requesterEmailAddress, String requesterName,
		long requesterUserId, Map<String, Serializable> values) {

		_createDate = createDate;
		_dsRequestRecipients = dsRequestRecipients;
		_requesterEmailAddress = requesterEmailAddress;
		_requesterName = requesterName;
		_requesterUserId = requesterUserId;

		_emailSubject = GetterUtil.getString(values.get("emailSubject"));
		_expirationDate = _toDate(values.get("expirationDateTime"));
		_providerKey = GetterUtil.getString(values.get("providerKey"));
		_providerRequestId = GetterUtil.getString(
			values.get("providerRequestId"));
		_status = GetterUtil.getString(values.get("requestStatus"));
		_statusDate = _toDate(values.get("statusDateTime"));
	}

	public Date getCreateDate() {
		if (_createDate == null) {
			return null;
		}

		return new Date(_createDate.getTime());
	}

	public List<DSRequestRecipient> getDSRequestRecipients() {
		if (_dsRequestRecipients == null) {
			return Collections.emptyList();
		}

		return _dsRequestRecipients;
	}

	public String getEmailSubject() {
		return _emailSubject;
	}

	public Date getExpirationDate() {
		if (_expirationDate == null) {
			return null;
		}

		return new Date(_expirationDate.getTime());
	}

	public String getProviderKey() {
		return _providerKey;
	}

	public String getProviderRequestId() {
		return _providerRequestId;
	}

	public String getRequesterEmailAddress() {
		return _requesterEmailAddress;
	}

	public String getRequesterName() {
		return _requesterName;
	}

	public long getRequesterUserId() {
		return _requesterUserId;
	}

	public String getStatus() {
		return _status;
	}

	public Date getStatusDate() {
		if (_statusDate == null) {
			return null;
		}

		return new Date(_statusDate.getTime());
	}

	public boolean isSignatureRequired(long userId) {
		if ((userId <= 0) || isTerminal()) {
			return false;
		}

		for (DSRequestRecipient dsRequestRecipient : getDSRequestRecipients()) {
			if ((dsRequestRecipient.getUserId() != userId) ||
				!ArrayUtil.contains(
					DigitalSignatureConstants.
						REQUEST_RECIPIENT_STATUSES_PENDING,
					dsRequestRecipient.getStatus())) {

				continue;
			}

			return true;
		}

		return false;
	}

	public boolean isTerminal() {
		if (Validator.isNull(_status)) {
			return false;
		}

		return ArrayUtil.contains(
			DigitalSignatureConstants.REQUEST_STATUSES_TERMINAL, _status);
	}

	private Date _toDate(Serializable value) {
		if (value instanceof Date) {
			return (Date)value;
		}

		return null;
	}

	private final Date _createDate;
	private final List<DSRequestRecipient> _dsRequestRecipients;
	private final String _emailSubject;
	private final Date _expirationDate;
	private final String _providerKey;
	private final String _providerRequestId;
	private final String _requesterEmailAddress;
	private final String _requesterName;
	private final long _requesterUserId;
	private final String _status;
	private final Date _statusDate;

}
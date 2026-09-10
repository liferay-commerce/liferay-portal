/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.model;

import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;

/**
 * @author Brian Kim
 * @author Danny Situ
 */
public class DSRequestRecipient implements Serializable {

	public DSRequestRecipient(Map<String, Serializable> values) {
		_emailAddress = GetterUtil.getString(values.get("emailAddress"));
		_name = GetterUtil.getString(values.get("name"));
		_providerRecipientId = GetterUtil.getString(
			values.get("providerRecipientId"));
		_sentDate = _toDate(values.get("sentDate"));
		_status = GetterUtil.getString(values.get("requestRecipientStatus"));
		_statusDate = _toDate(values.get("statusDateTime"));
		_userId = GetterUtil.getLong(
			values.get("r_userToDSRequestRecipient_userId"));
	}

	public String getEmailAddress() {
		return _emailAddress;
	}

	public String getName() {
		return _name;
	}

	public String getProviderRecipientId() {
		return _providerRecipientId;
	}

	public Date getSentDate() {
		if (_sentDate == null) {
			return null;
		}

		return new Date(_sentDate.getTime());
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

	public long getUserId() {
		return _userId;
	}

	private Date _toDate(Serializable value) {
		if (value instanceof Date) {
			return (Date)value;
		}

		return null;
	}

	private final String _emailAddress;
	private final String _name;
	private final String _providerRecipientId;
	private final Date _sentDate;
	private final String _status;
	private final Date _statusDate;
	private final long _userId;

}
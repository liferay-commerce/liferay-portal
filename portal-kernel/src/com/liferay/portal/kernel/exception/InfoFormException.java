/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.exception;

import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.Locale;

/**
 * @author Rubén Pulido
 */
public class InfoFormException extends PortalException {

	public InfoFormException() {
	}

	public InfoFormException(String message) {
		_message = message;
	}

	public String getLocalizedMessage(Locale locale) {
		if (_message != null) {
			return _message;
		}

		return LanguageUtil.get(
			locale, "an-error-occurred-while-sending-the-form-information");
	}

	private String _message;

}
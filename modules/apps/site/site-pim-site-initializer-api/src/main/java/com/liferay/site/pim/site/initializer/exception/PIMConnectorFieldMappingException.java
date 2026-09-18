/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Andrea Sbarra
 */
public class PIMConnectorFieldMappingException extends PortalException {

	public PIMConnectorFieldMappingException() {
	}

	public PIMConnectorFieldMappingException(String msg) {
		super(msg);
	}

	public PIMConnectorFieldMappingException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public PIMConnectorFieldMappingException(Throwable throwable) {
		super(throwable);
	}

}
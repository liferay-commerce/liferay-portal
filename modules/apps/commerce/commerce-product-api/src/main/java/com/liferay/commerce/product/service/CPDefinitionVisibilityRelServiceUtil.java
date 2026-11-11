/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

import com.liferay.portal.kernel.module.service.Snapshot;

/**
 * Provides the remote service utility for CPDefinitionVisibilityRel. This utility wraps
 * <code>com.liferay.commerce.product.service.impl.CPDefinitionVisibilityRelServiceImpl</code> and is an
 * access point for service operations in application layer code running on a
 * remote server. Methods of this service are expected to have security checks
 * based on the propagated JAAS credentials because this service can be
 * accessed remotely.
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelService
 * @generated
 */
public class CPDefinitionVisibilityRelServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.commerce.product.service.impl.CPDefinitionVisibilityRelServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	public static String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	public static CPDefinitionVisibilityRelService getService() {
		return _serviceSnapshot.get();
	}

	private static final Snapshot<CPDefinitionVisibilityRelService>
		_serviceSnapshot = new Snapshot<>(
			CPDefinitionVisibilityRelServiceUtil.class,
			CPDefinitionVisibilityRelService.class);

}
// LIFERAY-SERVICE-BUILDER-HASH:-457734091
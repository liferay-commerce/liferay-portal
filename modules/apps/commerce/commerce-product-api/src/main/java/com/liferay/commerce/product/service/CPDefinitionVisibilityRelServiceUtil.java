/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

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
	public static CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
			String externalReferenceCode, long cpDefinitionId, String className,
			long classPK)
		throws PortalException {

		return getService().addCPDefinitionVisibilityRel(
			externalReferenceCode, cpDefinitionId, className, classPK);
	}

	public static void deleteCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws PortalException {

		getService().deleteCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRelId);
	}

	public static CPDefinitionVisibilityRel getCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws PortalException {

		return getService().getCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRelId);
	}

	public static List<CPDefinitionVisibilityRel> getCPDefinitionVisibilityRels(
			long cpDefinitionId, String className, int start, int end,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws PortalException {

		return getService().getCPDefinitionVisibilityRels(
			cpDefinitionId, className, start, end, orderByComparator);
	}

	public static int getCPDefinitionVisibilityRelsCount(
			long cpDefinitionId, String className)
		throws PortalException {

		return getService().getCPDefinitionVisibilityRelsCount(
			cpDefinitionId, className);
	}

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
// LIFERAY-SERVICE-BUILDER-HASH:-905028152
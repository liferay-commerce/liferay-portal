/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link CPDefinitionVisibilityRelService}.
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelService
 * @generated
 */
public class CPDefinitionVisibilityRelServiceWrapper
	implements CPDefinitionVisibilityRelService,
			   ServiceWrapper<CPDefinitionVisibilityRelService> {

	public CPDefinitionVisibilityRelServiceWrapper() {
		this(null);
	}

	public CPDefinitionVisibilityRelServiceWrapper(
		CPDefinitionVisibilityRelService cpDefinitionVisibilityRelService) {

		_cpDefinitionVisibilityRelService = cpDefinitionVisibilityRelService;
	}

	@Override
	public CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
			String externalReferenceCode, long cpDefinitionId, String className,
			long classPK)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelService.addCPDefinitionVisibilityRel(
			externalReferenceCode, cpDefinitionId, className, classPK);
	}

	@Override
	public void deleteCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRelId);
	}

	@Override
	public CPDefinitionVisibilityRel getCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRelId);
	}

	@Override
	public java.util.List<CPDefinitionVisibilityRel>
			getCPDefinitionVisibilityRels(
				long cpDefinitionId, String className, int start, int end,
				com.liferay.portal.kernel.util.OrderByComparator
					<CPDefinitionVisibilityRel> orderByComparator)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRels(
			cpDefinitionId, className, start, end, orderByComparator);
	}

	@Override
	public int getCPDefinitionVisibilityRelsCount(
			long cpDefinitionId, String className)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelService.
			getCPDefinitionVisibilityRelsCount(cpDefinitionId, className);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _cpDefinitionVisibilityRelService.getOSGiServiceIdentifier();
	}

	@Override
	public CPDefinitionVisibilityRelService getWrappedService() {
		return _cpDefinitionVisibilityRelService;
	}

	@Override
	public void setWrappedService(
		CPDefinitionVisibilityRelService cpDefinitionVisibilityRelService) {

		_cpDefinitionVisibilityRelService = cpDefinitionVisibilityRelService;
	}

	private CPDefinitionVisibilityRelService _cpDefinitionVisibilityRelService;

}
// LIFERAY-SERVICE-BUILDER-HASH:63692066
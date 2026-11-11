/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

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
// LIFERAY-SERVICE-BUILDER-HASH:751045697
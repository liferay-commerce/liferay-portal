/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link CommerceOrderTypeRelService}.
 *
 * @author Alessio Antonio Rendina
 * @see CommerceOrderTypeRelService
 * @generated
 */
public class CommerceOrderTypeRelServiceWrapper
	implements CommerceOrderTypeRelService,
			   ServiceWrapper<CommerceOrderTypeRelService> {

	public CommerceOrderTypeRelServiceWrapper(
		CommerceOrderTypeRelService commerceOrderTypeRelService) {

		_commerceOrderTypeRelService = commerceOrderTypeRelService;
	}

	@Override
	public com.liferay.commerce.model.CommerceOrderTypeRel
			addCommerceOrderTypeRel(
				String className, long classPK, long commerceOrderTypeId,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceOrderTypeRelService.addCommerceOrderTypeRel(
			className, classPK, commerceOrderTypeId, serviceContext);
	}

	@Override
	public com.liferay.commerce.model.CommerceOrderTypeRel
			deleteCommerceOrderTypeRel(long commerceOrderTypeRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceOrderTypeRelService.deleteCommerceOrderTypeRel(
			commerceOrderTypeRelId);
	}

	@Override
	public void deleteCommerceOrderTypeRels(
			String className, long commerceOrderTypeId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_commerceOrderTypeRelService.deleteCommerceOrderTypeRels(
			className, commerceOrderTypeId);
	}

	@Override
	public java.util.List<com.liferay.commerce.model.CommerceOrderTypeRel>
			getCommerceOrderTypeCommerceChannelRels(
				long commerceOrderTypeId, String keywords, int start, int end)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceOrderTypeRelService.
			getCommerceOrderTypeCommerceChannelRels(
				commerceOrderTypeId, keywords, start, end);
	}

	@Override
	public int getCommerceOrderTypeCommerceChannelRelsCount(
			long commerceOrderTypeId, String keywords)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceOrderTypeRelService.
			getCommerceOrderTypeCommerceChannelRelsCount(
				commerceOrderTypeId, keywords);
	}

	@Override
	public com.liferay.commerce.model.CommerceOrderTypeRel
			getCommerceOrderTypeRel(long commerceOrderTypeRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceOrderTypeRelService.getCommerceOrderTypeRel(
			commerceOrderTypeRelId);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _commerceOrderTypeRelService.getOSGiServiceIdentifier();
	}

	@Override
	public CommerceOrderTypeRelService getWrappedService() {
		return _commerceOrderTypeRelService;
	}

	@Override
	public void setWrappedService(
		CommerceOrderTypeRelService commerceOrderTypeRelService) {

		_commerceOrderTypeRelService = commerceOrderTypeRelService;
	}

	private CommerceOrderTypeRelService _commerceOrderTypeRelService;

}
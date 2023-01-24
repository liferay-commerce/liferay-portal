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

package com.liferay.commerce.product.internal.util;

import com.liferay.commerce.currency.constants.CommerceCurrencyConstantsContributor;
import com.liferay.commerce.currency.exception.CommerceCurrencyCannotDeleteException;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.util.CommerceCurrencyContributor;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.portal.kernel.exception.PortalException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(
	property = "commerce.currency.contributor.key=" + CommerceCurrencyConstantsContributor.COMMERCE_CHANNEL,
	service = CommerceCurrencyContributor.class
)
public class CommerceChannelCommerceCurrencyContributor
	implements CommerceCurrencyContributor {

	@Override
	public void check(CommerceCurrency commerceCurrency)
		throws PortalException {

		int commerceCatalogsCount =
			_commerceCatalogLocalService.getCommerceChannelsCount(
				commerceCurrency.getCode());

		if (commerceCatalogsCount > 0) {
			throw new CommerceCurrencyCannotDeleteException();
		}
	}

	@Reference
	private CommerceChannelLocalService _commerceCatalogLocalService;

}
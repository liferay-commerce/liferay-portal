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

package com.liferay.commerce.discount.internal.validator.helper;

import com.liferay.commerce.discount.validator.CommerceDiscountValidator;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorRegistry;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorRequest;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorResult;
import com.liferay.commerce.discount.validator.helper.CommerceDiscountValidatorHelper;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
@Component(enabled = false, service = CommerceDiscountValidatorHelper.class)
public class CommerceDiscountValidatorHelperImpl
	implements CommerceDiscountValidatorHelper {

	@Override
	public boolean isValid(
			CommerceDiscountValidatorRequest commerceDiscountValidatorRequest,
			String type)
		throws PortalException {

		List<CommerceDiscountValidatorResult> commerceDiscountValidatorResults =
			validate(commerceDiscountValidatorRequest, type);

		return commerceDiscountValidatorResults.isEmpty();
	}

	@Override
	public List<CommerceDiscountValidatorResult> validate(
			CommerceDiscountValidatorRequest commerceDiscountValidatorRequest,
			String type)
		throws PortalException {

		List<CommerceDiscountValidatorResult> commerceDiscountValidatorResults =
			new ArrayList<>();

		List<CommerceDiscountValidator> commerceDiscountValidators =
			_commerceDiscountValidatorRegistry.getCommerceDiscountValidators(
				type);

		for (CommerceDiscountValidator commerceDiscountValidator :
				commerceDiscountValidators) {

			CommerceDiscountValidatorResult commerceDiscountValidatorResult =
				commerceDiscountValidator.validate(
					commerceDiscountValidatorRequest);

			if (!commerceDiscountValidatorResult.isValid()) {
				commerceDiscountValidatorResults.add(
					commerceDiscountValidatorResult);
			}
		}

		return commerceDiscountValidatorResults;
	}

	@Reference
	private CommerceDiscountValidatorRegistry
		_commerceDiscountValidatorRegistry;

}
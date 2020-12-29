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

package com.liferay.commerce.discount.test.util;

import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.validator.CommerceDiscountValidator;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorRequest;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorResult;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author Riccardo Alberti
 */
@Component(
	enabled = true, immediate = true,
	property = {
		"commerce.discount.validator.key=" + TestCommerceDiscountValidator.KEY,
		"commerce.discount.validator.priority:Integer=10",
		"commerce.discount.validator.type=test"
	},
	service = CommerceDiscountValidator.class
)
public class TestCommerceDiscountValidator
	implements CommerceDiscountValidator {

	public static final String KEY = "test";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public CommerceDiscountValidatorResult validate(
			CommerceDiscountValidatorRequest commerceDiscountValidatorRequest)
		throws PortalException {

		CommerceDiscount commerceDiscount =
			commerceDiscountValidatorRequest.getCommerceDiscount();

		if (Objects.equals(commerceDiscount.getTitle(), "validDiscount")) {
			return new CommerceDiscountValidatorResult(true);
		}

		return new CommerceDiscountValidatorResult(
			commerceDiscount.getCommerceDiscountId(), false,
			"the-discount-is-not-active");
	}

}
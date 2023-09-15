/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.internal.validator.helper;

import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.discount.exception.CommerceDiscountValidatorException;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.validator.CommerceDiscountValidator;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorRegistry;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorResult;
import com.liferay.commerce.discount.validator.helper.CommerceDiscountValidatorHelper;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
@Component(service = CommerceDiscountValidatorHelper.class)
public class CommerceDiscountValidatorHelperImpl
	implements CommerceDiscountValidatorHelper {

	@Override
	public void checkValid(
			CommerceContext commerceContext, CommerceDiscount commerceDiscount,
			String... types)
		throws PortalException {

		List<CommerceDiscountValidator> commerceDiscountValidators =
			_commerceDiscountValidatorRegistry.getCommerceDiscountValidators(
				types);

		for (CommerceDiscountValidator commerceDiscountValidator :
				commerceDiscountValidators) {

			CommerceDiscountValidatorResult commerceDiscountValidatorResult =
				commerceDiscountValidator.validate(
					commerceContext, commerceDiscount);

			if (!commerceDiscountValidatorResult.isValid()) {
				throw new CommerceDiscountValidatorException(
					commerceDiscountValidatorResult.getMessage());
			}
		}
	}

	@Override
	public void clearUsage(long commerceDiscountId) {
		_commerceDiscountUsageCounter.remove(commerceDiscountId);
	}

	@Override
	public void decrementUsage(long commerceDiscountId) {
		AtomicInteger usage = _commerceDiscountUsageCounter.get(
			commerceDiscountId);

		usage.getAndDecrement();
	}

	@Override
	public int getUsage(long commerceDiscountId) {
		AtomicInteger usage = _commerceDiscountUsageCounter.computeIfAbsent(
			commerceDiscountId, k -> new AtomicInteger(0));

		return usage.get();
	}

	@Override
	public void incrementUsage(long commerceDiscountId) {
		AtomicInteger usage = _commerceDiscountUsageCounter.computeIfAbsent(
			commerceDiscountId, k -> new AtomicInteger(0));

		usage.getAndIncrement();
	}

	@Override
	public boolean isValid(
			CommerceContext commerceContext, CommerceDiscount commerceDiscount,
			String... types)
		throws PortalException {

		List<CommerceDiscountValidatorResult> commerceDiscountValidatorResults =
			validate(commerceContext, commerceDiscount, types);

		return commerceDiscountValidatorResults.isEmpty();
	}

	@Override
	public List<CommerceDiscountValidatorResult> validate(
			CommerceContext commerceContext, CommerceDiscount commerceDiscount,
			String... types)
		throws PortalException {

		List<CommerceDiscountValidatorResult> commerceDiscountValidatorResults =
			new ArrayList<>();

		List<CommerceDiscountValidator> commerceDiscountValidators =
			_commerceDiscountValidatorRegistry.getCommerceDiscountValidators(
				types);

		for (CommerceDiscountValidator commerceDiscountValidator :
				commerceDiscountValidators) {

			CommerceDiscountValidatorResult commerceDiscountValidatorResult =
				commerceDiscountValidator.validate(
					commerceContext, commerceDiscount);

			if (!commerceDiscountValidatorResult.isValid()) {
				commerceDiscountValidatorResults.add(
					commerceDiscountValidatorResult);
			}
		}

		return commerceDiscountValidatorResults;
	}

	private final ConcurrentHashMap<Long, AtomicInteger>
		_commerceDiscountUsageCounter = new ConcurrentHashMap<>();

	@Reference
	private CommerceDiscountValidatorRegistry
		_commerceDiscountValidatorRegistry;

}
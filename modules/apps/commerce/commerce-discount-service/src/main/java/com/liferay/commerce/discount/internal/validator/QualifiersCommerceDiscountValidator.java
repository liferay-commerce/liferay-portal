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

package com.liferay.commerce.discount.internal.validator;

import com.liferay.commerce.account.model.CommerceAccount;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountAccountRel;
import com.liferay.commerce.discount.service.CommerceDiscountAccountRelLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountCommerceAccountGroupRelLocalService;
import com.liferay.commerce.discount.validator.CommerceDiscountValidator;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorRequest;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorResult;
import com.liferay.commerce.product.model.CommerceChannelRel;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
@Component(
	enabled = false, immediate = true,
	property = {
		"commerce.discount.validator.key=" + QualifiersCommerceDiscountValidator.KEY,
		"commerce.discount.validator.priority:Integer=10",
		"commerce.discount.validator.type=" + CommerceDiscountConstants.VALIDATOR_TYPE_FULL
	},
	service = CommerceDiscountValidator.class
)
public class QualifiersCommerceDiscountValidator
	implements CommerceDiscountValidator {

	public static final String KEY = "qualifiers";

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

		if (!commerceDiscount.isActive()) {
			return new CommerceDiscountValidatorResult(
				commerceDiscount.getCommerceDiscountId(), false,
				"the-discount-is-not-active");
		}

		if (_isUnqualifiedCommerceDiscount(
				commerceDiscount.getCommerceDiscountId())) {

			return new CommerceDiscountValidatorResult(true);
		}

		CommerceContext commerceContext =
			commerceDiscountValidatorRequest.getCommerceContext();

		CommerceAccount commerceAccount = commerceContext.getCommerceAccount();

		CommerceDiscountAccountRel commerceDiscountAccountRel =
			_commerceDiscountAccountRelLocalService.
				fetchCommerceDiscountAccountRel(
					commerceDiscount.getCommerceDiscountId(),
					commerceAccount.getCommerceAccountId());

		if (commerceDiscountAccountRel != null) {
			return new CommerceDiscountValidatorResult(true);
		}

		int commerceDiscountCommerceAccountGroupRels =
			_commerceDiscountCommerceAccountGroupRelLocalService.
				getCommerceDiscountCommerceAccountGroupRelsCount(
					commerceDiscount.getCommerceDiscountId(),
					commerceContext.getCommerceAccountGroupIds());

		if (commerceDiscountCommerceAccountGroupRels != 0) {
			return new CommerceDiscountValidatorResult(true);
		}

		CommerceChannelRel commerceChannelRel =
			_commerceChannelRelLocalService.fetchCommerceChannelRel(
				CommerceDiscount.class.getName(),
				commerceDiscount.getCommerceDiscountId(),
				commerceContext.getCommerceChannelId());

		if (commerceChannelRel != null) {
			return new CommerceDiscountValidatorResult(true);
		}

		return new CommerceDiscountValidatorResult(
			commerceDiscount.getCommerceDiscountId(), false,
			"the-discount-is-not-valid");
	}

	private boolean _isUnqualifiedCommerceDiscount(long commerceDiscountId) {
		int commerceDiscountAccountRelsCount =
			_commerceDiscountAccountRelLocalService.
				getCommerceDiscountAccountRelsCount(
					commerceDiscountId, StringPool.BLANK);

		if (commerceDiscountAccountRelsCount > 0) {
			return false;
		}

		int commerceDiscountCommerceAccountGroupRelsCount =
			_commerceDiscountCommerceAccountGroupRelLocalService.
				getCommerceDiscountCommerceAccountGroupRelsCount(
					commerceDiscountId, StringPool.BLANK);

		if (commerceDiscountCommerceAccountGroupRelsCount > 0) {
			return false;
		}

		int commerceChannelRelsCount =
			_commerceChannelRelLocalService.getCommerceChannelRelsCount(
				CommerceDiscount.class.getName(), commerceDiscountId,
				StringPool.BLANK);

		if (commerceChannelRelsCount > 0) {
			return false;
		}

		return true;
	}

	@Reference
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@Reference
	private CommerceDiscountAccountRelLocalService
		_commerceDiscountAccountRelLocalService;

	@Reference
	private CommerceDiscountCommerceAccountGroupRelLocalService
		_commerceDiscountCommerceAccountGroupRelLocalService;

}
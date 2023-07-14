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

import {isObject} from 'frontend-js-web/index';

const getInitialProductOptionValue = (productOption) => {
	if (!productOption.skuContributor || !productOption.productOptionValues) {
		return;
	}

	const selectedProductOptionValue = productOption.productOptionValues.find(
		(productOptionValue) => productOptionValue.preselected === true
	);

	if (selectedProductOptionValue) {
		return selectedProductOptionValue;
	}

	return productOption.productOptionValues[0];
};

const getName = (
	key,
	name,
	selectedProductOptionValueKey,
	skuId,
	relativePriceFormatted
) => {
	if (isObject(name)) {
		name = name[Liferay.ThemeDisplay.getLanguageId()];

		if (!name) {
			name = name[Liferay.ThemeDisplay.getDefaultLanguageId()];
		}
	}

	if (
		selectedProductOptionValueKey === key ||
		!skuId ||
		!relativePriceFormatted
	) {
		return name;
	}

	return name + relativePriceFormatted;
};

const getProductOptionName = (name) => {
	if (isObject(name)) {
		name = name[Liferay.ThemeDisplay.getLanguageId()];

		if (!name) {
			name = name[Liferay.ThemeDisplay.getDefaultLanguageId()];
		}
	}

	return name;
};

const getSkuOptionsErrors = (hasErrors, productOption, skuOptionsAtomState) =>
	hasErrors
		? [
				...skuOptionsAtomState.errors,
				{
					hasErrors: true,
					id: productOption.id,
				},
		  ]
		: skuOptionsAtomState.errors.filter(
				(error) => error.id !== productOption.id
		  );

const initialSkuOptionsAtomState = {
	errors: [],
	namespace: '',
	skuOptions: [],
	updating: false,
};

const isRequired = (forceRequired, isAdmin, productOption) =>
	isAdmin
		? forceRequired
		: forceRequired ||
		  productOption.required ||
		  productOption.skuContributor;

export {
	getInitialProductOptionValue,
	getName,
	getProductOptionName,
	getSkuOptionsErrors,
	initialSkuOptionsAtomState,
	isRequired,
};

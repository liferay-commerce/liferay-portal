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

import ClayForm, {ClayCheckbox} from '@clayui/form';
import {useLiferayState} from '@liferay/frontend-js-state-web';
import classnames from 'classnames';
import skuOptionsAtom from 'commerce-frontend-js/utilities/atoms/skuOptionsAtom';
import React, {useEffect, useState} from 'react';

import Asterisk from './Asterisk';
import {
	getProductOptionName,
	getSkuOptionsErrors,
	initialSkuOptionsAtomState,
	isRequired,
} from './utils';

const ProductOptionCheckboxMultiple = ({
	forceRequired,
	namespace,
	productOption,
}) => {
	const [hasErrors, setHasErrors] = useState(false);
	const [skuOptionsAtomState, setSkuOptionsAtomState] = useLiferayState(
		skuOptionsAtom
	);

	const [productOptionValues, setProductOptionValues] = useState(
		productOption.productOptionValues
	);

	useEffect(
		() =>
			setSkuOptionsAtomState({
				...skuOptionsAtomState,
				errors: getSkuOptionsErrors(
					hasErrors,
					productOption,
					skuOptionsAtomState
				),
				namespace,
			}),
		[hasErrors]
	);

	useEffect(() => {
		let hasPreselected = false;
		let initialSkuOptions = skuOptionsAtomState.skuOptions;

		setProductOptionValues(
			productOptionValues.map((productOptionValue) => {
				if (productOptionValue.preselected) {
					hasPreselected = true;

					initialSkuOptions = [
						...skuOptionsAtomState.skuOptions,
						{
							key: productOption.key,
							skuOptionKey: productOption.key,
							value: [productOptionValue.key],
						},
					];
				}

				return {
					...productOptionValue,
					selected: productOptionValue.preselected,
				};
			})
		);

		const required = productOption.required && !hasPreselected;

		if (required) {
			setHasErrors(true);
		}

		setSkuOptionsAtomState({
			...skuOptionsAtomState,
			errors: getSkuOptionsErrors(
				required,
				productOption,
				skuOptionsAtomState
			),
			namespace,
			skuOptions: initialSkuOptions,
		});

		return () => setSkuOptionsAtomState(initialSkuOptionsAtomState);
	}, []);

	const handleChange = ({target: {checked, value}}) => {
		if (skuOptionsAtomState.updating) {
			return;
		}

		setSkuOptionsAtomState({...skuOptionsAtomState, updating: true});

		let currentSkuOptions = skuOptionsAtomState.skuOptions;

		const curSkuOptionIndex = currentSkuOptions.findIndex(
			(skuOption) => skuOption.skuOptionKey === productOption.key
		);

		const currentSkuOption = currentSkuOptions.filter(
			(skuOption) => skuOption.skuOptionKey === productOption.key
		)[0];

		if (currentSkuOption) {
			currentSkuOptions[curSkuOptionIndex] = {
				key: productOption.key,
				skuOptionKey: productOption.key,
				value: checked
					? [...currentSkuOptions[curSkuOptionIndex].value, value]
					: currentSkuOptions[curSkuOptionIndex].value.filter(
							(curVal) => !(curVal === value)
					  ),
			};
		}
		else {
			currentSkuOptions = [
				...currentSkuOptions,
				{
					key: productOption.key,
					skuOptionKey: productOption.key,
					value: [value],
				},
			];
		}

		const curProductOptionValueIndex = productOptionValues.findIndex(
			(productOptionValue) => productOptionValue.key === value
		);

		productOptionValues[curProductOptionValueIndex] = {
			...productOptionValues[curProductOptionValueIndex],
			selected: checked,
		};

		setProductOptionValues(productOptionValues);

		const required =
			productOption.required &&
			currentSkuOptions[curSkuOptionIndex]?.value.length === 0;

		setHasErrors(required);
		setSkuOptionsAtomState({
			...skuOptionsAtomState,
			errors: getSkuOptionsErrors(
				required,
				productOption,
				skuOptionsAtomState
			),
			skuOptions: currentSkuOptions,
			updating: false,
		});
	};

	return (
		<ClayForm.Group className={classnames({'has-error': hasErrors})}>
			<label>
				{getProductOptionName(productOption.name)}

				<Asterisk
					required={isRequired(
						forceRequired,
						skuOptionsAtomState.isAdmin,
						productOption
					)}
				/>
			</label>

			{productOptionValues.map(({key, name, selected}) => (
				<ClayCheckbox
					checked={selected}
					key={key}
					label={name}
					name={key}
					onChange={handleChange}
					value={key}
				/>
			))}

			{hasErrors && (
				<ClayForm.FeedbackItem>
					<ClayForm.FeedbackIndicator symbol="exclamation-full" />

					{Liferay.Language.get('this-field-is-required')}
				</ClayForm.FeedbackItem>
			)}
		</ClayForm.Group>
	);
};

export default ProductOptionCheckboxMultiple;

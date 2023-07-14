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

const ProductOptionCheckbox = ({
	componentId,
	forceRequired,
	namespace,
	productOption,
}) => {
	const [hasErrors, setHasErrors] = useState(false);
	const [isChecked, setIsChecked] = useState(false);

	const [skuOptionsAtomState, setSkuOptionsAtomState] = useLiferayState(
		skuOptionsAtom
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
			}),
		[hasErrors]
	);

	useEffect(() => {
		setSkuOptionsAtomState({...skuOptionsAtomState, namespace});

		if (productOption.required) {
			setHasErrors(true);
		}

		setSkuOptionsAtomState({
			...skuOptionsAtomState,
			errors: getSkuOptionsErrors(
				productOption.required,
				productOption,
				skuOptionsAtomState
			),
			namespace,
		});

		return () => setSkuOptionsAtomState(initialSkuOptionsAtomState);
	}, []);

	const handleChange = ({target: {checked, value}}) => {
		if (skuOptionsAtomState.updating) {
			return;
		}

		setSkuOptionsAtomState({...skuOptionsAtomState, updating: true});

		setIsChecked(checked);

		let currentSkuOptions = skuOptionsAtomState.skuOptions;

		const currentSkuOption = currentSkuOptions.filter(
			(skuOption) => skuOption.skuOptionKey === productOption.key
		)[0];

		if (currentSkuOption) {
			const curIndex = currentSkuOptions.findIndex(
				(skuOption) => skuOption.skuOptionKey === productOption.key
			);

			currentSkuOptions[curIndex] = {
				key: productOption.key,
				skuOptionKey: productOption.key,
				value: checked ? [value] : [],
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

		const required = productOption.required && !checked;

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
			<label htmlFor={componentId}>
				{getProductOptionName(productOption.name)}

				<Asterisk
					required={isRequired(
						forceRequired,
						skuOptionsAtomState.isAdmin,
						productOption
					)}
				/>
			</label>

			<ClayCheckbox
				checked={isChecked}
				disabled={skuOptionsAtomState.updating}
				id={componentId}
				name={productOption.key}
				onChange={handleChange}
				value={productOption.key}
			/>

			{hasErrors && (
				<ClayForm.FeedbackItem>
					<ClayForm.FeedbackIndicator symbol="exclamation-full" />

					{Liferay.Language.get('this-field-is-required')}
				</ClayForm.FeedbackItem>
			)}
		</ClayForm.Group>
	);
};

export default ProductOptionCheckbox;

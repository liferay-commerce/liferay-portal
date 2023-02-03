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

import ClayDatePicker from '@clayui/date-picker';
import ClayForm, {
	ClayCheckbox,
	ClayInput,
	ClayRadio,
	ClayRadioGroup,
	ClaySelect,
} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {fetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

const CHANNEL_RESOURCE_ENDPOINT =
	'/o/headless-commerce-delivery-catalog/v1.0/channels';

const PRODUCT_OPTIONS_RESOURCE_ENDPOINT =
	'/o/headless-commerce-admin-catalog/v1.0/products';

const PRODUCT_OPTION_TYPES = {
	CHECKBOX: 'checkbox',
	CHECKBOX_MULTIPLE: 'checkbox_multiple',
	DATE: 'date',
	NUMERIC: 'numeric',
	RADIO: 'radio',
	SELECT: 'select',
	TEXT: 'text',
};

export default function ProductOptionsHandler({commerceContext, product}) {
	const [currentProductOptions, setCurrentProductOptions] = useState({});

	const {commerceChannelId} = commerceContext;
	const productId = product.CProductId;

	useEffect(() => {
		const productOptionsApiURL = new URL(
			`${themeDisplay.getPathContext()}${PRODUCT_OPTIONS_RESOURCE_ENDPOINT}/${productId}/productOptions`,
			themeDisplay.getPortalURL()
		);

		const channelProductOptionsApiURL = new URL(
			`${themeDisplay.getPathContext()}${CHANNEL_RESOURCE_ENDPOINT}/${commerceChannelId}/products/${productId}/product-options`,
			themeDisplay.getPortalURL()
		);

		fetch(productOptionsApiURL.toString())
			.then((productOptionsResponse) => productOptionsResponse.json())
			.then((productOptions) => {
				const firstHalf = productOptions.items;

				fetch(channelProductOptionsApiURL.toString())
					.then((productOptionsResponse) =>
						productOptionsResponse.json()
					)
					.then((channelProductOptions) => {
						const productWithOptions = firstHalf.map((item) => {
							const {
								productOptionValues,
							} = channelProductOptions.items.find(
								(two) => two.id === item.id
							);

							return {...item, productOptionValues};
						});

						setCurrentProductOptions(productWithOptions);
					});
			});
	}, [commerceChannelId, productId]);

	return (
		<form className="px-3">
			{!!currentProductOptions.length &&
				currentProductOptions.map((option) => {
					const {
						fieldType,
						id,
						key,
						name,
						productOptionValues,
						required,
					} = option;

					return (
						<ClayForm.Group key={id}>
							<label htmlFor={key}>
								{name[themeDisplay.getLanguageId()]}

								{required && (
									<span className="inline-item inline-item-after reference-mark">
										<ClayIcon symbol="asterisk" />

										<span className="hide-accessible sr-only">
											{Liferay.Language.get('required')}
										</span>
									</span>
								)}
							</label>

							{fieldType === PRODUCT_OPTION_TYPES.CHECKBOX && (
								<ClayCheckbox id={key} required={required} />
							)}

							{fieldType ===
								PRODUCT_OPTION_TYPES.CHECKBOX_MULTIPLE &&
								productOptionValues.map((optionValue) => (
									<ClayCheckbox
										id={key}
										key={optionValue.id}
										label={optionValue.name}
										value={optionValue.key}
									/>
								))}

							{fieldType === PRODUCT_OPTION_TYPES.DATE && (
								<ClayDatePicker id={key} />
							)}

							{fieldType === PRODUCT_OPTION_TYPES.NUMERIC && (
								<ClayInput id={key} />
							)}

							{fieldType === PRODUCT_OPTION_TYPES.RADIO && (
								<ClayRadioGroup id={key} inline>
									{productOptionValues.map((optionValue) => (
										<ClayRadio
											key={optionValue.id}
											label={optionValue.name}
											value={optionValue.key}
										/>
									))}
								</ClayRadioGroup>
							)}

							{fieldType === PRODUCT_OPTION_TYPES.SELECT && (
								<ClaySelect aria-label="Select Label" id={key}>
									{productOptionValues.map((optionValue) => (
										<ClaySelect.Option
											key={optionValue.id}
											label={optionValue.name}
											value={optionValue.key}
										/>
									))}
								</ClaySelect>
							)}

							{fieldType === PRODUCT_OPTION_TYPES.TEXT && (
								<ClayInput />
							)}
						</ClayForm.Group>
					);
				})}
		</form>
	);
}

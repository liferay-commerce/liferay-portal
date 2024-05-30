/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ServiceProvider from '../../ServiceProvider/index';
import {CURRENT_ORDER_UPDATED} from '../../utilities/eventsDefinitions';

const CartResource = ServiceProvider.DeliveryCartAPI('v1');

export function formatCartItem(
	cpInstance,
	namespace,
	skuOptions,
	skuOptionsNamespace
) {
	let optionsJSON = cpInstance.skuOptions || [];

	if (namespace === skuOptionsNamespace) {
		optionsJSON = skuOptions.map((skuOption) => ({
			...skuOption,
			skuId: skuOption.skuId ? String(skuOption.skuId) : null,
		}));
	}
	else if (optionsJSON.length) {
		optionsJSON = optionsJSON.map((optionJSON) => ({
			...optionJSON,
			key: optionJSON.skuOptionKey || optionJSON.key,
			value: optionJSON.skuOptionValueKey || optionJSON.value,
		}));
	}

	if (cpInstance.skuUnitOfMeasure) {
		cpInstance.skuUnitOfMeasure = {
			incrementalOrderQuantity:
				cpInstance.skuUnitOfMeasure.incrementalOrderQuantity,
			key: cpInstance.skuUnitOfMeasure.key,
			precision: cpInstance.skuUnitOfMeasure.precision,
		};
	}

	return {
		options: JSON.stringify(optionsJSON),
		quantity: Number(
			Number(cpInstance.quantity).toFixed(
				cpInstance.skuUnitOfMeasure?.precision || 0
			)
		),
		replacedSkuId: cpInstance.replacedSkuId ?? 0,
		skuId: cpInstance.skuId,
		skuUnitOfMeasure: cpInstance.skuUnitOfMeasure,
	};
}

export async function addToCart(
	cpInstances,
	cartId,
	channel,
	accountId,
	orderTypeId,
	namespace,
	skuOptions,
	skuOptionsNamespace
) {
	if (!cartId) {
		const newCart = await CartResource.createCartByChannelId(channel.id, {
			accountId,
			cartItems: cpInstances.map((cpInstance) =>
				formatCartItem(
					cpInstance,
					namespace,
					skuOptions,
					skuOptionsNamespace
				)
			),
			currencyCode: channel.currencyCode,
			orderTypeId,
		});

		Liferay.fire(CURRENT_ORDER_UPDATED, {order: newCart});

		return newCart;
	}

	for (const cpInstance of cpInstances) {
		await CartResource.createItemByCartId(cartId, formatCartItem(
			cpInstance,
			namespace,
			skuOptions,
			skuOptionsNamespace
		));
	}

	const fetchedCart = await CartResource.getCartByIdWithItems(cartId);

	Liferay.fire(CURRENT_ORDER_UPDATED, {order: fetchedCart});

	return fetchedCart;
}

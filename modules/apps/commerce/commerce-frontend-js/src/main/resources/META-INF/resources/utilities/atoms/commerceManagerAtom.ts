/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {State} from '@liferay/frontend-js-state-web';

import {TCommerceManager} from '../../types/types';

const CommerceContext = Liferay.CommerceContext;

const commerceManager: TCommerceManager = {
	accountGroupIds: CommerceContext?.commerceAccountGroupIds,
	channel: {
		configuration: {
			accountAllowedTypes: CommerceContext?.accountEntryAllowedTypes,
			accountCartMaxAllowed:
				CommerceContext?.configuration?.accountCartMaxAllowed || 0,
			checkoutRequestedDeliveryDateEnabled:
				CommerceContext?.configuration
					?.checkoutRequestedDeliveryDateEnabled || false,
			commerceSiteType: CommerceContext?.commerceSiteType || 0,
			guestCheckoutEnabled:
				CommerceContext?.configuration?.guestCheckoutEnabled || false,
			hideShippingPriceZero:
				CommerceContext?.configuration?.hideShippingPriceZero || false,
			multishippingEnabled:
				CommerceContext?.configuration?.multishippingEnabled || false,
			openOrdersVisibilityScope:
				CommerceContext?.configuration?.openOrdersVisibilityScope ||
				'account',
			orderImporterDateFormat:
				CommerceContext?.configuration?.orderImporterDateFormat ||
				'yyyy-MM-dd',
			orderSelectionDisabled:
				CommerceContext?.configuration?.orderSelectionDisabled || false,
			placedOrdersVisibilityScope:
				CommerceContext?.configuration?.placedOrdersVisibilityScope ||
				'account',
			quickCheckoutEnabled:
				CommerceContext?.configuration?.quickCheckoutEnabled || false,
			requestQuoteEnabled:
				CommerceContext?.configuration?.requestQuoteEnabled || false,
			showPurchaseOrderNumber:
				CommerceContext?.configuration?.showPurchaseOrderNumber || true,
			showSeparateOrderItems:
				CommerceContext?.configuration?.showSeparateOrderItems || false,
			slowConnectionOrderFlowEnabled:
				CommerceContext?.configuration
					?.slowConnectionOrderFlowEnabled || false,
			undoCartItemDeletionDisabled:
				CommerceContext?.configuration?.undoCartItemDeletionDisabled ||
				false,
		},
		...(CommerceContext?.currency &&
			isValidNumber(CommerceContext.currency.currencyId) && {
				currency: {
					code: CommerceContext.currency.currencyCode,
					id: parseInt(CommerceContext.currency.currencyId, 10),
				},
			}),
		groupId: isValidNumber(CommerceContext?.commerceChannelGroupId)
			? parseInt(CommerceContext?.commerceChannelGroupId, 10)
			: 0,
		id: isValidNumber(CommerceContext?.commerceChannelId)
			? parseInt(CommerceContext?.commerceChannelId, 10)
			: 0,
	},
	configuration: {
		showUnselectableOptions:
			CommerceContext?.configuration?.showUnselectableOptions || false,
	},
	...(CommerceContext?.account &&
		isValidNumber(CommerceContext.account.accountId) && {
			currentAccount: {
				id: parseInt(CommerceContext.account.accountId, 10),
				name: CommerceContext.account.accountName,
			},
		}),
	...(CommerceContext?.order &&
		isValidNumber(CommerceContext.order.orderId) && {
			currentOrder: {
				id: parseInt(CommerceContext.order.orderId, 10),
				...(isValidNumber(CommerceContext.order.orderType) && {
					orderType: {
						id: parseInt(CommerceContext.order.orderType, 10),
					},
				}),
			},
		}),
	...(CommerceContext?.orderTypes && {
		orderTypes: CommerceContext.orderTypes.map((orderType) => ({
			id: orderType.orderTypeId,
			label_i18n: orderType.label_i18n,
		})),
	}),
	updating: false,
};

const commerceManagerAtom = State.atom('commerceManagerAtom', commerceManager);

function isValidNumber(value: string) {
	return !Number.isNaN(parseInt(value, 10));
}

export default commerceManagerAtom;

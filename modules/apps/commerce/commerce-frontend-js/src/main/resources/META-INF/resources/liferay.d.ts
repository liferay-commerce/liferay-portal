/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// These come from CommerceFrontendJsDynamicInclude

declare module Liferay {
	export const CommerceContext: {
		account?: CommerceAccount;
		accountEntryAllowedTypes: string[];
		commerceAccountGroupIds: string[];
		commerceChannelGroupId: string;
		commerceChannelId: string;
		commerceSiteType: number;
		configuration?: Configuration;
		currency?: CommerceCurrency;
		order?: CommerceOrder;
		orderTypes?: CommerceOrderType[] | [];
		showSeparateOrderItems?: boolean;
		showUnselectableOptions?: boolean;
	};
}

interface CommerceAccount {
	accountId: string;
	accountName: string;
}

interface CommerceCurrency {
	currencyCode: string;
	currencyId: string;
}

interface CommerceOrder {
	orderId: string;
	orderType: string;
}

interface CommerceOrderType {
	label_i18n: string;
	orderTypeId: number;
}

interface Configuration {
	accountCartMaxAllowed: number;
	checkoutRequestedDeliveryDateEnabled: boolean;
	guestCheckoutEnabled: boolean;
	hideShippingPriceZero: boolean;
	multishippingEnabled: boolean;
	openOrdersVisibilityScope: string;
	orderImporterDateFormat: string;
	orderSelectionDisabled: boolean;
	placedOrdersVisibilityScope: string;
	quickCheckoutEnabled: boolean;
	requestQuoteEnabled: boolean;
	showPurchaseOrderNumber: boolean;
	showSeparateOrderItems: boolean;
	showUnselectableOptions: boolean;
	slowConnectionOrderFlowEnabled: boolean;
	undoCartItemDeletionDisabled: boolean;
}

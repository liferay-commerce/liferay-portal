/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export type TAccount = {
	id: number;
	logoURL?: string;
	name: string;
};

export type TChannel = {
	configuration?: TChannelConfiguration;
	currency?: TCurrency;
	groupId: number;
	id: number;
	name?: string;
	type?: string;
};

export type TChannelConfiguration = {
	accountAllowedTypes: string[];
	accountCartMaxAllowed: number;
	checkoutRequestedDeliveryDateEnabled: boolean;
	commerceSiteType: number;
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
	slowConnectionOrderFlowEnabled: boolean;
	undoCartItemDeletionDisabled: boolean;
};

export type TCommerceManager = {
	accountGroupIds?: string[];
	channel: TChannel;
	configuration: TCommerceManagerConfiguration;
	currentAccount?: TAccount;
	currentOrder?: TOrder;
	orderTypes?: TOrderType[];
	updating: boolean;
};

export type TCommerceManagerConfiguration = {
	showUnselectableOptions: boolean;
};

export type TCurrency = {
	code: string;
	id: number;
};

export type TOrder = {
	id: number;
	orderType?: TOrderType;
	workflowStatusInfo?: {
		code: string;
		label: string;
		label_i18n: string;
	};
};

export type TOrderType = {
	id: number;
	label_i18n?: string;
};

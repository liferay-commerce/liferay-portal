<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
OrderSummaryCheckoutStepDisplayContext orderSummaryCheckoutStepDisplayContext = (OrderSummaryCheckoutStepDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CommerceOrder commerceOrder = orderSummaryCheckoutStepDisplayContext.getCommerceOrder();
%>

<frontend-data-set:headless-display
	apiURL='<%= StringBundler.concat("/o/headless-commerce-delivery-cart/v1.0/carts/", commerceOrder.getCommerceOrderId(), "/items") %>'
	id="com_liferay_commerce_order_content_web_internal_fragment_renderer_OrderItemsDataSetFragmentRenderer-pendingOrderItems"
	nestedItemsKey="id"
	nestedItemsReferenceKey="cartItems"
	selectedItemsKey="id"
/>
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

Map<Long, List<CommerceOrderValidatorResult>> commerceOrderValidatorResultsMap = orderSummaryCheckoutStepDisplayContext.getCommerceOrderValidatorResultsMap();

String languageId = LanguageUtil.getLanguageId(locale);
%>

<c:if test="<%= !commerceOrderValidatorResultsMap.isEmpty() %>">
	<liferay-ui:error exception="<%= CommerceOrderValidatorException.class %>">

		<%
		List<CommerceOrderItem> commerceOrderItems = commerceOrder.getCommerceOrderItems();

		for (CommerceOrderItem commerceOrderItem : commerceOrderItems) {
			List<CommerceOrderValidatorResult> commerceOrderValidatorResults = commerceOrderValidatorResultsMap.get(commerceOrderItem.getCommerceOrderItemId());

			for (CommerceOrderValidatorResult commerceOrderValidatorResult : commerceOrderValidatorResults) {
				StringBundler sb = new StringBundler(4);

				sb.append(HtmlUtil.escape(commerceOrderItem.getName(languageId)));
				sb.append(StringPool.COLON);
				sb.append(StringPool.SPACE);
				sb.append(HtmlUtil.escape(commerceOrderValidatorResult.getLocalizedMessage()));
		%>

				<div class="alert-danger commerce-alert-danger">
					<span><liferay-ui:message key="<%= sb.toString() %>" /></span>
				</div>

		<%
			}
		}
		%>

	</liferay-ui:error>
</c:if>

<div class="commerce-checkout-summary-body" id="<portlet:namespace />entriesContainer">
	<frontend-data-set:headless-display
		apiURL='<%= StringBundler.concat("/o/headless-commerce-delivery-cart/v1.0/carts/", commerceOrder.getCommerceOrderId(), "/items") %>'
		id="com_liferay_commerce_order_content_web_internal_fragment_renderer_OrderItemsDataSetFragmentRenderer-pendingOrderItems"
		nestedItemsKey="id"
		nestedItemsReferenceKey="cartItems"
		selectedItemsKey="id"
	/>
</div>
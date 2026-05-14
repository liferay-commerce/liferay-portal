<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/fragment/renderer/order_items_data_set/init.jsp" %>

<c:if test="<%= !commerceOrderValidatorResultsMap.isEmpty() %>">
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

	<liferay-ui:error>
		<div class="alert-danger commerce-alert-danger">
			<span><liferay-ui:message key="<%= sb.toString() %>" /></span>
		</div>
	</liferay-ui:error>

	<%
		}
	}
	%>
</c:if>

<frontend-data-set:headless-display
	additionalProps="<%= additionalProps %>"
	apiURL="<%= apiURL %>"
	bulkActionDropdownItems="<%= bulkActionDropdownItems %>"
	fdsActionDropdownItems="<%= fdsActionDropdownItems %>"
	id="<%= name %>"
	nestedItemsKey="id"
	nestedItemsReferenceKey='<%= name.equals(CommerceOrderFragmentFDSNames.PENDING_ORDER_ITEMS) ? "cartItems" : "placedOrderItems" %>'
	propsTransformer="<%= propsTransformer %>"
	selectedItemsKey="id"
	selectionType="multiple"
	style="<%= displayStyle %>"
/>
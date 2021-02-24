<%--
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
--%>

<%@ include file="/add_to_order/init.jsp" %>

<div class="add-to-cart mb-2" id="<%= addToCartId %>">
	<liferay-util:include page="/add_to_order/skeleton.jsp" servletContext="<%= application %>" />

	<react:component
		module="add_to_order/AddToOrder"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"channel", HashMapBuilder.<String, Object>put(
					"currencyCode", currencyCode
				).put(
					"id", channelId
				).build()
			).put(
				"cpInstance", HashMapBuilder.<String, Object>put(
					"accountId", commerceAccountId
				).put(
					"inCart", inCart
				).put(
					"options", options
				).put(
					"skuId", skuId
				).put(
					"stockQuantity", stockQuantity
				).build()
			).put(
				"orderId", orderId
			).put(
				"settings", HashMapBuilder.<String, Object>put(
					"block", block
				).put(
					"disabled", disabled
				).put(
					"willUpdate", willUpdate
				).put(
					"withQuantity", HashMapBuilder.<String, Object>put(
						"forceDropdown", true
					).build()
				).build()
			).put(
				"spritemap", spritemap
			).build()
		%>'
	/>
</div>
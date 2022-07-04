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

package com.liferay.headless.commerce.delivery.order.internal.graphql.query.v1_0;

import com.liferay.headless.commerce.delivery.order.dto.v1_0.Address;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.OrderComment;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.OrderItem;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.PaymentMethod;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.ShippingMethod;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.AddressResource;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderCommentResource;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderItemResource;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderResource;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.PaymentMethodResource;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.ShippingMethodResource;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLTypeExtension;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.Generated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.ComponentServiceObjects;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public class Query {

	public static void setAddressResourceComponentServiceObjects(
		ComponentServiceObjects<AddressResource>
			addressResourceComponentServiceObjects) {

		_addressResourceComponentServiceObjects =
			addressResourceComponentServiceObjects;
	}

	public static void setOrderResourceComponentServiceObjects(
		ComponentServiceObjects<OrderResource>
			orderResourceComponentServiceObjects) {

		_orderResourceComponentServiceObjects =
			orderResourceComponentServiceObjects;
	}

	public static void setOrderCommentResourceComponentServiceObjects(
		ComponentServiceObjects<OrderCommentResource>
			orderCommentResourceComponentServiceObjects) {

		_orderCommentResourceComponentServiceObjects =
			orderCommentResourceComponentServiceObjects;
	}

	public static void setOrderItemResourceComponentServiceObjects(
		ComponentServiceObjects<OrderItemResource>
			orderItemResourceComponentServiceObjects) {

		_orderItemResourceComponentServiceObjects =
			orderItemResourceComponentServiceObjects;
	}

	public static void setPaymentMethodResourceComponentServiceObjects(
		ComponentServiceObjects<PaymentMethodResource>
			paymentMethodResourceComponentServiceObjects) {

		_paymentMethodResourceComponentServiceObjects =
			paymentMethodResourceComponentServiceObjects;
	}

	public static void setShippingMethodResourceComponentServiceObjects(
		ComponentServiceObjects<ShippingMethodResource>
			shippingMethodResourceComponentServiceObjects) {

		_shippingMethodResourceComponentServiceObjects =
			shippingMethodResourceComponentServiceObjects;
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderBillingAddres(orderId: ___){city, country, countryISOCode, description, id, latitude, longitude, name, phoneNumber, region, regionISOCode, street1, street2, street3, type, typeId, vatNumber, zip}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(description = "Retrive order billing address.")
	public Address orderBillingAddres(@GraphQLName("orderId") Long orderId)
		throws Exception {

		return _applyComponentServiceObjects(
			_addressResourceComponentServiceObjects,
			this::_populateResourceContext,
			addressResource -> addressResource.getOrderBillingAddres(orderId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderShippingAddres(orderId: ___){city, country, countryISOCode, description, id, latitude, longitude, name, phoneNumber, region, regionISOCode, street1, street2, street3, type, typeId, vatNumber, zip}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(description = "Retrive order billing address.")
	public Address orderShippingAddres(@GraphQLName("orderId") Long orderId)
		throws Exception {

		return _applyComponentServiceObjects(
			_addressResourceComponentServiceObjects,
			this::_populateResourceContext,
			addressResource -> addressResource.getOrderShippingAddres(orderId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {channelAccountOrders(accountId: ___, channelId: ___, page: ___, pageSize: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrieves orders for specific account in the given channel."
	)
	public OrderPage channelAccountOrders(
			@GraphQLName("accountId") Long accountId,
			@GraphQLName("channelId") Long channelId,
			@GraphQLName("pageSize") int pageSize,
			@GraphQLName("page") int page)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderResource -> new OrderPage(
				orderResource.getChannelAccountOrdersPage(
					accountId, channelId, Pagination.of(page, pageSize))));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {order(orderId: ___){account, accountId, author, billingAddress, billingAddressId, channelId, couponCode, createDate, currencyCode, customFields, errorMessages, id, lastPriceUpdateDate, modifiedDate, notes, orderItems, orderStatusInfo, orderTypeExternalReferenceCode, orderTypeId, orderUUID, paymentMethod, paymentMethodLabel, paymentStatus, paymentStatusInfo, paymentStatusLabel, printedNote, purchaseOrderNumber, shippingAddress, shippingAddressId, shippingMethod, shippingOption, status, summary, useAsBilling, valid, workflowStatusInfo}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(description = "Retrive information of the given Order.")
	public Order order(@GraphQLName("orderId") Long orderId) throws Exception {
		return _applyComponentServiceObjects(
			_orderResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderResource -> orderResource.getOrder(orderId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderPaymentUrl(callbackURL: ___, orderId: ___){}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField
	public String orderPaymentUrl(
			@GraphQLName("orderId") Long orderId,
			@GraphQLName("callbackURL") String callbackURL)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderResource -> orderResource.getOrderPaymentUrl(
				orderId, callbackURL));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderComment(orderCommentId: ___){author, content, id, orderId, restricted}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField
	public OrderComment orderComment(
			@GraphQLName("orderCommentId") Long orderCommentId)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderCommentResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderCommentResource -> orderCommentResource.getOrderComment(
				orderCommentId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderComments(orderId: ___, page: ___, pageSize: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField
	public OrderCommentPage orderComments(
			@GraphQLName("orderId") Long orderId,
			@GraphQLName("pageSize") int pageSize,
			@GraphQLName("page") int page)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderCommentResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderCommentResource -> new OrderCommentPage(
				orderCommentResource.getOrderCommentsPage(
					orderId, Pagination.of(page, pageSize))));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderItem(orderItemId: ___){adaptiveMediaImageHTMLTag, customFields, errorMessages, id, name, options, orderItems, parentOrderItemId, price, productId, productURLs, quantity, settings, sku, skuId, subscription, thumbnail, valid}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(description = "Retrive information of the given Order")
	public OrderItem orderItem(@GraphQLName("orderItemId") Long orderItemId)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderItemResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderItemResource -> orderItemResource.getOrderItem(orderItemId));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderItems(orderId: ___, page: ___, pageSize: ___, skuId: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(description = "Retrive order items of a Order.")
	public OrderItemPage orderItems(
			@GraphQLName("orderId") Long orderId,
			@GraphQLName("skuId") Long skuId,
			@GraphQLName("pageSize") int pageSize,
			@GraphQLName("page") int page)
		throws Exception {

		return _applyComponentServiceObjects(
			_orderItemResourceComponentServiceObjects,
			this::_populateResourceContext,
			orderItemResource -> new OrderItemPage(
				orderItemResource.getOrderItemsPage(
					orderId, skuId, Pagination.of(page, pageSize))));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderPaymentMethods(orderId: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrive payment methods available for the Order."
	)
	public PaymentMethodPage orderPaymentMethods(
			@GraphQLName("orderId") Long orderId)
		throws Exception {

		return _applyComponentServiceObjects(
			_paymentMethodResourceComponentServiceObjects,
			this::_populateResourceContext,
			paymentMethodResource -> new PaymentMethodPage(
				paymentMethodResource.getOrderPaymentMethodsPage(orderId)));
	}

	/**
	 * Invoke this method with the command line:
	 *
	 * curl -H 'Content-Type: text/plain; charset=utf-8' -X 'POST' 'http://localhost:8080/o/graphql' -d $'{"query": "query {orderShippingMethods(orderId: ___){items {__}, page, pageSize, totalCount}}"}' -u 'test@liferay.com:test'
	 */
	@GraphQLField(
		description = "Retrive payment methods available for the Order."
	)
	public ShippingMethodPage orderShippingMethods(
			@GraphQLName("orderId") Long orderId)
		throws Exception {

		return _applyComponentServiceObjects(
			_shippingMethodResourceComponentServiceObjects,
			this::_populateResourceContext,
			shippingMethodResource -> new ShippingMethodPage(
				shippingMethodResource.getOrderShippingMethodsPage(orderId)));
	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderShippingAddresTypeExtension {

		public GetOrderShippingAddresTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField(description = "Retrive order billing address.")
		public Address shippingAddres() throws Exception {
			return _applyComponentServiceObjects(
				_addressResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				addressResource -> addressResource.getOrderShippingAddres(
					_order.getId()));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(OrderComment.class)
	public class GetOrderTypeExtension {

		public GetOrderTypeExtension(OrderComment orderComment) {
			_orderComment = orderComment;
		}

		@GraphQLField(description = "Retrive information of the given Order.")
		public Order order() throws Exception {
			return _applyComponentServiceObjects(
				_orderResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				orderResource -> orderResource.getOrder(
					_orderComment.getOrderId()));
		}

		private OrderComment _orderComment;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderPaymentUrlTypeExtension {

		public GetOrderPaymentUrlTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField
		public String paymentUrl(@GraphQLName("callbackURL") String callbackURL)
			throws Exception {

			return _applyComponentServiceObjects(
				_orderResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				orderResource -> orderResource.getOrderPaymentUrl(
					_order.getId(), callbackURL));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderCommentsPageTypeExtension {

		public GetOrderCommentsPageTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField
		public OrderCommentPage comments(
				@GraphQLName("pageSize") int pageSize,
				@GraphQLName("page") int page)
			throws Exception {

			return _applyComponentServiceObjects(
				_orderCommentResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				orderCommentResource -> new OrderCommentPage(
					orderCommentResource.getOrderCommentsPage(
						_order.getId(), Pagination.of(page, pageSize))));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderPaymentMethodsPageTypeExtension {

		public GetOrderPaymentMethodsPageTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField(
			description = "Retrive payment methods available for the Order."
		)
		public PaymentMethodPage paymentMethods() throws Exception {
			return _applyComponentServiceObjects(
				_paymentMethodResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				paymentMethodResource -> new PaymentMethodPage(
					paymentMethodResource.getOrderPaymentMethodsPage(
						_order.getId())));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderItemsPageTypeExtension {

		public GetOrderItemsPageTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField(description = "Retrive order items of a Order.")
		public OrderItemPage items(
				@GraphQLName("skuId") Long skuId,
				@GraphQLName("pageSize") int pageSize,
				@GraphQLName("page") int page)
			throws Exception {

			return _applyComponentServiceObjects(
				_orderItemResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				orderItemResource -> new OrderItemPage(
					orderItemResource.getOrderItemsPage(
						_order.getId(), skuId, Pagination.of(page, pageSize))));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderBillingAddresTypeExtension {

		public GetOrderBillingAddresTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField(description = "Retrive order billing address.")
		public Address billingAddres() throws Exception {
			return _applyComponentServiceObjects(
				_addressResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				addressResource -> addressResource.getOrderBillingAddres(
					_order.getId()));
		}

		private Order _order;

	}

	@GraphQLTypeExtension(Order.class)
	public class GetOrderShippingMethodsPageTypeExtension {

		public GetOrderShippingMethodsPageTypeExtension(Order order) {
			_order = order;
		}

		@GraphQLField(
			description = "Retrive payment methods available for the Order."
		)
		public ShippingMethodPage shippingMethods() throws Exception {
			return _applyComponentServiceObjects(
				_shippingMethodResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				shippingMethodResource -> new ShippingMethodPage(
					shippingMethodResource.getOrderShippingMethodsPage(
						_order.getId())));
		}

		private Order _order;

	}

	@GraphQLName("AddressPage")
	public class AddressPage {

		public AddressPage(Page addressPage) {
			actions = addressPage.getActions();

			items = addressPage.getItems();
			lastPage = addressPage.getLastPage();
			page = addressPage.getPage();
			pageSize = addressPage.getPageSize();
			totalCount = addressPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<Address> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLName("OrderPage")
	public class OrderPage {

		public OrderPage(Page orderPage) {
			actions = orderPage.getActions();

			items = orderPage.getItems();
			lastPage = orderPage.getLastPage();
			page = orderPage.getPage();
			pageSize = orderPage.getPageSize();
			totalCount = orderPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<Order> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLName("OrderCommentPage")
	public class OrderCommentPage {

		public OrderCommentPage(Page orderCommentPage) {
			actions = orderCommentPage.getActions();

			items = orderCommentPage.getItems();
			lastPage = orderCommentPage.getLastPage();
			page = orderCommentPage.getPage();
			pageSize = orderCommentPage.getPageSize();
			totalCount = orderCommentPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<OrderComment> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLName("OrderItemPage")
	public class OrderItemPage {

		public OrderItemPage(Page orderItemPage) {
			actions = orderItemPage.getActions();

			items = orderItemPage.getItems();
			lastPage = orderItemPage.getLastPage();
			page = orderItemPage.getPage();
			pageSize = orderItemPage.getPageSize();
			totalCount = orderItemPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<OrderItem> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLName("PaymentMethodPage")
	public class PaymentMethodPage {

		public PaymentMethodPage(Page paymentMethodPage) {
			actions = paymentMethodPage.getActions();

			items = paymentMethodPage.getItems();
			lastPage = paymentMethodPage.getLastPage();
			page = paymentMethodPage.getPage();
			pageSize = paymentMethodPage.getPageSize();
			totalCount = paymentMethodPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<PaymentMethod> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLName("ShippingMethodPage")
	public class ShippingMethodPage {

		public ShippingMethodPage(Page shippingMethodPage) {
			actions = shippingMethodPage.getActions();

			items = shippingMethodPage.getItems();
			lastPage = shippingMethodPage.getLastPage();
			page = shippingMethodPage.getPage();
			pageSize = shippingMethodPage.getPageSize();
			totalCount = shippingMethodPage.getTotalCount();
		}

		@GraphQLField
		protected Map<String, Map> actions;

		@GraphQLField
		protected java.util.Collection<ShippingMethod> items;

		@GraphQLField
		protected long lastPage;

		@GraphQLField
		protected long page;

		@GraphQLField
		protected long pageSize;

		@GraphQLField
		protected long totalCount;

	}

	@GraphQLTypeExtension(OrderItem.class)
	public class ParentOrderItemOrderItemIdTypeExtension {

		public ParentOrderItemOrderItemIdTypeExtension(OrderItem orderItem) {
			_orderItem = orderItem;
		}

		@GraphQLField(description = "Retrive information of the given Order")
		public OrderItem parentOrderItem() throws Exception {
			if (_orderItem.getParentOrderItemId() == null) {
				return null;
			}

			return _applyComponentServiceObjects(
				_orderItemResourceComponentServiceObjects,
				Query.this::_populateResourceContext,
				orderItemResource -> orderItemResource.getOrderItem(
					_orderItem.getParentOrderItemId()));
		}

		private OrderItem _orderItem;

	}

	private <T, R, E1 extends Throwable, E2 extends Throwable> R
			_applyComponentServiceObjects(
				ComponentServiceObjects<T> componentServiceObjects,
				UnsafeConsumer<T, E1> unsafeConsumer,
				UnsafeFunction<T, R, E2> unsafeFunction)
		throws E1, E2 {

		T resource = componentServiceObjects.getService();

		try {
			unsafeConsumer.accept(resource);

			return unsafeFunction.apply(resource);
		}
		finally {
			componentServiceObjects.ungetService(resource);
		}
	}

	private void _populateResourceContext(AddressResource addressResource)
		throws Exception {

		addressResource.setContextAcceptLanguage(_acceptLanguage);
		addressResource.setContextCompany(_company);
		addressResource.setContextHttpServletRequest(_httpServletRequest);
		addressResource.setContextHttpServletResponse(_httpServletResponse);
		addressResource.setContextUriInfo(_uriInfo);
		addressResource.setContextUser(_user);
		addressResource.setGroupLocalService(_groupLocalService);
		addressResource.setRoleLocalService(_roleLocalService);
	}

	private void _populateResourceContext(OrderResource orderResource)
		throws Exception {

		orderResource.setContextAcceptLanguage(_acceptLanguage);
		orderResource.setContextCompany(_company);
		orderResource.setContextHttpServletRequest(_httpServletRequest);
		orderResource.setContextHttpServletResponse(_httpServletResponse);
		orderResource.setContextUriInfo(_uriInfo);
		orderResource.setContextUser(_user);
		orderResource.setGroupLocalService(_groupLocalService);
		orderResource.setRoleLocalService(_roleLocalService);
	}

	private void _populateResourceContext(
			OrderCommentResource orderCommentResource)
		throws Exception {

		orderCommentResource.setContextAcceptLanguage(_acceptLanguage);
		orderCommentResource.setContextCompany(_company);
		orderCommentResource.setContextHttpServletRequest(_httpServletRequest);
		orderCommentResource.setContextHttpServletResponse(
			_httpServletResponse);
		orderCommentResource.setContextUriInfo(_uriInfo);
		orderCommentResource.setContextUser(_user);
		orderCommentResource.setGroupLocalService(_groupLocalService);
		orderCommentResource.setRoleLocalService(_roleLocalService);
	}

	private void _populateResourceContext(OrderItemResource orderItemResource)
		throws Exception {

		orderItemResource.setContextAcceptLanguage(_acceptLanguage);
		orderItemResource.setContextCompany(_company);
		orderItemResource.setContextHttpServletRequest(_httpServletRequest);
		orderItemResource.setContextHttpServletResponse(_httpServletResponse);
		orderItemResource.setContextUriInfo(_uriInfo);
		orderItemResource.setContextUser(_user);
		orderItemResource.setGroupLocalService(_groupLocalService);
		orderItemResource.setRoleLocalService(_roleLocalService);
	}

	private void _populateResourceContext(
			PaymentMethodResource paymentMethodResource)
		throws Exception {

		paymentMethodResource.setContextAcceptLanguage(_acceptLanguage);
		paymentMethodResource.setContextCompany(_company);
		paymentMethodResource.setContextHttpServletRequest(_httpServletRequest);
		paymentMethodResource.setContextHttpServletResponse(
			_httpServletResponse);
		paymentMethodResource.setContextUriInfo(_uriInfo);
		paymentMethodResource.setContextUser(_user);
		paymentMethodResource.setGroupLocalService(_groupLocalService);
		paymentMethodResource.setRoleLocalService(_roleLocalService);
	}

	private void _populateResourceContext(
			ShippingMethodResource shippingMethodResource)
		throws Exception {

		shippingMethodResource.setContextAcceptLanguage(_acceptLanguage);
		shippingMethodResource.setContextCompany(_company);
		shippingMethodResource.setContextHttpServletRequest(
			_httpServletRequest);
		shippingMethodResource.setContextHttpServletResponse(
			_httpServletResponse);
		shippingMethodResource.setContextUriInfo(_uriInfo);
		shippingMethodResource.setContextUser(_user);
		shippingMethodResource.setGroupLocalService(_groupLocalService);
		shippingMethodResource.setRoleLocalService(_roleLocalService);
	}

	private static ComponentServiceObjects<AddressResource>
		_addressResourceComponentServiceObjects;
	private static ComponentServiceObjects<OrderResource>
		_orderResourceComponentServiceObjects;
	private static ComponentServiceObjects<OrderCommentResource>
		_orderCommentResourceComponentServiceObjects;
	private static ComponentServiceObjects<OrderItemResource>
		_orderItemResourceComponentServiceObjects;
	private static ComponentServiceObjects<PaymentMethodResource>
		_paymentMethodResourceComponentServiceObjects;
	private static ComponentServiceObjects<ShippingMethodResource>
		_shippingMethodResourceComponentServiceObjects;

	private AcceptLanguage _acceptLanguage;
	private com.liferay.portal.kernel.model.Company _company;
	private BiFunction<Object, String, Filter> _filterBiFunction;
	private GroupLocalService _groupLocalService;
	private HttpServletRequest _httpServletRequest;
	private HttpServletResponse _httpServletResponse;
	private RoleLocalService _roleLocalService;
	private BiFunction<Object, String, Sort[]> _sortsBiFunction;
	private UriInfo _uriInfo;
	private com.liferay.portal.kernel.model.User _user;

}
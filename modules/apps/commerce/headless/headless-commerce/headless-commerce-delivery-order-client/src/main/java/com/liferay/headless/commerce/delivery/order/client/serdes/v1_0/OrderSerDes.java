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

package com.liferay.headless.commerce.delivery.order.client.serdes.v1_0;

import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.OrderComment;
import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.OrderItem;
import com.liferay.headless.commerce.delivery.order.client.json.BaseJSONParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.Generated;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public class OrderSerDes {

	public static Order toDTO(String json) {
		OrderJSONParser orderJSONParser = new OrderJSONParser();

		return orderJSONParser.parseToDTO(json);
	}

	public static Order[] toDTOs(String json) {
		OrderJSONParser orderJSONParser = new OrderJSONParser();

		return orderJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Order order) {
		if (order == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (order.getAccount() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"account\": ");

			sb.append("\"");

			sb.append(_escape(order.getAccount()));

			sb.append("\"");
		}

		if (order.getAccountId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountId\": ");

			sb.append(order.getAccountId());
		}

		if (order.getAuthor() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"author\": ");

			sb.append("\"");

			sb.append(_escape(order.getAuthor()));

			sb.append("\"");
		}

		if (order.getBillingAddress() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"billingAddress\": ");

			sb.append(String.valueOf(order.getBillingAddress()));
		}

		if (order.getBillingAddressId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"billingAddressId\": ");

			sb.append(order.getBillingAddressId());
		}

		if (order.getChannelId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"channelId\": ");

			sb.append(order.getChannelId());
		}

		if (order.getCouponCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"couponCode\": ");

			sb.append("\"");

			sb.append(_escape(order.getCouponCode()));

			sb.append("\"");
		}

		if (order.getCreateDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"createDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(order.getCreateDate()));

			sb.append("\"");
		}

		if (order.getCurrencyCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"currencyCode\": ");

			sb.append("\"");

			sb.append(_escape(order.getCurrencyCode()));

			sb.append("\"");
		}

		if (order.getCustomFields() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customFields\": ");

			sb.append(_toJSON(order.getCustomFields()));
		}

		if (order.getErrorMessages() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"errorMessages\": ");

			sb.append("[");

			for (int i = 0; i < order.getErrorMessages().length; i++) {
				sb.append("\"");

				sb.append(_escape(order.getErrorMessages()[i]));

				sb.append("\"");

				if ((i + 1) < order.getErrorMessages().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (order.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(order.getId());
		}

		if (order.getLastPriceUpdateDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"lastPriceUpdateDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(order.getLastPriceUpdateDate()));

			sb.append("\"");
		}

		if (order.getModifiedDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"modifiedDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(order.getModifiedDate()));

			sb.append("\"");
		}

		if (order.getNotes() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"notes\": ");

			sb.append("[");

			for (int i = 0; i < order.getNotes().length; i++) {
				sb.append(String.valueOf(order.getNotes()[i]));

				if ((i + 1) < order.getNotes().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (order.getOrderItems() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderItems\": ");

			sb.append("[");

			for (int i = 0; i < order.getOrderItems().length; i++) {
				sb.append(String.valueOf(order.getOrderItems()[i]));

				if ((i + 1) < order.getOrderItems().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (order.getOrderStatusInfo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderStatusInfo\": ");

			sb.append(String.valueOf(order.getOrderStatusInfo()));
		}

		if (order.getOrderTypeExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderTypeExternalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(order.getOrderTypeExternalReferenceCode()));

			sb.append("\"");
		}

		if (order.getOrderTypeId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderTypeId\": ");

			sb.append(order.getOrderTypeId());
		}

		if (order.getOrderUUID() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderUUID\": ");

			sb.append("\"");

			sb.append(_escape(order.getOrderUUID()));

			sb.append("\"");
		}

		if (order.getPaymentMethod() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paymentMethod\": ");

			sb.append("\"");

			sb.append(_escape(order.getPaymentMethod()));

			sb.append("\"");
		}

		if (order.getPaymentMethodLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paymentMethodLabel\": ");

			sb.append("\"");

			sb.append(_escape(order.getPaymentMethodLabel()));

			sb.append("\"");
		}

		if (order.getPaymentStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paymentStatus\": ");

			sb.append(order.getPaymentStatus());
		}

		if (order.getPaymentStatusInfo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paymentStatusInfo\": ");

			sb.append(String.valueOf(order.getPaymentStatusInfo()));
		}

		if (order.getPaymentStatusLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paymentStatusLabel\": ");

			sb.append("\"");

			sb.append(_escape(order.getPaymentStatusLabel()));

			sb.append("\"");
		}

		if (order.getPrintedNote() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"printedNote\": ");

			sb.append("\"");

			sb.append(_escape(order.getPrintedNote()));

			sb.append("\"");
		}

		if (order.getPurchaseOrderNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"purchaseOrderNumber\": ");

			sb.append("\"");

			sb.append(_escape(order.getPurchaseOrderNumber()));

			sb.append("\"");
		}

		if (order.getShippingAddress() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingAddress\": ");

			sb.append(String.valueOf(order.getShippingAddress()));
		}

		if (order.getShippingAddressId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingAddressId\": ");

			sb.append(order.getShippingAddressId());
		}

		if (order.getShippingMethod() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingMethod\": ");

			sb.append("\"");

			sb.append(_escape(order.getShippingMethod()));

			sb.append("\"");
		}

		if (order.getShippingOption() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingOption\": ");

			sb.append("\"");

			sb.append(_escape(order.getShippingOption()));

			sb.append("\"");
		}

		if (order.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(order.getStatus()));

			sb.append("\"");
		}

		if (order.getSummary() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"summary\": ");

			sb.append(String.valueOf(order.getSummary()));
		}

		if (order.getUseAsBilling() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"useAsBilling\": ");

			sb.append(order.getUseAsBilling());
		}

		if (order.getValid() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"valid\": ");

			sb.append(order.getValid());
		}

		if (order.getWorkflowStatusInfo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"workflowStatusInfo\": ");

			sb.append(String.valueOf(order.getWorkflowStatusInfo()));
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		OrderJSONParser orderJSONParser = new OrderJSONParser();

		return orderJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Order order) {
		if (order == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (order.getAccount() == null) {
			map.put("account", null);
		}
		else {
			map.put("account", String.valueOf(order.getAccount()));
		}

		if (order.getAccountId() == null) {
			map.put("accountId", null);
		}
		else {
			map.put("accountId", String.valueOf(order.getAccountId()));
		}

		if (order.getAuthor() == null) {
			map.put("author", null);
		}
		else {
			map.put("author", String.valueOf(order.getAuthor()));
		}

		if (order.getBillingAddress() == null) {
			map.put("billingAddress", null);
		}
		else {
			map.put(
				"billingAddress", String.valueOf(order.getBillingAddress()));
		}

		if (order.getBillingAddressId() == null) {
			map.put("billingAddressId", null);
		}
		else {
			map.put(
				"billingAddressId",
				String.valueOf(order.getBillingAddressId()));
		}

		if (order.getChannelId() == null) {
			map.put("channelId", null);
		}
		else {
			map.put("channelId", String.valueOf(order.getChannelId()));
		}

		if (order.getCouponCode() == null) {
			map.put("couponCode", null);
		}
		else {
			map.put("couponCode", String.valueOf(order.getCouponCode()));
		}

		if (order.getCreateDate() == null) {
			map.put("createDate", null);
		}
		else {
			map.put(
				"createDate",
				liferayToJSONDateFormat.format(order.getCreateDate()));
		}

		if (order.getCurrencyCode() == null) {
			map.put("currencyCode", null);
		}
		else {
			map.put("currencyCode", String.valueOf(order.getCurrencyCode()));
		}

		if (order.getCustomFields() == null) {
			map.put("customFields", null);
		}
		else {
			map.put("customFields", String.valueOf(order.getCustomFields()));
		}

		if (order.getErrorMessages() == null) {
			map.put("errorMessages", null);
		}
		else {
			map.put("errorMessages", String.valueOf(order.getErrorMessages()));
		}

		if (order.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(order.getId()));
		}

		if (order.getLastPriceUpdateDate() == null) {
			map.put("lastPriceUpdateDate", null);
		}
		else {
			map.put(
				"lastPriceUpdateDate",
				liferayToJSONDateFormat.format(order.getLastPriceUpdateDate()));
		}

		if (order.getModifiedDate() == null) {
			map.put("modifiedDate", null);
		}
		else {
			map.put(
				"modifiedDate",
				liferayToJSONDateFormat.format(order.getModifiedDate()));
		}

		if (order.getNotes() == null) {
			map.put("notes", null);
		}
		else {
			map.put("notes", String.valueOf(order.getNotes()));
		}

		if (order.getOrderItems() == null) {
			map.put("orderItems", null);
		}
		else {
			map.put("orderItems", String.valueOf(order.getOrderItems()));
		}

		if (order.getOrderStatusInfo() == null) {
			map.put("orderStatusInfo", null);
		}
		else {
			map.put(
				"orderStatusInfo", String.valueOf(order.getOrderStatusInfo()));
		}

		if (order.getOrderTypeExternalReferenceCode() == null) {
			map.put("orderTypeExternalReferenceCode", null);
		}
		else {
			map.put(
				"orderTypeExternalReferenceCode",
				String.valueOf(order.getOrderTypeExternalReferenceCode()));
		}

		if (order.getOrderTypeId() == null) {
			map.put("orderTypeId", null);
		}
		else {
			map.put("orderTypeId", String.valueOf(order.getOrderTypeId()));
		}

		if (order.getOrderUUID() == null) {
			map.put("orderUUID", null);
		}
		else {
			map.put("orderUUID", String.valueOf(order.getOrderUUID()));
		}

		if (order.getPaymentMethod() == null) {
			map.put("paymentMethod", null);
		}
		else {
			map.put("paymentMethod", String.valueOf(order.getPaymentMethod()));
		}

		if (order.getPaymentMethodLabel() == null) {
			map.put("paymentMethodLabel", null);
		}
		else {
			map.put(
				"paymentMethodLabel",
				String.valueOf(order.getPaymentMethodLabel()));
		}

		if (order.getPaymentStatus() == null) {
			map.put("paymentStatus", null);
		}
		else {
			map.put("paymentStatus", String.valueOf(order.getPaymentStatus()));
		}

		if (order.getPaymentStatusInfo() == null) {
			map.put("paymentStatusInfo", null);
		}
		else {
			map.put(
				"paymentStatusInfo",
				String.valueOf(order.getPaymentStatusInfo()));
		}

		if (order.getPaymentStatusLabel() == null) {
			map.put("paymentStatusLabel", null);
		}
		else {
			map.put(
				"paymentStatusLabel",
				String.valueOf(order.getPaymentStatusLabel()));
		}

		if (order.getPrintedNote() == null) {
			map.put("printedNote", null);
		}
		else {
			map.put("printedNote", String.valueOf(order.getPrintedNote()));
		}

		if (order.getPurchaseOrderNumber() == null) {
			map.put("purchaseOrderNumber", null);
		}
		else {
			map.put(
				"purchaseOrderNumber",
				String.valueOf(order.getPurchaseOrderNumber()));
		}

		if (order.getShippingAddress() == null) {
			map.put("shippingAddress", null);
		}
		else {
			map.put(
				"shippingAddress", String.valueOf(order.getShippingAddress()));
		}

		if (order.getShippingAddressId() == null) {
			map.put("shippingAddressId", null);
		}
		else {
			map.put(
				"shippingAddressId",
				String.valueOf(order.getShippingAddressId()));
		}

		if (order.getShippingMethod() == null) {
			map.put("shippingMethod", null);
		}
		else {
			map.put(
				"shippingMethod", String.valueOf(order.getShippingMethod()));
		}

		if (order.getShippingOption() == null) {
			map.put("shippingOption", null);
		}
		else {
			map.put(
				"shippingOption", String.valueOf(order.getShippingOption()));
		}

		if (order.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(order.getStatus()));
		}

		if (order.getSummary() == null) {
			map.put("summary", null);
		}
		else {
			map.put("summary", String.valueOf(order.getSummary()));
		}

		if (order.getUseAsBilling() == null) {
			map.put("useAsBilling", null);
		}
		else {
			map.put("useAsBilling", String.valueOf(order.getUseAsBilling()));
		}

		if (order.getValid() == null) {
			map.put("valid", null);
		}
		else {
			map.put("valid", String.valueOf(order.getValid()));
		}

		if (order.getWorkflowStatusInfo() == null) {
			map.put("workflowStatusInfo", null);
		}
		else {
			map.put(
				"workflowStatusInfo",
				String.valueOf(order.getWorkflowStatusInfo()));
		}

		return map;
	}

	public static class OrderJSONParser extends BaseJSONParser<Order> {

		@Override
		protected Order createDTO() {
			return new Order();
		}

		@Override
		protected Order[] createDTOArray(int size) {
			return new Order[size];
		}

		@Override
		protected void setField(
			Order order, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "account")) {
				if (jsonParserFieldValue != null) {
					order.setAccount((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "accountId")) {
				if (jsonParserFieldValue != null) {
					order.setAccountId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "author")) {
				if (jsonParserFieldValue != null) {
					order.setAuthor((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "billingAddress")) {
				if (jsonParserFieldValue != null) {
					order.setBillingAddress(
						AddressSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "billingAddressId")) {
				if (jsonParserFieldValue != null) {
					order.setBillingAddressId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "channelId")) {
				if (jsonParserFieldValue != null) {
					order.setChannelId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "couponCode")) {
				if (jsonParserFieldValue != null) {
					order.setCouponCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "createDate")) {
				if (jsonParserFieldValue != null) {
					order.setCreateDate(toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "currencyCode")) {
				if (jsonParserFieldValue != null) {
					order.setCurrencyCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customFields")) {
				if (jsonParserFieldValue != null) {
					order.setCustomFields(
						(Map)OrderSerDes.toMap((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "errorMessages")) {
				if (jsonParserFieldValue != null) {
					order.setErrorMessages(
						toStrings((Object[])jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					order.setId(Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "lastPriceUpdateDate")) {

				if (jsonParserFieldValue != null) {
					order.setLastPriceUpdateDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "modifiedDate")) {
				if (jsonParserFieldValue != null) {
					order.setModifiedDate(toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "notes")) {
				if (jsonParserFieldValue != null) {
					order.setNotes(
						Stream.of(
							toStrings((Object[])jsonParserFieldValue)
						).map(
							object -> OrderCommentSerDes.toDTO((String)object)
						).toArray(
							size -> new OrderComment[size]
						));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "orderItems")) {
				if (jsonParserFieldValue != null) {
					order.setOrderItems(
						Stream.of(
							toStrings((Object[])jsonParserFieldValue)
						).map(
							object -> OrderItemSerDes.toDTO((String)object)
						).toArray(
							size -> new OrderItem[size]
						));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "orderStatusInfo")) {
				if (jsonParserFieldValue != null) {
					order.setOrderStatusInfo(
						StatusSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName,
						"orderTypeExternalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					order.setOrderTypeExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "orderTypeId")) {
				if (jsonParserFieldValue != null) {
					order.setOrderTypeId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "orderUUID")) {
				if (jsonParserFieldValue != null) {
					order.setOrderUUID((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "paymentMethod")) {
				if (jsonParserFieldValue != null) {
					order.setPaymentMethod((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "paymentMethodLabel")) {

				if (jsonParserFieldValue != null) {
					order.setPaymentMethodLabel((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "paymentStatus")) {
				if (jsonParserFieldValue != null) {
					order.setPaymentStatus(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "paymentStatusInfo")) {
				if (jsonParserFieldValue != null) {
					order.setPaymentStatusInfo(
						StatusSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "paymentStatusLabel")) {

				if (jsonParserFieldValue != null) {
					order.setPaymentStatusLabel((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "printedNote")) {
				if (jsonParserFieldValue != null) {
					order.setPrintedNote((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "purchaseOrderNumber")) {

				if (jsonParserFieldValue != null) {
					order.setPurchaseOrderNumber((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingAddress")) {
				if (jsonParserFieldValue != null) {
					order.setShippingAddress(
						AddressSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingAddressId")) {
				if (jsonParserFieldValue != null) {
					order.setShippingAddressId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingMethod")) {
				if (jsonParserFieldValue != null) {
					order.setShippingMethod((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingOption")) {
				if (jsonParserFieldValue != null) {
					order.setShippingOption((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					order.setStatus((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "summary")) {
				if (jsonParserFieldValue != null) {
					order.setSummary(
						SummarySerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "useAsBilling")) {
				if (jsonParserFieldValue != null) {
					order.setUseAsBilling((Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "valid")) {
				if (jsonParserFieldValue != null) {
					order.setValid((Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "workflowStatusInfo")) {

				if (jsonParserFieldValue != null) {
					order.setWorkflowStatusInfo(
						StatusSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}
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

package com.liferay.headless.commerce.delivery.order.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.OrderItem;
import com.liferay.headless.commerce.delivery.order.client.http.HttpInvoker;
import com.liferay.headless.commerce.delivery.order.client.pagination.Page;
import com.liferay.headless.commerce.delivery.order.client.pagination.Pagination;
import com.liferay.headless.commerce.delivery.order.client.resource.v1_0.OrderItemResource;
import com.liferay.headless.commerce.delivery.order.client.serdes.v1_0.OrderItemSerDes;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.Method;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Generated;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public abstract class BaseOrderItemResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_orderItemResource.setContextCompany(testCompany);

		OrderItemResource.Builder builder = OrderItemResource.builder();

		orderItemResource = builder.authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		OrderItem orderItem1 = randomOrderItem();

		String json = objectMapper.writeValueAsString(orderItem1);

		OrderItem orderItem2 = OrderItemSerDes.toDTO(json);

		Assert.assertTrue(equals(orderItem1, orderItem2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		OrderItem orderItem = randomOrderItem();

		String json1 = objectMapper.writeValueAsString(orderItem);
		String json2 = OrderItemSerDes.toJSON(orderItem);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		OrderItem orderItem = randomOrderItem();

		orderItem.setAdaptiveMediaImageHTMLTag(regex);
		orderItem.setName(regex);
		orderItem.setOptions(regex);
		orderItem.setSku(regex);
		orderItem.setThumbnail(regex);

		String json = OrderItemSerDes.toJSON(orderItem);

		Assert.assertFalse(json.contains(regex));

		orderItem = OrderItemSerDes.toDTO(json);

		Assert.assertEquals(regex, orderItem.getAdaptiveMediaImageHTMLTag());
		Assert.assertEquals(regex, orderItem.getName());
		Assert.assertEquals(regex, orderItem.getOptions());
		Assert.assertEquals(regex, orderItem.getSku());
		Assert.assertEquals(regex, orderItem.getThumbnail());
	}

	@Test
	public void testGetOrderItem() throws Exception {
		OrderItem postOrderItem = testGetOrderItem_addOrderItem();

		OrderItem getOrderItem = orderItemResource.getOrderItem(
			postOrderItem.getId());

		assertEquals(postOrderItem, getOrderItem);
		assertValid(getOrderItem);
	}

	protected OrderItem testGetOrderItem_addOrderItem() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetOrderItem() throws Exception {
		OrderItem orderItem = testGraphQLGetOrderItem_addOrderItem();

		Assert.assertTrue(
			equals(
				orderItem,
				OrderItemSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"orderItem",
								new HashMap<String, Object>() {
									{
										put("orderItemId", orderItem.getId());
									}
								},
								getGraphQLFields())),
						"JSONObject/data", "Object/orderItem"))));
	}

	@Test
	public void testGraphQLGetOrderItemNotFound() throws Exception {
		Long irrelevantOrderItemId = RandomTestUtil.randomLong();

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"orderItem",
						new HashMap<String, Object>() {
							{
								put("orderItemId", irrelevantOrderItemId);
							}
						},
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	protected OrderItem testGraphQLGetOrderItem_addOrderItem()
		throws Exception {

		return testGraphQLOrderItem_addOrderItem();
	}

	@Test
	public void testGetOrderItemsPage() throws Exception {
		Long orderId = testGetOrderItemsPage_getOrderId();
		Long irrelevantOrderId = testGetOrderItemsPage_getIrrelevantOrderId();

		Page<OrderItem> page = orderItemResource.getOrderItemsPage(
			orderId, null, Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());

		if (irrelevantOrderId != null) {
			OrderItem irrelevantOrderItem = testGetOrderItemsPage_addOrderItem(
				irrelevantOrderId, randomIrrelevantOrderItem());

			page = orderItemResource.getOrderItemsPage(
				irrelevantOrderId, null, Pagination.of(1, 2));

			Assert.assertEquals(1, page.getTotalCount());

			assertEquals(
				Arrays.asList(irrelevantOrderItem),
				(List<OrderItem>)page.getItems());
			assertValid(page);
		}

		OrderItem orderItem1 = testGetOrderItemsPage_addOrderItem(
			orderId, randomOrderItem());

		OrderItem orderItem2 = testGetOrderItemsPage_addOrderItem(
			orderId, randomOrderItem());

		page = orderItemResource.getOrderItemsPage(
			orderId, null, Pagination.of(1, 10));

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(orderItem1, orderItem2),
			(List<OrderItem>)page.getItems());
		assertValid(page);
	}

	@Test
	public void testGetOrderItemsPageWithPagination() throws Exception {
		Long orderId = testGetOrderItemsPage_getOrderId();

		OrderItem orderItem1 = testGetOrderItemsPage_addOrderItem(
			orderId, randomOrderItem());

		OrderItem orderItem2 = testGetOrderItemsPage_addOrderItem(
			orderId, randomOrderItem());

		OrderItem orderItem3 = testGetOrderItemsPage_addOrderItem(
			orderId, randomOrderItem());

		Page<OrderItem> page1 = orderItemResource.getOrderItemsPage(
			orderId, null, Pagination.of(1, 2));

		List<OrderItem> orderItems1 = (List<OrderItem>)page1.getItems();

		Assert.assertEquals(orderItems1.toString(), 2, orderItems1.size());

		Page<OrderItem> page2 = orderItemResource.getOrderItemsPage(
			orderId, null, Pagination.of(2, 2));

		Assert.assertEquals(3, page2.getTotalCount());

		List<OrderItem> orderItems2 = (List<OrderItem>)page2.getItems();

		Assert.assertEquals(orderItems2.toString(), 1, orderItems2.size());

		Page<OrderItem> page3 = orderItemResource.getOrderItemsPage(
			orderId, null, Pagination.of(1, 3));

		assertEqualsIgnoringOrder(
			Arrays.asList(orderItem1, orderItem2, orderItem3),
			(List<OrderItem>)page3.getItems());
	}

	protected OrderItem testGetOrderItemsPage_addOrderItem(
			Long orderId, OrderItem orderItem)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetOrderItemsPage_getOrderId() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetOrderItemsPage_getIrrelevantOrderId()
		throws Exception {

		return null;
	}

	@Test
	public void testGraphQLGetOrderItemsPage() throws Exception {
		Long orderId = testGetOrderItemsPage_getOrderId();

		GraphQLField graphQLField = new GraphQLField(
			"orderItems",
			new HashMap<String, Object>() {
				{
					put("page", 1);
					put("pageSize", 10);

					put("orderId", orderId);
				}
			},
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		JSONObject orderItemsJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/orderItems");

		Assert.assertEquals(0, orderItemsJSONObject.get("totalCount"));

		OrderItem orderItem1 = testGraphQLGetOrderItemsPage_addOrderItem();
		OrderItem orderItem2 = testGraphQLGetOrderItemsPage_addOrderItem();

		orderItemsJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/orderItems");

		Assert.assertEquals(2, orderItemsJSONObject.getLong("totalCount"));

		assertEqualsIgnoringOrder(
			Arrays.asList(orderItem1, orderItem2),
			Arrays.asList(
				OrderItemSerDes.toDTOs(
					orderItemsJSONObject.getString("items"))));
	}

	protected OrderItem testGraphQLGetOrderItemsPage_addOrderItem()
		throws Exception {

		return testGraphQLOrderItem_addOrderItem();
	}

	protected OrderItem testGraphQLOrderItem_addOrderItem() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected void assertContains(
		OrderItem orderItem, List<OrderItem> orderItems) {

		boolean contains = false;

		for (OrderItem item : orderItems) {
			if (equals(orderItem, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			orderItems + " does not contain " + orderItem, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(OrderItem orderItem1, OrderItem orderItem2) {
		Assert.assertTrue(
			orderItem1 + " does not equal " + orderItem2,
			equals(orderItem1, orderItem2));
	}

	protected void assertEquals(
		List<OrderItem> orderItems1, List<OrderItem> orderItems2) {

		Assert.assertEquals(orderItems1.size(), orderItems2.size());

		for (int i = 0; i < orderItems1.size(); i++) {
			OrderItem orderItem1 = orderItems1.get(i);
			OrderItem orderItem2 = orderItems2.get(i);

			assertEquals(orderItem1, orderItem2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<OrderItem> orderItems1, List<OrderItem> orderItems2) {

		Assert.assertEquals(orderItems1.size(), orderItems2.size());

		for (OrderItem orderItem1 : orderItems1) {
			boolean contains = false;

			for (OrderItem orderItem2 : orderItems2) {
				if (equals(orderItem1, orderItem2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				orderItems2 + " does not contain " + orderItem1, contains);
		}
	}

	protected void assertValid(OrderItem orderItem) throws Exception {
		boolean valid = true;

		if (orderItem.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"adaptiveMediaImageHTMLTag", additionalAssertFieldName)) {

				if (orderItem.getAdaptiveMediaImageHTMLTag() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("customFields", additionalAssertFieldName)) {
				if (orderItem.getCustomFields() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("errorMessages", additionalAssertFieldName)) {
				if (orderItem.getErrorMessages() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (orderItem.getName() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("options", additionalAssertFieldName)) {
				if (orderItem.getOptions() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("orderItems", additionalAssertFieldName)) {
				if (orderItem.getOrderItems() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"parentOrderItemId", additionalAssertFieldName)) {

				if (orderItem.getParentOrderItemId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("price", additionalAssertFieldName)) {
				if (orderItem.getPrice() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("productId", additionalAssertFieldName)) {
				if (orderItem.getProductId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("productURLs", additionalAssertFieldName)) {
				if (orderItem.getProductURLs() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("quantity", additionalAssertFieldName)) {
				if (orderItem.getQuantity() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (orderItem.getSettings() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("sku", additionalAssertFieldName)) {
				if (orderItem.getSku() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("skuId", additionalAssertFieldName)) {
				if (orderItem.getSkuId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("subscription", additionalAssertFieldName)) {
				if (orderItem.getSubscription() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("thumbnail", additionalAssertFieldName)) {
				if (orderItem.getThumbnail() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("valid", additionalAssertFieldName)) {
				if (orderItem.getValid() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<OrderItem> page) {
		boolean valid = false;

		java.util.Collection<OrderItem> orderItems = page.getItems();

		int size = orderItems.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.commerce.delivery.order.dto.v1_0.
						OrderItem.class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(OrderItem orderItem1, OrderItem orderItem2) {
		if (orderItem1 == orderItem2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"adaptiveMediaImageHTMLTag", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						orderItem1.getAdaptiveMediaImageHTMLTag(),
						orderItem2.getAdaptiveMediaImageHTMLTag())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("customFields", additionalAssertFieldName)) {
				if (!equals(
						(Map)orderItem1.getCustomFields(),
						(Map)orderItem2.getCustomFields())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("errorMessages", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getErrorMessages(),
						orderItem2.getErrorMessages())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getId(), orderItem2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getName(), orderItem2.getName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("options", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getOptions(), orderItem2.getOptions())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("orderItems", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getOrderItems(),
						orderItem2.getOrderItems())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"parentOrderItemId", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						orderItem1.getParentOrderItemId(),
						orderItem2.getParentOrderItemId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("price", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getPrice(), orderItem2.getPrice())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("productId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getProductId(), orderItem2.getProductId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("productURLs", additionalAssertFieldName)) {
				if (!equals(
						(Map)orderItem1.getProductURLs(),
						(Map)orderItem2.getProductURLs())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("quantity", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getQuantity(), orderItem2.getQuantity())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getSettings(), orderItem2.getSettings())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("sku", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getSku(), orderItem2.getSku())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("skuId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getSkuId(), orderItem2.getSkuId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("subscription", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getSubscription(),
						orderItem2.getSubscription())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("thumbnail", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getThumbnail(), orderItem2.getThumbnail())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("valid", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderItem1.getValid(), orderItem2.getValid())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		Stream<java.lang.reflect.Field> stream = Stream.of(
			ReflectionUtil.getDeclaredFields(clazz));

		return stream.filter(
			field -> !field.isSynthetic()
		).toArray(
			java.lang.reflect.Field[]::new
		);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_orderItemResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_orderItemResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		java.util.Collection<EntityField> entityFields = getEntityFields();

		Stream<EntityField> stream = entityFields.stream();

		return stream.filter(
			entityField ->
				Objects.equals(entityField.getType(), type) &&
				!ArrayUtil.contains(
					getIgnoredEntityFieldNames(), entityField.getName())
		).collect(
			Collectors.toList()
		);
	}

	protected String getFilterString(
		EntityField entityField, String operator, OrderItem orderItem) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("adaptiveMediaImageHTMLTag")) {
			sb.append("'");
			sb.append(String.valueOf(orderItem.getAdaptiveMediaImageHTMLTag()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("customFields")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("errorMessages")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("id")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("name")) {
			sb.append("'");
			sb.append(String.valueOf(orderItem.getName()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("options")) {
			sb.append("'");
			sb.append(String.valueOf(orderItem.getOptions()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("orderItems")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("parentOrderItemId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("price")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("productId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("productURLs")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("quantity")) {
			sb.append(String.valueOf(orderItem.getQuantity()));

			return sb.toString();
		}

		if (entityFieldName.equals("settings")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("sku")) {
			sb.append("'");
			sb.append(String.valueOf(orderItem.getSku()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("skuId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("subscription")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("thumbnail")) {
			sb.append("'");
			sb.append(String.valueOf(orderItem.getThumbnail()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("valid")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword("test@liferay.com:test");

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected OrderItem randomOrderItem() throws Exception {
		return new OrderItem() {
			{
				adaptiveMediaImageHTMLTag = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				options = StringUtil.toLowerCase(RandomTestUtil.randomString());
				parentOrderItemId = RandomTestUtil.randomLong();
				productId = RandomTestUtil.randomLong();
				quantity = RandomTestUtil.randomInt();
				sku = StringUtil.toLowerCase(RandomTestUtil.randomString());
				skuId = RandomTestUtil.randomLong();
				subscription = RandomTestUtil.randomBoolean();
				thumbnail = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				valid = RandomTestUtil.randomBoolean();
			}
		};
	}

	protected OrderItem randomIrrelevantOrderItem() throws Exception {
		OrderItem randomIrrelevantOrderItem = randomOrderItem();

		return randomIrrelevantOrderItem;
	}

	protected OrderItem randomPatchOrderItem() throws Exception {
		return randomOrderItem();
	}

	protected OrderItemResource orderItemResource;
	protected Group irrelevantGroup;
	protected Company testCompany;
	protected Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = _getSuperClass(source.getClass());

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					sourceClass.getDeclaredFields()) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				Method setMethod = _getMethod(
					targetClass, field.getName(), "set",
					getMethod.getReturnType());

				setMethod.invoke(target, getMethod.invoke(source));
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Class<?> _getSuperClass(Class<?> clazz) {
			Class<?> superClass = clazz.getSuperclass();

			if ((superClass == null) || (superClass == Object.class)) {
				return clazz;
			}

			return superClass;
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseOrderItemResourceTestCase.class);

	private static DateFormat _dateFormat;

	@Inject
	private
		com.liferay.headless.commerce.delivery.order.resource.v1_0.
			OrderItemResource _orderItemResource;

}
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

import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.OrderComment;
import com.liferay.headless.commerce.delivery.order.client.http.HttpInvoker;
import com.liferay.headless.commerce.delivery.order.client.pagination.Page;
import com.liferay.headless.commerce.delivery.order.client.pagination.Pagination;
import com.liferay.headless.commerce.delivery.order.client.resource.v1_0.OrderCommentResource;
import com.liferay.headless.commerce.delivery.order.client.serdes.v1_0.OrderCommentSerDes;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
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
public abstract class BaseOrderCommentResourceTestCase {

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

		_orderCommentResource.setContextCompany(testCompany);

		OrderCommentResource.Builder builder = OrderCommentResource.builder();

		orderCommentResource = builder.authentication(
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

		OrderComment orderComment1 = randomOrderComment();

		String json = objectMapper.writeValueAsString(orderComment1);

		OrderComment orderComment2 = OrderCommentSerDes.toDTO(json);

		Assert.assertTrue(equals(orderComment1, orderComment2));
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

		OrderComment orderComment = randomOrderComment();

		String json1 = objectMapper.writeValueAsString(orderComment);
		String json2 = OrderCommentSerDes.toJSON(orderComment);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		OrderComment orderComment = randomOrderComment();

		orderComment.setAuthor(regex);
		orderComment.setContent(regex);

		String json = OrderCommentSerDes.toJSON(orderComment);

		Assert.assertFalse(json.contains(regex));

		orderComment = OrderCommentSerDes.toDTO(json);

		Assert.assertEquals(regex, orderComment.getAuthor());
		Assert.assertEquals(regex, orderComment.getContent());
	}

	@Test
	public void testDeleteOrderComment() throws Exception {
		@SuppressWarnings("PMD.UnusedLocalVariable")
		OrderComment orderComment = testDeleteOrderComment_addOrderComment();

		assertHttpResponseStatusCode(
			204,
			orderCommentResource.deleteOrderCommentHttpResponse(
				orderComment.getId()));

		assertHttpResponseStatusCode(
			404,
			orderCommentResource.getOrderCommentHttpResponse(
				orderComment.getId()));

		assertHttpResponseStatusCode(
			404, orderCommentResource.getOrderCommentHttpResponse(0L));
	}

	protected OrderComment testDeleteOrderComment_addOrderComment()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLDeleteOrderComment() throws Exception {
		OrderComment orderComment =
			testGraphQLDeleteOrderComment_addOrderComment();

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteOrderComment",
						new HashMap<String, Object>() {
							{
								put("orderCommentId", orderComment.getId());
							}
						})),
				"JSONObject/data", "Object/deleteOrderComment"));
		JSONArray errorsJSONArray = JSONUtil.getValueAsJSONArray(
			invokeGraphQLQuery(
				new GraphQLField(
					"orderComment",
					new HashMap<String, Object>() {
						{
							put("orderCommentId", orderComment.getId());
						}
					},
					new GraphQLField("id"))),
			"JSONArray/errors");

		Assert.assertTrue(errorsJSONArray.length() > 0);
	}

	protected OrderComment testGraphQLDeleteOrderComment_addOrderComment()
		throws Exception {

		return testGraphQLOrderComment_addOrderComment();
	}

	@Test
	public void testGetOrderComment() throws Exception {
		OrderComment postOrderComment = testGetOrderComment_addOrderComment();

		OrderComment getOrderComment = orderCommentResource.getOrderComment(
			postOrderComment.getId());

		assertEquals(postOrderComment, getOrderComment);
		assertValid(getOrderComment);
	}

	protected OrderComment testGetOrderComment_addOrderComment()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetOrderComment() throws Exception {
		OrderComment orderComment =
			testGraphQLGetOrderComment_addOrderComment();

		Assert.assertTrue(
			equals(
				orderComment,
				OrderCommentSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"orderComment",
								new HashMap<String, Object>() {
									{
										put(
											"orderCommentId",
											orderComment.getId());
									}
								},
								getGraphQLFields())),
						"JSONObject/data", "Object/orderComment"))));
	}

	@Test
	public void testGraphQLGetOrderCommentNotFound() throws Exception {
		Long irrelevantOrderCommentId = RandomTestUtil.randomLong();

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"orderComment",
						new HashMap<String, Object>() {
							{
								put("orderCommentId", irrelevantOrderCommentId);
							}
						},
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	protected OrderComment testGraphQLGetOrderComment_addOrderComment()
		throws Exception {

		return testGraphQLOrderComment_addOrderComment();
	}

	@Test
	public void testPatchOrderComment() throws Exception {
		OrderComment postOrderComment = testPatchOrderComment_addOrderComment();

		OrderComment randomPatchOrderComment = randomPatchOrderComment();

		@SuppressWarnings("PMD.UnusedLocalVariable")
		OrderComment patchOrderComment = orderCommentResource.patchOrderComment(
			postOrderComment.getId(), randomPatchOrderComment);

		OrderComment expectedPatchOrderComment = postOrderComment.clone();

		BeanTestUtil.copyProperties(
			randomPatchOrderComment, expectedPatchOrderComment);

		OrderComment getOrderComment = orderCommentResource.getOrderComment(
			patchOrderComment.getId());

		assertEquals(expectedPatchOrderComment, getOrderComment);
		assertValid(getOrderComment);
	}

	protected OrderComment testPatchOrderComment_addOrderComment()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testPutOrderComment() throws Exception {
		OrderComment postOrderComment = testPutOrderComment_addOrderComment();

		OrderComment randomOrderComment = randomOrderComment();

		OrderComment putOrderComment = orderCommentResource.putOrderComment(
			postOrderComment.getId(), randomOrderComment);

		assertEquals(randomOrderComment, putOrderComment);
		assertValid(putOrderComment);

		OrderComment getOrderComment = orderCommentResource.getOrderComment(
			putOrderComment.getId());

		assertEquals(randomOrderComment, getOrderComment);
		assertValid(getOrderComment);
	}

	protected OrderComment testPutOrderComment_addOrderComment()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGetOrderCommentsPage() throws Exception {
		Long orderId = testGetOrderCommentsPage_getOrderId();
		Long irrelevantOrderId =
			testGetOrderCommentsPage_getIrrelevantOrderId();

		Page<OrderComment> page = orderCommentResource.getOrderCommentsPage(
			orderId, Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());

		if (irrelevantOrderId != null) {
			OrderComment irrelevantOrderComment =
				testGetOrderCommentsPage_addOrderComment(
					irrelevantOrderId, randomIrrelevantOrderComment());

			page = orderCommentResource.getOrderCommentsPage(
				irrelevantOrderId, Pagination.of(1, 2));

			Assert.assertEquals(1, page.getTotalCount());

			assertEquals(
				Arrays.asList(irrelevantOrderComment),
				(List<OrderComment>)page.getItems());
			assertValid(page);
		}

		OrderComment orderComment1 = testGetOrderCommentsPage_addOrderComment(
			orderId, randomOrderComment());

		OrderComment orderComment2 = testGetOrderCommentsPage_addOrderComment(
			orderId, randomOrderComment());

		page = orderCommentResource.getOrderCommentsPage(
			orderId, Pagination.of(1, 10));

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(orderComment1, orderComment2),
			(List<OrderComment>)page.getItems());
		assertValid(page);

		orderCommentResource.deleteOrderComment(orderComment1.getId());

		orderCommentResource.deleteOrderComment(orderComment2.getId());
	}

	@Test
	public void testGetOrderCommentsPageWithPagination() throws Exception {
		Long orderId = testGetOrderCommentsPage_getOrderId();

		OrderComment orderComment1 = testGetOrderCommentsPage_addOrderComment(
			orderId, randomOrderComment());

		OrderComment orderComment2 = testGetOrderCommentsPage_addOrderComment(
			orderId, randomOrderComment());

		OrderComment orderComment3 = testGetOrderCommentsPage_addOrderComment(
			orderId, randomOrderComment());

		Page<OrderComment> page1 = orderCommentResource.getOrderCommentsPage(
			orderId, Pagination.of(1, 2));

		List<OrderComment> orderComments1 =
			(List<OrderComment>)page1.getItems();

		Assert.assertEquals(
			orderComments1.toString(), 2, orderComments1.size());

		Page<OrderComment> page2 = orderCommentResource.getOrderCommentsPage(
			orderId, Pagination.of(2, 2));

		Assert.assertEquals(3, page2.getTotalCount());

		List<OrderComment> orderComments2 =
			(List<OrderComment>)page2.getItems();

		Assert.assertEquals(
			orderComments2.toString(), 1, orderComments2.size());

		Page<OrderComment> page3 = orderCommentResource.getOrderCommentsPage(
			orderId, Pagination.of(1, 3));

		assertEqualsIgnoringOrder(
			Arrays.asList(orderComment1, orderComment2, orderComment3),
			(List<OrderComment>)page3.getItems());
	}

	protected OrderComment testGetOrderCommentsPage_addOrderComment(
			Long orderId, OrderComment orderComment)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetOrderCommentsPage_getOrderId() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetOrderCommentsPage_getIrrelevantOrderId()
		throws Exception {

		return null;
	}

	@Test
	public void testGraphQLGetOrderCommentsPage() throws Exception {
		Long orderId = testGetOrderCommentsPage_getOrderId();

		GraphQLField graphQLField = new GraphQLField(
			"orderComments",
			new HashMap<String, Object>() {
				{
					put("page", 1);
					put("pageSize", 10);

					put("orderId", orderId);
				}
			},
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		JSONObject orderCommentsJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/orderComments");

		Assert.assertEquals(0, orderCommentsJSONObject.get("totalCount"));

		OrderComment orderComment1 =
			testGraphQLGetOrderCommentsPage_addOrderComment();
		OrderComment orderComment2 =
			testGraphQLGetOrderCommentsPage_addOrderComment();

		orderCommentsJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/orderComments");

		Assert.assertEquals(2, orderCommentsJSONObject.getLong("totalCount"));

		assertEqualsIgnoringOrder(
			Arrays.asList(orderComment1, orderComment2),
			Arrays.asList(
				OrderCommentSerDes.toDTOs(
					orderCommentsJSONObject.getString("items"))));
	}

	protected OrderComment testGraphQLGetOrderCommentsPage_addOrderComment()
		throws Exception {

		return testGraphQLOrderComment_addOrderComment();
	}

	@Test
	public void testPostOrderComment() throws Exception {
		OrderComment randomOrderComment = randomOrderComment();

		OrderComment postOrderComment = testPostOrderComment_addOrderComment(
			randomOrderComment);

		assertEquals(randomOrderComment, postOrderComment);
		assertValid(postOrderComment);
	}

	protected OrderComment testPostOrderComment_addOrderComment(
			OrderComment orderComment)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected OrderComment testGraphQLOrderComment_addOrderComment()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected void assertContains(
		OrderComment orderComment, List<OrderComment> orderComments) {

		boolean contains = false;

		for (OrderComment item : orderComments) {
			if (equals(orderComment, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			orderComments + " does not contain " + orderComment, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		OrderComment orderComment1, OrderComment orderComment2) {

		Assert.assertTrue(
			orderComment1 + " does not equal " + orderComment2,
			equals(orderComment1, orderComment2));
	}

	protected void assertEquals(
		List<OrderComment> orderComments1, List<OrderComment> orderComments2) {

		Assert.assertEquals(orderComments1.size(), orderComments2.size());

		for (int i = 0; i < orderComments1.size(); i++) {
			OrderComment orderComment1 = orderComments1.get(i);
			OrderComment orderComment2 = orderComments2.get(i);

			assertEquals(orderComment1, orderComment2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<OrderComment> orderComments1, List<OrderComment> orderComments2) {

		Assert.assertEquals(orderComments1.size(), orderComments2.size());

		for (OrderComment orderComment1 : orderComments1) {
			boolean contains = false;

			for (OrderComment orderComment2 : orderComments2) {
				if (equals(orderComment1, orderComment2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				orderComments2 + " does not contain " + orderComment1,
				contains);
		}
	}

	protected void assertValid(OrderComment orderComment) throws Exception {
		boolean valid = true;

		if (orderComment.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("author", additionalAssertFieldName)) {
				if (orderComment.getAuthor() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("content", additionalAssertFieldName)) {
				if (orderComment.getContent() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("orderId", additionalAssertFieldName)) {
				if (orderComment.getOrderId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("restricted", additionalAssertFieldName)) {
				if (orderComment.getRestricted() == null) {
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

	protected void assertValid(Page<OrderComment> page) {
		boolean valid = false;

		java.util.Collection<OrderComment> orderComments = page.getItems();

		int size = orderComments.size();

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
						OrderComment.class)) {

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

	protected boolean equals(
		OrderComment orderComment1, OrderComment orderComment2) {

		if (orderComment1 == orderComment2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("author", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderComment1.getAuthor(), orderComment2.getAuthor())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("content", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderComment1.getContent(),
						orderComment2.getContent())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderComment1.getId(), orderComment2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("orderId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderComment1.getOrderId(),
						orderComment2.getOrderId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("restricted", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						orderComment1.getRestricted(),
						orderComment2.getRestricted())) {

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

		if (!(_orderCommentResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_orderCommentResource;

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
		EntityField entityField, String operator, OrderComment orderComment) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("author")) {
			sb.append("'");
			sb.append(String.valueOf(orderComment.getAuthor()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("content")) {
			sb.append("'");
			sb.append(String.valueOf(orderComment.getContent()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("id")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("orderId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("restricted")) {
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

	protected OrderComment randomOrderComment() throws Exception {
		return new OrderComment() {
			{
				author = StringUtil.toLowerCase(RandomTestUtil.randomString());
				content = StringUtil.toLowerCase(RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				orderId = RandomTestUtil.randomLong();
				restricted = RandomTestUtil.randomBoolean();
			}
		};
	}

	protected OrderComment randomIrrelevantOrderComment() throws Exception {
		OrderComment randomIrrelevantOrderComment = randomOrderComment();

		return randomIrrelevantOrderComment;
	}

	protected OrderComment randomPatchOrderComment() throws Exception {
		return randomOrderComment();
	}

	protected OrderCommentResource orderCommentResource;
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
		LogFactoryUtil.getLog(BaseOrderCommentResourceTestCase.class);

	private static DateFormat _dateFormat;

	@Inject
	private com.liferay.headless.commerce.delivery.order.resource.v1_0.
		OrderCommentResource _orderCommentResource;

}
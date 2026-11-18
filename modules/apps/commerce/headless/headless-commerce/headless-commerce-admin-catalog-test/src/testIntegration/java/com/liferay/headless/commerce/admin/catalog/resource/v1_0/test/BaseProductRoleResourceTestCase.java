/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.headless.batch.engine.client.dto.v1_0.ImportTask;
import com.liferay.headless.batch.engine.client.http.HttpInvoker.HttpResponse;
import com.liferay.headless.batch.engine.client.resource.v1_0.ImportTaskResource;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductRole;
import com.liferay.headless.commerce.admin.catalog.client.http.HttpInvoker;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.catalog.client.resource.v1_0.ProductRoleResource;
import com.liferay.headless.commerce.admin.catalog.client.serdes.v1_0.ProductRoleSerDes;
import com.liferay.oauth2.provider.scope.ScopeChecker;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.JAXRSWhiteboardTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegate;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegateBuilderRegistry;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.annotation.Generated;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Method;

import java.net.URI;

import java.text.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Zoltán Takács
 * @generated
 */
@Generated("")
public abstract class BaseProductRoleResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		JAXRSWhiteboardTestUtil.ensureReady();
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_productRoleResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		productRoleResource = ProductRoleResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();

		importTaskResource = ImportTaskResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
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
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ProductRole productRole1 = randomProductRole();

		String json = objectMapper.writeValueAsString(productRole1);

		ProductRole productRole2 = ProductRoleSerDes.toDTO(json);

		Assert.assertTrue(equals(productRole1, productRole2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ProductRole productRole = randomProductRole();

		String json1 = objectMapper.writeValueAsString(productRole);
		String json2 = ProductRoleSerDes.toJSON(productRole);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	protected ObjectMapper getClientSerDesObjectMapper() {
		return new ObjectMapper() {
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
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		ProductRole productRole = randomProductRole();

		productRole.setExternalReferenceCode(regex);
		productRole.setName(regex);

		String json = ProductRoleSerDes.toJSON(productRole);

		Assert.assertFalse(json.contains(regex));

		productRole = ProductRoleSerDes.toDTO(json);

		Assert.assertEquals(regex, productRole.getExternalReferenceCode());
		Assert.assertEquals(regex, productRole.getName());
	}

	@Test
	public void testDeleteProductRole() throws Exception {
		@SuppressWarnings("PMD.UnusedLocalVariable")
		ProductRole productRole = testDeleteProductRole_addProductRole();

		assertHttpResponseStatusCode(
			204,
			productRoleResource.deleteProductRoleHttpResponse(
				productRole.getId()));

		assertHttpResponseStatusCode(
			404,
			productRoleResource.getProductRoleHttpResponse(
				productRole.getId()));
		assertHttpResponseStatusCode(
			404, productRoleResource.getProductRoleHttpResponse(0L));
	}

	protected ProductRole testDeleteProductRole_addProductRole()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLDeleteProductRole() throws Exception {

		// No namespace

		@SuppressWarnings("PMD.UnusedLocalVariable")
		ProductRole productRole1 =
			testGraphQLDeleteProductRole_addProductRole();

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteProductRole",
						new HashMap<String, Object>() {
							{
								put("id", productRole1.getId());
							}
						})),
				"JSONObject/data", "Object/deleteProductRole"));

		JSONArray errorsJSONArray1 = JSONUtil.getValueAsJSONArray(
			invokeGraphQLQuery(
				new GraphQLField(
					"productRole",
					new HashMap<String, Object>() {
						{
							put("id", productRole1.getId());
						}
					},
					getGraphQLFields())),
			"JSONArray/errors");

		Assert.assertTrue(errorsJSONArray1.length() > 0);

		// Using the namespace headlessCommerceAdminCatalog_v1_0

		@SuppressWarnings("PMD.UnusedLocalVariable")
		ProductRole productRole2 =
			testGraphQLDeleteProductRole_addProductRole();

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"headlessCommerceAdminCatalog_v1_0",
						new GraphQLField(
							"deleteProductRole",
							new HashMap<String, Object>() {
								{
									put("id", productRole2.getId());
								}
							}))),
				"JSONObject/data",
				"JSONObject/headlessCommerceAdminCatalog_v1_0",
				"Object/deleteProductRole"));

		JSONArray errorsJSONArray2 = JSONUtil.getValueAsJSONArray(
			invokeGraphQLQuery(
				new GraphQLField(
					"headlessCommerceAdminCatalog_v1_0",
					new GraphQLField(
						"productRole",
						new HashMap<String, Object>() {
							{
								put("id", productRole2.getId());
							}
						},
						getGraphQLFields()))),
			"JSONArray/errors");

		Assert.assertTrue(errorsJSONArray2.length() > 0);
	}

	protected ProductRole testGraphQLDeleteProductRole_addProductRole()
		throws Exception {

		return testGraphQLProductRole_addProductRole();
	}

	@Test
	public void testDeleteProductRoleBatch() throws Exception {
		ProductRole productRole1 = testDeleteProductRoleBatch_addProductRole();

		testDeleteProductRoleBatch_deleteProductRole(
			202, null, productRole1.getId());

		assertHttpResponseStatusCode(
			404,
			productRoleResource.getProductRoleHttpResponse(
				productRole1.getId()));
	}

	protected ProductRole testDeleteProductRoleBatch_addProductRole()
		throws Exception {

		return testDeleteProductRole_addProductRole();
	}

	protected void testDeleteProductRoleBatch_deleteProductRole(
			int expectedStatusCode, String externalReferenceCode, Long id)
		throws Exception {

		HttpInvoker.HttpResponse httpResponse =
			productRoleResource.deleteProductRoleBatchHttpResponse(
				null,
				JSONUtil.putAll(
					JSONUtil.put(
						"externalReferenceCode", () -> externalReferenceCode
					).put(
						"id", () -> id
					)));

		Assert.assertEquals(expectedStatusCode, httpResponse.getStatusCode());

		waitForFinish(
			"COMPLETED",
			JSONFactoryUtil.createJSONObject(httpResponse.getContent()));
	}

	@Test
	public void testGetProductByExternalReferenceCodeProductRolesPage()
		throws Exception {

		String externalReferenceCode =
			testGetProductByExternalReferenceCodeProductRolesPage_getExternalReferenceCode();
		String irrelevantExternalReferenceCode =
			testGetProductByExternalReferenceCodeProductRolesPage_getIrrelevantExternalReferenceCode();

		Page<ProductRole> page =
			productRoleResource.
				getProductByExternalReferenceCodeProductRolesPage(
					externalReferenceCode, Pagination.of(1, 10));

		long totalCount = page.getTotalCount();

		if (irrelevantExternalReferenceCode != null) {
			ProductRole irrelevantProductRole =
				testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
					irrelevantExternalReferenceCode,
					randomIrrelevantProductRole());

			page =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						irrelevantExternalReferenceCode,
						Pagination.of(1, (int)totalCount + 1));

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantProductRole, (List<ProductRole>)page.getItems());
			assertValid(
				page,
				testGetProductByExternalReferenceCodeProductRolesPage_getExpectedActions(
					irrelevantExternalReferenceCode));
		}

		ProductRole productRole1 =
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				externalReferenceCode, randomProductRole());

		ProductRole productRole2 =
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				externalReferenceCode, randomProductRole());

		page =
			productRoleResource.
				getProductByExternalReferenceCodeProductRolesPage(
					externalReferenceCode,
					Pagination.of(1, (int)totalCount + 2));

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(productRole1, (List<ProductRole>)page.getItems());
		assertContains(productRole2, (List<ProductRole>)page.getItems());
		assertValid(
			page,
			testGetProductByExternalReferenceCodeProductRolesPage_getExpectedActions(
				externalReferenceCode));

		productRoleResource.deleteProductRole(productRole1.getId());

		productRoleResource.deleteProductRole(productRole2.getId());
	}

	protected Map<String, Map<String, String>>
			testGetProductByExternalReferenceCodeProductRolesPage_getExpectedActions(
				String externalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetProductByExternalReferenceCodeProductRolesPageWithPagination()
		throws Exception {

		String externalReferenceCode =
			testGetProductByExternalReferenceCodeProductRolesPage_getExternalReferenceCode();

		Page<ProductRole> productRolesPage =
			productRoleResource.
				getProductByExternalReferenceCodeProductRolesPage(
					externalReferenceCode, null);

		int totalCount = GetterUtil.getInteger(
			productRolesPage.getTotalCount());

		ProductRole productRole1 =
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				externalReferenceCode, randomProductRole());

		ProductRole productRole2 =
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				externalReferenceCode, randomProductRole());

		ProductRole productRole3 =
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				externalReferenceCode, randomProductRole());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<ProductRole> page1 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(
							(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
							pageSizeLimit));

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(productRole1, (List<ProductRole>)page1.getItems());

			Page<ProductRole> page2 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(
							(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
							pageSizeLimit));

			assertContains(productRole2, (List<ProductRole>)page2.getItems());

			Page<ProductRole> page3 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(
							(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
							pageSizeLimit));

			assertContains(productRole3, (List<ProductRole>)page3.getItems());
		}
		else {
			Page<ProductRole> page1 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(1, totalCount + 2));

			List<ProductRole> productRoles1 =
				(List<ProductRole>)page1.getItems();

			Assert.assertEquals(
				productRoles1.toString(), totalCount + 2, productRoles1.size());

			Page<ProductRole> page2 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(2, totalCount + 2));

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<ProductRole> productRoles2 =
				(List<ProductRole>)page2.getItems();

			Assert.assertEquals(
				productRoles2.toString(), 1, productRoles2.size());

			Page<ProductRole> page3 =
				productRoleResource.
					getProductByExternalReferenceCodeProductRolesPage(
						externalReferenceCode,
						Pagination.of(1, (int)totalCount + 3));

			assertContains(productRole1, (List<ProductRole>)page3.getItems());
			assertContains(productRole2, (List<ProductRole>)page3.getItems());
			assertContains(productRole3, (List<ProductRole>)page3.getItems());
		}
	}

	protected ProductRole
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				String externalReferenceCode, ProductRole productRole)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetProductByExternalReferenceCodeProductRolesPage_getExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetProductByExternalReferenceCodeProductRolesPage_getIrrelevantExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testGetProductIdProductRolesPage() throws Exception {
		Long id = testGetProductIdProductRolesPage_getId();
		Long irrelevantId = testGetProductIdProductRolesPage_getIrrelevantId();

		Page<ProductRole> page =
			productRoleResource.getProductIdProductRolesPage(
				id, Pagination.of(1, 10));

		long totalCount = page.getTotalCount();

		if (irrelevantId != null) {
			ProductRole irrelevantProductRole =
				testGetProductIdProductRolesPage_addProductRole(
					irrelevantId, randomIrrelevantProductRole());

			page = productRoleResource.getProductIdProductRolesPage(
				irrelevantId, Pagination.of(1, (int)totalCount + 1));

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantProductRole, (List<ProductRole>)page.getItems());
			assertValid(
				page,
				testGetProductIdProductRolesPage_getExpectedActions(
					irrelevantId));
		}

		ProductRole productRole1 =
			testGetProductIdProductRolesPage_addProductRole(
				id, randomProductRole());

		ProductRole productRole2 =
			testGetProductIdProductRolesPage_addProductRole(
				id, randomProductRole());

		page = productRoleResource.getProductIdProductRolesPage(
			id, Pagination.of(1, (int)totalCount + 2));

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(productRole1, (List<ProductRole>)page.getItems());
		assertContains(productRole2, (List<ProductRole>)page.getItems());
		assertValid(
			page, testGetProductIdProductRolesPage_getExpectedActions(id));

		productRoleResource.deleteProductRole(productRole1.getId());

		productRoleResource.deleteProductRole(productRole2.getId());
	}

	protected Map<String, Map<String, String>>
			testGetProductIdProductRolesPage_getExpectedActions(Long id)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetProductIdProductRolesPageWithPagination()
		throws Exception {

		Long id = testGetProductIdProductRolesPage_getId();

		Page<ProductRole> productRolesPage =
			productRoleResource.getProductIdProductRolesPage(id, null);

		int totalCount = GetterUtil.getInteger(
			productRolesPage.getTotalCount());

		ProductRole productRole1 =
			testGetProductIdProductRolesPage_addProductRole(
				id, randomProductRole());

		ProductRole productRole2 =
			testGetProductIdProductRolesPage_addProductRole(
				id, randomProductRole());

		ProductRole productRole3 =
			testGetProductIdProductRolesPage_addProductRole(
				id, randomProductRole());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<ProductRole> page1 =
				productRoleResource.getProductIdProductRolesPage(
					id,
					Pagination.of(
						(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
						pageSizeLimit));

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(productRole1, (List<ProductRole>)page1.getItems());

			Page<ProductRole> page2 =
				productRoleResource.getProductIdProductRolesPage(
					id,
					Pagination.of(
						(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
						pageSizeLimit));

			assertContains(productRole2, (List<ProductRole>)page2.getItems());

			Page<ProductRole> page3 =
				productRoleResource.getProductIdProductRolesPage(
					id,
					Pagination.of(
						(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
						pageSizeLimit));

			assertContains(productRole3, (List<ProductRole>)page3.getItems());
		}
		else {
			Page<ProductRole> page1 =
				productRoleResource.getProductIdProductRolesPage(
					id, Pagination.of(1, totalCount + 2));

			List<ProductRole> productRoles1 =
				(List<ProductRole>)page1.getItems();

			Assert.assertEquals(
				productRoles1.toString(), totalCount + 2, productRoles1.size());

			Page<ProductRole> page2 =
				productRoleResource.getProductIdProductRolesPage(
					id, Pagination.of(2, totalCount + 2));

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<ProductRole> productRoles2 =
				(List<ProductRole>)page2.getItems();

			Assert.assertEquals(
				productRoles2.toString(), 1, productRoles2.size());

			Page<ProductRole> page3 =
				productRoleResource.getProductIdProductRolesPage(
					id, Pagination.of(1, (int)totalCount + 3));

			assertContains(productRole1, (List<ProductRole>)page3.getItems());
			assertContains(productRole2, (List<ProductRole>)page3.getItems());
			assertContains(productRole3, (List<ProductRole>)page3.getItems());
		}
	}

	protected ProductRole testGetProductIdProductRolesPage_addProductRole(
			Long id, ProductRole productRole)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetProductIdProductRolesPage_getId() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetProductIdProductRolesPage_getIrrelevantId()
		throws Exception {

		return null;
	}

	@Test
	public void testGetProductRole() throws Exception {
		ProductRole postProductRole = testGetProductRole_addProductRole();

		ProductRole getProductRole = productRoleResource.getProductRole(
			postProductRole.getId());

		assertEquals(postProductRole, getProductRole);
		assertValid(getProductRole);
	}

	@Test
	public void testVulcanCRUDItemDelegateGetItem() throws Exception {
		ProductRole postProductRole = testGetProductRole_addProductRole();

		ProductRole getProductRole = productRoleResource.getProductRole(
			postProductRole.getId());

		VulcanCRUDItemDelegate vulcanCRUDItemDelegate =
			_vulcanCRUDItemDelegateBuilderRegistry.builder(
				testCompany,
				"com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductRole"
			).acceptLanguage(
				new AcceptLanguage() {

					@Override
					public List<Locale> getLocales() {
						return Arrays.asList(LocaleUtil.getDefault());
					}

					@Override
					public String getPreferredLanguageId() {
						return LocaleUtil.toLanguageId(LocaleUtil.getDefault());
					}

					@Override
					public Locale getPreferredLocale() {
						return LocaleUtil.getDefault();
					}

				}
			).groupLocalService(
				_groupLocalService
			).httpServletRequest(
				testVulcanCRUDItemDelegate_getHttpServletRequest()
			).httpServletResponse(
				new MockHttpServletResponse()
			).resourceActionLocalService(
				_resourceActionLocalService
			).resourcePermissionLocalService(
				_resourcePermissionLocalService
			).roleLocalService(
				_roleLocalService
			).scopeChecker(
				_scopeChecker
			).uriInfo(
				testVulcanCRUDItemDelegate_getUriInfo()
			).user(
				testVulcanCRUDItemDelegate_getUser()
			).build();

		Object item = vulcanCRUDItemDelegate.getItem(postProductRole.getId());

		assertEquals(getProductRole, ProductRoleSerDes.toDTO(item.toString()));
	}

	protected HttpServletRequest
		testVulcanCRUDItemDelegate_getHttpServletRequest() {

		return new MockHttpServletRequest() {

			@Override
			public StringBuffer getRequestURL() {
				return new StringBuffer(
					StringBundler.concat(
						"http://localhost:",
						String.valueOf(PortalUtil.getPortalServerPort(false)),
						"/o/v1.0/", RandomTestUtil.randomString(), "/",
						RandomTestUtil.randomString()));
			}

		};
	}

	protected UriInfo testVulcanCRUDItemDelegate_getUriInfo() {
		String applicationPath = RandomTestUtil.randomString() + "/";
		String resourcePath = RandomTestUtil.randomString();

		return new UriInfo() {

			@Override
			public String getPath() {
				return resourcePath;
			}

			@Override
			public String getPath(boolean decode) {
				return getPath();
			}

			@Override
			public List<PathSegment> getPathSegments() {
				return Collections.emptyList();
			}

			@Override
			public List<PathSegment> getPathSegments(boolean decode) {
				return getPathSegments();
			}

			@Override
			public URI getRequestUri() {
				return URI.create(
					StringBundler.concat(
						"http://localhost:",
						PortalUtil.getPortalServerPort(false), "/o/",
						applicationPath, resourcePath));
			}

			@Override
			public UriBuilder getRequestUriBuilder() {
				return UriBuilder.fromUri(getRequestUri());
			}

			@Override
			public URI getAbsolutePath() {
				return getRequestUri();
			}

			@Override
			public UriBuilder getAbsolutePathBuilder() {
				return getRequestUriBuilder();
			}

			@Override
			public URI getBaseUri() {
				return URI.create(
					StringBundler.concat(
						"http://localhost:",
						PortalUtil.getPortalServerPort(false), "/o/",
						applicationPath));
			}

			@Override
			public UriBuilder getBaseUriBuilder() {
				return UriBuilder.fromUri(getBaseUri());
			}

			@Override
			public MultivaluedMap<String, String> getPathParameters() {
				return new MultivaluedHashMap<>();
			}

			@Override
			public MultivaluedMap<String, String> getPathParameters(
				boolean decode) {

				return getPathParameters();
			}

			@Override
			public MultivaluedMap<String, String> getQueryParameters() {
				return new MultivaluedHashMap<>();
			}

			@Override
			public MultivaluedMap<String, String> getQueryParameters(
				boolean decode) {

				return getQueryParameters();
			}

			@Override
			public List<String> getMatchedURIs() {
				return Collections.emptyList();
			}

			@Override
			public List<String> getMatchedURIs(boolean decode) {
				return getMatchedURIs();
			}

			@Override
			public List<Object> getMatchedResources() {
				return Collections.emptyList();
			}

			@Override
			public URI resolve(URI requestUri) {
				return getBaseUri().resolve(requestUri);
			}

			@Override
			public URI relativize(URI uri) {
				return getBaseUri().relativize(uri);
			}

		};
	}

	protected com.liferay.portal.kernel.model.User
		testVulcanCRUDItemDelegate_getUser() {

		return _testCompanyAdminUser;
	}

	protected ProductRole testGetProductRole_addProductRole() throws Exception {
		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetProductRole() throws Exception {
		ProductRole productRole = testGraphQLGetProductRole_addProductRole();

		// No namespace

		Assert.assertTrue(
			equals(
				productRole,
				ProductRoleSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"productRole",
								new HashMap<String, Object>() {
									{
										put("id", productRole.getId());
									}
								},
								getGraphQLFields())),
						"JSONObject/data", "Object/productRole"))));

		// Using the namespace headlessCommerceAdminCatalog_v1_0

		Assert.assertTrue(
			equals(
				productRole,
				ProductRoleSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"headlessCommerceAdminCatalog_v1_0",
								new GraphQLField(
									"productRole",
									new HashMap<String, Object>() {
										{
											put("id", productRole.getId());
										}
									},
									getGraphQLFields()))),
						"JSONObject/data",
						"JSONObject/headlessCommerceAdminCatalog_v1_0",
						"Object/productRole"))));
	}

	@Test
	public void testGraphQLGetProductRoleNotFound() throws Exception {
		Long irrelevantId = RandomTestUtil.randomLong();

		// No namespace

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"productRole",
						new HashMap<String, Object>() {
							{
								put("id", irrelevantId);
							}
						},
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));

		// Using the namespace headlessCommerceAdminCatalog_v1_0

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"headlessCommerceAdminCatalog_v1_0",
						new GraphQLField(
							"productRole",
							new HashMap<String, Object>() {
								{
									put("id", irrelevantId);
								}
							},
							getGraphQLFields()))),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	protected ProductRole testGraphQLGetProductRole_addProductRole()
		throws Exception {

		return testGraphQLProductRole_addProductRole();
	}

	@Test
	public void testBatchEngineDeleteImportTask() throws Exception {
		ProductRole productRole1 =
			testBatchEngineDeleteImportTask_addProductRole();

		testBatchEngineDeleteImportTask_deleteProductRole(
			200, null, productRole1.getId());

		assertHttpResponseStatusCode(
			404,
			productRoleResource.getProductRoleHttpResponse(
				productRole1.getId()));
	}

	protected ProductRole testBatchEngineDeleteImportTask_addProductRole()
		throws Exception {

		return testDeleteProductRole_addProductRole();
	}

	protected void testBatchEngineDeleteImportTask_deleteProductRole(
			int expectedStatusCode, String externalReferenceCode, Long id,
			String... parameters)
		throws Exception {

		ImportTaskResource importTaskResource = ImportTaskResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).parameters(
			parameters
		).build();

		HttpResponse httpResponse =
			importTaskResource.deleteImportTaskHttpResponse(
				"com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductRole",
				null, null, null, null,
				JSONUtil.putAll(
					JSONUtil.put(
						"externalReferenceCode", () -> externalReferenceCode
					).put(
						"id", () -> id
					)));

		Assert.assertEquals(expectedStatusCode, httpResponse.getStatusCode());

		if (expectedStatusCode == 200) {
			waitForFinish(
				"COMPLETED",
				JSONFactoryUtil.createJSONObject(httpResponse.getContent()));
		}
	}

	protected ProductRole testGraphQLProductRole_addProductRole()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected void assertContains(
		ProductRole productRole, List<ProductRole> productRoles) {

		boolean contains = false;

		for (ProductRole item : productRoles) {
			if (equals(productRole, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			productRoles + " does not contain " + productRole, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		ProductRole productRole1, ProductRole productRole2) {

		Assert.assertTrue(
			productRole1 + " does not equal " + productRole2,
			equals(productRole1, productRole2));
	}

	protected void assertEquals(
		List<ProductRole> productRoles1, List<ProductRole> productRoles2) {

		Assert.assertEquals(productRoles1.size(), productRoles2.size());

		for (int i = 0; i < productRoles1.size(); i++) {
			ProductRole productRole1 = productRoles1.get(i);
			ProductRole productRole2 = productRoles2.get(i);

			assertEquals(productRole1, productRole2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<ProductRole> productRoles1, List<ProductRole> productRoles2) {

		Assert.assertEquals(productRoles1.size(), productRoles2.size());

		for (ProductRole productRole1 : productRoles1) {
			boolean contains = false;

			for (ProductRole productRole2 : productRoles2) {
				if (equals(productRole1, productRole2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				productRoles2 + " does not contain " + productRole1, contains);
		}
	}

	protected void assertValid(ProductRole productRole) throws Exception {
		boolean valid = true;

		if (productRole.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (productRole.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (productRole.getName() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("roleId", additionalAssertFieldName)) {
				if (productRole.getRoleId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("roleType", additionalAssertFieldName)) {
				if (productRole.getRoleType() == null) {
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

	protected void assertValid(Page<ProductRole> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<ProductRole> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<ProductRole> productRoles = page.getItems();

		int size = productRoles.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		graphQLFields.add(new GraphQLField("externalReferenceCode"));

		graphQLFields.add(new GraphQLField("id"));

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.commerce.admin.catalog.dto.v1_0.
						ProductRole.class)) {

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
		ProductRole productRole1, ProductRole productRole2) {

		if (productRole1 == productRole2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						productRole1.getExternalReferenceCode(),
						productRole2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						productRole1.getId(), productRole2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						productRole1.getName(), productRole2.getName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("roleId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						productRole1.getRoleId(), productRole2.getRoleId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("roleType", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						productRole1.getRoleType(),
						productRole2.getRoleType())) {

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

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_productRoleResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_productRoleResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator, ProductRole productRole) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("externalReferenceCode")) {
			Object object = productRole.getExternalReferenceCode();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("id")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("name")) {
			Object object = productRole.getName();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("roleId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("roleType")) {
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
		httpInvoker.path(
			"http://localhost:" + PortalUtil.getPortalServerPort(false) +
				"/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

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

	protected ProductRole randomProductRole() throws Exception {
		return new ProductRole() {
			{
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				roleId = RandomTestUtil.randomLong();
			}
		};
	}

	protected ProductRole randomIrrelevantProductRole() throws Exception {
		ProductRole randomIrrelevantProductRole = randomProductRole();

		return randomIrrelevantProductRole;
	}

	protected ProductRole randomPatchProductRole() throws Exception {
		return randomProductRole();
	}

	protected final JSONObject waitForFinish(
			String expectedExecuteStatus, JSONObject jsonObject)
		throws Exception {

		while (true) {
			ImportTask importTask = importTaskResource.getImportTask(
				jsonObject.getLong("id"));

			ImportTask.ExecuteStatus executeStatus =
				importTask.getExecuteStatus();

			if (StringUtil.equals(executeStatus.getValue(), "COMPLETED") ||
				StringUtil.equals(executeStatus.getValue(), "FAILED")) {

				Assert.assertEquals(
					expectedExecuteStatus, executeStatus.getValue());

				return jsonObject;
			}
		}
	}

	protected ProductRoleResource productRoleResource;
	protected ImportTaskResource importTaskResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
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

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
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
		LogFactoryUtil.getLog(BaseProductRoleResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private com.liferay.headless.commerce.admin.catalog.resource.v1_0.
		ProductRoleResource _productRoleResource;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private ResourceActionLocalService _resourceActionLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private ScopeChecker _scopeChecker;

	@Inject
	private UserLocalService _userLocalService;

	@Inject
	private VulcanCRUDItemDelegateBuilderRegistry
		_vulcanCRUDItemDelegateBuilderRegistry;

}
// LIFERAY-REST-BUILDER-HASH:1458959891
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.servlet;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.pim.site.initializer.connector.PIMConnector;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorRegistry;
import com.liferay.site.pim.site.initializer.exception.PIMConnectorFieldMappingException;

import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Andrea Sbarra
 */
public class ExportPIMProductsServletTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtil.setFieldValue(
			_exportPIMProductsServlet, "_objectEntryLocalService",
			_objectEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_exportPIMProductsServlet, "_pimConnectorRegistry",
			_pimConnectorRegistry);
	}

	@Test
	public void testDoGet() throws Exception {
		_testDoGet();
		_testDoGetWithMissingObjectEntry();
		_testDoGetWithMissingPIMConnector();
		_testDoGetWithPIMConnectorFieldMappingException();
		_testDoGetWithPortalException();
	}

	private MockHttpServletResponse _getMockHttpServletResponse(
			long objectEntryId)
		throws Exception {

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter(
			"objectEntryId", String.valueOf(objectEntryId));

		_exportPIMProductsServlet.doGet(
			mockHttpServletRequest, mockHttpServletResponse);

		return mockHttpServletResponse;
	}

	private ObjectEntry _mockObjectEntry(long objectEntryId) throws Exception {
		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(objectEntryId)
		).thenReturn(
			objectEntry
		);

		Mockito.when(
			_objectEntryLocalService.getValues(objectEntry)
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"key", _KEY
			).build()
		);

		return objectEntry;
	}

	private PIMConnector _mockPIMConnector() {
		PIMConnector pimConnector = Mockito.mock(PIMConnector.class);

		Mockito.when(
			_pimConnectorRegistry.getPIMConnector(_KEY)
		).thenReturn(
			pimConnector
		);

		return pimConnector;
	}

	private void _testDoGet() throws Exception {
		long objectEntryId = RandomTestUtil.randomLong();

		ObjectEntry objectEntry = _mockObjectEntry(objectEntryId);

		PIMConnector pimConnector = _mockPIMConnector();

		Mockito.when(
			pimConnector.exportProducts(objectEntry)
		).thenReturn(
			"[]"
		);

		MockHttpServletResponse mockHttpServletResponse =
			_getMockHttpServletResponse(objectEntryId);

		Assert.assertEquals("[]", mockHttpServletResponse.getContentAsString());
		Assert.assertEquals(
			ContentTypes.APPLICATION_JSON,
			mockHttpServletResponse.getContentType());
		Assert.assertEquals(
			HttpHeaders.CONTENT_DISPOSITION_ATTACHMENT +
				"; filename=\"pim-products.json\"",
			mockHttpServletResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION));
		Assert.assertEquals(
			HttpServletResponse.SC_OK, mockHttpServletResponse.getStatus());
	}

	private void _testDoGetWithMissingObjectEntry() throws Exception {
		MockHttpServletResponse mockHttpServletResponse =
			_getMockHttpServletResponse(RandomTestUtil.randomLong());

		Assert.assertEquals(
			JSONUtil.put(
				"error", "Unable to get the PIM connector"
			).toString(),
			mockHttpServletResponse.getContentAsString());
		Assert.assertNull(
			mockHttpServletResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION));
		Assert.assertEquals(
			HttpServletResponse.SC_BAD_REQUEST,
			mockHttpServletResponse.getStatus());
	}

	private void _testDoGetWithMissingPIMConnector() throws Exception {
		long objectEntryId = RandomTestUtil.randomLong();

		_mockObjectEntry(objectEntryId);

		Mockito.when(
			_pimConnectorRegistry.getPIMConnector(_KEY)
		).thenReturn(
			null
		);

		MockHttpServletResponse mockHttpServletResponse =
			_getMockHttpServletResponse(objectEntryId);

		Assert.assertEquals(
			JSONUtil.put(
				"error", "Unable to get the PIM connector with key " + _KEY
			).toString(),
			mockHttpServletResponse.getContentAsString());
		Assert.assertEquals(
			HttpServletResponse.SC_BAD_REQUEST,
			mockHttpServletResponse.getStatus());
	}

	private void _testDoGetWithPIMConnectorFieldMappingException()
		throws Exception {

		long objectEntryId = RandomTestUtil.randomLong();

		ObjectEntry objectEntry = _mockObjectEntry(objectEntryId);

		PIMConnector pimConnector = _mockPIMConnector();

		Mockito.when(
			pimConnector.exportProducts(objectEntry)
		).thenThrow(
			new PIMConnectorFieldMappingException(
				"The required fields are not mapped")
		);

		MockHttpServletResponse mockHttpServletResponse =
			_getMockHttpServletResponse(objectEntryId);

		Assert.assertEquals(
			JSONUtil.put(
				"error", "The required fields are not mapped"
			).toString(),
			mockHttpServletResponse.getContentAsString());
		Assert.assertEquals(
			HttpServletResponse.SC_BAD_REQUEST,
			mockHttpServletResponse.getStatus());
	}

	private void _testDoGetWithPortalException() throws Exception {
		long objectEntryId = RandomTestUtil.randomLong();

		ObjectEntry objectEntry = _mockObjectEntry(objectEntryId);

		PIMConnector pimConnector = _mockPIMConnector();

		Mockito.when(
			pimConnector.exportProducts(objectEntry)
		).thenThrow(
			new PortalException()
		);

		MockHttpServletResponse mockHttpServletResponse =
			_getMockHttpServletResponse(objectEntryId);

		Assert.assertEquals(
			JSONUtil.put(
				"error", "Unable to export the products"
			).toString(),
			mockHttpServletResponse.getContentAsString());
		Assert.assertEquals(
			HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			mockHttpServletResponse.getStatus());
	}

	private static final String _KEY = "liferay-commerce";

	private final ExportPIMProductsServlet _exportPIMProductsServlet =
		new ExportPIMProductsServlet();
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);
	private final PIMConnectorRegistry _pimConnectorRegistry = Mockito.mock(
		PIMConnectorRegistry.class);

}
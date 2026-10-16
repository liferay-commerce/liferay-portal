/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brian I. Kim
 */
public class DSEnvelopeManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testVoidDSEnvelope() throws Exception {
		_mockDSHttp(JSONUtil.put("envelopeId", "envelopeId"));

		_dsEnvelopeManagerImpl.voidDSEnvelope(1, 2, "envelopeId", "reason");

		_mockDSHttp(
			JSONUtil.put(
				"errorCode", "ENVELOPE_CANNOT_VOID_INVALID_STATE"
			).put(
				"message", "Only envelopes in the sent state may be voided."
			));

		try {
			_dsEnvelopeManagerImpl.voidDSEnvelope(1, 2, "envelopeId", "reason");

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertEquals(
				"Unable to void envelope envelopeId: Only envelopes in the " +
					"sent state may be voided.",
				portalException.getMessage());
		}
	}

	private void _mockDSHttp(JSONObject jsonObject) {
		DSHttp dsHttp = Mockito.mock(DSHttp.class);

		Mockito.when(
			dsHttp.put(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(JSONObject.class))
		).thenReturn(
			jsonObject
		);

		ReflectionTestUtil.setFieldValue(
			_dsEnvelopeManagerImpl, "_dsHttp", dsHttp);
	}

	private final DSEnvelopeManagerImpl _dsEnvelopeManagerImpl =
		new DSEnvelopeManagerImpl();

}
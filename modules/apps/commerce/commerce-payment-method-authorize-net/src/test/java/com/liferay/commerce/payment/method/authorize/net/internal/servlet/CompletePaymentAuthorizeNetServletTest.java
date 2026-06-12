/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.payment.method.authorize.net.internal.servlet;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Danny Situ
 */
public class CompletePaymentAuthorizeNetServletTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_bytes = RandomTestUtil.randomBytes();
		_signatureKey = RandomTestUtil.randomString();
	}

	@Test
	public void testVerifySignature() throws Exception {
		String signature = ReflectionTestUtil.invoke(
			_completePaymentAuthorizeNetServlet, "_generateHMAC",
			new Class<?>[] {byte[].class, String.class}, _bytes, _signatureKey);

		Assert.assertTrue(
			ReflectionTestUtil.invoke(
				_completePaymentAuthorizeNetServlet, "_verifySignature",
				new Class<?>[] {byte[].class, String.class, String.class},
				_bytes, "sha512=" + StringUtil.toUpperCase(signature),
				_signatureKey));
	}

	@Test
	public void testVerifySignatureWithInvalidSignature() throws Exception {
		Assert.assertFalse(
			ReflectionTestUtil.invoke(
				_completePaymentAuthorizeNetServlet, "_verifySignature",
				new Class<?>[] {byte[].class, String.class, String.class},
				_bytes, "sha512=" + RandomTestUtil.randomString(),
				_signatureKey));
	}

	@Test
	public void testVerifySignatureWithNullSignatureKey() throws Exception {
		Assert.assertFalse(
			ReflectionTestUtil.invoke(
				_completePaymentAuthorizeNetServlet, "_verifySignature",
				new Class<?>[] {byte[].class, String.class, String.class},
				_bytes, "sha512=" + RandomTestUtil.randomString(), null));
	}

	private byte[] _bytes;
	private final CompletePaymentAuthorizeNetServlet
		_completePaymentAuthorizeNetServlet =
			new CompletePaymentAuthorizeNetServlet();
	private String _signatureKey;

}
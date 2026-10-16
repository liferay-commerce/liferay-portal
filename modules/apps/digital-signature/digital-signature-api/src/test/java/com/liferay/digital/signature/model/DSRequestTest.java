/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.model;

import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Brian I. Kim
 */
public class DSRequestTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsSignatureRequired() {
		long userId = RandomTestUtil.randomLong();

		DSRequest dsRequest = _createDSRequest(
			DSRequestConstants.STATUS_COMPLETED,
			DSRequestConstants.STATUS_COMPLETED, userId);

		Assert.assertFalse(dsRequest.isSignatureRequired(userId));

		dsRequest = _createDSRequest(
			DSRequestConstants.STATUS_CREATED, DSRequestConstants.STATUS_SENT,
			userId);

		Assert.assertFalse(dsRequest.isSignatureRequired(userId));

		dsRequest = _createDSRequest(
			DSRequestConstants.STATUS_SENT, DSRequestConstants.STATUS_SENT,
			userId);

		Assert.assertTrue(dsRequest.isSignatureRequired(userId));
		Assert.assertFalse(
			dsRequest.isSignatureRequired(RandomTestUtil.randomLong()));
	}

	private DSRequest _createDSRequest(
		String recipientStatus, String requestStatus, long userId) {

		return new DSRequest(
			RandomTestUtil.randomLong(), new Date(),
			RandomTestUtil.randomLong(),
			Collections.singletonList(
				new DSRequestRecipient(
					HashMapBuilder.<String, Serializable>put(
						"r_userToDSRequestRecipients_userId", userId
					).put(
						"requestRecipientStatus", recipientStatus
					).build())),
			RandomTestUtil.randomString(), Collections.emptyList(),
			RandomTestUtil.randomString(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(),
			HashMapBuilder.<String, Serializable>put(
				"requestStatus", requestStatus
			).build());
	}

}
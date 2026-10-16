/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.url.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.constants.DigitalSignaturePortletKeys;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Danny Situ
 */
@RunWith(Arquillian.class)
public class SignDSURLProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetModalURL() throws Exception {
		String backURL = "/" + RandomTestUtil.randomString();

		String modalURL = _signDSURLProvider.getModalURL(
			TestPropsValues.getCompanyId(), _group.getGroupId(), backURL,
			RandomTestUtil.randomLong());

		String portletNamespace = _portal.getPortletNamespace(
			DigitalSignaturePortletKeys.SIGN_DIGITAL_SIGNATURE);

		Assert.assertEquals(
			backURL,
			URLCodec.decodeURL(
				HttpComponentsUtil.getParameter(
					modalURL, portletNamespace + "backURL", false)));

		Assert.assertEquals(
			LiferayWindowState.POP_UP.toString(),
			HttpComponentsUtil.getParameter(modalURL, "p_p_state", false));
	}

	@Test
	public void testGetURL() throws Exception {
		long dsRequestId = RandomTestUtil.randomLong();

		String url = _signDSURLProvider.getURL(
			TestPropsValues.getCompanyId(), _group.getGroupId(), dsRequestId);

		Assert.assertTrue(
			url,
			url.endsWith(
				"/c/digital_signature/open_ds_request?dsRequestId=" +
					dsRequestId));
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Portal _portal;

	@Inject
	private SignDSURLProvider _signDSURLProvider;

}
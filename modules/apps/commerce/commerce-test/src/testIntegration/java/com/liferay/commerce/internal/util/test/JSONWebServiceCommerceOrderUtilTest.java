/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.util.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderHttpHelper;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matyas Wollner
 */
@RunWith(Arquillian.class)
public class JSONWebServiceCommerceOrderUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		_commerceOrder = _commerceOrderLocalService.addCommerceOrder(
			TestPropsValues.getUserId(), commerceChannel.getGroupId(),
			AccountConstants.ACCOUNT_ENTRY_ID_GUEST, commerceCurrency.getCode(),
			0);
	}

	@Test
	public void testCheckGuestCommerceOrder() throws Exception {
		String json = _http.URLtoString(_getOptions());

		Assert.assertFalse(json.contains(_commerceOrder.getUuid()));

		Http.Options options = _getOptions();

		options.addHeader("Commerce-Cart-UUID", _commerceOrder.getUuid());

		json = _http.URLtoString(options);

		Assert.assertTrue(json.contains(_commerceOrder.getUuid()));

		options = _getOptions();

		Cookie cookie = new Cookie(
			_commerceOrderHttpHelper.getCookieName(_commerceOrder.getGroupId()),
			_commerceOrder.getUuid() + StringPool.PIPE +
				RandomTestUtil.randomString());

		cookie.setDomain("localhost");
		cookie.setPath(StringPool.SLASH);

		options.setCookies(new Cookie[] {cookie});

		json = _http.URLtoString(options);

		Assert.assertTrue(json.contains(_commerceOrder.getUuid()));
	}

	private Http.Options _getOptions() {
		Http.Options options = new Http.Options();

		options.setLocation(
			StringBundler.concat(
				"http://localhost:", PortalUtil.getPortalServerPort(false),
				"/api/jsonws/commerce.commerceorder/get-commerce-order",
				"?commerceOrderId=", _commerceOrder.getCommerceOrderId()));

		return options;
	}

	private CommerceOrder _commerceOrder;

	@Inject
	private CommerceOrderHttpHelper _commerceOrderHttpHelper;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Inject
	private Http _http;

}
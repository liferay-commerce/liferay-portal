/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.catalog.web.internal.display.context;

import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Michele Vigilante
 */
public class CommerceCatalogDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		Mockito.when(
			_activeCommerceCurrency.isActive()
		).thenReturn(
			true
		);

		Mockito.when(
			_commerceCurrencyLocalService.getCommerceCurrencies(
				_COMPANY_ID, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)
		).thenReturn(
			List.of(_activeCommerceCurrency)
		);

		Mockito.when(
			_themeDisplay.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);
	}

	@Test
	public void testGetCommerceCatalogInactiveCommerceCurrency()
		throws Exception {

		_testGetCommerceCatalogInactiveCommerceCurrencyWithActiveCommerceCurrency();
		_testGetCommerceCatalogInactiveCommerceCurrencyWithInactiveCommerceCurrency();
		_testGetCommerceCatalogInactiveCommerceCurrencyWithoutCommerceCatalog();
	}

	@Test
	public void testGetCommerceCurrencies() throws Exception {
		_testGetCommerceCurrenciesWithActiveCommerceCurrency();
		_testGetCommerceCurrenciesWithInactiveCommerceCurrency();
		_testGetCommerceCurrenciesWithoutCommerceCatalog();
	}

	private CommerceCatalogDisplayContext _getCommerceCatalogDisplayContext(
			CommerceCurrency commerceCurrency)
		throws Exception {

		long commerceCatalogId = RandomTestUtil.randomLong();

		Mockito.when(
			_commerceCatalogService.fetchCommerceCatalog(commerceCatalogId)
		).thenReturn(
			_commerceCatalog
		);

		String commerceCurrencyCode = RandomTestUtil.randomString();

		Mockito.when(
			_commerceCatalog.getCommerceCurrencyCode()
		).thenReturn(
			commerceCurrencyCode
		);

		Mockito.when(
			_commerceCurrencyLocalService.fetchCommerceCurrency(
				_COMPANY_ID, commerceCurrencyCode)
		).thenReturn(
			commerceCurrency
		);

		return _getCommerceCatalogDisplayContext(commerceCatalogId);
	}

	private CommerceCatalogDisplayContext _getCommerceCatalogDisplayContext(
		long commerceCatalogId) {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _themeDisplay);
		mockHttpServletRequest.setParameter(
			"commerceCatalogId", String.valueOf(commerceCatalogId));

		return new CommerceCatalogDisplayContext(
			null, null, mockHttpServletRequest, null, _commerceCatalogService,
			null, _commerceCurrencyLocalService, null, null, null, null, null,
			null);
	}

	private void _testGetCommerceCatalogInactiveCommerceCurrencyWithActiveCommerceCurrency()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(_activeCommerceCurrency);

		Assert.assertNull(
			commerceCatalogDisplayContext.
				getCommerceCatalogInactiveCommerceCurrency());
	}

	private void _testGetCommerceCatalogInactiveCommerceCurrencyWithInactiveCommerceCurrency()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(_inactiveCommerceCurrency);

		Assert.assertEquals(
			_inactiveCommerceCurrency,
			commerceCatalogDisplayContext.
				getCommerceCatalogInactiveCommerceCurrency());
	}

	private void _testGetCommerceCatalogInactiveCommerceCurrencyWithoutCommerceCatalog()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(0);

		Assert.assertNull(
			commerceCatalogDisplayContext.
				getCommerceCatalogInactiveCommerceCurrency());
	}

	private void _testGetCommerceCurrenciesWithActiveCommerceCurrency()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(_activeCommerceCurrency);

		Assert.assertEquals(
			List.of(_activeCommerceCurrency),
			commerceCatalogDisplayContext.getCommerceCurrencies());
	}

	private void _testGetCommerceCurrenciesWithInactiveCommerceCurrency()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(_inactiveCommerceCurrency);

		Assert.assertEquals(
			List.of(_inactiveCommerceCurrency, _activeCommerceCurrency),
			commerceCatalogDisplayContext.getCommerceCurrencies());
	}

	private void _testGetCommerceCurrenciesWithoutCommerceCatalog()
		throws Exception {

		CommerceCatalogDisplayContext commerceCatalogDisplayContext =
			_getCommerceCatalogDisplayContext(0);

		Assert.assertEquals(
			List.of(_activeCommerceCurrency),
			commerceCatalogDisplayContext.getCommerceCurrencies());
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	@Mock
	private CommerceCurrency _activeCommerceCurrency;

	@Mock
	private CommerceCatalog _commerceCatalog;

	@Mock
	private CommerceCatalogService _commerceCatalogService;

	@Mock
	private CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Mock
	private CommerceCurrency _inactiveCommerceCurrency;

	@Mock
	private ThemeDisplay _themeDisplay;

}
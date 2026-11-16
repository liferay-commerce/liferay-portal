/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shipping.engine.fixed.web.internal.frontend.data.set.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.FDSDataProviderRegistry;
import com.liferay.frontend.data.set.provider.search.FDSKeywordsFactory;
import com.liferay.frontend.data.set.provider.search.FDSKeywordsFactoryRegistry;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class CommerceShippingFixedOptionFDSDataProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testGetItems() throws Exception {
		frutillaRule.scenario(
			"The shipping options are sorted by priority"
		).given(
			"A shipping method with two shipping options of different " +
				"priorities"
		).when(
			"The shipping options are listed"
		).then(
			"The priority sort lists the higher priority first, and the " +
				"reversed priority sort lists the lower priority first"
		);

		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());

		CommerceShippingMethod commerceShippingMethod =
			CommerceTestUtil.addCommerceShippingMethod(
				TestPropsValues.getUserId(), commerceChannel.getGroupId());

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter(
			"commerceShippingMethodId",
			String.valueOf(
				commerceShippingMethod.getCommerceShippingMethodId()));

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(group.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.US);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		CommerceShippingFixedOption commerceShippingFixedOption1 =
			_addCommerceShippingFixedOption(commerceShippingMethod, 2);
		CommerceShippingFixedOption commerceShippingFixedOption2 =
			_addCommerceShippingFixedOption(commerceShippingMethod, 1);

		_assertGetItems(
			mockHttpServletRequest, false, commerceShippingFixedOption1,
			commerceShippingFixedOption2);
		_assertGetItems(
			mockHttpServletRequest, true, commerceShippingFixedOption2,
			commerceShippingFixedOption1);
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private CommerceShippingFixedOption _addCommerceShippingFixedOption(
			CommerceShippingMethod commerceShippingMethod, double priority)
		throws Exception {

		return _commerceShippingFixedOptionLocalService.
			addCommerceShippingFixedOption(
				TestPropsValues.getUserId(),
				commerceShippingMethod.getGroupId(),
				commerceShippingMethod.getCommerceShippingMethodId(),
				BigDecimal.valueOf(RandomTestUtil.randomDouble()),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), priority);
	}

	private void _assertGetItems(
			MockHttpServletRequest mockHttpServletRequest, boolean reverse,
			CommerceShippingFixedOption... expectedCommerceShippingFixedOptions)
		throws Exception {

		FDSDataProvider<?> fdsDataProvider =
			_fdsDataProviderRegistry.getFDSDataProvider(_FDS_NAME);

		FDSKeywordsFactory fdsKeywordsFactory =
			_fdsKeywordsFactoryRegistry.getFDSKeywordsFactory(_FDS_NAME);

		List<?> items = fdsDataProvider.getItems(
			fdsKeywordsFactory.create(mockHttpServletRequest),
			new FDSPagination() {

				@Override
				public int getEndPosition() {
					return expectedCommerceShippingFixedOptions.length;
				}

				@Override
				public int getPage() {
					return 1;
				}

				@Override
				public int getPageSize() {
					return expectedCommerceShippingFixedOptions.length;
				}

				@Override
				public int getStartPosition() {
					return 0;
				}

			},
			mockHttpServletRequest,
			SortFactoryUtil.create("priority", reverse));

		Assert.assertEquals(
			TransformUtil.transformToList(
				expectedCommerceShippingFixedOptions,
				CommerceShippingFixedOption::getPriority),
			TransformUtil.transform(
				items, item -> BeanPropertiesUtil.getDouble(item, "priority")));
		Assert.assertEquals(
			TransformUtil.transformToList(
				expectedCommerceShippingFixedOptions,
				CommerceShippingFixedOption::getCommerceShippingFixedOptionId),
			TransformUtil.transform(
				items,
				item -> BeanPropertiesUtil.getLong(
					item, "shippingFixedOptionId")));
	}

	private static final String _FDS_NAME =
		CommercePortletKeys.COMMERCE_SHIPPING_METHODS + "-shippingFixedOptions";

	@Inject
	private CommerceShippingFixedOptionLocalService
		_commerceShippingFixedOptionLocalService;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private FDSDataProviderRegistry _fdsDataProviderRegistry;

	@Inject
	private FDSKeywordsFactoryRegistry _fdsKeywordsFactoryRegistry;

}
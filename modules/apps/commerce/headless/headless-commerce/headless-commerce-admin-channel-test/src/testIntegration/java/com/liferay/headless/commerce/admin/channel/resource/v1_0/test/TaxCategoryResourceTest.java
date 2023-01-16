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

package com.liferay.headless.commerce.admin.channel.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPTaxCategory;
import com.liferay.commerce.product.service.CPTaxCategoryLocalService;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.TaxCategory;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class TaxCategoryResourceTest extends BaseTaxCategoryResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"description", "name"};
	}

	@Override
	protected TaxCategory randomTaxCategory() throws Exception {
		return new TaxCategory() {
			{
				description = LanguageUtils.getLanguageIdMap(
					RandomTestUtil.randomLocaleStringMap());
				groupId = testGroup.getGroupId();
				id = RandomTestUtil.randomLong();
				name = LanguageUtils.getLanguageIdMap(
					RandomTestUtil.randomLocaleStringMap());
			}
		};
	}

	@Override
	protected TaxCategory testGetTaxCategoriesPage_addTaxCategory(
			TaxCategory taxCategory)
		throws Exception {

		return _addCPTaxCategory(taxCategory);
	}

	@Override
	protected TaxCategory testGetTaxCategory_addTaxCategory() throws Exception {
		return _addCPTaxCategory(randomTaxCategory());
	}

	@Override
	protected TaxCategory testGraphQLTaxCategory_addTaxCategory()
		throws Exception {

		return _addCPTaxCategory(randomTaxCategory());
	}

	private TaxCategory _addCPTaxCategory(TaxCategory taxCategory)
		throws Exception {

		CPTaxCategory cpTaxCategory =
			_cpTaxCategoryLocalService.addCPTaxCategory(
				RandomTestUtil.randomString(),
				LanguageUtils.getLocalizedMap(taxCategory.getName()),
				LanguageUtils.getLocalizedMap(taxCategory.getDescription()),
				_serviceContext);

		_cpTaxCategories.add(cpTaxCategory);

		return new TaxCategory() {
			{
				description = taxCategory.getDescription();
				groupId = taxCategory.getGroupId();
				id = cpTaxCategory.getCPTaxCategoryId();
				name = taxCategory.getName();
			}
		};
	}

	@Inject
	private static CPTaxCategoryLocalService _cpTaxCategoryLocalService;

	@DeleteAfterTestRun
	private final List<CPTaxCategory> _cpTaxCategories = new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
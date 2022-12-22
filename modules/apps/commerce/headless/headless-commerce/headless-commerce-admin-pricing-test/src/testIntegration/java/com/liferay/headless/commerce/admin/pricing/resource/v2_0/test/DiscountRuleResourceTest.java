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

package com.liferay.headless.commerce.admin.pricing.resource.v2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.constants.CommerceDiscountRuleConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.DiscountRule;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class DiscountRuleResourceTest extends BaseDiscountRuleResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_commerceDiscount =
			_commerceDiscountLocalService.addOrUpdateCommerceDiscount(
				RandomTestUtil.randomString(), _user.getUserId(), 0,
				RandomTestUtil.randomString(),
				CommerceDiscountConstants.TARGET_PRODUCTS, false, null, false,
				BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO,
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED, 0, true, 1,
				1, 2022, 12, 0, 0, 0, 0, 0, 0, true,
				ServiceContextTestUtil.getServiceContext(
					testCompany.getCompanyId(), testGroup.getGroupId(),
					_user.getUserId()));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"discountId", "name", "type"};
	}

	@Override
	protected Collection<EntityField> getEntityFields() throws Exception {
		try {
			return super.getEntityFields();
		}
		catch (NullPointerException nullPointerException) {
			Map<String, EntityField> entityFieldsMap = new HashMap<>();

			return entityFieldsMap.values();
		}
	}

	@Override
	protected DiscountRule randomDiscountRule() throws Exception {
		return new DiscountRule() {
			{
				discountId = _commerceDiscount.getCommerceDiscountId();
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				type = CommerceDiscountRuleConstants.TYPE_CART_TOTAL;
				typeSettings = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
			}
		};
	}

	@Override
	protected DiscountRule testDeleteDiscountRule_addDiscountRule()
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountRule());
	}

	@Override
	protected DiscountRule
			testGetDiscountByExternalReferenceCodeDiscountRulesPage_addDiscountRule(
				String externalReferenceCode, DiscountRule discountRule)
		throws Exception {

		return discountRuleResource.
			postDiscountByExternalReferenceCodeDiscountRule(
				externalReferenceCode, discountRule);
	}

	@Override
	protected String
			testGetDiscountByExternalReferenceCodeDiscountRulesPage_getExternalReferenceCode()
		throws Exception {

		return _commerceDiscount.getExternalReferenceCode();
	}

	@Override
	protected DiscountRule testGetDiscountIdDiscountRulesPage_addDiscountRule(
			Long id, DiscountRule discountRule)
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			id, discountRule);
	}

	@Override
	protected Long testGetDiscountIdDiscountRulesPage_getId() throws Exception {
		return _commerceDiscount.getCommerceDiscountId();
	}

	@Override
	protected DiscountRule testGetDiscountRule_addDiscountRule()
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountRule());
	}

	@Override
	protected DiscountRule testGraphQLDiscountRule_addDiscountRule()
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountRule());
	}

	@Override
	protected DiscountRule testPatchDiscountRule_addDiscountRule()
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			_commerceDiscount.getCommerceDiscountId(), randomDiscountRule());
	}

	@Override
	protected DiscountRule
			testPostDiscountByExternalReferenceCodeDiscountRule_addDiscountRule(
				DiscountRule discountRule)
		throws Exception {

		return discountRuleResource.
			postDiscountByExternalReferenceCodeDiscountRule(
				_commerceDiscount.getExternalReferenceCode(), discountRule);
	}

	@Override
	protected DiscountRule testPostDiscountIdDiscountRule_addDiscountRule(
			DiscountRule discountRule)
		throws Exception {

		return discountRuleResource.postDiscountIdDiscountRule(
			_commerceDiscount.getCommerceDiscountId(), discountRule);
	}

	@Inject
	private static CommerceDiscountLocalService _commerceDiscountLocalService;

	@DeleteAfterTestRun
	private CommerceDiscount _commerceDiscount;

	@DeleteAfterTestRun
	private User _user;

}
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
import com.liferay.headless.commerce.admin.pricing.client.dto.v2_0.Discount;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;

import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class DiscountResourceTest extends BaseDiscountResourceTestCase {

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"active", "couponCode", "level", "limitationTimes",
			"limitationTimesPerAccount", "limitationType", "rulesConjunction",
			"title", "useCouponCode", "usePercentage"
		};
	}

	@Override
	protected Discount randomDiscount() throws Exception {
		return new Discount() {
			{
				active = RandomTestUtil.randomBoolean();
				couponCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				level = CommerceDiscountConstants.LEVEL_L1;
				limitationTimes = RandomTestUtil.randomInt();
				limitationTimesPerAccount = RandomTestUtil.randomInt();
				limitationType =
					CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED;
				neverExpire = true;
				numberOfUse = RandomTestUtil.randomInt();
				rulesConjunction = RandomTestUtil.randomBoolean();
				target = CommerceDiscountConstants.TARGET_PRODUCTS;
				title = StringUtil.toLowerCase(RandomTestUtil.randomString());
				useCouponCode = RandomTestUtil.randomBoolean();
				usePercentage = RandomTestUtil.randomBoolean();
			}
		};
	}

	@Override
	protected Discount testDeleteDiscount_addDiscount() throws Exception {
		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testDeleteDiscountByExternalReferenceCode_addDiscount()
		throws Exception {

		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testGetDiscount_addDiscount() throws Exception {
		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testGetDiscountByExternalReferenceCode_addDiscount()
		throws Exception {

		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testGetDiscountsPage_addDiscount(Discount discount)
		throws Exception {

		return discountResource.postDiscount(discount);
	}

	@Override
	protected Discount testGraphQLDiscount_addDiscount() throws Exception {
		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testPatchDiscount_addDiscount() throws Exception {
		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testPatchDiscountByExternalReferenceCode_addDiscount()
		throws Exception {

		return discountResource.postDiscount(randomDiscount());
	}

	@Override
	protected Discount testPostDiscount_addDiscount(Discount discount)
		throws Exception {

		return discountResource.postDiscount(discount);
	}

}
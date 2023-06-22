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

package com.liferay.dynamic.data.mapping.form.evaluator.internal.function;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mahmoud Azzam
 */
public class IsValidURLSeparatorFunctionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsValidURLSeparator() {
		IsValidURLSeparatorFunction isValidURLSeparatorFunction =
			new IsValidURLSeparatorFunction();

		Assert.assertTrue(isValidURLSeparatorFunction.apply("T"));

		Assert.assertTrue(isValidURLSeparatorFunction.apply("T-"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("-"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("~"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("d"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("b"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("w"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("字"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("字T"));

		Assert.assertFalse(isValidURLSeparatorFunction.apply("T字T"));
	}

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Balazs Breier
 */
public class PIMVariantUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetCombinations() {
		Assert.assertEquals(
			Arrays.asList(
				Arrays.asList("black", "l"), Arrays.asList("black", "xl"),
				Arrays.asList("white", "l"), Arrays.asList("white", "xl")),
			PIMVariantUtil.getCombinations(
				Arrays.asList(
					Arrays.asList("black", "white"),
					Arrays.asList("l", "xl"))));
	}

	@Test
	public void testGetSKU() {
		Set<String> skus = new HashSet<>();

		Assert.assertEquals(
			"LebronShortsWhiteXL",
			PIMVariantUtil.getSKU(
				Arrays.asList("White", "X L"), skus, "Lebron\tShorts"));

		skus.add("LebronShortsWhiteXL");

		Assert.assertEquals(
			"LebronShortsWhiteXL1",
			PIMVariantUtil.getSKU(
				Arrays.asList("White", "XL"), skus, "Lebron Shorts"));

		skus.add("LebronShortsWhiteXL1");

		Assert.assertEquals(
			"LebronShortsWhiteXL2",
			PIMVariantUtil.getSKU(
				Arrays.asList("White", "XL"), skus, "Lebron Shorts"));
	}

}
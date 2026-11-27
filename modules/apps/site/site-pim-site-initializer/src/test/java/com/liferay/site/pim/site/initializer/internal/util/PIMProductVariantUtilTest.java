/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Balazs Breier
 */
public class PIMProductVariantUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetListTypeEntryCombinations() {
		ListTypeEntry blueListTypeEntry = Mockito.mock(ListTypeEntry.class);
		ListTypeEntry extraLargeListTypeEntry = Mockito.mock(
			ListTypeEntry.class);
		ListTypeEntry largeListTypeEntry = Mockito.mock(ListTypeEntry.class);
		ListTypeEntry yellowListTypeEntry = Mockito.mock(ListTypeEntry.class);

		Assert.assertEquals(
			Arrays.asList(
				Arrays.asList(blueListTypeEntry, largeListTypeEntry),
				Arrays.asList(blueListTypeEntry, extraLargeListTypeEntry),
				Arrays.asList(yellowListTypeEntry, largeListTypeEntry),
				Arrays.asList(yellowListTypeEntry, extraLargeListTypeEntry)),
			PIMProductVariantUtil.getListTypeEntryCombinations(
				Arrays.asList(
					Arrays.asList(blueListTypeEntry, yellowListTypeEntry),
					Arrays.asList(
						largeListTypeEntry, extraLargeListTypeEntry))));
	}

	@Test
	public void testGetSKU() throws Exception {
		Language language = Mockito.mock(Language.class);

		Mockito.when(
			language.get(Mockito.any(Locale.class), Mockito.eq("copy"))
		).thenReturn(
			"Copy"
		);

		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(language);

		Set<String> skus = new HashSet<>();

		Assert.assertEquals(
			"RunningShortsYellowXL",
			PIMProductVariantUtil.getSKU(
				Arrays.asList("Yellow", "X L"), skus, "Running\tShorts"));

		skus.add("RunningShortsYellowXL");

		Assert.assertEquals(
			"RunningShortsYellowXL (Copy)",
			PIMProductVariantUtil.getSKU(
				Arrays.asList("Yellow", "XL"), skus, "Running Shorts"));

		Assert.assertEquals(
			"RunningShortsBlueXL",
			PIMProductVariantUtil.getSKU(
				Arrays.asList("Blue", "XL"), skus, "Running Shorts"));
	}

}
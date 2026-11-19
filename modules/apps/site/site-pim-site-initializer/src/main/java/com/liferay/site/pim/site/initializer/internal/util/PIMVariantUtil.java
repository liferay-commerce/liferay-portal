/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Balazs Breier
 */
public class PIMVariantUtil {

	public static <T> List<List<T>> getCombinations(List<List<T>> valuesList) {
		List<List<T>> combinations = Collections.singletonList(
			Collections.emptyList());

		for (List<T> values : valuesList) {
			List<List<T>> newCombinations = new ArrayList<>();

			for (List<T> combination : combinations) {
				for (T value : values) {
					List<T> newCombination = new ArrayList<>(combination);

					newCombination.add(value);

					newCombinations.add(newCombination);
				}
			}

			combinations = newCombinations;
		}

		return combinations;
	}

	public static String getSKU(
		List<String> labels, Set<String> skus, String title) {

		StringBundler sb = new StringBundler(labels.size() + 1);

		sb.append(title);

		for (String label : labels) {
			sb.append(label);
		}

		String sku = _pattern.matcher(
			sb.toString()
		).replaceAll(
			StringPool.BLANK
		);

		String uniqueSKU = sku;

		for (int i = 1; skus.contains(uniqueSKU); i++) {
			uniqueSKU = sku + i;
		}

		return uniqueSKU;
	}

	private static final Pattern _pattern = Pattern.compile("\\s");

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.UniqueUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Balazs Breier
 */
public class PIMProductVariantUtil {

	public static List<List<ListTypeEntry>> getListTypeEntryCombinations(
		List<List<ListTypeEntry>> listTypeEntriesList) {

		List<List<ListTypeEntry>> oldListTypeEntryCombinations =
			Collections.singletonList(Collections.emptyList());

		for (List<ListTypeEntry> listTypeEntries : listTypeEntriesList) {
			List<List<ListTypeEntry>> newListTypeEntryCombinations =
				new ArrayList<>();

			for (List<ListTypeEntry> oldListTypeEntryCombination :
					oldListTypeEntryCombinations) {

				for (ListTypeEntry listTypeEntry : listTypeEntries) {
					List<ListTypeEntry> newListTypeEntryCombination =
						new ArrayList<>(oldListTypeEntryCombination);

					newListTypeEntryCombination.add(listTypeEntry);

					newListTypeEntryCombinations.add(
						newListTypeEntryCombination);
				}
			}

			oldListTypeEntryCombinations = newListTypeEntryCombinations;
		}

		return oldListTypeEntryCombinations;
	}

	public static String getSKU(
			List<String> labels, Set<String> skus, String title)
		throws PortalException {

		StringBundler sb = new StringBundler(labels.size() + 1);

		sb.append(title);

		for (String label : labels) {
			sb.append(label);
		}

		Matcher matcher = _pattern.matcher(sb.toString());

		String sku = matcher.replaceAll(StringPool.BLANK);

		if (!skus.contains(sku)) {
			return sku;
		}

		return UniqueUtil.getUniqueValue(
			"copy", uniqueSKU -> !skus.contains(uniqueSKU), sku);
	}

	private static final Pattern _pattern = Pattern.compile("\\s");

}
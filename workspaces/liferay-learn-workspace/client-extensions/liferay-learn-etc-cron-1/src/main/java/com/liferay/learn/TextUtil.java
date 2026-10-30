/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Francesco Acciaro
 */
public class TextUtil {

	public static String normalizeHTML(String html) {
		Matcher matcher = _lineTrailingWhitespacePattern.matcher(html);

		String normalizedHTML = matcher.replaceAll(StringPool.BLANK);

		return normalizedHTML.stripTrailing();
	}

	public static String unescapeUnicode(String text) {
		StringBuilder sb = new StringBuilder(text.length());

		int index = 0;

		while (index < text.length()) {
			char c = text.charAt(index);

			if ((c != CharPool.BACK_SLASH) || ((index + 1) >= text.length())) {
				sb.append(c);

				index++;

				continue;
			}

			char nextChar = text.charAt(index + 1);

			if (nextChar == CharPool.BACK_SLASH) {
				sb.append(CharPool.BACK_SLASH);

				index += 2;

				continue;
			}

			if ((nextChar == 'u') && ((index + 6) <= text.length()) &&
				_unicodeEscapePattern.matcher(
					text.substring(index, index + 6)
				).matches()) {

				sb.append(
					(char)Integer.parseInt(
						text.substring(index + 2, index + 6), 16));

				index += 6;

				continue;
			}

			sb.append(c);

			index++;
		}

		return sb.toString();
	}

	private static final Pattern _lineTrailingWhitespacePattern =
		Pattern.compile("[ \\t]+(?=\\n)");
	private static final Pattern _unicodeEscapePattern = Pattern.compile(
		"\\\\u([0-9a-fA-F]{4})");

}
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.headless.delivery.client.dto.v1_0.ContentField;
import com.liferay.headless.delivery.client.dto.v1_0.ContentFieldValue;
import com.liferay.headless.delivery.client.dto.v1_0.StructuredContent;
import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;

import java.util.Set;
import java.util.function.Function;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author Francesco Acciaro
 */
public class StructuredContentBuilder {

	public StructuredContentBuilder(
		DocsDirectory docsDirectory, long liferayContentStructureId,
		MarkdownParser markdownParser, NavigationHelper navigationHelper,
		Set<String> skipLocales,
		Function<String, Long[]> taxonomyCategoryIdsFunction) {

		_docsDirectory = docsDirectory;
		_liferayContentStructureId = liferayContentStructureId;
		_markdownParser = markdownParser;
		_navigationHelper = navigationHelper;
		_skipLocales = skipLocales;
		_taxonomyCategoryIdsFunction = taxonomyCategoryIdsFunction;
	}

	public String generateMD5Hex(File file) throws Exception {
		StringBundler sb = new StringBundler(3);

		sb.append(
			DigestUtils.md5Hex(
				new FileInputStream(_docsDirectory.getHTMLFile(file))));

		if (!_skipLocales.contains("ja")) {
			File japaneseHTMLFile = _docsDirectory.getHTMLFile(
				new File(
					StringUtil.replace(
						file.getCanonicalPath(), "/en/", "/ja/")));

			if (japaneseHTMLFile.exists()) {
				sb.append(
					DigestUtils.md5Hex(new FileInputStream(japaneseHTMLFile)));
			}
		}

		sb.append(
			DigestUtils.md5Hex(
				String.valueOf(
					_navigationHelper.getNavigationJSONObject(file))));

		return DigestUtils.md5Hex(sb.toString());
	}

	public StructuredContent toJapaneseStructuredContent(File japaneseFile)
		throws Exception {

		StructuredContent structuredContent = new StructuredContent();

		String japaneseText = FileUtils.readFileToString(
			japaneseFile, StandardCharsets.UTF_8);

		structuredContent.setContentFields(
			() -> new ContentField[] {
				_toContentField(
					"content", () -> _docsDirectory.getHTML(japaneseFile)),
				_toContentField(
					"navigation",
					() -> String.valueOf(
						_navigationHelper.getNavigationJSONObject(
							japaneseFile))),
				_toContentField(
					"showChildrenCards",
					() -> String.valueOf(
						_markdownParser.isShowChildrenCards(japaneseFile)))
			});

		structuredContent.setContentStructureId(
			() -> _liferayContentStructureId);
		structuredContent.setDescription(
			() -> _markdownParser.getDescription(japaneseText));
		structuredContent.setTitle(
			() -> _markdownParser.getTitle(japaneseText));

		return structuredContent;
	}

	public StructuredContent toStructuredContent(String fileName)
		throws Exception {

		StructuredContent structuredContent = new StructuredContent();

		File englishFile = new File(fileName);

		String englishText = FileUtils.readFileToString(
			englishFile, StandardCharsets.UTF_8);

		structuredContent.setContentFields(
			() -> new ContentField[] {
				_toContentField(
					"content", () -> _docsDirectory.getHTML(englishFile)),
				_toContentField("md5Hex", () -> generateMD5Hex(englishFile)),
				_toContentField(
					"navigation",
					() -> String.valueOf(
						_navigationHelper.getNavigationJSONObject(
							englishFile))),
				_toContentField(
					"showChildrenCards",
					() -> String.valueOf(
						_markdownParser.isShowChildrenCards(englishFile)))
			});

		structuredContent.setContentStructureId(
			() -> _liferayContentStructureId);
		structuredContent.setDescription(
			() -> _markdownParser.getDescription(englishText));
		structuredContent.setExternalReferenceCode(
			() -> _markdownParser.getUuid(englishText));
		structuredContent.setFriendlyUrlPath(
			() -> _docsDirectory.toFriendlyURLPath(englishFile));
		structuredContent.setTaxonomyCategoryIds(
			() -> _taxonomyCategoryIdsFunction.apply(englishText));

		structuredContent.setTitle(() -> _markdownParser.getTitle(englishText));

		return structuredContent;
	}

	private ContentField _toContentField(
		String name, UnsafeSupplier<String, Exception> unsafeSupplier) {

		ContentFieldValue contentFieldValue = new ContentFieldValue();

		contentFieldValue.setData(unsafeSupplier::get);

		ContentField contentField = new ContentField();

		contentField.setContentFieldValue(() -> contentFieldValue);
		contentField.setName(() -> name);

		return contentField;
	}

	private final DocsDirectory _docsDirectory;
	private final long _liferayContentStructureId;
	private final MarkdownParser _markdownParser;
	private final NavigationHelper _navigationHelper;
	private final Set<String> _skipLocales;
	private final Function<String, Long[]> _taxonomyCategoryIdsFunction;

}
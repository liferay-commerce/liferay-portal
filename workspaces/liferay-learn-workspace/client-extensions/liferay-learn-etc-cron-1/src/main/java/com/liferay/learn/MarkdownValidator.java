/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.StringBundler;

import java.io.File;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;

/**
 * @author Francesco Acciaro
 */
public class MarkdownValidator {

	public MarkdownValidator(
		DocsDirectory docsDirectory, MarkdownParser markdownParser,
		Set<String> preflightTocExclusions,
		Predicate<String> taxonomyCategoryNamePredicate) {

		_docsDirectory = docsDirectory;
		_markdownParser = markdownParser;
		_preflightTocExclusions = preflightTocExclusions;
		_taxonomyCategoryNamePredicate = taxonomyCategoryNamePredicate;
	}

	public int validate() throws Exception {
		List<String> preflightErrorMessages = new ArrayList<>();
		int preflightWarningCount = 0;

		Map<String, String> uuidFileNames = new HashMap<>();
		int validatedCount = 0;

		for (String fileName : _docsDirectory.getFileNames()) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			validatedCount++;

			Map<String, Object> data = null;
			String text = null;

			try {
				text = FileUtils.readFileToString(
					new File(fileName), StandardCharsets.UTF_8);

				data = _markdownParser.getFrontMatter(text);
			}
			catch (Exception exception) {
				preflightErrorMessages.add(
					fileName + ": the front matter is unparsable: " +
						exception);

				continue;
			}

			if (data == null) {
				preflightErrorMessages.add(
					fileName + ": the front matter is missing");

				continue;
			}

			String uuid = _markdownParser.getUuid(data);

			if (uuid.isEmpty()) {
				preflightErrorMessages.add(
					fileName + ": the uuid is missing in the front matter");
			}
			else {
				String duplicateFileName = uuidFileNames.put(uuid, fileName);

				if (duplicateFileName != null) {
					preflightErrorMessages.add(
						StringBundler.concat(
							"The uuid ", uuid, " is duplicated in ",
							duplicateFileName, " and ", fileName));
				}
			}

			if (data.containsKey("visibility") &&
				!(data.get("visibility") instanceof List)) {

				preflightErrorMessages.add(
					StringBundler.concat(
						fileName, ": \"visibility\" must be a list of role ",
						"names, otherwise the article is published with no ",
						"permissions"));
			}

			Object taxonomyCategoryNames = data.get("taxonomy-category-names");

			if (taxonomyCategoryNames instanceof List) {
				for (Object taxonomyCategoryName :
						(List)taxonomyCategoryNames) {

					if (!(taxonomyCategoryName instanceof String)) {
						preflightErrorMessages.add(
							StringBundler.concat(
								fileName, ": the taxonomy category name \"",
								taxonomyCategoryName,
								"\" must be quoted in the front matter"));
					}
					else if (!_taxonomyCategoryNamePredicate.test(
								(String)taxonomyCategoryName)) {

						preflightErrorMessages.add(
							StringBundler.concat(
								fileName, ": no taxonomy category exists with ",
								"the name \"", taxonomyCategoryName, "\""));
					}
				}
			}

			Object toc = data.get("toc");

			if (toc instanceof List) {
				for (Object tocEntry : (List)toc) {
					if (!(tocEntry instanceof String)) {
						preflightErrorMessages.add(
							StringBundler.concat(
								fileName, ": the toc entry \"", tocEntry,
								"\" must be quoted in the front matter"));

						continue;
					}

					if (NavigationHelper.isMarkdownLink((String)tocEntry)) {
						continue;
					}

					File file = new File(fileName);

					File tocFile = new File(
						file.getParent() + File.separator + tocEntry);

					if (!tocFile.exists() || tocFile.isDirectory()) {
						if (_isExcludedTocFile(tocFile)) {
							continue;
						}

						preflightWarningCount++;

						System.out.println(
							StringBundler.concat(
								"[preflight] Warning: ", fileName,
								" lists a nonexistent toc entry ", tocEntry));
					}
				}
			}
		}

		System.out.println(
			StringBundler.concat(
				"[preflight] Validated ", validatedCount, " markdown files: ",
				preflightErrorMessages.size(), " errors, ",
				preflightWarningCount, " warnings."));

		if (preflightErrorMessages.isEmpty()) {
			System.out.println("[preflight] PASSED");

			return 0;
		}

		System.out.println(
			"[preflight] FAILED (content): " + preflightErrorMessages.size() +
				" blocking problems, nothing was generated or imported.");

		for (String preflightErrorMessage : preflightErrorMessages) {
			System.out.println("[preflight] " + preflightErrorMessage);
		}

		return 1;
	}

	private boolean _isExcludedTocFile(File tocFile) throws Exception {
		if (_preflightTocExclusions.isEmpty()) {
			return false;
		}

		String relativePath = _docsDirectory.getRelativePath(tocFile);

		if (relativePath == null) {
			return false;
		}

		return _preflightTocExclusions.contains(relativePath);
	}

	private final DocsDirectory _docsDirectory;
	private final MarkdownParser _markdownParser;
	private final Set<String> _preflightTocExclusions;
	private final Predicate<String> _taxonomyCategoryNamePredicate;

}
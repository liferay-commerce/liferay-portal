/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.StringBundler;

import java.io.File;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Francesco Acciaro
 */
public class NavigationHelper {

	public static boolean isMarkdownLink(String text) {
		Matcher matcher = _markdownLinkPattern.matcher(text);

		return matcher.find();
	}

	public NavigationHelper(
		DocsDirectory docsDirectory, MarkdownParser markdownParser,
		RunReport runReport) {

		_docsDirectory = docsDirectory;
		_markdownParser = markdownParser;
		_runReport = runReport;
	}

	public JSONObject getNavigationJSONObject(File file) throws Exception {
		JSONObject navigationJSONObject = new JSONObject();

		navigationJSONObject.put(
			"breadcrumb", _getBreadcrumbJSONArray(file)
		).put(
			"children", _getChildrenJSONArray(file, true)
		).put(
			"parent", _getNavigationItemJSONObject(_getParentMarkdownFile(file))
		).put(
			"self", _getNavigationItemJSONObject(file)
		).put(
			"siblings",
			_getChildrenJSONArray(_getParentMarkdownFile(file), false)
		);

		return navigationJSONObject;
	}

	private JSONArray _getBreadcrumbJSONArray(File file) throws Exception {
		JSONArray breadcrumbJSONArray = new JSONArray();

		if (file == null) {
			return breadcrumbJSONArray;
		}

		File parentMarkdownFile = null;

		while ((parentMarkdownFile = _getParentMarkdownFile(file)) != null) {
			breadcrumbJSONArray.put(
				_getNavigationItemJSONObject(parentMarkdownFile));

			file = parentMarkdownFile;
		}

		return breadcrumbJSONArray;
	}

	private JSONArray _getChildrenJSONArray(File file, boolean nested)
		throws Exception {

		JSONArray childrenJSONArray = new JSONArray();

		if (file == null) {
			return childrenJSONArray;
		}

		Map<String, Object> data = _markdownParser.getFrontMatter(file);

		if ((data == null) || !data.containsKey("toc")) {
			return childrenJSONArray;
		}

		Object toc = data.get("toc");

		if (!(toc instanceof List)) {
			return childrenJSONArray;
		}

		for (Object tocEntry : (List)toc) {
			if (!(tocEntry instanceof String)) {
				continue;
			}

			Matcher matcher = _markdownLinkPattern.matcher((String)tocEntry);

			if (matcher.find()) {
				JSONObject linkJSONObject = new JSONObject();

				linkJSONObject.put(
					"title", matcher.group(1)
				).put(
					"url", matcher.group(2)
				);

				childrenJSONArray.put(linkJSONObject);

				continue;
			}

			String tocFileName = (String)tocEntry;

			String filePathString =
				file.getParent() + File.separator + tocFileName;

			File tocFile = new File(filePathString);

			if (!tocFile.exists() || tocFile.isDirectory()) {
				_runReport.addWarning(
					StringBundler.concat(
						"Nonexistent or invalid TOC file ", tocFile.getPath(),
						" in file ", file.getPath()));

				continue;
			}

			JSONObject childJSONObject = _getNavigationItemJSONObject(tocFile);

			if (nested) {
				childJSONObject.put(
					"children", _getChildrenJSONArray(tocFile, false));
			}

			childrenJSONArray.put(childJSONObject);
		}

		return childrenJSONArray;
	}

	private JSONObject _getNavigationItemJSONObject(File file)
		throws Exception {

		JSONObject navigationItemJSONObject = new JSONObject();

		if (file == null) {
			return navigationItemJSONObject;
		}

		navigationItemJSONObject.put(
			"title",
			_markdownParser.getTitle(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8)));

		Path docsPath = Paths.get(_docsDirectory.getDocsDirName());
		Path filePath = Paths.get(file.toURI());

		Path relativePath = docsPath.relativize(filePath);

		String urlString =
			"/w/" + FilenameUtils.removeExtension(String.valueOf(relativePath));

		urlString =
			urlString.substring(0, urlString.indexOf("/latest/")) +
				urlString.substring(urlString.indexOf("/latest/") + 10);

		navigationItemJSONObject.put("url", urlString);

		return navigationItemJSONObject;
	}

	private File _getParentMarkdownFile(File file) throws Exception {
		if (Objects.equals(file.getName(), "index.md")) {
			return null;
		}

		File parentFile = file.getParentFile();

		File parentMarkdownFile = new File(parentFile.getPath() + ".md");

		while (!parentMarkdownFile.exists()) {
			parentFile = parentFile.getParentFile();

			if (Objects.equals(
					parentFile.getPath(), _docsDirectory.getDocsDirName())) {

				break;
			}

			parentMarkdownFile = new File(parentFile.getPath() + ".md");
		}

		if (!parentMarkdownFile.exists()) {
			parentFile = file.getParentFile();

			parentMarkdownFile = new File(
				parentFile.getPath() + File.separator + "index.md");
		}

		if (!parentMarkdownFile.exists()) {
			_runReport.addWarning(
				"Missing parent markdown for " + parentMarkdownFile.getPath());

			return null;
		}

		return parentMarkdownFile;
	}

	private static final Pattern _markdownLinkPattern = Pattern.compile(
		"\\[(.*)\\]\\((.*)\\)");

	private final DocsDirectory _docsDirectory;
	private final MarkdownParser _markdownParser;
	private final RunReport _runReport;

}
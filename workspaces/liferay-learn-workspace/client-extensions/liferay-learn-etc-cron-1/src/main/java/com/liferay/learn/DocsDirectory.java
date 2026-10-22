/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Francesco Acciaro
 */
public class DocsDirectory {

	public DocsDirectory(File baseDir) throws Exception {
		_baseDirName = baseDir.getCanonicalPath();

		_docsDirName = _baseDirName + "/docs";

		_addFileNames(_docsDirName);
	}

	public String[] getDirNames(String fileName) {
		List<String> dirNames = new ArrayList<>();

		String[] parts = fileName.split(
			Matcher.quoteReplacement(File.separator));

		for (String part : parts) {
			if (StringUtil.equalsIgnoreCase(part, "en") ||
				StringUtil.equalsIgnoreCase(part, "ja") ||
				StringUtil.equalsIgnoreCase(part, "latest")) {

				continue;
			}

			String dirName = part;

			dirNames.add(dirName);
		}

		return dirNames.toArray(new String[0]);
	}

	public String getDocsDirName() {
		return _docsDirName;
	}

	public Set<String> getFileNames() {
		return _fileNames;
	}

	public String getHTML(File file) throws Exception {
		return FileUtils.readFileToString(
			getHTMLFile(file), StandardCharsets.UTF_8);
	}

	public File getHTMLFile(File file) throws Exception {
		String htmlFilePath = file.getCanonicalPath();

		htmlFilePath = htmlFilePath.replaceFirst(
			_docsDirName, _baseDirName + "/site");

		htmlFilePath = htmlFilePath.replaceFirst("\\.md", ".html");

		return new File(htmlFilePath);
	}

	public String getRelativePath(File file) throws Exception {
		String canonicalPath = file.getCanonicalPath();

		if (!canonicalPath.startsWith(_docsDirName + File.separator)) {
			return null;
		}

		return canonicalPath.substring(_docsDirName.length() + 1);
	}

	public String toFriendlyURLPath(File file) {
		String filePathString = file.getPath();

		String relativeFilePathString = filePathString.substring(
			_docsDirName.length() + 1);

		String friendlyURLPathString = StringUtil.merge(
			getDirNames(relativeFilePathString), StringPool.FORWARD_SLASH);

		return FilenameUtils.removeExtension(friendlyURLPathString);
	}

	private void _addFileNames(String fileName) {
		File file = new File(fileName);

		if (file.isDirectory() &&
			!Objects.equals(file.getName(), "resources") &&
			!Objects.equals(file.getName(), "_snippets")) {

			for (String currentFileName : file.list()) {
				_addFileNames(fileName + "/" + currentFileName);
			}
		}

		_fileNames.add(fileName);
	}

	private final String _baseDirName;
	private final String _docsDirName;
	private final Set<String> _fileNames = new TreeSet<>();

}
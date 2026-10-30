/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import java.io.File;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

/**
 * @author Francesco Acciaro
 */
public class RunReport {

	public void addError(String errorMessage) {
		System.out.println(errorMessage);

		_errorMessages.add(errorMessage);
	}

	public void addWarning(String warningMessage) {
		System.out.println(warningMessage);

		_warningMessages.add(warningMessage);
	}

	public int getErrorCount() {
		return _errorMessages.size();
	}

	public List<String> getErrorMessages() {
		return _errorMessages;
	}

	public boolean hasErrors() {
		return !_errorMessages.isEmpty();
	}

	public void printErrors() {
		if (!_errorMessages.isEmpty()) {
			System.out.println(_errorMessages.size() + " error messages:");

			for (String errorMessage : _errorMessages) {
				System.out.println(errorMessage);
			}
		}
	}

	public void printSummary(
		int addedCount, int deletedCount, int updatedCount) {

		System.out.println(addedCount + " structured contents were added.");

		if (deletedCount > 0) {
			System.out.println(
				deletedCount + " structured contents were deleted.");
		}

		System.out.println(updatedCount + " structured contents were updated.");

		if (!_warningMessages.isEmpty()) {
			System.out.println(_warningMessages.size() + " warning messages:");

			for (String warningMessage : _warningMessages) {
				System.out.println(warningMessage);
			}
		}
	}

	public void recordSuccess() {
		if (Objects.equals(
				System.getenv("LIFERAY_LEARN_ETC_CRON_PARTIAL"), "true")) {

			System.out.println(
				"The success marker was not updated because a partial run " +
					"does not reconcile the whole site.");

			return;
		}

		try {
			File file = new File("/public_html/.learn-importer-success");

			FileUtils.writeStringToFile(
				file, String.valueOf(System.currentTimeMillis() / 1000),
				StandardCharsets.UTF_8);

			System.out.println(
				"The success marker was updated in " + file.getPath() + ".");
		}
		catch (Exception exception) {
			System.out.println(
				"Unable to update the success marker: " + exception);
		}
	}

	private final List<String> _errorMessages = new ArrayList<>();
	private final List<String> _warningMessages = new ArrayList<>();

}
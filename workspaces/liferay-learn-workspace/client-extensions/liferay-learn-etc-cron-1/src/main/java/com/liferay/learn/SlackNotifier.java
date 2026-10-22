/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.io.File;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.json.JSONObject;

/**
 * @author Francesco Acciaro
 */
public class SlackNotifier {

	public SlackNotifier(RunReport runReport) {
		_runReport = runReport;
	}

	public void sendReport(
		int addedCount, int deletedCount, int skippedCount, int updatedCount) {

		String endpoint = System.getenv(
			"LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT");

		if ((endpoint == null) || endpoint.isEmpty()) {
			System.out.println(
				"No Slack endpoint is configured, skipping the notification.");

			return;
		}

		Map<String, String> phases = _readPhases();

		StringBundler sb = new StringBundler();

		if (!_runReport.hasErrors()) {
			sb.append(":sunflower: *liferay-learn-etc-cron-1* run completed");
		}
		else {
			sb.append(":rotating_light: *liferay-learn-etc-cron-1* run ");
			sb.append("failed with ");
			sb.append(_runReport.getErrorCount());
			sb.append(" errors");
		}

		String durations = _toDurations(phases);

		if (!durations.isEmpty()) {
			sb.append("\nPhases: ");
			sb.append(durations);
		}

		sb.append("\nArticles: ");
		sb.append(addedCount);
		sb.append(" added, ");
		sb.append(updatedCount);
		sb.append(" updated, ");
		sb.append(skippedCount);
		sb.append(" unchanged, ");
		sb.append(deletedCount);
		sb.append(" deleted");

		String commit = phases.get("commit");

		if (commit != null) {
			sb.append("\nSource: <");
			sb.append(_getCommitURL(commit));
			sb.append("|");
			sb.append(commit);
			sb.append(">");
		}

		String logURL = _getLogURL();

		if (logURL != null) {
			sb.append(" \u00b7 <");
			sb.append(logURL);
			sb.append("|console log>");
		}

		int reportedErrorCount = Math.min(_runReport.getErrorCount(), 10);

		for (int i = 0; i < reportedErrorCount; i++) {
			sb.append("\n> ");
			sb.append(
				_runReport.getErrorMessages(
				).get(
					i
				));
		}

		if (_runReport.getErrorCount() > reportedErrorCount) {
			sb.append("\n> and ");
			sb.append(_runReport.getErrorCount() - reportedErrorCount);
			sb.append(" more, see the console log");
		}

		_sendSlackMessage(endpoint, sb.toString());

		try {
			Files.createFile(Paths.get("/tmp/liferay_learn_slack_notified"));
		}
		catch (Exception exception) {
			System.out.println(
				"Unable to mark the notification as sent: " + exception);
		}
	}

	private long _addGroupSeconds(
		Map<String, Long> groupSeconds, String name, long seconds) {

		groupSeconds.merge(
			_phaseGroups.getOrDefault(name, name), seconds, Long::sum);

		return seconds;
	}

	private String _getCommitURL(String commit) {
		return StringBundler.concat(
			"https://github.com/",
			GetterUtil.getString(
				System.getenv("LIFERAY_LEARN_ETC_CRON_GITHUB_USER"), "liferay"),
			"/liferay-learn/commit/", commit);
	}

	private String _getLogURL() {
		String infrastructureDomain = System.getenv(
			"LCP_INFRASTRUCTURE_DOMAIN");
		String projectId = System.getenv("LCP_PROJECT_ID");
		String serviceId = System.getenv("LCP_SERVICE_ID");

		if ((infrastructureDomain == null) || (projectId == null) ||
			(serviceId == null)) {

			return null;
		}

		return StringBundler.concat(
			"https://console.", infrastructureDomain, "/projects/", projectId,
			"/logs?instanceId=", System.getenv("HOSTNAME"), "&logServiceId=",
			serviceId);
	}

	private Map<String, String> _readPhases() {
		Map<String, String> phases = new LinkedHashMap<>();

		File file = new File("/tmp/liferay_learn_run_phases");

		if (!file.exists()) {
			return phases;
		}

		try {
			for (String line :
					FileUtils.readLines(file, StandardCharsets.UTF_8)) {

				String[] parts = line.split(StringPool.SPACE);

				if (parts.length == 3) {
					phases.put(parts[1], parts[2]);
				}
				else if (parts.length == 2) {
					phases.put(parts[0], parts[1]);
				}
			}
		}
		catch (Exception exception) {
			System.out.println(
				"Unable to read the phase timings: " + exception);
		}

		return phases;
	}

	private void _sendSlackMessage(String endpoint, String text) {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(
			"channel", System.getenv("LIFERAY_LEARN_ETC_CRON_SLACK_CHANNEL")
		).put(
			"icon_emoji", ":robot_face:"
		).put(
			"text", text
		).put(
			"username", "learn-importer"
		);

		HttpPost httpPost = new HttpPost(endpoint);

		httpPost.setHeader("Content-Type", "application/json");

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

		httpClientBuilder.setDefaultRequestConfig(
			RequestConfig.custom(
			).setConnectionRequestTimeout(
				30000
			).setConnectTimeout(
				30000
			).setSocketTimeout(
				30000
			).build());

		try (CloseableHttpClient closeableHttpClient =
				httpClientBuilder.build()) {

			httpPost.setEntity(
				new StringEntity(
					jsonObject.toString(), StandardCharsets.UTF_8));

			try (CloseableHttpResponse closeableHttpResponse =
					closeableHttpClient.execute(httpPost)) {

				StatusLine statusLine = closeableHttpResponse.getStatusLine();

				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					System.out.println(
						"The Slack notification was refused with status " +
							statusLine.getStatusCode());
				}
			}
		}
		catch (Exception exception) {
			System.out.println(
				"Unable to send the Slack notification: " + exception);
		}
	}

	private String _toDuration(long seconds) {
		if (seconds < 60) {
			return seconds + "s";
		}

		return StringBundler.concat(seconds / 60, "m", seconds % 60, "s");
	}

	private String _toDurations(Map<String, String> phases) {
		Map<String, Long> groupSeconds = new LinkedHashMap<>();

		String previousName = null;
		long previousSeconds = 0;
		long totalSeconds = 0;

		for (Map.Entry<String, String> entry : phases.entrySet()) {
			String name = entry.getKey();

			if (Objects.equals(name, "commit")) {
				continue;
			}

			long seconds = GetterUtil.getLong(entry.getValue());

			if (previousName != null) {
				totalSeconds += _addGroupSeconds(
					groupSeconds, previousName, seconds - previousSeconds);
			}

			previousName = name;
			previousSeconds = seconds;
		}

		if (previousName == null) {
			return StringPool.BLANK;
		}

		totalSeconds += _addGroupSeconds(
			groupSeconds, previousName,
			(System.currentTimeMillis() / 1000) - previousSeconds);

		StringBundler sb = new StringBundler();

		sb.append(_toDuration(totalSeconds));
		sb.append(" total");

		for (Map.Entry<String, Long> entry : groupSeconds.entrySet()) {
			sb.append(" \u00b7 ");
			sb.append(entry.getKey());
			sb.append(StringPool.SPACE);
			sb.append(_toDuration(entry.getValue()));
		}

		return sb.toString();
	}

	private static final Map<String, String> _phaseGroups = HashMapBuilder.put(
		"clone", "sources"
	).put(
		"copy", "publish"
	).put(
		"generation", "sources"
	).put(
		"import", "import"
	).put(
		"manifest", "publish"
	).put(
		"preflight", "sources"
	).put(
		"preflight-dxp", "sources"
	).build();

	private final RunReport _runReport;

}
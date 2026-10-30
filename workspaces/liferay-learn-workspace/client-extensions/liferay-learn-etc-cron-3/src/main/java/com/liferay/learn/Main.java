/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.net.URLEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Francesco Acciaro
 */
public class Main {

	public static void main(String[] args) {
		int exitCode = 0;

		try {
			_checkEnvironmentVariables();

			Set<String> expectedScopeAliases = new TreeSet<>();

			for (String expectedScopeAlias :
					StringUtil.split(
						System.getenv(
							"LIFERAY_LEARN_ETC_CRON_EXPECTED_SCOPES"))) {

				expectedScopeAlias = expectedScopeAlias.trim();

				if (!expectedScopeAlias.isEmpty()) {
					expectedScopeAliases.add(expectedScopeAlias);
				}
			}

			Main main = new Main(
				expectedScopeAliases, "https://api.liferay.cloud",
				System.getenv("LIFERAY_LEARN_ETC_CRON_LCP_EMAIL"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_LCP_PASSWORD"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_LCP_PROJECT_ID"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_OAUTH_APPLICATION_ERC"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_SECRET"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_LIFERAY_URL"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_TARGET_SERVICE_ID"));

			exitCode = main.run();
		}
		catch (Exception exception) {
			System.out.println(exception.getMessage());

			exitCode = 1;
		}

		System.exit(exitCode);
	}

	public Main(
		Set<String> expectedScopeAliases, String lcpApiUrl, String lcpEmail,
		String lcpPassword, String lcpProjectId,
		String liferayOAuthApplicationExternalReferenceCode,
		String liferayOAuthClientSecret, String liferayUrl,
		String targetServiceId) {

		_expectedScopeAliases = expectedScopeAliases;
		_lcpApiUrl = lcpApiUrl;
		_lcpEmail = lcpEmail;
		_lcpPassword = lcpPassword;
		_lcpProjectId = lcpProjectId;
		_liferayOAuthApplicationExternalReferenceCode =
			liferayOAuthApplicationExternalReferenceCode;
		_liferayOAuthClientSecret = liferayOAuthClientSecret;
		_liferayUrl = liferayUrl;
		_targetServiceId = targetServiceId;
	}

	public int run() throws Exception {
		Set<String> missingScopeAliases = _getMissingScopeAliases();

		if (missingScopeAliases.isEmpty()) {
			System.out.println(
				StringBundler.concat(
					"All ", _expectedScopeAliases.size(),
					" expected scopes are granted to ",
					_liferayOAuthApplicationExternalReferenceCode));

			return 0;
		}

		System.out.println(
			StringBundler.concat(
				"Scope drift detected on ",
				_liferayOAuthApplicationExternalReferenceCode,
				". Missing scopes: ", missingScopeAliases));

		String lcpAuthorizationToken = _getLcpAuthorizationToken();

		String buildGroupUid = _getLatestBuildGroupUid(lcpAuthorizationToken);

		System.out.println(
			StringBundler.concat(
				"Redeploying build group ", buildGroupUid, " of service ",
				_targetServiceId));

		_deployBuild(lcpAuthorizationToken, buildGroupUid);

		for (int i = 0; i < _VERIFY_MAX_ATTEMPTS; i++) {
			try {
				Thread.sleep(_VERIFY_INTERVAL_MILLIS);
			}
			catch (InterruptedException interruptedException) {
				Thread.currentThread(
				).interrupt();

				throw interruptedException;
			}

			missingScopeAliases = _getMissingScopeAliases();

			if (missingScopeAliases.isEmpty()) {
				System.out.println(
					StringBundler.concat(
						"Scope drift was remediated by deploying build group ",
						buildGroupUid, " of service ", _targetServiceId));

				return 0;
			}
		}

		System.out.println(
			StringBundler.concat(
				"Scope drift was NOT remediated after redeploying build group ",
				buildGroupUid, ". Still missing: ", missingScopeAliases));

		return 1;
	}

	private static void _checkEnvironmentVariables() throws Exception {
		Set<String> missingNames = new TreeSet<>();

		for (String name :
				Arrays.asList(
					"LIFERAY_LEARN_ETC_CRON_EXPECTED_SCOPES",
					"LIFERAY_LEARN_ETC_CRON_LCP_EMAIL",
					"LIFERAY_LEARN_ETC_CRON_LCP_PASSWORD",
					"LIFERAY_LEARN_ETC_CRON_LCP_PROJECT_ID",
					"LIFERAY_LEARN_ETC_CRON_LIFERAY_URL",
					"LIFERAY_LEARN_ETC_CRON_OAUTH_APPLICATION_ERC",
					"LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_SECRET",
					"LIFERAY_LEARN_ETC_CRON_TARGET_SERVICE_ID")) {

			if (Validator.isNull(System.getenv(name))) {
				missingNames.add(name);
			}
		}

		if (!missingNames.isEmpty()) {
			throw new Exception(
				"Missing required environment variables: " + missingNames);
		}
	}

	private void _deployBuild(
			String lcpAuthorizationToken, String buildGroupUid)
		throws Exception {

		HttpPost httpPost = new HttpPost(
			StringBundler.concat(
				_lcpApiUrl, "/projects/", _lcpProjectId, "/deploy"));

		JSONObject payloadJSONObject = new JSONObject(
		).put(
			"buildGroupUid", buildGroupUid
		);

		httpPost.setEntity(new StringEntity(payloadJSONObject.toString()));

		httpPost.setHeader("Authorization", "Bearer " + lcpAuthorizationToken);
		httpPost.setHeader("Content-Type", "application/json");

		_execute(httpPost, "deploy build group " + buildGroupUid);
	}

	private String _execute(HttpUriRequest httpUriRequest, String action)
		throws Exception {

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

		try (CloseableHttpClient closeableHttpClient =
				httpClientBuilder.build();

			CloseableHttpResponse closeableHttpResponse =
				closeableHttpClient.execute(httpUriRequest)) {

			StatusLine statusLine = closeableHttpResponse.getStatusLine();

			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				throw new Exception(
					StringBundler.concat(
						"Unable to ", action, ": HTTP ",
						statusLine.getStatusCode()));
			}

			return EntityUtils.toString(
				closeableHttpResponse.getEntity(), Charset.defaultCharset());
		}
	}

	private Set<String> _getGrantedScopeAliases() throws Exception {
		Set<String> grantedScopeAliases = new TreeSet<>();

		HttpGet httpGet = new HttpGet(
			StringBundler.concat(
				_liferayUrl, "/o/oauth2/application?externalReferenceCode=",
				URLEncoder.encode(
					_liferayOAuthApplicationExternalReferenceCode,
					StandardCharsets.UTF_8)));

		JSONObject applicationJSONObject = new JSONObject(
			_execute(httpGet, "fetch the OAuth application client ID"));

		HttpPost httpPost = new HttpPost(_liferayUrl + "/o/oauth2/token");

		httpPost.setEntity(
			new UrlEncodedFormEntity(
				Arrays.asList(
					new BasicNameValuePair(
						"client_id",
						applicationJSONObject.getString("client_id")),
					new BasicNameValuePair(
						"client_secret", _liferayOAuthClientSecret),
					new BasicNameValuePair(
						"grant_type", "client_credentials"))));
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		JSONObject tokenJSONObject = new JSONObject(
			_execute(httpPost, "request a client credentials token"));

		for (String grantedScopeAlias :
				StringUtil.split(
					tokenJSONObject.optString("scope"), CharPool.SPACE)) {

			if (!grantedScopeAlias.isEmpty()) {
				grantedScopeAliases.add(grantedScopeAlias);
			}
		}

		return grantedScopeAliases;
	}

	private String _getLatestBuildGroupUid(String lcpAuthorizationToken)
		throws Exception {

		HttpGet httpGet = new HttpGet(
			StringBundler.concat(
				_lcpApiUrl, "/projects/", _lcpProjectId, "/builds?perPage=50"));

		httpGet.setHeader("Authorization", "Bearer " + lcpAuthorizationToken);

		JSONArray buildsJSONArray = new JSONArray(
			_execute(httpGet, "list builds"));

		String buildGroupUid = null;
		long latestCreatedAt = 0;

		for (int i = 0; i < buildsJSONArray.length(); i++) {
			JSONObject buildJSONObject = buildsJSONArray.getJSONObject(i);

			if (!Objects.equals(
					_targetServiceId, buildJSONObject.optString("serviceId")) ||
				!Objects.equals(
					buildJSONObject.optString("status"), "SUCCEEDED")) {

				continue;
			}

			long createdAt = buildJSONObject.optLong("createdAt");

			if (createdAt > latestCreatedAt) {
				buildGroupUid = buildJSONObject.optString("buildGroupUid");
				latestCreatedAt = createdAt;
			}
		}

		if (buildGroupUid == null) {
			throw new Exception(
				"No successful build was found for service " +
					_targetServiceId);
		}

		return buildGroupUid;
	}

	private String _getLcpAuthorizationToken() throws Exception {
		HttpPost httpPost = new HttpPost(_lcpApiUrl + "/login");

		JSONObject payloadJSONObject = new JSONObject(
		).put(
			"email", _lcpEmail
		).put(
			"password", _lcpPassword
		);

		httpPost.setEntity(new StringEntity(payloadJSONObject.toString()));

		httpPost.setHeader("Content-Type", "application/json");

		JSONObject responseJSONObject = new JSONObject(
			_execute(httpPost, "log into the Liferay Cloud API"));

		return responseJSONObject.getString("token");
	}

	private Set<String> _getMissingScopeAliases() throws Exception {
		Set<String> missingScopeAliases = new TreeSet<>(_expectedScopeAliases);

		missingScopeAliases.removeAll(_getGrantedScopeAliases());

		return missingScopeAliases;
	}

	private static final long _VERIFY_INTERVAL_MILLIS = 20 * 1000;

	private static final int _VERIFY_MAX_ATTEMPTS = 24;

	private final Set<String> _expectedScopeAliases;
	private final String _lcpApiUrl;
	private final String _lcpEmail;
	private final String _lcpPassword;
	private final String _lcpProjectId;
	private final String _liferayOAuthApplicationExternalReferenceCode;
	private final String _liferayOAuthClientSecret;
	private final String _liferayUrl;
	private final String _targetServiceId;

}
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import com.liferay.client.extension.util.spring.boot3.client.LiferayOAuth2AccessTokenManager;
import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Caleb Hall
 * @author Nilton Vieira
 */
@RequestMapping("/object/action/exam/results/synchronization")
@RestController
public class ObjectActionExamResultsSynchronizationRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isInfoEnabled()) {
			_log.info("Started exam results synchronization");
		}

		JSONObject jsonObject = new JSONObject(json);

		int examResultAmount = 0;
		long startTime = System.currentTimeMillis();
		String status = "Failed";

		try {
			JSONObject objectEntryJSONObject = jsonObject.getJSONObject(
				"objectEntry");

			JSONObject valuesJSONObject = objectEntryJSONObject.getJSONObject(
				"values");

			String source = valuesJSONObject.optString("source");

			if (Validator.isNull(source)) {
				source = "webassessor";
			}

			OffsetDateTime offsetDateTime =
				_getLatestSuccessfulExecutionOffsetDateTime(source);

			if (Objects.equals(source, "certmanager")) {
				examResultAmount += _reimportMissedPrerequisiteExamResults();

				while (offsetDateTime.isBefore(
							OffsetDateTime.now(ZoneOffset.UTC))) {

					examResultAmount += _importCertificationExamReports(
						offsetDateTime);

					offsetDateTime = offsetDateTime.plusDays(14);
				}
			}
			else {
				while (offsetDateTime.isBefore(
							OffsetDateTime.now(ZoneOffset.UTC))) {

					examResultAmount += _importWebAssessorExamResults(
						offsetDateTime);

					offsetDateTime = offsetDateTime.plusDays(7);
				}
			}

			status = "Successful";
		}
		catch (WebClientResponseException webClientResponseException) {
			_log.error(webClientResponseException.getResponseBodyAsString());
		}
		catch (Exception exception) {
			_log.error("Unable to synchronize exam results", exception);
		}

		_updateExamResultsSynchronization(
			jsonObject.getLong("classPK"), examResultAmount,
			System.currentTimeMillis() - startTime, status);

		if (_log.isInfoEnabled()) {
			_log.info("Finished exam results synchronization");
		}

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private String _getAuthorization() {
		return _liferayOAuth2AccessTokenManager.getAuthorization(
			"liferay-learn-etc-spring-boot-oahs");
	}

	private JSONArray _getCertificationExamReportsJSONArray(
			OffsetDateTime offsetDateTime)
		throws Exception {

		JSONArray jsonArray = new JSONArray();

		int lastPage = 1;

		for (int page = 1; page <= lastPage; page++) {
			JSONObject jsonObject = new JSONObject(
				get(
					_getCertmanagerAuthorization(),
					UriComponentsBuilder.fromPath(
						"/o/certification-exam/report"
					).host(
						"certmanager.liferay.com"
					).queryParam(
						"endDate",
						offsetDateTime.plusDays(
							14
						).format(
							DateTimeFormatter.ISO_LOCAL_DATE
						)
					).queryParam(
						"examCode", "all"
					).queryParam(
						"page", page
					).queryParam(
						"pageSize", 100
					).queryParam(
						"startDate",
						offsetDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
					).scheme(
						"https"
					).build(
					).toUri()));

			jsonArray.putAll(jsonObject.getJSONArray("examResults"));

			int pageSize = jsonObject.getInt("pageSize");

			lastPage = (jsonObject.getInt("total") + pageSize - 1) / pageSize;
		}

		return jsonArray;
	}

	private String _getCertmanagerAuthorization() throws Exception {
		if ((_certmanagerAuthorization != null) &&
			(System.currentTimeMillis() <
				_certmanagerAuthorizationExpirationTime)) {

			return _certmanagerAuthorization;
		}

		JSONObject jsonObject = new JSONObject(
			post(
				StringBundler.concat(
					"client_id=", _certmanagerClientId, "&client_secret=",
					_certmanagerClientSecret, "&grant_type=client_credentials"),
				Collections.singletonMap(
					HttpHeaders.CONTENT_TYPE,
					MediaType.APPLICATION_FORM_URLENCODED_VALUE),
				UriComponentsBuilder.fromPath(
					"/o/oauth2/token"
				).host(
					"certmanager.liferay.com"
				).scheme(
					"https"
				).build(
				).toUri()));

		String accessToken = jsonObject.optString("access_token");

		if (Validator.isNull(accessToken)) {
			throw new Exception("Unable to get access token");
		}

		_certmanagerAuthorization = "Bearer " + accessToken;

		_certmanagerAuthorizationExpirationTime =
			System.currentTimeMillis() +
				(jsonObject.getLong("expires_in") * 1000);

		return _certmanagerAuthorization;
	}

	private JSONObject _getExamResultJSONObject(
			JSONObject certificationExamReportJSONObject)
		throws Exception {

		String emailAddress = certificationExamReportJSONObject.getString(
			"emailAddress");

		emailAddress = emailAddress.trim();

		String examName = certificationExamReportJSONObject.getString(
			"examName");

		examName = examName.trim();

		examName = _examNames.getOrDefault(examName, examName);

		OffsetDateTime offsetDateTime = OffsetDateTime.parse(
			certificationExamReportJSONObject.getString("date")
		).withOffsetSameInstant(
			ZoneOffset.UTC
		);

		return new JSONObject(
		).put(
			"date", offsetDateTime.format(DateTimeFormatter.ISO_INSTANT)
		).put(
			"emailAddress", emailAddress
		).put(
			"examName", examName
		).put(
			"externalReferenceCode",
			_getExternalReferenceCode(
				emailAddress, examName, offsetDateTime.toLocalDate())
		).put(
			"firstName",
			certificationExamReportJSONObject.getString("firstName")
		).put(
			"lastName", certificationExamReportJSONObject.getString("lastName")
		).put(
			"missedPrerequisite",
			StringUtil.equalsIgnoreCase(
				certificationExamReportJSONObject.optString(
					"passedPrerequisite"),
				"false")
		).put(
			"result",
			StringUtil.toLowerCase(
				certificationExamReportJSONObject.getString("result"))
		).put(
			"score", certificationExamReportJSONObject.getDouble("score")
		).put(
			"source", "certmanager"
		).put(
			"testName", examName
		);
	}

	private String _getExternalReferenceCode(
			String emailAddress, String examName, LocalDate localDate)
		throws Exception {

		String keyString = StringBundler.concat(
			StringUtil.toLowerCase(emailAddress), "|", examName, "|",
			localDate);

		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

		HexFormat hexFormat = HexFormat.of();

		return hexFormat.formatHex(
			messageDigest.digest(keyString.getBytes(StandardCharsets.UTF_8)));
	}

	private OffsetDateTime _getLatestSuccessfulExecutionOffsetDateTime(
		String source) {

		JSONObject jsonObject = new JSONObject(
			get(
				_getAuthorization(),
				UriComponentsBuilder.fromPath(
					"/o/c/p2s3examresultssynchronizations"
				).queryParam(
					"fields", "dateCreated,source"
				).queryParam(
					"filter", "synchronizationStatus eq 'Successful'"
				).queryParam(
					"pageSize", 10
				).queryParam(
					"sort", "dateCreated:desc"
				).build(
				).toUri()));

		JSONArray itemsJSONArray = jsonObject.getJSONArray("items");

		for (int i = 0; i < itemsJSONArray.length(); i++) {
			JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);

			if (!Objects.equals(_getSource(itemJSONObject), source)) {
				continue;
			}

			return OffsetDateTime.parse(
				itemJSONObject.getString("dateCreated"));
		}

		return OffsetDateTime.of(
			GetterUtil.getInteger(
				System.getenv("LIFERAY_LEARN_ETC_SPRING_BOOT_START_YEAR")),
			1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
	}

	private String _getPayload(JSONObject jsonObject) {
		return new JSONObject(
		).put(
			"date",
			OffsetDateTime.parse(
				jsonObject.getString("date")
			).atZoneSameInstant(
				ZoneOffset.UTC
			).format(
				DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX")
			)
		).put(
			"emailAddress",
			jsonObject.getJSONObject(
				"simpleRegistration"
			).getJSONObject(
				"candidate"
			).getString(
				"email"
			)
		).put(
			"examName", jsonObject.getString("examName")
		).put(
			"externalReferenceCode", jsonObject.getLong("id")
		).put(
			"firstName",
			jsonObject.getJSONObject(
				"simpleRegistration"
			).getJSONObject(
				"candidate"
			).getString(
				"firstName"
			)
		).put(
			"lastName",
			jsonObject.getJSONObject(
				"simpleRegistration"
			).getJSONObject(
				"candidate"
			).getString(
				"lastName"
			)
		).put(
			"result", jsonObject.getString("passFail")
		).put(
			"score", jsonObject.getDouble("score")
		).put(
			"source", "webassessor"
		).put(
			"testName",
			jsonObject.getJSONObject(
				"simpleRegistration"
			).getString(
				"testName"
			)
		).toString();
	}

	private String _getSource(JSONObject jsonObject) {
		JSONObject sourceJSONObject = jsonObject.optJSONObject("source");

		if ((sourceJSONObject == null) ||
			Validator.isNull(sourceJSONObject.optString("key"))) {

			return "webassessor";
		}

		return sourceJSONObject.getString("key");
	}

	private boolean _hasExamResult(
		String emailAddress, String externalReferenceCode) {

		try {
			JSONObject jsonObject = new JSONObject(
				get(
					_getAuthorization(),
					UriComponentsBuilder.fromPath(
						"/o/c/p2s3examresults/by-external-reference-code/" +
							externalReferenceCode
					).queryParam(
						"fields", "emailAddress"
					).build(
					).toUri()));

			String existingEmailAddress = jsonObject.optString("emailAddress");

			return StringUtil.equalsIgnoreCase(
				emailAddress, existingEmailAddress.trim());
		}
		catch (WebClientResponseException webClientResponseException) {
			HttpStatusCode httpStatusCode =
				webClientResponseException.getStatusCode();

			if (!httpStatusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
				throw webClientResponseException;
			}

			if (_log.isDebugEnabled()) {
				_log.debug(webClientResponseException);
			}

			return false;
		}
	}

	private int _importCertificationExamReports(OffsetDateTime offsetDateTime)
		throws Exception {

		JSONArray jsonArray = _getCertificationExamReportsJSONArray(
			offsetDateTime);

		int failedAmount = 0;

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			try {
				JSONObject examResultJSONObject = _getExamResultJSONObject(
					jsonObject);

				long examInstanceTokenId = jsonObject.optLong(
					"examInstanceTokenId");

				if ((examInstanceTokenId > 0) &&
					_retry(
						() -> _hasExamResult(
							examResultJSONObject.getString("emailAddress"),
							String.valueOf(examInstanceTokenId)))) {

					examResultJSONObject.put(
						"externalReferenceCode",
						String.valueOf(examInstanceTokenId));
				}

				_retry(() -> _putExamResult(examResultJSONObject));
			}
			catch (Exception exception) {
				failedAmount++;

				_log.error(
					"Unable to synchronize certification exam report " +
						jsonObject,
					exception);
			}
		}

		if (failedAmount > 0) {
			throw new Exception(
				StringBundler.concat(
					"Unable to synchronize ", failedAmount, " of ",
					jsonArray.length(), " certification exam reports"));
		}

		return jsonArray.length();
	}

	private int _importWebAssessorExamResults(OffsetDateTime offsetDateTime) {
		JSONArray jsonArray = new JSONArray(
			post(
				null,
				new JSONObject(
				).put(
					"endDate",
					offsetDateTime.plusDays(
						7
					).format(
						DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
					)
				).put(
					"requestType", "GET TRANSCRIPTS BY DATE RANGE"
				).put(
					"returnFormat", "JSON"
				).put(
					"securityToken", _webassessorSecurityToken
				).put(
					"startDate",
					offsetDateTime.format(
						DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"))
				).toString(),
				UriComponentsBuilder.fromPath(
					"/WebAssessorWebServices/jaxrs/wawebservices/processRequest"
				).host(
					"webassessor.com"
				).scheme(
					"https"
				).build(
				).toUri()));

		if (jsonArray.get(0) instanceof String) {
			return 0;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			put(
				_getAuthorization(), _getPayload(jsonObject),
				UriComponentsBuilder.fromPath(
					"/o/c/p2s3examresults/by-external-reference-code/" +
						jsonObject.getLong("id")
				).build(
				).toUri());
		}

		return jsonArray.length();
	}

	private String _putExamResult(JSONObject jsonObject) {
		String externalReferenceCode = jsonObject.getString(
			"externalReferenceCode");

		return put(
			_getAuthorization(), jsonObject.toString(),
			UriComponentsBuilder.fromPath(
				"/o/c/p2s3examresults/by-external-reference-code/" +
					externalReferenceCode
			).build(
			).toUri());
	}

	private int _reimportMissedPrerequisiteExamResults() throws Exception {
		Set<LocalDate> localDates = new TreeSet<>();

		int lastPage = 1;

		for (int page = 1; page <= lastPage; page++) {
			JSONObject jsonObject = new JSONObject(
				get(
					_getAuthorization(),
					UriComponentsBuilder.fromPath(
						"/o/c/p2s3examresults"
					).queryParam(
						"fields", "date"
					).queryParam(
						"filter",
						"missedPrerequisite eq true and result eq 'pass'"
					).queryParam(
						"page", page
					).queryParam(
						"pageSize", 100
					).build(
					).toUri()));

			JSONArray itemsJSONArray = jsonObject.getJSONArray("items");

			for (int i = 0; i < itemsJSONArray.length(); i++) {
				JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);

				OffsetDateTime offsetDateTime = OffsetDateTime.parse(
					itemJSONObject.getString("date"));

				localDates.add(
					LocalDate.ofInstant(
						offsetDateTime.toInstant(), ZoneOffset.UTC));
			}

			lastPage = jsonObject.getInt("lastPage");
		}

		int examResultAmount = 0;
		LocalDate endLocalDate = LocalDate.MIN;

		for (LocalDate localDate : localDates) {
			if (localDate.isBefore(endLocalDate)) {
				continue;
			}

			examResultAmount += _importCertificationExamReports(
				OffsetDateTime.of(
					localDate.minusDays(1), LocalTime.MIDNIGHT,
					ZoneOffset.UTC));

			endLocalDate = localDate.plusDays(12);
		}

		return examResultAmount;
	}

	private <T> T _retry(UnsafeSupplier<T, Exception> unsafeSupplier)
		throws Exception {

		for (long delay : _RETRY_DELAYS) {
			try {
				return unsafeSupplier.get();
			}
			catch (WebClientRequestException webClientRequestException) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						StringBundler.concat(
							"Retrying in ", delay / 1000, " seconds after ",
							webClientRequestException.getMessage()));
				}

				Thread.sleep(delay);
			}
		}

		return unsafeSupplier.get();
	}

	private void _updateExamResultsSynchronization(
		Long classPK, int examResultAmount, long executionTime,
		String synchronizationStatus) {

		patch(
			_getAuthorization(),
			new JSONObject(
			).put(
				"examResultAmount", examResultAmount
			).put(
				"executionTime", executionTime
			).put(
				"synchronizationStatus", synchronizationStatus
			).toString(),
			UriComponentsBuilder.fromPath(
				"/o/c/p2s3examresultssynchronizations/" + classPK
			).build(
			).toUri());
	}

	private static final long[] _RETRY_DELAYS = {5000, 15000, 30000};

	private static final Log _log = LogFactory.getLog(
		ObjectActionExamResultsSynchronizationRestController.class);

	private static final Map<String, String> _examNames = Map.of(
		"Building Enterprise Websites with Liferay",
		"Building Enterprise Websites with Liferay Certification Exam");

	private String _certmanagerAuthorization;
	private long _certmanagerAuthorizationExpirationTime;

	@Value("${liferay.learn.certmanager.client.id}")
	private String _certmanagerClientId;

	@Value("${liferay.learn.certmanager.client.secret}")
	private String _certmanagerClientSecret;

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

	@Value("${liferay.learn.webassessor.security.token}")
	private String _webassessorSecurityToken;

}
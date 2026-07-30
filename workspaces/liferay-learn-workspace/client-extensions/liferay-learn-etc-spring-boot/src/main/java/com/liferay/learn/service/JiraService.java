/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn.service;

import com.liferay.client.extension.util.spring.boot3.service.BaseService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Caleb Hall
 */
@Component
public class JiraService extends BaseService {

	public int calculateStartAt(int page, int pageSize) {
		return (page - 1) * pageSize;
	}

	@Cacheable("affectedVersions")
	public JSONArray getAffectedVersionsJSONArray() throws Exception {
		try {
			Set<String> affectedVersions = new TreeSet<>();

			String[] issueFields = {_FIELD_VERSIONS};

			StringBundler sb = new StringBundler(7);

			sb.append("project = '");
			sb.append(_jiraSecurityVulnerabilityProject);
			sb.append("' AND ");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldPublishingStatus));
			sb.append(" = 'Ready for Publishing' AND ");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldPartnerPublishingDate));
			sb.append(" <= now()");

			String nextPageToken = StringPool.BLANK;

			while (true) {
				JSONObject jsonObject = _searchJSONObject(
					sb.toString(), 100, nextPageToken, issueFields);

				if (jsonObject == null) {
					break;
				}

				JSONArray issuesJSONArray = jsonObject.getJSONArray("issues");

				for (int i = 0; i < issuesJSONArray.length(); i++) {
					JSONObject issueJSONObject = issuesJSONArray.getJSONObject(
						i);

					JSONObject fieldsJSONObject = issueJSONObject.getJSONObject(
						"fields");

					JSONArray versionsJSONArray = fieldsJSONObject.getJSONArray(
						"versions");

					for (int j = 0; j < versionsJSONArray.length(); j++) {
						JSONObject versionJSONObject =
							versionsJSONArray.getJSONObject(j);

						affectedVersions.add(
							versionJSONObject.optString("name"));
					}
				}

				nextPageToken = jsonObject.optString("nextPageToken");

				if (Validator.isNull(nextPageToken)) {
					break;
				}
			}

			return new JSONArray(affectedVersions);
		}
		catch (Exception exception) {
			_log.error("Unable to get affected versions", exception);
		}

		return null;
	}

	@Cacheable("issue")
	public JSONObject getIssueJSONObject(String issueKey) throws Exception {
		try {
			JSONObject issueJSONObject = new JSONObject(
				get(
					_getCredentials(),
					UriComponentsBuilder.fromUriString(
						StringBundler.concat(
							_jiraURL, _URL_REST_API_3, "/issue/", issueKey)
					).queryParam(
						"expand", "renderedFields"
					).build(
					).toUri()));

			return _transformIssueJSONObject(issueJSONObject);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get Jira issue with key " + issueKey, exception);
			}
		}

		return null;
	}

	@CacheEvict(allEntries = true, value = "affectedVersions")
	@Scheduled(
		cron = "${liferay.customer.jira.service.affected.versions.cache.eviction.cron}"
	)
	public void scheduledAffectedVersionsCacheEviction() throws Exception {
	}

	@CacheEvict(
		allEntries = true, value = {"assetObjectFieldOptions", "assetObjects"}
	)
	@Scheduled(
		cron = "${liferay.customer.jira.service.jsm.objects.cache.eviction.cron}"
	)
	public void scheduledAssetObjectsCacheEviction() throws Exception {
	}

	@CacheEvict(allEntries = true, value = {"issue", "issues"})
	@Scheduled(
		cron = "${liferay.customer.jira.service.issues.cache.eviction.cron}"
	)
	public void scheduledIssuesCacheEviction() throws Exception {
	}

	@Cacheable("issues")
	public List<JSONObject> search(
			String[] filterAffectedVersions, String[] filterCategories,
			String[] filterClassifications, String[] filterFixVersions,
			String[] filterSeverities, String keywords, String sortOrder,
			boolean hasEarlyPublishAccess)
		throws Exception {

		List<JSONObject> jsonObjects = new ArrayList<>();

		String nextPageToken = StringPool.BLANK;

		StringBundler sb = new StringBundler(49);

		sb.append("project = '");
		sb.append(_jiraSecurityVulnerabilityProject);
		sb.append("' AND ");
		sb.append(
			_toJQLCustomField(_jiraSecurityVulnerabilityFieldPublishingStatus));
		sb.append(" = 'Ready for Publishing'");

		if (hasEarlyPublishAccess) {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldPartnerPublishingDate));
			sb.append(" <= now()");
		}
		else {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldCustomerPublishingDate));
			sb.append(" <= now()");
		}

		if (ArrayUtil.isNotEmpty(filterAffectedVersions)) {
			sb.append(" AND ");
			sb.append(_FIELD_AFFECTED_VERSION);
			sb.append(" in ('");
			sb.append(StringUtil.merge(filterAffectedVersions, "','"));
			sb.append("')");
		}

		if (ArrayUtil.isNotEmpty(filterCategories)) {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(_jiraSecurityVulnerabilityFieldCategories));
			sb.append(" in ('");
			sb.append(StringUtil.merge(filterCategories, "','"));
			sb.append("')");
		}

		if (ArrayUtil.isNotEmpty(filterClassifications)) {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldIssueClassification));
			sb.append(" in ('");
			sb.append(StringUtil.merge(filterClassifications, "','"));
			sb.append("')");
		}

		if (ArrayUtil.isNotEmpty(filterFixVersions)) {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(_jiraSecurityVulnerabilityFieldFixVersions));
			sb.append(" in ('");
			sb.append(StringUtil.merge(filterFixVersions, "','"));
			sb.append("')");
		}

		if (ArrayUtil.isNotEmpty(filterSeverities)) {
			sb.append(" AND ");
			sb.append(
				_toJQLCustomField(_jiraSecurityVulnerabilityFieldSeverity));
			sb.append(" in ('");
			sb.append(StringUtil.merge(filterSeverities, "','"));
			sb.append("')");
		}

		if (Validator.isNotNull(keywords)) {
			sb.append(" AND (");
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldCustomerPortalSummary));
			sb.append(" ~ ");
			sb.append(StringUtil.quote(keywords));
			sb.append(" OR ");
			sb.append(_toJQLCustomField(_jiraSecurityVulnerabilityFieldCVEIds));
			sb.append(" ~ ");
			sb.append(StringUtil.quote(keywords));
			sb.append(")");
		}

		sb.append(" ORDER BY ");

		if (hasEarlyPublishAccess) {
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldPartnerPublishingDate));
		}
		else {
			sb.append(
				_toJQLCustomField(
					_jiraSecurityVulnerabilityFieldCustomerPublishingDate));
		}

		sb.append(" ");
		sb.append(sortOrder);
		sb.append(", ");
		sb.append(_toJQLCustomField(_jiraSecurityVulnerabilityFieldSeverity));
		sb.append(" ASC");

		String[] securityVulnerabilitiesIssueFields = {
			_FIELD_COMPONENTS, _FIELD_ISSUE_KEY, _FIELD_VERSIONS,
			_jiraSecurityVulnerabilityFieldAffectedVersionsDetails,
			_jiraSecurityVulnerabilityFieldAffects,
			_jiraSecurityVulnerabilityFieldCategories,
			_jiraSecurityVulnerabilityFieldCustomerPortalDescription,
			_jiraSecurityVulnerabilityFieldCustomerPortalSummary,
			_jiraSecurityVulnerabilityFieldCustomerPublishingDate,
			_jiraSecurityVulnerabilityFieldCVEIds,
			_jiraSecurityVulnerabilityFieldCVSSBaseScore,
			_jiraSecurityVulnerabilityFieldCVSSVectorString,
			_jiraSecurityVulnerabilityFieldCWEIds,
			_jiraSecurityVulnerabilityFieldFixVersions,
			_jiraSecurityVulnerabilityFieldIssueClassification,
			_jiraSecurityVulnerabilityFieldPartnerPublishingDate,
			_jiraSecurityVulnerabilityFieldPublishingStatus,
			_jiraSecurityVulnerabilityFieldSeverity
		};

		while (true) {
			JSONObject searchResponseJSONObject = _searchJSONObject(
				sb.toString(), 100, nextPageToken,
				securityVulnerabilitiesIssueFields);

			if (searchResponseJSONObject == null) {
				break;
			}

			JSONArray issuesJSONArray = searchResponseJSONObject.getJSONArray(
				"issues");

			for (int i = 0; i < issuesJSONArray.length(); i++) {
				jsonObjects.add(
					_transformIssueJSONObject(
						issuesJSONArray.getJSONObject(i)));
			}

			nextPageToken = searchResponseJSONObject.optString("nextPageToken");

			if (Validator.isNull(nextPageToken)) {
				break;
			}
		}

		return jsonObjects;
	}

	private JSONArray _flattenJSONArray(JSONArray jsonArray) {
		if (jsonArray == null) {
			return new JSONArray();
		}

		JSONArray flattenedJSONArray = new JSONArray();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject itemJSONObject = jsonArray.getJSONObject(i);

			String name = itemJSONObject.optString("name");

			if (Validator.isNotNull(name)) {
				flattenedJSONArray.put(name);
			}

			String value = itemJSONObject.optString("value");

			if (Validator.isNotNull(value)) {
				flattenedJSONArray.put(value);
			}
		}

		return flattenedJSONArray;
	}

	private String _getAssetObjectFieldId(JSONArray jsonArray) {
		if ((jsonArray != null) && (jsonArray.length() > 0)) {
			JSONObject assetJSONObject = jsonArray.getJSONObject(0);

			return assetJSONObject.getString("id");
		}

		return null;
	}

	private String _getCredentials() {
		Base64.Encoder encoder = Base64.getEncoder();

		String jiraUserNameAndJiraApiToken =
			_jiraAPIEmailAddress + StringPool.COLON + _jiraAPIToken;

		return "Basic " +
			encoder.encodeToString(jiraUserNameAndJiraApiToken.getBytes());
	}

	private String _getJSONObjectFieldValue(JSONObject jsonObject, String key) {
		if (jsonObject != null) {
			return jsonObject.optString(key);
		}

		return null;
	}

	private JSONObject _searchJSONObject(
			String jql, int maxResults, String nextPageToken,
			String[] returnFields)
		throws Exception {

		try {
			return new JSONObject(
				get(
					_getCredentials(),
					UriComponentsBuilder.fromUriString(
						StringBundler.concat(
							_jiraURL, _URL_REST_API_3, "/search/jql")
					).queryParam(
						"expand", "renderedFields"
					).queryParam(
						"fields", StringUtil.merge(returnFields)
					).queryParam(
						"jql", jql
					).queryParam(
						"maxResults", maxResults
					).queryParam(
						"nextPageToken", nextPageToken
					).build(
					).toUri()));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to get Jira issues with JQL " + jql, exception);
			}
		}

		return null;
	}

	private String _toJQLCustomField(String customField) {
		int pos = customField.indexOf(StringPool.UNDERLINE);

		return "cf[" + customField.substring(pos + 1) + "]";
	}

	private JSONObject _transformIssueFieldsJSONObject(
		JSONObject issueFieldsJSONObject,
		JSONObject issueRenderedFieldsJSONObject) {

		JSONObject transformedFieldsJSONObject = new JSONObject();

		if (issueFieldsJSONObject != null) {
			transformedFieldsJSONObject.put(
				"affectedVersions",
				_flattenJSONArray(
					issueFieldsJSONObject.optJSONArray(_FIELD_VERSIONS))
			).put(
				"affectedVersionsDetails",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldAffectedVersionsDetails)
			).put(
				"affects",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldAffects)
			).put(
				"categories",
				_flattenJSONArray(
					issueFieldsJSONObject.optJSONArray(
						_jiraSecurityVulnerabilityFieldCategories))
			).put(
				"components",
				_flattenJSONArray(
					issueFieldsJSONObject.optJSONArray(_FIELD_COMPONENTS))
			).put(
				"customerPortalSummary",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCustomerPortalSummary)
			).put(
				"customerPublishingDate",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCustomerPublishingDate)
			).put(
				"cveIds",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCVEIds)
			).put(
				"cvssBaseScore",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCVSSBaseScore)
			).put(
				"cvssVectorString",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCVSSVectorString)
			).put(
				"cweIds",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCWEIds)
			).put(
				"fixVersions",
				_flattenJSONArray(
					issueFieldsJSONObject.optJSONArray(
						_jiraSecurityVulnerabilityFieldFixVersions))
			).put(
				"issueClassification",
				_getJSONObjectFieldValue(
					issueFieldsJSONObject.optJSONObject(
						_jiraSecurityVulnerabilityFieldIssueClassification),
					"value")
			).put(
				"organization",
				_getAssetObjectFieldId(
					issueFieldsJSONObject.optJSONArray(
						_jiraSupportHCFieldOrganization))
			).put(
				"partnerPublishingDate",
				issueFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldPartnerPublishingDate)
			).put(
				"publishingStatus",
				_getJSONObjectFieldValue(
					issueFieldsJSONObject.optJSONObject(
						_jiraSecurityVulnerabilityFieldPublishingStatus),
					"value")
			).put(
				"severity",
				_getJSONObjectFieldValue(
					issueFieldsJSONObject.optJSONObject(
						_jiraSecurityVulnerabilityFieldSeverity),
					"value")
			).put(
				"status",
				_getJSONObjectFieldValue(
					issueFieldsJSONObject.optJSONObject(_FIELD_STATUS), "name")
			);
		}

		if (issueRenderedFieldsJSONObject != null) {
			transformedFieldsJSONObject.put(
				"customerPortalDescription",
				issueRenderedFieldsJSONObject.optString(
					_jiraSecurityVulnerabilityFieldCustomerPortalDescription));
		}

		return transformedFieldsJSONObject;
	}

	private JSONObject _transformIssueJSONObject(JSONObject issueJSONObject) {
		return new JSONObject(
		).put(
			"fields",
			_transformIssueFieldsJSONObject(
				issueJSONObject.optJSONObject("fields"),
				issueJSONObject.optJSONObject("renderedFields"))
		).put(
			"key", issueJSONObject.getString(_FIELD_ISSUE_KEY)
		);
	}

	private static final String _FIELD_AFFECTED_VERSION = "affectedVersion";

	private static final String _FIELD_COMPONENTS = "components";

	private static final String _FIELD_ISSUE_KEY = "key";

	private static final String _FIELD_STATUS = "status";

	private static final String _FIELD_VERSIONS = "versions";

	private static final String _URL_REST_API_3 = "/rest/api/3";

	private static final Log _log = LogFactory.getLog(JiraService.class);

	@Value("${liferay.learn.jira.api.email.address}")
	private String _jiraAPIEmailAddress;

	@Value("${liferay.learn.jira.api.token}")
	private String _jiraAPIToken;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.affected.versions." +
			"details}"
	)
	private String _jiraSecurityVulnerabilityFieldAffectedVersionsDetails;

	@Value("${liferay.learn.jira.security.vulnerability.field.affects}")
	private String _jiraSecurityVulnerabilityFieldAffects;

	@Value("${liferay.learn.jira.security.vulnerability.field.categories}")
	private String _jiraSecurityVulnerabilityFieldCategories;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.customer.portal." +
			"description}"
	)
	private String _jiraSecurityVulnerabilityFieldCustomerPortalDescription;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.customer.portal." +
			"summary}"
	)
	private String _jiraSecurityVulnerabilityFieldCustomerPortalSummary;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.customer." +
			"publishing.date}"
	)
	private String _jiraSecurityVulnerabilityFieldCustomerPublishingDate;

	@Value("${liferay.learn.jira.security.vulnerability.field.cve.ids}")
	private String _jiraSecurityVulnerabilityFieldCVEIds;

	@Value("${liferay.learn.jira.security.vulnerability.field.cvss.base.score}")
	private String _jiraSecurityVulnerabilityFieldCVSSBaseScore;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.cvss.vector.string}"
	)
	private String _jiraSecurityVulnerabilityFieldCVSSVectorString;

	@Value("${liferay.learn.jira.security.vulnerability.field.cwe.ids}")
	private String _jiraSecurityVulnerabilityFieldCWEIds;

	@Value("${liferay.learn.jira.security.vulnerability.field.fix.versions}")
	private String _jiraSecurityVulnerabilityFieldFixVersions;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.issue." +
			"classification}"
	)
	private String _jiraSecurityVulnerabilityFieldIssueClassification;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.partner." +
			"publishing.date}"
	)
	private String _jiraSecurityVulnerabilityFieldPartnerPublishingDate;

	@Value(
		"${liferay.learn.jira.security.vulnerability.field.publishing.status}"
	)
	private String _jiraSecurityVulnerabilityFieldPublishingStatus;

	@Value("${liferay.learn.jira.security.vulnerability.field.severity}")
	private String _jiraSecurityVulnerabilityFieldSeverity;

	@Value("${liferay.learn.jira.security.vulnerability.project}")
	private String _jiraSecurityVulnerabilityProject;

	@Value("${liferay.learn.jira.support.hc.field.organization}")
	private String _jiraSupportHCFieldOrganization;

	@Value("${liferay.learn.jira.url}")
	private String _jiraURL;

}
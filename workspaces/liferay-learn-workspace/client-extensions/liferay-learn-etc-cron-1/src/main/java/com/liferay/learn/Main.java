/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.data.engine.rest.client.dto.v2_0.DataDefinition;
import com.liferay.data.engine.rest.client.resource.v2_0.DataDefinitionResource;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.TaxonomyCategory;
import com.liferay.headless.admin.taxonomy.client.dto.v1_0.TaxonomyVocabulary;
import com.liferay.headless.admin.taxonomy.client.problem.Problem;
import com.liferay.headless.admin.taxonomy.client.resource.v1_0.TaxonomyCategoryResource;
import com.liferay.headless.admin.taxonomy.client.resource.v1_0.TaxonomyVocabularyResource;
import com.liferay.headless.admin.user.client.dto.v1_0.Site;
import com.liferay.headless.admin.user.client.resource.v1_0.SiteResource;
import com.liferay.headless.delivery.client.dto.v1_0.ContentField;
import com.liferay.headless.delivery.client.dto.v1_0.ContentFieldValue;
import com.liferay.headless.delivery.client.dto.v1_0.StructuredContent;
import com.liferay.headless.delivery.client.dto.v1_0.StructuredContentFolder;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.headless.delivery.client.permission.Permission;
import com.liferay.headless.delivery.client.resource.v1_0.StructuredContentFolderResource;
import com.liferay.headless.delivery.client.resource.v1_0.StructuredContentResource;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.URL;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Brian Wing Shun Chan
 * @author Rich Sezov
 * @author Allen Ziegenfus
 * @author Francesco Acciaro
 */
public class Main {

	public static void main(String[] arguments) throws Exception {
		List<String> argumentsList = Arrays.asList(arguments);

		boolean preflight = argumentsList.contains("--preflight");

		if (!preflight) {
			_captureConsole();
		}

		if (!preflight &&
			Files.exists(Paths.get("/tmp/liferay_jar_runner_skipped"))) {

			System.out.println(
				"liferay-learn-etc-cron-1: the setup script found nothing to " +
					"do, skipping the import");

			return;
		}

		if (!preflight &&
			!Files.exists(Paths.get("/tmp/liferay_jar_runner_set_up_ok"))) {

			System.err.println(
				"liferay-learn-etc-cron-1: the setup script did not " +
					"complete, failing the run");

			System.exit(1);
		}

		System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
		System.setProperty("sun.net.client.defaultReadTimeout", "120000");

		String liferayDataDefinitionKey = System.getenv(
			"LIFERAY_LEARN_ETC_CRON_DXP_DATA_DEFINITION_KEY");

		if (liferayDataDefinitionKey == null) {
			liferayDataDefinitionKey = "LEARN-ARTICLE";
		}

		String liferaySiteFriendlyUrlPath = System.getenv(
			"LIFERAY_LEARN_ETC_CRON_DXP_SITE_FRIENDLY_URL_PATH");

		if (liferaySiteFriendlyUrlPath == null) {
			liferaySiteFriendlyUrlPath = "liferay-learn";
		}

		String liferayURL = System.getenv("LIFERAY_LEARN_ETC_CRON_DXP_URL");

		if (liferayURL == null) {
			liferayURL = "http://localhost:8080";
		}

		String baseDir = System.getenv(
			"LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR");

		if (baseDir == null) {
			baseDir = "~/liferay-learn";
		}

		File baseDirFile = new File(baseDir);

		Main main = null;

		try {
			main = new Main(
				liferayDataDefinitionKey,
				System.getenv("LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_ID"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_SECRET"),
				liferaySiteFriendlyUrlPath, new URL(liferayURL), baseDirFile,
				GetterUtil.getBoolean(
					System.getenv("LIFERAY_LEARN_ETC_CRON_OFFLINE")),
				System.getenv("LIFERAY_LEARN_ETC_CRON_ORPHAN_EXCLUSIONS"),
				System.getenv(
					"LIFERAY_LEARN_ETC_CRON_PREFLIGHT_TOC_EXCLUSIONS"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_RESTRICTED_PATHS"),
				System.getenv("LIFERAY_LEARN_ETC_CRON_RESTRICTED_ROLES"),
				GetterUtil.getBoolean(
					System.getenv("LIFERAY_LEARN_ETC_CRON_SKIP_MD5_CHECK")),
				System.getenv("LIFERAY_LEARN_ETC_CRON_SKIP_LOCALES_CONTENT"));
		}
		catch (Exception exception) {
			if (!preflight) {
				throw exception;
			}

			System.out.println(
				StringBundler.concat(
					"[preflight] FAILED (DXP): unable to authenticate against ",
					liferayURL, " or to resolve the site \"",
					liferaySiteFriendlyUrlPath, "\" or the \"",
					liferayDataDefinitionKey, "\" content structure: ",
					exception));

			System.exit(1);
		}

		try {
			main.loadTaxonomyVocabularies();
		}
		catch (Exception exception) {
			if (!preflight) {
				throw exception;
			}

			System.out.println(
				StringBundler.concat(
					"[preflight] FAILED (DXP): unable to reconcile the ",
					"taxonomy vocabularies: ", exception));

			System.exit(1);
		}

		if (preflight) {
			System.exit(main.preflight());
		}

		main.uploadToLiferay();

		Files.createFile(Paths.get("/tmp/liferay_jar_runner_main_ok"));
	}

	public Main(
			String liferayDataDefinitionKey, String liferayOAuthClientId,
			String liferayOAuthClientSecret, String liferaySiteFriendlyUrlPath,
			URL liferayURL, File baseDir, boolean offline,
			String orphanExclusions, String preflightTocExclusions,
			String restrictedPaths, String restrictedRoles,
			boolean skipDiffCheck, String skipLocalesContent)
		throws Exception {

		_liferayOAuthClientId = liferayOAuthClientId;
		_liferayOAuthClientSecret = liferayOAuthClientSecret;
		_liferayURL = liferayURL;
		_offline = offline;
		_skipDiffCheck = skipDiffCheck;

		if (orphanExclusions != null) {
			for (String orphanExclusion : orphanExclusions.split(",")) {
				String trimmedOrphanExclusion = StringUtil.trim(
					orphanExclusion.trim(), CharPool.FORWARD_SLASH);

				if (!trimmedOrphanExclusion.isEmpty()) {
					_orphanExclusions.add(trimmedOrphanExclusion);
				}
			}
		}

		System.out.println(
			"Friendly URL paths excluded from the orphan cleanup: " +
				_orphanExclusions);

		if (preflightTocExclusions != null) {
			for (String preflightTocExclusion :
					preflightTocExclusions.split(",")) {

				String trimmedPreflightTocExclusion =
					preflightTocExclusion.trim();

				if (!trimmedPreflightTocExclusion.isEmpty()) {
					_preflightTocExclusions.add(trimmedPreflightTocExclusion);
				}
			}
		}

		System.out.println(
			"Toc entries excluded from the preflight: " +
				_preflightTocExclusions);

		if (restrictedPaths != null) {
			for (String restrictedPath : restrictedPaths.split(",")) {
				String trimmedRestrictedPath = restrictedPath.trim();

				if (!trimmedRestrictedPath.isEmpty()) {
					_restrictedPaths.add(trimmedRestrictedPath);
				}
			}
		}

		if (restrictedRoles != null) {
			for (String restrictedRole : restrictedRoles.split(",")) {
				String trimmedRestrictedRole = restrictedRole.trim();

				if (!trimmedRestrictedRole.isEmpty()) {
					_restrictedRoles.add(trimmedRestrictedRole);
				}
			}
		}

		System.out.println(
			StringBundler.concat(
				"Paths restricted to ", _restrictedRoles, ": ",
				_restrictedPaths));

		if (skipLocalesContent != null) {
			for (String skipLocale : skipLocalesContent.split(",")) {
				String trimmedSkipLocale = skipLocale.trim();

				if (!trimmedSkipLocale.isEmpty()) {
					_skipLocales.add(trimmedSkipLocale);
				}
			}
		}

		System.out.println("Locales excluded from import: " + _skipLocales);

		_docsDirectory = new DocsDirectory(baseDir);

		_navigationHelper = new NavigationHelper(
			_docsDirectory, _markdownParser, _runReport);

		System.out.println("Liferay URL: " + _liferayURL);

		if (_offline) {
			_globalSiteId = 0;
			_liferayContentStructureId = 0;
			_liferaySiteId = 0;
		}
		else {
			_initResourceBuilders(_getOAuthAuthorization());

			Site globalSite = _siteResource.getSiteByFriendlyUrlPath("global");

			_globalSiteId = globalSite.getId();

			Site liferaySite = _siteResource.getSiteByFriendlyUrlPath(
				liferaySiteFriendlyUrlPath);

			_liferaySiteId = liferaySite.getId();

			System.out.println("Liferay site ID: " + liferaySite.getId());
			System.out.println("Liferay site name: " + liferaySite.getName());

			DataDefinition dataDefinition =
				_dataDefinitionResource.
					getSiteDataDefinitionByContentTypeByDataDefinitionKey(
						liferaySite.getId(), "journal",
						liferayDataDefinitionKey);

			_liferayContentStructureId = dataDefinition.getId();
		}

		_structuredContentBuilder = new StructuredContentBuilder(
			_docsDirectory, _liferayContentStructureId, _markdownParser,
			_navigationHelper, _skipLocales, this::_getTaxonomyCategoryIds);
	}

	public void loadTaxonomyVocabularies() throws Exception {
		if (_offline) {
			return;
		}

		File file = new File(
			_docsDirectory.getDocsDirName() + "/../taxonomy-vocabularies.json");

		if (!file.exists()) {
			return;
		}

		JSONObject taxonomyVocabulariesJSONObject = new JSONObject(
			FileUtils.readFileToString(file, StandardCharsets.UTF_8));

		if (taxonomyVocabulariesJSONObject.isEmpty()) {
			return;
		}

		List<TaxonomyVocabulary> taxonomyVocabularies = new ArrayList<>();

		for (int page = 1;; page++) {
			com.liferay.headless.admin.taxonomy.client.pagination.Page
				<TaxonomyVocabulary> taxonomyVocabulariesPage;

			try {
				taxonomyVocabulariesPage =
					_taxonomyVocabularyResource.getSiteTaxonomyVocabulariesPage(
						_globalSiteId, null, null, null,
						com.liferay.headless.admin.taxonomy.client.pagination.
							Pagination.of(page, 50),
						null);
			}
			catch (Problem.ProblemException problemException) {
				Problem problem = problemException.getProblem();

				System.err.println(
					StringBundler.concat(
						"Unable to get taxonomy vocabularies: status ",
						problem.getStatus(), ", title ", problem.getTitle(),
						", detail ", problem.getDetail()));

				throw problemException;
			}

			taxonomyVocabularies.addAll(taxonomyVocabulariesPage.getItems());

			if (taxonomyVocabulariesPage.getLastPage() == page) {
				break;
			}
		}

		Map<String, String> existingTaxonomyCategories = new HashMap<>();
		Map<String, Long> existingTaxonomyVocabularies = new HashMap<>();

		for (TaxonomyVocabulary taxonomyVocabulary : taxonomyVocabularies) {
			if (StringUtil.equals(
					taxonomyVocabulary.getExternalReferenceCode(),
					"RESOURCE_TYPE")) {

				TaxonomyCategory taxonomyCategory =
					_taxonomyCategoryResource.
						getTaxonomyVocabularyTaxonomyCategoryByExternalReferenceCode(
							taxonomyVocabulary.getId(),
							"OFFICIAL_DOCUMENTATION");

				_taxonomyCategoriesJSONObject.put(
					taxonomyCategory.getExternalReferenceCode(),
					taxonomyCategory.getId());

				continue;
			}

			existingTaxonomyVocabularies.put(
				taxonomyVocabulary.getName(), taxonomyVocabulary.getId());

			for (int page = 1;; page++) {
				com.liferay.headless.admin.taxonomy.client.pagination.Page
					<TaxonomyCategory> taxonomyCategoriesPage =
						_taxonomyCategoryResource.
							getTaxonomyVocabularyTaxonomyCategoriesPage(
								taxonomyVocabulary.getId(), true, null, null,
								null,
								com.liferay.headless.admin.taxonomy.client.
									pagination.Pagination.of(page, 50),
								null);

				for (TaxonomyCategory taxonomyCategory :
						taxonomyCategoriesPage.getItems()) {

					existingTaxonomyCategories.put(
						_getTaxonomyCategoryKey(
							taxonomyVocabulary.getName(),
							taxonomyCategory.getName()),
						taxonomyCategory.getId());
				}

				if (taxonomyCategoriesPage.getLastPage() == page) {
					break;
				}
			}
		}

		JSONArray jsonArray = taxonomyVocabulariesJSONObject.getJSONArray(
			"taxonomyVocabularies");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject taxonomyVocabularyJSONObject = jsonArray.getJSONObject(
				i);

			String name = taxonomyVocabularyJSONObject.getString("name");

			Long taxonomyVocabularyId = existingTaxonomyVocabularies.get(name);

			if (taxonomyVocabularyId == null) {
				TaxonomyVocabulary taxonomyVocabulary =
					new TaxonomyVocabulary();

				taxonomyVocabulary.setName(() -> name);

				taxonomyVocabulary =
					_taxonomyVocabularyResource.postSiteTaxonomyVocabulary(
						_globalSiteId, taxonomyVocabulary);

				taxonomyVocabularyId = taxonomyVocabulary.getId();
			}

			_loadTaxonomyCategories(
				existingTaxonomyCategories, null,
				taxonomyVocabularyJSONObject.optJSONArray("taxonomyCategories"),
				taxonomyVocabularyId, name);
		}
	}

	public int preflight() throws Exception {
		System.out.println(
			"[preflight] DXP is reachable, the site, content structure and " +
				"taxonomies are resolved.");

		MarkdownValidator markdownValidator = new MarkdownValidator(
			_docsDirectory, _markdownParser, _preflightTocExclusions,
			_taxonomyCategoriesJSONObject::has);

		return markdownValidator.validate();
	}

	public void uploadToLiferay() throws Exception {
		int addedStructuredContentCount = 0;
		Set<Long> importedStructuredContentIds = new HashSet<>();
		int skippedStructuredContentCount = 0;
		int updatedStructuredContentCount = 0;

		List<StructuredContent> siteStructuredContents =
			_getSiteStructuredContents(_liferaySiteId);

		System.out.println(
			"Site has " + siteStructuredContents.size() +
				" structured contents");

		Map<String, StructuredContent> externalReferenceCodeStructuredContents =
			_getExternalReferenceCodeStructuredContents(siteStructuredContents);
		Map<String, StructuredContent> friendlyUrlPathStructuredContents =
			_getFriendlyUrlPathStructuredContents(siteStructuredContents);

		Set<String> divergentExternalReferenceCodes =
			_getDivergentExternalReferenceCodes(
				externalReferenceCodeStructuredContents);

		for (String fileName : _docsDirectory.getFileNames()) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			if (_offline) {
				JSONObject jsonObject = new JSONObject(
					_structuredContentBuilder.toStructuredContent(fileName));

				_write(
					jsonObject.toString(4), "build/structured-content",
					new File(fileName));

				continue;
			}

			_renewOAuthAuthorization();

			StructuredContent desiredStructuredContent = null;
			Long publishedStructuredContentId = null;

			try {
				StructuredContent structuredContent =
					_structuredContentBuilder.toStructuredContent(fileName);

				desiredStructuredContent = structuredContent;

				StructuredContent importedStructuredContent = null;

				StructuredContent siteStructuredContent =
					externalReferenceCodeStructuredContents.get(
						structuredContent.getExternalReferenceCode());

				if ((siteStructuredContent != null) &&
					_isMovedStructuredContent(
						siteStructuredContent, structuredContent)) {

					importedStructuredContentIds.add(
						siteStructuredContent.getId());

					System.out.println(
						StringBundler.concat(
							"Moving structured content ",
							siteStructuredContent.getFriendlyUrlPath(), " to ",
							structuredContent.getFriendlyUrlPath()));

					_structuredContentResource.deleteStructuredContent(
						siteStructuredContent.getId());

					friendlyUrlPathStructuredContents.remove(
						siteStructuredContent.getFriendlyUrlPath());

					siteStructuredContent = null;
				}

				if (siteStructuredContent != null) {
					importedStructuredContentIds.add(
						siteStructuredContent.getId());

					if (StringUtil.equals(
							_structuredContentBuilder.generateMD5Hex(
								new File(fileName)),
							_getMD5Hex(siteStructuredContent)) &&
						StringUtil.equals(
							structuredContent.getFriendlyUrlPath(),
							siteStructuredContent.getFriendlyUrlPath()) &&
						!divergentExternalReferenceCodes.contains(
							structuredContent.getExternalReferenceCode()) &&
						!_skipDiffCheck) {

						skippedStructuredContentCount++;

						System.out.println(
							"Skipping structured content (same md5Hex) " +
								structuredContent.getFriendlyUrlPath());

						continue;
					}

					System.out.println(
						"Updating structured content " +
							structuredContent.getFriendlyUrlPath());

					publishedStructuredContentId =
						siteStructuredContent.getId();

					importedStructuredContent =
						_structuredContentResource.putStructuredContent(
							siteStructuredContent.getId(), structuredContent);

					try {
						_structuredContentResource.
							putStructuredContentPermissionsPage(
								importedStructuredContent.getId(),
								_getPermissions(
									fileName,
									importedStructuredContent.getId()));
					}
					catch (Exception exception) {
						throw new Exception(
							"Unable to set permissions: " + exception,
							exception);
					}

					updatedStructuredContentCount++;
				}
				else {
					StructuredContent friendlyUrlPathSiteStructuredContent =
						friendlyUrlPathStructuredContents.get(
							structuredContent.getFriendlyUrlPath());

					if (friendlyUrlPathSiteStructuredContent != null) {
						importedStructuredContentIds.add(
							friendlyUrlPathSiteStructuredContent.getId());

						System.out.println(
							"Deleting structured content " +
								structuredContent.getFriendlyUrlPath());

						_structuredContentResource.deleteStructuredContent(
							friendlyUrlPathSiteStructuredContent.getId());
					}

					System.out.println(
						"Adding structured content " +
							structuredContent.getFriendlyUrlPath());

					structuredContent.setStructuredContentFolderId(
						() -> _getStructuredContentFolderId(
							FilenameUtils.getPathNoEndSeparator(
								fileName.substring(
									_docsDirectory.getDocsDirName(
									).length()))));

					structuredContent.setPermissions(
						() -> _getPermissions(
							fileName, structuredContent.getId()));

					importedStructuredContent =
						_structuredContentResource.
							postStructuredContentFolderStructuredContent(
								structuredContent.
									getStructuredContentFolderId(),
								structuredContent);

					publishedStructuredContentId =
						importedStructuredContent.getId();

					importedStructuredContent =
						_structuredContentResource.putStructuredContent(
							publishedStructuredContentId, structuredContent);

					addedStructuredContentCount++;
				}

				if (!Objects.equals(
						importedStructuredContent.getFriendlyUrlPath(),
						structuredContent.getFriendlyUrlPath())) {

					String friendlyUrlPath =
						importedStructuredContent.getFriendlyUrlPath();

					_structuredContentResource.deleteStructuredContent(
						importedStructuredContent.getId());

					publishedStructuredContentId = null;

					throw new Exception(
						"Modified friendly URL path " + friendlyUrlPath);
				}

				_publishJapaneseStructuredContent(
					importedStructuredContent.getId(), fileName);
			}
			catch (Exception exception) {
				_runReport.addError(fileName + ": " + exception);

				if (publishedStructuredContentId != null) {
					_invalidateMD5Hex(
						publishedStructuredContentId, desiredStructuredContent);
				}
			}
		}

		int deletedStructuredContentCount = _deleteOrphans(
			_getIdStructuredContents(siteStructuredContents),
			importedStructuredContentIds);

		_runReport.printSummary(
			addedStructuredContentCount, deletedStructuredContentCount,
			updatedStructuredContentCount);

		SlackNotifier slackNotifier = new SlackNotifier(_runReport);

		slackNotifier.sendReport(
			addedStructuredContentCount, deletedStructuredContentCount,
			skippedStructuredContentCount, updatedStructuredContentCount);

		_runReport.printErrors();

		if (_runReport.hasErrors()) {
			throw new Exception(_runReport.getErrorCount() + " error messages");
		}

		_runReport.recordSuccess();
	}

	private static void _captureConsole() {
		try {
			OutputStream outputStream = Files.newOutputStream(
				Paths.get("/tmp/liferay_learn_run.log"),
				StandardOpenOption.APPEND, StandardOpenOption.CREATE);

			System.setErr(
				new PrintStream(
					new TeeOutputStream(System.err, outputStream), true,
					StandardCharsets.UTF_8.name()));
			System.setOut(
				new PrintStream(
					new TeeOutputStream(System.out, outputStream), true,
					StandardCharsets.UTF_8.name()));
		}
		catch (Exception exception) {
			System.out.println(
				"Unable to capture the console output: " + exception);
		}
	}

	private int _deleteOrphans(
		Map<Long, StructuredContent> idStructuredContents,
		Set<Long> importedStructuredContentIds) {

		Set<Long> orphanedStructuredContentIds = new HashSet<>(
			idStructuredContents.keySet());

		orphanedStructuredContentIds.removeAll(importedStructuredContentIds);

		int excludedCount = 0;

		Iterator<Long> iterator = orphanedStructuredContentIds.iterator();

		while (iterator.hasNext()) {
			StructuredContent structuredContent = idStructuredContents.get(
				iterator.next());

			if (!_isExcludedOrphan(structuredContent.getFriendlyUrlPath())) {
				continue;
			}

			excludedCount++;

			iterator.remove();
		}

		if (excludedCount > 0) {
			System.out.println(
				excludedCount +
					" orphaned structured contents were kept (excluded).");
		}

		int orphanCount = orphanedStructuredContentIds.size();

		if (!_isOrphanCleanupEnabled()) {
			System.out.println(
				orphanCount +
					" orphaned structured contents were kept (cleanup " +
						"disabled).");

			return 0;
		}

		if (_runReport.hasErrors()) {
			System.out.println(
				StringBundler.concat(
					"Skipping orphan cleanup because the run has ",
					_runReport.getErrorCount(), " errors: ", orphanCount,
					" orphaned structured contents were kept."));

			return 0;
		}

		int inventoryCount = idStructuredContents.size();

		int orphanThreshold = _getOrphanThreshold(inventoryCount);

		if (orphanCount > orphanThreshold) {
			_runReport.addError(
				StringBundler.concat(
					"Orphan cleanup aborted: ", orphanCount, " orphans exceed ",
					"the threshold of ", orphanThreshold,
					" for an inventory of ", inventoryCount,
					". No structured content was deleted."));

			return 0;
		}

		for (Long orphanedStructuredContentId : orphanedStructuredContentIds) {
			StructuredContent structuredContent = idStructuredContents.get(
				orphanedStructuredContentId);

			try {
				_renewOAuthAuthorization();

				System.out.println(
					"Deleting orphaned structured content " +
						structuredContent.getFriendlyUrlPath());

				_structuredContentResource.deleteStructuredContent(
					orphanedStructuredContentId);
			}
			catch (Exception exception) {
				_runReport.addError(
					structuredContent.getFriendlyUrlPath() + ": " + exception);
			}
		}

		return orphanCount;
	}

	private void _describeDivergence(
		StructuredContent structuredContent, String html) {

		String content = _getContent(structuredContent);

		int index = 0;

		while ((index < content.length()) && (index < html.length()) &&
			   (content.charAt(index) == html.charAt(index))) {

			index++;
		}

		System.out.println(
			StringBundler.concat(
				"First divergence in ", structuredContent.getFriendlyUrlPath(),
				": the published content is ", content.length(),
				" characters, the generated HTML is ", html.length(),
				", they differ from character ", index));

		String publishedExcerpt = StringUtil.replace(
			content.substring(
				Math.max(0, index - 60),
				Math.min(content.length(), index + 60)),
			CharPool.NEW_LINE, "\\n");

		System.out.println("Published: " + publishedExcerpt);

		String generatedExcerpt = StringUtil.replace(
			html.substring(
				Math.max(0, index - 60), Math.min(html.length(), index + 60)),
			CharPool.NEW_LINE, "\\n");

		System.out.println("Generated: " + generatedExcerpt);
	}

	private String _getContent(StructuredContent structuredContent) {
		ContentField[] contentFields = structuredContent.getContentFields();

		for (ContentField contentField : contentFields) {
			if (!StringUtil.equals(contentField.getName(), "content")) {
				continue;
			}

			ContentFieldValue contentFieldValue =
				contentField.getContentFieldValue();

			return contentFieldValue.getData();
		}

		return StringPool.BLANK;
	}

	private int _getDivergenceThreshold(int publishedCount) {
		int percentage = GetterUtil.getInteger(
			System.getenv("LIFERAY_LEARN_ETC_CRON_VERIFY_THRESHOLD_PERCENTAGE"),
			5);

		int threshold = (publishedCount * percentage) / 100;

		if (threshold < 5) {
			return 5;
		}

		return threshold;
	}

	private Set<String> _getDivergentExternalReferenceCodes(
			Map<String, StructuredContent>
				externalReferenceCodeStructuredContents)
		throws Exception {

		Set<String> divergentExternalReferenceCodes = new TreeSet<>();

		List<String> divergentFriendlyUrlPaths = new ArrayList<>();
		int publishedCount = 0;

		for (String fileName : _docsDirectory.getFileNames()) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			File file = new File(fileName);

			String uuid = _markdownParser.getUuid(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8));

			StructuredContent siteStructuredContent =
				externalReferenceCodeStructuredContents.get(uuid);

			if (siteStructuredContent == null) {
				continue;
			}

			publishedCount++;

			if (!StringUtil.equals(
					_structuredContentBuilder.generateMD5Hex(file),
					_getMD5Hex(siteStructuredContent))) {

				continue;
			}

			String html = FileUtils.readFileToString(
				_docsDirectory.getHTMLFile(file), StandardCharsets.UTF_8);

			if (StringUtil.equals(
					TextUtil.normalizeHTML(
						TextUtil.unescapeUnicode(
							_getContent(siteStructuredContent))),
					TextUtil.normalizeHTML(html))) {

				continue;
			}

			if (divergentExternalReferenceCodes.isEmpty()) {
				_describeDivergence(siteStructuredContent, html);
			}

			divergentExternalReferenceCodes.add(uuid);
			divergentFriendlyUrlPaths.add(
				siteStructuredContent.getFriendlyUrlPath());
		}

		if (divergentExternalReferenceCodes.isEmpty()) {
			System.out.println(
				StringBundler.concat(
					"Verified ", publishedCount,
					" published structured contents against the repository: ",
					"none diverged."));

			return divergentExternalReferenceCodes;
		}

		int threshold = _getDivergenceThreshold(publishedCount);

		if (divergentExternalReferenceCodes.size() > threshold) {
			_runReport.addError(
				StringBundler.concat(
					"Verification aborted: ",
					divergentExternalReferenceCodes.size(), " of ",
					publishedCount,
					" published structured contents diverge from the ",
					"repository, above the threshold of ", threshold,
					". Nothing was realigned: such a number points at the ",
					"comparison itself rather than at manual edits."));

			return Collections.emptySet();
		}

		System.out.println(
			StringBundler.concat(
				"Verification found ", divergentExternalReferenceCodes.size(),
				" of ", publishedCount,
				" published structured contents diverging from the ",
				"repository, realigning them:"));

		for (String divergentFriendlyUrlPath : divergentFriendlyUrlPaths) {
			System.out.println(
				"Diverged from the repository: " + divergentFriendlyUrlPath);
		}

		return divergentExternalReferenceCodes;
	}

	private Map<String, StructuredContent>
		_getExternalReferenceCodeStructuredContents(
			List<StructuredContent> structuredContents) {

		Map<String, StructuredContent> externalReferenceCodeStructuredContents =
			new HashMap<>();

		for (StructuredContent structuredContent : structuredContents) {
			if (structuredContent.getContentStructureId() ==
					_liferayContentStructureId) {

				externalReferenceCodeStructuredContents.put(
					structuredContent.getExternalReferenceCode(),
					structuredContent);
			}
		}

		return externalReferenceCodeStructuredContents;
	}

	private Map<String, StructuredContent>
		_getFriendlyUrlPathStructuredContents(
			List<StructuredContent> structuredContents) {

		Map<String, StructuredContent> friendlyUrlPathStructuredContents =
			new HashMap<>();

		for (StructuredContent structuredContent : structuredContents) {
			if (structuredContent.getContentStructureId() ==
					_liferayContentStructureId) {

				friendlyUrlPathStructuredContents.put(
					structuredContent.getFriendlyUrlPath(), structuredContent);
			}
		}

		return friendlyUrlPathStructuredContents;
	}

	private Map<Long, StructuredContent> _getIdStructuredContents(
		List<StructuredContent> structuredContents) {

		Map<Long, StructuredContent> idStructuredContents = new HashMap<>();

		for (StructuredContent structuredContent : structuredContents) {
			if (structuredContent.getContentStructureId() ==
					_liferayContentStructureId) {

				idStructuredContents.put(
					structuredContent.getId(), structuredContent);
			}
		}

		return idStructuredContents;
	}

	private String _getMD5Hex(StructuredContent structuredContent) {
		ContentField[] contentFields = structuredContent.getContentFields();

		for (ContentField contentField : contentFields) {
			if (!StringUtil.equals(contentField.getName(), "md5Hex")) {
				continue;
			}

			ContentFieldValue contentFieldValue =
				contentField.getContentFieldValue();

			return contentFieldValue.getData();
		}

		return StringPool.BLANK;
	}

	private String _getOAuthAuthorization() throws Exception {
		HttpPost httpPost = new HttpPost(_liferayURL + "/o/oauth2/token");

		httpPost.setEntity(
			new UrlEncodedFormEntity(
				Arrays.asList(
					new BasicNameValuePair("client_id", _liferayOAuthClientId),
					new BasicNameValuePair(
						"client_secret", _liferayOAuthClientSecret),
					new BasicNameValuePair(
						"grant_type", "client_credentials"))));
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

		httpClientBuilder.setDefaultRequestConfig(
			RequestConfig.custom(
			).setConnectionRequestTimeout(
				30000
			).setConnectTimeout(
				30000
			).setSocketTimeout(
				120000
			).build());

		try (CloseableHttpClient closeableHttpClient =
				httpClientBuilder.build();

			CloseableHttpResponse closeableHttpResponse =
				closeableHttpClient.execute(httpPost)) {

			StatusLine statusLine = closeableHttpResponse.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				JSONObject jsonObject = new JSONObject(
					EntityUtils.toString(
						closeableHttpResponse.getEntity(),
						Charset.defaultCharset()));

				_oauthExpirationMillis =
					jsonObject.getLong("expires_in") * 1000;

				_oauthIssuedMillis = System.currentTimeMillis();

				return jsonObject.getString("token_type") + " " +
					jsonObject.getString("access_token");
			}

			throw new Exception("Unable to get OAuth authorization");
		}
	}

	private int _getOrphanThreshold(int inventoryCount) {
		int percentage = GetterUtil.getInteger(
			System.getenv("LIFERAY_LEARN_ETC_CRON_ORPHAN_THRESHOLD_PERCENTAGE"),
			5);

		int threshold = (inventoryCount * percentage) / 100;

		if (threshold < 5) {
			return 5;
		}

		return threshold;
	}

	private Permission[] _getPermissions(
			String fileName, Long structuredContentId)
		throws Exception {

		List<Permission> permissions = new ArrayList<>();

		if (structuredContentId != null) {
			Page<Permission> structuredContentPermissionsPage =
				_structuredContentResource.getStructuredContentPermissionsPage(
					structuredContentId, null);

			for (Permission permission :
					structuredContentPermissionsPage.getItems()) {

				if (Objects.equals(permission.getRoleName(), "Owner")) {
					continue;
				}

				permission.setActionIds(new String[0]);

				permissions.add(permission);
			}
		}

		if (_isRestrictedFile(fileName)) {
			for (String restrictedRole : _restrictedRoles) {
				permissions.add(
					new Permission() {
						{
							setActionIds(
								new String[] {"ADD_DISCUSSION", "VIEW"});
							setRoleName(restrictedRole);
						}
					});
			}

			permissions.add(
				new Permission() {
					{
						setActionIds(new String[0]);
						setRoleName("Guest");
					}
				});

			return permissions.toArray(new Permission[0]);
		}

		Map<String, Object> data = _markdownParser.getFrontMatter(
			new File(fileName));

		if ((data == null) || !data.containsKey("visibility")) {
			permissions.add(
				new Permission() {
					{
						setActionIds(new String[] {"VIEW"});
						setRoleName("Guest");
					}
				});

			return permissions.toArray(new Permission[0]);
		}

		Object visibilityObject = data.get("visibility");

		if (!(visibilityObject instanceof List)) {
			return null;
		}

		for (Object object : (List)visibilityObject) {
			if (!(object instanceof String)) {
				continue;
			}

			permissions.add(
				new Permission() {
					{
						setActionIds(new String[] {"ADD_DISCUSSION", "VIEW"});
						setRoleName((String)object);
					}
				});
		}

		if (permissions.isEmpty()) {
			return null;
		}

		permissions.add(
			new Permission() {
				{
					setActionIds(new String[0]);
					setRoleName("Guest");
				}
			});

		return permissions.toArray(new Permission[0]);
	}

	private List<StructuredContent> _getSiteStructuredContents(long siteId)
		throws Exception {

		if (_offline) {
			return Collections.emptyList();
		}

		List<StructuredContent> structuredContents = new ArrayList<>();

		long lastPage = 0;

		for (int page = 1;; page++) {
			Page<StructuredContent> structuredContentsPage = null;

			for (int attempt = 1;; attempt++) {
				_renewOAuthAuthorization();

				try {
					structuredContentsPage =
						_structuredContentResource.
							getSiteStructuredContentsPage(
								siteId, true, null, null, null,
								Pagination.of(page, 50), null);

					break;
				}
				catch (Exception exception) {
					if (attempt >= _READ_ATTEMPT_COUNT) {
						throw new Exception(
							StringBundler.concat(
								"Unable to read page ", page, " of ", lastPage,
								" after reading ", structuredContents.size(),
								" structured contents in ", attempt,
								" attempts: ", exception),
							exception);
					}

					System.err.println(
						StringBundler.concat(
							"Retrying page ", page, " of ", lastPage,
							" after attempt ", attempt, " failed: ",
							exception));

					Thread.sleep(_READ_ATTEMPT_DELAY * attempt);
				}
			}

			structuredContents.addAll(structuredContentsPage.getItems());

			lastPage = structuredContentsPage.getLastPage();

			if (lastPage == page) {
				break;
			}
		}

		return structuredContents;
	}

	private Long _getStructuredContentFolderId(String fileName)
		throws Exception {

		Long structuredContentFolderId = 0L;

		for (String dirName : _docsDirectory.getDirNames(fileName)) {
			structuredContentFolderId = _getStructuredContentFolderId(
				dirName, structuredContentFolderId);
		}

		return structuredContentFolderId;
	}

	private Long _getStructuredContentFolderId(
			String dirName, Long parentStructuredContentFolderId)
		throws Exception {

		String key = parentStructuredContentFolderId + "#" + dirName;

		Long structuredContentFolderId = _structuredContentFolderIds.get(key);

		if (structuredContentFolderId != null) {
			return structuredContentFolderId;
		}

		StructuredContentFolder structuredContentFolder = null;

		if (parentStructuredContentFolderId == 0) {
			Page<StructuredContentFolder> page =
				_structuredContentFolderResource.
					getSiteStructuredContentFoldersPage(
						_liferaySiteId, null, null, null,
						"name eq '" + dirName + "'", null, null);

			structuredContentFolder = page.fetchFirstItem();

			if (structuredContentFolder == null) {
				structuredContentFolder =
					_structuredContentFolderResource.
						postSiteStructuredContentFolder(
							_liferaySiteId,
							new StructuredContentFolder() {
								{
									setDescription(() -> "");
									setName(() -> dirName);
									setViewableBy(() -> ViewableBy.ANYONE);
								}
							});
			}
		}
		else {
			Page<StructuredContentFolder> page =
				_structuredContentFolderResource.
					getStructuredContentFolderStructuredContentFoldersPage(
						parentStructuredContentFolderId, null, null,
						"name eq '" + dirName + "'", null, null);

			structuredContentFolder = page.fetchFirstItem();

			if (structuredContentFolder == null) {
				structuredContentFolder =
					_structuredContentFolderResource.
						postStructuredContentFolderStructuredContentFolder(
							parentStructuredContentFolderId,
							new StructuredContentFolder() {
								{
									setDescription(() -> "");
									setName(() -> dirName);
									setViewableBy(() -> ViewableBy.ANYONE);
								}
							});
			}
		}

		structuredContentFolderId = structuredContentFolder.getId();

		_structuredContentFolderIds.put(key, structuredContentFolderId);

		return structuredContentFolderId;
	}

	private Long[] _getTaxonomyCategoryIds(String text) {
		Map<String, Object> data = _markdownParser.getFrontMatter(text);

		if ((data == null) || !data.containsKey("taxonomy-category-names")) {
			return new Long[0];
		}

		Object taxonomyCategoryNames = data.get("taxonomy-category-names");

		if (!(taxonomyCategoryNames instanceof List)) {
			return new Long[0];
		}

		List<Long> taxonomyCategoryIds = new ArrayList<>();

		try {
			taxonomyCategoryIds.add(
				_taxonomyCategoriesJSONObject.getLong(
					"OFFICIAL_DOCUMENTATION"));
		}
		catch (Exception exception) {
			_runReport.addError(exception.getMessage());
		}

		for (Object taxonomyCategoryNameObject : (List)taxonomyCategoryNames) {
			if (!(taxonomyCategoryNameObject instanceof String)) {
				continue;
			}

			String taxonomyCategoryName = (String)taxonomyCategoryNameObject;

			if (!_taxonomyCategoriesJSONObject.has(taxonomyCategoryName)) {
				_runReport.addWarning(
					"No taxonomy category exists with the name: " +
						taxonomyCategoryName);

				continue;
			}

			taxonomyCategoryIds.add(
				_taxonomyCategoriesJSONObject.getLong(taxonomyCategoryName));
		}

		if (taxonomyCategoryIds.isEmpty()) {
			return new Long[0];
		}

		return taxonomyCategoryIds.toArray(new Long[0]);
	}

	private String _getTaxonomyCategoryKey(
		String taxonomyVocabularyName, String taxonomyCategoryName) {

		return taxonomyVocabularyName + StringPool.COLON + taxonomyCategoryName;
	}

	private void _initResourceBuilders(String authorization) throws Exception {
		DataDefinitionResource.Builder dataDefinitionResourceBuilder =
			DataDefinitionResource.builder();

		_dataDefinitionResource = dataDefinitionResourceBuilder.header(
			"Authorization", authorization
		).endpoint(
			_liferayURL
		).build();

		SiteResource.Builder siteResourceBuilder = SiteResource.builder();

		_siteResource = siteResourceBuilder.header(
			"Authorization", authorization
		).endpoint(
			_liferayURL
		).build();

		StructuredContentFolderResource.Builder
			structuredContentFolderResourceBuilder =
				StructuredContentFolderResource.builder();

		_structuredContentFolderResource =
			structuredContentFolderResourceBuilder.header(
				"Authorization", authorization
			).endpoint(
				_liferayURL
			).build();

		StructuredContentResource.Builder
			japaneseStructuredContentResourceBuilder =
				StructuredContentResource.builder();

		_japaneseStructuredContentResource =
			japaneseStructuredContentResourceBuilder.header(
				"Accept-Language", "ja-JP"
			).header(
				"Authorization", authorization
			).endpoint(
				_liferayURL
			).build();

		StructuredContentResource.Builder structuredContentResourceBuilder =
			StructuredContentResource.builder();

		_structuredContentResource = structuredContentResourceBuilder.header(
			"Authorization", authorization
		).endpoint(
			_liferayURL
		).build();

		TaxonomyCategoryResource.Builder taxonomyCategoryResourceBuilder =
			TaxonomyCategoryResource.builder();

		_taxonomyCategoryResource = taxonomyCategoryResourceBuilder.header(
			"Authorization", authorization
		).endpoint(
			_liferayURL
		).build();

		TaxonomyVocabularyResource.Builder taxonomyVocabularyResourceBuilder =
			TaxonomyVocabularyResource.builder();

		_taxonomyVocabularyResource = taxonomyVocabularyResourceBuilder.header(
			"Authorization", authorization
		).endpoint(
			_liferayURL
		).build();
	}

	private void _invalidateMD5Hex(
		long structuredContentId, StructuredContent structuredContent) {

		try {
			ContentField[] contentFields = structuredContent.getContentFields();

			for (ContentField contentField : contentFields) {
				if (!StringUtil.equals(contentField.getName(), "md5Hex")) {
					continue;
				}

				ContentFieldValue contentFieldValue =
					contentField.getContentFieldValue();

				contentFieldValue.setData(() -> StringPool.BLANK);
			}

			_structuredContentResource.putStructuredContent(
				structuredContentId, structuredContent);

			System.out.println(
				"Invalidated md5Hex so the next run retries " +
					structuredContent.getFriendlyUrlPath());
		}
		catch (Exception exception) {
			_runReport.addError(
				StringBundler.concat(
					"Unable to invalidate md5Hex for ",
					structuredContent.getFriendlyUrlPath(), ": ", exception));
		}
	}

	private boolean _isExcludedOrphan(String friendlyUrlPath) {
		if (_orphanExclusions.isEmpty() || (friendlyUrlPath == null)) {
			return false;
		}

		if (friendlyUrlPath.startsWith(StringPool.FORWARD_SLASH)) {
			friendlyUrlPath = friendlyUrlPath.substring(1);
		}

		for (String orphanExclusion : _orphanExclusions) {
			if (friendlyUrlPath.equals(orphanExclusion) ||
				friendlyUrlPath.startsWith(
					orphanExclusion + StringPool.FORWARD_SLASH)) {

				return true;
			}
		}

		return false;
	}

	private boolean _isMovedStructuredContent(
		StructuredContent siteStructuredContent,
		StructuredContent structuredContent) {

		String friendlyUrlPath = siteStructuredContent.getFriendlyUrlPath();

		if (friendlyUrlPath == null) {
			return false;
		}

		return !StringUtil.equals(
			FilenameUtils.getPathNoEndSeparator(friendlyUrlPath),
			FilenameUtils.getPathNoEndSeparator(
				structuredContent.getFriendlyUrlPath()));
	}

	private boolean _isOrphanCleanupEnabled() {
		if (Objects.equals(
				System.getenv("LIFERAY_LEARN_ETC_CRON_ORPHAN_CLEANUP"),
				"true") &&
			!Objects.equals(
				System.getenv("LIFERAY_LEARN_ETC_CRON_PARTIAL"), "true")) {

			return true;
		}

		return false;
	}

	private boolean _isRestrictedFile(String fileName) throws Exception {
		if (_restrictedPaths.isEmpty()) {
			return false;
		}

		String docsRelativePath = _docsDirectory.getRelativePath(
			new File(fileName));

		if (docsRelativePath == null) {
			return false;
		}

		for (String restrictedPath : _restrictedPaths) {
			if (docsRelativePath.equals(restrictedPath) ||
				docsRelativePath.startsWith(restrictedPath + "/")) {

				return true;
			}
		}

		return false;
	}

	private void _loadTaxonomyCategories(
			Map<String, String> existingTaxonomyCategories,
			String parentTaxonomyCategoryId,
			JSONArray taxonomyCategoriesJSONArray, long taxonomyVocabularyId,
			String taxonomyVocabularyName)
		throws Exception {

		if (taxonomyCategoriesJSONArray == null) {
			return;
		}

		for (int i = 0; i < taxonomyCategoriesJSONArray.length(); i++) {
			JSONObject taxonomyCategoryJSONObject =
				taxonomyCategoriesJSONArray.getJSONObject(i);

			String name = taxonomyCategoryJSONObject.getString("name");

			String taxonomyCategoryKey = _getTaxonomyCategoryKey(
				taxonomyVocabularyName, name);

			if (!existingTaxonomyCategories.containsKey(taxonomyCategoryKey)) {
				TaxonomyCategory taxonomyCategory = new TaxonomyCategory();

				taxonomyCategory.setName(() -> name);
				taxonomyCategory.setTaxonomyVocabularyId(
					() -> taxonomyVocabularyId);

				if (parentTaxonomyCategoryId != null) {
					taxonomyCategory =
						_taxonomyCategoryResource.
							postTaxonomyCategoryTaxonomyCategory(
								parentTaxonomyCategoryId, taxonomyCategory);
				}
				else {
					taxonomyCategory =
						_taxonomyCategoryResource.
							postTaxonomyVocabularyTaxonomyCategory(
								taxonomyCategory.getTaxonomyVocabularyId(),
								taxonomyCategory);
				}

				existingTaxonomyCategories.put(
					taxonomyCategoryKey, taxonomyCategory.getId());
			}

			String taxonomyCategoryId = existingTaxonomyCategories.get(
				taxonomyCategoryKey);

			_taxonomyCategoriesJSONObject.put(
				taxonomyCategoryKey, taxonomyCategoryId);

			if (!_taxonomyCategoriesJSONObject.has(name)) {
				_taxonomyCategoriesJSONObject.put(name, taxonomyCategoryId);
			}

			_loadTaxonomyCategories(
				existingTaxonomyCategories, taxonomyCategoryId,
				taxonomyCategoryJSONObject.optJSONArray("taxonomyCategories"),
				taxonomyVocabularyId, taxonomyVocabularyName);
		}
	}

	private void _publishJapaneseStructuredContent(
			long structuredContentId, String fileName)
		throws Exception {

		if (_skipLocales.contains("ja")) {
			return;
		}

		File japaneseFile = new File(
			StringUtil.replace(fileName, "/en/", "/ja/"));

		if (!japaneseFile.exists()) {
			return;
		}

		_japaneseStructuredContentResource.putStructuredContent(
			structuredContentId,
			_structuredContentBuilder.toJapaneseStructuredContent(
				japaneseFile));
	}

	private void _renewOAuthAuthorization() throws Exception {
		if (_offline) {
			return;
		}

		long delta = System.currentTimeMillis() - _oauthIssuedMillis;

		if (delta > (_oauthExpirationMillis - 100000)) {
			_initResourceBuilders(_getOAuthAuthorization());
		}
	}

	private void _write(String content, String dirName, File markdownFile)
		throws Exception {

		String markdownFileName = markdownFile.getCanonicalPath();

		markdownFileName = markdownFileName.substring(
			_docsDirectory.getDocsDirName(
			).length());

		File file = new File(dirName + markdownFileName);

		FileUtils.forceMkdirParent(file);

		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
	}

	private static final int _READ_ATTEMPT_COUNT = 3;

	private static final long _READ_ATTEMPT_DELAY = 3000;

	private DataDefinitionResource _dataDefinitionResource;
	private final DocsDirectory _docsDirectory;
	private final long _globalSiteId;
	private StructuredContentResource _japaneseStructuredContentResource;
	private final long _liferayContentStructureId;
	private final String _liferayOAuthClientId;
	private final String _liferayOAuthClientSecret;
	private final long _liferaySiteId;
	private final URL _liferayURL;
	private final MarkdownParser _markdownParser = new MarkdownParser();
	private final NavigationHelper _navigationHelper;
	private long _oauthExpirationMillis;
	private long _oauthIssuedMillis;
	private final boolean _offline;
	private final Set<String> _orphanExclusions = new TreeSet<>();
	private final Set<String> _preflightTocExclusions = new TreeSet<>();
	private final Set<String> _restrictedPaths = new TreeSet<>();
	private final Set<String> _restrictedRoles = new TreeSet<>();
	private final RunReport _runReport = new RunReport();
	private SiteResource _siteResource;
	private final boolean _skipDiffCheck;
	private final Set<String> _skipLocales = new TreeSet<>();
	private final StructuredContentBuilder _structuredContentBuilder;
	private final Map<String, Long> _structuredContentFolderIds =
		new HashMap<>();
	private StructuredContentFolderResource _structuredContentFolderResource;
	private StructuredContentResource _structuredContentResource;
	private final JSONObject _taxonomyCategoriesJSONObject = new JSONObject();
	private TaxonomyCategoryResource _taxonomyCategoryResource;
	private TaxonomyVocabularyResource _taxonomyVocabularyResource;

}
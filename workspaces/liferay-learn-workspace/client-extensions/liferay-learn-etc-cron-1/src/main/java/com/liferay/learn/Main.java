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
import com.liferay.headless.delivery.client.function.UnsafeSupplier;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.headless.delivery.client.permission.Permission;
import com.liferay.headless.delivery.client.resource.v1_0.StructuredContentFolderResource;
import com.liferay.headless.delivery.client.resource.v1_0.StructuredContentResource;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterNode;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterVisitorExt;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.URL;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import org.yaml.snakeyaml.Yaml;

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
				System.getenv(
					"LIFERAY_LEARN_ETC_CRON_PREFLIGHT_TOC_EXCLUSIONS"),
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
			String preflightTocExclusions, boolean skipDiffCheck,
			String skipLocalesContent)
		throws Exception {

		_liferayOAuthClientId = liferayOAuthClientId;
		_liferayOAuthClientSecret = liferayOAuthClientSecret;
		_liferayURL = liferayURL;
		_offline = offline;
		_skipDiffCheck = skipDiffCheck;

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

		if (skipLocalesContent != null) {
			for (String skipLocale : skipLocalesContent.split(",")) {
				String trimmedSkipLocale = skipLocale.trim();

				if (!trimmedSkipLocale.isEmpty()) {
					_skipLocales.add(trimmedSkipLocale);
				}
			}
		}

		System.out.println("Locales excluded from import: " + _skipLocales);

		_baseDirName = baseDir.getCanonicalPath();

		_docsDirName = _baseDirName + "/docs";

		System.out.println("Liferay URL: " + _liferayURL);

		_addFileNames(_docsDirName);

		_initFlexmark();

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
	}

	public void loadTaxonomyVocabularies() throws Exception {
		if (_offline) {
			return;
		}

		File file = new File(_docsDirName + "/../taxonomy-vocabularies.json");

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

		List<String> preflightErrorMessages = new ArrayList<>();
		int preflightWarningCount = 0;

		Map<String, String> uuidFileNames = new HashMap<>();
		int validatedCount = 0;

		for (String fileName : _fileNames) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			validatedCount++;

			Map<String, Object> data = null;
			String text = null;

			try {
				text = FileUtils.readFileToString(
					new File(fileName), StandardCharsets.UTF_8);

				SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
					new SnakeYamlFrontMatterVisitor();

				snakeYamlFrontMatterVisitor.visit(_parser.parse(text));

				data = snakeYamlFrontMatterVisitor.getData();
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

			String uuid = _getUuid(data);

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
					else if (!_taxonomyCategoriesJSONObject.has(
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

					Matcher matcher = _markdownLinkPattern.matcher(
						(String)tocEntry);

					if (matcher.find()) {
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

		for (String fileName : _fileNames) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			if (_offline) {
				JSONObject jsonObject = new JSONObject(
					_toStructuredContent(fileName));

				_write(
					jsonObject.toString(4), "build/structured-content",
					new File(fileName));

				continue;
			}

			_renewOAuthAuthorization();

			StructuredContent desiredStructuredContent = null;
			Long publishedStructuredContentId = null;

			try {
				StructuredContent structuredContent = _toStructuredContent(
					fileName);

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
							_generateMD5Hex(new File(fileName)),
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
								fileName.substring(_docsDirName.length()))));

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
				_error(fileName + ": " + exception);

				if (publishedStructuredContentId != null) {
					_invalidateMD5Hex(
						publishedStructuredContentId, desiredStructuredContent);
				}
			}
		}

		int deletedStructuredContentCount = _deleteOrphans(
			_getIdStructuredContents(siteStructuredContents),
			importedStructuredContentIds);

		System.out.println(
			addedStructuredContentCount + " structured contents were added.");

		if (deletedStructuredContentCount > 0) {
			System.out.println(
				deletedStructuredContentCount +
					" structured contents were deleted.");
		}

		System.out.println(
			updatedStructuredContentCount +
				" structured contents were updated.");

		if (!_warningMessages.isEmpty()) {
			System.out.println(_warningMessages.size() + " warning messages:");

			for (String warningMessage : _warningMessages) {
				System.out.println(warningMessage);
			}
		}

		_notifySlack(
			addedStructuredContentCount, deletedStructuredContentCount,
			skippedStructuredContentCount, updatedStructuredContentCount);

		if (!_errorMessages.isEmpty()) {
			System.out.println(_errorMessages.size() + " error messages:");

			for (String errorMessage : _errorMessages) {
				System.out.println(errorMessage);
			}

			throw new Exception(_errorMessages.size() + " error messages");
		}

		_recordSuccess();
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

	private long _addGroupSeconds(
		Map<String, Long> groupSeconds, String name, long seconds) {

		groupSeconds.merge(
			_phaseGroups.getOrDefault(name, name), seconds, Long::sum);

		return seconds;
	}

	private int _deleteOrphans(
		Map<Long, StructuredContent> idStructuredContents,
		Set<Long> importedStructuredContentIds) {

		Set<Long> orphanedStructuredContentIds = new HashSet<>(
			idStructuredContents.keySet());

		orphanedStructuredContentIds.removeAll(importedStructuredContentIds);

		int orphanCount = orphanedStructuredContentIds.size();

		if (!_isOrphanCleanupEnabled()) {
			System.out.println(
				orphanCount +
					" orphaned structured contents were kept (cleanup " +
						"disabled).");

			return 0;
		}

		if (!_errorMessages.isEmpty()) {
			System.out.println(
				StringBundler.concat(
					"Skipping orphan cleanup because the run has ",
					_errorMessages.size(), " errors: ", orphanCount,
					" orphaned structured contents were kept."));

			return 0;
		}

		int inventoryCount = idStructuredContents.size();

		int orphanThreshold = _getOrphanThreshold(inventoryCount);

		if (orphanCount > orphanThreshold) {
			_error(
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
				_error(
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

	private void _error(String errorMessage) {
		System.out.println(errorMessage);

		_errorMessages.add(errorMessage);
	}

	private String _generateMD5Hex(File file) throws Exception {
		StringBundler sb = new StringBundler(3);

		sb.append(DigestUtils.md5Hex(new FileInputStream(_getHTMLFile(file))));

		if (!_skipLocales.contains("ja")) {
			File japaneseHTMLFile = _getHTMLFile(
				new File(
					StringUtil.replace(
						file.getCanonicalPath(), "/en/", "/ja/")));

			if (japaneseHTMLFile.exists()) {
				sb.append(
					DigestUtils.md5Hex(new FileInputStream(japaneseHTMLFile)));
			}
		}

		sb.append(
			DigestUtils.md5Hex(String.valueOf(_getNavigationJSONObject(file))));

		return DigestUtils.md5Hex(sb.toString());
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

		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		snakeYamlFrontMatterVisitor.visit(
			_parser.parse(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8)));

		Map<String, Object> data = snakeYamlFrontMatterVisitor.getData();

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
				_warn(
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

	private String _getCommitURL(String commit) {
		return StringBundler.concat(
			"https://github.com/",
			GetterUtil.getString(
				System.getenv("LIFERAY_LEARN_ETC_CRON_GITHUB_USER"), "liferay"),
			"/liferay-learn/commit/", commit);
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

	private String _getDescription(String text) {
		TextCollectingVisitor textCollectingVisitor =
			new TextCollectingVisitor();

		return StringUtil.shorten(
			textCollectingVisitor.collectAndGetText(_parser.parse(text)), 300);
	}

	private String[] _getDirNames(String fileName) {
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

		for (String fileName : _fileNames) {
			if (!fileName.contains("/en/") || !fileName.endsWith(".md")) {
				continue;
			}

			File file = new File(fileName);

			String uuid = _getUuid(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8));

			StructuredContent siteStructuredContent =
				externalReferenceCodeStructuredContents.get(uuid);

			if (siteStructuredContent == null) {
				continue;
			}

			publishedCount++;

			if (!StringUtil.equals(
					_generateMD5Hex(file), _getMD5Hex(siteStructuredContent))) {

				continue;
			}

			String html = FileUtils.readFileToString(
				_getHTMLFile(file), StandardCharsets.UTF_8);

			if (StringUtil.equals(
					_normalizeHTML(
						_unescapeUnicode(_getContent(siteStructuredContent))),
					_normalizeHTML(html))) {

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
			_error(
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

	private String _getHTML(File file) throws Exception {
		return FileUtils.readFileToString(
			_getHTMLFile(file), StandardCharsets.UTF_8);
	}

	private File _getHTMLFile(File file) throws Exception {
		String htmlFilePath = file.getCanonicalPath();

		htmlFilePath = htmlFilePath.replaceFirst(
			_docsDirName, _baseDirName + "/site");

		htmlFilePath = htmlFilePath.replaceFirst("\\.md", ".html");

		return new File(htmlFilePath);
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

	private JSONObject _getNavigationItemJSONObject(File file)
		throws Exception {

		JSONObject navigationItemJSONObject = new JSONObject();

		if (file == null) {
			return navigationItemJSONObject;
		}

		navigationItemJSONObject.put(
			"title",
			_getTitle(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8)));

		Path docsPath = Paths.get(_docsDirName);
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

	private JSONObject _getNavigationJSONObject(File file) throws Exception {
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

	private File _getParentMarkdownFile(File file) throws Exception {
		if (Objects.equals(file.getName(), "index.md")) {
			return null;
		}

		File parentFile = file.getParentFile();

		File parentMarkdownFile = new File(parentFile.getPath() + ".md");

		while (!parentMarkdownFile.exists()) {
			parentFile = parentFile.getParentFile();

			if (Objects.equals(parentFile.getPath(), _docsDirName)) {
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
			_warn(
				"Missing parent markdown for " + parentMarkdownFile.getPath());

			return null;
		}

		return parentMarkdownFile;
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

		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		File file = new File(fileName);

		snakeYamlFrontMatterVisitor.visit(
			_parser.parse(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8)));

		Map<String, Object> data = snakeYamlFrontMatterVisitor.getData();

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

		for (String dirName : _getDirNames(fileName)) {
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
		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		snakeYamlFrontMatterVisitor.visit(_parser.parse(text));

		Map<String, Object> data = snakeYamlFrontMatterVisitor.getData();

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
			_error(exception.getMessage());
		}

		for (Object taxonomyCategoryNameObject : (List)taxonomyCategoryNames) {
			if (!(taxonomyCategoryNameObject instanceof String)) {
				continue;
			}

			String taxonomyCategoryName = (String)taxonomyCategoryNameObject;

			if (!_taxonomyCategoriesJSONObject.has(taxonomyCategoryName)) {
				_warn(
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

	private String _getTitle(Node node) {
		if (node instanceof Heading) {
			Heading heading = (Heading)node;

			if ((heading.getLevel() == 1) && heading.hasChildren()) {
				TextCollectingVisitor textCollectingVisitor =
					new TextCollectingVisitor();

				return textCollectingVisitor.collectAndGetText(heading);
			}
		}

		if ((node instanceof Block) && node.hasChildren()) {
			Node childNode = node.getFirstChild();

			while (childNode != null) {
				String title = _getTitle(childNode);

				if (title != null) {
					return title;
				}

				childNode = childNode.getNext();
			}
		}

		return null;
	}

	private String _getTitle(String text) {
		return _getTitle(_parser.parse(text));
	}

	private String _getUuid(Map<String, Object> data) {
		if ((data == null) || !data.containsKey("uuid")) {
			return StringPool.BLANK;
		}

		Object uuid = data.get("uuid");

		if (!(uuid instanceof String)) {
			return StringPool.BLANK;
		}

		return uuid.toString();
	}

	private String _getUuid(String text) {
		Document document = _parser.parse(text);

		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		snakeYamlFrontMatterVisitor.visit(document);

		return _getUuid(snakeYamlFrontMatterVisitor.getData());
	}

	private void _initFlexmark() {
		MutableDataSet mutableDataSet = new MutableDataSet(
		).set(
			AdmonitionExtension.QUALIFIER_TYPE_MAP,
			HashMapBuilder.put(
				"error", "error"
			).put(
				"important", "important"
			).put(
				"note", "note"
			).put(
				"tip", "tip"
			).put(
				"warning", "warning"
			).build()
		).set(
			AdmonitionExtension.TYPE_SVG_MAP, new HashMap<String, String>()
		).set(
			AsideExtension.ALLOW_LEADING_SPACE, true
		).set(
			AsideExtension.EXTEND_TO_BLANK_LINE, false
		).set(
			AsideExtension.IGNORE_BLANK_LINE, false
		).set(
			AsideExtension.INTERRUPTS_ITEM_PARAGRAPH, true
		).set(
			AsideExtension.INTERRUPTS_PARAGRAPH, true
		).set(
			AsideExtension.WITH_LEAD_SPACES_INTERRUPTS_ITEM_PARAGRAPH, true
		).set(
			HtmlRenderer.GENERATE_HEADER_ID, true
		).set(
			Parser.EXTENSIONS,
			Arrays.asList(
				AdmonitionExtension.create(), AnchorLinkExtension.create(),
				AsideExtension.create(), AttributesExtension.create(),
				DefinitionExtension.create(), FootnoteExtension.create(),
				MediaTagsExtension.create(), StrikethroughExtension.create(),
				SuperscriptExtension.create(), TablesExtension.create(),
				TocExtension.create(), TypographicExtension.create(),
				YamlFrontMatterExtension.create())
		);

		_parser = Parser.builder(
			mutableDataSet
		).build();
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
			_error(
				StringBundler.concat(
					"Unable to invalidate md5Hex for ",
					structuredContent.getFriendlyUrlPath(), ": ", exception));
		}
	}

	private boolean _isExcludedTocFile(File tocFile) throws Exception {
		if (_preflightTocExclusions.isEmpty()) {
			return false;
		}

		String canonicalPath = tocFile.getCanonicalPath();

		if (!canonicalPath.startsWith(_docsDirName + File.separator)) {
			return false;
		}

		return _preflightTocExclusions.contains(
			canonicalPath.substring(_docsDirName.length() + 1));
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

	private boolean _isShowChildrenCards(File file) throws Exception {
		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		snakeYamlFrontMatterVisitor.visit(
			_parser.parse(
				FileUtils.readFileToString(file, StandardCharsets.UTF_8)));

		Map<String, Object> data = snakeYamlFrontMatterVisitor.getData();

		if ((data == null) || !data.containsKey("showChildrenCards") ||
			!StringUtil.equals(
				data.get(
					"showChildrenCards"
				).toString(),
				"false")) {

			return true;
		}

		return GetterUtil.getBoolean(data.get("showChildrenCards"));
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

	private String _normalizeHTML(String html) {
		Matcher matcher = _lineTrailingWhitespacePattern.matcher(html);

		String normalizedHTML = matcher.replaceAll(StringPool.BLANK);

		return normalizedHTML.stripTrailing();
	}

	private void _notifySlack(
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

		if (_errorMessages.isEmpty()) {
			sb.append(":sunflower: *liferay-learn-etc-cron-1* run completed");
		}
		else {
			sb.append(":rotating_light: *liferay-learn-etc-cron-1* run ");
			sb.append("failed with ");
			sb.append(_errorMessages.size());
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

		int reportedErrorCount = Math.min(_errorMessages.size(), 10);

		for (int i = 0; i < reportedErrorCount; i++) {
			sb.append("\n> ");
			sb.append(_errorMessages.get(i));
		}

		if (_errorMessages.size() > reportedErrorCount) {
			sb.append("\n> and ");
			sb.append(_errorMessages.size() - reportedErrorCount);
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
			structuredContentId, _toJapaneseStructuredContent(japaneseFile));
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

	private void _recordSuccess() {
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

	private void _renewOAuthAuthorization() throws Exception {
		if (_offline) {
			return;
		}

		long delta = System.currentTimeMillis() - _oauthIssuedMillis;

		if (delta > (_oauthExpirationMillis - 100000)) {
			_initResourceBuilders(_getOAuthAuthorization());
		}
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

	private ContentField _toContentField(
		String name, UnsafeSupplier<String, Exception> unsafeSupplier) {

		ContentFieldValue contentFieldValue = new ContentFieldValue();

		contentFieldValue.setData(unsafeSupplier::get);

		ContentField contentField = new ContentField();

		contentField.setContentFieldValue(() -> contentFieldValue);
		contentField.setName(() -> name);

		return contentField;
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

	private String _toFriendlyURLPath(File file) {
		String filePathString = file.getPath();

		String relativeFilePathString = filePathString.substring(
			_docsDirName.length() + 1);

		String friendlyURLPathString = StringUtil.merge(
			_getDirNames(relativeFilePathString), StringPool.FORWARD_SLASH);

		return FilenameUtils.removeExtension(friendlyURLPathString);
	}

	private StructuredContent _toJapaneseStructuredContent(File japaneseFile)
		throws Exception {

		StructuredContent structuredContent = new StructuredContent();

		String japaneseText = FileUtils.readFileToString(
			japaneseFile, StandardCharsets.UTF_8);

		structuredContent.setContentFields(
			() -> new ContentField[] {
				_toContentField("content", () -> _getHTML(japaneseFile)),
				_toContentField(
					"navigation",
					() -> String.valueOf(
						_getNavigationJSONObject(japaneseFile))),
				_toContentField(
					"showChildrenCards",
					() -> String.valueOf(_isShowChildrenCards(japaneseFile)))
			});

		structuredContent.setContentStructureId(
			() -> _liferayContentStructureId);
		structuredContent.setDescription(() -> _getDescription(japaneseText));
		structuredContent.setTitle(() -> _getTitle(japaneseText));

		return structuredContent;
	}

	private StructuredContent _toStructuredContent(String fileName)
		throws Exception {

		StructuredContent structuredContent = new StructuredContent();

		File englishFile = new File(fileName);

		String englishText = FileUtils.readFileToString(
			englishFile, StandardCharsets.UTF_8);

		structuredContent.setContentFields(
			() -> new ContentField[] {
				_toContentField("content", () -> _getHTML(englishFile)),
				_toContentField("md5Hex", () -> _generateMD5Hex(englishFile)),
				_toContentField(
					"navigation",
					() -> String.valueOf(
						_getNavigationJSONObject(englishFile))),
				_toContentField(
					"showChildrenCards",
					() -> String.valueOf(_isShowChildrenCards(englishFile)))
			});

		structuredContent.setContentStructureId(
			() -> _liferayContentStructureId);
		structuredContent.setDescription(() -> _getDescription(englishText));
		structuredContent.setExternalReferenceCode(() -> _getUuid(englishText));
		structuredContent.setFriendlyUrlPath(
			() -> _toFriendlyURLPath(englishFile));
		structuredContent.setTaxonomyCategoryIds(
			() -> _getTaxonomyCategoryIds(englishText));

		structuredContent.setTitle(() -> _getTitle(englishText));

		return structuredContent;
	}

	private String _unescapeUnicode(String text) {
		StringBuilder sb = new StringBuilder(text.length());

		int index = 0;

		while (index < text.length()) {
			char c = text.charAt(index);

			if ((c != CharPool.BACK_SLASH) || ((index + 1) >= text.length())) {
				sb.append(c);

				index++;

				continue;
			}

			char nextChar = text.charAt(index + 1);

			if (nextChar == CharPool.BACK_SLASH) {
				sb.append(CharPool.BACK_SLASH);

				index += 2;

				continue;
			}

			if ((nextChar == 'u') && ((index + 6) <= text.length()) &&
				_unicodeEscapePattern.matcher(
					text.substring(index, index + 6)
				).matches()) {

				sb.append(
					(char)Integer.parseInt(
						text.substring(index + 2, index + 6), 16));

				index += 6;

				continue;
			}

			sb.append(c);

			index++;
		}

		return sb.toString();
	}

	private void _warn(String warningMessage) {
		System.out.println(warningMessage);

		_warningMessages.add(warningMessage);
	}

	private void _write(String content, String dirName, File markdownFile)
		throws Exception {

		String markdownFileName = markdownFile.getCanonicalPath();

		markdownFileName = markdownFileName.substring(_docsDirName.length());

		File file = new File(dirName + markdownFileName);

		FileUtils.forceMkdirParent(file);

		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
	}

	private static final int _READ_ATTEMPT_COUNT = 3;

	private static final long _READ_ATTEMPT_DELAY = 3000;

	private static final Pattern _lineTrailingWhitespacePattern =
		Pattern.compile("[ \\t]+(?=\\n)");
	private static final Pattern _markdownLinkPattern = Pattern.compile(
		"\\[(.*)\\]\\((.*)\\)");
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
	private static final Pattern _unicodeEscapePattern = Pattern.compile(
		"\\\\u([0-9a-fA-F]{4})");

	private final String _baseDirName;
	private DataDefinitionResource _dataDefinitionResource;
	private final String _docsDirName;
	private final List<String> _errorMessages = new ArrayList<>();
	private final Set<String> _fileNames = new TreeSet<>();
	private final long _globalSiteId;
	private StructuredContentResource _japaneseStructuredContentResource;
	private final long _liferayContentStructureId;
	private final String _liferayOAuthClientId;
	private final String _liferayOAuthClientSecret;
	private final long _liferaySiteId;
	private final URL _liferayURL;
	private long _oauthExpirationMillis;
	private long _oauthIssuedMillis;
	private final boolean _offline;
	private Parser _parser;
	private final Set<String> _preflightTocExclusions = new TreeSet<>();
	private SiteResource _siteResource;
	private final boolean _skipDiffCheck;
	private final Set<String> _skipLocales = new TreeSet<>();
	private final Map<String, Long> _structuredContentFolderIds =
		new HashMap<>();
	private StructuredContentFolderResource _structuredContentFolderResource;
	private StructuredContentResource _structuredContentResource;
	private final JSONObject _taxonomyCategoriesJSONObject = new JSONObject();
	private TaxonomyCategoryResource _taxonomyCategoryResource;
	private TaxonomyVocabularyResource _taxonomyVocabularyResource;
	private final List<String> _warningMessages = new ArrayList<>();
	private final Yaml _yaml = new Yaml();

	private class SnakeYamlFrontMatterVisitor
		implements YamlFrontMatterVisitor {

		public Map<String, Object> getData() {
			return _data;
		}

		public void visit(Node node) {
			_yamlFrontMatterVisitor.visit(node);
		}

		@Override
		public void visit(YamlFrontMatterBlock yamlFrontMatterBlock) {
			String yamlString = String.valueOf(yamlFrontMatterBlock.getChars());

			yamlString = yamlString.replaceAll("---", "");

			_data = _yaml.load(yamlString);
		}

		@Override
		public void visit(YamlFrontMatterNode yamlFrontMatterNode) {
		}

		private Map<String, Object> _data;
		private final NodeVisitor _yamlFrontMatterVisitor = new NodeVisitor(
			YamlFrontMatterVisitorExt.VISIT_HANDLERS(this));

	}

}
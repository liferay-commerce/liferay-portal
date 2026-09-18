/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.site.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.importer.CPFileImporter;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.url.CPFriendlyURL;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutSetLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.PrefsPropsTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.TreeMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReader;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.theme.ThemeDisplayFactory;
import com.liferay.site.provider.SitemapURLProvider;

import jakarta.servlet.http.HttpServletRequest;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Alec Sloan
 */
@RunWith(Arquillian.class)
@Sync
public class CommerceSitemapURLProviderTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_company = CompanyLocalServiceUtil.getCompany(_group.getCompanyId());

		_user = UserTestUtil.addUser();

		_httpServletRequest = new MockHttpServletRequest();

		_themeDisplay = ThemeDisplayFactory.create();

		_themeDisplay.setCompany(_company);
		_themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_user));
		_themeDisplay.setPortalDomain(_company.getVirtualHostname());
		_themeDisplay.setPortalURL(_company.getPortalURL(_group.getGroupId()));
		_themeDisplay.setScopeGroupId(_group.getGroupId());
		_themeDisplay.setServerPort(PortalUtil.getPortalServerPort(false));
		_themeDisplay.setSignedIn(true);
		_themeDisplay.setSiteGroupId(_group.getGroupId());
		_themeDisplay.setUser(_user);

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_company.getCompanyId());

		CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_company.getCompanyId(), _group.getGroupId(), _user.getUserId());

		_themeDisplay.setRequest(_httpServletRequest);

		Class<?> clazz = CommerceSitemapURLProviderTest.class;

		InputStream inputStream = clazz.getResourceAsStream(
			"dependencies/layouts.json");

		String json = StringUtil.read(inputStream);

		JSONArray jsonArray = _jsonFactory.createJSONArray(json);

		_cpFileImporter.createLayouts(
			jsonArray, CommerceSitemapURLProviderTest.class.getClassLoader(),
			null, _serviceContext);
	}

	@Test
	public void testAssetCategorySitemapURLProvider() throws Exception {
		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.addVocabulary(
				_serviceContext.getUserId(), _company.getGroupId(),
				_group.getName(_themeDisplay.getLocale()), _serviceContext);

		String title = RandomTestUtil.randomString();

		AssetCategory assetCategory = _assetCategoryLocalService.addCategory(
			null, _serviceContext.getUserId(), assetVocabulary.getGroupId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID,
			Collections.singletonMap(LocaleUtil.getSiteDefault(), title), null,
			assetVocabulary.getVocabularyId(), false, new String[0],
			_serviceContext);

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.addFriendlyURLEntry(
				_company.getGroupId(),
				_portal.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId(), title, _serviceContext);

		Element element = _createURLSetElement();

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			_group.getGroupId(), false);

		long plid = _portal.getPlidFromPortletId(
			layoutSet.getGroupId(), layoutSet.isPrivateLayout(),
			CPPortletKeys.CP_CATEGORY_CONTENT_WEB);

		Layout layout = _layoutLocalService.getLayout(plid);

		_httpServletRequest.setAttribute(WebKeys.LAYOUT, layout);

		_themeDisplay.setLayoutSet(layout.getLayoutSet());

		_assetCategorySitemapURLProvider.visitLayout(
			element, layout.getUuid(), layoutSet, _themeDisplay);

		Assert.assertTrue(element.hasContent());

		List<Node> nodes = element.content();

		Node node = nodes.get(0);

		Assert.assertTrue(node.hasContent());

		String categoryFriendlyURL = StringBundler.concat(
			_portal.getGroupFriendlyURL(
				layout.getLayoutSet(), _themeDisplay, false, false),
			_cpFriendlyURL.getAssetCategoryURLSeparator(
				_themeDisplay.getCompanyId()),
			friendlyURLEntry.getUrlTitle(_themeDisplay.getLanguageId()));

		String xml = node.asXML();

		Assert.assertTrue(xml.contains(categoryFriendlyURL));
	}

	@Test
	public void testCPDefinitionSitemapURLProvider() throws Exception {
		CPDefinition cpDefinition = _addCPDefinition();

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.getMainFriendlyURLEntry(
				_portal.getClassNameId(CProduct.class),
				cpDefinition.getCProductId());

		String productFriendlyURL = StringBundler.concat(
			_portal.getGroupFriendlyURL(
				_layoutSetLocalService.getLayoutSet(_group.getGroupId(), false),
				_themeDisplay, false, false),
			_cpFriendlyURL.getProductURLSeparator(_themeDisplay.getCompanyId()),
			friendlyURLEntry.getUrlTitle(_themeDisplay.getLanguageId()));

		Element element = _visitLayout();

		String xml = element.asXML();

		Assert.assertTrue(xml, xml.contains(productFriendlyURL));
		Assert.assertTrue(xml, xml.contains("rel=\"alternate\""));
		Assert.assertTrue(xml, xml.contains("hreflang=\"x-default\""));
		Assert.assertTrue(
			xml,
			xml.contains(
				"hreflang=\"" +
					LocaleUtil.toW3cLanguageId(_themeDisplay.getLocale()) +
						"\""));
	}

	@Test
	public void testCPDefinitionSitemapURLProviderGetModifiedDate()
		throws Exception {

		CPDefinition cpDefinition1 = _addCPDefinition();

		cpDefinition1.setModifiedDate(
			new Date(System.currentTimeMillis() - Time.DAY));

		_cpDefinitionLocalService.updateCPDefinition(cpDefinition1);

		CPDefinition cpDefinition2 = _addCPDefinition();

		Assert.assertEquals(
			cpDefinition2.getModifiedDate(),
			_cpDefinitionSitemapURLProvider.getModifiedDate(
				_company.getCompanyId(), _group.getGroupId()));
	}

	@Test
	public void testCPDefinitionSitemapURLProviderReflectsTranslatedFriendlyURL()
		throws Exception {

		List<Locale> siteLocales = new ArrayList<>(
			_language.getCompanyAvailableLocales(_company.getCompanyId()));

		GroupTestUtil.updateDisplaySettings(
			_group.getGroupId(), siteLocales, siteLocales.get(0));

		CPDefinition cpDefinition = _addCPDefinition();

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.getMainFriendlyURLEntry(
				_portal.getClassNameId(CProduct.class),
				cpDefinition.getCProductId());

		String translatedUrlTitle =
			"translated-" +
				StringUtil.toLowerCase(RandomTestUtil.randomString());

		_friendlyURLEntryLocalService.updateFriendlyURLEntryLocalization(
			friendlyURLEntry, _language.getLanguageId(siteLocales.get(1)),
			translatedUrlTitle);

		Element element = _visitLayout();

		String xml = element.asXML();

		Assert.assertTrue(xml, xml.contains(translatedUrlTitle));
	}

	@Test
	public void testCPDefinitionSitemapURLProviderVisitLayoutSet()
		throws Exception {

		_addCPDefinition();

		Element layoutElement = _visitLayout();
		Element layoutSetElement = _visitLayoutSet();

		Assert.assertEquals(layoutElement.asXML(), layoutSetElement.asXML());
	}

	@Test
	public void testCPDefinitionSitemapURLProviderVisitLayoutSetExcludesDraftCPDefinitions()
		throws Exception {

		CPDefinition cpDefinition = _addCPDefinition();

		CPDefinition draftCPDefinition = _addDraftCPDefinition();

		Element element = _visitLayoutSet();

		String xml = element.asXML();

		Assert.assertTrue(
			xml,
			xml.contains(
				CommerceTestUtil.getCPDefinitionURLTitle(cpDefinition)));
		Assert.assertFalse(
			xml,
			xml.contains(
				CommerceTestUtil.getCPDefinitionURLTitle(draftCPDefinition)));
	}

	@Test
	public void testCPDefinitionSitemapURLProviderWithLocalePrependedFriendlyURLStyle()
		throws Exception {

		_addCPDefinition();

		try (SafeCloseable safeCloseable =
				PrefsPropsTestUtil.swapWithSafeCloseable(
					_company.getCompanyId(),
					PropsKeys.LOCALE_PREPEND_FRIENDLY_URL_STYLE, "2")) {

			Element element = _visitLayout();

			String xml = element.asXML();

			Assert.assertTrue(
				xml, xml.contains("/" + LocaleUtil.US.getLanguage() + "/"));
		}
	}

	@Test
	public void testCPDefinitionSitemapURLProviderWithVirtualHost()
		throws Exception {

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			_group.getGroupId(), false);

		_virtualHostLocalService.updateVirtualHosts(
			_company.getCompanyId(), layoutSet.getLayoutSetId(),
			TreeMapBuilder.put(
				StringBundler.concat(
					"www.", RandomTestUtil.randomString(), ".test"),
				StringPool.BLANK
			).build());

		try {
			_addCPDefinition();

			try (SafeCloseable safeCloseable =
					PrefsPropsTestUtil.swapWithSafeCloseable(
						_company.getCompanyId(),
						PropsKeys.LOCALE_PREPEND_FRIENDLY_URL_STYLE, "2")) {

				Element element = _visitLayout();

				String xml = element.asXML();

				Assert.assertFalse(xml, xml.matches("(?s).*://[^/]*//.*"));
			}
		}
		finally {
			_virtualHostLocalService.updateVirtualHosts(
				_company.getCompanyId(), layoutSet.getLayoutSetId(),
				new TreeMap<>());
		}
	}

	private CPDefinition _addCPDefinition() throws Exception {
		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_company.getCompanyId(), _group.getGroupId(), _user.getUserId(),
			_commerceCurrency.getCode());

		CPInstance cpInstance =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				commerceCatalog.getGroupId());

		return cpInstance.getCPDefinition();
	}

	private CPDefinition _addDraftCPDefinition() throws Exception {
		CPDefinition cpDefinition = _addCPDefinition();

		cpDefinition.setStatus(WorkflowConstants.STATUS_DRAFT);

		return _cpDefinitionLocalService.updateCPDefinition(cpDefinition);
	}

	private Element _createURLSetElement() {
		Document document = _saxReader.createDocument();

		document.setXMLEncoding("UTF-8");

		Element element = document.addElement(
			"urlset", "http://www.sitemaps.org/schemas/sitemap/0.9");

		element.addAttribute(
			"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		element.addAttribute(
			"xsi:schemaLocation",
			"http://www.w3.org/1999/xhtml " +
				"http://www.w3.org/2002/08/xhtml/xhtml1-strict.xsd");
		element.addAttribute("xmlns:xhtml", "http://www.w3.org/1999/xhtml");

		return element;
	}

	private Element _visitLayout() throws Exception {
		Element element = _createURLSetElement();

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			_group.getGroupId(), false);

		long plid = _portal.getPlidFromPortletId(
			layoutSet.getGroupId(), layoutSet.isPrivateLayout(),
			CPPortletKeys.CP_CONTENT_WEB);

		Layout layout = _layoutLocalService.getLayout(plid);

		_httpServletRequest.setAttribute(WebKeys.LAYOUT, layout);

		_themeDisplay.setLayoutSet(layout.getLayoutSet());

		_cpDefinitionSitemapURLProvider.visitLayout(
			element, layout.getUuid(), layoutSet, _themeDisplay);

		return element;
	}

	private Element _visitLayoutSet() throws Exception {
		Element element = _createURLSetElement();

		LayoutSet layoutSet = _layoutSetLocalService.getLayoutSet(
			_group.getGroupId(), false);

		_themeDisplay.setLayoutSet(layoutSet);

		_cpDefinitionSitemapURLProvider.visitLayoutSet(
			element, layoutSet, _themeDisplay);

		return element;
	}

	@Inject
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Inject(
		filter = "component.name=com.liferay.commerce.product.internal.site.provider.AssetCategorySitemapURLProvider",
		type = SitemapURLProvider.class
	)
	private SitemapURLProvider _assetCategorySitemapURLProvider;

	@Inject
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	private CommerceCurrency _commerceCurrency;
	private Company _company;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject(
		filter = "component.name=com.liferay.commerce.product.internal.site.provider.CPDefinitionSitemapURLProvider",
		type = SitemapURLProvider.class
	)
	private SitemapURLProvider _cpDefinitionSitemapURLProvider;

	@Inject
	private CPFileImporter _cpFileImporter;

	@Inject
	private CPFriendlyURL _cpFriendlyURL;

	@Inject
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private HttpServletRequest _httpServletRequest;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject
	private Language _language;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private LayoutSetLocalService _layoutSetLocalService;

	@Inject
	private Portal _portal;

	@Inject
	private SAXReader _saxReader;

	private ServiceContext _serviceContext;
	private ThemeDisplay _themeDisplay;
	private User _user;

	@Inject
	private VirtualHostLocalService _virtualHostLocalService;

}
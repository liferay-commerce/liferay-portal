/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.product.exception.CPDefinitionVisibilityRelClassNameIdException;
import com.liferay.commerce.product.exception.CPDefinitionVisibilityRelClassPKException;
import com.liferay.commerce.product.exception.DuplicateCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class CPDefinitionVisibilityRelLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			group.getGroupId());

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			TestPropsValues.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com",
			RandomTestUtil.randomString(),
			new long[] {TestPropsValues.getUserId()}, null, _serviceContext);

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), _serviceContext);

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
	}

	@Test
	public void testAddCPDefinitionVisibilityRel() throws Exception {
		_assertSearchCommerceAccountIds(_cpDefinition);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
			_accountEntry.getAccountEntryId());

		_assertSearchCommerceAccountIds(_cpDefinition, _accountEntry);

		try {
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

			Assert.fail();
		}
		catch (DuplicateCPDefinitionVisibilityRelException
					duplicateCPDefinitionVisibilityRelException) {

			Assert.assertNotNull(duplicateCPDefinitionVisibilityRelException);
		}

		try {
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), User.class.getName(),
				TestPropsValues.getUserId());

			Assert.fail();
		}
		catch (CPDefinitionVisibilityRelClassNameIdException
					cpDefinitionVisibilityRelClassNameIdException) {

			Assert.assertNotNull(cpDefinitionVisibilityRelClassNameIdException);
		}

		try {
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				RandomTestUtil.randomLong());

			Assert.fail();
		}
		catch (CPDefinitionVisibilityRelClassPKException
					cpDefinitionVisibilityRelClassPKException) {

			Assert.assertNotNull(cpDefinitionVisibilityRelClassPKException);
		}

		Company company = CompanyTestUtil.addCompany();

		User user = UserTestUtil.getAdminUser(company.getCompanyId());

		AccountEntry accountEntry =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				user.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				ServiceContextTestUtil.getServiceContext(
					company.getCompanyId(), company.getGroupId(),
					user.getUserId()));

		try {
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				accountEntry.getAccountEntryId());

			Assert.fail();
		}
		catch (CPDefinitionVisibilityRelClassPKException
					cpDefinitionVisibilityRelClassPKException) {

			Assert.assertNotNull(cpDefinitionVisibilityRelClassPKException);
		}
	}

	@Test
	public void testDeleteCPDefinitionVisibilityRel() throws Exception {
		AccountEntry accountEntry =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null,
				_serviceContext);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

		_cpDefinitionVisibilityRelLocalService.deleteCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());

		Assert.assertNotNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(),
					accountEntry.getAccountEntryId()));
		Assert.assertNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(),
					_accountEntry.getAccountEntryId()));

		_assertSearchCommerceAccountIds(_cpDefinition, accountEntry);
	}

	@Test
	public void testDeleteCPDefinitionVisibilityRels() throws Exception {
		AccountEntry accountEntry =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null,
				_serviceContext);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		for (CPDefinition curCPDefinition :
				Arrays.asList(_cpDefinition, cpDefinition)) {

			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				curCPDefinition.getCPDefinitionId(),
				AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());
		}

		_cpDefinitionVisibilityRelLocalService.deleteCPDefinitionVisibilityRels(
			AccountEntry.class.getName(), _accountEntry.getAccountEntryId());

		_assertSearchCommerceAccountIds(cpDefinition);
		_assertSearchCommerceAccountIds(_cpDefinition, accountEntry);
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private void _assertSearchCommerceAccountIds(
		CPDefinition cpDefinition, AccountEntry... accountEntries) {

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).companyId(
				cpDefinition.getCompanyId()
			).emptySearchEnabled(
				true
			).fields(
				"commerceAccountIds"
			).modelIndexerClasses(
				CPDefinition.class
			).query(
				QueriesUtil.term(
					Field.ENTRY_CLASS_PK, cpDefinition.getCPDefinitionId())
			).build());

		List<Document> documents = searchResponse.getDocuments();

		Assert.assertEquals(documents.toString(), 1, documents.size());

		Document document = documents.get(0);

		Assert.assertEquals(
			TransformUtil.transformToList(
				accountEntries, AccountEntry::getAccountEntryId),
			document.getLongs("commerceAccountIds"));
	}

	private AccountEntry _accountEntry;
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private Searcher _searcher;

	private ServiceContext _serviceContext;

}
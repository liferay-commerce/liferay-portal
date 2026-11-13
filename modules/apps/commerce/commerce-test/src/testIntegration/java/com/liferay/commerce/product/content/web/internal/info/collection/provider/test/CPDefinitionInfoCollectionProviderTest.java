/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.web.internal.info.collection.provider.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.constants.CommerceChannelAccountEntryRelConstants;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceChannelAccountEntryRelLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.commerce.test.util.context.TestCommerceContext;
import com.liferay.commerce.util.CommerceContextThreadLocal;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.filter.InfoFilter;
import com.liferay.info.filter.KeywordsInfoFilter;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.pagination.Pagination;
import com.liferay.info.sort.Sort;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;
import java.util.List;

import org.junit.After;
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
public class CPDefinitionInfoCollectionProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());

		_commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(),
			TestPropsValues.getUserId(), _commerceCurrency.getCode());

		_accountEntry1 = CommerceAccountTestUtil.addBusinessAccountEntry(
			TestPropsValues.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com", _serviceContext);
		_accountEntry2 = CommerceAccountTestUtil.addBusinessAccountEntry(
			TestPropsValues.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com", _serviceContext);

		_cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		AccountGroup accountGroup =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				_group.getCompanyId(), RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				_accountEntry1.getAccountEntryId(), _serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
			_cpDefinition1.getCPDefinitionId());

		_cpDefinition1.setAccountGroupFilterEnabled(true);

		_cpDefinition1 = _cpDefinitionLocalService.updateCPDefinition(
			_cpDefinition1);

		_cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testGetCollectionInfoPage() throws Exception {
		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertTrue(pageItems.contains(_cpDefinition1));
		Assert.assertTrue(pageItems.contains(_cpDefinition2));

		infoPage = _getInfoPage(_accountEntry2);

		pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(_cpDefinition1));
		Assert.assertTrue(pageItems.contains(_cpDefinition2));

		infoPage = _getInfoPage(null);

		pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(_cpDefinition1));
		Assert.assertTrue(pageItems.contains(_cpDefinition2));
	}

	@Test
	public void testGetCollectionInfoPageWithChannelFilterEnabled()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition1.setChannelFilterEnabled(true);

		cpDefinition1 = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition1);

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			_commerceCurrency.getCode());

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition1.getCPDefinitionId(),
			commerceChannel.getCommerceChannelId(), _serviceContext);

		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition2.setChannelFilterEnabled(true);

		cpDefinition2 = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition2);

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition2.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(), _serviceContext);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(cpDefinition1));
		Assert.assertTrue(pageItems.contains(cpDefinition2));
	}

	@Test
	public void testGetCollectionInfoPageWithCommerceCatalogPermission()
		throws Exception {

		Role role = _roleLocalService.addRole(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(), null, 0,
			RandomTestUtil.randomString(), null, null,
			RoleConstants.TYPE_REGULAR, null, _serviceContext);

		_resourcePermissionLocalService.setResourcePermissions(
			_group.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			role.getRoleId(), new String[] {ActionKeys.VIEW});

		User user = UserTestUtil.addUser();

		_roleLocalService.addUserRole(user.getUserId(), role.getRoleId());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(),
			TestPropsValues.getUserId(), _commerceCurrency.getCode());

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			InfoPage<CPDefinition> infoPage = _getInfoPage(null);

			List<? extends CPDefinition> pageItems = infoPage.getPageItems();

			Assert.assertTrue(pageItems.contains(cpDefinition));
			Assert.assertTrue(pageItems.contains(_cpDefinition2));
		}
	}

	@Test
	public void testGetCollectionInfoPageWithDraftCPDefinition()
		throws Exception {

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition.setStatus(WorkflowConstants.STATUS_DRAFT);

		cpDefinition = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(cpDefinition));
	}

	@Test
	public void testGetCollectionInfoPageWithEligibleAccountEntry()
		throws Exception {

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition.setChannelFilterEnabled(true);

		cpDefinition = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition);

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(), _serviceContext);

		_commerceChannelAccountEntryRelLocalService.
			addCommerceChannelAccountEntryRel(
				TestPropsValues.getUserId(), _accountEntry1.getAccountEntryId(),
				AccountEntry.class.getName(),
				_accountEntry1.getAccountEntryId(),
				_commerceChannel.getCommerceChannelId(), true, 0,
				CommerceChannelAccountEntryRelConstants.TYPE_ELIGIBILITY);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertTrue(pageItems.contains(cpDefinition));

		infoPage = _getInfoPage(_accountEntry2);

		pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(cpDefinition));
	}

	@Test
	public void testGetCollectionInfoPageWithGuestAccountEntry()
		throws Exception {

		AccountGroup accountGroup =
			_accountGroupLocalService.checkGuestAccountGroup(
				_group.getCompanyId());

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
			cpDefinition.getCPDefinitionId());

		cpDefinition.setAccountGroupFilterEnabled(true);

		cpDefinition = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(cpDefinition));

		AccountEntry accountEntry =
			_accountEntryLocalService.getGuestAccountEntry(
				_group.getCompanyId());

		infoPage = _getInfoPage(accountEntry);

		pageItems = infoPage.getPageItems();

		Assert.assertTrue(pageItems.contains(cpDefinition));
		Assert.assertFalse(pageItems.contains(_cpDefinition1));
		Assert.assertTrue(pageItems.contains(_cpDefinition2));
	}

	@Test
	public void testGetCollectionInfoPageWithKeywords() throws Exception {
		CollectionQuery collectionQuery = new CollectionQuery();

		KeywordsInfoFilter keywordsInfoFilter = new KeywordsInfoFilter();

		keywordsInfoFilter.setKeywords(
			_cpDefinition2.getName(
				LocaleUtil.toLanguageId(LocaleUtil.getDefault())));

		collectionQuery.setInfoFilters(
			HashMapBuilder.<String, InfoFilter>put(
				KeywordsInfoFilter.class.getName(), keywordsInfoFilter
			).build());

		InfoPage<CPDefinition> infoPage = _getInfoPage(
			_accountEntry1, collectionQuery);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(_cpDefinition1));
		Assert.assertTrue(pageItems.contains(_cpDefinition2));
	}

	@Test
	public void testGetCollectionInfoPageWithPagination() throws Exception {
		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> cpDefinitions = infoPage.getPageItems();

		CollectionQuery collectionQuery = new CollectionQuery();

		collectionQuery.setPagination(Pagination.of(1, 0));

		infoPage = _getInfoPage(_accountEntry1, collectionQuery);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertEquals(pageItems.toString(), 1, pageItems.size());
		Assert.assertEquals(cpDefinitions.get(0), pageItems.get(0));

		Assert.assertEquals(cpDefinitions.size(), infoPage.getTotalCount());
	}

	@Test
	public void testGetCollectionInfoPageWithSort() throws Exception {
		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition1.setCreateDate(
			new Date(System.currentTimeMillis() - Time.DAY));

		cpDefinition1 = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition1);

		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertTrue(
			pageItems.indexOf(cpDefinition1) < pageItems.indexOf(
				cpDefinition2));

		CollectionQuery collectionQuery = new CollectionQuery();

		collectionQuery.setPagination(
			Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS));
		collectionQuery.setSort(new Sort(Field.CREATE_DATE, false));

		infoPage = _getInfoPage(_accountEntry1, collectionQuery);

		pageItems = infoPage.getPageItems();

		Assert.assertTrue(
			pageItems.indexOf(cpDefinition1) < pageItems.indexOf(
				cpDefinition2));

		collectionQuery.setSort(new Sort(Field.CREATE_DATE, true));

		infoPage = _getInfoPage(_accountEntry1, collectionQuery);

		pageItems = infoPage.getPageItems();

		Assert.assertTrue(
			pageItems.indexOf(cpDefinition2) < pageItems.indexOf(
				cpDefinition1));
	}

	@Test
	public void testGetCollectionInfoPageWithUnpublishedCPDefinition()
		throws Exception {

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		cpDefinition.setPublished(false);

		cpDefinition = _cpDefinitionLocalService.updateCPDefinition(
			cpDefinition);

		InfoPage<CPDefinition> infoPage = _getInfoPage(_accountEntry1);

		List<? extends CPDefinition> pageItems = infoPage.getPageItems();

		Assert.assertFalse(pageItems.contains(cpDefinition));
	}

	private InfoPage<CPDefinition> _getInfoPage(AccountEntry accountEntry)
		throws Exception {

		CollectionQuery collectionQuery = new CollectionQuery();

		collectionQuery.setPagination(
			Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS));

		return _getInfoPage(accountEntry, collectionQuery);
	}

	private InfoPage<CPDefinition> _getInfoPage(
			AccountEntry accountEntry, CollectionQuery collectionQuery)
		throws Exception {

		try (SafeCloseable safeCloseable =
				CommerceContextThreadLocal.setCommerceContextWithSafeCloseable(
					new TestCommerceContext(
						accountEntry, _commerceCurrency, _commerceChannel,
						TestPropsValues.getUser(), _group, null))) {

			return _infoCollectionProvider.getCollectionInfoPage(
				collectionQuery);
		}
	}

	private AccountEntry _accountEntry1;
	private AccountEntry _accountEntry2;

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	private CommerceCatalog _commerceCatalog;
	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelAccountEntryRelLocalService
		_commerceChannelAccountEntryRelLocalService;

	@Inject
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	private CommerceCurrency _commerceCurrency;
	private CPDefinition _cpDefinition1;
	private CPDefinition _cpDefinition2;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private Group _group;

	@Inject(
		filter = "component.name=com.liferay.commerce.product.content.web.internal.info.collection.provider.CPDefinitionInfoCollectionProvider"
	)
	private InfoCollectionProvider<CPDefinition> _infoCollectionProvider;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	private ServiceContext _serviceContext;

}
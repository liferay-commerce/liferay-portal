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
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
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
import com.liferay.info.collection.provider.RelatedInfoItemCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.pagination.Pagination;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
public class CPDefinitionOptionValueRelRelatedInfoItemCollectionProviderTest {

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
		_cpDefinition3 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);
	}

	@After
	public void tearDown() {
		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testGetCollectionInfoPage() throws Exception {
		List<Long> cProductIds = _getCProductIds(_accountEntry1);

		Assert.assertTrue(cProductIds.contains(_cpDefinition1.getCProductId()));
		Assert.assertTrue(cProductIds.contains(_cpDefinition2.getCProductId()));

		cProductIds = _getCProductIds(_accountEntry2);

		Assert.assertFalse(
			cProductIds.contains(_cpDefinition1.getCProductId()));
		Assert.assertTrue(cProductIds.contains(_cpDefinition2.getCProductId()));

		cProductIds = _getCProductIds(null);

		Assert.assertFalse(
			cProductIds.contains(_cpDefinition1.getCProductId()));
		Assert.assertTrue(cProductIds.contains(_cpDefinition2.getCProductId()));
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

		List<Long> cProductIds = _getCProductIds(_accountEntry1);

		Assert.assertTrue(cProductIds.contains(cpDefinition.getCProductId()));

		cProductIds = _getCProductIds(_accountEntry2);

		Assert.assertFalse(cProductIds.contains(cpDefinition.getCProductId()));
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

		List<Long> cProductIds = _getCProductIds(_accountEntry1);

		Assert.assertFalse(cProductIds.contains(cpDefinition.getCProductId()));

		AccountEntry accountEntry =
			_accountEntryLocalService.getGuestAccountEntry(
				_group.getCompanyId());

		cProductIds = _getCProductIds(accountEntry);

		Assert.assertTrue(cProductIds.contains(cpDefinition.getCProductId()));
		Assert.assertFalse(
			cProductIds.contains(_cpDefinition1.getCProductId()));
		Assert.assertTrue(cProductIds.contains(_cpDefinition2.getCProductId()));
	}

	private List<Long> _getCProductIds(AccountEntry accountEntry)
		throws Exception {

		CollectionQuery collectionQuery = new CollectionQuery();

		collectionQuery.setPagination(
			Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS));
		collectionQuery.setRelatedItemObject(_cpDefinition3);

		try (SafeCloseable safeCloseable =
				CommerceContextThreadLocal.setCommerceContextWithSafeCloseable(
					new TestCommerceContext(
						accountEntry, _commerceCurrency, _commerceChannel,
						TestPropsValues.getUser(), _group, null))) {

			InfoPage<CPDefinitionOptionValueRel> infoPage =
				_relatedInfoItemCollectionProvider.getCollectionInfoPage(
					collectionQuery);

			return TransformUtil.transform(
				infoPage.getPageItems(),
				CPDefinitionOptionValueRel::getCProductId);
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
	private CPDefinition _cpDefinition3;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private Group _group;

	@Inject(
		filter = "component.name=com.liferay.commerce.product.content.web.internal.info.collection.provider.CPDefinitionOptionValueRelRelatedInfoItemCollectionProvider"
	)
	private RelatedInfoItemCollectionProvider
		<CPDefinition, CPDefinitionOptionValueRel>
			_relatedInfoItemCollectionProvider;

	private ServiceContext _serviceContext;

}
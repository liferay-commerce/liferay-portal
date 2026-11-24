/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalService;
import com.liferay.commerce.product.service.CPOptionLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.ProductOption;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Page;
import com.liferay.headless.commerce.delivery.catalog.client.problem.Problem;
import com.liferay.headless.commerce.delivery.catalog.client.resource.v1_0.ProductOptionResource;
import com.liferay.portal.configuration.test.util.GroupConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class ProductOptionResourceTest
	extends BaseProductOptionResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), RandomTestUtil.randomString());
		_cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);
	}

	@Override
	@Test
	public void testGetChannelProductProductOptionsPage() throws Exception {
		super.testGetChannelProductProductOptionsPage();

		_testGetChannelProductProductOptionsPageWithAccountGroupFilter();
		_testGetChannelProductProductOptionsPageWithChannelFilter();
	}

	@Override
	protected ProductOption randomProductOption() throws Exception {
		return new ProductOption() {
			{
				description = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				fieldType = "text";
				id = RandomTestUtil.randomLong();
				key = StringUtil.toLowerCase(RandomTestUtil.randomString());
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				priority = RandomTestUtil.randomDouble();

				setCatalogId(
					() -> {
						CommerceCatalog commerceCatalog =
							_cpDefinition.getCommerceCatalog();

						return commerceCatalog.getCommerceCatalogId();
					});
			}
		};
	}

	@Override
	protected ProductOption
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionsPage_addProductOption(
				String channelExternalReferenceCode,
				String productExternalReferenceCode,
				ProductOption productOption)
		throws Exception {

		return _addCPDefinitionOptionRel();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionsPage_getChannelExternalReferenceCode()
		throws Exception {

		return _commerceChannel.getExternalReferenceCode();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionsPage_getProductExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected ProductOption
			testGetChannelProductProductOptionsPage_addProductOption(
				Long channelId, Long productId, ProductOption productOption)
		throws Exception {

		return _addCPDefinitionOptionRel();
	}

	@Override
	protected Long testGetChannelProductProductOptionsPage_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGetChannelProductProductOptionsPage_getProductId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	private CPDefinition _addCPDefinition() throws Exception {
		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);

		CPOption cpOption = _cpOptionLocalService.addCPOption(
			RandomTestUtil.randomString(), _user.getUserId(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), "text", true, false, false,
			RandomTestUtil.randomString(), _serviceContext);

		_cpDefinitionOptionRelLocalService.addCPDefinitionOptionRel(
			cpDefinition.getCPDefinitionId(), cpOption.getCPOptionId(), false,
			_serviceContext);

		return cpDefinition;
	}

	private ProductOption _addCPDefinitionOptionRel() throws Exception {
		CPOption cpOption = _cpOptionLocalService.addCPOption(
			RandomTestUtil.randomString(), _user.getUserId(),
			RandomTestUtil.randomLocaleStringMap(),
			RandomTestUtil.randomLocaleStringMap(), "text", true, false, false,
			RandomTestUtil.randomString(), _serviceContext);

		_cpOptions.add(cpOption);

		CPDefinitionOptionRel cpDefinitionOptionRel =
			_cpDefinitionOptionRelLocalService.addCPDefinitionOptionRel(
				_cpDefinition.getCPDefinitionId(), cpOption.getCPOptionId(),
				false, _serviceContext);

		_cpDefinitionOptionRels.add(cpDefinitionOptionRel);

		return new ProductOption() {
			{
				description = cpDefinitionOptionRel.getDescription();
				fieldType = cpOption.getCommerceOptionTypeKey();
				id = cpDefinitionOptionRel.getCPDefinitionOptionRelId();
				key = cpDefinitionOptionRel.getKey();
				name = cpDefinitionOptionRel.getName();
				optionId = cpDefinitionOptionRel.getCPOptionId();
				priority = cpDefinitionOptionRel.getPriority();

				setCatalogId(
					() -> {
						CommerceCatalog commerceCatalog =
							_cpDefinition.getCommerceCatalog();

						return commerceCatalog.getCommerceCatalogId();
					});
			}
		};
	}

	private void _assertNotFound(ThrowingRunnable throwingRunnable) {
		Problem.ProblemException problemException = Assert.assertThrows(
			Problem.ProblemException.class, throwingRunnable);

		Problem problem = problemException.getProblem();

		Assert.assertEquals("NOT_FOUND", problem.getStatus());
	}

	private Page<ProductOption> _getChannelProductProductOptionsPage(
			CPDefinition cpDefinition, String password, User user,
			String... parameters)
		throws Exception {

		ProductOptionResource productOptionResource =
			ProductOptionResource.builder(
			).authentication(
				user.getEmailAddress(), password
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).parameters(
				parameters
			).build();

		return productOptionResource.getChannelProductProductOptionsPage(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), null);
	}

	private void _testGetChannelProductProductOptionsPageWithAccountGroupFilter()
		throws Exception {

		try (GroupConfigurationTemporarySwapper
				groupConfigurationTemporarySwapper =
					new GroupConfigurationTemporarySwapper(
						_commerceChannel.getGroupId(),
						CommerceConstants.SERVICE_NAME_COMMERCE_ACCOUNT,
						HashMapDictionaryBuilder.<String, Object>put(
							"commerceSiteType",
							CommerceChannelConstants.SITE_TYPE_B2B
						).build())) {

			CPDefinition cpDefinition = _addCPDefinition();

			_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
				cpDefinition.getCPDefinitionId(), true);

			String password = RandomTestUtil.randomString();

			User user1 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry1 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user1.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			AccountGroup accountGroup1 =
				CommerceAccountTestUtil.addAccountGroupAndAccountRel(
					RandomTestUtil.randomString(),
					AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
					accountEntry1.getAccountEntryId(), _serviceContext);

			_accountGroupRelLocalService.addAccountGroupRel(
				accountGroup1.getAccountGroupId(), CPDefinition.class.getName(),
				cpDefinition.getCPDefinitionId());

			Page<ProductOption> productOptionsPage =
				_getChannelProductProductOptionsPage(
					cpDefinition, password, user1);

			Assert.assertEquals(1, productOptionsPage.getTotalCount());

			User user2 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry2 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user2.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry2.getAccountEntryId(), _serviceContext);

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user2));

			User user3 = UserTestUtil.addUser(testCompany, password);

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user3));

			User user4 = UserTestUtil.addUser(testCompany, password);

			CommerceAccountTestUtil.addAccountEntryUserRels(
				accountEntry1.getAccountEntryId(),
				new long[] {user4.getUserId()}, _serviceContext);
			CommerceAccountTestUtil.addAccountEntryUserRels(
				accountEntry2.getAccountEntryId(),
				new long[] {user4.getUserId()}, _serviceContext);

			productOptionsPage = _getChannelProductProductOptionsPage(
				cpDefinition, password, user4);

			Assert.assertEquals(1, productOptionsPage.getTotalCount());

			productOptionsPage = _getChannelProductProductOptionsPage(
				cpDefinition, password, user4, "accountId",
				String.valueOf(AccountConstants.ACCOUNT_ENTRY_ID_GUEST));

			Assert.assertEquals(1, productOptionsPage.getTotalCount());

			productOptionsPage = _getChannelProductProductOptionsPage(
				cpDefinition, password, user4, "accountId",
				String.valueOf(accountEntry1.getAccountEntryId()));

			Assert.assertEquals(1, productOptionsPage.getTotalCount());

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user4, "accountId",
					String.valueOf(accountEntry2.getAccountEntryId())));

			User user5 = UserTestUtil.addUser(testCompany, password);

			CommerceAccountTestUtil.addBusinessAccountEntry(
				user5.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				_serviceContext);
			CommerceAccountTestUtil.addAccountEntryUserRels(
				accountEntry2.getAccountEntryId(),
				new long[] {user5.getUserId()}, _serviceContext);

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user5));
			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user5, "accountId",
					String.valueOf(accountEntry1.getAccountEntryId())));

			_accountEntryLocalService.updateStatus(
				accountEntry1.getAccountEntryId(),
				WorkflowConstants.STATUS_INACTIVE);

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user1));
			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user4));
			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user4, "accountId",
					String.valueOf(accountEntry1.getAccountEntryId())));

			_accountEntryLocalService.updateStatus(
				accountEntry2.getAccountEntryId(),
				WorkflowConstants.STATUS_INACTIVE);

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user4));

			AccountGroup accountGroup2 =
				_accountGroupLocalService.checkGuestAccountGroup(
					testCompany.getCompanyId());

			_accountGroupRelLocalService.addAccountGroupRel(
				accountGroup2.getAccountGroupId(), CPDefinition.class.getName(),
				cpDefinition.getCPDefinitionId());

			productOptionsPage = _getChannelProductProductOptionsPage(
				cpDefinition, password, user4);

			Assert.assertEquals(1, productOptionsPage.getTotalCount());

			_assertNotFound(
				() -> _getChannelProductProductOptionsPage(
					cpDefinition, password, user5));
		}
	}

	private void _testGetChannelProductProductOptionsPageWithChannelFilter()
		throws Exception {

		CPDefinition cpDefinition = _addCPDefinition();

		_cpDefinitionLocalService.updateCPDefinitionChannelFilter(
			cpDefinition.getCPDefinitionId(), true);

		CommerceChannel commerceChannel1 = CommerceTestUtil.addCommerceChannel(
			RandomTestUtil.randomString());

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			commerceChannel1.getCommerceChannelId(), _serviceContext);

		_assertNotFound(
			() -> productOptionResource.getChannelProductProductOptionsPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null));

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(), _serviceContext);

		Page<ProductOption> productOptionsPage =
			productOptionResource.getChannelProductProductOptionsPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null);

		Assert.assertEquals(1, productOptionsPage.getTotalCount());
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionOptionRelLocalService
		_cpDefinitionOptionRelLocalService;

	@DeleteAfterTestRun
	private final List<CPDefinitionOptionRel> _cpDefinitionOptionRels =
		new ArrayList<>();

	@Inject
	private CPOptionLocalService _cpOptionLocalService;

	@DeleteAfterTestRun
	private final List<CPOption> _cpOptions = new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
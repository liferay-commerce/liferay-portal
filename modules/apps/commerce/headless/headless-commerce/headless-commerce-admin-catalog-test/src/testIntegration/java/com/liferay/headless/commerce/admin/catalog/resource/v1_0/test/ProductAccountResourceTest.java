/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductAccount;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@FeatureFlag("LPD-112683")
@RunWith(Arquillian.class)
public class ProductAccountResourceTest
	extends BaseProductAccountResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			TestPropsValues.getUserId());

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), "USD", "en_US", false,
			_serviceContext);

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
	}

	@Override
	@Test
	public void testGetProductIdProductAccountsPage() throws Exception {
		super.testGetProductIdProductAccountsPage();

		_testGetProductIdProductAccountsPageWithNonexistentProduct();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"accountExternalReferenceCode", "accountId",
			"externalReferenceCode", "name"
		};
	}

	@Override
	protected ProductAccount randomProductAccount() throws Exception {
		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED, _serviceContext);

		return new ProductAccount() {
			{
				accountExternalReferenceCode =
					accountEntry.getExternalReferenceCode();
				accountId = accountEntry.getAccountEntryId();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				name = accountEntry.getName();
			}
		};
	}

	@Override
	protected ProductAccount testDeleteProductAccount_addProductAccount()
		throws Exception {

		return _addProductAccount(randomProductAccount());
	}

	@Override
	protected ProductAccount testDeleteProductAccountBatch_addProductAccount()
		throws Exception {

		return _addProductAccount(randomProductAccount());
	}

	@Override
	protected ProductAccount testGetProductAccount_addProductAccount()
		throws Exception {

		return _addProductAccount(randomProductAccount());
	}

	@Override
	protected ProductAccount
			testGetProductByExternalReferenceCodeProductAccountsPage_addProductAccount(
				String externalReferenceCode, ProductAccount productAccount)
		throws Exception {

		return _addProductAccount(productAccount);
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodeProductAccountsPage_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected ProductAccount
			testGetProductIdProductAccountsPage_addProductAccount(
				Long id, ProductAccount productAccount)
		throws Exception {

		return _addProductAccount(productAccount);
	}

	@Override
	protected Long testGetProductIdProductAccountsPage_getId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected ProductAccount testGraphQLDeleteProductAccount_addProductAccount()
		throws Exception {

		return _addProductAccount(randomProductAccount());
	}

	@Override
	protected ProductAccount testGraphQLGetProductAccount_addProductAccount()
		throws Exception {

		return _addProductAccount(randomProductAccount());
	}

	private ProductAccount _addProductAccount(ProductAccount productAccount)
		throws Exception {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				productAccount.getExternalReferenceCode(),
				TestPropsValues.getUserId(), _cpDefinition.getCPDefinitionId(),
				AccountEntry.class.getName(), productAccount.getAccountId());

		return productAccountResource.getProductAccount(
			cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());
	}

	private void _testGetProductIdProductAccountsPageWithNonexistentProduct()
		throws Exception {

		Page<ProductAccount> page =
			productAccountResource.getProductIdProductAccountsPage(
				RandomTestUtil.randomLong(), Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

	private ServiceContext _serviceContext;

}
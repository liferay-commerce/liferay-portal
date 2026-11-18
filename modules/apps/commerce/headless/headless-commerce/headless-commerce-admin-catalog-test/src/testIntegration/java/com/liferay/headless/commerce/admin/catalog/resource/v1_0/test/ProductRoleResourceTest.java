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
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductRole;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
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
public class ProductRoleResourceTest extends BaseProductRoleResourceTestCase {

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
	public void testDeleteProductRole() throws Exception {
		super.testDeleteProductRole();

		_testDeleteProductRoleWithProductAccount();
	}

	@Override
	@Test
	public void testGetProductIdProductRolesPage() throws Exception {
		super.testGetProductIdProductRolesPage();

		_testGetProductIdProductRolesPageWithNonexistentProduct();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"externalReferenceCode", "name", "roleExternalReferenceCode",
			"roleId", "roleType"
		};
	}

	@Override
	protected ProductRole randomProductRole() throws Exception {
		Role role = _roleLocalService.addRole(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(), null, 0,
			RandomTestUtil.randomString(), null, null,
			RoleConstants.TYPE_REGULAR, null, _serviceContext);

		return new ProductRole() {
			{
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				name = role.getName();
				roleExternalReferenceCode = role.getExternalReferenceCode();
				roleId = role.getRoleId();
				roleType = ProductRole.RoleType.REGULAR;
			}
		};
	}

	@Override
	protected ProductRole testDeleteProductRole_addProductRole()
		throws Exception {

		return _addProductRole(randomProductRole());
	}

	@Override
	protected ProductRole
			testGetProductByExternalReferenceCodeProductRolesPage_addProductRole(
				String externalReferenceCode, ProductRole productRole)
		throws Exception {

		return _addProductRole(productRole);
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodeProductRolesPage_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected ProductRole testGetProductIdProductRolesPage_addProductRole(
			Long id, ProductRole productRole)
		throws Exception {

		return _addProductRole(productRole);
	}

	@Override
	protected Long testGetProductIdProductRolesPage_getId() throws Exception {
		return _cpDefinition.getCProductId();
	}

	@Override
	protected ProductRole testGetProductRole_addProductRole() throws Exception {
		return _addProductRole(randomProductRole());
	}

	@Override
	protected ProductRole testGraphQLDeleteProductRole_addProductRole()
		throws Exception {

		return _addProductRole(randomProductRole());
	}

	@Override
	protected ProductRole testGraphQLGetProductRole_addProductRole()
		throws Exception {

		return _addProductRole(randomProductRole());
	}

	private CPDefinitionVisibilityRel _addCPDefinitionVisibilityRel()
		throws Exception {

		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED, _serviceContext);

		return _cpDefinitionVisibilityRelLocalService.
			addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				accountEntry.getAccountEntryId());
	}

	private ProductRole _addProductRole(ProductRole productRole)
		throws Exception {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				productRole.getExternalReferenceCode(),
				TestPropsValues.getUserId(), _cpDefinition.getCPDefinitionId(),
				Role.class.getName(), productRole.getRoleId());

		return productRoleResource.getProductRole(
			cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());
	}

	private void _testDeleteProductRoleWithProductAccount() throws Exception {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_addCPDefinitionVisibilityRel();

		assertHttpResponseStatusCode(
			404,
			productRoleResource.deleteProductRoleHttpResponse(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId()));
	}

	private void _testGetProductIdProductRolesPageWithNonexistentProduct()
		throws Exception {

		Page<ProductRole> page =
			productRoleResource.getProductIdProductRolesPage(
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

	@Inject
	private RoleLocalService _roleLocalService;

	private ServiceContext _serviceContext;

}
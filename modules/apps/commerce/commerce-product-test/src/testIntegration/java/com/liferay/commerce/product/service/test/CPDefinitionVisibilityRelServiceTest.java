/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.product.exception.NoSuchCPDefinitionException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

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
public class CPDefinitionVisibilityRelServiceTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			TestPropsValues.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com", serviceContext);

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), serviceContext);

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		_role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_user = UserTestUtil.addUser();

		_userLocalService.addRoleUser(_role.getRoleId(), _user);
	}

	@Test
	public void testAddCPDefinitionVisibilityRel() throws Exception {
		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.addCPDefinitionVisibilityRel(
				null, _cpDefinition.getCPDefinitionId(),
				AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.addCPDefinitionVisibilityRel(
				null, _cpDefinition.getCPDefinitionId(),
				AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.UPDATE});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.addCPDefinitionVisibilityRel(
				null, _cpDefinition.getCPDefinitionId(),
				AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());
		}

		Assert.assertNotNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(),
					_accountEntry.getAccountEntryId()));

		try {
			_cpDefinitionVisibilityRelService.addCPDefinitionVisibilityRel(
				null, RandomTestUtil.randomLong(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

			Assert.fail();
		}
		catch (NoSuchCPDefinitionException noSuchCPDefinitionException) {
			Assert.assertNotNull(noSuchCPDefinitionException);
		}
	}

	@Test
	public void testDeleteCPDefinitionVisibilityRel() throws Exception {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.UPDATE});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());
		}

		Assert.assertNull(
			_cpDefinitionVisibilityRelLocalService.
				fetchCPDefinitionVisibilityRel(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(),
					_accountEntry.getAccountEntryId()));
	}

	@Test
	public void testGetCPDefinitionVisibilityRel() throws Exception {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRel(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			Assert.assertEquals(
				cpDefinitionVisibilityRel,
				_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRel(
					cpDefinitionVisibilityRel.
						getCPDefinitionVisibilityRelId()));
		}
	}

	@Test
	public void testGetCPDefinitionVisibilityRels() throws Exception {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
				null, TestPropsValues.getUserId(),
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				_accountEntry.getAccountEntryId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRels(
				_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			List<CPDefinitionVisibilityRel> cpDefinitionVisibilityRels =
				_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRels(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);

			Assert.assertEquals(
				cpDefinitionVisibilityRels.toString(), 1,
				cpDefinitionVisibilityRels.size());
			Assert.assertEquals(
				cpDefinitionVisibilityRel, cpDefinitionVisibilityRels.get(0));
		}
	}

	@Test
	public void testGetCPDefinitionVisibilityRelsCount() throws Exception {
		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
			_accountEntry.getAccountEntryId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			_cpDefinitionVisibilityRelService.
				getCPDefinitionVisibilityRelsCount(
					_cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName());

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertNotNull(principalException);
		}

		_resourcePermissionLocalService.setResourcePermissions(
			_commerceCatalog.getCompanyId(), CommerceCatalog.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_commerceCatalog.getCommerceCatalogId()),
			_role.getRoleId(), new String[] {ActionKeys.VIEW});

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				_user, PermissionCheckerFactoryUtil.create(_user))) {

			Assert.assertEquals(
				1,
				_cpDefinitionVisibilityRelService.
					getCPDefinitionVisibilityRelsCount(
						_cpDefinition.getCPDefinitionId(),
						AccountEntry.class.getName()));
		}
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
	private CPDefinitionVisibilityRelService _cpDefinitionVisibilityRelService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
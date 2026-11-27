/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.configuration.CProductVersionConfiguration;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionValueRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.ProductOptionValue;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.SkuOption;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Page;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.delivery.catalog.client.problem.Problem;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class ProductOptionValueResourceTest
	extends BaseProductOptionValueResourceTestCase {

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

		_cpDefinitionOptionRel = CPTestUtil.addCPDefinitionOptionRel(
			testGroup.getGroupId(), _cpDefinition.getCPDefinitionId(), false,
			0);
	}

	@Override
	@Test
	public void testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage()
		throws Exception {

		super.
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage();

		_testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPageWithAnotherCPDefinition();
	}

	@Override
	@Test
	public void testGetChannelProductProductOptionProductOptionValuesPage()
		throws Exception {

		super.testGetChannelProductProductOptionProductOptionValuesPage();

		_testGetChannelProductProductOptionProductOptionValuesPageWithAnotherCPDefinition();
		_testGetChannelProductProductOptionProductOptionValuesPageWithCPDefinitionVersions();
	}

	@Override
	@Test
	public void testPostChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage()
		throws Exception {

		_addCPDefinitionOptionValueRel();

		CPOption cpOption = _cpDefinitionOptionRel.getCPOption();
		CProduct cProduct = _cpDefinition.getCProduct();

		Page<ProductOptionValue> productOptionValuesPage =
			productOptionValueResource.
				postChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage(
					_commerceChannel.getExternalReferenceCode(),
					cProduct.getExternalReferenceCode(),
					cpOption.getExternalReferenceCode(), null, null, null, null,
					Pagination.of(1, 10), new SkuOption[0]);

		Assert.assertEquals(1, productOptionValuesPage.getTotalCount());
	}

	@Override
	@Test
	public void testPostChannelProductProductOptionProductOptionValuesPage()
		throws Exception {

		_addCPDefinitionOptionValueRel();

		Page<ProductOptionValue> productOptionValuesPage =
			productOptionValueResource.
				postChannelProductProductOptionProductOptionValuesPage(
					_commerceChannel.getCommerceChannelId(),
					_cpDefinition.getCProductId(),
					_cpDefinitionOptionRel.getCPDefinitionOptionRelId(), null,
					null, null, null, Pagination.of(1, 10), new SkuOption[0]);

		Assert.assertEquals(1, productOptionValuesPage.getTotalCount());
	}

	@Override
	protected ProductOptionValue
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage_addProductOptionValue(
				String channelExternalReferenceCode,
				String productExternalReferenceCode,
				String productOptionExternalReferenceCode,
				ProductOptionValue productOptionValue)
		throws Exception {

		return _addCPDefinitionOptionValueRel();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage_getChannelExternalReferenceCode()
		throws Exception {

		return _commerceChannel.getExternalReferenceCode();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage_getProductExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected String
			testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage_getProductOptionExternalReferenceCode()
		throws Exception {

		CPOption cpOption = _cpDefinitionOptionRel.getCPOption();

		return cpOption.getExternalReferenceCode();
	}

	@Override
	protected ProductOptionValue
			testGetChannelProductProductOptionProductOptionValuesPage_addProductOptionValue(
				Long channelId, Long productId, Long productOptionId,
				ProductOptionValue productOptionValue)
		throws Exception {

		return _addCPDefinitionOptionValueRel();
	}

	@Override
	protected Long
			testGetChannelProductProductOptionProductOptionValuesPage_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long
			testGetChannelProductProductOptionProductOptionValuesPage_getProductId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected Long
			testGetChannelProductProductOptionProductOptionValuesPage_getProductOptionId()
		throws Exception {

		return _cpDefinitionOptionRel.getCPDefinitionOptionRelId();
	}

	private ProductOptionValue _addCPDefinitionOptionValueRel()
		throws Exception {

		CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
			_cpDefinitionOptionValueRelLocalService.
				addCPDefinitionOptionValueRel(
					null, _cpDefinitionOptionRel.getCPDefinitionOptionRelId(),
					StringUtil.toLowerCase(RandomTestUtil.randomString()),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomDouble(), _serviceContext);

		return new ProductOptionValue() {
			{
				id =
					cpDefinitionOptionValueRel.
						getCPDefinitionOptionValueRelId();
				key = cpDefinitionOptionValueRel.getKey();
				priority = cpDefinitionOptionValueRel.getPriority();
				productOptionId =
					cpDefinitionOptionValueRel.getCPDefinitionOptionRelId();
			}
		};
	}

	private void _assertNotFound(ThrowingRunnable throwingRunnable) {
		Problem.ProblemException problemException = Assert.assertThrows(
			Problem.ProblemException.class, throwingRunnable);

		Problem problem = problemException.getProblem();

		Assert.assertEquals("NOT_FOUND", problem.getStatus());
	}

	private void _testGetChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPageWithAnotherCPDefinition()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);

		CPDefinitionOptionRel cpDefinitionOptionRel =
			CPTestUtil.addCPDefinitionOptionRel(
				testGroup.getGroupId(), cpDefinition1.getCPDefinitionId(),
				false, 1);

		CProduct cProduct1 = cpDefinition1.getCProduct();

		CPOption cpOption = cpDefinitionOptionRel.getCPOption();

		Page<ProductOptionValue> productOptionValuesPage =
			productOptionValueResource.
				getChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage(
					_commerceChannel.getExternalReferenceCode(),
					cProduct1.getExternalReferenceCode(),
					cpOption.getExternalReferenceCode(), null, null, null, null,
					Pagination.of(1, 10));

		Assert.assertEquals(1, productOptionValuesPage.getTotalCount());

		CProduct cProduct2 = _cpDefinition.getCProduct();

		_assertNotFound(
			() ->
				productOptionValueResource.
					getChannelByExternalReferenceCodeChannelExternalReferenceCodeProductByExternalReferenceCodeProductExternalReferenceCodeProductOptionByExternalReferenceCodeProductOptionExternalReferenceCodeProductOptionValuesPage(
						_commerceChannel.getExternalReferenceCode(),
						cProduct2.getExternalReferenceCode(),
						cpOption.getExternalReferenceCode(), null, null, null,
						null, Pagination.of(1, 10)));
	}

	private void _testGetChannelProductProductOptionProductOptionValuesPageWithAnotherCPDefinition()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);

		CPDefinitionOptionRel cpDefinitionOptionRel =
			CPTestUtil.addCPDefinitionOptionRel(
				testGroup.getGroupId(), cpDefinition1.getCPDefinitionId(),
				false, 1);

		Page<ProductOptionValue> productOptionValuesPage =
			productOptionValueResource.
				getChannelProductProductOptionProductOptionValuesPage(
					_commerceChannel.getCommerceChannelId(),
					cpDefinition1.getCProductId(),
					cpDefinitionOptionRel.getCPDefinitionOptionRelId(), null,
					null, null, null, Pagination.of(1, 10));

		Assert.assertEquals(1, productOptionValuesPage.getTotalCount());

		_assertNotFound(
			() ->
				productOptionValueResource.
					getChannelProductProductOptionProductOptionValuesPage(
						_commerceChannel.getCommerceChannelId(),
						_cpDefinition.getCProductId(),
						cpDefinitionOptionRel.getCPDefinitionOptionRelId(),
						null, null, null, null, Pagination.of(1, 10)));
	}

	private void _testGetChannelProductProductOptionProductOptionValuesPageWithCPDefinitionVersions()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);

		CPDefinitionOptionRel cpDefinitionOptionRel1 =
			CPTestUtil.addCPDefinitionOptionRel(
				testGroup.getGroupId(), cpDefinition1.getCPDefinitionId(),
				false, 1);

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						testCompany.getCompanyId(),
						CProductVersionConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).put(
							"versionThreshold", 2
						).build())) {

			CPDefinition cpDefinition2 =
				_cpDefinitionLocalService.copyCPDefinition(
					cpDefinition1.getCPDefinitionId());

			CPDefinitionOptionRel cpDefinitionOptionRel2 =
				_cpDefinitionOptionRelLocalService.fetchCPDefinitionOptionRel(
					cpDefinition2.getCPDefinitionId(),
					cpDefinitionOptionRel1.getCPOptionId());

			_assertNotFound(
				() ->
					productOptionValueResource.
						getChannelProductProductOptionProductOptionValuesPage(
							_commerceChannel.getCommerceChannelId(),
							cpDefinition1.getCProductId(),
							cpDefinitionOptionRel2.getCPDefinitionOptionRelId(),
							null, null, null, null, Pagination.of(1, 10)));

			_cpDefinitionLocalService.updateStatus(
				_user.getUserId(), cpDefinition2.getCPDefinitionId(),
				WorkflowConstants.STATUS_APPROVED, _serviceContext,
				Collections.emptyMap());

			Page<ProductOptionValue> productOptionValuesPage =
				productOptionValueResource.
					getChannelProductProductOptionProductOptionValuesPage(
						_commerceChannel.getCommerceChannelId(),
						cpDefinition1.getCProductId(),
						cpDefinitionOptionRel1.getCPDefinitionOptionRelId(),
						null, null, null, null, Pagination.of(1, 10));

			Assert.assertEquals(1, productOptionValuesPage.getTotalCount());

			productOptionValuesPage =
				productOptionValueResource.
					getChannelProductProductOptionProductOptionValuesPage(
						_commerceChannel.getCommerceChannelId(),
						cpDefinition1.getCProductId(),
						cpDefinitionOptionRel2.getCPDefinitionOptionRelId(),
						null, null, null, null, Pagination.of(1, 10));

			Assert.assertEquals(1, productOptionValuesPage.getTotalCount());
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private CommerceChannel _commerceChannel;
	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private CPDefinitionOptionRel _cpDefinitionOptionRel;

	@Inject
	private CPDefinitionOptionRelLocalService
		_cpDefinitionOptionRelLocalService;

	@Inject
	private CPDefinitionOptionValueRelLocalService
		_cpDefinitionOptionValueRelLocalService;

	private ServiceContext _serviceContext;
	private User _user;

}
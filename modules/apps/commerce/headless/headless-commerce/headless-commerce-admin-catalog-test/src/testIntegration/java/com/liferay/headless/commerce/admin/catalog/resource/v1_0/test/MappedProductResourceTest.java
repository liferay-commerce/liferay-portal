/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalServiceUtil;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.shop.by.diagram.constants.CSDiagramCPTypeConstants;
import com.liferay.commerce.shop.by.diagram.model.CSDiagramPin;
import com.liferay.commerce.shop.by.diagram.service.CSDiagramPinLocalService;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.MappedProduct;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class MappedProductResourceTest
	extends BaseMappedProductResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		CommerceCatalog commerceCatalog =
			CommerceCatalogLocalServiceUtil.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(),
				LocaleUtil.US.getDisplayLanguage(),
				ServiceContextTestUtil.getServiceContext(
					testCompany.getGroupId()));

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), CSDiagramCPTypeConstants.NAME, true,
			true);
	}

	@Override
	@Test
	public void testDeleteMappedProduct() throws Exception {
		super.testDeleteMappedProduct();

		_testDeleteMappedProductWithPins();
	}

	@Override
	@Test
	public void testPatchMappedProduct() throws Exception {
		MappedProduct postMappedProduct = _addMappedProduct(
			randomMappedProduct());

		MappedProduct randomPatchMappedProduct = randomPatchMappedProduct();

		mappedProductResource.patchMappedProduct(
			postMappedProduct.getId(), randomPatchMappedProduct);

		MappedProduct expectedMappedProduct = postMappedProduct.clone();

		BeanTestUtil.copyProperties(
			randomPatchMappedProduct, expectedMappedProduct);

		MappedProduct getMappedProduct =
			mappedProductResource.getProductIdMappedProductBySequence(
				_cpDefinition.getCProductId(),
				randomPatchMappedProduct.getSequence());

		assertEquals(expectedMappedProduct, getMappedProduct);
		assertValid(getMappedProduct);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"quantity", "sequence", "sku", "type"};
	}

	@Override
	protected MappedProduct randomMappedProduct() throws Exception {
		return new MappedProduct() {
			{
				quantity = RandomTestUtil.randomInt();
				sequence = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				sku = StringUtil.toLowerCase(RandomTestUtil.randomString());
				type = MappedProduct.Type.EXTERNAL;
			}
		};
	}

	@Override
	protected MappedProduct testDeleteMappedProduct_addMappedProduct()
		throws Exception {

		return _addMappedProduct(randomMappedProduct());
	}

	@Override
	protected MappedProduct
			testGetProductByExternalReferenceCodeMappedProductBySequence_addMappedProduct()
		throws Exception {

		return _addMappedProduct(randomMappedProduct());
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodeMappedProductBySequence_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected MappedProduct
			testGetProductByExternalReferenceCodeMappedProductsPage_addMappedProduct(
				String externalReferenceCode, MappedProduct mappedProduct)
		throws Exception {

		return mappedProductResource.
			postProductByExternalReferenceCodeMappedProduct(
				externalReferenceCode, mappedProduct);
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodeMappedProductsPage_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected MappedProduct
			testGetProductIdMappedProductBySequence_addMappedProduct()
		throws Exception {

		return _addMappedProduct(randomMappedProduct());
	}

	@Override
	protected Long testGetProductIdMappedProductBySequence_getId(
			MappedProduct mappedProduct)
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected MappedProduct testGetProductIdMappedProductsPage_addMappedProduct(
			Long id, MappedProduct mappedProduct)
		throws Exception {

		return mappedProductResource.postProductIdMappedProduct(
			id, mappedProduct);
	}

	@Override
	protected Long testGetProductIdMappedProductsPage_getId() throws Exception {
		return _cpDefinition.getCProductId();
	}

	@Override
	protected String
			testGraphQLGetProductByExternalReferenceCodeMappedProductBySequence_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected Long testGraphQLGetProductIdMappedProductBySequence_getId(
			MappedProduct mappedProduct)
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected MappedProduct testGraphQLMappedProduct_addMappedProduct()
		throws Exception {

		return _addMappedProduct(randomMappedProduct());
	}

	@Override
	protected MappedProduct
			testPostProductByExternalReferenceCodeMappedProduct_addMappedProduct(
				MappedProduct mappedProduct)
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return mappedProductResource.
			postProductByExternalReferenceCodeMappedProduct(
				cProduct.getExternalReferenceCode(), mappedProduct);
	}

	@Override
	protected MappedProduct testPostProductIdMappedProduct_addMappedProduct(
			MappedProduct mappedProduct)
		throws Exception {

		return _addMappedProduct(mappedProduct);
	}

	private MappedProduct _addMappedProduct(MappedProduct mappedProduct)
		throws Exception {

		return mappedProductResource.postProductIdMappedProduct(
			_cpDefinition.getCProductId(), mappedProduct);
	}

	private void _testDeleteMappedProductWithPins() throws Exception {
		MappedProduct mappedProduct = _addMappedProduct(randomMappedProduct());

		CSDiagramPin csDiagramPin = _csDiagramPinLocalService.addCSDiagramPin(
			TestPropsValues.getUserId(), _cpDefinition.getCPDefinitionId(),
			RandomTestUtil.randomDouble(), RandomTestUtil.randomDouble(),
			mappedProduct.getSequence());

		mappedProductResource.deleteMappedProduct(mappedProduct.getId());

		Assert.assertNull(
			_csDiagramPinLocalService.fetchCSDiagramPin(
				csDiagramPin.getCSDiagramPinId()));
	}

	private CPDefinition _cpDefinition;

	@Inject
	private CSDiagramPinLocalService _csDiagramPinLocalService;

}
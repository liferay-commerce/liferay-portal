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
import com.liferay.commerce.shop.by.diagram.service.CSDiagramEntryLocalService;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.MappedProduct;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Pin;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 */
@RunWith(Arquillian.class)
public class PinResourceTest extends BasePinResourceTestCase {

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
	public void testDeletePin() throws Exception {
		super.testDeletePin();

		_testDeletePinWithMappedProduct();
	}

	@Override
	@Test
	public void testPatchPin() throws Exception {
		Pin postPin = _addPin(randomPin());

		Pin randomPatchPin = randomPatchPin();

		pinResource.patchPin(postPin.getId(), randomPatchPin);

		Pin expectedPin = postPin.clone();

		BeanTestUtil.copyProperties(randomPatchPin, expectedPin);

		Page<Pin> page = pinResource.getProductIdPinsPage(
			_cpDefinition.getCProductId(), null, Pagination.of(1, 10), null);

		assertContains(expectedPin, (List<Pin>)page.getItems());
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"positionX", "positionY", "sequence"};
	}

	@Override
	protected Pin testDeletePin_addPin() throws Exception {
		return _addPin(randomPin());
	}

	@Override
	protected Pin testGetProductByExternalReferenceCodePinsPage_addPin(
			String externalReferenceCode, Pin pin)
		throws Exception {

		return pinResource.postProductByExternalReferenceCodePin(
			externalReferenceCode, pin);
	}

	@Override
	protected String
			testGetProductByExternalReferenceCodePinsPage_getExternalReferenceCode()
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return cProduct.getExternalReferenceCode();
	}

	@Override
	protected Pin testGetProductIdPinsPage_addPin(Long id, Pin pin)
		throws Exception {

		return pinResource.postProductIdPin(id, pin);
	}

	@Override
	protected Long testGetProductIdPinsPage_getId() throws Exception {
		return _cpDefinition.getCProductId();
	}

	@Override
	protected Pin testGraphQLPin_addPin() throws Exception {
		return _addPin(randomPin());
	}

	@Override
	protected Pin testPostProductByExternalReferenceCodePin_addPin(Pin pin)
		throws Exception {

		CProduct cProduct = _cpDefinition.getCProduct();

		return pinResource.postProductByExternalReferenceCodePin(
			cProduct.getExternalReferenceCode(), pin);
	}

	@Override
	protected Pin testPostProductIdPin_addPin(Pin pin) throws Exception {
		return _addPin(pin);
	}

	private Pin _addPin(Pin pin) throws Exception {
		return pinResource.postProductIdPin(_cpDefinition.getCProductId(), pin);
	}

	private void _testDeletePinWithMappedProduct() throws Exception {
		Pin randomPin1 = randomPin();

		randomPin1.setMappedProduct(
			new MappedProduct() {
				{
					quantity = RandomTestUtil.randomInt();
					sequence = randomPin1.getSequence();
					sku = StringUtil.toLowerCase(RandomTestUtil.randomString());
					type = MappedProduct.Type.EXTERNAL;
				}
			});

		Pin pin1 = _addPin(randomPin1);

		Pin randomPin2 = randomPin();

		randomPin2.setSequence(randomPin1.getSequence());

		Pin pin2 = _addPin(randomPin2);

		pinResource.deletePin(pin2.getId());

		Assert.assertNotNull(
			_csDiagramEntryLocalService.fetchCSDiagramEntry(
				_cpDefinition.getCPDefinitionId(), randomPin1.getSequence()));

		pinResource.deletePin(pin1.getId());

		Assert.assertNull(
			_csDiagramEntryLocalService.fetchCSDiagramEntry(
				_cpDefinition.getCPDefinitionId(), randomPin1.getSequence()));
	}

	private CPDefinition _cpDefinition;

	@Inject
	private CSDiagramEntryLocalService _csDiagramEntryLocalService;

}
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.service.CPInstanceLocalServiceUtil;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.SkuSubscriptionConfiguration;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 * @author Crescenzo Rega
 */
@RunWith(Arquillian.class)
public class SkuSubscriptionConfigurationResourceTest
	extends BaseSkuSubscriptionConfigurationResourceTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);
	}

	@Override
	@Test
	public void testGetSkuByExternalReferenceCodeSubscriptionConfiguration()
		throws Exception {

		SkuSubscriptionConfiguration addSkuSubscriptionConfiguration =
			_addSkuSubscriptionConfiguration(
				randomSkuSubscriptionConfiguration());

		SkuSubscriptionConfiguration
			getSkuByExternalReferenceCodeSubscriptionConfiguration =
				skuSubscriptionConfigurationResource.
					getSkuByExternalReferenceCodeSubscriptionConfiguration(
						_cpInstance.getExternalReferenceCode());

		assertValid(getSkuByExternalReferenceCodeSubscriptionConfiguration);
		assertEquals(
			addSkuSubscriptionConfiguration,
			getSkuByExternalReferenceCodeSubscriptionConfiguration);
	}

	@Override
	@Test
	public void testGetSkuIdSubscriptionConfiguration() throws Exception {
		SkuSubscriptionConfiguration addSkuSubscriptionConfiguration =
			_addSkuSubscriptionConfiguration(
				randomSkuSubscriptionConfiguration());

		SkuSubscriptionConfiguration
			getSkuByExternalReferenceCodeSubscriptionConfiguration =
				skuSubscriptionConfigurationResource.
					getSkuIdSubscriptionConfiguration(
						_cpInstance.getCPInstanceId());

		assertValid(getSkuByExternalReferenceCodeSubscriptionConfiguration);
		assertEquals(
			addSkuSubscriptionConfiguration,
			getSkuByExternalReferenceCodeSubscriptionConfiguration);
	}

	@Override
	@Test
	public void testPatchSkuByExternalReferenceCodeSubscriptionConfiguration()
		throws Exception {

		_addSkuSubscriptionConfiguration(randomSkuSubscriptionConfiguration());

		SkuSubscriptionConfiguration
			getSkuByExternalReferenceCodeSubscriptionConfiguration =
				skuSubscriptionConfigurationResource.
					getSkuIdSubscriptionConfiguration(
						_cpInstance.getCPInstanceId());

		getSkuByExternalReferenceCodeSubscriptionConfiguration.setEnable(
			Boolean.TRUE);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setSubscriptionType(
				SkuSubscriptionConfiguration.SubscriptionType.create("daily"));
		getSkuByExternalReferenceCodeSubscriptionConfiguration.setLength(11);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setOverrideSubscriptionInfo(Boolean.TRUE);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setNumberOfLength(11L);

		SkuSubscriptionConfiguration patchSkuIdSubscriptionConfiguration =
			skuSubscriptionConfigurationResource.
				patchSkuByExternalReferenceCodeSubscriptionConfiguration(
					_cpInstance.getExternalReferenceCode(),
					getSkuByExternalReferenceCodeSubscriptionConfiguration);

		assertValid(getSkuByExternalReferenceCodeSubscriptionConfiguration);
		assertEquals(
			getSkuByExternalReferenceCodeSubscriptionConfiguration,
			patchSkuIdSubscriptionConfiguration);
	}

	@Override
	@Test
	public void testPatchSkuIdSubscriptionConfiguration() throws Exception {
		_addSkuSubscriptionConfiguration(randomSkuSubscriptionConfiguration());

		SkuSubscriptionConfiguration
			getSkuByExternalReferenceCodeSubscriptionConfiguration =
				skuSubscriptionConfigurationResource.
					getSkuIdSubscriptionConfiguration(
						_cpInstance.getCPInstanceId());

		getSkuByExternalReferenceCodeSubscriptionConfiguration.setEnable(
			Boolean.TRUE);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setSubscriptionType(
				SkuSubscriptionConfiguration.SubscriptionType.create(
					"monthly"));
		getSkuByExternalReferenceCodeSubscriptionConfiguration.setLength(22);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setOverrideSubscriptionInfo(Boolean.TRUE);
		getSkuByExternalReferenceCodeSubscriptionConfiguration.
			setNumberOfLength(22L);

		SkuSubscriptionConfiguration patchSkuIdSubscriptionConfiguration =
			skuSubscriptionConfigurationResource.
				patchSkuIdSubscriptionConfiguration(
					_cpInstance.getCPInstanceId(),
					getSkuByExternalReferenceCodeSubscriptionConfiguration);

		assertValid(getSkuByExternalReferenceCodeSubscriptionConfiguration);
		assertEquals(
			getSkuByExternalReferenceCodeSubscriptionConfiguration,
			patchSkuIdSubscriptionConfiguration);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"enable", "length", "numberOfLength", "overrideSubscriptionInfo",
			"subscriptionType"
		};
	}

	@Override
	protected SkuSubscriptionConfiguration randomSkuSubscriptionConfiguration()
		throws Exception {

		return new SkuSubscriptionConfiguration() {
			{
				enable = RandomTestUtil.randomBoolean();
				length = RandomTestUtil.randomInt();
				numberOfLength = RandomTestUtil.randomLong();
				overrideSubscriptionInfo = RandomTestUtil.randomBoolean();
				subscriptionType = SubscriptionType.create(
					_SUBSCRIPTIONTYPES
						[RandomTestUtil.randomInt(
							0, _SUBSCRIPTIONTYPES.length - 1)]);
			}
		};
	}

	private SkuSubscriptionConfiguration _addSkuSubscriptionConfiguration(
			SkuSubscriptionConfiguration skuSubscriptionConfiguration)
		throws Exception {

		_cpInstance = CPTestUtil.addCPInstance(_user.getGroupId());

		_cpInstance.setOverrideSubscriptionInfo(
			skuSubscriptionConfiguration.getOverrideSubscriptionInfo());
		_cpInstance.setSubscriptionEnabled(
			skuSubscriptionConfiguration.getEnable());
		_cpInstance.setSubscriptionLength(
			skuSubscriptionConfiguration.getLength());
		_cpInstance.setSubscriptionType(
			skuSubscriptionConfiguration.getSubscriptionTypeAsString());
		_cpInstance.setMaxSubscriptionCycles(
			skuSubscriptionConfiguration.getNumberOfLength());

		_cpInstance = CPInstanceLocalServiceUtil.updateCPInstance(_cpInstance);

		return _toSkuSubscriptionConfiguration();
	}

	private SkuSubscriptionConfiguration _toSkuSubscriptionConfiguration() {
		return new SkuSubscriptionConfiguration() {
			{
				enable = _cpInstance.isSubscriptionEnabled();
				length = _cpInstance.getSubscriptionLength();
				numberOfLength = _cpInstance.getMaxSubscriptionCycles();
				overrideSubscriptionInfo =
					_cpInstance.getOverrideSubscriptionInfo();
				subscriptionType = SubscriptionType.create(
					_cpInstance.getSubscriptionType());
				subscriptionTypeSettings =
					_cpInstance.getSubscriptionTypeSettingsProperties();
			}
		};
	}

	private static final String[] _SUBSCRIPTIONTYPES = {
		"daily", "monthly", "weekly", "yearly"
	};

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	@DeleteAfterTestRun
	private User _user;

}
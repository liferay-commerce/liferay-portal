/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.notification.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.unit.BatchEngineUnitProcessor;
import com.liferay.batch.engine.unit.BatchEngineUnitReader;
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.notification.service.NotificationTemplateLocalService;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.File;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Stefano Motta
 */
@FeatureFlags("LPD-24498")
@RunWith(Arquillian.class)
public class CommerceOrderNotificationTemplateTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {

		// TODO Delete the bundle deployment when the FF LPD-24498 is removed

		Bundle testBundle = FrameworkUtil.getBundle(
			CommerceOrderNotificationTemplateTest.class);

		BundleContext bundleContext = testBundle.getBundleContext();

		for (Bundle bundle : bundleContext.getBundles()) {
			if (Objects.equals(
					bundle.getSymbolicName(),
					"com.liferay.commerce.notification.service")) {

				_setUpProcessedFile(bundle, "00.notification.template");
				_setUpProcessedFile(bundle, "01.object.definition");

				CompletableFuture<Void> completableFuture =
					_batchEngineUnitProcessor.processBatchEngineUnits(
						_batchEngineUnitReader.getBatchEngineUnits(bundle));

				completableFuture.join();
			}
		}
	}

	@Test
	public void testCommerceOrderNotificationObjectActionAvailability()
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				TestPropsValues.getCompanyId(), "CommerceOrder");

		Assert.assertNotNull(objectDefinition);

		ObjectAction objectAction = _objectActionLocalService.fetchObjectAction(
			"L_COMMERCE_ORDER_NOTIFICATION",
			objectDefinition.getObjectDefinitionId());

		Assert.assertNotNull(objectAction);
		Assert.assertFalse(objectAction.isActive());
	}

	@Test
	public void testCommerceOrderNotificationTemplateAvailability()
		throws Exception {

		NotificationTemplate notificationTemplate =
			_notificationTemplateLocalService.
				fetchNotificationTemplateByExternalReferenceCode(
					"L_COMMERCE_ORDER_TEMPLATE",
					TestPropsValues.getCompanyId());

		Assert.assertNotNull(notificationTemplate);
	}

	private void _setUpProcessedFile(Bundle bundle, String processedFileName) {
		File processedFile = bundle.getDataFile(
			".com.liferay.commerce.notification.internal.batch." +
				processedFileName + ".batch.engine.data.json.0.processed");

		if ((processedFile != null) && processedFile.exists()) {
			processedFile.delete();
		}
	}

	@Inject
	private BatchEngineUnitProcessor _batchEngineUnitProcessor;

	@Inject
	private BatchEngineUnitReader _batchEngineUnitReader;

	@Inject
	private NotificationTemplateLocalService _notificationTemplateLocalService;

	@Inject
	private ObjectActionLocalService _objectActionLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}
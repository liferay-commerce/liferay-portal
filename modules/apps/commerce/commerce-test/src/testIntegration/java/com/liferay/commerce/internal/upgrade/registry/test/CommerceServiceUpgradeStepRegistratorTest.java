/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.upgrade.registry.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.upgrade.DummyUpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeStep;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Lianne Louie
 */
@RunWith(Arquillian.class)
public class CommerceServiceUpgradeStepRegistratorTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() {
		Bundle bundle = FrameworkUtil.getBundle(
			_upgradeStepRegistrator.getClass());

		Dictionary<String, String> headers = bundle.getHeaders(
			StringPool.BLANK);

		_requiredSchemaVersion = headers.get("Liferay-Require-SchemaVersion");

		_upgradeStepRegistrator.register(
			(fromSchemaVersion, toSchemaVersion, upgradeSteps) -> {
				_schemaVersions.put(fromSchemaVersion, toSchemaVersion);
				_upgradeSteps.put(fromSchemaVersion, upgradeSteps);
			});
	}

	@Test
	public void testRegister() {
		Assert.assertEquals("15.0.4", _schemaVersions.get("15.0.3"));
		Assert.assertEquals("15.0.5", _schemaVersions.get("15.0.4"));
		Assert.assertEquals("15.1.0", _schemaVersions.get("15.0.5"));

		UpgradeStep[] upgradeSteps = _upgradeSteps.get("15.0.3");

		Assert.assertEquals(
			DummyUpgradeProcess.class, upgradeSteps[0].getClass());

		upgradeSteps = _upgradeSteps.get("15.0.4");

		Class<?> clazz = upgradeSteps[0].getClass();

		Assert.assertEquals(
			"com.liferay.commerce.internal.upgrade.v15_0_5." +
				"CommerceOrderAttachmentRoleUpgradeProcess",
			clazz.getName());

		for (String fromSchemaVersion : _schemaVersions.keySet()) {
			Assert.assertEquals(
				_requiredSchemaVersion,
				_getFinalSchemaVersion(fromSchemaVersion));
		}
	}

	private String _getFinalSchemaVersion(String fromSchemaVersion) {
		String schemaVersion = fromSchemaVersion;

		while (_schemaVersions.containsKey(schemaVersion)) {
			schemaVersion = _schemaVersions.get(schemaVersion);
		}

		return schemaVersion;
	}

	private String _requiredSchemaVersion;
	private final Map<String, String> _schemaVersions = new HashMap<>();

	@Inject(
		filter = "(&(component.name=com.liferay.commerce.internal.upgrade.registry.CommerceServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

	private final Map<String, UpgradeStep[]> _upgradeSteps = new HashMap<>();

}
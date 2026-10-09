/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.manager.v1_0;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.internal.configuration.FunctionObjectEntryManagerConfiguration;
import com.liferay.object.rest.manager.exception.ObjectEntryManagerHttpException;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.petra.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.catapult.PortalCatapult;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermissionRegistryUtil;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Carolina Barbosa
 */
public class FunctionObjectEntryManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_setUpDTOConverterContext();
		_setUpFunctionObjectEntryManagerConfiguration();
		_setUpModelResourcePermissionRegistryUtilMockedStatic();
		_setUpObjectDefinition();
		_setUpObjectScopeProviderRegistry();
		_setUpPermissionCheckerFactory();
		_setUpPortalCatapult();
	}

	@After
	public void tearDown() {
		_httpComponentsUtilMockedStatic.close();
		_modelResourcePermissionRegistryUtilMockedStatic.close();
	}

	@Test
	public void testGetObjectEntry() throws Exception {
		DefaultNoticeableFuture<byte[]> defaultNoticeableFuture =
			new DefaultNoticeableFuture<>();

		String message = RandomTestUtil.randomString();

		defaultNoticeableFuture.setException(new PortalException(message));

		Mockito.when(
			_portalCatapult.launch(
				Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.anyLong())
		).thenReturn(
			defaultNoticeableFuture
		);

		ObjectEntryManagerHttpException objectEntryManagerHttpException =
			Assert.assertThrows(
				ObjectEntryManagerHttpException.class,
				() -> _functionObjectEntryManagerImpl.getObjectEntry(
					RandomTestUtil.randomLong(), _dtoConverterContext,
					RandomTestUtil.randomString(), _objectDefinition,
					RandomTestUtil.randomString()));

		Assert.assertEquals(
			message, objectEntryManagerHttpException.getMessage());
	}

	private void _setUpDTOConverterContext() {
		Mockito.when(
			_dtoConverterContext.getLocale()
		).thenReturn(
			LocaleUtil.US
		);

		Mockito.when(
			_dtoConverterContext.getUser()
		).thenReturn(
			Mockito.mock(User.class)
		);
	}

	private void _setUpFunctionObjectEntryManagerConfiguration() {
		ReflectionTestUtil.setFieldValue(
			_functionObjectEntryManagerImpl,
			"_functionObjectEntryManagerConfiguration",
			Mockito.mock(FunctionObjectEntryManagerConfiguration.class));
	}

	private void _setUpModelResourcePermissionRegistryUtilMockedStatic() {
		ModelResourcePermission<?> modelResourcePermission = Mockito.mock(
			ModelResourcePermission.class);

		Mockito.doReturn(
			Mockito.mock(PortletResourcePermission.class)
		).when(
			modelResourcePermission
		).getPortletResourcePermission();

		_modelResourcePermissionRegistryUtilMockedStatic.when(
			() ->
				ModelResourcePermissionRegistryUtil.getModelResourcePermission(
					Mockito.anyString())
		).thenReturn(
			modelResourcePermission
		);
	}

	private void _setUpObjectDefinition() {
		Mockito.when(
			_objectDefinition.getClassName()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_objectDefinition.getExternalReferenceCode()
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_objectDefinition.getScope()
		).thenReturn(
			RandomTestUtil.randomString()
		);
	}

	private void _setUpObjectScopeProviderRegistry() {
		ObjectScopeProviderRegistry objectScopeProviderRegistry = Mockito.mock(
			ObjectScopeProviderRegistry.class);

		Mockito.when(
			objectScopeProviderRegistry.getObjectScopeProvider(
				Mockito.anyString())
		).thenReturn(
			Mockito.mock(ObjectScopeProvider.class)
		);

		ReflectionTestUtil.setFieldValue(
			_functionObjectEntryManagerImpl, "objectScopeProviderRegistry",
			objectScopeProviderRegistry);
	}

	private void _setUpPermissionCheckerFactory() {
		ReflectionTestUtil.setFieldValue(
			_functionObjectEntryManagerImpl, "permissionCheckerFactory",
			Mockito.mock(PermissionCheckerFactory.class));
	}

	private void _setUpPortalCatapult() {
		ReflectionTestUtil.setFieldValue(
			_functionObjectEntryManagerImpl, "_portalCatapult",
			_portalCatapult);
	}

	private final DTOConverterContext _dtoConverterContext = Mockito.mock(
		DTOConverterContext.class);
	private final FunctionObjectEntryManagerImpl
		_functionObjectEntryManagerImpl = new FunctionObjectEntryManagerImpl();
	private final MockedStatic<HttpComponentsUtil>
		_httpComponentsUtilMockedStatic = Mockito.mockStatic(
			HttpComponentsUtil.class);
	private final MockedStatic<ModelResourcePermissionRegistryUtil>
		_modelResourcePermissionRegistryUtilMockedStatic = Mockito.mockStatic(
			ModelResourcePermissionRegistryUtil.class);
	private final ObjectDefinition _objectDefinition = Mockito.mock(
		ObjectDefinition.class);
	private final PortalCatapult _portalCatapult = Mockito.mock(
		PortalCatapult.class);

}
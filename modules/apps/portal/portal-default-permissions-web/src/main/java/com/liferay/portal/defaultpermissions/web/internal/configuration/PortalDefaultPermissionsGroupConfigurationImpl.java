/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.web.internal.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.defaultpermissions.configuration.PortalDefaultPermissionsConfiguration;
import com.liferay.portal.defaultpermissions.configuration.PortalDefaultPermissionsGroupConfiguration;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(
	property = "portal.default.permissions.scope=group",
	service = PortalDefaultPermissionsConfiguration.class
)
public class PortalDefaultPermissionsGroupConfigurationImpl
	implements PortalDefaultPermissionsConfiguration {

	@Override
	public Map<String, Map<String, String[]>> getDefaultPermissions(
		long companyId, long groupId) {

		try {
			PortalDefaultPermissionsGroupConfiguration
				portalDefaultPermissionsGroupConfiguration =
					_configurationProvider.getGroupConfiguration(
						PortalDefaultPermissionsGroupConfiguration.class,
						groupId);

			String defaultPermissions =
				portalDefaultPermissionsGroupConfiguration.defaultPermissions();

			if (Validator.isNull(defaultPermissions)) {
				return new HashMap<>();
			}

			return _objectMapper.readValue(defaultPermissions, _typeReference);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	@Override
	public Map<String, String[]> getDefaultPermissions(
		long companyId, long groupId, String className) {

		Map<String, Map<String, String[]>> defaultPermissions =
			getDefaultPermissions(companyId, groupId);

		if ((defaultPermissions != null) &&
			(defaultPermissions.get(className) != null)) {

			return defaultPermissions.get(className);
		}

		return _companyPortalDefaultPermissionsConfiguration.
			getDefaultPermissions(companyId, groupId, className);
	}

	@Override
	public ExtendedObjectClassDefinition.Scope getScope() {
		return ExtendedObjectClassDefinition.Scope.GROUP;
	}

	@Override
	public void setDefaultPermissions(
		long groupId, Map<String, Map<String, String[]>> defaultPermissions) {

		try {
			_configurationProvider.saveGroupConfiguration(
				PortalDefaultPermissionsGroupConfiguration.class, groupId,
				HashMapDictionaryBuilder.<String, Object>put(
					"defaultPermissions",
					_objectMapper.writeValueAsString(defaultPermissions)
				).build());
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PortalDefaultPermissionsGroupConfigurationImpl.class);

	@Reference(target = "(portal.default.permissions.scope=company)")
	private PortalDefaultPermissionsConfiguration
		_companyPortalDefaultPermissionsConfiguration;

	@Reference
	private ConfigurationProvider _configurationProvider;

	private final ObjectMapper _objectMapper = new ObjectMapper();
	private final TypeReference<Map<String, Map<String, String[]>>>
		_typeReference =
			new TypeReference<Map<String, Map<String, String[]>>>() {
			};

}
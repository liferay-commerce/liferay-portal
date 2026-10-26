/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.user.internal.util.v1_0;

import com.liferay.object.constants.ObjectConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Balazs Breier
 */
public class ObjectDefinitionResourceNameUtil {

	public static String toExternalReferenceCodeResourceName(
		long companyId,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		String resourceName) {

		if (StringUtil.startsWith(resourceName, _RESOURCE_NAME_PREFIX)) {
			return _toExternalReferenceCodeResourceName(
				objectDefinitionLocalService.fetchObjectDefinition(
					GetterUtil.getLong(
						resourceName.substring(
							_RESOURCE_NAME_PREFIX.length()))),
				_RESOURCE_NAME_PREFIX, resourceName);
		}

		if (StringUtil.startsWith(resourceName, _CLASS_NAME_PREFIX)) {
			return _toExternalReferenceCodeResourceName(
				objectDefinitionLocalService.fetchObjectDefinitionByClassName(
					companyId, resourceName),
				_CLASS_NAME_PREFIX, resourceName);
		}

		if (StringUtil.startsWith(resourceName, _PORTLET_ID_PREFIX)) {
			return _toExternalReferenceCodeResourceName(
				objectDefinitionLocalService.fetchObjectDefinitionByClassName(
					companyId,
					_CLASS_NAME_PREFIX +
						resourceName.substring(_PORTLET_ID_PREFIX.length())),
				_PORTLET_ID_PREFIX, resourceName);
		}

		return resourceName;
	}

	public static String toLocalResourceName(
		long companyId,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		String resourceName) {

		if (StringUtil.startsWith(resourceName, _RESOURCE_NAME_PREFIX)) {
			ObjectDefinition objectDefinition = _fetchObjectDefinition(
				companyId, objectDefinitionLocalService, _RESOURCE_NAME_PREFIX,
				resourceName);

			if (objectDefinition != null) {
				return _RESOURCE_NAME_PREFIX +
					objectDefinition.getObjectDefinitionId();
			}
		}
		else if (StringUtil.startsWith(resourceName, _CLASS_NAME_PREFIX)) {
			ObjectDefinition objectDefinition = _fetchObjectDefinition(
				companyId, objectDefinitionLocalService, _CLASS_NAME_PREFIX,
				resourceName);

			if (objectDefinition != null) {
				return objectDefinition.getClassName();
			}
		}
		else if (StringUtil.startsWith(resourceName, _PORTLET_ID_PREFIX)) {
			ObjectDefinition objectDefinition = _fetchObjectDefinition(
				companyId, objectDefinitionLocalService, _PORTLET_ID_PREFIX,
				resourceName);

			if (objectDefinition != null) {
				return objectDefinition.getPortletId();
			}
		}

		return resourceName;
	}

	private static ObjectDefinition _fetchObjectDefinition(
		long companyId,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		String prefix, String resourceName) {

		return objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				resourceName.substring(prefix.length()), companyId);
	}

	private static String _toExternalReferenceCodeResourceName(
		ObjectDefinition objectDefinition, String prefix, String resourceName) {

		if (objectDefinition == null) {
			return resourceName;
		}

		return prefix + objectDefinition.getExternalReferenceCode();
	}

	private static final String _CLASS_NAME_PREFIX =
		ObjectDefinitionConstants.CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION;

	private static final String _PORTLET_ID_PREFIX =
		ObjectPortletKeys.OBJECT_DEFINITIONS + StringPool.UNDERLINE;

	private static final String _RESOURCE_NAME_PREFIX =
		ObjectConstants.RESOURCE_NAME + StringPool.POUND;

}
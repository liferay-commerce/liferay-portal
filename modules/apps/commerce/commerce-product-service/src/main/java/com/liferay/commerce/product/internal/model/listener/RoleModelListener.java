/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.model.listener;

import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.Role;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Fabio Monaco
 */
@Component(service = ModelListener.class)
public class RoleModelListener extends BaseModelListener<Role> {

	@Override
	public void onBeforeRemove(Role role) throws ModelListenerException {
		try {
			_cpDefinitionVisibilityRelLocalService.
				deleteCPDefinitionVisibilityRels(
					Role.class.getName(), role.getRoleId());
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

}
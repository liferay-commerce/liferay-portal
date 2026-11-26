/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductRole;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Fabio Monaco
 */
@Component(
	property = "dto.class.name=com.liferay.commerce.product.model.CPDefinitionVisibilityRel",
	service = DTOConverter.class
)
public class ProductRoleDTOConverter
	implements DTOConverter<CPDefinitionVisibilityRel, ProductRole> {

	@Override
	public String getContentType() {
		return ProductRole.class.getSimpleName();
	}

	@Override
	public ProductRole toDTO(
			DTOConverterContext dtoConverterContext,
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws Exception {

		Role role = _roleLocalService.getRole(
			cpDefinitionVisibilityRel.getClassPK());

		return new ProductRole() {
			{
				setExternalReferenceCode(role::getExternalReferenceCode);
				setId(
					cpDefinitionVisibilityRel::getCPDefinitionVisibilityRelId);
				setName(role::getName);
				setRoleId(role::getRoleId);
				setRoleType(
					() -> ProductRole.RoleType.create(role.getTypeLabel()));
			}
		};
	}

	@Reference
	private RoleLocalService _roleLocalService;

}
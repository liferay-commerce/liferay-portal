/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.exception.NoSuchCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.service.CPDefinitionService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductRole;
import com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter.constants.DTOConverterConstants;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.ProductRoleResource;
import com.liferay.portal.kernel.change.tracking.CTAware;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Collections;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Fabio Monaco
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/product-role.properties",
	scope = ServiceScope.PROTOTYPE, service = ProductRoleResource.class
)
@CTAware
public class ProductRoleResourceImpl extends BaseProductRoleResourceImpl {

	@Override
	public void deleteProductRole(Long id) throws Exception {
		_getCPDefinitionVisibilityRel(id);

		_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(id);
	}

	@Override
	public Page<ProductRole> getProductByExternalReferenceCodeProductRolesPage(
			String externalReferenceCode, Pagination pagination)
		throws Exception {

		return _getProductRolesPage(
			_cpDefinitionService.
				fetchCPDefinitionByCProductExternalReferenceCode(
					externalReferenceCode, contextCompany.getCompanyId(),
					false),
			pagination);
	}

	@Override
	public Page<ProductRole> getProductIdProductRolesPage(
			Long id, Pagination pagination)
		throws Exception {

		return _getProductRolesPage(
			_cpDefinitionService.fetchCPDefinitionByCProductId(id, false),
			pagination);
	}

	@Override
	public ProductRole getProductRole(Long id) throws Exception {
		return _toProductRole(_getCPDefinitionVisibilityRel(id));
	}

	private CPDefinitionVisibilityRel _getCPDefinitionVisibilityRel(long id)
		throws Exception {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRel(id);

		if (!Objects.equals(
				cpDefinitionVisibilityRel.getClassName(),
				Role.class.getName())) {

			throw new NoSuchCPDefinitionVisibilityRelException();
		}

		return cpDefinitionVisibilityRel;
	}

	private Page<ProductRole> _getProductRolesPage(
			CPDefinition cpDefinition, Pagination pagination)
		throws Exception {

		if (cpDefinition == null) {
			return Page.of(Collections.emptyList());
		}

		return Page.of(
			transform(
				_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRels(
					cpDefinition.getCPDefinitionId(), Role.class.getName(),
					pagination.getStartPosition(), pagination.getEndPosition(),
					null),
				this::_toProductRole),
			pagination,
			_cpDefinitionVisibilityRelService.
				getCPDefinitionVisibilityRelsCount(
					cpDefinition.getCPDefinitionId(), Role.class.getName()));
	}

	private ProductRole _toProductRole(
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws Exception {

		return _productRoleDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId(),
				contextAcceptLanguage.getPreferredLocale()),
			cpDefinitionVisibilityRel);
	}

	@Reference
	private CPDefinitionService _cpDefinitionService;

	@Reference
	private CPDefinitionVisibilityRelService _cpDefinitionVisibilityRelService;

	@Reference(target = DTOConverterConstants.PRODUCT_ROLE_DTO_CONVERTER)
	private DTOConverter<CPDefinitionVisibilityRel, ProductRole>
		_productRoleDTOConverter;

}
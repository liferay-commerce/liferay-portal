/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.service.CPDefinitionService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductAccount;
import com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter.constants.DTOConverterConstants;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.ProductAccountResource;
import com.liferay.portal.kernel.change.tracking.CTAware;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Collections;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Fabio Monaco
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/product-account.properties",
	scope = ServiceScope.PROTOTYPE, service = ProductAccountResource.class
)
@CTAware
public class ProductAccountResourceImpl extends BaseProductAccountResourceImpl {

	@Override
	public void deleteProductAccount(Long id) throws Exception {
		_cpDefinitionVisibilityRelService.deleteCPDefinitionVisibilityRel(id);
	}

	@Override
	public ProductAccount getProductAccount(Long id) throws Exception {
		return _toProductAccount(
			_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRel(id));
	}

	@Override
	public Page<ProductAccount>
			getProductByExternalReferenceCodeProductAccountsPage(
				String externalReferenceCode, Pagination pagination)
		throws Exception {

		return _getProductAccountsPage(
			_cpDefinitionService.
				fetchCPDefinitionByCProductExternalReferenceCode(
					externalReferenceCode, contextCompany.getCompanyId(),
					false),
			pagination);
	}

	@Override
	public Page<ProductAccount> getProductIdProductAccountsPage(
			Long id, Pagination pagination)
		throws Exception {

		return _getProductAccountsPage(
			_cpDefinitionService.fetchCPDefinitionByCProductId(id, false),
			pagination);
	}

	private Page<ProductAccount> _getProductAccountsPage(
			CPDefinition cpDefinition, Pagination pagination)
		throws Exception {

		if (cpDefinition == null) {
			return Page.of(Collections.emptyList());
		}

		return Page.of(
			transform(
				_cpDefinitionVisibilityRelService.getCPDefinitionVisibilityRels(
					cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName(), pagination.getStartPosition(),
					pagination.getEndPosition(), null),
				this::_toProductAccount),
			pagination,
			_cpDefinitionVisibilityRelService.
				getCPDefinitionVisibilityRelsCount(
					cpDefinition.getCPDefinitionId(),
					AccountEntry.class.getName()));
	}

	private ProductAccount _toProductAccount(
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws Exception {

		return _productAccountDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				cpDefinitionVisibilityRel.getCPDefinitionVisibilityRelId(),
				contextAcceptLanguage.getPreferredLocale()),
			cpDefinitionVisibilityRel);
	}

	@Reference
	private CPDefinitionService _cpDefinitionService;

	@Reference
	private CPDefinitionVisibilityRelService _cpDefinitionVisibilityRelService;

	@Reference(target = DTOConverterConstants.PRODUCT_ACCOUNT_DTO_CONVERTER)
	private DTOConverter<CPDefinitionVisibilityRel, ProductAccount>
		_productAccountDTOConverter;

}
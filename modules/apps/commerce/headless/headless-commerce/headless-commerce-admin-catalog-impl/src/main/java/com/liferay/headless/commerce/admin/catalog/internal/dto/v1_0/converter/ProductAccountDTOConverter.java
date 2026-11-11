/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductAccount;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Fabio Monaco
 */
@Component(
	property = {
		"default=true",
		"dto.class.name=com.liferay.commerce.product.model.CPDefinitionVisibilityRel"
	},
	service = DTOConverter.class
)
public class ProductAccountDTOConverter
	implements DTOConverter<CPDefinitionVisibilityRel, ProductAccount> {

	@Override
	public String getContentType() {
		return ProductAccount.class.getSimpleName();
	}

	@Override
	public ProductAccount toDTO(
			DTOConverterContext dtoConverterContext,
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws Exception {

		AccountEntry accountEntry = _accountEntryLocalService.getAccountEntry(
			cpDefinitionVisibilityRel.getClassPK());

		return new ProductAccount() {
			{
				setAccountExternalReferenceCode(
					accountEntry::getExternalReferenceCode);
				setAccountId(accountEntry::getAccountEntryId);
				setAccountType(
					() -> ProductAccount.AccountType.create(
						accountEntry.getType()));
				setExternalReferenceCode(
					cpDefinitionVisibilityRel::getExternalReferenceCode);
				setId(
					cpDefinitionVisibilityRel::getCPDefinitionVisibilityRelId);
				setName(accountEntry::getName);
			}
		};
	}

	@Reference
	private AccountEntryLocalService _accountEntryLocalService;

}
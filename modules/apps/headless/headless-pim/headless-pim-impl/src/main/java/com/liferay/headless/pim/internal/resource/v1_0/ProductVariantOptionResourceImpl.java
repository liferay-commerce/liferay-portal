/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.internal.resource.v1_0;

import com.liferay.headless.pim.dto.v1_0.ProductVariantOption;
import com.liferay.headless.pim.dto.v1_0.ProductVariantOptionValue;
import com.liferay.headless.pim.resource.v1_0.ProductVariantOptionResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.GroupUtil;
import com.liferay.site.pim.site.initializer.variant.PIMProductVariantHelper;
import com.liferay.site.pim.site.initializer.variant.PIMProductVariantOption;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Balazs Breier
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/product-variant-option.properties",
	scope = ServiceScope.PROTOTYPE, service = ProductVariantOptionResource.class
)
public class ProductVariantOptionResourceImpl
	extends BaseProductVariantOptionResourceImpl {

	@Override
	public Page<ProductVariantOption> getScopeScopeKeyProductVariantOptionsPage(
			String scopeKey, String className, String externalReferenceCode,
			String search, Pagination pagination)
		throws Exception {

		Long groupId = GroupUtil.getGroupId(
			contextCompany.getCompanyId(), scopeKey, groupLocalService);

		if (groupId == null) {
			throw new NoSuchGroupException();
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinitionByClassName(
				contextCompany.getCompanyId(), className);

		List<PIMProductVariantOption> pimProductVariantOptions =
			_pimProductVariantHelper.getPIMProductVariantOptions(
				_objectEntryLocalService.getObjectEntry(
					externalReferenceCode, groupId,
					objectDefinition.getObjectDefinitionId()));

		if (Validator.isNotNull(search)) {
			String lowerCaseSearch = StringUtil.toLowerCase(search);

			pimProductVariantOptions = ListUtil.filter(
				pimProductVariantOptions,
				pimProductVariantOption -> {
					ObjectField objectField =
						pimProductVariantOption.getObjectField();

					String label = StringUtil.toLowerCase(
						objectField.getLabel(
							contextAcceptLanguage.getPreferredLocale()));
					String name = StringUtil.toLowerCase(objectField.getName());

					return label.contains(lowerCaseSearch) ||
						   name.contains(lowerCaseSearch);
				});
		}

		return Page.of(
			transform(
				ListUtil.subList(
					pimProductVariantOptions, pagination.getStartPosition(),
					pagination.getEndPosition()),
				this::_toProductVariantOption),
			pagination, pimProductVariantOptions.size());
	}

	private ProductVariantOption _toProductVariantOption(
		PIMProductVariantOption pimProductVariantOption) {

		ObjectField objectField = pimProductVariantOption.getObjectField();

		return new ProductVariantOption() {
			{
				setLabel(
					() -> objectField.getLabel(
						contextAcceptLanguage.getPreferredLocale()));
				setName(objectField::getName);
				setProductVariantOptionValues(
					() -> transformToArray(
						pimProductVariantOption.getListTypeEntries(),
						listTypeEntry -> new ProductVariantOptionValue() {
							{
								setKey(listTypeEntry::getKey);
								setLabel(
									() -> listTypeEntry.getName(
										contextAcceptLanguage.
											getPreferredLocale()));
							}
						},
						ProductVariantOptionValue.class));
				setValue(
					() -> {
						if (Validator.isNull(
								pimProductVariantOption.getValue())) {

							return null;
						}

						return pimProductVariantOption.getValue();
					});
			}
		};
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PIMProductVariantHelper _pimProductVariantHelper;

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.internal.resource.v1_0;

import com.liferay.headless.pim.dto.v1_0.VariantOption;
import com.liferay.headless.pim.dto.v1_0.VariantOptionValue;
import com.liferay.headless.pim.resource.v1_0.VariantOptionResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
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
import com.liferay.site.pim.site.initializer.engine.PIMVariantEngine;
import com.liferay.site.pim.site.initializer.variant.PIMVariantOption;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Balazs Breier
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/variant-option.properties",
	scope = ServiceScope.PROTOTYPE, service = VariantOptionResource.class
)
public class VariantOptionResourceImpl extends BaseVariantOptionResourceImpl {

	@Override
	public Page<VariantOption> getScopeScopeKeyVariantOptionsPage(
			String scopeKey, String className, String externalReferenceCode,
			String search, Pagination pagination)
		throws Exception {

		List<PIMVariantOption> pimVariantOptions =
			_pimVariantEngine.getPIMVariantOptions(
				_getObjectEntry(
					className, externalReferenceCode, _getGroupId(scopeKey)));

		if (Validator.isNotNull(search)) {
			String lowerCaseSearch = StringUtil.toLowerCase(search);

			pimVariantOptions = ListUtil.filter(
				pimVariantOptions,
				pimVariantOption -> {
					ObjectField objectField = pimVariantOption.getObjectField();

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
					pimVariantOptions, pagination.getStartPosition(),
					pagination.getEndPosition()),
				this::_toVariantOption),
			pagination, pimVariantOptions.size());
	}

	private long _getGroupId(String scopeKey) throws Exception {
		Long groupId = GroupUtil.getGroupId(
			contextCompany.getCompanyId(), scopeKey, groupLocalService);

		if (groupId == null) {
			throw new NoSuchGroupException();
		}

		return groupId;
	}

	private ObjectEntry _getObjectEntry(
			String className, String externalReferenceCode, long groupId)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinitionByClassName(
				contextCompany.getCompanyId(), className);

		return _objectEntryLocalService.getObjectEntry(
			externalReferenceCode, groupId,
			objectDefinition.getObjectDefinitionId());
	}

	private VariantOption _toVariantOption(PIMVariantOption pimVariantOption) {
		ObjectField objectField = pimVariantOption.getObjectField();

		return new VariantOption() {
			{
				setLabel(
					() -> objectField.getLabel(
						contextAcceptLanguage.getPreferredLocale()));
				setName(objectField::getName);
				setValue(
					() -> {
						if (Validator.isNull(pimVariantOption.getValue())) {
							return null;
						}

						return pimVariantOption.getValue();
					});
				setVariantOptionValues(
					() -> transformToArray(
						pimVariantOption.getListTypeEntries(),
						listTypeEntry -> new VariantOptionValue() {
							{
								setKey(listTypeEntry::getKey);
								setLabel(
									() -> listTypeEntry.getName(
										contextAcceptLanguage.
											getPreferredLocale()));
							}
						},
						VariantOptionValue.class));
			}
		};
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PIMVariantEngine _pimVariantEngine;

}
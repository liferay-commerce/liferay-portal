/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter;

import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductOptionValue;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.text.DateFormat;
import java.text.Format;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "dto.class.name=com.liferay.commerce.product.model.CPDefinitionOptionValueRel",
	service = DTOConverter.class
)
public class ProductOptionValueDTOConverter
	implements DTOConverter<CPDefinitionOptionValueRel, ProductOptionValue> {

	@Override
	public String getContentType() {
		return ProductOptionValue.class.getSimpleName();
	}

	@Override
	public ProductOptionValue toDTO(
			DTOConverterContext dtoConverterContext,
			CPDefinitionOptionValueRel cpDefinitionOptionValueRel)
		throws Exception {

		return new ProductOptionValue() {
			{
				setDeltaPrice(cpDefinitionOptionValueRel::getPrice);
				setDuration(cpDefinitionOptionValueRel::getDuration);
				setDurationType(cpDefinitionOptionValueRel::getDurationType);
				setId(
					cpDefinitionOptionValueRel::
						getCPDefinitionOptionValueRelId);
				setKey(cpDefinitionOptionValueRel::getKey);
				setName(
					() -> {
						name = LanguageUtils.getLanguageIdMap(
							cpDefinitionOptionValueRel.getNameMap());

						CPDefinitionOptionRel cpDefinitionOptionRel =
							cpDefinitionOptionValueRel.
								getCPDefinitionOptionRel();

						if (cpDefinitionOptionRel.isDateTime() &&
							(cpDefinitionOptionValueRel.getOptionValueDate() !=
								null)) {

							User user = dtoConverterContext.getUser();

							name = HashMapBuilder.put(
								user.getLanguageId(),
								() -> {
									Format dateFormat =
										FastDateFormatFactoryUtil.getDate(
											DateFormat.MEDIUM,
											dtoConverterContext.getLocale(),
											user.getTimeZone());

									Format timeFormat =
										FastDateFormatFactoryUtil.getTime(
											DateFormat.SHORT,
											dtoConverterContext.getLocale(),
											user.getTimeZone());

									String formattedDate = dateFormat.format(
										cpDefinitionOptionValueRel.
											getOptionValueDate());

									String formattedTime = timeFormat.format(
										cpDefinitionOptionValueRel.
											getOptionValueDate());

									int duration =
										cpDefinitionOptionValueRel.
											getDuration();

									if (duration < 0) {
										return formattedDate +
											StringPool.SPACE + formattedTime;
									}

									return StringBundler.concat(
										formattedDate, StringPool.SPACE,
										formattedTime, StringPool.SPACE,
										cpDefinitionOptionValueRel.
											getDuration(),
										StringPool.SPACE,
										cpDefinitionOptionValueRel.
											getDurationType());
								}
							).build();
						}

						return name;
					});
				setOptionValueDate(
					cpDefinitionOptionValueRel::getOptionValueDate);
				setPreselected(cpDefinitionOptionValueRel::isPreselected);
				setPriority(cpDefinitionOptionValueRel::getPriority);
				setQuantity(cpDefinitionOptionValueRel::getQuantity);
				setSkuId(
					() -> {
						CPInstance cpInstance =
							cpDefinitionOptionValueRel.fetchCPInstance();

						if (cpInstance == null) {
							return null;
						}

						return cpInstance.getCPInstanceId();
					});
				setUnitOfMeasureKey(
					cpDefinitionOptionValueRel::getUnitOfMeasureKey);
			}
		};
	}

}
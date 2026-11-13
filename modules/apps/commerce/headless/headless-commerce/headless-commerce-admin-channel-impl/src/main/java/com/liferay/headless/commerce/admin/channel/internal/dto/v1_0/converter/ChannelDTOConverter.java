/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.channel.internal.dto.v1_0.converter;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.configuration.CommerceOrderCheckoutConfiguration;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.commerce.util.CommerceChannelConfigurationUtil;
import com.liferay.headless.commerce.admin.channel.dto.v1_0.Channel;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = {
		"default=true",
		"dto.class.name=com.liferay.commerce.product.model.CommerceChannel"
	},
	service = DTOConverter.class
)
public class ChannelDTOConverter
	implements DTOConverter<CommerceChannel, Channel> {

	@Override
	public String getContentType() {
		return Channel.class.getSimpleName();
	}

	@Override
	public Channel toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelService.getCommerceChannel(
				(Long)dtoConverterContext.getId());

		CommerceCurrency commerceCurrency =
			_commerceCurrencyLocalService.getCommerceCurrency(
				commerceChannel.getCompanyId(),
				commerceChannel.getCommerceCurrencyCode());

		return new Channel() {
			{
				setAccountExternalReferenceCode(
					() -> {
						AccountEntry accountEntry =
							_accountEntryLocalService.fetchAccountEntry(
								commerceChannel.getAccountEntryId());

						if (accountEntry == null) {
							return null;
						}

						return accountEntry.getExternalReferenceCode();
					});
				setAccountId(commerceChannel::getAccountEntryId);
				setCurrencyCode(commerceCurrency::getCode);
				setCurrencyExternalReferenceCode(
					commerceCurrency::getExternalReferenceCode);
				setCurrencyId(commerceCurrency::getCommerceCurrencyId);
				setExternalReferenceCode(
					commerceChannel::getExternalReferenceCode);
				setGuestBrowsingEnabled(
					() -> {
						if (!FeatureFlagManagerUtil.isEnabled(
								commerceChannel.getCompanyId(), "LPD-109588")) {

							return null;
						}

						return CommerceChannelConfigurationUtil.
							isGuestBrowsingEnabled(
								commerceChannel.getGroupId());
					});
				setGuestCheckoutEnabled(
					() -> {
						CommerceOrderCheckoutConfiguration
							commerceOrderCheckoutConfiguration =
								_configurationProvider.getConfiguration(
									CommerceOrderCheckoutConfiguration.class,
									new GroupServiceSettingsLocator(
										commerceChannel.getGroupId(),
										CommerceConstants.
											SERVICE_NAME_COMMERCE_ORDER));

						return commerceOrderCheckoutConfiguration.
							guestCheckoutEnabled();
					});
				setId(commerceChannel::getCommerceChannelId);
				setName(commerceChannel::getName);
				setSiteGroupId(commerceChannel::getSiteGroupId);
				setType(commerceChannel::getType);
			}
		};
	}

	@Reference
	private AccountEntryLocalService _accountEntryLocalService;

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference
	private CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

}
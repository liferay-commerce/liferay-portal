/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.machine.learning.internal.recommendation.info.collection.provider;

import com.liferay.commerce.constants.CommerceWebKeys;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.machine.learning.internal.recommendation.configuration.CommerceMLRecommendationsCollectionProviderConfiguration;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.util.CommerceChannelConfigurationUtil;
import com.liferay.commerce.util.CommerceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Riccardo Ferrari
 */
public class BaseCommerceMLRecommendationCollectionProvider {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		commerceMLRecommendationsCollectionProviderConfiguration =
			ConfigurableUtil.createConfigurable(
				CommerceMLRecommendationsCollectionProviderConfiguration.class,
				properties);
	}

	protected CPDefinition getVisibleCPDefinition(CPDefinition cpDefinition)
		throws PortalException {

		if ((cpDefinition == null) ||
			!FeatureFlagManagerUtil.isEnabled(
				cpDefinition.getCompanyId(), "LPD-112683")) {

			return cpDefinition;
		}

		CommerceProductViewPermission commerceProductViewPermission =
			_commerceProductViewPermissionSnapshot.get();

		if (!commerceProductViewPermission.contains(
				PermissionThreadLocal.getPermissionChecker(),
				CommerceUtil.getCommerceAccountId(_getCommerceContext()),
				cpDefinition.getCPDefinitionId())) {

			return null;
		}

		return cpDefinition;
	}

	protected boolean isGuestBrowsingEnabled() throws PortalException {
		CommerceContext commerceContext = _getCommerceContext();

		if (commerceContext == null) {
			return true;
		}

		return CommerceChannelConfigurationUtil.isGuestBrowsingEnabled(
			PermissionThreadLocal.getPermissionChecker(),
			commerceContext.getCommerceChannelGroupId());
	}

	protected volatile CommerceMLRecommendationsCollectionProviderConfiguration
		commerceMLRecommendationsCollectionProviderConfiguration;

	private CommerceContext _getCommerceContext() {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			return null;
		}

		HttpServletRequest httpServletRequest = serviceContext.getRequest();

		if (httpServletRequest == null) {
			return null;
		}

		return (CommerceContext)httpServletRequest.getAttribute(
			CommerceWebKeys.COMMERCE_CONTEXT);
	}

	private static final Snapshot<CommerceProductViewPermission>
		_commerceProductViewPermissionSnapshot = new Snapshot<>(
			BaseCommerceMLRecommendationCollectionProvider.class,
			CommerceProductViewPermission.class);

}
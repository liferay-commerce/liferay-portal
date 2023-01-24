/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.currency.internal.util;

import com.liferay.commerce.currency.util.CommerceCurrencyContributor;
import com.liferay.commerce.currency.util.CommerceCurrencyContributorRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Crescenzo Rega
 */
@Component(service = CommerceCurrencyContributorRegistry.class)
public class CommerceCurrencyContributorRegistryImpl
	implements CommerceCurrencyContributorRegistry {

	@Override
	public CommerceCurrencyContributor getCommerceCurrencyContributor(
		String key) {

		if (Validator.isNull(key)) {
			return null;
		}

		ServiceTrackerCustomizerFactory.ServiceWrapper
			<CommerceCurrencyContributor>
				commerceCurrencyContributorServiceWrapper =
					_serviceTrackerMap.getService(key);

		if (commerceCurrencyContributorServiceWrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No commerce currency version contributor registered " +
						"with key " + key);
			}

			return null;
		}

		return commerceCurrencyContributorServiceWrapper.getService();
	}

	@Override
	public List<CommerceCurrencyContributor> getCommerceCurrencyContributors() {
		List<CommerceCurrencyContributor> commerceCurrencyContributors =
			new ArrayList<>();

		List
			<ServiceTrackerCustomizerFactory.ServiceWrapper
				<CommerceCurrencyContributor>>
					commerceCurrencyContributorServiceWrappers =
						ListUtil.fromCollection(_serviceTrackerMap.values());

		for (ServiceTrackerCustomizerFactory.ServiceWrapper
				<CommerceCurrencyContributor>
					commerceCurrencyContributorServiceWrapper :
						commerceCurrencyContributorServiceWrappers) {

			commerceCurrencyContributors.add(
				commerceCurrencyContributorServiceWrapper.getService());
		}

		return Collections.unmodifiableList(commerceCurrencyContributors);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, CommerceCurrencyContributor.class,
			"commerce.currency.contributor.key",
			ServiceTrackerCustomizerFactory.
				<CommerceCurrencyContributor>serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceCurrencyContributorRegistryImpl.class);

	private ServiceTrackerMap
		<String,
		 ServiceTrackerCustomizerFactory.ServiceWrapper
			 <CommerceCurrencyContributor>> _serviceTrackerMap;

}
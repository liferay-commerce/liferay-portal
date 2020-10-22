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

package com.liferay.commerce.service.impl;

import com.liferay.commerce.exception.CommerceRegionNameException;
import com.liferay.commerce.model.CommerceCountry;
import com.liferay.commerce.model.CommerceRegion;
import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.commerce.service.base.CommerceRegionLocalServiceBaseImpl;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Alessio Antonio Rendina
 * @author Andrea Di Giorgi
 * @author Marco Leo
 */
public class CommerceRegionLocalServiceImpl
	extends CommerceRegionLocalServiceBaseImpl {

	@Override
	public CommerceRegion addCommerceRegion(
			long commerceCountryId, Map<Locale, String> nameMap, String code,
			double priority, boolean active, ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(serviceContext.getUserId());

		CommerceCountry commerceCountry =
			commerceCountryPersistence.findByPrimaryKey(commerceCountryId);

		validate(nameMap);

		long commerceRegionId = counterLocalService.increment();

		CommerceRegion commerceRegion = commerceRegionPersistence.create(
			commerceRegionId);

		commerceRegion.setCompanyId(user.getCompanyId());
		commerceRegion.setUserId(user.getUserId());
		commerceRegion.setUserName(user.getFullName());
		commerceRegion.setCommerceCountryId(
			commerceCountry.getCommerceCountryId());
		commerceRegion.setCode(code);
		commerceRegion.setPriority(priority);
		commerceRegion.setActive(active);

		// add CommerceRegion localization

		_addCommerceRegionLocalizedFields(
			user.getCompanyId(), commerceRegionId, nameMap);

		return commerceRegionPersistence.update(commerceRegion);
	}

	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public CommerceRegion deleteCommerceRegion(CommerceRegion commerceRegion)
		throws PortalException {

		// Commerce region

		commerceRegionPersistence.remove(commerceRegion);

		// Commerce addresses

		commerceAddressLocalService.deleteRegionCommerceAddresses(
			commerceRegion.getCommerceRegionId());

		return commerceRegion;
	}

	@Override
	public CommerceRegion deleteCommerceRegion(long commerceRegionId)
		throws PortalException {

		CommerceRegion commerceRegion =
			commerceRegionPersistence.findByPrimaryKey(commerceRegionId);

		return commerceRegionLocalService.deleteCommerceRegion(commerceRegion);
	}

	@Override
	public void deleteCommerceRegions(long commerceCountryId)
		throws PortalException {

		List<CommerceRegion> commerceRegions =
			commerceRegionPersistence.findByCommerceCountryId(
				commerceCountryId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (CommerceRegion commerceRegion : commerceRegions) {
			deleteCommerceRegion(commerceRegion);
		}
	}

	@Override
	public CommerceRegion getCommerceRegion(long commerceCountryId, String code)
		throws PortalException {

		return commerceRegionPersistence.findByC_C(commerceCountryId, code);
	}

	@Override
	public List<CommerceRegion> getCommerceRegions(
		long commerceCountryId, boolean active) {

		return commerceRegionPersistence.findByC_A(commerceCountryId, active);
	}

	@Override
	public List<CommerceRegion> getCommerceRegions(
		long commerceCountryId, boolean active, int start, int end,
		OrderByComparator<CommerceRegion> orderByComparator) {

		return commerceRegionPersistence.findByC_A(
			commerceCountryId, active, start, end, orderByComparator);
	}

	@Override
	public List<CommerceRegion> getCommerceRegions(
		long commerceCountryId, int start, int end,
		OrderByComparator<CommerceRegion> orderByComparator) {

		return commerceRegionPersistence.findByCommerceCountryId(
			commerceCountryId, start, end, orderByComparator);
	}

	@Override
	public List<CommerceRegion> getCommerceRegions(
			long companyId, String countryTwoLettersISOCode, boolean active)
		throws PortalException {

		CommerceCountry commerceCountry =
			commerceCountryLocalService.getCommerceCountry(
				companyId, countryTwoLettersISOCode);

		return commerceRegionPersistence.findByC_A(
			commerceCountry.getCommerceCountryId(), active);
	}

	@Override
	public int getCommerceRegionsCount(long commerceCountryId) {
		return commerceRegionPersistence.countByCommerceCountryId(
			commerceCountryId);
	}

	@Override
	public int getCommerceRegionsCount(long commerceCountryId, boolean active) {
		return commerceRegionPersistence.countByC_A(commerceCountryId, active);
	}

	@Override
	public CommerceRegion setActive(long commerceRegionId, boolean active)
		throws PortalException {

		CommerceRegion commerceRegion =
			commerceRegionPersistence.findByPrimaryKey(commerceRegionId);

		commerceRegion.setActive(active);

		return commerceRegionPersistence.update(commerceRegion);
	}

	@Override
	public CommerceRegion updateCommerceRegion(
			long commerceRegionId, Map<Locale, String> nameMap, String code,
			double priority, boolean active, ServiceContext serviceContext)
		throws PortalException {

		CommerceRegion commerceRegion =
			commerceRegionPersistence.findByPrimaryKey(commerceRegionId);

		validate(nameMap);

		commerceRegion.setCode(code);
		commerceRegion.setPriority(priority);
		commerceRegion.setActive(active);

		// Update CommerceRegion localization

		_updateCommerceRegionLocalizedFields(
			commerceRegion.getCompanyId(), commerceRegionId, nameMap);

		return commerceRegionPersistence.update(commerceRegion);
	}

	protected void validate(Map<Locale, String> nameMap)
		throws PortalException {

		if (nameMap == null) {
			throw new CommerceRegionNameException();
		}
	}

	private List<CommerceRegionLocalization> _addCommerceRegionLocalizedFields(
		long companyId, long commerceRegionId, Map<Locale, String> nameMap) {

		List<CommerceRegionLocalization> commerceRegionLocalizations =
			new ArrayList<>();

		Set<Locale> localeSet = new HashSet<>();

		if (nameMap != null) {
			localeSet.addAll(nameMap.keySet());
		}

		for (Locale locale : localeSet) {
			String name = nameMap.get(locale);

			if (Validator.isNull(name)) {
				continue;
			}

			CommerceRegionLocalization commerceRegionLocalization =
				_addCommerceRegionLocalizedFields(
					companyId, commerceRegionId, name,
					LocaleUtil.toLanguageId(locale));

			commerceRegionLocalizations.add(commerceRegionLocalization);
		}

		return commerceRegionLocalizations;
	}

	private CommerceRegionLocalization _addCommerceRegionLocalizedFields(
		long companyId, long commerceRegionId, String name, String languageId) {

		CommerceRegionLocalization commerceRegionLocalization =
			commerceRegionLocalizationPersistence.
				fetchByCommerceRegionId_LanguageId(
					commerceRegionId, languageId);

		if (commerceRegionLocalization == null) {
			long commerceRegionLocalizationId = counterLocalService.increment();

			commerceRegionLocalization =
				commerceRegionLocalizationPersistence.create(
					commerceRegionLocalizationId);

			commerceRegionLocalization.setCompanyId(companyId);
			commerceRegionLocalization.setCommerceRegionId(commerceRegionId);
			commerceRegionLocalization.setLanguageId(languageId);
			commerceRegionLocalization.setName(name);
		}
		else {
			commerceRegionLocalization.setName(name);
		}

		return commerceRegionLocalizationPersistence.update(
			commerceRegionLocalization);
	}

	private List<CommerceRegionLocalization>
		_updateCommerceRegionLocalizedFields(
			long companyId, long commerceRegionId,
			Map<Locale, String> nameMap) {

		List<CommerceRegionLocalization> oldCommerceRegionLocalizations =
			new ArrayList<>(
				commerceRegionLocalizationPersistence.findByCommerceRegionId(
					commerceRegionId));

		List<CommerceRegionLocalization> newCommerceRegionLocalizations =
			_addCommerceRegionLocalizedFields(
				companyId, commerceRegionId, nameMap);

		oldCommerceRegionLocalizations.removeAll(
			newCommerceRegionLocalizations);

		for (CommerceRegionLocalization oldCommerceRegionLocalization :
				oldCommerceRegionLocalizations) {

			commerceRegionLocalizationPersistence.remove(
				oldCommerceRegionLocalization);
		}

		return newCommerceRegionLocalizations;
	}

}
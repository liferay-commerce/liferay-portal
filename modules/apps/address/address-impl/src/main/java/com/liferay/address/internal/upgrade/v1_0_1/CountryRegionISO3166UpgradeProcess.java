/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.address.internal.upgrade.v1_0_1;

import com.liferay.address.internal.util.CompanyCountriesUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.RegionLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Loc Pham
 */
public class CountryRegionISO3166UpgradeProcess extends UpgradeProcess {

	public CountryRegionISO3166UpgradeProcess(
		CompanyLocalService companyLocalService,
		CountryLocalService countryLocalService,
		RegionLocalService regionLocalService) {

		_companyLocalService = companyLocalService;
		_countryLocalService = countryLocalService;
		_regionLocalService = regionLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		String template = StringUtil.read(
			CompanyCountriesUtil.class.getClassLoader(),
			"com/liferay/address/dependencies/sql" +
				"/country-regions-iso-3166-upgrade.sql",
			false);

		runSQLTemplateString(template, false);

		// Re-update region follow the ISO-3611

		_companyLocalService.forEachCompany(
			company -> {
				try {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"Upgrade countries and region for company " +
								company.getCompanyId());
					}

					JSONArray countriesJSONArray =
						CompanyCountriesUtil.getJSONArray(
							"com/liferay/address/dependencies/countries.json");

					for (int i = 0; i < countriesJSONArray.length(); i++) {
						JSONObject countryJSONObject =
							countriesJSONArray.getJSONObject(i);

						_upgradeCountries(company.getCompanyId(), countryJSONObject);

					}
				}
				catch (Exception exception) {
					_log.error(
						"Unable to upgrade company " + company.getCompanyId(),
						exception);
				}
			});
	}

	private void _upgradeCountries(long companyId, JSONObject countryJSONObject) {

		try {
			Country country =
				_countryLocalService.getCountryByA2(
					companyId,
					countryJSONObject.getString("a2"));

			country = _countryLocalService.updateCountry(
				country.getCountryId(),
				countryJSONObject.getString("a2"),
				countryJSONObject.getString("a3"),
				country.isActive(), country.isBillingAllowed(),
				countryJSONObject.getString("idd"),
				countryJSONObject.getString("name"),
				countryJSONObject.getString("number"),
				country.getPosition(),
				country.isShippingAllowed(),
				country.isSubjectToVAT());

			_upgradeRegion(country);
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private void _upgradeCIWarehouse(
			String newRegionCode, String oldRegionCode, String a2)
		throws Exception {

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				SQLTransformer.transform(
					StringBundler.concat(
						"update CIWarehouse set commerceRegionCode = ?)",
						"where commerceRegionCode = ? and ",
						"countryTwoLettersISOCode = ?")))) {

			preparedStatement.setString(1, newRegionCode);
			preparedStatement.setString(2, oldRegionCode);
			preparedStatement.setString(3, a2);
			preparedStatement.executeUpdate();
		}
	}

	private void _upgradeRegion(Country country) {
		try {
			String path =
				"com/liferay/address/dependencies/regions/" + country.getA2() +
					".json";

			ClassLoader classLoader =
				CompanyCountriesUtil.class.getClassLoader();

			if (classLoader.getResource(path) == null) {
				return;
			}

			JSONArray regionsJSONArray = CompanyCountriesUtil.getJSONArray(
				path);

			if (_log.isDebugEnabled()) {
				_log.debug("Regions found for country " + country.getA2());
			}

			for (int j = 0; j < regionsJSONArray.length(); j++) {
				try {
					JSONObject regionJSONObject =
						regionsJSONArray.getJSONObject(j);

					ServiceContext serviceContext = new ServiceContext();

					serviceContext.setCompanyId(country.getCompanyId());
					serviceContext.setUserId(country.getUserId());

					String newRegionCode = regionJSONObject.getString(
						"regionCode");

					Region region = _regionLocalService.fetchRegion(
						country.getCountryId(), newRegionCode);

					if ((region == null) && Validator.isNumber(newRegionCode)) {
						region = _regionLocalService.fetchRegion(
							country.getCountryId(),
							StringUtil.removeLast(newRegionCode, ".0"));
					}

					if (region == null) {
						region = _regionLocalService.addRegion(
							country.getCountryId(), true,
							regionJSONObject.getString("name"), 0,
							regionJSONObject.getString("regionCode"),
							serviceContext);
					}
					else {
						String oldRegionCode = region.getRegionCode();

						region.setName(regionJSONObject.getString("name"));
						region.setRegionCode(newRegionCode);

						region = _regionLocalService.updateRegion(region);

						_upgradeCIWarehouse(
							region.getRegionCode(), oldRegionCode,
							country.getA2());
					}

					JSONObject localizationsJSONObject =
						regionJSONObject.getJSONObject("localizations");

					if (localizationsJSONObject == null) {
						Map<String, String> titleMap = new HashMap<>();

						for (Locale locale :
								LanguageUtil.getCompanyAvailableLocales(
									country.getCompanyId())) {

							titleMap.put(
								LanguageUtil.getLanguageId(locale),
								region.getName());
						}

						_regionLocalService.updateRegionLocalizations(
							region, titleMap);
					}
					else {
						for (String key : localizationsJSONObject.keySet()) {
							_regionLocalService.updateRegionLocalization(
								region, key,
								localizationsJSONObject.getString(key));
						}
					}
				}
				catch (PortalException portalException) {
					_log.error(portalException);
				}
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No regions found for country " + country.getA2(),
					exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CountryRegionISO3166UpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final CountryLocalService _countryLocalService;
	private final RegionLocalService _regionLocalService;

}
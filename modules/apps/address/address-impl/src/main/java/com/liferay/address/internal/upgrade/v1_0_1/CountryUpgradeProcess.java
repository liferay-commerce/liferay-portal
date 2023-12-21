/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.address.internal.upgrade.v1_0_1;

import com.liferay.address.internal.util.CompanyCountriesUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.RegionLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Map;

/**
 * @author Stefano Motta
 */
public class CountryUpgradeProcess extends UpgradeProcess {

	public CountryUpgradeProcess(
		CompanyLocalService companyLocalService,
		CountryLocalService countryLocalService, JSONFactory jsonFactory,
		RegionLocalService regionLocalService) {

		_companyLocalService = companyLocalService;
		_countryLocalService = countryLocalService;
		_jsonFactory = jsonFactory;
		_regionLocalService = regionLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompany(
			company -> {
				try {
					_addCountry(
						company,
						HashMapBuilder.<String, Object>put(
							"a2", "HK"
						).put(
							"a3", "HKG"
						).put(
							"idd", 852
						).put(
							"name", "hong-kong"
						).put(
							"number", 344
						).put(
							"zipRequired", false
						).build());
					_addCountry(
						company,
						HashMapBuilder.<String, Object>put(
							"a2", "MO"
						).put(
							"a3", "MAC"
						).put(
							"idd", 853
						).put(
							"name", "macau"
						).put(
							"number", 446
						).put(
							"zipRequired", true
						).build());
				}
				catch (Exception exception) {
					_log.error(
						"Unable to populate company " + company.getCompanyId(),
						exception);
				}
			});
	}

	private void _addCountry(Company company, Map<String, Object> countryMap)
		throws PortalException, SQLException {

		Country country = _countryLocalService.fetchCountryByName(
			company.getCompanyId(),
			countryMap.get(
				"name"
			).toString());

		if (country != null) {
			return;
		}

		CompanyCountriesUtil.addCountry(
			company, _jsonFactory.createJSONObject(countryMap),
			_countryLocalService, _regionLocalService);

		Country chinaCountry = _countryLocalService.fetchCountryByA2(
			company.getCompanyId(), "CN");

		if (chinaCountry == null) {
			return;
		}

		Region region = _regionLocalService.fetchRegion(
			chinaCountry.getCountryId(),
			countryMap.get(
				"a2"
			).toString());

		if (region == null) {
			return;
		}

		country = _countryLocalService.fetchCountryByName(
			company.getCompanyId(),
			countryMap.get(
				"name"
			).toString());

		_updateData(
			country.getCountryId(), chinaCountry.getCountryId(),
			region.getRegionId(), "address");
		_updateData(
			country.getA2(), chinaCountry.getA2(), region.getRegionCode(),
			"ciwarehouse");
		_updateData(
			country.getCountryId(), chinaCountry.getCountryId(),
			region.getRegionId(), "commercetaxfixedrateaddressrel");
		_updateData(
			country.getCountryId(), chinaCountry.getCountryId(),
			region.getRegionId(), "cshippingfixedoptionrel");
		_updateData(
			country.getCountryId(), chinaCountry.getCountryId(),
			region.getRegionId(), "organization_");

		_regionLocalService.deleteRegion(region.getRegionId());
	}

	private void _updateData(
			long countryId, long oldCountryId, long oldRegionId,
			String tableName)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			StringBundler.concat(
				"update ", tableName,
				" set countryid = ?, regionid = 0 where countryid = ? and ",
				"regionid = ?"));

		preparedStatement.setLong(1, countryId);
		preparedStatement.setLong(2, oldCountryId);
		preparedStatement.setLong(3, oldRegionId);

		preparedStatement.execute();
	}

	private void _updateData(
			String countryA2, String oldCountryA2, String oldRegionCode,
			String tableName)
		throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(
			StringBundler.concat(
				"update ", tableName, " set countrytwolettersisocode = ?, ",
				"commerceregioncode = null where countrytwolettersisocode = ? ",
				"and commerceregioncode = ?"));

		preparedStatement.setString(1, countryA2);
		preparedStatement.setString(2, oldCountryA2);
		preparedStatement.setString(3, oldRegionCode);

		preparedStatement.execute();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CountryUpgradeProcess.class);

	private final CompanyLocalService _companyLocalService;
	private final CountryLocalService _countryLocalService;
	private final JSONFactory _jsonFactory;
	private final RegionLocalService _regionLocalService;

}
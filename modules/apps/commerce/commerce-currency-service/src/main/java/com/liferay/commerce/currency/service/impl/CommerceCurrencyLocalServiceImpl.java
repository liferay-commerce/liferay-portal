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

package com.liferay.commerce.currency.service.impl;

import com.liferay.commerce.currency.configuration.CommerceCurrencyConfiguration;
import com.liferay.commerce.currency.configuration.RoundingTypeConfiguration;
import com.liferay.commerce.currency.constants.CommerceCurrencyConstants;
import com.liferay.commerce.currency.constants.CommerceCurrencyExchangeRateConstants;
import com.liferay.commerce.currency.constants.RoundingTypeConstants;
import com.liferay.commerce.currency.exception.CommerceCurrencyCodeException;
import com.liferay.commerce.currency.exception.CommerceCurrencyNameException;
import com.liferay.commerce.currency.exception.NoSuchCurrencyException;
import com.liferay.commerce.currency.internal.model.listener.PortalInstanceLifecycleListenerImpl;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.base.CommerceCurrencyLocalServiceBaseImpl;
import com.liferay.commerce.currency.util.ExchangeRateProvider;
import com.liferay.commerce.currency.util.ExchangeRateProviderRegistry;
import com.liferay.commerce.currency.util.comparator.CommerceCurrencyPriorityComparator;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Di Giorgi
 * @author Marco Leo
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "model.class.name=com.liferay.commerce.currency.model.CommerceCurrency",
	service = AopService.class
)
public class CommerceCurrencyLocalServiceImpl
	extends CommerceCurrencyLocalServiceBaseImpl {

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public CommerceCurrency addCommerceCurrency(
			long userId, String code, Map<Locale, String> nameMap,
			String symbol, BigDecimal rate,
			Map<Locale, String> formatPatternMap, int maxFractionDigits,
			int minFractionDigits, String roundingMode, boolean primary,
			double priority, boolean active)
		throws PortalException {

		User user = _userLocalService.getUser(userId);

		if (primary) {
			rate = BigDecimal.ONE;
		}

		_validate(0, user.getCompanyId(), code, nameMap, primary);

		if (formatPatternMap.isEmpty()) {
			formatPatternMap.put(
				user.getLocale(),
				CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN);
		}

		if (Validator.isNull(roundingMode)) {
			RoundingTypeConfiguration roundingTypeConfiguration =
				_configurationProvider.getConfiguration(
					RoundingTypeConfiguration.class,
					new SystemSettingsLocator(
						RoundingTypeConstants.SERVICE_NAME));

			RoundingMode roundingModeEnum =
				roundingTypeConfiguration.roundingMode();

			roundingMode = roundingModeEnum.name();
		}

		long commerceCurrencyId = counterLocalService.increment();

		CommerceCurrency commerceCurrency = commerceCurrencyPersistence.create(
			commerceCurrencyId);

		commerceCurrency.setCompanyId(user.getCompanyId());
		commerceCurrency.setUserId(user.getUserId());
		commerceCurrency.setUserName(user.getFullName());
		commerceCurrency.setCode(code);
		commerceCurrency.setNameMap(nameMap);
		commerceCurrency.setSymbol(symbol);
		commerceCurrency.setRate(rate);
		commerceCurrency.setFormatPatternMap(formatPatternMap);
		commerceCurrency.setMaxFractionDigits(maxFractionDigits);
		commerceCurrency.setMinFractionDigits(minFractionDigits);
		commerceCurrency.setRoundingMode(roundingMode);
		commerceCurrency.setPrimary(primary);
		commerceCurrency.setPriority(priority);
		commerceCurrency.setActive(active);

		return commerceCurrencyPersistence.update(commerceCurrency);
	}

	@Override
	public void deleteCommerceCurrencies(long companyId) {
		commerceCurrencyPersistence.removeByCompanyId(companyId);
	}

	@Indexable(type = IndexableType.DELETE)
	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public CommerceCurrency deleteCommerceCurrency(
		CommerceCurrency commerceCurrency) {

		return commerceCurrencyPersistence.remove(commerceCurrency);
	}

	@Override
	public CommerceCurrency deleteCommerceCurrency(long commerceCurrencyId)
		throws PortalException {

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		return commerceCurrencyLocalService.deleteCommerceCurrency(
			commerceCurrency);
	}

	@Override
	public CommerceCurrency fetchPrimaryCommerceCurrency(long companyId) {
		return commerceCurrencyPersistence.fetchByC_P_A_First(
			companyId, true, true, new CommerceCurrencyPriorityComparator());
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, boolean active) {

		return commerceCurrencyPersistence.findByC_A(companyId, active);
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, boolean active, int start, int end,
		OrderByComparator<CommerceCurrency> orderByComparator) {

		return commerceCurrencyPersistence.findByC_A(
			companyId, active, start, end, orderByComparator);
	}

	@Override
	public List<CommerceCurrency> getCommerceCurrencies(
		long companyId, int start, int end,
		OrderByComparator<CommerceCurrency> orderByComparator) {

		return commerceCurrencyPersistence.findByCompanyId(
			companyId, start, end, orderByComparator);
	}

	@Override
	public int getCommerceCurrenciesCount(long companyId) {
		return commerceCurrencyPersistence.countByCompanyId(companyId);
	}

	@Override
	public int getCommerceCurrenciesCount(long companyId, boolean active) {
		return commerceCurrencyPersistence.countByC_A(companyId, active);
	}

	@Override
	public CommerceCurrency getCommerceCurrency(long companyId, String code)
		throws NoSuchCurrencyException {

		return commerceCurrencyPersistence.findByC_C(companyId, code);
	}

	@Override
	public void importDefaultValues(
			boolean updateExchangeRate, ServiceContext serviceContext)
		throws Exception {

		Class<?> clazz = getClass();

		String currenciesPath =
			"com/liferay/commerce/currency/service/impl/dependencies" +
				"/currencies.json";

		String countriesJSON = StringUtil.read(
			clazz.getClassLoader(), currenciesPath, false);

		JSONArray jsonArray = _jsonFactory.createJSONArray(countriesJSON);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String code = jsonObject.getString("code");

			CommerceCurrency commerceCurrency =
				commerceCurrencyPersistence.fetchByC_C(
					serviceContext.getCompanyId(), code);

			if (commerceCurrency == null) {
				boolean primary = jsonObject.getBoolean("primary");
				double priority = jsonObject.getDouble("priority");
				double rate = jsonObject.getDouble("rate");
				String symbol = jsonObject.getString("symbol");

				RoundingTypeConfiguration roundingTypeConfiguration =
					_configurationProvider.getConfiguration(
						RoundingTypeConfiguration.class,
						new SystemSettingsLocator(
							RoundingTypeConstants.SERVICE_NAME));

				Map<Locale, String> nameMap = HashMapBuilder.put(
					serviceContext.getLocale(), jsonObject.getString("name")
				).build();

				Map<Locale, String> formatPatternMap = HashMapBuilder.put(
					serviceContext.getLocale(),
					StringBundler.concat(
						symbol, StringPool.SPACE,
						CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN)
				).build();

				RoundingMode roundingMode =
					roundingTypeConfiguration.roundingMode();

				commerceCurrencyLocalService.addCommerceCurrency(
					serviceContext.getUserId(), code, nameMap, symbol,
					BigDecimal.valueOf(rate), formatPatternMap,
					roundingTypeConfiguration.maximumFractionDigits(),
					roundingTypeConfiguration.minimumFractionDigits(),
					roundingMode.name(), primary, priority, true);
			}
		}

		if (updateExchangeRate) {
			for (String exchangeRateProviderKey :
					_exchangeRateProviderRegistry.
						getExchangeRateProviderKeys()) {

				_updateExchangeRates(
					serviceContext.getCompanyId(), exchangeRateProviderKey);

				break;
			}
		}
	}

	@Override
	public BaseModelSearchResult<CommerceCurrency> searchCommerceCurrencies(
			long companyId, String keywords, boolean navigationActive,
			int start, int end, Sort sort)
		throws PortalException {

		SearchContext searchContext = _buildSearchContext(
			companyId, keywords, start, end, sort);

		return _searchCommerceCurrencies(searchContext, navigationActive);
	}

	@Override
	public BaseModelSearchResult<CommerceCurrency> searchCommerceCurrencies(
			long companyId, String keywords, int start, int end, Sort sort)
		throws PortalException {

		SearchContext searchContext = _buildSearchContext(
			companyId, keywords, start, end, sort);

		return _searchCommerceCurrencies(searchContext);
	}

	@Override
	public CommerceCurrency setActive(long commerceCurrencyId, boolean active)
		throws PortalException {

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		commerceCurrency.setActive(active);

		return commerceCurrencyPersistence.update(commerceCurrency);
	}

	@Override
	public void setAopProxy(Object aopProxy) {
		super.setAopProxy(aopProxy);

		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			PortalInstanceLifecycleListener.class,
			new PortalInstanceLifecycleListenerImpl(
				commerceCurrencyLocalService),
			null);
	}

	@Override
	public CommerceCurrency setPrimary(long commerceCurrencyId, boolean primary)
		throws PortalException {

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		_validate(
			commerceCurrencyId, commerceCurrency.getCompanyId(),
			commerceCurrency.getCode(), commerceCurrency.getNameMap(), primary);

		commerceCurrency.setPrimary(primary);

		return commerceCurrencyPersistence.update(commerceCurrency);
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public CommerceCurrency updateCommerceCurrency(
			long commerceCurrencyId, Map<Locale, String> nameMap, String symbol,
			BigDecimal rate, Map<Locale, String> formatPatternMap,
			int maxFractionDigits, int minFractionDigits, String roundingMode,
			boolean primary, double priority, boolean active,
			ServiceContext serviceContext)
		throws PortalException {

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		if (primary) {
			rate = BigDecimal.ONE;
		}

		_validate(
			commerceCurrency.getCommerceCurrencyId(),
			serviceContext.getCompanyId(), commerceCurrency.getCode(), nameMap,
			primary);

		if (formatPatternMap.isEmpty()) {
			formatPatternMap.put(
				serviceContext.getLocale(),
				CommerceCurrencyConstants.DECIMAL_FORMAT_PATTERN);
		}

		if (Validator.isNull(roundingMode)) {
			RoundingTypeConfiguration roundingTypeConfiguration =
				_configurationProvider.getConfiguration(
					RoundingTypeConfiguration.class,
					new SystemSettingsLocator(
						RoundingTypeConstants.SERVICE_NAME));

			RoundingMode roundingModeEnum =
				roundingTypeConfiguration.roundingMode();

			roundingMode = roundingModeEnum.name();
		}

		commerceCurrency.setNameMap(nameMap);
		commerceCurrency.setSymbol(symbol);
		commerceCurrency.setRate(rate);
		commerceCurrency.setFormatPatternMap(formatPatternMap);
		commerceCurrency.setMaxFractionDigits(maxFractionDigits);
		commerceCurrency.setMinFractionDigits(minFractionDigits);
		commerceCurrency.setRoundingMode(roundingMode);
		commerceCurrency.setPrimary(primary);
		commerceCurrency.setPriority(priority);
		commerceCurrency.setActive(active);

		return commerceCurrencyPersistence.update(commerceCurrency);
	}

	@Override
	public CommerceCurrency updateCommerceCurrencyRate(
			long commerceCurrencyId, BigDecimal rate)
		throws PortalException {

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		commerceCurrency.setRate(rate);

		return commerceCurrencyPersistence.update(commerceCurrency);
	}

	@Override
	public void updateExchangeRate(
			long commerceCurrencyId, String exchangeRateProviderKey)
		throws PortalException {

		ExchangeRateProvider exchangeRateProvider =
			_exchangeRateProviderRegistry.getExchangeRateProvider(
				exchangeRateProviderKey);

		if (exchangeRateProvider == null) {
			return;
		}

		CommerceCurrency commerceCurrency =
			commerceCurrencyPersistence.findByPrimaryKey(commerceCurrencyId);

		CommerceCurrency primaryCommerceCurrency =
			commerceCurrencyLocalService.fetchPrimaryCommerceCurrency(
				commerceCurrency.getCompanyId());

		if (primaryCommerceCurrency == null) {
			return;
		}

		BigDecimal exchangeRate = BigDecimal.ZERO;

		try {
			exchangeRate = exchangeRateProvider.getExchangeRate(
				primaryCommerceCurrency, commerceCurrency);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return;
		}

		commerceCurrency.setRate(exchangeRate);

		commerceCurrencyLocalService.updateCommerceCurrency(commerceCurrency);
	}

	@Override
	public void updateExchangeRates() throws PortalException {
		_companyLocalService.forEachCompanyId(
			companyId -> {
				CommerceCurrencyConfiguration commerceCurrencyConfiguration =
					_configurationProvider.getConfiguration(
						CommerceCurrencyConfiguration.class,
						new CompanyServiceSettingsLocator(
							companyId,
							CommerceCurrencyExchangeRateConstants.
								SERVICE_NAME));

				if (commerceCurrencyConfiguration.enableAutoUpdate()) {
					String defaultExchangeRateProviderKey =
						commerceCurrencyConfiguration.
							defaultExchangeRateProviderKey();

					_updateExchangeRates(
						companyId, defaultExchangeRateProviderKey);
				}
			},
			ArrayUtil.toLongArray(commerceCurrencyFinder.getCompanyIds()));
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();

		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	private SearchContext _buildSearchContext(
		long companyId, String keywords, int start, int end, Sort sort) {

		SearchContext searchContext = new SearchContext();

		searchContext.setAttributes(
			HashMapBuilder.<String, Serializable>put(
				Field.CODE, keywords
			).put(
				Field.ENTRY_CLASS_PK, keywords
			).put(
				Field.NAME, keywords
			).put(
				"params",
				LinkedHashMapBuilder.<String, Object>put(
					"keywords", keywords
				).build()
			).build());
		searchContext.setCompanyId(companyId);
		searchContext.setEnd(end);

		if (Validator.isNotNull(keywords)) {
			searchContext.setKeywords(keywords);
		}

		if (sort != null) {
			searchContext.setSorts(sort);
		}

		searchContext.setStart(start);

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		return searchContext;
	}

	private List<CommerceCurrency> _getCommerceCurrencies(Hits hits)
		throws PortalException {

		List<Document> documents = hits.toList();

		List<CommerceCurrency> commerceCurrencies = new ArrayList<>(
			documents.size());

		for (Document document : documents) {
			long commerceCurrencyId = GetterUtil.getLong(
				document.get(Field.ENTRY_CLASS_PK));

			CommerceCurrency commerceCurrency = fetchCommerceCurrency(
				commerceCurrencyId);

			if (commerceCurrency == null) {
				Indexer<CommerceCurrency> indexer =
					IndexerRegistryUtil.getIndexer(CommerceCurrency.class);

				long companyId = GetterUtil.getLong(
					document.get(Field.COMPANY_ID));

				indexer.delete(companyId, document.getUID());
			}
			else if (commerceCurrency != null) {
				commerceCurrencies.add(commerceCurrency);
			}
		}

		return commerceCurrencies;
	}

	private List<CommerceCurrency> _getCommerceCurrencies(
			Hits hits, boolean navigationActive)
		throws PortalException {

		List<Document> documents = hits.toList();

		List<CommerceCurrency> commerceCurrencies = new ArrayList<>(
			documents.size());

		for (Document document : documents) {
			long commerceCurrencyId = GetterUtil.getLong(
				document.get(Field.ENTRY_CLASS_PK));

			CommerceCurrency commerceCurrency = fetchCommerceCurrency(
				commerceCurrencyId);

			if (commerceCurrency == null) {
				Indexer<CommerceCurrency> indexer =
					IndexerRegistryUtil.getIndexer(CommerceCurrency.class);

				long companyId = GetterUtil.getLong(
					document.get(Field.COMPANY_ID));

				indexer.delete(companyId, document.getUID());
			}
			else if ((commerceCurrency != null) &&
					 (commerceCurrency.isActive() == navigationActive)) {

				commerceCurrencies.add(commerceCurrency);
			}
		}

		return commerceCurrencies;
	}

	private BaseModelSearchResult<CommerceCurrency> _searchCommerceCurrencies(
			SearchContext searchContext)
		throws PortalException {

		Indexer<CommerceCurrency> indexer =
			IndexerRegistryUtil.nullSafeGetIndexer(CommerceCurrency.class);

		for (int i = 0; i < 10; i++) {
			Hits hits = indexer.search(searchContext, _SELECTED_FIELD_NAMES);

			return new BaseModelSearchResult<>(
				_getCommerceCurrencies(hits), hits.getLength());
		}

		throw new SearchException(
			"Unable to fix the search index after 10 attempts");
	}

	private BaseModelSearchResult<CommerceCurrency> _searchCommerceCurrencies(
			SearchContext searchContext, boolean navigationActive)
		throws PortalException {

		Indexer<CommerceCurrency> indexer =
			IndexerRegistryUtil.nullSafeGetIndexer(CommerceCurrency.class);

		for (int i = 0; i < 10; i++) {
			Hits hits = indexer.search(searchContext, _SELECTED_FIELD_NAMES);

			return new BaseModelSearchResult<>(
				_getCommerceCurrencies(hits, navigationActive),
				hits.getLength());
		}

		throw new SearchException(
			"Unable to fix the search index after 10 attempts");
	}

	private void _updateExchangeRates(
			long companyId, String exchangeRateProviderKey)
		throws PortalException {

		List<CommerceCurrency> commerceCurrencies =
			commerceCurrencyLocalService.getCommerceCurrencies(companyId, true);

		for (CommerceCurrency commerceCurrency : commerceCurrencies) {
			commerceCurrencyLocalService.updateExchangeRate(
				commerceCurrency.getCommerceCurrencyId(),
				exchangeRateProviderKey);
		}
	}

	private void _validate(
			long commerceCurrencyId, long companyId, String code,
			Map<Locale, String> nameMap, boolean primary)
		throws PortalException {

		if (Validator.isNull(code)) {
			throw new CommerceCurrencyCodeException();
		}

		String name = nameMap.get(LocaleUtil.getSiteDefault());

		if (Validator.isNull(name)) {
			throw new CommerceCurrencyNameException();
		}

		if (primary) {
			List<CommerceCurrency> commerceCurrencies =
				commerceCurrencyPersistence.findByC_P(companyId, primary);

			for (CommerceCurrency commerceCurrency : commerceCurrencies) {
				if (commerceCurrency.getCommerceCurrencyId() !=
						commerceCurrencyId) {

					commerceCurrency.setPrimary(false);

					commerceCurrencyPersistence.update(commerceCurrency);
				}
			}
		}
	}

	private static final String[] _SELECTED_FIELD_NAMES = {
		Field.ENTRY_CLASS_PK, Field.COMPANY_ID, Field.UID
	};

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceCurrencyLocalServiceImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private ExchangeRateProviderRegistry _exchangeRateProviderRegistry;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceRegistration<?> _serviceRegistration;

	@Reference
	private UserLocalService _userLocalService;

}
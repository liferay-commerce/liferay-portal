/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.definitions.web.internal.model;

/**
 * @author Alessio Antonio Rendina
 */
public class ProductOptionValue {

	public ProductOptionValue(
		long cpDefinitionOptionValueRelId, String deltaPrice, int duration,
		String durationType, String key, String name, String optionValueDate,
		double position, String preselected, String sku) {

		_cpDefinitionOptionValueRelId = cpDefinitionOptionValueRelId;
		_deltaPrice = deltaPrice;
		_duration = duration;
		_durationType = durationType;
		_key = key;
		_name = name;
		_optionValueDate = optionValueDate;
		_position = position;
		_preselected = preselected;
		_sku = sku;
	}

	public long getCPDefinitionOptionValueRelId() {
		return _cpDefinitionOptionValueRelId;
	}

	public String getDeltaPrice() {
		return _deltaPrice;
	}

	public int getDuration() {
		return _duration;
	}

	public String getDurationType() {
		return _durationType;
	}

	public String getKey() {
		return _key;
	}

	public String getName() {
		return _name;
	}

	public String getOptionValueDate() {
		return _optionValueDate;
	}

	public double getPosition() {
		return _position;
	}

	public String getPreselected() {
		return _preselected;
	}

	public String getSku() {
		return _sku;
	}

	private final long _cpDefinitionOptionValueRelId;
	private final String _deltaPrice;
	private final int _duration;
	private final String _durationType;
	private final String _key;
	private final String _name;
	private final String _optionValueDate;
	private final double _position;
	private final String _preselected;
	private final String _sku;

}
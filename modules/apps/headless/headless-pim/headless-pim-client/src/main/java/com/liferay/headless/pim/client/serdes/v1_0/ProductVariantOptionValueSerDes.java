/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.client.serdes.v1_0;

import com.liferay.headless.pim.client.dto.v1_0.ProductVariantOptionValue;
import com.liferay.headless.pim.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Stefano Motta
 * @generated
 */
@Generated("")
public class ProductVariantOptionValueSerDes {

	public static ProductVariantOptionValue toDTO(String json) {
		ProductVariantOptionValueJSONParser
			productVariantOptionValueJSONParser =
				new ProductVariantOptionValueJSONParser();

		return productVariantOptionValueJSONParser.parseToDTO(json);
	}

	public static ProductVariantOptionValue[] toDTOs(String json) {
		ProductVariantOptionValueJSONParser
			productVariantOptionValueJSONParser =
				new ProductVariantOptionValueJSONParser();

		return productVariantOptionValueJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		ProductVariantOptionValue productVariantOptionValue) {

		if (productVariantOptionValue == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (productVariantOptionValue.getKey() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"key\": ");

			sb.append("\"");

			sb.append(_escape(productVariantOptionValue.getKey()));

			sb.append("\"");
		}

		if (productVariantOptionValue.getLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"label\": ");

			sb.append("\"");

			sb.append(_escape(productVariantOptionValue.getLabel()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ProductVariantOptionValueJSONParser
			productVariantOptionValueJSONParser =
				new ProductVariantOptionValueJSONParser();

		return productVariantOptionValueJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		ProductVariantOptionValue productVariantOptionValue) {

		if (productVariantOptionValue == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (productVariantOptionValue.getKey() == null) {
			map.put("key", null);
		}
		else {
			map.put("key", String.valueOf(productVariantOptionValue.getKey()));
		}

		if (productVariantOptionValue.getLabel() == null) {
			map.put("label", null);
		}
		else {
			map.put(
				"label", String.valueOf(productVariantOptionValue.getLabel()));
		}

		return map;
	}

	public static class ProductVariantOptionValueJSONParser
		extends BaseJSONParser<ProductVariantOptionValue> {

		@Override
		protected ProductVariantOptionValue createDTO() {
			return new ProductVariantOptionValue();
		}

		@Override
		protected ProductVariantOptionValue[] createDTOArray(int size) {
			return new ProductVariantOptionValue[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "key")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "label")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ProductVariantOptionValue productVariantOptionValue,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "key")) {
				if (jsonParserFieldValue != null) {
					productVariantOptionValue.setKey(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "label")) {
				if (jsonParserFieldValue != null) {
					productVariantOptionValue.setLabel(
						(String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>)value;

			return _toJSON(collection.toArray());
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:654174493
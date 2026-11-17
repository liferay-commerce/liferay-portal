/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.client.serdes.v1_0;

import com.liferay.headless.pim.client.dto.v1_0.ProductVariantOption;
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
public class ProductVariantOptionSerDes {

	public static ProductVariantOption toDTO(String json) {
		ProductVariantOptionJSONParser productVariantOptionJSONParser =
			new ProductVariantOptionJSONParser();

		return productVariantOptionJSONParser.parseToDTO(json);
	}

	public static ProductVariantOption[] toDTOs(String json) {
		ProductVariantOptionJSONParser productVariantOptionJSONParser =
			new ProductVariantOptionJSONParser();

		return productVariantOptionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ProductVariantOption productVariantOption) {
		if (productVariantOption == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (productVariantOption.getLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"label\": ");

			sb.append("\"");

			sb.append(_escape(productVariantOption.getLabel()));

			sb.append("\"");
		}

		if (productVariantOption.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(productVariantOption.getName()));

			sb.append("\"");
		}

		if (productVariantOption.getProductVariantOptionValues() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"productVariantOptionValues\": ");

			sb.append("[");

			for (int i = 0;
				 i <
					 productVariantOption.
						 getProductVariantOptionValues().length;
				 i++) {

				sb.append(
					String.valueOf(
						productVariantOption.getProductVariantOptionValues()
							[i]));

				if ((i + 1) < productVariantOption.
						getProductVariantOptionValues().length) {

					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (productVariantOption.getValue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"value\": ");

			sb.append("\"");

			sb.append(_escape(productVariantOption.getValue()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ProductVariantOptionJSONParser productVariantOptionJSONParser =
			new ProductVariantOptionJSONParser();

		return productVariantOptionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		ProductVariantOption productVariantOption) {

		if (productVariantOption == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (productVariantOption.getLabel() == null) {
			map.put("label", null);
		}
		else {
			map.put("label", String.valueOf(productVariantOption.getLabel()));
		}

		if (productVariantOption.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(productVariantOption.getName()));
		}

		if (productVariantOption.getProductVariantOptionValues() == null) {
			map.put("productVariantOptionValues", null);
		}
		else {
			map.put(
				"productVariantOptionValues",
				String.valueOf(
					productVariantOption.getProductVariantOptionValues()));
		}

		if (productVariantOption.getValue() == null) {
			map.put("value", null);
		}
		else {
			map.put("value", String.valueOf(productVariantOption.getValue()));
		}

		return map;
	}

	public static class ProductVariantOptionJSONParser
		extends BaseJSONParser<ProductVariantOption> {

		@Override
		protected ProductVariantOption createDTO() {
			return new ProductVariantOption();
		}

		@Override
		protected ProductVariantOption[] createDTOArray(int size) {
			return new ProductVariantOption[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "label")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "productVariantOptionValues")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "value")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ProductVariantOption productVariantOption,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "label")) {
				if (jsonParserFieldValue != null) {
					productVariantOption.setLabel((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					productVariantOption.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "productVariantOptionValues")) {

				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					ProductVariantOptionValue[]
						productVariantOptionValuesArray =
							new ProductVariantOptionValue
								[jsonParserFieldValues.length];

					for (int i = 0; i < productVariantOptionValuesArray.length;
						 i++) {

						productVariantOptionValuesArray[i] =
							ProductVariantOptionValueSerDes.toDTO(
								(String)jsonParserFieldValues[i]);
					}

					productVariantOption.setProductVariantOptionValues(
						productVariantOptionValuesArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "value")) {
				if (jsonParserFieldValue != null) {
					productVariantOption.setValue((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:-2088360333
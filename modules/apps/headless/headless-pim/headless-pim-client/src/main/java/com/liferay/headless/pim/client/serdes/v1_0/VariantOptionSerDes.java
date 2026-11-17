/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.client.serdes.v1_0;

import com.liferay.headless.pim.client.dto.v1_0.VariantOption;
import com.liferay.headless.pim.client.dto.v1_0.VariantOptionValue;
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
public class VariantOptionSerDes {

	public static VariantOption toDTO(String json) {
		VariantOptionJSONParser variantOptionJSONParser =
			new VariantOptionJSONParser();

		return variantOptionJSONParser.parseToDTO(json);
	}

	public static VariantOption[] toDTOs(String json) {
		VariantOptionJSONParser variantOptionJSONParser =
			new VariantOptionJSONParser();

		return variantOptionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(VariantOption variantOption) {
		if (variantOption == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (variantOption.getLabel() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"label\": ");

			sb.append("\"");

			sb.append(_escape(variantOption.getLabel()));

			sb.append("\"");
		}

		if (variantOption.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(variantOption.getName()));

			sb.append("\"");
		}

		if (variantOption.getValue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"value\": ");

			sb.append("\"");

			sb.append(_escape(variantOption.getValue()));

			sb.append("\"");
		}

		if (variantOption.getVariantOptionValues() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"variantOptionValues\": ");

			sb.append("[");

			for (int i = 0; i < variantOption.getVariantOptionValues().length;
				 i++) {

				sb.append(
					String.valueOf(variantOption.getVariantOptionValues()[i]));

				if ((i + 1) < variantOption.getVariantOptionValues().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		VariantOptionJSONParser variantOptionJSONParser =
			new VariantOptionJSONParser();

		return variantOptionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(VariantOption variantOption) {
		if (variantOption == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (variantOption.getLabel() == null) {
			map.put("label", null);
		}
		else {
			map.put("label", String.valueOf(variantOption.getLabel()));
		}

		if (variantOption.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(variantOption.getName()));
		}

		if (variantOption.getValue() == null) {
			map.put("value", null);
		}
		else {
			map.put("value", String.valueOf(variantOption.getValue()));
		}

		if (variantOption.getVariantOptionValues() == null) {
			map.put("variantOptionValues", null);
		}
		else {
			map.put(
				"variantOptionValues",
				String.valueOf(variantOption.getVariantOptionValues()));
		}

		return map;
	}

	public static class VariantOptionJSONParser
		extends BaseJSONParser<VariantOption> {

		@Override
		protected VariantOption createDTO() {
			return new VariantOption();
		}

		@Override
		protected VariantOption[] createDTOArray(int size) {
			return new VariantOption[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "label")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "value")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "variantOptionValues")) {

				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			VariantOption variantOption, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "label")) {
				if (jsonParserFieldValue != null) {
					variantOption.setLabel((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					variantOption.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "value")) {
				if (jsonParserFieldValue != null) {
					variantOption.setValue((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "variantOptionValues")) {

				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					VariantOptionValue[] variantOptionValuesArray =
						new VariantOptionValue[jsonParserFieldValues.length];

					for (int i = 0; i < variantOptionValuesArray.length; i++) {
						variantOptionValuesArray[i] =
							VariantOptionValueSerDes.toDTO(
								(String)jsonParserFieldValues[i]);
					}

					variantOption.setVariantOptionValues(
						variantOptionValuesArray);
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
// LIFERAY-REST-BUILDER-HASH:851865111
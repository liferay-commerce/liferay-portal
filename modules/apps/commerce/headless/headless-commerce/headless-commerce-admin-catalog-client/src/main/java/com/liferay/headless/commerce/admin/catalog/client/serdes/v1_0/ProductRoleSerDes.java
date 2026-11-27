/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.client.serdes.v1_0;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductRole;
import com.liferay.headless.commerce.admin.catalog.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Zoltán Takács
 * @generated
 */
@Generated("")
public class ProductRoleSerDes {

	public static ProductRole toDTO(String json) {
		ProductRoleJSONParser productRoleJSONParser =
			new ProductRoleJSONParser();

		return productRoleJSONParser.parseToDTO(json);
	}

	public static ProductRole[] toDTOs(String json) {
		ProductRoleJSONParser productRoleJSONParser =
			new ProductRoleJSONParser();

		return productRoleJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ProductRole productRole) {
		if (productRole == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (productRole.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(productRole.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (productRole.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(productRole.getId());
		}

		if (productRole.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(productRole.getName()));

			sb.append("\"");
		}

		if (productRole.getRoleId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"roleId\": ");

			sb.append(productRole.getRoleId());
		}

		if (productRole.getRoleType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"roleType\": ");

			sb.append("\"");
			sb.append(productRole.getRoleType());
			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ProductRoleJSONParser productRoleJSONParser =
			new ProductRoleJSONParser();

		return productRoleJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ProductRole productRole) {
		if (productRole == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (productRole.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(productRole.getExternalReferenceCode()));
		}

		if (productRole.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(productRole.getId()));
		}

		if (productRole.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(productRole.getName()));
		}

		if (productRole.getRoleId() == null) {
			map.put("roleId", null);
		}
		else {
			map.put("roleId", String.valueOf(productRole.getRoleId()));
		}

		if (productRole.getRoleType() == null) {
			map.put("roleType", null);
		}
		else {
			map.put("roleType", String.valueOf(productRole.getRoleType()));
		}

		return map;
	}

	public static class ProductRoleJSONParser
		extends BaseJSONParser<ProductRole> {

		@Override
		protected ProductRole createDTO() {
			return new ProductRole();
		}

		@Override
		protected ProductRole[] createDTOArray(int size) {
			return new ProductRole[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "roleId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "roleType")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ProductRole productRole, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				if (jsonParserFieldValue != null) {
					productRole.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					productRole.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					productRole.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "roleId")) {
				if (jsonParserFieldValue != null) {
					productRole.setRoleId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "roleType")) {
				if (jsonParserFieldValue != null) {
					productRole.setRoleType(
						ProductRole.RoleType.create(
							(String)jsonParserFieldValue));
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
// LIFERAY-REST-BUILDER-HASH:1085187809
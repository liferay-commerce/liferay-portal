/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.client.serdes.v1_0;

import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.ProductAccount;
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
public class ProductAccountSerDes {

	public static ProductAccount toDTO(String json) {
		ProductAccountJSONParser productAccountJSONParser =
			new ProductAccountJSONParser();

		return productAccountJSONParser.parseToDTO(json);
	}

	public static ProductAccount[] toDTOs(String json) {
		ProductAccountJSONParser productAccountJSONParser =
			new ProductAccountJSONParser();

		return productAccountJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ProductAccount productAccount) {
		if (productAccount == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (productAccount.getAccountExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountExternalReferenceCode\": ");

			sb.append("\"");

			sb.append(
				_escape(productAccount.getAccountExternalReferenceCode()));

			sb.append("\"");
		}

		if (productAccount.getAccountId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountId\": ");

			sb.append(productAccount.getAccountId());
		}

		if (productAccount.getAccountType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountType\": ");

			sb.append("\"");
			sb.append(productAccount.getAccountType());
			sb.append("\"");
		}

		if (productAccount.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(productAccount.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (productAccount.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(productAccount.getId());
		}

		if (productAccount.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(productAccount.getName()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ProductAccountJSONParser productAccountJSONParser =
			new ProductAccountJSONParser();

		return productAccountJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ProductAccount productAccount) {
		if (productAccount == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (productAccount.getAccountExternalReferenceCode() == null) {
			map.put("accountExternalReferenceCode", null);
		}
		else {
			map.put(
				"accountExternalReferenceCode",
				String.valueOf(
					productAccount.getAccountExternalReferenceCode()));
		}

		if (productAccount.getAccountId() == null) {
			map.put("accountId", null);
		}
		else {
			map.put("accountId", String.valueOf(productAccount.getAccountId()));
		}

		if (productAccount.getAccountType() == null) {
			map.put("accountType", null);
		}
		else {
			map.put(
				"accountType", String.valueOf(productAccount.getAccountType()));
		}

		if (productAccount.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(productAccount.getExternalReferenceCode()));
		}

		if (productAccount.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(productAccount.getId()));
		}

		if (productAccount.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(productAccount.getName()));
		}

		return map;
	}

	public static class ProductAccountJSONParser
		extends BaseJSONParser<ProductAccount> {

		@Override
		protected ProductAccount createDTO() {
			return new ProductAccount();
		}

		@Override
		protected ProductAccount[] createDTOArray(int size) {
			return new ProductAccount[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(
					jsonParserFieldName, "accountExternalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "accountId")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "accountType")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ProductAccount productAccount, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(
					jsonParserFieldName, "accountExternalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					productAccount.setAccountExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "accountId")) {
				if (jsonParserFieldValue != null) {
					productAccount.setAccountId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "accountType")) {
				if (jsonParserFieldValue != null) {
					productAccount.setAccountType(
						ProductAccount.AccountType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					productAccount.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					productAccount.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					productAccount.setName((String)jsonParserFieldValue);
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
// LIFERAY-REST-BUILDER-HASH:732593874
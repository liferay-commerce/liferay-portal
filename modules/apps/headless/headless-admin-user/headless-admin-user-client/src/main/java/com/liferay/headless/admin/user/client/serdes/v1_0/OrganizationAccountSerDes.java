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

package com.liferay.headless.admin.user.client.serdes.v1_0;

import com.liferay.headless.admin.user.client.dto.v1_0.OrganizationAccount;
import com.liferay.headless.admin.user.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class OrganizationAccountSerDes {

	public static OrganizationAccount toDTO(String json) {
		OrganizationAccountJSONParser organizationAccountJSONParser =
			new OrganizationAccountJSONParser();

		return organizationAccountJSONParser.parseToDTO(json);
	}

	public static OrganizationAccount[] toDTOs(String json) {
		OrganizationAccountJSONParser organizationAccountJSONParser =
			new OrganizationAccountJSONParser();

		return organizationAccountJSONParser.parseToDTOs(json);
	}

	public static String toJSON(OrganizationAccount organizationAccount) {
		if (organizationAccount == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (organizationAccount.getDescription() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"description\": ");

			sb.append("\"");

			sb.append(_escape(organizationAccount.getDescription()));

			sb.append("\"");
		}

		if (organizationAccount.getDomains() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"domains\": ");

			sb.append("[");

			for (int i = 0; i < organizationAccount.getDomains().length; i++) {
				sb.append("\"");

				sb.append(_escape(organizationAccount.getDomains()[i]));

				sb.append("\"");

				if ((i + 1) < organizationAccount.getDomains().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (organizationAccount.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(organizationAccount.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (organizationAccount.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(organizationAccount.getId());
		}

		if (organizationAccount.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(organizationAccount.getName()));

			sb.append("\"");
		}

		if (organizationAccount.getOrganizationIds() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"organizationIds\": ");

			sb.append("[");

			for (int i = 0; i < organizationAccount.getOrganizationIds().length;
				 i++) {

				sb.append(organizationAccount.getOrganizationIds()[i]);

				if ((i + 1) < organizationAccount.getOrganizationIds().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (organizationAccount.getParentAccountId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"parentAccountId\": ");

			sb.append(organizationAccount.getParentAccountId());
		}

		if (organizationAccount.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append(organizationAccount.getStatus());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		OrganizationAccountJSONParser organizationAccountJSONParser =
			new OrganizationAccountJSONParser();

		return organizationAccountJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		OrganizationAccount organizationAccount) {

		if (organizationAccount == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (organizationAccount.getDescription() == null) {
			map.put("description", null);
		}
		else {
			map.put(
				"description",
				String.valueOf(organizationAccount.getDescription()));
		}

		if (organizationAccount.getDomains() == null) {
			map.put("domains", null);
		}
		else {
			map.put(
				"domains", String.valueOf(organizationAccount.getDomains()));
		}

		if (organizationAccount.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(organizationAccount.getExternalReferenceCode()));
		}

		if (organizationAccount.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(organizationAccount.getId()));
		}

		if (organizationAccount.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(organizationAccount.getName()));
		}

		if (organizationAccount.getOrganizationIds() == null) {
			map.put("organizationIds", null);
		}
		else {
			map.put(
				"organizationIds",
				String.valueOf(organizationAccount.getOrganizationIds()));
		}

		if (organizationAccount.getParentAccountId() == null) {
			map.put("parentAccountId", null);
		}
		else {
			map.put(
				"parentAccountId",
				String.valueOf(organizationAccount.getParentAccountId()));
		}

		if (organizationAccount.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(organizationAccount.getStatus()));
		}

		return map;
	}

	public static class OrganizationAccountJSONParser
		extends BaseJSONParser<OrganizationAccount> {

		@Override
		protected OrganizationAccount createDTO() {
			return new OrganizationAccount();
		}

		@Override
		protected OrganizationAccount[] createDTOArray(int size) {
			return new OrganizationAccount[size];
		}

		@Override
		protected void setField(
			OrganizationAccount organizationAccount, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "description")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setDescription(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "domains")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setDomains(
						toStrings((Object[])jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					organizationAccount.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "organizationIds")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setOrganizationIds(
						toLongs((Object[])jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "parentAccountId")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setParentAccountId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					organizationAccount.setStatus(
						Integer.valueOf((String)jsonParserFieldValue));
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

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}
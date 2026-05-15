<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %><%@
taglib uri="http://liferay.com/tld/frontend-data-set" prefix="frontend-data-set" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.commerce.model.CommerceOrder" %><%@
page import="com.liferay.commerce.model.CommerceOrderItem" %><%@
page import="com.liferay.commerce.order.CommerceOrderValidatorResult" %><%@
page import="com.liferay.commerce.order.content.web.internal.constants.CommerceOrderFragmentFDSNames" %><%@
page import="com.liferay.frontend.data.set.model.FDSActionDropdownItem" %><%@
page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem" %><%@
page import="com.liferay.petra.string.StringBundler" %><%@
page import="com.liferay.petra.string.StringPool" %><%@
page import="com.liferay.portal.kernel.language.LanguageUtil" %><%@
page import="com.liferay.portal.kernel.util.HtmlUtil" %>

<%@ page import="java.util.List" %><%@
page import="java.util.Map" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
Map<String, Object> additionalProps = (Map<String, Object>)request.getAttribute("liferay-commerce:order-data-set:additionalProps");
String apiURL = (String)request.getAttribute("liferay-commerce:order-data-set:apiURL");
List<DropdownItem> bulkActionDropdownItems = (List<DropdownItem>)request.getAttribute("liferay-commerce:order-data-set:fdsBulkActionDropdownItems");
CommerceOrder commerceOrder = (CommerceOrder)request.getAttribute("liferay-commerce:order-data-set:commerceOrder");
Map<Long, List<CommerceOrderValidatorResult>> commerceOrderValidatorResultsMap = (Map<Long, List<CommerceOrderValidatorResult>>)request.getAttribute("liferay-commerce:order-data-set:commerceOrderValidatorResults");
String displayStyle = (String)request.getAttribute("liferay-commerce:order-data-set:displayStyle");
List<FDSActionDropdownItem> fdsActionDropdownItems = (List<FDSActionDropdownItem>)request.getAttribute("liferay-commerce:order-data-set:fdsActionDropdownItems");
String name = (String)request.getAttribute("liferay-commerce:order-data-set:name");
String propsTransformer = (String)request.getAttribute("liferay-commerce:order-data-set:propsTransformer");

String languageId = LanguageUtil.getLanguageId(locale);
%>
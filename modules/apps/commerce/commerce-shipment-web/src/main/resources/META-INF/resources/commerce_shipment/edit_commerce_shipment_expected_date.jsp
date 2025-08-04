<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CommerceShipmentDisplayContext commerceShipmentDisplayContext = (CommerceShipmentDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CommerceShipment commerceShipment = commerceShipmentDisplayContext.getCommerceShipment();

Date expectedDate = commerceShipment.getExpectedDate();

int expectedDay = 0;
int expectedMonth = -1;
int expectedYear = 0;
String dateString = null;

Format format = FastDateFormatFactoryUtil.getSimpleDateFormat("yyyy-MM-dd", locale);

if (expectedDate != null) {
	Calendar calendar = CalendarFactoryUtil.getCalendar(expectedDate.getTime());

	expectedDay = calendar.get(Calendar.DAY_OF_MONTH);
	expectedMonth = calendar.get(Calendar.MONTH);
	expectedYear = calendar.get(Calendar.YEAR);

	dateString = format.format(calendar.getTime());
}
%>

<portlet:actionURL name="/commerce_shipment/edit_commerce_shipment" var="editCommerceShipmentURL" />

<liferay-ui:error exception="<%= CommerceShipmentExpectedDateException.class %>" />

<aui:form action="<%= editCommerceShipmentURL %>" cssClass="container-fluid container-fluid-max-xl p-4" method="post" name="fm">
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="expectedDate" />
	<aui:input name="commerceShipmentId" type="hidden" value="<%= commerceShipment.getCommerceShipmentId() %>" />
	<aui:input name="expectedDeliveryDate" type="date" value="<%= dateString %>" />
</aui:form>
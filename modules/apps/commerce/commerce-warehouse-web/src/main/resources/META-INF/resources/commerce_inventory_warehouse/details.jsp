<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
CommerceInventoryWarehousesDisplayContext commerceInventoryWarehousesDisplayContext = (CommerceInventoryWarehousesDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CommerceInventoryWarehouse commerceInventoryWarehouse = commerceInventoryWarehousesDisplayContext.getCommerceInventoryWarehouse();
%>

<liferay-ui:error-marker
	key="<%= WebKeys.ERROR_SECTION %>"
	value="details"
/>

<liferay-ui:error exception="<%= CommerceInventoryWarehouseActiveException.class %>" message="please-add-geolocation-information-to-the-warehouse-to-activate" />
<liferay-ui:error exception="<%= CommerceInventoryWarehouseNameException.class %>" message="please-enter-a-valid-name" />
<liferay-ui:error exception="<%= MVCCException.class %>" message="this-item-is-no-longer-valid-please-try-again" />

<aui:model-context bean="<%= commerceInventoryWarehouse %>" model="<%= CommerceInventoryWarehouse.class %>" />

<%--<aui:form action="<%= editCommercePriceListActionURL %>" cssClass="pt-4" method="post" name="fm">--%>

<div class="mt-4 row">
	<div class="col-lg-6">
		<commerce-ui:panel
			title='<%= LanguageUtil.get(request, "details") %>'
		>
			<aui:fieldset>
				<aui:input name="name" required="<%= true %>" />

				<aui:input name="description" />

				<aui:input checked="<%= (commerceInventoryWarehouse == null) ? false : commerceInventoryWarehouse.isActive() %>" inlineLabel="right" labelCssClass="simple-toggle-switch" name="active" type="toggle-switch" />
			</aui:fieldset>
		</commerce-ui:panel>
	</div>

	<div class="col-lg-6">
		<commerce-ui:panel
			title='<%= LanguageUtil.get(request, "geolocation") %>'
		>
			<aui:fieldset>
				<aui:input name="latitude" />

				<aui:input name="longitude" />
			</aui:fieldset>
		</commerce-ui:panel>
	</div>

	<div class="col">
		<commerce-ui:panel
			title='<%= LanguageUtil.get(request, "address") %>'
		>
			<%-- <div class="row no-gutters">--%>

			<aui:fieldset>
				<div class="row">
					<div class="col-lg-6">
						<aui:input name="street1" />

						<aui:input name="street3" />

						<aui:select label="region" name="commerceRegionCode" />

						<aui:input name="city" />
					</div>

					<div class="col-lg-6">
						<aui:input name="street2" />

						<aui:select label="country" name="countryTwoLettersISOCode" />

						<aui:input label="postal-code" name="zip" />
					</div>
				</div>
			</aui:fieldset>

			<%-- </div>--%>
		</commerce-ui:panel>
	</div>
</div>
<%--</aui:form>--%>
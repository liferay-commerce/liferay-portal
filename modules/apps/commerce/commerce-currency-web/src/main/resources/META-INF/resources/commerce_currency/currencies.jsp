<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CommerceCurrenciesDisplayContext commerceCurrenciesDisplayContext = (CommerceCurrenciesDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);
%>

<c:if test="<%= commerceCurrenciesDisplayContext.hasManageCommerceCurrencyPermission() %>">
	<div class="container-fluid container-xl mt-4">
		<commerce-ui:panel
			bodyClasses="flex-fill"
		>
			<frontend-data-set:headless-display
				apiURL="<%= commerceCurrenciesDisplayContext.getCurrencyApiURL() %>"
				bulkActionDropdownItems="<%= commerceCurrenciesDisplayContext.getBulkActions() %>"
				creationMenu="<%= commerceCurrenciesDisplayContext.getFDSCreationMenu() %>"
				fdsActionDropdownItems="<%= commerceCurrenciesDisplayContext.getFDSActionDropdownItems() %>"
				formName="fm"
				id="<%= CommerceCurrencyFDSNames.COMMERCE_CURRENCIES %>"
				itemsPerPage="<%= 10 %>"
				selectedItemsKey="id"
				selectionType="multiple"
				style="fluid"
			/>
		</commerce-ui:panel>
	</div>
</c:if>
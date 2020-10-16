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

<%@ include file="/data_set_display/init.jsp" %>

<link href="<%= PortalUtil.getStaticResourceURL(request, PortalUtil.getPathModule() + "/frontend-taglib-clay/data_set_display/styles/main.css") %>" rel="stylesheet" />

<div class="table-root" id="<%= containerId %>">
	<span aria-hidden="true" class="loading-animation my-7"></span>
</div>

<aui:script require='<%= module + " as dataSetDisplay" %>'>
	var container = document.getElementById('<%= containerId %>');

	dataSetDisplay.default(
		{
			actionParameterName:
				'<%= HtmlUtil.escapeJS(GetterUtil.getString(actionParameterName)) %>',
			activeViewSettings: <%= activeViewSettingsJSON %>,
			apiURL: '<%= HtmlUtil.escapeJS(apiURL) %>',
			appURL: '<%= HtmlUtil.escapeJS(appURL) %>',
			bulkActions: <%= jsonSerializer.serializeDeep(bulkActionDropdownItems) %>,
			componentId: '<%= HtmlUtil.escapeJS(containerId) %>',
			creationMenu: <%= jsonSerializer.serializeDeep(creationMenu) %>,
			currentURL:
				'<%= HtmlUtil.escapeJS(PortalUtil.getCurrentURL(request)) %>',
			dataProviderKey: '<%= HtmlUtil.escapeJS(dataProviderKey) %>',
			formId: '<%= HtmlUtil.escapeJS(GetterUtil.getString(formId)) %>',
			id: '<%= HtmlUtil.escapeJS(id) %>',
			nestedItemsKey:
				'<%= HtmlUtil.escapeJS(GetterUtil.getString(nestedItemsKey)) %>',
			nestedItemsReferenceKey:
				'<%= HtmlUtil.escapeJS(GetterUtil.getString(nestedItemsReferenceKey)) %>',
			pagination: {
				deltas: <%= jsonSerializer.serializeDeep(clayPaginationEntries) %>,
				initialDelta: <%= itemsPerPage %>,
				initialPageNumber: <%= pageNumber %>,
			},
			showManagementBar: <%= showManagementBar %>,
			showPagination: <%= showPagination %>,
			showSearch: <%= showSearch %>,
			namespace: '<%= HtmlUtil.escapeJS(namespace) %>',
			portletId: '<%= portletDisplay.getRootPortletId() %>',
			portletURL: '<%= HtmlUtil.escapeJS(portletURL.toString()) %>',
			selectedItems: <%= jsonSerializer.serializeDeep(selectedItems) %>,
			selectedItemsKey:
				'<%= HtmlUtil.escapeJS(GetterUtil.getString(selectedItemsKey)) %>',
			selectionType:
				'<%= HtmlUtil.escapeJS(GetterUtil.getString(selectionType)) %>',
			sorting: <%= jsonSerializer.serializeDeep(sortItemList) %>,
			style: '<%= HtmlUtil.escapeJS(style) %>',
			views: <%= jsonSerializer.serializeDeep(clayDataSetDisplayViewsContext) %>,
		},
		container
	);
</aui:script>
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
ResultRow resultRow = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

BatchPlannerLogDisplay batchPlannerLogDisplay = (BatchPlannerLogDisplay)resultRow.getObject();
%>

<liferay-ui:icon-menu
	direction="right-side"
	icon="<%= StringPool.BLANK %>"
	markupView="lexicon"
	message="<%= StringPool.BLANK %>"
	showWhenSingleIcon="<%= true %>"
>
	<c:if test="<%= batchPlannerLogDisplay.getFailedItemsCount() > 0 %>">
		<portlet:resourceURL id="/batch_planner/download_error_report" var="downloadErrorReportURL">
			<portlet:param name="batchEngineImportTaskId" value="<%= batchPlannerLogDisplay.getBatchEngineImportTaskERC() %>" />
			<portlet:param name="title" value="<%= batchPlannerLogDisplay.getTitle() %>" />
		</portlet:resourceURL>

		<liferay-ui:icon
			message="download-error-report"
			url="<%= downloadErrorReportURL %>"
		/>
	</c:if>
</liferay-ui:icon-menu>
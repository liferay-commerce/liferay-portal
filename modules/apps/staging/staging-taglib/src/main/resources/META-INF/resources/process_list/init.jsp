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
boolean deleteMenu = GetterUtil.getBoolean(request.getAttribute("liferay-staging:process-list:deleteMenu"));
String localTaskExecutorClassName = GetterUtil.getString(request.getAttribute("liferay-staging:process-list:localTaskExecutorClassName"));
String mvcRenderCommandName = GetterUtil.getString(request.getAttribute("liferay-staging:process-list:mvcRenderCommandName"));
boolean relaunchMenu = GetterUtil.getBoolean(request.getAttribute("liferay-staging:process-list:relaunchMenu"));
String remoteTaskExecutorClassName = GetterUtil.getString(request.getAttribute("liferay-staging:process-list:remoteTaskExecutorClassName"));
ResultRowSplitter resultRowSplitter = (ResultRowSplitter)request.getAttribute("liferay-staging:process-list:resultRowSplitter");
boolean summaryMenu = GetterUtil.getBoolean(request.getAttribute("liferay-staging:process-list:summaryMenu"));

if (Validator.isNull(remoteTaskExecutorClassName)) {
	remoteTaskExecutorClassName = localTaskExecutorClassName;
}

String displayStyle = ParamUtil.getString(request, "displayStyle", "descriptive");
String navigation = ParamUtil.getString(request, "navigation", "all");
String orderByCol = ParamUtil.getString(request, "orderByCol");
String orderByType = ParamUtil.getString(request, "orderByType");
String searchContainerId = ParamUtil.getString(request, "searchContainerId");

OrderByComparator<BackgroundTask> orderByComparator = BackgroundTaskComparatorFactoryUtil.getBackgroundTaskOrderByComparator(orderByCol, orderByType);

String processListListViewCss = "process-list";

if (Objects.equals(displayStyle, "list")) {
	processListListViewCss += " process-list-list-view";
}

StagingGroupHelper stagingGroupHelper = StagingGroupHelperUtil.getStagingGroupHelper();

boolean localPublishing = (liveGroup == null) || stagingGroupHelper.isLocalStagingOrLocalLiveGroup(liveGroup);

PortletURL renderURL = PortletURLBuilder.createRenderURL(
	liferayPortletResponse
).setMVCRenderCommandName(
	mvcRenderCommandName
).setNavigation(
	navigation
).setTabs1(
	"processes"
).setParameter(
	"displayStyle", displayStyle
).setParameter(
	"localPublishing", localPublishing
).setParameter(
	"orderByCol", orderByCol
).setParameter(
	"orderByType", orderByType
).setParameter(
	"searchContainerId", searchContainerId
).build();

String taskExecutorClassName = localPublishing ? localTaskExecutorClassName : remoteTaskExecutorClassName;
%>
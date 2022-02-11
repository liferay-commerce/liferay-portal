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
CPSpecificationOptionFacetsDisplayContext cpSpecificationOptionSearchFacetDisplayContext = (CPSpecificationOptionFacetsDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CPSpecificationOptionFacetPortletInstanceConfiguration cpSpecificationOptionFacetPortletInstanceConfiguration = cpSpecificationOptionSearchFacetDisplayContext.getCPSpecificationOptionFacetPortletInstanceConfiguration();
%>

<c:choose>
	<c:when test="<%= !cpSpecificationOptionSearchFacetDisplayContext.hasCommerceChannel() %>">
		<div class="alert alert-info mx-auto">
			<liferay-ui:message key="this-site-does-not-have-a-channel" />
		</div>
	</c:when>
	<c:otherwise>

		<%
		for (CPSpecificationOptionsSearchFacetDisplayContext cpSpecificationOptionsSearchFacetDisplayContext : cpSpecificationOptionSearchFacetDisplayContext.getCpSpecificationOptionsSearchFacetDisplayContext()) {
			Facet facet = cpSpecificationOptionsSearchFacetDisplayContext.getFacet();

			String parameterName = cpSpecificationOptionsSearchFacetDisplayContext.getParameterName();
		%>

			<aui:form method="post" name='<%= "assetEntriesFacetForm_" + parameterName %>'>
				<aui:input autocomplete="off" name="<%= HtmlUtil.escapeAttribute(cpSpecificationOptionsSearchFacetDisplayContext.getParameterName()) %>" type="hidden" value="<%= cpSpecificationOptionsSearchFacetDisplayContext.getParameterValue() %>" />
				<aui:input cssClass="facet-parameter-name" name="facet-parameter-name" type="hidden" value="<%= parameterName %>" />
				<aui:input cssClass="start-parameter-name" name="start-parameter-name" type="hidden" value="<%= cpSpecificationOptionsSearchFacetDisplayContext.getPaginationStartParameterName() %>" />

				<liferay-ddm:template-renderer
					className="<%= CPSpecificationOptionsSearchFacetTermDisplayContext.class.getName() %>"
					contextObjects='<%=
						HashMapBuilder.<String, Object>put(
							"cpSpecificationOptionsSearchFacetDisplayContext", cpSpecificationOptionsSearchFacetDisplayContext
						).put(
							"namespace", liferayPortletResponse.getNamespace()
						).put(
							"title", HtmlUtil.escape(cpSpecificationOptionsSearchFacetDisplayContext.getCPSpecificationOptionTitle(facet.getFieldName()))
						).build()
					%>'
					displayStyle="<%= cpSpecificationOptionFacetPortletInstanceConfiguration.displayStyle() %>"
					displayStyleGroupId="<%= cpSpecificationOptionsSearchFacetDisplayContext.getDisplayStyleGroupId() %>"
					entries="<%= cpSpecificationOptionsSearchFacetDisplayContext.getTermDisplayContexts() %>"
				>
					<liferay-ui:panel-container
						extended="<%= true %>"
						id='<%= liferayPortletResponse.getNamespace() + "facetCPSpecificationOptionsPanelContainer" %>'
						markupView="lexicon"
						persistState="<%= true %>"
					>
						<liferay-ui:panel
							collapsible="<%= true %>"
							cssClass="search-facet"
							id='<%= liferayPortletResponse.getNamespace() + "facetCPSpecificationOptionsPanel" %>'
							markupView="lexicon"
							persistState="<%= true %>"
							title="<%= HtmlUtil.escape(cpSpecificationOptionsSearchFacetDisplayContext.getCPSpecificationOptionTitle(facet.getFieldName())) %>"
						>
							<aui:fieldset>
								<ul class="list-unstyled">

									<%
									int i = 0;

									for (CPSpecificationOptionsSearchFacetTermDisplayContext cpSpecificationOptionsSearchFacetTermDisplayContext : cpSpecificationOptionsSearchFacetDisplayContext.getTermDisplayContexts()) {
										i++;
									%>

										<li class="facet-value">
											<div class="custom-checkbox custom-control">
												<label class="facet-checkbox-label" for="<portlet:namespace />term_<%= i %>">
													<input
														<%= cpSpecificationOptionsSearchFacetTermDisplayContext.isSelected() ? "checked" : StringPool.BLANK %>
														class="custom-control-input facet-term"
														data-term-id="<%= cpSpecificationOptionsSearchFacetTermDisplayContext.getDisplayName() %>"
														id="<portlet:namespace />term_<%= parameterName + i %>"
														name="<portlet:namespace />term_<%= parameterName + i %>"
														onChange="Liferay.Search.FacetUtil.changeSelection(event);"
														type="checkbox"
													/>

													<span class="custom-control-label term-name <%= cpSpecificationOptionsSearchFacetTermDisplayContext.isSelected() ? "facet-term-selected" : "facet-term-unselected" %>">
														<span class="custom-control-label-text"><%= HtmlUtil.escape(cpSpecificationOptionsSearchFacetTermDisplayContext.getDisplayName()) %></span>
													</span>

													<c:if test="<%= cpSpecificationOptionsSearchFacetTermDisplayContext.isFrequencyVisible() %>">
														<small class="term-count">
															(<%= cpSpecificationOptionsSearchFacetTermDisplayContext.getFrequency() %>)
														</small>
													</c:if>
												</label>
											</div>
										</li>

									<%
									}
									%>

								</ul>
							</aui:fieldset>

							<c:if test="<%= !cpSpecificationOptionsSearchFacetDisplayContext.isNothingSelected() %>">
								<aui:button cssClass="btn-link btn-unstyled facet-clear-btn" onClick="Liferay.Search.FacetUtil.clearSelections(event);" value="clear" />
							</c:if>
						</liferay-ui:panel>
					</liferay-ui:panel-container>
				</liferay-ddm:template-renderer>
			</aui:form>

		<%
		}
		%>

	</c:otherwise>
</c:choose>

<aui:script use="liferay-search-facet-util"></aui:script>
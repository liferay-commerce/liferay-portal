<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CPDefinitionSpecificationOptionValueDisplayContext cpDefinitionSpecificationOptionValueDisplayContext = (CPDefinitionSpecificationOptionValueDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CPDefinitionSpecificationOptionValue cpDefinitionSpecificationOptionValue = cpDefinitionSpecificationOptionValueDisplayContext.getCPDefinitionSpecificationOptionValue();
List<CPOptionCategory> cpOptionCategories = cpDefinitionSpecificationOptionValueDisplayContext.getCPOptionCategories();

CPSpecificationOption cpSpecificationOption = cpDefinitionSpecificationOptionValue.getCPSpecificationOption();

long cpOptionCategoryId = BeanParamUtil.getLong(cpDefinitionSpecificationOptionValue, request, "CPOptionCategoryId");
%>

<portlet:actionURL name="/cp_definitions/edit_cp_definition_specification_option_value" var="editProductDefinitionSpecificationOptionValueActionURL" />

<liferay-frontend:side-panel-content
	title="<%= cpSpecificationOption.getTitle(locale) %>"
>
	<commerce-ui:panel
		title='<%= LanguageUtil.get(request, "detail") %>'
	>
		<aui:form action="<%= editProductDefinitionSpecificationOptionValueActionURL %>" method="post" name="fm">
			<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
			<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />
			<aui:input name="cpDefinitionSpecificationOptionValueId" type="hidden" value="<%= String.valueOf(cpDefinitionSpecificationOptionValue.getCPDefinitionSpecificationOptionValueId()) %>" />

			<liferay-ui:error exception="<%= CPDefinitionSpecificationOptionValueKeyException.class %>" message="please-enter-a-valid-key" />

			<aui:field-wrapper label='<%= LanguageUtil.get(resourceBundle, "value") %>' name="valueFieldWrapper">
				<div id="autocomplete-root"></div>
			</aui:field-wrapper>

			<aui:select label="group" name="CPOptionCategoryId" showEmptyOption="<%= true %>">

				<%
				for (CPOptionCategory cpOptionCategory : cpOptionCategories) {
				%>

					<aui:option label="<%= cpOptionCategory.getTitle(locale) %>" selected="<%= cpOptionCategoryId == cpOptionCategory.getCPOptionCategoryId() %>" value="<%= cpOptionCategory.getCPOptionCategoryId() %>" />

				<%
				}
				%>

			</aui:select>

			<aui:input label="key" name="key" required="<%= true %>" value="<%= cpDefinitionSpecificationOptionValue.getKey() %>" />

			<%
			NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

			numberFormat.setMinimumFractionDigits(0);
			%>

			<aui:input label="position" name="priority" value="<%= numberFormat.format(cpDefinitionSpecificationOptionValue.getPriority()) %>">
				<aui:validator name="min">[0]</aui:validator>
				<aui:validator name="number" />
			</aui:input>

			<c:if test="<%= cpDefinitionSpecificationOptionValueDisplayContext.hasCustomAttributesAvailable() %>">
				<liferay-expando:custom-attribute-list
					className="<%= CPDefinitionSpecificationOptionValue.class.getName() %>"
					classPK="<%= (cpDefinitionSpecificationOptionValue != null) ? cpDefinitionSpecificationOptionValue.getCPDefinitionSpecificationOptionValueId() : 0 %>"
					editable="<%= true %>"
					label="<%= true %>"
				/>
			</c:if>

			<aui:button-row>
				<aui:button cssClass="btn-lg" type="submit" value="save" />

				<aui:button cssClass="btn-lg" type="cancel" />
			</aui:button-row>
		</aui:form>
	</commerce-ui:panel>
</liferay-frontend:side-panel-content>

<aui:script require="commerce-frontend-js/components/autocomplete/entry as autocomplete">
	autocomplete.default('autocomplete', 'autocomplete-root', {
		apiUrl:
			'<%= cpDefinitionSpecificationOptionValueDisplayContext.getListTypeEntriesByExternalReferenceCodeURL() %>',
		initialLabel:
			'<%= cpDefinitionSpecificationOptionValue.getValue(locale) %>',
		initialValue:
			'<%= cpDefinitionSpecificationOptionValue.getValue(locale) %>',
		inputId: 'value',
		inputName: '<%= liferayPortletResponse.getNamespace() %>value',
		itemsKey: 'name',
		itemsLabel: 'name',
	});
</aui:script>
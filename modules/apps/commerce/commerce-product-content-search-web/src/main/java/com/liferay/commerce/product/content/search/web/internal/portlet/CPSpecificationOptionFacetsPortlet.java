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

package com.liferay.commerce.product.content.search.web.internal.portlet;

import com.liferay.commerce.product.constants.CPField;
import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.content.search.web.internal.configuration.CPSpecificationOptionsFacetConfiguration;
import com.liferay.commerce.product.content.search.web.internal.display.builder.CPSpecificationOptionPermissionChecker;
import com.liferay.commerce.product.content.search.web.internal.display.builder.CPSpecificationOptionsSearchFacetDisplayBuilder;
import com.liferay.commerce.product.content.search.web.internal.display.context.CPSpecificationOptionFacetsDisplayContext;
import com.liferay.commerce.product.content.search.web.internal.display.context.CPSpecificationOptionsSearchFacetDisplayContext;
import com.liferay.commerce.product.content.search.web.internal.util.CPSpecificationOptionFacetsUtil;
import com.liferay.commerce.product.model.CPSpecificationOption;
import com.liferay.commerce.product.permission.CPSpecificationOptionPermission;
import com.liferay.commerce.product.service.CPSpecificationOptionLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchRequest;
import com.liferay.portal.search.web.portlet.shared.search.PortletSharedSearchResponse;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	enabled = false,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=portlet-cp-specification-option-facets",
		"com.liferay.portlet.display-category=commerce",
		"com.liferay.portlet.instanceable=true",
		"com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.restore-current-view=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Specification Facet",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/specification_option_facets/view.jsp",
		"javax.portlet.name=" + CPPortletKeys.CP_SPECIFICATION_OPTION_FACETS,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=guest,power-user,user"
	},
	service = Portlet.class
)
public class CPSpecificationOptionFacetsPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletSharedSearchResponse portletSharedSearchResponse =
			portletSharedSearchRequest.search(renderRequest);

		try {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

			List<Facet> filledFacets = new ArrayList<>();

			Facet facet = portletSharedSearchResponse.getFacet(
				CPField.SPECIFICATION_NAMES);

			FacetCollector facetCollector = facet.getFacetCollector();

			for (TermCollector termCollector :
					facetCollector.getTermCollectors()) {

				CPSpecificationOption cpSpecificationOption =
					_cpSpecificationOptionLocalService.getCPSpecificationOption(
						themeDisplay.getCompanyId(), termCollector.getTerm());

				if (cpSpecificationOption.isFacetable()) {
					filledFacets.add(
						portletSharedSearchResponse.getFacet(
							CPSpecificationOptionFacetsUtil.getIndexFieldName(
								termCollector.getTerm(),
								themeDisplay.getLanguageId())));
				}
			}

			List<CPSpecificationOptionsSearchFacetDisplayContext> list =
				new ArrayList<>();

			for (Facet filledFacet : filledFacets) {
				CPSpecificationOptionsSearchFacetDisplayContext
					cpSpecificationOptionsSearchFacetDisplayContext =
						_buildDisplayContext(
							filledFacet, portletSharedSearchResponse,
							renderRequest);

				list.add(cpSpecificationOptionsSearchFacetDisplayContext);
			}

			CPSpecificationOptionFacetsDisplayContext
				cpSpecificationOptionSearchFacetDisplayContext =
					new CPSpecificationOptionFacetsDisplayContext(
						portal.getHttpServletRequest(renderRequest));

			cpSpecificationOptionSearchFacetDisplayContext.
				setCpSpecificationOptionsSearchFacetDisplayContext(list);

			cpSpecificationOptionSearchFacetDisplayContext.setRenderRequest(
				renderRequest);

			renderRequest.setAttribute(
				WebKeys.PORTLET_DISPLAY_CONTEXT,
				cpSpecificationOptionSearchFacetDisplayContext);
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}

		super.render(renderRequest, renderResponse);
	}

	protected String getPaginationStartParameterName(
		PortletSharedSearchResponse portletSharedSearchResponse) {

		SearchResponse searchResponse =
			portletSharedSearchResponse.getSearchResponse();

		SearchRequest searchRequest = searchResponse.getRequest();

		return searchRequest.getPaginationStartParameterName();
	}

	@Reference
	protected CPSpecificationOptionPermission cpSpecificationOptionPermission;

	@Reference
	protected Portal portal;

	@Reference
	protected PortletSharedSearchRequest portletSharedSearchRequest;

	private CPSpecificationOptionsSearchFacetDisplayContext
			_buildDisplayContext(
				Facet facet,
				PortletSharedSearchResponse portletSharedSearchResponse,
				RenderRequest renderRequest)
		throws PortalException {

		CPSpecificationOptionFacetPortletPreferences
			cpSpecificationOptionFacetPortletPreferences =
				new CPSpecificationOptionFacetPortletPreferences(
					portletSharedSearchResponse.getPortletPreferences(
						renderRequest));

		CPSpecificationOptionsFacetConfiguration
			cpSpecificationOptionsFacetConfiguration =
				new CPSpecificationOptionsFacetConfiguration(
					facet.getFacetConfiguration());

		CPSpecificationOptionsSearchFacetDisplayBuilder
			cpSpecificationOptionsSearchFacetDisplayBuilder =
				new CPSpecificationOptionsSearchFacetDisplayBuilder(
					renderRequest);

		cpSpecificationOptionsSearchFacetDisplayBuilder.
			setCPSpecificationOptionLocalService(
				_cpSpecificationOptionLocalService);
		cpSpecificationOptionsSearchFacetDisplayBuilder.setDisplayStyle(
			cpSpecificationOptionFacetPortletPreferences.getDisplayStyle());
		cpSpecificationOptionsSearchFacetDisplayBuilder.setFacet(facet);
		cpSpecificationOptionsSearchFacetDisplayBuilder.setFrequenciesVisible(
			cpSpecificationOptionFacetPortletPreferences.
				isFrequenciesVisible());
		cpSpecificationOptionsSearchFacetDisplayBuilder.setFrequencyThreshold(
			cpSpecificationOptionsFacetConfiguration.getFrequencyThreshold());
		cpSpecificationOptionsSearchFacetDisplayBuilder.setMaxTerms(
			cpSpecificationOptionsFacetConfiguration.getMaxTerms());
		cpSpecificationOptionsSearchFacetDisplayBuilder.
			setPaginationStartParameterName(
				_getPaginationStartParameterName(portletSharedSearchResponse));
		cpSpecificationOptionsSearchFacetDisplayBuilder.setPortal(portal);

		cpSpecificationOptionsSearchFacetDisplayBuilder.
			setPortletSharedSearchResponse(portletSharedSearchResponse);

		ThemeDisplay themeDisplay = portletSharedSearchResponse.getThemeDisplay(
			renderRequest);

		Group group = themeDisplay.getScopeGroup();

		Group stagingGroup = group.getStagingGroup();

		if (stagingGroup != null) {
			cpSpecificationOptionsSearchFacetDisplayBuilder.setExcludedGroupId(
				stagingGroup.getGroupId());
		}

		cpSpecificationOptionsSearchFacetDisplayBuilder.setLocale(
			themeDisplay.getLocale());
		cpSpecificationOptionsSearchFacetDisplayBuilder.
			setCPSpecificationOptionPermissionChecker(
				new CPSpecificationOptionPermissionChecker(
					themeDisplay.getPermissionChecker(),
					cpSpecificationOptionPermission));

		CPSpecificationOptionFacetsUtil.copy(
			() -> portletSharedSearchResponse.getParameterValues(
				facet.getFieldName(), renderRequest),
			cpSpecificationOptionsSearchFacetDisplayBuilder::
				setParameterValues);

		return cpSpecificationOptionsSearchFacetDisplayBuilder.build();
	}

	private String _getPaginationStartParameterName(
		PortletSharedSearchResponse portletSharedSearchResponse) {

		SearchResponse searchResponse =
			portletSharedSearchResponse.getSearchResponse();

		SearchRequest searchRequest = searchResponse.getRequest();

		return searchRequest.getPaginationStartParameterName();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CPSpecificationOptionFacetsPortlet.class);

	@Reference
	private CPSpecificationOptionLocalService
		_cpSpecificationOptionLocalService;

}
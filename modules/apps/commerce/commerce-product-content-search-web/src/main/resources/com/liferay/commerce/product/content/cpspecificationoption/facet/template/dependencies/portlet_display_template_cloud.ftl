<@liferay_ui["panel-container"]
	extended=true
	id="${namespace + 'facetCPSpecificationOptionsPanelContainer'}"
	markupView="lexicon"
	persistState=true
>
	<@liferay_ui.panel
		collapsible=true
		cssClass="search-facet"
		id="${namespace + 'facetCPSpecificationOptionsPanel'}"
		markupView="lexicon"
		persistState=true
		title="${title}"
	>
		<ul class="list-unstyled tag-cloud">
			<#if entries?has_content>
				<#list entries as entry>
					<span class="facet-value">
						<button
							class="btn btn-link btn-unstyled facet-term ${(entry.isSelected())?then('facet-term-selected', 'facet-term-unselected')} tag-popularity-${entry.getPopularity()} term-name"
							data-term-id="${entry.getDisplayName()}"
							onClick="Liferay.Search.FacetUtil.changeSelection(event);"
						>
							${htmlUtil.escape(entry.getDisplayName())}
						</button>
					</span>
				</#list>
			</#if>
		</ul>

		<#if !cpSpecificationOptionsSearchFacetDisplayContext.isNothingSelected()>
			<@liferay_aui.button
				cssClass="btn-link btn-unstyled facet-clear-btn"
				onClick="Liferay.Search.FacetUtil.clearSelections(event);"
				value="clear"
			/>
		</#if>
	</@>
</@>
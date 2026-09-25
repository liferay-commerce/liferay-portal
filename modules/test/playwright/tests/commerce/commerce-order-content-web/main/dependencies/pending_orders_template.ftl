<#if entries?has_content>
	<#list entries as curCommerceOrder>
		<p>Pending Order ${curCommerceOrder.getCommerceOrderId()}</p>
	</#list>
</#if>
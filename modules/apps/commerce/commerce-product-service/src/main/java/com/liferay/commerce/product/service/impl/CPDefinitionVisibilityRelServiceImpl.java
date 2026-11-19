/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.impl;

import com.liferay.commerce.product.exception.NoSuchCPDefinitionException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.service.base.CPDefinitionVisibilityRelServiceBaseImpl;
import com.liferay.commerce.product.service.persistence.CPDefinitionPersistence;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Fabio Monaco
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CPDefinitionVisibilityRel"
	},
	service = AopService.class
)
public class CPDefinitionVisibilityRelServiceImpl
	extends CPDefinitionVisibilityRelServiceBaseImpl {

	@Override
	public CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
			String externalReferenceCode, long cpDefinitionId, String className,
			long classPK)
		throws PortalException {

		_checkCommerceCatalog(cpDefinitionId, ActionKeys.UPDATE);

		return cpDefinitionVisibilityRelLocalService.
			addCPDefinitionVisibilityRel(
				externalReferenceCode, getUserId(), cpDefinitionId, className,
				classPK);
	}

	@Override
	public void deleteCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws PortalException {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			cpDefinitionVisibilityRelPersistence.findByPrimaryKey(
				cpDefinitionVisibilityRelId);

		_checkCommerceCatalog(
			cpDefinitionVisibilityRel.getCPDefinitionId(), ActionKeys.UPDATE);

		cpDefinitionVisibilityRelLocalService.deleteCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRel);
	}

	@Override
	public CPDefinitionVisibilityRel getCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws PortalException {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			cpDefinitionVisibilityRelPersistence.findByPrimaryKey(
				cpDefinitionVisibilityRelId);

		_checkCommerceCatalog(
			cpDefinitionVisibilityRel.getCPDefinitionId(), ActionKeys.VIEW);

		return cpDefinitionVisibilityRel;
	}

	@Override
	public List<CPDefinitionVisibilityRel> getCPDefinitionVisibilityRels(
			long cpDefinitionId, String className, int start, int end,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws PortalException {

		_checkCommerceCatalog(cpDefinitionId, ActionKeys.VIEW);

		return cpDefinitionVisibilityRelPersistence.findByCPD_C(
			cpDefinitionId, _classNameLocalService.getClassNameId(className),
			start, end, orderByComparator);
	}

	@Override
	public int getCPDefinitionVisibilityRelsCount(
			long cpDefinitionId, String className)
		throws PortalException {

		_checkCommerceCatalog(cpDefinitionId, ActionKeys.VIEW);

		return cpDefinitionVisibilityRelPersistence.countByCPD_C(
			cpDefinitionId, _classNameLocalService.getClassNameId(className));
	}

	private void _checkCommerceCatalog(long cpDefinitionId, String actionId)
		throws PortalException {

		CPDefinition cpDefinition = _cpDefinitionPersistence.fetchByPrimaryKey(
			cpDefinitionId);

		if (cpDefinition == null) {
			throw new NoSuchCPDefinitionException();
		}

		CommerceCatalog commerceCatalog =
			_commerceCatalogLocalService.fetchCommerceCatalogByGroupId(
				cpDefinition.getGroupId());

		if (commerceCatalog == null) {
			throw new PrincipalException();
		}

		_commerceCatalogModelResourcePermission.check(
			getPermissionChecker(), commerceCatalog, actionId);
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.product.model.CommerceCatalog)"
	)
	private ModelResourcePermission<CommerceCatalog>
		_commerceCatalogModelResourcePermission;

	@Reference
	private CPDefinitionPersistence _cpDefinitionPersistence;

}
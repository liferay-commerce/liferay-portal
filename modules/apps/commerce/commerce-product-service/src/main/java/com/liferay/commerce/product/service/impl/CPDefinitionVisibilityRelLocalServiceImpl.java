/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.impl;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.product.exception.CPDefinitionVisibilityRelClassNameIdException;
import com.liferay.commerce.product.exception.CPDefinitionVisibilityRelClassPKException;
import com.liferay.commerce.product.exception.DuplicateCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.service.base.CPDefinitionVisibilityRelLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Fabio Monaco
 */
@Component(
	property = "model.class.name=com.liferay.commerce.product.model.CPDefinitionVisibilityRel",
	service = AopService.class
)
public class CPDefinitionVisibilityRelLocalServiceImpl
	extends CPDefinitionVisibilityRelLocalServiceBaseImpl {

	@Override
	public CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
			String externalReferenceCode, long userId, long cpDefinitionId,
			String className, long classPK)
		throws PortalException {

		long classNameId = _classNameLocalService.getClassNameId(className);

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			cpDefinitionVisibilityRelPersistence.fetchByCPD_C_C(
				cpDefinitionId, classNameId, classPK);

		if (existingCPDefinitionVisibilityRel != null) {
			throw new DuplicateCPDefinitionVisibilityRelException();
		}

		User user = _userLocalService.getUser(userId);

		_validate(className, classPK, user.getCompanyId());

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			cpDefinitionVisibilityRelPersistence.create(
				counterLocalService.increment());

		cpDefinitionVisibilityRel.setExternalReferenceCode(
			externalReferenceCode);
		cpDefinitionVisibilityRel.setCompanyId(user.getCompanyId());
		cpDefinitionVisibilityRel.setUserId(user.getUserId());
		cpDefinitionVisibilityRel.setUserName(user.getFullName());
		cpDefinitionVisibilityRel.setCPDefinitionId(cpDefinitionId);
		cpDefinitionVisibilityRel.setClassNameId(classNameId);
		cpDefinitionVisibilityRel.setClassPK(classPK);

		cpDefinitionVisibilityRel = cpDefinitionVisibilityRelPersistence.update(
			cpDefinitionVisibilityRel);

		_reindexCPDefinition(cpDefinitionId);

		return cpDefinitionVisibilityRel;
	}

	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws PortalException {

		cpDefinitionVisibilityRelPersistence.remove(cpDefinitionVisibilityRel);

		_reindexCPDefinition(cpDefinitionVisibilityRel.getCPDefinitionId());

		return cpDefinitionVisibilityRel;
	}

	@Override
	public CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
			long cpDefinitionVisibilityRelId)
		throws PortalException {

		return cpDefinitionVisibilityRelLocalService.
			deleteCPDefinitionVisibilityRel(
				cpDefinitionVisibilityRelPersistence.findByPrimaryKey(
					cpDefinitionVisibilityRelId));
	}

	@Override
	public void deleteCPDefinitionVisibilityRels(String className, long classPK)
		throws PortalException {

		for (CPDefinitionVisibilityRel cpDefinitionVisibilityRel :
				cpDefinitionVisibilityRelPersistence.findByC_C(
					_classNameLocalService.getClassNameId(className),
					classPK)) {

			cpDefinitionVisibilityRelLocalService.
				deleteCPDefinitionVisibilityRel(cpDefinitionVisibilityRel);
		}
	}

	@Override
	public CPDefinitionVisibilityRel fetchCPDefinitionVisibilityRel(
		long cpDefinitionId, String className, long classPK) {

		return cpDefinitionVisibilityRelPersistence.fetchByCPD_C_C(
			cpDefinitionId, _classNameLocalService.getClassNameId(className),
			classPK);
	}

	@Override
	public List<CPDefinitionVisibilityRel> getCPDefinitionVisibilityRels(
		long cpDefinitionId, String className, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return cpDefinitionVisibilityRelPersistence.findByCPD_C(
			cpDefinitionId, _classNameLocalService.getClassNameId(className),
			start, end, orderByComparator);
	}

	private void _reindexCPDefinition(long cpDefinitionId)
		throws PortalException {

		Indexer<CPDefinition> indexer = IndexerRegistryUtil.nullSafeGetIndexer(
			CPDefinition.class);

		indexer.reindex(CPDefinition.class.getName(), cpDefinitionId);
	}

	private void _validate(String className, long classPK, long companyId)
		throws PortalException {

		if (!className.equals(AccountEntry.class.getName())) {
			throw new CPDefinitionVisibilityRelClassNameIdException();
		}

		AccountEntry accountEntry = _accountEntryLocalService.fetchAccountEntry(
			classPK);

		if ((accountEntry == null) ||
			(accountEntry.getCompanyId() != companyId)) {

			throw new CPDefinitionVisibilityRelClassPKException();
		}
	}

	@Reference
	private AccountEntryLocalService _accountEntryLocalService;

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private UserLocalService _userLocalService;

}
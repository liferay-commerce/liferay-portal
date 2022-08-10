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

package com.liferay.commerce.product.service.impl;

import com.liferay.commerce.product.exception.DuplicateCommerceChannelAccountEntryRelException;
import com.liferay.commerce.product.model.CommerceChannelAccountEntryRel;
import com.liferay.commerce.product.model.CommerceChannelAccountEntryRelTable;
import com.liferay.commerce.product.service.base.CommerceChannelAccountEntryRelLocalServiceBaseImpl;
import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

/**
 * @author Alessio Antonio Rendina
 */
public class CommerceChannelAccountEntryRelLocalServiceImpl
	extends CommerceChannelAccountEntryRelLocalServiceBaseImpl {

	@Override
	public CommerceChannelAccountEntryRel addCommerceChannelAccountEntryRel(
			long userId, long accountEntryId, String className, long classPK,
			long commerceChannelId, boolean overrideEligibility,
			double priority, int type)
		throws PortalException {

		int commerceChannelAccountEntryRelsCount =
			commerceChannelAccountEntryRelPersistence.countByA_C_T(
				accountEntryId, commerceChannelId, type);

		if (commerceChannelAccountEntryRelsCount > 0) {
			throw new DuplicateCommerceChannelAccountEntryRelException();
		}

		User user = userLocalService.getUser(userId);

		long commerceChannelAccountEntryRelId = counterLocalService.increment();

		CommerceChannelAccountEntryRel commerceChannelAccountEntryRel =
			commerceChannelAccountEntryRelPersistence.create(
				commerceChannelAccountEntryRelId);

		commerceChannelAccountEntryRel.setCompanyId(user.getCompanyId());
		commerceChannelAccountEntryRel.setUserId(user.getUserId());
		commerceChannelAccountEntryRel.setUserName(user.getFullName());
		commerceChannelAccountEntryRel.setAccountEntryId(accountEntryId);
		commerceChannelAccountEntryRel.setClassNameId(
			classNameLocalService.getClassNameId(className));
		commerceChannelAccountEntryRel.setClassPK(classPK);
		commerceChannelAccountEntryRel.setCommerceChannelId(commerceChannelId);
		commerceChannelAccountEntryRel.setOverrideEligibility(
			overrideEligibility);
		commerceChannelAccountEntryRel.setPriority(priority);
		commerceChannelAccountEntryRel.setType(type);

		return commerceChannelAccountEntryRelPersistence.update(
			commerceChannelAccountEntryRel);
	}

	@Override
	public void deleteCommerceChannelAccountEntryRels(
		String className, long classPK) {

		commerceChannelAccountEntryRelPersistence.removeByC_C(
			classNameLocalService.getClassNameId(className), classPK);
	}

	@Override
	public void deleteCommerceChannelAccountEntryRelsByAccountEntryId(
		long accountEntryId) {

		commerceChannelAccountEntryRelPersistence.removeByAccountEntryId(
			accountEntryId);
	}

	@Override
	public void deleteCommerceChannelAccountEntryRelsByCommerceChannelId(
		long commerceChannelId) {

		commerceChannelAccountEntryRelPersistence.removeByCommerceChannelId(
			commerceChannelId);
	}

	@Override
	public CommerceChannelAccountEntryRel fetchCommerceChannelAccountEntryRel(
		long accountEntryId, long commerceChannelId, int type) {

		List<CommerceChannelAccountEntryRel> commerceChannelAccountEntryRels =
			commerceChannelAccountEntryRelPersistence.dslQuery(
				DSLQueryFactoryUtil.select(
					CommerceChannelAccountEntryRelTable.INSTANCE
				).from(
					CommerceChannelAccountEntryRelTable.INSTANCE
				).where(
					_getPredicate(
						accountEntryId,
						CommerceChannelAccountEntryRelTable.INSTANCE.
							accountEntryId,
						commerceChannelId,
						CommerceChannelAccountEntryRelTable.INSTANCE.
							commerceChannelId,
						type, CommerceChannelAccountEntryRelTable.INSTANCE.type)
				).orderBy(
					CommerceChannelAccountEntryRelTable.INSTANCE.
						commerceChannelId.descending(),
					CommerceChannelAccountEntryRelTable.INSTANCE.priority.
						descending()
				).limit(
					0, 1
				));

		if (commerceChannelAccountEntryRels.isEmpty()) {
			return null;
		}

		return commerceChannelAccountEntryRels.get(0);
	}

	@Override
	public CommerceChannelAccountEntryRel fetchCommerceChannelAccountEntryRel(
		long accountEntryId, String className, long classPK,
		long commerceChannelId, int type) {

		return commerceChannelAccountEntryRelPersistence.fetchByA_C_C_C_T(
			accountEntryId, classNameLocalService.getClassNameId(className),
			classPK, commerceChannelId, type);
	}

	@Override
	public List<CommerceChannelAccountEntryRel>
		getCommerceChannelAccountEntryRels(
			long accountEntryId, int type, int start, int end,
			OrderByComparator<CommerceChannelAccountEntryRel>
				orderByComparator) {

		return commerceChannelAccountEntryRelPersistence.findByA_T(
			accountEntryId, type, start, end, orderByComparator);
	}

	@Override
	public int getCommerceChannelAccountEntryRelsCount(
		long accountEntryId, int type) {

		return commerceChannelAccountEntryRelPersistence.countByA_T(
			accountEntryId, type);
	}

	public CommerceChannelAccountEntryRel updateCommerceChannelAccountEntryRel(
			long commerceChannelAccountEntryRelId, long commerceChannelId,
			long classPK, boolean overrideEligibility, double priority)
		throws PortalException {

		CommerceChannelAccountEntryRel commerceChannelAccountEntryRel =
			commerceChannelAccountEntryRelPersistence.findByPrimaryKey(
				commerceChannelAccountEntryRelId);

		CommerceChannelAccountEntryRel oldCommerceChannelAccountEntryRel =
			commerceChannelAccountEntryRelPersistence.fetchByA_C_C_C_T(
				commerceChannelAccountEntryRel.getAccountEntryId(),
				commerceChannelAccountEntryRel.getClassNameId(), classPK,
				commerceChannelId, commerceChannelAccountEntryRel.getType());

		if (oldCommerceChannelAccountEntryRel != null) {
			long oldCommerceChannelAccountEntryRelId =
				oldCommerceChannelAccountEntryRel.
					getCommerceChannelAccountEntryRelId();

			if (oldCommerceChannelAccountEntryRelId !=
					commerceChannelAccountEntryRel.
						getCommerceChannelAccountEntryRelId()) {

				throw new DuplicateCommerceChannelAccountEntryRelException();
			}
		}

		commerceChannelAccountEntryRel.setClassPK(classPK);
		commerceChannelAccountEntryRel.setCommerceChannelId(commerceChannelId);
		commerceChannelAccountEntryRel.setOverrideEligibility(
			overrideEligibility);
		commerceChannelAccountEntryRel.setPriority(priority);

		return commerceChannelAccountEntryRelPersistence.update(
			commerceChannelAccountEntryRel);
	}

	private Predicate _getPredicate(
		long accountEntryId,
		Column<CommerceChannelAccountEntryRelTable, Long> accountEntryIdColumn,
		long commerceChannelId,
		Column<CommerceChannelAccountEntryRelTable, Long>
			commerceChannelIdColumn,
		int type,
		Column<CommerceChannelAccountEntryRelTable, Integer> typeColumn) {

		return accountEntryIdColumn.eq(
			accountEntryId
		).and(
			commerceChannelIdColumn.in(new Long[] {commerceChannelId, 0L})
		).and(
			typeColumn.eq(type)
		);
	}

}
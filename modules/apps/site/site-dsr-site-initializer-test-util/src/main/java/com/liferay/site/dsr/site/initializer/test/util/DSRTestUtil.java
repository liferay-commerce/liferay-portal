/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.test.util;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerRegistry;

import java.io.Serializable;

/**
 * @author Stefano Motta
 */
public class DSRTestUtil {

	public static AccountEntry addAccountEntry() throws Exception {
		return AccountEntryLocalServiceUtil.addAccountEntry(
			StringPool.BLANK, TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			"business", 1, ServiceContextTestUtil.getServiceContext());
	}

	public static ObjectEntry addRoom(AccountEntry accountEntry, long userId)
		throws Exception {

		ObjectDefinition objectDefinition = getRoomObjectDefinition();

		return ObjectEntryLocalServiceUtil.addObjectEntry(
			0, userId, objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"name",
				StringUtil.toLowerCase("A" + RandomTestUtil.randomString())
			).put(
				"r_accountToDSRRooms_accountEntryId",
				accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	public static Group getOrAddGroup() throws Exception {
		Group group = GroupLocalServiceUtil.fetchGroup(
			TestPropsValues.getCompanyId(), GroupConstants.DSR);

		if (group != null) {
			return group;
		}

		group = GroupTestUtil.addGroup(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(), 0,
			GroupConstants.DSR);

		group.setExternalReferenceCode("L_" + GroupConstants.DSR);

		group = GroupLocalServiceUtil.updateGroup(group);

		PermissionChecker originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		String originalName = PrincipalThreadLocal.getName();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));

			PrincipalThreadLocal.setName(TestPropsValues.getUserId());

			ServiceContextThreadLocal.pushServiceContext(
				ServiceContextTestUtil.getServiceContext(group.getGroupId()));

			try (SafeCloseable safeCloseable =
					CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						group.getCompanyId())) {

				SiteInitializerRegistry siteInitializerRegistry =
					_siteInitializerRegistrySnapshot.get();

				SiteInitializer siteInitializer =
					siteInitializerRegistry.getSiteInitializer(
						_BUNDLE_SYMBOLIC_NAME);

				siteInitializer.initialize(group.getGroupId());
			}
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(
				originalPermissionChecker);

			PrincipalThreadLocal.setName(originalName);

			ServiceContextThreadLocal.popServiceContext();
		}

		return group;
	}

	public static Group getRoomGroup(ObjectEntry objectEntry) throws Exception {
		ObjectDefinition objectDefinition = getRoomObjectDefinition();

		return GroupLocalServiceUtil.fetchGroup(
			TestPropsValues.getCompanyId(),
			ClassNameLocalServiceUtil.getClassNameId(
				objectDefinition.getClassName()),
			objectEntry.getObjectEntryId());
	}

	public static ObjectDefinition getRoomObjectDefinition() throws Exception {
		return ObjectDefinitionLocalServiceUtil.
			getObjectDefinitionByExternalReferenceCode(
				"L_DSR_ROOM", TestPropsValues.getCompanyId());
	}

	public static ObjectEntry updateRoomStatus(
			ObjectEntry objectEntry, int roomStatus)
		throws Exception {

		return ObjectEntryLocalServiceUtil.partialUpdateObjectEntry(
			TestPropsValues.getUserId(), objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"roomStatus", roomStatus
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private static final String _BUNDLE_SYMBOLIC_NAME =
		"com.liferay.site.initializer.dsr";

	private static final Snapshot<SiteInitializerRegistry>
		_siteInitializerRegistrySnapshot = new Snapshot<>(
			DSRTestUtil.class, SiteInitializerRegistry.class);

}
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.security.permission.resource;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Tancredi Covioli
 */
public class DSRArchivedRoomFolderPortletResourcePermissionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_dsrArchivedRoomFolderPortletResourcePermission =
			new DSRArchivedRoomFolderPortletResourcePermission(
				_delegatePortletResourcePermission, _groupLocalService,
				_objectDefinitionLocalService, _objectEntryLocalService);
	}

	@Test
	public void testCheck() throws Exception {
		long groupId = RandomTestUtil.randomLong();
		long userId = RandomTestUtil.randomLong();

		String resourceName = RandomTestUtil.randomString();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_delegatePortletResourcePermission.getResourceName()
		).thenReturn(
			resourceName
		);

		Mockito.when(
			_permissionChecker.getUserId()
		).thenReturn(
			userId
		);

		try {
			_dsrArchivedRoomFolderPortletResourcePermission.check(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT);

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertEquals(
				String.format(
					"User %s must have %s permission for %s %s", userId,
					ActionKeys.ADD_DOCUMENT, resourceName, groupId),
				principalException.getMessage());
		}
	}

	@Test
	public void testContains() throws Exception {
		_testContainsByGroupDeniesArchivedRoom();
		_testContainsDeniesArchivedRoomForNonadmin();
		_testContainsDelegatesForNonroomGroup();
		_testContainsDelegatesForActiveRoom();
		_testContainsDelegatesAllowedActionOnArchivedRoom();
		_testContainsDelegatesForCompanyAdmin();
	}

	@Test
	public void testGetResourceName() {
		String resourceName = RandomTestUtil.randomString();

		Mockito.when(
			_delegatePortletResourcePermission.getResourceName()
		).thenReturn(
			resourceName
		);

		Assert.assertEquals(
			resourceName,
			_dsrArchivedRoomFolderPortletResourcePermission.getResourceName());
	}

	private void _initializeMocks(long groupId, boolean room, int roomStatus) {
		long companyId = RandomTestUtil.randomLong();
		long classPK = RandomTestUtil.randomLong();
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);
		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			_group.getClassName()
		).thenReturn(
			room ? _ROOM_CLASS_NAME : RandomTestUtil.randomString()
		);

		Mockito.when(
			_group.getClassPK()
		).thenReturn(
			classPK
		);

		Mockito.when(
			_group.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			_groupLocalService.fetchGroup(groupId)
		).thenReturn(
			_group
		);

		Mockito.when(
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", companyId)
		).thenReturn(
			objectDefinition
		);

		Mockito.when(
			objectDefinition.getClassName()
		).thenReturn(
			_ROOM_CLASS_NAME
		);

		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(classPK)
		).thenReturn(
			objectEntry
		);

		Mockito.when(
			objectEntry.getValues()
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"roomStatus", roomStatus
			).build()
		);
	}

	private void _testContainsByGroupDeniesArchivedRoom() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_group.getGroupId()
		).thenReturn(
			groupId
		);

		Assert.assertFalse(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, _group, ActionKeys.ADD_DOCUMENT));
	}

	private void _testContainsDelegatesAllowedActionOnArchivedRoom()
		throws Exception {

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_delegatePortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.VIEW)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.VIEW));

		Mockito.verify(
			_delegatePortletResourcePermission
		).contains(
			_permissionChecker, groupId, ActionKeys.VIEW
		);
	}

	private void _testContainsDelegatesForActiveRoom() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_APPROVED);

		Mockito.when(
			_delegatePortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegatePortletResourcePermission
		).contains(
			_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDelegatesForCompanyAdmin() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			true
		);

		Mockito.when(
			_delegatePortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegatePortletResourcePermission
		).contains(
			_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDelegatesForNonroomGroup() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, false, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_delegatePortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegatePortletResourcePermission
		).contains(
			_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDeniesArchivedRoomForNonadmin() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Assert.assertFalse(
			_dsrArchivedRoomFolderPortletResourcePermission.contains(
				_permissionChecker, groupId, ActionKeys.ADD_DOCUMENT));
	}

	private static final String _ROOM_CLASS_NAME =
		"com.liferay.object.model.ObjectEntry#L_DSR_ROOM";

	private final PortletResourcePermission _delegatePortletResourcePermission =
		Mockito.mock(PortletResourcePermission.class);
	private DSRArchivedRoomFolderPortletResourcePermission
		_dsrArchivedRoomFolderPortletResourcePermission;
	private final Group _group = Mockito.mock(Group.class);
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);
	private final ObjectDefinitionLocalService _objectDefinitionLocalService =
		Mockito.mock(ObjectDefinitionLocalService.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);

}
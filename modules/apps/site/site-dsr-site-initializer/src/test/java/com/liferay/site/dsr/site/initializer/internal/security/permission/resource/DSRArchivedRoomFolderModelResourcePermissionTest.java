/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.security.permission.resource;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
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
public class DSRArchivedRoomFolderModelResourcePermissionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		Mockito.when(
			_delegateModelResourcePermission.getPortletResourcePermission()
		).thenReturn(
			Mockito.mock(PortletResourcePermission.class)
		);

		_dsrArchivedRoomFolderModelResourcePermission =
			new DSRArchivedRoomFolderModelResourcePermission(
				_delegateModelResourcePermission, _dlFolderLocalService,
				_groupLocalService, _objectDefinitionLocalService,
				_objectEntryLocalService);
	}

	@Test
	public void testCheck() throws Exception {
		long folderId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();
		long userId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		String modelName = RandomTestUtil.randomString();

		Mockito.when(
			_delegateModelResourcePermission.getModelName()
		).thenReturn(
			modelName
		);

		Mockito.when(
			_folder.getFolderId()
		).thenReturn(
			folderId
		);

		Mockito.when(
			_folder.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_permissionChecker.getUserId()
		).thenReturn(
			userId
		);

		try {
			_dsrArchivedRoomFolderModelResourcePermission.check(
				_permissionChecker, _folder, ActionKeys.ADD_DOCUMENT);

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertEquals(
				String.format(
					"User %s must have %s permission for %s %s", userId,
					ActionKeys.ADD_DOCUMENT, modelName, folderId),
				principalException.getMessage());
		}
	}

	@Test
	public void testContains() throws Exception {
		_testContainsDeniesArchivedRoomForNonadmin();
		_testContainsByPrimaryKeyDeniesArchivedRoom();
		_testContainsDelegatesForNonroomGroup();
		_testContainsDelegatesForActiveRoom();
		_testContainsByPrimaryKeyDelegatesWhenDLFolderNull();
		_testContainsDelegatesAllowedActionOnArchivedRoom();
		_testContainsDelegatesForCompanyAdmin();
	}

	@Test
	public void testGetPortletResourcePermission() {
		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.
				getPortletResourcePermission() instanceof
					DSRArchivedRoomFolderPortletResourcePermission);
	}

	private void _initializeMocks(long groupId, boolean room, int roomStatus) {
		long companyId = RandomTestUtil.randomLong();
		long classPK = RandomTestUtil.randomLong();
		Group group = Mockito.mock(Group.class);
		ObjectDefinition objectDefinition = Mockito.mock(
			ObjectDefinition.class);
		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			group.getClassName()
		).thenReturn(
			room ? _ROOM_CLASS_NAME : RandomTestUtil.randomString()
		);

		Mockito.when(
			group.getClassPK()
		).thenReturn(
			classPK
		);

		Mockito.when(
			group.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			_groupLocalService.fetchGroup(groupId)
		).thenReturn(
			group
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

	private void _testContainsByPrimaryKeyDelegatesWhenDLFolderNull()
		throws Exception {

		long primaryKey = RandomTestUtil.randomLong();

		Mockito.when(
			_dlFolderLocalService.fetchDLFolder(primaryKey)
		).thenReturn(
			null
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, primaryKey, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsByPrimaryKeyDeniesArchivedRoom()
		throws Exception {

		long groupId = RandomTestUtil.randomLong();
		long primaryKey = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_dlFolderLocalService.fetchDLFolder(primaryKey)
		).thenReturn(
			Mockito.mock(DLFolder.class)
		);

		Mockito.when(
			Mockito.mock(
				DLFolder.class
			).getGroupId()
		).thenReturn(
			groupId
		);

		Assert.assertFalse(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.ADD_DOCUMENT));
	}

	private void _testContainsDelegatesAllowedActionOnArchivedRoom()
		throws Exception {

		Folder folder = Mockito.mock(Folder.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			folder.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.VIEW)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.VIEW));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, folder, ActionKeys.VIEW
		);
	}

	private void _testContainsDelegatesForActiveRoom() throws Exception {
		Folder folder = Mockito.mock(Folder.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_APPROVED);

		Mockito.when(
			folder.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, folder, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDelegatesForCompanyAdmin() throws Exception {
		Folder folder = Mockito.mock(Folder.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			true
		);

		Mockito.when(
			folder.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, folder, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDelegatesForNonroomGroup() throws Exception {
		Folder folder = Mockito.mock(Folder.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, false, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			folder.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, folder, ActionKeys.ADD_DOCUMENT));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, folder, ActionKeys.ADD_DOCUMENT
		);
	}

	private void _testContainsDeniesArchivedRoomForNonadmin() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_folder.getGroupId()
		).thenReturn(
			groupId
		);

		Assert.assertFalse(
			_dsrArchivedRoomFolderModelResourcePermission.contains(
				_permissionChecker, _folder, ActionKeys.ADD_DOCUMENT));
	}

	private static final String _ROOM_CLASS_NAME =
		"com.liferay.object.model.ObjectEntry#L_DSR_ROOM";

	private final ModelResourcePermission<Folder>
		_delegateModelResourcePermission = Mockito.mock(
			ModelResourcePermission.class);
	private final DLFolderLocalService _dlFolderLocalService = Mockito.mock(
		DLFolderLocalService.class);
	private DSRArchivedRoomFolderModelResourcePermission
		_dsrArchivedRoomFolderModelResourcePermission;
	private final Folder _folder = Mockito.mock(Folder.class);
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);
	private final ObjectDefinitionLocalService _objectDefinitionLocalService =
		Mockito.mock(ObjectDefinitionLocalService.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);

}
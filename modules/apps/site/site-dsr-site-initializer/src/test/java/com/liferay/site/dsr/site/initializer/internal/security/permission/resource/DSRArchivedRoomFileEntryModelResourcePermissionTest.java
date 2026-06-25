/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.site.initializer.internal.security.permission.resource;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.FileEntry;
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
public class DSRArchivedRoomFileEntryModelResourcePermissionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_dsrArchivedRoomFileEntryModelResourcePermission =
			new DSRArchivedRoomFileEntryModelResourcePermission(
				_delegateModelResourcePermission, _dlFileEntryLocalService,
				_groupLocalService, _objectDefinitionLocalService,
				_objectEntryLocalService);
	}

	@Test
	public void testCheck() throws Exception {
		long fileEntryId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();
		long userId = RandomTestUtil.randomLong();

		String modelName = RandomTestUtil.randomString();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_delegateModelResourcePermission.getModelName()
		).thenReturn(
			modelName
		);

		Mockito.when(
			_fileEntry.getFileEntryId()
		).thenReturn(
			fileEntryId
		);

		Mockito.when(
			_fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_permissionChecker.getUserId()
		).thenReturn(
			userId
		);

		try {
			_dsrArchivedRoomFileEntryModelResourcePermission.check(
				_permissionChecker, _fileEntry, ActionKeys.UPDATE);

			Assert.fail();
		}
		catch (PrincipalException.MustHavePermission principalException) {
			Assert.assertEquals(
				String.format(
					"User %s must have %s permission for %s %s", userId,
					ActionKeys.UPDATE, modelName, fileEntryId),
				principalException.getMessage());
		}
	}

	@Test
	public void testContains() throws Exception {
		_testContainsDeniesArchivedRoomForNonadmin();
		_testContainsByPrimaryKeyDeniesArchivedRoom();
		_testContainsDelegatesForNonroomGroup();
		_testContainsDelegatesForActiveRoom();
		_testContainsByPrimaryKeyDelegatesWhenDLFileEntryNull();
		_testContainsDelegatesAllowedActionOnArchivedRoom();
		_testContainsDelegatesForCompanyAdmin();
	}

	@Test
	public void testGetPortletResourcePermission() {
		PortletResourcePermission portletResourcePermission = Mockito.mock(
			PortletResourcePermission.class);

		Mockito.when(
			_delegateModelResourcePermission.getPortletResourcePermission()
		).thenReturn(
			portletResourcePermission
		);

		Assert.assertSame(
			portletResourcePermission,
			_dsrArchivedRoomFileEntryModelResourcePermission.
				getPortletResourcePermission());
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
			room ? "com.liferay.object.model.ObjectEntry#L_DSR_ROOM" :
				RandomTestUtil.randomString()
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
			"com.liferay.object.model.ObjectEntry#L_DSR_ROOM"
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

	private void _testContainsByPrimaryKeyDelegatesWhenDLFileEntryNull()
		throws Exception {

		long primaryKey = RandomTestUtil.randomLong();

		Mockito.when(
			_dlFileEntryLocalService.fetchDLFileEntry(primaryKey)
		).thenReturn(
			null
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.UPDATE)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.UPDATE));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, primaryKey, ActionKeys.UPDATE
		);
	}

	private void _testContainsByPrimaryKeyDeniesArchivedRoom()
		throws Exception {

		long groupId = RandomTestUtil.randomLong();
		DLFileEntry dlFileEntry = Mockito.mock(DLFileEntry.class);
		long primaryKey = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_dlFileEntryLocalService.fetchDLFileEntry(primaryKey)
		).thenReturn(
			dlFileEntry
		);

		Mockito.when(
			dlFileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Assert.assertFalse(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, primaryKey, ActionKeys.DELETE));
	}

	private void _testContainsDelegatesAllowedActionOnArchivedRoom()
		throws Exception {

		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.VIEW)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.VIEW));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, fileEntry, ActionKeys.VIEW
		);
	}

	private void _testContainsDelegatesForActiveRoom() throws Exception {
		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_APPROVED);

		Mockito.when(
			fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.UPDATE)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.UPDATE));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, fileEntry, ActionKeys.UPDATE
		);
	}

	private void _testContainsDelegatesForCompanyAdmin() throws Exception {
		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_permissionChecker.isCompanyAdmin()
		).thenReturn(
			true
		);

		Mockito.when(
			fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.DELETE)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.DELETE));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, fileEntry, ActionKeys.DELETE
		);
	}

	private void _testContainsDelegatesForNonroomGroup() throws Exception {
		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, false, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_delegateModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.UPDATE)
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, fileEntry, ActionKeys.UPDATE));

		Mockito.verify(
			_delegateModelResourcePermission
		).contains(
			_permissionChecker, fileEntry, ActionKeys.UPDATE
		);
	}

	private void _testContainsDeniesArchivedRoomForNonadmin() throws Exception {
		long groupId = RandomTestUtil.randomLong();

		_initializeMocks(groupId, true, WorkflowConstants.STATUS_INACTIVE);

		Mockito.when(
			_fileEntry.getGroupId()
		).thenReturn(
			groupId
		);

		Assert.assertFalse(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, _fileEntry, ActionKeys.UPDATE));
		Assert.assertFalse(
			_dsrArchivedRoomFileEntryModelResourcePermission.contains(
				_permissionChecker, _fileEntry, ActionKeys.DELETE));
	}

	private final ModelResourcePermission<FileEntry>
		_delegateModelResourcePermission = Mockito.mock(
			ModelResourcePermission.class);
	private final DLFileEntryLocalService _dlFileEntryLocalService =
		Mockito.mock(DLFileEntryLocalService.class);
	private DSRArchivedRoomFileEntryModelResourcePermission
		_dsrArchivedRoomFileEntryModelResourcePermission;
	private final FileEntry _fileEntry = Mockito.mock(FileEntry.class);
	private final GroupLocalService _groupLocalService = Mockito.mock(
		GroupLocalService.class);
	private final ObjectDefinitionLocalService _objectDefinitionLocalService =
		Mockito.mock(ObjectDefinitionLocalService.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);

}
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.product.constants.CPAttachmentFileEntryConstants;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Attachment;
import com.liferay.headless.commerce.delivery.catalog.client.pagination.Page;
import com.liferay.headless.commerce.delivery.catalog.client.problem.Problem;
import com.liferay.headless.commerce.delivery.catalog.client.resource.v1_0.AttachmentResource;
import com.liferay.portal.configuration.test.util.GroupConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class AttachmentResourceTest extends BaseAttachmentResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), RandomTestUtil.randomString());
		_cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);
	}

	@Override
	@Test
	public void testGetChannelProductAttachmentsPage() throws Exception {
		super.testGetChannelProductAttachmentsPage();

		_testGetChannelProductAttachmentsPageWithAccountGroupFilter();
		_testGetChannelProductAttachmentsPageWithChannelFilter();
	}

	@Override
	@Test
	public void testGetChannelProductImagesPage() throws Exception {
		super.testGetChannelProductImagesPage();

		_testGetChannelProductImagesPageWithAccountGroupFilter();
		_testGetChannelProductImagesPageWithChannelFilter();
	}

	@Override
	protected Attachment randomAttachment() throws Exception {
		return new Attachment() {
			{
				displayDate = RandomTestUtil.nextDate();
				expirationDate = RandomTestUtil.nextDate();
				id = RandomTestUtil.randomLong();
				neverExpire = true;
				priority = RandomTestUtil.randomDouble();
				title = StringUtil.toLowerCase(RandomTestUtil.randomString());
				type = CPAttachmentFileEntryConstants.TYPE_OTHER;
			}
		};
	}

	@Override
	protected Attachment testGetChannelProductAttachmentsPage_addAttachment(
			Long channelId, Long productId, Attachment attachment)
		throws Exception {

		return _addCPAttachmentFileEntry(attachment, _cpDefinition);
	}

	@Override
	protected Long testGetChannelProductAttachmentsPage_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGetChannelProductAttachmentsPage_getProductId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	@Override
	protected Attachment testGetChannelProductImagesPage_addAttachment(
			Long channelId, Long productId, Attachment attachment)
		throws Exception {

		attachment.setType(CPAttachmentFileEntryConstants.TYPE_IMAGE);

		return _addCPAttachmentFileEntry(attachment, _cpDefinition);
	}

	@Override
	protected Long testGetChannelProductImagesPage_getChannelId()
		throws Exception {

		return _commerceChannel.getCommerceChannelId();
	}

	@Override
	protected Long testGetChannelProductImagesPage_getProductId()
		throws Exception {

		return _cpDefinition.getCProductId();
	}

	private Attachment _addCPAttachmentFileEntry(
			Attachment attachment, CPDefinition cpDefinition)
		throws Exception {

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			RandomTestUtil.randomString(), _user.getUserId(),
			testGroup.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			null, null, RandomTestUtil.nextDate(), _serviceContext);

		Calendar displayDate = Calendar.getInstance();
		Calendar expirationDate = Calendar.getInstance();

		displayDate.setTime(attachment.getDisplayDate());
		expirationDate.setTime(attachment.getExpirationDate());

		CPAttachmentFileEntry attachmentFileEntry =
			_cpAttachmentFileEntryLocalService.addCPAttachmentFileEntry(
				RandomTestUtil.randomString(), _user.getUserId(),
				testGroup.getGroupId(),
				_classNameLocalService.getClassNameId(CPDefinition.class),
				cpDefinition.getCPDefinitionId(), fileEntry.getFileEntryId(),
				false, null, displayDate.get(Calendar.MONTH),
				displayDate.get(Calendar.DAY_OF_MONTH),
				displayDate.get(Calendar.YEAR), displayDate.get(Calendar.HOUR),
				displayDate.get(Calendar.MINUTE),
				expirationDate.get(Calendar.MONTH),
				expirationDate.get(Calendar.DAY_OF_MONTH),
				expirationDate.get(Calendar.YEAR),
				expirationDate.get(Calendar.HOUR),
				expirationDate.get(Calendar.MINUTE), true, true,
				RandomTestUtil.randomLocaleStringMap(), null,
				RandomTestUtil.nextDouble(), attachment.getType(),
				_serviceContext);

		_attachmentFileEntries.add(attachmentFileEntry);

		return new Attachment() {
			{
				displayDate = attachmentFileEntry.getDisplayDate();
				expirationDate = attachmentFileEntry.getExpirationDate();
				id = attachmentFileEntry.getCPAttachmentFileEntryId();
				priority = attachmentFileEntry.getPriority();
				title = attachmentFileEntry.getTitle();
				type = attachmentFileEntry.getType();
			}
		};
	}

	private CPDefinition _addCPDefinition(int type) throws Exception {
		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);

		Attachment attachment = randomAttachment();

		attachment.setType(type);

		_addCPAttachmentFileEntry(attachment, cpDefinition);

		return cpDefinition;
	}

	private void _assertNotFound(ThrowingRunnable throwingRunnable) {
		Problem.ProblemException problemException = Assert.assertThrows(
			Problem.ProblemException.class, throwingRunnable);

		Problem problem = problemException.getProblem();

		Assert.assertEquals("NOT_FOUND", problem.getStatus());
	}

	private Page<Attachment> _getChannelProductAttachmentsPage(
			Long accountId, CPDefinition cpDefinition, String password,
			User user)
		throws Exception {

		AttachmentResource attachmentResource = AttachmentResource.builder(
		).authentication(
			user.getEmailAddress(), password
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();

		return attachmentResource.getChannelProductAttachmentsPage(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), accountId, null);
	}

	private Page<Attachment> _getChannelProductImagesPage(
			Long accountId, CPDefinition cpDefinition, String password,
			User user)
		throws Exception {

		AttachmentResource attachmentResource = AttachmentResource.builder(
		).authentication(
			user.getEmailAddress(), password
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();

		return attachmentResource.getChannelProductImagesPage(
			_commerceChannel.getCommerceChannelId(),
			cpDefinition.getCProductId(), accountId, null);
	}

	private void _testGetChannelProductAttachmentsPageWithAccountGroupFilter()
		throws Exception {

		try (GroupConfigurationTemporarySwapper
				groupConfigurationTemporarySwapper =
					new GroupConfigurationTemporarySwapper(
						_commerceChannel.getGroupId(),
						CommerceConstants.SERVICE_NAME_COMMERCE_ACCOUNT,
						HashMapDictionaryBuilder.<String, Object>put(
							"commerceSiteType",
							CommerceChannelConstants.SITE_TYPE_B2B
						).build())) {

			CPDefinition cpDefinition = _addCPDefinition(
				CPAttachmentFileEntryConstants.TYPE_OTHER);

			_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
				cpDefinition.getCPDefinitionId(), true);

			String password = RandomTestUtil.randomString();

			User user1 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry1 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user1.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			AccountGroup accountGroup =
				CommerceAccountTestUtil.addAccountGroupAndAccountRel(
					RandomTestUtil.randomString(),
					AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
					accountEntry1.getAccountEntryId(), _serviceContext);

			_accountGroupRelLocalService.addAccountGroupRel(
				accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
				cpDefinition.getCPDefinitionId());

			Page<Attachment> attachmentsPage =
				_getChannelProductAttachmentsPage(
					null, cpDefinition, password, user1);

			Assert.assertEquals(1, attachmentsPage.getTotalCount());

			User user2 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry2 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user2.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry2.getAccountEntryId(), _serviceContext);

			_assertNotFound(
				() -> _getChannelProductAttachmentsPage(
					null, cpDefinition, password, user2));

			User user3 = UserTestUtil.addUser(testCompany, password);

			_assertNotFound(
				() -> _getChannelProductAttachmentsPage(
					null, cpDefinition, password, user3));

			attachmentsPage = _getChannelProductAttachmentsPage(
				accountEntry1.getAccountEntryId(), cpDefinition, password,
				user1);

			Assert.assertEquals(1, attachmentsPage.getTotalCount());

			_accountEntryLocalService.updateStatus(
				accountEntry1.getAccountEntryId(),
				WorkflowConstants.STATUS_INACTIVE);

			_assertNotFound(
				() -> _getChannelProductAttachmentsPage(
					accountEntry1.getAccountEntryId(), cpDefinition, password,
					user1));
			_assertNotFound(
				() -> _getChannelProductAttachmentsPage(
					null, cpDefinition, password, user1));
		}
	}

	private void _testGetChannelProductAttachmentsPageWithChannelFilter()
		throws Exception {

		CPDefinition cpDefinition = _addCPDefinition(
			CPAttachmentFileEntryConstants.TYPE_OTHER);

		_cpDefinitionLocalService.updateCPDefinitionChannelFilter(
			cpDefinition.getCPDefinitionId(), true);

		CommerceChannel commerceChannel1 = CommerceTestUtil.addCommerceChannel(
			RandomTestUtil.randomString());

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			commerceChannel1.getCommerceChannelId(), _serviceContext);

		_assertNotFound(
			() -> attachmentResource.getChannelProductAttachmentsPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null, null));

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(), _serviceContext);

		Page<Attachment> attachmentsPage =
			attachmentResource.getChannelProductAttachmentsPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null, null);

		Assert.assertEquals(1, attachmentsPage.getTotalCount());
	}

	private void _testGetChannelProductImagesPageWithAccountGroupFilter()
		throws Exception {

		try (GroupConfigurationTemporarySwapper
				groupConfigurationTemporarySwapper =
					new GroupConfigurationTemporarySwapper(
						_commerceChannel.getGroupId(),
						CommerceConstants.SERVICE_NAME_COMMERCE_ACCOUNT,
						HashMapDictionaryBuilder.<String, Object>put(
							"commerceSiteType",
							CommerceChannelConstants.SITE_TYPE_B2B
						).build())) {

			CPDefinition cpDefinition = _addCPDefinition(
				CPAttachmentFileEntryConstants.TYPE_IMAGE);

			_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
				cpDefinition.getCPDefinitionId(), true);

			String password = RandomTestUtil.randomString();

			User user1 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry1 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user1.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			AccountGroup accountGroup =
				CommerceAccountTestUtil.addAccountGroupAndAccountRel(
					RandomTestUtil.randomString(),
					AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
					accountEntry1.getAccountEntryId(), _serviceContext);

			_accountGroupRelLocalService.addAccountGroupRel(
				accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
				cpDefinition.getCPDefinitionId());

			Page<Attachment> imagesPage = _getChannelProductImagesPage(
				null, cpDefinition, password, user1);

			Assert.assertEquals(1, imagesPage.getTotalCount());

			User user2 = UserTestUtil.addUser(testCompany, password);

			AccountEntry accountEntry2 =
				CommerceAccountTestUtil.addBusinessAccountEntry(
					user2.getUserId(), RandomTestUtil.randomString(),
					RandomTestUtil.randomString() + "@liferay.com",
					_serviceContext);

			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry2.getAccountEntryId(), _serviceContext);

			_assertNotFound(
				() -> _getChannelProductImagesPage(
					null, cpDefinition, password, user2));

			User user3 = UserTestUtil.addUser(testCompany, password);

			_assertNotFound(
				() -> _getChannelProductImagesPage(
					null, cpDefinition, password, user3));

			imagesPage = _getChannelProductImagesPage(
				accountEntry1.getAccountEntryId(), cpDefinition, password,
				user1);

			Assert.assertEquals(1, imagesPage.getTotalCount());

			_accountEntryLocalService.updateStatus(
				accountEntry1.getAccountEntryId(),
				WorkflowConstants.STATUS_INACTIVE);

			_assertNotFound(
				() -> _getChannelProductImagesPage(
					accountEntry1.getAccountEntryId(), cpDefinition, password,
					user1));
			_assertNotFound(
				() -> _getChannelProductImagesPage(
					null, cpDefinition, password, user1));
		}
	}

	private void _testGetChannelProductImagesPageWithChannelFilter()
		throws Exception {

		CPDefinition cpDefinition = _addCPDefinition(
			CPAttachmentFileEntryConstants.TYPE_IMAGE);

		_cpDefinitionLocalService.updateCPDefinitionChannelFilter(
			cpDefinition.getCPDefinitionId(), true);

		CommerceChannel commerceChannel1 = CommerceTestUtil.addCommerceChannel(
			RandomTestUtil.randomString());

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			commerceChannel1.getCommerceChannelId(), _serviceContext);

		_assertNotFound(
			() -> attachmentResource.getChannelProductImagesPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null, null));

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(), _serviceContext);

		Page<Attachment> imagesPage =
			attachmentResource.getChannelProductImagesPage(
				_commerceChannel.getCommerceChannelId(),
				cpDefinition.getCProductId(), null, null);

		Assert.assertEquals(1, imagesPage.getTotalCount());
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	@DeleteAfterTestRun
	private final List<CPAttachmentFileEntry> _attachmentFileEntries =
		new ArrayList<>();

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@Inject
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}
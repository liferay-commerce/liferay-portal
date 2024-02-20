/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.object.notification.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.engine.CommerceOrderEngine;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.notification.model.NotificationQueueEntry;
import com.liferay.notification.model.NotificationRecipient;
import com.liferay.notification.model.NotificationRecipientSetting;
import com.liferay.notification.rest.dto.v1_0.NotificationTemplate;
import com.liferay.notification.rest.resource.v1_0.NotificationTemplateResource;
import com.liferay.notification.service.NotificationQueueEntryLocalService;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.OrganizationTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.io.InputStream;

import java.util.List;
import java.util.Objects;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luca Pellizzon
 */
@RunWith(Arquillian.class)
@Sync
public class CommerceObjectNotificationTermEvaluatorTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Group group = GroupTestUtil.addGroup();

		_company = CompanyLocalServiceUtil.getCompany(group.getCompanyId());

		_user = UserTestUtil.addUser(_company);

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), _commerceCurrency.getCode());

		_organization = OrganizationTestUtil.addOrganization();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			group.getGroupId());

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com",
			RandomTestUtil.randomString(), new long[] {_user.getUserId()},
			new long[] {_organization.getOrganizationId()}, _serviceContext);

		_salesAgentUser = UserTestUtil.addUser(
			_company.getCompanyId(), _user.getUserId(), "password",
			"sales.agent@liferay.com", "SalesAgent", LocaleUtil.US, "Sales",
			"Agent", null, _serviceContext);

		String salesAgentRoleName = "Sales Agent";

		Role salesAgentRole = _roleLocalService.fetchRole(
			_commerceChannel.getCompanyId(), salesAgentRoleName);

		if (salesAgentRole == null) {
			salesAgentRole = _addSalesAgentRole(salesAgentRoleName);
		}

		_roleLocalService.addUserRole(
			_salesAgentUser.getUserId(), salesAgentRole);

		_organizationLocalService.addUserOrganization(
			_salesAgentUser.getUserId(),
			GetterUtil.getLong(_organization.getOrganizationId()));
	}

	@Test
	public void testSalesAgentNotificationTermEvaluator() throws Exception {
		frutillaRule.scenario(
			"Sales Agent to Receive Order Notifications for their Accounts " +
				"from Objects framework"
		).given(
			"A notification template has the [%SALES_AGENT%] term"
		).and(
			"The template is linked to an CommerceOrder object action"
		).and(
			"The object action trigger is the order status change"
		).and(
			"There is a sales agent linked to the account placing the order"
		).when(
			"The order is checked out"
		).then(
			"A new notification is sent (added to the queue)"
		);

		int notificationQueueEntriesCount =
			_notificationQueueEntryLocalService.
				getNotificationQueueEntriesCount();

		Assert.assertEquals(0, notificationQueueEntriesCount);

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(_user));

		NotificationTemplate notificationTemplate = _addNotificationTemplate();

		_addObjectAction(notificationTemplate);

		PrincipalThreadLocal.setName(_user.getUserId());

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_commerceChannel.getSiteGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		commerceOrder = CommerceTestUtil.addCheckoutDetailsToCommerceOrder(
			commerceOrder, commerceOrder.getUserId(), false);

		commerceOrder = _commerceOrderEngine.checkoutCommerceOrder(
			commerceOrder, _user.getUserId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, commerceOrder.getStatus());

		notificationQueueEntriesCount =
			_notificationQueueEntryLocalService.
				getNotificationQueueEntriesCount();

		Assert.assertEquals(1, notificationQueueEntriesCount);

		List<NotificationQueueEntry> notificationQueueEntries =
			_notificationQueueEntryLocalService.getNotificationQueueEntries(
				0, 5);

		NotificationQueueEntry notificationQueueEntry =
			notificationQueueEntries.get(0);

		NotificationRecipient notificationRecipient =
			notificationQueueEntry.getNotificationRecipient();

		Assert.assertEquals(
			_salesAgentUser.getUserId(), notificationRecipient.getUserId());

		for (NotificationRecipientSetting notificationRecipientSetting :
				notificationRecipient.getNotificationRecipientSettings()) {

			if (Objects.equals(notificationRecipientSetting.getName(), "to")) {
				Assert.assertEquals(
					_salesAgentUser.getEmailAddress(),
					notificationRecipientSetting.getValue());
			}
		}
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private NotificationTemplate _addNotificationTemplate() throws Exception {
		Class<CommerceObjectNotificationTermEvaluatorTest> clazz =
			CommerceObjectNotificationTermEvaluatorTest.class;

		InputStream inputStream = clazz.getResourceAsStream(
			"dependencies/notification-template.json");

		String json = StringUtil.read(inputStream);

		JSONObject notificationTemplateJSONObject =
			_jsonFactory.createJSONObject(json);

		notificationTemplateJSONObject.put(
			"body", _jsonFactory.createJSONObject());

		NotificationTemplate notificationTemplate = NotificationTemplate.toDTO(
			notificationTemplateJSONObject.toString());

		NotificationTemplateResource.Builder
			notificationTemplateResourceBuilder =
				_notificationTemplateResourceFactory.create();

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		RoleTestUtil.addResourcePermission(
			role, "com.liferay.notification.template",
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(_company.getCompanyId()),
			"ADD_NOTIFICATION_TEMPLATE");

		UserLocalServiceUtil.addRoleUser(role.getRoleId(), _user.getUserId());

		NotificationTemplateResource notificationTemplateResource =
			notificationTemplateResourceBuilder.user(
				_user
			).build();

		return notificationTemplateResource.postNotificationTemplate(
			notificationTemplate);
	}

	private void _addObjectAction(NotificationTemplate notificationTemplate)
		throws Exception {

		ObjectDefinition commerceOrderObjectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				_company.getCompanyId(), "CommerceOrder");

		UnicodeProperties unicodeProperties =
			new UnicodePropertiesBuilder.UnicodePropertiesWrapper(
			).put(
				"notificationTemplateExternalReferenceCode",
				notificationTemplate.getExternalReferenceCode()
			).build();

		_objectActionLocalService.addObjectAction(
			RandomTestUtil.randomString(), _user.getUserId(),
			commerceOrderObjectDefinition.getObjectDefinitionId(), true,
			StringPool.BLANK, RandomTestUtil.randomString(),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			RandomTestUtil.randomString(),
			ObjectActionExecutorConstants.KEY_NOTIFICATION,
			"liferay/commerce_order_status", unicodeProperties, false);
	}

	private Role _addSalesAgentRole(String salesAgentRoleName)
		throws Exception {

		Role salesAgentRole = _roleLocalService.addRole(
			_serviceContext.getUserId(), null, 0, salesAgentRoleName,
			HashMapBuilder.put(
				_serviceContext.getLocale(), salesAgentRoleName
			).build(),
			null, 1, null, _serviceContext);

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(),
			"com.liferay.account.model.AccountEntry", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "MANAGE_ORGANIZATIONS");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(),
			"com.liferay.account.model.AccountEntry", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "MANAGE_USERS");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(),
			"com.liferay.account.model.AccountRole", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "VIEW");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(), "com.liferay.commerce.order", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "ADD_COMMERCE_ORDER");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(), "com.liferay.commerce.order", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "CHECKOUT_OPEN_COMMERCE_ORDERS");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(), "com.liferay.commerce.order", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "MANAGE_QUOTES");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(), "com.liferay.commerce.order", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "VIEW_OPEN_COMMERCE_ORDERS");

		_resourcePermissionLocalService.addResourcePermission(
			_serviceContext.getCompanyId(),
			"com.liferay.portal.kernel.model.Organization", 1,
			String.valueOf(salesAgentRole.getCompanyId()),
			salesAgentRole.getRoleId(), "MANAGE_AVAILABLE_ACCOUNTS");

		return salesAgentRole;
	}

	private static Company _company;
	private static User _user;

	private AccountEntry _accountEntry;
	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelLocalService _commerceChannelLocalService;

	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceOrderEngine _commerceOrderEngine;

	@Inject
	private JSONFactory _jsonFactory;

	@Inject
	private NotificationQueueEntryLocalService
		_notificationQueueEntryLocalService;

	@Inject
	private NotificationTemplateResource.Factory
		_notificationTemplateResourceFactory;

	@Inject
	private ObjectActionLocalService _objectActionLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private Organization _organization;

	@Inject
	private OrganizationLocalService _organizationLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	private User _salesAgentUser;
	private ServiceContext _serviceContext;

}
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.channel.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.configuration.CommerceOrderCheckoutConfiguration;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.constants.CPActionKeys;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelRel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.Channel;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.Creator;
import com.liferay.headless.commerce.admin.channel.client.pagination.Page;
import com.liferay.headless.commerce.admin.channel.client.pagination.Pagination;
import com.liferay.headless.commerce.admin.channel.client.problem.Problem;
import com.liferay.headless.commerce.admin.channel.client.resource.v1_0.ChannelResource;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class ChannelResourceTest extends BaseChannelResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			testGroup.getCompanyId());

		_group = GroupTestUtil.addGroup();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());
	}

	@Override
	@Test
	public void testGetChannelsPage() throws Exception {
		super.testGetChannelsPage();

		_testGetChannelsPageWithFilter();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetChannelsPage() throws Exception {
		super.testGraphQLGetChannelsPage();
	}

	@FeatureFlag("LPD-109588")
	@Override
	@Test
	public void testPatchChannel() throws Exception {
		super.testPatchChannel();

		_testPatchChannelWithAccountExternalReferenceCode();
		_testPatchChannelWithGuestSettings();
		_testPatchChannelWithSiteGroupId();
		_testPatchChannelWithoutGuestSettings();
	}

	@FeatureFlag("LPD-109588")
	@Override
	@Test
	public void testPatchChannelByExternalReferenceCode() throws Exception {
		super.testPatchChannelByExternalReferenceCode();

		_testPatchChannelByExternalReferenceCodeWithAccountExternalReferenceCode();
		_testPatchChannelByExternalReferenceCodeWithGuestSettings();
		_testPatchChannelByExternalReferenceCodeWithSiteGroupId();
		_testPatchChannelByExternalReferenceCodeWithoutGuestSettings();
	}

	@FeatureFlag("LPD-109588")
	@Override
	@Test
	public void testPostChannel() throws Exception {
		super.testPostChannel();

		_testPostChannelWithAccountExternalReferenceCode();
		_testPostChannelWithCreator();
		_testPostChannelWithGuestSettings();
		_testPostChannelWithSiteGroupId();
		_testPostChannelWithoutGuestSettings();
	}

	@Override
	@Test
	public void testPutChannel() throws Exception {
		super.testPutChannel();

		_testPutChannelWithAccountExternalReferenceCode();
		_testPutChannelWithCommerceChannelSettings();
		_testPutChannelWithGuestSettings();
		_testPutChannelWithSiteGroupId();
		_testPutChannelWithoutGuestSettings();
		_testPutChannelWithoutSiteGroupId();
	}

	@FeatureFlag("LPD-109588")
	@Override
	@Test
	public void testPutChannelByExternalReferenceCode() throws Exception {
		super.testPutChannelByExternalReferenceCode();

		_testPutChannelByExternalReferenceCodeWithAccountExternalReferenceCode();
		_testPutChannelByExternalReferenceCodeWithCommerceChannelSettings();
		_testPutChannelByExternalReferenceCodeWithGuestSettings();
		_testPutChannelByExternalReferenceCodeWithSiteGroupId();
		_testPutChannelByExternalReferenceCodeWithoutGuestSettings();
		_testPutChannelByExternalReferenceCodeWithoutUpdatePermission();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"currencyCode", "name", "siteGroupId", "type"};
	}

	@Override
	protected Channel randomChannel() throws Exception {
		return new Channel() {
			{
				accountId = AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT;
				currencyCode = _commerceCurrency.getCode();
				currencyExternalReferenceCode =
					_commerceCurrency.getExternalReferenceCode();
				currencyId = _commerceCurrency.getCommerceCurrencyId();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				siteGroupId = testGroup.getGroupId();
				type = CommerceChannelConstants.CHANNEL_TYPE_SITE;
			}
		};
	}

	@Override
	protected Channel randomPatchChannel() throws Exception {
		return new Channel() {
			{
				accountId = AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT;
				currencyCode = _commerceCurrency.getCode();
				currencyExternalReferenceCode =
					_commerceCurrency.getExternalReferenceCode();
				currencyId = _commerceCurrency.getCommerceCurrencyId();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				siteGroupId = _group.getGroupId();
				type = CommerceChannelConstants.CHANNEL_TYPE_SITE;
			}
		};
	}

	@Override
	protected Channel testDeleteChannel_addChannel() throws Exception {
		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testDeleteChannelByExternalReferenceCode_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testGetAccountAddressChannelChannel_addChannel()
		throws Exception {

		if (_commerceChannelRel == null) {
			_getCommerceChannelRelId();
		}

		return _toChannel(
			_commerceChannelLocalService.getCommerceChannel(
				_commerceChannelRel.getCommerceChannelId()));
	}

	@Override
	protected Long
			testGetAccountAddressChannelChannel_getAccountAddressChannelId()
		throws Exception {

		return _getCommerceChannelRelId();
	}

	@Override
	protected Channel testGetChannel_addChannel() throws Exception {
		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testGetChannelByExternalReferenceCode_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testGetChannelPermissionsPage_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testGetChannelsPage_addChannel(Channel channel)
		throws Exception {

		return channelResource.postChannel(channel);
	}

	@Override
	protected Channel testGraphQLChannel_addChannel() throws Exception {
		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testGraphQLChannel_addChannel(Channel channel)
		throws Exception {

		return channelResource.postChannel(channel);
	}

	@Override
	protected Channel testGraphQLGetAccountAddressChannelChannel_addChannel()
		throws Exception {

		return testGetAccountAddressChannelChannel_addChannel();
	}

	@Override
	protected Long
			testGraphQLGetAccountAddressChannelChannel_getAccountAddressChannelId()
		throws Exception {

		return _getCommerceChannelRelId();
	}

	@Override
	protected Channel testPatchChannel_addChannel() throws Exception {
		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testPatchChannelByExternalReferenceCode_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testPostChannel_addChannel(Channel channel)
		throws Exception {

		return channelResource.postChannel(channel);
	}

	@Override
	protected Channel testPostChannel_addPermissionsChannel(Channel channel)
		throws Exception {

		return permissionsChannelResource.postChannel(channel);
	}

	@Override
	protected Channel testPutChannel_addChannel() throws Exception {
		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testPutChannelByExternalReferenceCode_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	@Override
	protected Channel testPutChannelPermissionsPage_addChannel()
		throws Exception {

		return channelResource.postChannel(randomChannel());
	}

	private AccountEntry _addAccountEntry(String type) throws Exception {
		return _accountEntryLocalService.addAccountEntry(
			StringPool.BLANK, _user.getUserId(), 0,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null,
			RandomTestUtil.randomString(), type,
			WorkflowConstants.STATUS_APPROVED, _serviceContext);
	}

	private long _getCommerceChannelRelId() throws Exception {
		if (_accountEntry == null) {
			_accountEntry = _addAccountEntry(
				AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS);
		}

		if (_address == null) {
			_address = _addressLocalService.addAddress(
				RandomTestUtil.randomString(), _user.getUserId(),
				AccountEntry.class.getName(), _accountEntry.getAccountEntryId(),
				0, 0, 0, RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), false,
				RandomTestUtil.randomString(), false,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString(), null,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				_serviceContext);
		}

		Channel channel = channelResource.postChannel(randomChannel());

		if (_commerceChannelRel == null) {
			_commerceChannelRel =
				_commerceChannelRelLocalService.addCommerceChannelRel(
					Address.class.getName(), _address.getAddressId(),
					channel.getId(), _serviceContext);
		}

		return _commerceChannelRel.getCommerceChannelRelId();
	}

	private String _getGraphQLMutationErrorMessage(
			String name, Map<String, Object> arguments)
		throws Exception {

		JSONObject jsonObject = invokeGraphQLMutation(
			new GraphQLField(
				"headlessCommerceAdminChannel_v1_0",
				new GraphQLField(name, arguments, new GraphQLField("id"))));

		return JSONUtil.getValueAsString(
			jsonObject, "JSONArray/errors", "Object/0", "Object/message");
	}

	private void _testGetChannelsPageWithFilter() throws Exception {
		Channel randomChannel = randomChannel();

		randomChannel.setExternalReferenceCode(
			"ERC-" + RandomTestUtil.randomString());

		Channel channel = testGetChannelsPage_addChannel(randomChannel);

		_commerceChannels.add(
			_commerceChannelLocalService.getCommerceChannel(channel.getId()));

		Page<Channel> channelsPage = channelResource.getChannelsPage(
			null,
			"externalReferenceCode eq '" + channel.getExternalReferenceCode() +
				"'",
			Pagination.of(1, 10), null);

		Assert.assertEquals(1, channelsPage.getTotalCount());

		assertEquals(
			Collections.singletonList(channel),
			(List<Channel>)channelsPage.getItems());

		channelsPage = channelResource.getChannelsPage(
			null, "id eq " + channel.getId(), Pagination.of(1, 10), null);

		Assert.assertEquals(1, channelsPage.getTotalCount());

		assertEquals(
			Collections.singletonList(channel),
			(List<Channel>)channelsPage.getItems());

		Channel notFilteredChannel = testGetChannelsPage_addChannel(
			randomChannel());

		_commerceChannels.add(
			_commerceChannelLocalService.getCommerceChannel(
				notFilteredChannel.getId()));

		channelsPage = channelResource.getChannelsPage(
			null, null, Pagination.of(1, 10), null);

		long totalCount = channelsPage.getTotalCount();

		channelsPage = channelResource.getChannelsPage(
			null, "not (id in (" + channel.getId() + "))", Pagination.of(1, -1),
			null);

		Assert.assertEquals(totalCount - 1, channelsPage.getTotalCount());

		assertContains(
			notFilteredChannel, (List<Channel>)channelsPage.getItems());
	}

	private void _testPatchChannelByExternalReferenceCodeWithAccountExternalReferenceCode()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomPatchChannel = randomPatchChannel();

		AccountEntry accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		randomPatchChannel.setAccountId(0L);
		randomPatchChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		Channel patchChannel =
			channelResource.patchChannelByExternalReferenceCode(
				postChannel.getExternalReferenceCode(), randomPatchChannel);

		randomPatchChannel.setAccountId(accountEntry.getAccountEntryId());

		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(patchChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			patchChannel.getAccountExternalReferenceCode());

		Channel expectedPatchChannel = postChannel.clone();

		BeanTestUtil.copyProperties(randomPatchChannel, expectedPatchChannel);

		Channel getChannel = channelResource.getChannelByExternalReferenceCode(
			patchChannel.getExternalReferenceCode());

		assertEquals(expectedPatchChannel, getChannel);
		assertValid(getChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(getChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			getChannel.getAccountExternalReferenceCode());
	}

	private void _testPatchChannelByExternalReferenceCodeWithGuestSettings()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Assert.assertTrue(postChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

		Channel patchChannel =
			channelResource.patchChannelByExternalReferenceCode(
				postChannel.getExternalReferenceCode(),
				new Channel() {
					{
						guestCheckoutEnabled = true;
					}
				});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(patchChannel.getGuestCheckoutEnabled());

		patchChannel = channelResource.patchChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(),
			new Channel() {
				{
					guestBrowsingEnabled = false;
				}
			});

		Assert.assertFalse(patchChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(patchChannel.getGuestCheckoutEnabled());

		assertHttpResponseStatusCode(
			400,
			channelResource.patchChannelByExternalReferenceCodeHttpResponse(
				postChannel.getExternalReferenceCode(),
				new Channel() {
					{
						guestCheckoutEnabled = true;
					}
				}));

		Assert.assertEquals(
			"Exception while fetching data " +
				"(/headlessCommerceAdminChannel_v1_0/patchChannel" +
					"ByExternalReferenceCode) : null",
			_getGraphQLMutationErrorMessage(
				"patchChannelByExternalReferenceCode",
				HashMapBuilder.<String, Object>put(
					"channel",
					StringBundler.concat(
						"{currencyCode: \"", _commerceCurrency.getCode(),
						"\", guestCheckoutEnabled: true, name: \"",
						RandomTestUtil.randomString(), "\", type: \"",
						CommerceChannelConstants.CHANNEL_TYPE_SITE, "\"}")
				).put(
					"externalReferenceCode",
					"\"" + postChannel.getExternalReferenceCode() + "\""
				).build()));

		Channel getChannel = channelResource.getChannel(postChannel.getId());

		Assert.assertFalse(getChannel.getGuestCheckoutEnabled());
		Assert.assertEquals(patchChannel.getName(), getChannel.getName());

		patchChannel = channelResource.patchChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(),
			new Channel() {
				{
					guestBrowsingEnabled = true;
					guestCheckoutEnabled = true;
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(patchChannel.getGuestCheckoutEnabled());
	}

	private void _testPatchChannelByExternalReferenceCodeWithSiteGroupId()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomPatchChannel = randomPatchChannel();

		randomPatchChannel.setSiteGroupId(RandomTestUtil.nextLong());

		assertHttpResponseStatusCode(
			400,
			channelResource.patchChannelByExternalReferenceCodeHttpResponse(
				postChannel.getExternalReferenceCode(), randomPatchChannel));

		Channel getChannel = channelResource.getChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode());

		Assert.assertEquals(
			postChannel.getSiteGroupId(), getChannel.getSiteGroupId());
	}

	private void _testPatchChannelByExternalReferenceCodeWithoutGuestSettings()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		channelResource.patchChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(),
			new Channel() {
				{
					name = StringUtil.toLowerCase(
						RandomTestUtil.randomString());
				}
			});

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestCheckoutEnabled());
		}
	}

	private void _testPatchChannelWithAccountExternalReferenceCode()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomPatchChannel = randomPatchChannel();

		AccountEntry accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		randomPatchChannel.setAccountId(0L);
		randomPatchChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		Channel patchChannel = channelResource.patchChannel(
			postChannel.getId(), randomPatchChannel);

		randomPatchChannel.setAccountId(accountEntry.getAccountEntryId());

		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(patchChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			patchChannel.getAccountExternalReferenceCode());

		Channel expectedPatchChannel = postChannel.clone();

		BeanTestUtil.copyProperties(randomPatchChannel, expectedPatchChannel);

		Channel getChannel = channelResource.getChannel(patchChannel.getId());

		assertEquals(expectedPatchChannel, getChannel);
		assertValid(getChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(getChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			getChannel.getAccountExternalReferenceCode());
	}

	private void _testPatchChannelWithGuestSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		Assert.assertTrue(postChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

		Channel patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestCheckoutEnabled = true;
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(patchChannel.getGuestCheckoutEnabled());

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					name = StringUtil.toLowerCase(
						RandomTestUtil.randomString());
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(patchChannel.getGuestCheckoutEnabled());

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestBrowsingEnabled = false;
				}
			});

		Assert.assertFalse(patchChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(patchChannel.getGuestCheckoutEnabled());

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestCheckoutEnabled = false;
				}
			});

		Assert.assertFalse(patchChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(patchChannel.getGuestCheckoutEnabled());

		try {
			channelResource.patchChannel(
				postChannel.getId(),
				new Channel() {
					{
						guestCheckoutEnabled = true;
					}
				});

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("BAD_REQUEST", problem.getStatus());
			Assert.assertEquals(
				"Guest checkout cannot be enabled while guest browsing is " +
					"disabled.",
				problem.getTitle());
		}

		Assert.assertEquals(
			"Exception while fetching data " +
				"(/headlessCommerceAdminChannel_v1_0/patchChannel) : null",
			_getGraphQLMutationErrorMessage(
				"patchChannel",
				HashMapBuilder.<String, Object>put(
					"channel",
					StringBundler.concat(
						"{currencyCode: \"", _commerceCurrency.getCode(),
						"\", guestCheckoutEnabled: true, name: \"",
						RandomTestUtil.randomString(), "\", type: \"",
						CommerceChannelConstants.CHANNEL_TYPE_SITE, "\"}")
				).put(
					"channelId", postChannel.getId()
				).build()));

		Channel getChannel = channelResource.getChannel(postChannel.getId());

		Assert.assertFalse(getChannel.getGuestCheckoutEnabled());
		Assert.assertEquals(patchChannel.getName(), getChannel.getName());

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestBrowsingEnabled = true;
					guestCheckoutEnabled = true;
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(patchChannel.getGuestCheckoutEnabled());

		assertHttpResponseStatusCode(
			400,
			channelResource.patchChannelHttpResponse(
				postChannel.getId(),
				new Channel() {
					{
						guestBrowsingEnabled = false;
						guestCheckoutEnabled = true;
					}
				}));

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestCheckoutEnabled = false;
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(patchChannel.getGuestCheckoutEnabled());

		patchChannel = channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					guestBrowsingEnabled = true;
				}
			});

		Assert.assertTrue(patchChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(patchChannel.getGuestCheckoutEnabled());
	}

	private void _testPatchChannelWithSiteGroupId() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomPatchChannel = randomPatchChannel();

		randomPatchChannel.setSiteGroupId(RandomTestUtil.nextLong());

		assertHttpResponseStatusCode(
			400,
			channelResource.patchChannelHttpResponse(
				postChannel.getId(), randomPatchChannel));

		Channel getChannel = channelResource.getChannel(postChannel.getId());

		Assert.assertEquals(
			postChannel.getSiteGroupId(), getChannel.getSiteGroupId());
	}

	private void _testPatchChannelWithoutGuestSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		channelResource.patchChannel(
			postChannel.getId(),
			new Channel() {
				{
					name = StringUtil.toLowerCase(
						RandomTestUtil.randomString());
				}
			});

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestCheckoutEnabled());
		}
	}

	private void _testPostChannelWithAccountExternalReferenceCode()
		throws Exception {

		Channel randomChannel = randomChannel();

		AccountEntry accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		randomChannel.setAccountId(0L);
		randomChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		Channel postChannel = channelResource.postChannel(randomChannel);

		randomChannel.setAccountId(accountEntry.getAccountEntryId());

		assertEquals(randomChannel, postChannel);
		assertValid(postChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(postChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			postChannel.getAccountExternalReferenceCode());
	}

	private void _testPostChannelWithCreator() throws Exception {
		String password = RandomTestUtil.randomString();
		User user = UserTestUtil.addOmniadminUser();

		_userLocalService.updatePassword(
			user.getUserId(), password, password, false, true);

		ChannelResource channelResource = ChannelResource.builder(
		).authentication(
			user.getEmailAddress(), password
		).locale(
			LocaleUtil.getDefault()
		).parameters(
			"nestedFields", "creator"
		).build();

		Channel postChannel = channelResource.postChannel(randomChannel());

		Creator creator = postChannel.getCreator();

		Assert.assertEquals(
			user.getExternalReferenceCode(),
			creator.getExternalReferenceCode());
	}

	private void _testPostChannelWithGuestSettings() throws Exception {
		Channel randomChannel = randomChannel();

		randomChannel.setGuestBrowsingEnabled(false);
		randomChannel.setGuestCheckoutEnabled(true);

		assertHttpResponseStatusCode(
			400, channelResource.postChannelHttpResponse(randomChannel));

		Assert.assertEquals(
			"Exception while fetching data " +
				"(/headlessCommerceAdminChannel_v1_0/createChannel) : null",
			_getGraphQLMutationErrorMessage(
				"createChannel",
				HashMapBuilder.<String, Object>put(
					"channel",
					StringBundler.concat(
						"{currencyCode: \"", _commerceCurrency.getCode(),
						"\", externalReferenceCode: \"",
						randomChannel.getExternalReferenceCode(),
						"\", guestBrowsingEnabled: false, ",
						"guestCheckoutEnabled: true, name: \"",
						RandomTestUtil.randomString(), "\", type: \"",
						CommerceChannelConstants.CHANNEL_TYPE_SITE, "\"}")
				).build()));

		assertHttpResponseStatusCode(
			404,
			channelResource.getChannelByExternalReferenceCodeHttpResponse(
				randomChannel.getExternalReferenceCode()));

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			randomChannel.setGuestCheckoutEnabled((Boolean)null);

			Channel postChannel = channelResource.postChannel(randomChannel);

			Assert.assertFalse(postChannel.getGuestBrowsingEnabled());
			Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

			randomChannel = randomChannel();

			randomChannel.setGuestBrowsingEnabled(false);
			randomChannel.setGuestCheckoutEnabled(false);

			postChannel = channelResource.postChannel(randomChannel);

			Assert.assertFalse(postChannel.getGuestBrowsingEnabled());
			Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

			randomChannel = randomChannel();

			randomChannel.setGuestBrowsingEnabled(true);
			randomChannel.setGuestCheckoutEnabled(false);

			postChannel = channelResource.postChannel(randomChannel);

			Assert.assertTrue(postChannel.getGuestBrowsingEnabled());
			Assert.assertFalse(postChannel.getGuestCheckoutEnabled());
		}

		randomChannel = randomChannel();

		randomChannel.setGuestCheckoutEnabled(true);

		Channel postChannel = channelResource.postChannel(randomChannel);

		Assert.assertTrue(postChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(postChannel.getGuestCheckoutEnabled());
	}

	private void _testPostChannelWithSiteGroupId() throws Exception {
		Channel randomChannel = randomChannel();

		randomChannel.setSiteGroupId(RandomTestUtil.nextLong());

		assertHttpResponseStatusCode(
			400, channelResource.postChannelHttpResponse(randomChannel));
		assertHttpResponseStatusCode(
			404,
			channelResource.getChannelByExternalReferenceCodeHttpResponse(
				randomChannel.getExternalReferenceCode()));
	}

	private void _testPostChannelWithoutGuestSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestCheckoutEnabled());
		}
	}

	private void _testPutChannelByExternalReferenceCodeWithAccountExternalReferenceCode()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomChannel = randomChannel();

		AccountEntry accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		randomChannel.setAccountId(0L);
		randomChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		Channel putChannel = channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel);

		randomChannel.setAccountId(accountEntry.getAccountEntryId());

		assertEquals(randomChannel, putChannel);
		assertValid(putChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(putChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			putChannel.getAccountExternalReferenceCode());

		Channel getChannel = channelResource.getChannelByExternalReferenceCode(
			putChannel.getExternalReferenceCode());

		assertEquals(randomChannel, getChannel);
		assertValid(getChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(getChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			getChannel.getAccountExternalReferenceCode());

		Channel newChannel =
			testPutChannelByExternalReferenceCode_createChannel();

		accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		newChannel.setAccountId(0L);
		newChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		putChannel = channelResource.putChannelByExternalReferenceCode(
			newChannel.getExternalReferenceCode(), newChannel);

		newChannel.setAccountId(accountEntry.getAccountEntryId());

		assertEquals(newChannel, putChannel);
		assertValid(putChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(putChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			putChannel.getAccountExternalReferenceCode());

		getChannel = channelResource.getChannelByExternalReferenceCode(
			putChannel.getExternalReferenceCode());

		assertEquals(newChannel, getChannel);

		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(getChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			getChannel.getAccountExternalReferenceCode());

		Assert.assertEquals(
			newChannel.getExternalReferenceCode(),
			putChannel.getExternalReferenceCode());
	}

	private void _testPutChannelByExternalReferenceCodeWithCommerceChannelSettings()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(
				postChannel.getId());

		commerceChannel = _commerceChannelLocalService.updateCommerceChannel(
			commerceChannel.getCommerceChannelId(),
			commerceChannel.getAccountEntryId(),
			commerceChannel.getSiteGroupId(), commerceChannel.getName(),
			commerceChannel.getType(),
			UnicodePropertiesBuilder.put(
				RandomTestUtil.randomString(), RandomTestUtil.randomString()
			).build(),
			commerceChannel.getCommerceCurrencyCode(),
			RandomTestUtil.randomString(), true);

		channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel());

		CommerceChannel putCommerceChannel =
			_commerceChannelLocalService.getCommerceChannel(
				postChannel.getId());

		Assert.assertTrue(putCommerceChannel.isDiscountsTargetNetPrice());
		Assert.assertEquals(
			commerceChannel.getPriceDisplayType(),
			putCommerceChannel.getPriceDisplayType());
		Assert.assertEquals(
			commerceChannel.getTypeSettings(),
			putCommerceChannel.getTypeSettings());
	}

	private void _testPutChannelByExternalReferenceCodeWithGuestSettings()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Assert.assertTrue(postChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

		Channel randomChannel = randomChannel();

		randomChannel.setGuestCheckoutEnabled(true);

		Channel putChannel = channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel);

		Assert.assertTrue(putChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(putChannel.getGuestCheckoutEnabled());

		randomChannel = randomChannel();

		randomChannel.setGuestBrowsingEnabled(false);

		putChannel = channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel);

		Assert.assertFalse(putChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(putChannel.getGuestCheckoutEnabled());

		randomChannel = randomChannel();

		randomChannel.setGuestCheckoutEnabled(true);

		assertHttpResponseStatusCode(
			400,
			channelResource.putChannelByExternalReferenceCodeHttpResponse(
				postChannel.getExternalReferenceCode(), randomChannel));

		Assert.assertEquals(
			"Exception while fetching data " +
				"(/headlessCommerceAdminChannel_v1_0/updateChannel" +
					"ByExternalReferenceCode) : null",
			_getGraphQLMutationErrorMessage(
				"updateChannelByExternalReferenceCode",
				HashMapBuilder.<String, Object>put(
					"channel",
					StringBundler.concat(
						"{currencyCode: \"", _commerceCurrency.getCode(),
						"\", guestCheckoutEnabled: true, name: \"",
						RandomTestUtil.randomString(), "\", type: \"",
						CommerceChannelConstants.CHANNEL_TYPE_SITE, "\"}")
				).put(
					"externalReferenceCode",
					"\"" + postChannel.getExternalReferenceCode() + "\""
				).build()));

		Channel getChannel = channelResource.getChannel(postChannel.getId());

		Assert.assertFalse(getChannel.getGuestCheckoutEnabled());
		Assert.assertEquals(putChannel.getName(), getChannel.getName());

		randomChannel = randomChannel();

		randomChannel.setGuestBrowsingEnabled(true);
		randomChannel.setGuestCheckoutEnabled(true);

		putChannel = channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel);

		Assert.assertTrue(putChannel.getGuestBrowsingEnabled());
		Assert.assertTrue(putChannel.getGuestCheckoutEnabled());

		randomChannel = randomChannel();

		randomChannel.setGuestBrowsingEnabled(false);
		randomChannel.setGuestCheckoutEnabled(true);

		assertHttpResponseStatusCode(
			400,
			channelResource.putChannelByExternalReferenceCodeHttpResponse(
				randomChannel.getExternalReferenceCode(), randomChannel));
		assertHttpResponseStatusCode(
			404,
			channelResource.getChannelByExternalReferenceCodeHttpResponse(
				randomChannel.getExternalReferenceCode()));
	}

	private void _testPutChannelByExternalReferenceCodeWithSiteGroupId()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomChannel = randomChannel();

		randomChannel.setSiteGroupId(RandomTestUtil.nextLong());

		assertHttpResponseStatusCode(
			400,
			channelResource.putChannelByExternalReferenceCodeHttpResponse(
				postChannel.getExternalReferenceCode(), randomChannel));

		Channel getChannel = channelResource.getChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode());

		Assert.assertEquals(
			postChannel.getSiteGroupId(), getChannel.getSiteGroupId());
	}

	private void _testPutChannelByExternalReferenceCodeWithoutGuestSettings()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		channelResource.putChannelByExternalReferenceCode(
			postChannel.getExternalReferenceCode(), randomChannel());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestCheckoutEnabled());
		}
	}

	private void _testPutChannelByExternalReferenceCodeWithoutUpdatePermission()
		throws Exception {

		String primKey = String.valueOf(testCompany.getCompanyId());
		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		RoleTestUtil.addResourcePermission(
			role, CPConstants.RESOURCE_NAME_CHANNEL,
			ResourceConstants.SCOPE_COMPANY, primKey,
			CPActionKeys.ADD_COMMERCE_CHANNEL);
		RoleTestUtil.addResourcePermission(
			role, CommerceChannel.class.getName(),
			ResourceConstants.SCOPE_COMPANY, primKey, ActionKeys.VIEW);

		User user = UserTestUtil.addUser(testCompany, _PASSWORD);

		_roleLocalService.addUserRoles(
			user.getUserId(), new long[] {role.getRoleId()});

		Channel postChannel = channelResource.postChannel(randomChannel());

		ChannelResource channelResource = ChannelResource.builder(
		).authentication(
			user.getEmailAddress(), _PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();

		assertHttpResponseStatusCode(
			403,
			channelResource.putChannelByExternalReferenceCodeHttpResponse(
				postChannel.getExternalReferenceCode(), randomChannel()));

		Channel newChannel =
			testPutChannelByExternalReferenceCode_createChannel();

		newChannel.setGuestCheckoutEnabled(true);

		Channel putChannel = channelResource.putChannelByExternalReferenceCode(
			newChannel.getExternalReferenceCode(), newChannel);

		Assert.assertTrue(putChannel.getGuestCheckoutEnabled());
	}

	private void _testPutChannelWithAccountExternalReferenceCode()
		throws Exception {

		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomChannel = randomChannel();

		AccountEntry accountEntry = _addAccountEntry(
			AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER);

		randomChannel.setAccountId(0L);
		randomChannel.setAccountExternalReferenceCode(
			accountEntry.getExternalReferenceCode());

		Channel putChannel = channelResource.putChannel(
			postChannel.getId(), randomChannel);

		randomChannel.setAccountId(accountEntry.getAccountEntryId());

		assertEquals(randomChannel, putChannel);
		assertValid(putChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(putChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			putChannel.getAccountExternalReferenceCode());

		Channel getChannel = channelResource.getChannel(putChannel.getId());

		assertEquals(randomChannel, getChannel);
		assertValid(getChannel);
		Assert.assertEquals(
			accountEntry.getAccountEntryId(),
			GetterUtil.getLong(getChannel.getAccountId()));
		Assert.assertEquals(
			accountEntry.getExternalReferenceCode(),
			getChannel.getAccountExternalReferenceCode());
	}

	private void _testPutChannelWithCommerceChannelSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(
				postChannel.getId());

		commerceChannel = _commerceChannelLocalService.updateCommerceChannel(
			commerceChannel.getCommerceChannelId(),
			commerceChannel.getAccountEntryId(),
			commerceChannel.getSiteGroupId(), commerceChannel.getName(),
			commerceChannel.getType(),
			UnicodePropertiesBuilder.put(
				RandomTestUtil.randomString(), RandomTestUtil.randomString()
			).build(),
			commerceChannel.getCommerceCurrencyCode(),
			RandomTestUtil.randomString(), true);

		channelResource.putChannel(postChannel.getId(), randomChannel());

		CommerceChannel putCommerceChannel =
			_commerceChannelLocalService.getCommerceChannel(
				postChannel.getId());

		Assert.assertTrue(putCommerceChannel.isDiscountsTargetNetPrice());
		Assert.assertEquals(
			commerceChannel.getPriceDisplayType(),
			putCommerceChannel.getPriceDisplayType());
		Assert.assertEquals(
			commerceChannel.getTypeSettings(),
			putCommerceChannel.getTypeSettings());
	}

	private void _testPutChannelWithGuestSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		Assert.assertNull(postChannel.getGuestBrowsingEnabled());
		Assert.assertFalse(postChannel.getGuestCheckoutEnabled());

		Channel randomChannel = randomChannel();

		randomChannel.setGuestBrowsingEnabled(false);
		randomChannel.setGuestCheckoutEnabled(true);

		Channel putChannel = channelResource.putChannel(
			postChannel.getId(), randomChannel);

		Assert.assertTrue(putChannel.getGuestCheckoutEnabled());

		putChannel = channelResource.putChannel(
			postChannel.getId(), randomChannel());

		Assert.assertTrue(putChannel.getGuestCheckoutEnabled());

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					true, "LPD-109588")) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestBrowsingEnabled());

			randomChannel = randomChannel();

			randomChannel.setGuestBrowsingEnabled(false);
			randomChannel.setGuestCheckoutEnabled(true);

			assertHttpResponseStatusCode(
				400,
				channelResource.putChannelHttpResponse(
					postChannel.getId(), randomChannel));

			randomChannel = randomChannel();

			randomChannel.setGuestBrowsingEnabled(false);

			putChannel = channelResource.putChannel(
				postChannel.getId(), randomChannel);

			Assert.assertFalse(putChannel.getGuestBrowsingEnabled());
			Assert.assertFalse(putChannel.getGuestCheckoutEnabled());

			Assert.assertEquals(
				"Exception while fetching data " +
					"(/headlessCommerceAdminChannel_v1_0/updateChannel) : null",
				_getGraphQLMutationErrorMessage(
					"updateChannel",
					HashMapBuilder.<String, Object>put(
						"channel",
						StringBundler.concat(
							"{currencyCode: \"", _commerceCurrency.getCode(),
							"\", guestCheckoutEnabled: true, name: \"",
							RandomTestUtil.randomString(), "\", type: \"",
							CommerceChannelConstants.CHANNEL_TYPE_SITE, "\"}")
					).put(
						"channelId", postChannel.getId()
					).build()));

			getChannel = channelResource.getChannel(postChannel.getId());

			Assert.assertFalse(getChannel.getGuestCheckoutEnabled());
			Assert.assertEquals(putChannel.getName(), getChannel.getName());

			randomChannel = randomChannel();

			randomChannel.setGuestCheckoutEnabled(true);

			assertHttpResponseStatusCode(
				400,
				channelResource.putChannelHttpResponse(
					postChannel.getId(), randomChannel));
		}
	}

	private void _testPutChannelWithSiteGroupId() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomChannel = randomChannel();

		randomChannel.setSiteGroupId(RandomTestUtil.nextLong());

		assertHttpResponseStatusCode(
			400,
			channelResource.putChannelHttpResponse(
				postChannel.getId(), randomChannel));

		Channel getChannel = channelResource.getChannel(postChannel.getId());

		Assert.assertEquals(
			postChannel.getSiteGroupId(), getChannel.getSiteGroupId());
	}

	private void _testPutChannelWithoutGuestSettings() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		channelResource.putChannel(postChannel.getId(), randomChannel());

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					CommerceOrderCheckoutConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"guestCheckoutEnabled", true
					).build())) {

			Channel getChannel = channelResource.getChannel(
				postChannel.getId());

			Assert.assertTrue(getChannel.getGuestCheckoutEnabled());
		}
	}

	private void _testPutChannelWithoutSiteGroupId() throws Exception {
		Channel postChannel = channelResource.postChannel(randomChannel());

		Channel randomChannel = randomChannel();

		randomChannel.setSiteGroupId((Long)null);

		Channel putChannel = channelResource.putChannel(
			postChannel.getId(), randomChannel);

		Assert.assertEquals(0, GetterUtil.getLong(putChannel.getSiteGroupId()));
	}

	private Channel _toChannel(CommerceChannel commerceChannel) {
		return new Channel() {
			{
				accountId = commerceChannel.getAccountEntryId();
				currencyCode = commerceChannel.getCommerceCurrencyCode();
				externalReferenceCode =
					commerceChannel.getExternalReferenceCode();
				id = commerceChannel.getCommerceChannelId();
				name = commerceChannel.getName();
				siteGroupId = commerceChannel.getSiteGroupId();
				type = commerceChannel.getType();
			}
		};
	}

	private static final String _PASSWORD = RandomTestUtil.randomString();

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@DeleteAfterTestRun
	private Address _address;

	@Inject
	private AddressLocalService _addressLocalService;

	@Inject
	private CommerceChannelLocalService _commerceChannelLocalService;

	@DeleteAfterTestRun
	private CommerceChannelRel _commerceChannelRel;

	@Inject
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@DeleteAfterTestRun
	private final List<CommerceChannel> _commerceChannels = new ArrayList<>();

	private CommerceCurrency _commerceCurrency;
	private Group _group;

	@Inject
	private RoleLocalService _roleLocalService;

	private ServiceContext _serviceContext;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}
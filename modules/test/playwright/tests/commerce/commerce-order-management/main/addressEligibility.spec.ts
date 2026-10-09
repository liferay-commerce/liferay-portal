/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {accountsPagesTest} from '../../../../fixtures/accountsPagesTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import getRandomString from '../../../../utils/getRandomString';
import {performUserSwitch} from '../../../../utils/performLogin';
import {checkoutStorefrontSetUp} from '../../utils/commerce';

export const test = mergeTests(
	accountsPagesTest,
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest()
);

test(
	'Address channel eligibility is managed from the eligibility tab and filters the checkout addresses',
	{
		tag: [
			'@COMMERCE-12890',
			'@COMMERCE-12893',
			'@COMMERCE-12934',
			'@COMMERCE-12958',
			'@LPD-108644',
		],
	},
	async ({
		apiHelpers,
		checkoutPage,
		commerceAccountAddressEligibilityPage,
		commerceAdminChannelsPage,
		commerceMiniCartPage,
		page,
		site,
	}) => {
		const {account, buyerUser, channel, miniCartLayout, sku} =
			await checkoutStorefrontSetUp(
				apiHelpers,
				commerceAdminChannelsPage,
				page,
				site
			);

		const otherChannel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				name: getRandomString(),
			});

		const restrictedAddress =
			await apiHelpers.headlessCommerceAdminAccount.postAddress(
				account.id,
				{
					defaultBilling: false,
					defaultShipping: false,
					name: 'Restricted Address',
					type: 2,
				}
			);

		apiHelpers.data.push({id: restrictedAddress.id, type: 'address'});

		const eligibleAddress =
			await apiHelpers.headlessCommerceAdminAccount.postAddress(
				account.id,
				{
					defaultBilling: false,
					defaultShipping: false,
					name: 'Eligible Address',
					type: 2,
				}
			);

		apiHelpers.data.push({id: eligibleAddress.id, type: 'address'});

		await apiHelpers.headlessCommerceAdminChannel.postAccountAddressChannel(
			eligibleAddress.id,
			channel.id
		);

		await test.step('Restrict an address to another channel from the eligibility tab', async () => {
			await commerceAccountAddressEligibilityPage.goto(
				account.id,
				restrictedAddress.id
			);

			await expect(
				commerceAccountAddressEligibilityPage.allChannelsRadio
			).toBeChecked();

			await commerceAccountAddressEligibilityPage.addChannel(
				otherChannel.name
			);
		});

		await test.step('Only the address eligible for the channel is offered at checkout', async () => {
			await apiHelpers.headlessCommerceDeliveryCart.postCart(
				{
					accountId: account.id,
					cartItems: [{quantity: 1, skuId: sku.id}],
				},
				channel.id
			);

			await performUserSwitch(page, buyerUser.alternateName);

			await page.goto(
				`/web${site.friendlyUrlPath}${miniCartLayout.friendlyUrlPath}`
			);

			await commerceMiniCartPage.miniCartButton.click();

			await commerceMiniCartPage.submitButton.click();

			await expect(commerceMiniCartPage.submitButton).toBeHidden();

			for (const step of ['Shipping Address', 'Billing Address']) {
				await checkoutPage.performCheckoutUntilStep(step);

				await expect(checkoutPage.commerceAddressSelect).toBeVisible();

				const commerceAddressOptionLabels = (
					await checkoutPage.commerceAddressOptions.allTextContents()
				).map((commerceAddressOptionLabel) =>
					commerceAddressOptionLabel.trim()
				);

				expect(commerceAddressOptionLabels).toContain(
					'Add New Address'
				);
				expect(commerceAddressOptionLabels).toContain(
					eligibleAddress.name
				);
				expect(commerceAddressOptionLabels).not.toContain(
					restrictedAddress.name
				);

				if (step === 'Shipping Address') {
					await checkoutPage.commerceAddressSelect.selectOption({
						label: eligibleAddress.name,
					});

					await checkoutPage.useAsBillingCheckbox.setChecked(false);
				}
			}
		});

		await test.step('Remove the channel from the eligibility tab', async () => {
			await performUserSwitch(page, 'test');

			await commerceAccountAddressEligibilityPage.goto(
				account.id,
				restrictedAddress.id
			);

			await commerceAccountAddressEligibilityPage.removeChannel(
				otherChannel.name
			);

			await commerceAccountAddressEligibilityPage.goto(
				account.id,
				restrictedAddress.id
			);

			await expect(
				commerceAccountAddressEligibilityPage.allChannelsRadio
			).toBeChecked();
		});
	}
);

test(
	'Channel default addresses are preselected at checkout only while eligible for the channel',
	{
		tag: [
			'@COMMERCE-12891',
			'@COMMERCE-12894',
			'@COMMERCE-12910',
			'@COMMERCE-12911',
			'@LPD-108644',
		],
	},
	async ({
		accountsPage,
		apiHelpers,
		backendPage,
		checkoutPage,
		commerceAdminChannelsPage,
		commerceMiniCartPage,
		editAccountChannelDefaultsPage,
		editAccountPage,
		page,
		site,
	}) => {
		const {account, buyerUser, channel, miniCartLayout, sku} =
			await checkoutStorefrontSetUp(
				apiHelpers,
				commerceAdminChannelsPage,
				page,
				site
			);

		const otherChannel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				name: getRandomString(),
			});

		const billingAddress =
			await apiHelpers.headlessCommerceAdminAccount.postAddress(
				account.id,
				{
					defaultBilling: false,
					defaultShipping: false,
					name: 'Billing Address',
					type: 1,
				}
			);

		apiHelpers.data.push({id: billingAddress.id, type: 'address'});

		const shippingAddress =
			await apiHelpers.headlessCommerceAdminAccount.postAddress(
				account.id,
				{
					defaultBilling: false,
					defaultShipping: false,
					name: 'Shipping Address',
					type: 3,
				}
			);

		apiHelpers.data.push({id: shippingAddress.id, type: 'address'});

		const billingAddressChannel =
			await apiHelpers.headlessCommerceAdminChannel.postAccountAddressChannel(
				billingAddress.id,
				channel.id
			);
		const shippingAddressChannel =
			await apiHelpers.headlessCommerceAdminChannel.postAccountAddressChannel(
				shippingAddress.id,
				channel.id
			);

		await apiHelpers.headlessCommerceAdminAccount.postAccountChannelBillingAddress(
			account.id,
			{
				channelId: channel.id,
				classPK: billingAddress.id,
			}
		);
		await apiHelpers.headlessCommerceAdminAccount.postAccountChannelShippingAddress(
			account.id,
			{
				channelId: channel.id,
				classPK: shippingAddress.id,
			}
		);

		const miniCartPageURL = `/web${site.friendlyUrlPath}${miniCartLayout.friendlyUrlPath}`;

		await test.step('The eligible channel default addresses are preselected at checkout', async () => {
			await apiHelpers.headlessCommerceDeliveryCart.postCart(
				{
					accountId: account.id,
					cartItems: [{quantity: 1, skuId: sku.id}],
				},
				channel.id
			);

			await performUserSwitch(page, buyerUser.alternateName);

			await page.goto(miniCartPageURL);

			await commerceMiniCartPage.miniCartButton.click();

			await commerceMiniCartPage.submitButton.click();

			await expect(commerceMiniCartPage.submitButton).toBeHidden();

			for (const {defaultAddress, step} of [
				{defaultAddress: shippingAddress, step: 'Shipping Address'},
				{defaultAddress: billingAddress, step: 'Billing Address'},
			]) {
				await checkoutPage.performCheckoutUntilStep(step);

				await expect(checkoutPage.commerceAddressSelect).toHaveValue(
					String(defaultAddress.id)
				);

				const commerceAddressOptionLabels = (
					await checkoutPage.commerceAddressOptions.allTextContents()
				).map((commerceAddressOptionLabel) =>
					commerceAddressOptionLabel.trim()
				);

				expect(commerceAddressOptionLabels).toContain(
					'Add New Address'
				);
				expect(commerceAddressOptionLabels).toContain(
					defaultAddress.name
				);

				if (step === 'Shipping Address') {
					await checkoutPage.useAsBillingCheckbox.setChecked(false);
				}
			}

			await checkoutPage.performCheckoutUntilStep('Order Confirmation');

			await expect(checkoutPage.orderSuccessMessage).toBeVisible();
		});

		await test.step('Restrict the channel default addresses to another channel', async () => {
			const adminApiHelpers = new DataApiHelpers(backendPage);

			await adminApiHelpers.headlessCommerceAdminChannel.deleteAccountAddressChannel(
				billingAddressChannel.accountAddressChannelId
			);
			await adminApiHelpers.headlessCommerceAdminChannel.deleteAccountAddressChannel(
				shippingAddressChannel.accountAddressChannelId
			);

			await adminApiHelpers.headlessCommerceAdminChannel.postAccountAddressChannel(
				billingAddress.id,
				otherChannel.id
			);
			await adminApiHelpers.headlessCommerceAdminChannel.postAccountAddressChannel(
				shippingAddress.id,
				otherChannel.id
			);

			await adminApiHelpers.headlessCommerceDeliveryCart.postCart(
				{
					accountId: account.id,
					cartItems: [{quantity: 1, skuId: sku.id}],
				},
				channel.id
			);
		});

		await test.step('The channel defaults stay listed but cannot be saved again with the ineligible addresses', async () => {
			await performUserSwitch(page, 'test');

			await accountsPage.gotoAccountAdmin();

			await accountsPage.accountsTable.search(account.name);

			await accountsPage.accountNameLink(account.name).click();

			await editAccountPage.channelDefaultsLink.click();

			for (const {addressSelectOptions, addressType, defaultAddress} of [
				{
					addressSelectOptions:
						editAccountChannelDefaultsPage.setDefaultBillingAddressFrameBillingAddressDropdownOptions,
					addressType: 'Billing' as const,
					defaultAddress: billingAddress,
				},
				{
					addressSelectOptions:
						editAccountChannelDefaultsPage.setDefaultShippingAddressFrameBillingAddressDropdownOptions,
					addressType: 'Shipping' as const,
					defaultAddress: shippingAddress,
				},
			]) {
				await expect(
					await editAccountChannelDefaultsPage.addressTableRowColumn(
						1,
						addressType,
						defaultAddress.name
					)
				).toBeVisible();

				await (
					await editAccountChannelDefaultsPage.addressTableRowColumn(
						3,
						addressType,
						defaultAddress.name
					)
				).click();

				await editAccountChannelDefaultsPage.editMenuItem.click();

				await expect(
					editAccountChannelDefaultsPage.modalSaveButton
				).toBeVisible();

				const addressSelectOptionLabels = (
					await addressSelectOptions.allTextContents()
				).map((addressSelectOptionLabel) =>
					addressSelectOptionLabel.trim()
				);

				expect(addressSelectOptionLabels).not.toContain(
					defaultAddress.name
				);

				await editAccountChannelDefaultsPage.modalSaveButton.click();

				await expect(
					editAccountChannelDefaultsPage.modalContainer.getByText(
						`The ${addressType} Address field is required.`
					)
				).toBeVisible();

				await editAccountChannelDefaultsPage.modalContainer
					.getByRole('button', {exact: true, name: 'Cancel'})
					.click();

				await expect(
					editAccountChannelDefaultsPage.modalIframe
				).toBeHidden();
			}
		});

		await test.step('The ineligible channel default addresses are not offered at checkout', async () => {
			const billingAndShippingAddress =
				await apiHelpers.headlessCommerceAdminAccount.postAddress(
					account.id,
					{
						defaultBilling: false,
						defaultShipping: false,
						name: 'Billing and Shipping Address',
						type: 2,
					}
				);

			apiHelpers.data.push({
				id: billingAndShippingAddress.id,
				type: 'address',
			});

			await performUserSwitch(page, buyerUser.alternateName);

			await page.goto(miniCartPageURL);

			await commerceMiniCartPage.miniCartButton.click();

			await commerceMiniCartPage.submitButton.click();

			await expect(commerceMiniCartPage.submitButton).toBeHidden();

			for (const {defaultAddress, step} of [
				{defaultAddress: shippingAddress, step: 'Shipping Address'},
				{defaultAddress: billingAddress, step: 'Billing Address'},
			]) {
				await checkoutPage.performCheckoutUntilStep(step);

				await expect(checkoutPage.commerceAddressSelect).toBeVisible();

				const commerceAddressOptionLabels = (
					await checkoutPage.commerceAddressOptions.allTextContents()
				).map((commerceAddressOptionLabel) =>
					commerceAddressOptionLabel.trim()
				);

				expect(commerceAddressOptionLabels).toContain(
					'Add New Address'
				);
				expect(commerceAddressOptionLabels).toContain(
					billingAndShippingAddress.name
				);
				expect(commerceAddressOptionLabels).not.toContain(
					defaultAddress.name
				);

				if (step === 'Shipping Address') {
					await checkoutPage.commerceAddressSelect.selectOption({
						label: billingAndShippingAddress.name,
					});

					await checkoutPage.useAsBillingCheckbox.setChecked(false);
				}
			}
		});
	}
);

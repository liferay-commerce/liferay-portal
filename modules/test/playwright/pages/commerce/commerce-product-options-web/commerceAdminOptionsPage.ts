/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';
import {GlobalMenuPage} from '../../product-navigation-applications-menu/GlobalMenuPage';

export class CommerceAdminOptionsPage {
	readonly addOptionButton: Locator;
	readonly addOptionValueButton: Locator;
	readonly descriptionInput: Locator;
	readonly globalMenuPage: GlobalMenuPage;
	readonly modalFieldTypeSelect: Locator;
	readonly modalFrame: FrameLocator;
	readonly modalKeyInput: Locator;
	readonly modalNameInput: Locator;
	readonly modalSubmitButton: Locator;
	readonly nameInput: Locator;
	readonly optionRow: (optionName: string) => Locator;
	readonly optionRowActionsButton: (optionName: string) => Locator;
	readonly optionValueLink: (optionValueName: string) => Locator;
	readonly optionValueSidePanelTitle: Locator;
	readonly page: Page;
	readonly rowMenuItem: (action: string) => Locator;
	readonly saveButton: Locator;

	constructor(page: Page) {
		this.addOptionButton = page
			.getByTestId('managementToolbar')
			.getByRole('button', {exact: true, name: 'Add Option'});
		this.addOptionValueButton = page
			.getByRole('button', {
				exact: true,
				name: 'Add Option Value Template',
			})
			.first();
		this.descriptionInput = page.getByLabel('Characters Maximum: 4000');
		this.globalMenuPage = new GlobalMenuPage(page);
		this.modalFrame = page.frameLocator('.modal.show iframe');
		this.modalFieldTypeSelect =
			this.modalFrame.getByLabel('Option Field Type');
		this.modalKeyInput = this.modalFrame.getByLabel('Key');
		this.modalNameInput = this.modalFrame.getByLabel('Name');
		this.modalSubmitButton = this.modalFrame.getByRole('button', {
			exact: true,
			name: 'Submit',
		});
		this.nameInput = page.getByLabel('Name Required');
		this.optionRow = (optionName: string) =>
			page.getByRole('row').filter({hasText: optionName});
		this.optionRowActionsButton = (optionName: string) =>
			page.getByRole('button', {
				exact: true,
				name: `${optionName} Actions`,
			});
		this.optionValueLink = (optionValueName: string) =>
			page.getByRole('link', {exact: true, name: optionValueName});
		this.optionValueSidePanelTitle = page
			.frameLocator('.fds-side-panel.is-visible iframe')
			.locator('.side-panel-iframe-header');
		this.page = page;
		this.rowMenuItem = (action: string) =>
			page.getByRole('menuitem', {exact: true, name: action});
		this.saveButton = page.getByRole('link', {exact: true, name: 'Save'});
	}

	async addOption({
		fieldType,
		key,
		name,
	}: {
		fieldType: string;
		key: string;
		name: string;
	}) {
		await this.addOptionButton.click();

		await this.modalNameInput.fill(name);
		await this.modalFieldTypeSelect.selectOption({label: fieldType});
		await this.modalKeyInput.fill(key);
		await this.modalSubmitButton.click();

		await expect(this.nameInput).toHaveValue(name);
	}

	async addOptionValue({key, name}: {key: string; name: string}) {
		await this.addOptionValueButton.click();

		await this.modalNameInput.fill(name);
		await this.modalKeyInput.fill(key);
		await this.modalSubmitButton.click();

		await expect(this.optionValueLink(name)).toBeVisible();
	}

	async deleteOption(optionName: string) {
		await this.optionRowActionsButton(optionName).click();
		await this.rowMenuItem('Delete').click();

		await waitForAlert(this.page);

		await expect(this.optionRow(optionName)).toHaveCount(0);
	}

	async goto() {
		await this.globalMenuPage.goToCommerce('Options');
	}
}

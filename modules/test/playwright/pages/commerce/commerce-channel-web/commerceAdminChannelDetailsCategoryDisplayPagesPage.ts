/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';

export class CommerceAdminChannelDetailsCategoryDisplayPagesPage {
	readonly addDisplayLayoutButton: Locator;
	readonly addDisplayLayoutFrame: FrameLocator;
	readonly categoryDisplayPageLabel: (pageName: string) => Promise<Locator>;
	readonly categoryDisplayPageList: (pageName: string) => Promise<Locator>;
	readonly categoryLabel: (categoryName: string) => Promise<Locator>;
	readonly categoryNameRowTableLink: (
		categoryName: string
	) => Promise<Locator>;
	readonly closeCategoryLabel: Locator;
	readonly errorMessageSelectCategory: Locator;
	readonly frameButton: (buttonName: string) => Promise<Locator>;
	readonly frameSearchBar: Locator;
	readonly page: Page;
	readonly selectCategoryButton: Locator;
	readonly selectCategoryDisplayPageFrame: FrameLocator;
	readonly selectCategoryModal: Locator;
	readonly selectVocabularyModal: Locator;
	readonly successMessage: Locator;

	constructor(page: Page) {
		this.addDisplayLayoutButton = page.getByLabel('Add Display Layout');
		this.addDisplayLayoutFrame = page.frameLocator('iframe');

		this.selectCategoryDisplayPageFrame =
			this.addDisplayLayoutFrame.frameLocator(
				'iframe[title="Select Category Display Page"]'
			);

		this.frameButton = async (buttonName: string) => {
			return this.addDisplayLayoutFrame.getByRole('button', {
				name: buttonName,
			});
		};

		this.categoryDisplayPageList = async (pageName: string) => {
			return this.selectCategoryDisplayPageFrame.getByText(pageName);
		};

		this.categoryLabel = async (categoryName: string) => {
			return this.addDisplayLayoutFrame.getByRole('link', {
				name: categoryName,
			});
		};

		this.categoryDisplayPageLabel = async (pageName: string) => {
			return this.addDisplayLayoutFrame.getByText('Pages > ' + pageName);
		};

		this.categoryNameRowTableLink = async (categoryName: string) => {
			return page.getByRole('link', {name: categoryName});
		};

		this.closeCategoryLabel =
			this.addDisplayLayoutFrame.getByLabel('Close');

		this.errorMessageSelectCategory = this.addDisplayLayoutFrame.getByText(
			'Close Error:Please select a valid category.'
		);

		this.frameSearchBar =
			this.selectCategoryDisplayPageFrame.getByPlaceholder('Search');

		this.page = page;

		this.selectCategoryButton = this.addDisplayLayoutFrame.locator(
			'[id$="selectCategories"]'
		);

		this.selectCategoryModal = this.addDisplayLayoutFrame.getByRole(
			'dialog',
			{exact: true, name: 'Select Category'}
		);

		this.selectVocabularyModal = this.addDisplayLayoutFrame.getByRole(
			'dialog',
			{exact: true, name: 'Select Vocabulary'}
		);

		this.successMessage = this.addDisplayLayoutFrame.getByText(
			'Success:Your request completed successfully.'
		);
	}

	async clickFrameButton(buttonName: string) {
		await expect(await this.frameButton(buttonName)).toBeAttached();

		await (await this.frameButton(buttonName)).waitFor();

		await (await this.frameButton(buttonName)).click();
	}

	async selectCategory(vocabularyName: string, categoryName: string) {
		await clickAndExpectToBeVisible({
			target: this.selectVocabularyModal,
			trigger: this.selectCategoryButton,
		});

		await this.selectVocabularyModal
			.getByRole('row')
			.filter({hasText: vocabularyName})
			.getByRole('radio')
			.check();
		await this.selectVocabularyModal
			.getByRole('button', {exact: true, name: 'Select'})
			.click();

		await this.selectCategoryModal
			.getByRole('treeitem', {name: categoryName})
			.click();

		await expect(this.selectCategoryModal).toBeHidden();
	}

	async selectCategoryDisplayPage(pageName: string) {
		await (await this.categoryDisplayPageList(pageName)).click();
	}
}

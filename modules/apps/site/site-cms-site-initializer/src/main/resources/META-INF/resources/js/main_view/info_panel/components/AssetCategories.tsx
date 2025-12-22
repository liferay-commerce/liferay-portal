/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import Label from '@clayui/label';
import ClayPanel from '@clayui/panel';
import {ItemSelector} from '@liferay/frontend-js-item-selector-web';
import React, {useCallback, useMemo, useState} from 'react';

import {
	IAssetObjectEntry,
	IGroupedTaxonomies, ISearchAssetObjectEntry,
	ITaxonomyCategoryFacade,
} from '../../../common/types/AssetType';
import {CategorizationInputSize} from './AssetCategorization';

import type {EntryCategorizationDTO} from '../services/ObjectEntryService';

const AssetCategories = ({
	cmsGroupId,
	collapsable = true,
	hasUpdatePermission,
	inputSize,
	objectEntry,
	updateObjectEntry,
}: {
	cmsGroupId: number | string;
	collapsable?: boolean;
	hasUpdatePermission?: boolean;
	inputSize?: CategorizationInputSize;
	objectEntry: IAssetObjectEntry | EntryCategorizationDTO;
	updateObjectEntry: (object: EntryCategorizationDTO) => void | Promise<void>;
}) => {
	const [value, setValue] = useState('');

	const apiURL = useMemo(() => `${
		Liferay.ThemeDisplay.getPortalURL()
	}/o/headless-admin-taxonomy/v1.0/asset-libraries/${
		objectEntry?.scopeId || cmsGroupId
	}/taxonomy-categories?filter=assetTypes in ('0', '${
		objectEntry?.systemProperties?.objectDefinitionBrief?.classNameId
	}')`, [cmsGroupId, objectEntry]);

	const groupedTaxonomies: IGroupedTaxonomies = useMemo(() => (
		(objectEntry as IAssetObjectEntry).taxonomyCategoryBriefs || []
	).reduce(
		(groupedTaxonomies, {embeddedTaxonomyCategory: categoryBrief}) => {
			const {id, taxonomyVocabularyId} = categoryBrief;

			const taxonomyCategories: string[] =
				groupedTaxonomies.taxonomyVocabularies[taxonomyVocabularyId] ||
				[];

			taxonomyCategories.push(categoryBrief);

			return {
				taxonomyCategoryIds: [
					...groupedTaxonomies.taxonomyCategoryIds,
					parseInt(id, 10),
				],
				taxonomyVocabularies: {
					...groupedTaxonomies.taxonomyVocabularies,
					[taxonomyVocabularyId]: taxonomyCategories,
				},
			};
		},
		{
			taxonomyCategoryIds: [],
			taxonomyVocabularies: {},
		} as IGroupedTaxonomies
	), [(objectEntry as IAssetObjectEntry).taxonomyCategoryBriefs]);

	const addCategory = useCallback(
		async (item: any) => {
			const taxonomyCategoryId = parseInt(item.id, 10);

			if (
				groupedTaxonomies.taxonomyCategoryIds?.includes(
					taxonomyCategoryId
				)
			) {
				return;
			}

			const updated = [
				...groupedTaxonomies.taxonomyCategoryIds,
				taxonomyCategoryId,
			];

			await updateObjectEntry({
				lastAddedBrief: item,
				taxonomyCategoryIds: updated,
				taxonomyCategoryIdsToAdd: updated,
				/** TODO filter removed categories **/
				taxonomyCategoryIdsToRemove: [],
			} as unknown as EntryCategorizationDTO);
		},
		[groupedTaxonomies.taxonomyCategoryIds, updateObjectEntry]
	);

	const removeCategory = useCallback(
		async (category: ITaxonomyCategoryFacade) => {
			const {taxonomyCategoryIds} = groupedTaxonomies;

			const index = taxonomyCategoryIds.findIndex(
				(id) => id === parseInt(category.id, 10)
			);

			if (index === -1) {
				return;
			}

			taxonomyCategoryIds.splice(index, 1);

			/**
			 * TODO track removed IDs for bulk
			 * TODO and fill the key: toRemoveCategoryIds
			 */

			await updateObjectEntry({
				taxonomyCategoryIds
			} as EntryCategorizationDTO);
		},
		[groupedTaxonomies, updateObjectEntry]
	);

	return (
		<ClayPanel
			collapsable={collapsable}
			defaultExpanded={true}
			displayTitle={
				<ClayPanel.Title className="panel-title text-secondary">
					{Liferay.Language.get('categories')}
				</ClayPanel.Title>
			}
			displayType="unstyled"
			showCollapseIcon={collapsable}
		>
			<ClayPanel.Body>
				<ItemSelector<any>
					apiURL={apiURL}
					disabled={!hasUpdatePermission}
					locator={{
						id: 'id',
						label: 'name',
						value: 'externalReferenceCode',
					}}
					onChange={setValue}
					onItemsChange={(newItems: any) => {
						if (newItems[0]) {
							addCategory(newItems[0]);

							// The reason for this timeout is because of react's
							// batch rendering. Clay internals set the value of
							// the input, but we need to wait for the next 'tick' to set the value.

							setTimeout(() => setValue(''));
						}
					}}
					placeholder={Liferay.Language.get('add-category')}
					refetchOnActive
					sizing={inputSize}
					value={value}
				>
					{(item) => (
						<ItemSelector.Item
							key={item.name}
							textValue={item.name}
						>
							{item.name}
						</ItemSelector.Item>
					)}
				</ItemSelector>

				{groupedTaxonomies.taxonomyVocabularies &&
					Object.entries(groupedTaxonomies?.taxonomyVocabularies).map(
						([, vocabularyCategories], index) => {
							const vocabularyName =
								vocabularyCategories[0].parentTaxonomyVocabulary
									.name;

							return vocabularyCategories.length ? (
								<div className="pt-3" key={index}>
									<p className="font-weight-semi-bold vocabulary-name">
										{vocabularyName}
									</p>

									{vocabularyCategories.map(
										(category: ITaxonomyCategoryFacade) => (
											<Label
												closeButtonProps={{
													'aria-label':
														Liferay.Language.get(
															'close'
														),
													'disabled':
														!hasUpdatePermission,
													'onClick': async (
														event
													) => {
														event.preventDefault();

														await removeCategory(
															category
														);
													},
													'title':
														Liferay.Language.get(
															'close'
														),
												}}
												displayType="secondary"
												key={`${category.taxonomyVocabularyId}_${category.id}`}
											>
												{category.name}
											</Label>
										)
									)}
								</div>
							) : null;
						}
					)}
			</ClayPanel.Body>
		</ClayPanel>
	);
};

export default AssetCategories;

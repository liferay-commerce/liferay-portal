/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import Autocomplete from '@clayui/autocomplete';
import ClayButton from '@clayui/button';
import {useResource} from '@clayui/data-provider';
import Label from '@clayui/label';
import ClayModal from '@clayui/modal';
import ClayPanel from '@clayui/panel';
import {fetch, sub} from 'frontend-js-web';
import React, {useCallback, useEffect, useState} from 'react';

import {
	IGroupedTaxonomies,
	ITaxonomyCategoryFacade,
} from '../../structure_builder/types/AssetType';
import ObjectEntryService from '../info_panel/services/ObjectEntryService';

export default function EditCategoriesBulkActionModalContent({
	actionId,
	closeModal,
	cmsGroupId,
	selectedData,
}: {
	actionId: string;
	closeModal: () => void;
	cmsGroupId: string;
	selectedData: any;
}) {
	const [groupedTaxonomies, setGroupedTaxonomies] = useState({
		taxonomyCategoryIds: [],
		taxonomyVocabularies: {},
	} as IGroupedTaxonomies);
	const [networkStatus, setNetworkStatus] = useState(4);
	const [value, setValue] = useState('');

	const {resource} = useResource({
		fetch,
		link: `${Liferay.ThemeDisplay.getPortalURL()}/o/headless-admin-taxonomy/v1.0/sites/${cmsGroupId}/taxonomy-categories`,
		onNetworkStatusChange: setNetworkStatus,
	});

	const addCategory = useCallback(
		async (item: any) => {
			const taxonomyCategoryId = parseInt(item.id, 10);

			if (
				groupedTaxonomies.taxonomyCategoryIds.includes(
					taxonomyCategoryId
				)
			) {
				return;
			}

			setGroupedTaxonomies(() => {
				const {taxonomyVocabularyId} = item;

				const taxonomyCategories =
					groupedTaxonomies.taxonomyVocabularies[
						taxonomyVocabularyId
					] || [];

				taxonomyCategories.push(item);

				return {
					taxonomyCategoryIds: [
						...groupedTaxonomies.taxonomyCategoryIds,
						taxonomyCategoryId,
					],
					taxonomyVocabularies: {
						...groupedTaxonomies.taxonomyVocabularies,
						[taxonomyVocabularyId]: taxonomyCategories,
					},
				};
			});
		},
		[groupedTaxonomies.taxonomyCategoryIds]
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

			console.log('Category Deleted: ', category);

			taxonomyCategoryIds.splice(index, 1);

			const {taxonomyVocabularyId} = category;

			const taxonomyCategories =
				groupedTaxonomies.taxonomyVocabularies[taxonomyVocabularyId] ||
				[];

			console.log('taxonomyCategories: ', taxonomyCategories);

			const vocabularyIndex = taxonomyCategories.findIndex(
				(vocabulary) => vocabulary.id === category.id
			);

			console.log('vocabularyIndex: ', vocabularyIndex);

			taxonomyCategories.splice(vocabularyIndex, 1);

			console.log(
				'taxonomyCategories after splice: ',
				taxonomyCategories
			);

			// Not working
            // setGroupedTaxonomies({
			//     taxonomyCategoryIds: taxonomyCategoryIds,
			//     taxonomyVocabularies: {
			//         [taxonomyVocabularyId]: taxonomyCategories,
			//     },
			// });

		},
		[groupedTaxonomies]
	);

	const handleSubmit = async (event: React.FormEvent) => {
		event.preventDefault();

		// postBulkActionCategories

		// Liferay.fire(START_TASK, {taskId});

	};

	return (
		<>
			<ClayModal.Header>
				<div className="autofit-row autofit-row-center">
					<div className="autofit-col autofit-col-expand">
						<div className="text-truncate">Edit Categories</div>
					</div>
				</div>
			</ClayModal.Header>
			<ClayModal.Body>
				<ClayPanel
					collapsable={false}
					displayTitle={
						<ClayPanel.Title className="panel-title text-secondary">
							{Liferay.Language.get('categories')}
						</ClayPanel.Title>
					}
					displayType="unstyled"
				>
					<ClayPanel.Body>
						{resource?.items ? (
							<Autocomplete
								defaultItems={resource?.items}
								filterKey="name"
								id="asset-categories-autocomplete"
								loadingState={networkStatus}
								menuTrigger="focus"
								onChange={setValue}
								placeholder={sub(
									Liferay.Language.get('add-x'),
									'category'
								)}
								sizing="sm"
								value={value}
							>
								{(item: any) => (
									<Autocomplete.Item
										key={item.id}
										onClick={async (event: any) => {
											event.preventDefault();

											await addCategory(item);
										}}
									>
										{item.name}
									</Autocomplete.Item>
								)}
							</Autocomplete>
						) : null}

						{groupedTaxonomies.taxonomyVocabularies &&
							Object.entries(
								groupedTaxonomies?.taxonomyVocabularies
							).map(([, vocabularyCategories], index) => {
								const vocabularyName =
									vocabularyCategories[0]
										.parentTaxonomyVocabulary.name;

								return vocabularyCategories.length ? (
									<div className="pt-3" key={index}>
										<p className="font-weight-semi-bold vocabulary-name">
											{vocabularyName}
										</p>

										{vocabularyCategories.map(
											(
												category: ITaxonomyCategoryFacade
											) => (
												<Label
													closeButtonProps={{
														'aria-label':
															Liferay.Language.get(
																'close'
															),
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
							})}
					</ClayPanel.Body>
				</ClayPanel>
			</ClayModal.Body>
			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
							type="button"
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							displayType="primary"
							onClick={handleSubmit}
							type="submit"
						>
							{Liferay.Language.get('submit')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}

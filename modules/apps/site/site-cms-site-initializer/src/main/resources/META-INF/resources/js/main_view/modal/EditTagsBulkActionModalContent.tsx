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

import {START_TASK} from '../../common/utils/events';
import EditCategoriesBulkActionModalContent from './EditCategoriesBulkActionModalContent';

export default function EditTagsBulkActionModalContent({
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
	const [keywords, setKeywords] = useState([] as string[]);
	const [networkStatus, setNetworkStatus] = useState(4);

	const [value, setValue] = useState('');

	const {resource} = useResource({
		fetch,
		link: `${Liferay.ThemeDisplay.getPortalURL()}/o/headless-admin-taxonomy/v1.0/sites/${cmsGroupId}/keywords`,
		onNetworkStatusChange: setNetworkStatus,
	});

	const [items, setItems] = useState([] as {[key: string]: any}[]);

	const addKeyword = useCallback(
		async (keyword: any) => {
			if (keywords.includes(keyword.name)) {
				return;
			}

			setValue('');

			setKeywords((prevKeywords) => {
				return [...prevKeywords, keyword.name];
			});
		},
		[keywords]
	);

	const removeKeyword = useCallback(
		async (keyword: string) => {
			const index = keywords.findIndex((value) => value === keyword);

			if (index === -1) {
				return;
			}

			const curKeywords = [...keywords];

			curKeywords.splice(index, 1);

			setKeywords(curKeywords);
		},
		[keywords]
	);

	useEffect(() => {
		setItems(() => {
			if (value.length) {
				return [
					...resource?.items.filter(({name}: {name: string}) =>
						name.includes(value)
					),
				];
			}

			return [...(resource?.items || [])];
		});
	}, [value, resource, setItems]);

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
						<div className="text-truncate">Edit Tags</div>
					</div>
				</div>
			</ClayModal.Header>
			<ClayModal.Body>
				<ClayPanel
					collapsable={false}
					displayTitle={
						<ClayPanel.Title className="panel-title text-secondary">
							{Liferay.Language.get('tags')}
						</ClayPanel.Title>
					}
					displayType="unstyled"
				>
					<ClayPanel.Body>
						<Autocomplete
							filterKey="name"
							id="asset-tags-autocomplete"
							items={items}
							loadingState={networkStatus}
							menuTrigger="focus"
							onChange={setValue}
							placeholder={sub(
								Liferay.Language.get('add-x'),
								'tag'
							)}
							sizing="sm"
							value={value}
						>
							{!items.length ? (
								<Autocomplete.Item
									className="text-info"
									key="createNewKeyword"
								/>
							) : (
								items.map((item) => {
									return (
										<Autocomplete.Item
											key={item.id}
											onClick={async (event) => {
												event.preventDefault();

												await addKeyword(item);
											}}
										>
											{item.name}
										</Autocomplete.Item>
									);
								})
							)}
						</Autocomplete>

						<div className="asset-tags mt-3">
							{keywords.map((keyword: string, index: number) => {
								return (
									<Label
										className="mr-2 mt-2"
										closeButtonProps={{
											'aria-label':
												Liferay.Language.get('close'),
											'onClick': async (event) => {
												event.preventDefault();

												await removeKeyword(keyword);
											},
											'title':
												Liferay.Language.get('close'),
										}}
										displayType="secondary"
										key={`${keyword}_${index}`}
									>
										{keyword}
									</Label>
								);
							})}
						</div>
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

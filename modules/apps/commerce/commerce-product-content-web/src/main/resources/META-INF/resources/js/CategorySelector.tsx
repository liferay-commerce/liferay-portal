/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

// @ts-ignore

import {AssetVocabularyCategoriesSelector} from 'asset-taglib';
import React from 'react';

import {BaseRule} from './Rule';

export interface Category {
	label: string;
	value: string;
}

export interface CategoryRule extends BaseRule {
	categoryIdsTitles?: string[];
	queryValues?: string;
	type: 'assetCategories';
}

interface Props {
	categorySelectorURL: string;
	eventName: string;
	groupIds: string;
	index: number;
	namespace: string;
	onChange: (categories: Category[]) => void;
	rule: CategoryRule;
	vocabularyIds: string;
}

export function CategorySelector({
	categorySelectorURL,
	eventName,
	groupIds,
	index = 0,
	namespace,
	onChange,
	rule,
	vocabularyIds,
}: Props) {
	const inputId = `${namespace}queryCategoryIds${index}`;
	const selectorId = `${namespace}assetCategoriesSelector${index}`;

	const groupIdsList =
		groupIds && !!groupIds.length ? groupIds.split(',') : [];

	const selectedItems =
		rule.queryValues && !!rule.queryValues.length
			? rule.queryValues.split(',').map((categoryId, index) => ({
					label: rule.categoryIdsTitles?.[index],
					value: categoryId,
			  }))
			: [];

	const sourceItemsVocabularyIds = vocabularyIds.length
		? vocabularyIds.split(',')
		: [];

	return (
		<div className="category-selector d-inline-block">
			<input name={inputId} type="hidden" value={rule.queryValues} />

			<AssetVocabularyCategoriesSelector
				eventName={eventName}
				groupIds={groupIdsList}
				id={selectorId}
				inputName={selectorId}
				label={Liferay.Language.get('categories')}
				onSelectedItemsChange={onChange}
				portletURL={categorySelectorURL}
				required={false}
				selectedItems={selectedItems}
				showLabel={false}
				singleSelect={false}
				sourceItemsVocabularyIds={sourceItemsVocabularyIds}
				useFallbackInput={false}
			/>
		</div>
	);
}

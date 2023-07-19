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

import {AssetTagsSelector} from 'asset-taglib';
import React from 'react';

import {BaseRule} from './Rule';

export interface Tag {
	label: string;
	value: string;
}

export interface TagRule extends BaseRule {
	queryValues?: string;
	type: 'assetTags';
}

interface Props {
	groupIds: string;
	index: number;
	namespace: string;
	onChange: (tags: Tag[]) => void;
	rule: TagRule;
	tagSelectorURL: string;
}

export function TagSelector({
	groupIds,
	index = 0,
	namespace,
	onChange,
	rule,
	tagSelectorURL,
}: Props) {
	const groupIdsList =
		groupIds && !!groupIds.length ? groupIds.split(',') : [];

	const selectedItems =
		rule.queryValues && !!rule.queryValues.length
			? rule.queryValues.split(',').map((tagName) => ({
					label: tagName,
					value: tagName,
			  }))
			: [];

	return (
		<div className="d-inline-block tag-selector">
			<input
				name={`${namespace}queryTagNames${index}`}
				type="hidden"
				value={rule.queryValues}
			/>

			<AssetTagsSelector
				groupIds={groupIdsList}
				onSelectedItemsChange={onChange}
				portletURL={tagSelectorURL}
				selectedItems={selectedItems}
				showLabel={false}
				showSelectButton
				showSubtitle={false}
				subtitle=""
			/>
		</div>
	);
}

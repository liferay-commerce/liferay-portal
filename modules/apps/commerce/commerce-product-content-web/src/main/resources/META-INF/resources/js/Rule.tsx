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

import {ClayButtonWithIcon} from '@clayui/button';
import Form, {ClaySelectWithOption} from '@clayui/form';
import React from 'react';

import {CategoryRule, CategorySelector} from './CategorySelector';
import {TagRule, TagSelector} from './TagSelector';

export type RuleType = 'assetCategories' | 'assetTags';

export interface BaseRule {
	queryAndOperator: boolean;
	queryContains: boolean;
	type: RuleType;
}

export type Rule<T extends RuleType = RuleType> = T extends 'assetTags'
	? TagRule
	: T extends 'assetCategories'
	? CategoryRule
	: never;

export const QUERY_AND_OPERATOR_OPTIONS = [
	{label: Liferay.Language.get('all'), value: 'true'},
	{label: Liferay.Language.get('any'), value: 'false'},
];

export const QUERY_CONTAINS_OPTIONS = [
	{
		label: Liferay.Language.get('contains'),
		value: 'true',
	},
	{
		label: Liferay.Language.get('does-not-contain'),
		value: 'false',
	},
];

export const QUERY_NAME_OPTIONS: Array<{label: string; value: RuleType}> = [
	{
		label: Liferay.Language.get('categories'),
		value: 'assetCategories',
	},
	{
		label: Liferay.Language.get('tags'),
		value: 'assetTags',
	},
];

export interface RuleProps<T extends RuleType> {
	categorySelectorURL: string;
	groupIds: string;
	index: number;
	namespace: string;
	onChange: (index: number, changes: Partial<Rule>) => void;
	onDelete: (index: number) => void;
	rule: Rule<T>;
	tagSelectorURL: string;
	vocabularyIds: string;
}

export function Rule<T extends RuleType>({
	categorySelectorURL,
	groupIds,
	index,
	namespace,
	onChange,
	onDelete,
	rule,
	tagSelectorURL,
	vocabularyIds,
}: RuleProps<T>) {
	const queryContainsId = `${namespace}queryContains${index}`;
	const queryAndOperatorId = `${namespace}queryAndOperator${index}`;
	const queryNameId = `${namespace}queryName${index}`;

	return (
		<li className="timeline-item">
			<div className="panel panel-default">
				<div className="align-items-center c-gap-3 d-flex flex-wrap panel-body">
					<Form.Group>
						<ClaySelectWithOption
							name={queryContainsId}
							onChange={(event) =>
								onChange(index, {
									queryContains:
										event.target.value === 'true',
								})
							}
							options={QUERY_CONTAINS_OPTIONS}
							title={Liferay.Language.get('query-contains')}
							value={rule.queryContains.toString()}
						/>
					</Form.Group>

					<Form.Group>
						<ClaySelectWithOption
							name={queryAndOperatorId}
							onChange={(event) =>
								onChange(index, {
									queryAndOperator:
										event.target.value === 'true',
								})
							}
							options={QUERY_AND_OPERATOR_OPTIONS}
							title={Liferay.Language.get('and-operator')}
							value={rule.queryAndOperator.toString()}
						/>
					</Form.Group>

					<Form.Group>
						<label className="control-label" htmlFor={queryNameId}>
							{Liferay.Language.get('of-the-following')}
						</label>
					</Form.Group>

					<Form.Group>
						<ClaySelectWithOption
							className="asset-query-name"
							id={queryNameId}
							name={queryNameId}
							onChange={(event) =>
								onChange(index, {
									type: event.target.value as RuleType,
								})
							}
							options={QUERY_NAME_OPTIONS}
							value={rule.type.toString()}
						/>
					</Form.Group>

					{rule.type === 'assetTags' ? (
						<TagSelector
							groupIds={groupIds}
							index={index}
							namespace={namespace}
							onChange={(tags) =>
								onChange(index, {
									queryValues: tags
										.map((tag) => tag.value)
										.join(','),
								})
							}
							rule={rule}
							tagSelectorURL={tagSelectorURL}
						/>
					) : (
						<CategorySelector
							categorySelectorURL={categorySelectorURL}
							eventName={`${namespace}selectCategory`}
							groupIds={groupIds}
							index={index}
							namespace={namespace}
							onChange={(categories) =>
								onChange(index, {
									categoryIdsTitles: categories.map(
										(category) => category.label
									),
									queryValues: categories
										.map((category) => category.value)
										.join(','),
								})
							}
							rule={rule}
							vocabularyIds={vocabularyIds}
						/>
					)}

					<div className="timeline-increment">
						<span className="timeline-icon"></span>
					</div>
				</div>
			</div>

			<div className="container-trash">
				<ClayButtonWithIcon
					aria-label={Liferay.Language.get('delete-rule')}
					className="condition-card-delete"
					displayType="link"
					onClick={() => onDelete(index)}
					size="xs"
					symbol="trash"
					type="button"
				/>
			</div>
		</li>
	);
}

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
import {ClayInput} from '@clayui/form';
import React, {useCallback, useState} from 'react';

import {Rule, RuleType} from './Rule';
import {TagRule} from './TagSelector';

const DEFAULT_RULE: TagRule = {
	queryAndOperator: false,
	queryContains: true,
	type: 'assetTags',
};

export interface Props {
	categorySelectorURL: string;
	groupIds: string;
	id: string;
	namespace: string;
	rules: Rule[];
	tagSelectorURL: string;
	vocabularyIds: string;
}

export default function AutoField({
	categorySelectorURL,
	groupIds,
	id,
	namespace,
	rules: initialRules,
	tagSelectorURL,
	vocabularyIds,
}: Props) {
	const [rules, setRules] = useState<Rule[]>(() => initialRules);

	const onAddRuleButtonClick = useCallback(() => {
		setRules((previousRules) => [...previousRules, DEFAULT_RULE]);
	}, []);

	const onDeleteRule = useCallback((ruleIndex: number) => {
		setRules((previousRules) => {
			if (ruleIndex >= previousRules.length) {
				return previousRules;
			}

			const nextRules = [...previousRules];
			nextRules.splice(ruleIndex, 1);

			return nextRules;
		});
	}, []);

	const onRuleChange = useCallback(
		(ruleIndex: number, changes: Partial<Rule<RuleType>>) => {
			setRules((previousRules) => {
				const nextRules = [...previousRules];

				if ('type' in changes) {
					nextRules[ruleIndex] = {
						...DEFAULT_RULE,
						...changes,
					} as Rule;
				}
				else {
					nextRules[ruleIndex] = {
						...nextRules[ruleIndex],
						...changes,
					} as Rule;
				}

				return nextRules;
			});
		},
		[]
	);

	return (
		<div id={id}>
			<ClayInput
				name={`${namespace}queryLogicIndexes`}
				type="hidden"
				value={Object.keys(rules).join(',')}
			/>

			<ul className="timeline">
				<li className="timeline-item">
					<div className="panel panel-default">
						<div className="d-flex flex-wrap panel-body">
							<div className="h4 panel-title">
								{Liferay.Language.get('rules')}
							</div>

							<div className="timeline-increment">
								<span className="timeline-icon"></span>
							</div>
						</div>
					</div>
				</li>

				{rules.map((rule, index) => (
					<Rule
						categorySelectorURL={categorySelectorURL}
						groupIds={groupIds}
						index={index}
						key={index}
						namespace={namespace}
						onChange={onRuleChange}
						onDelete={onDeleteRule}
						rule={rule}
						tagSelectorURL={tagSelectorURL}
						vocabularyIds={vocabularyIds}
					/>
				))}
			</ul>

			<div className="addbutton-timeline-item">
				<div className="add-condition timeline-increment-icon">
					<ClayButtonWithIcon
						aria-label={Liferay.Language.get('add-rule')}
						className="form-builder-rule-add-condition form-builder-timeline-add-item"
						onClick={onAddRuleButtonClick}
						size="xs"
						symbol="plus"
						type="button"
					/>
				</div>
			</div>
		</div>
	);
}

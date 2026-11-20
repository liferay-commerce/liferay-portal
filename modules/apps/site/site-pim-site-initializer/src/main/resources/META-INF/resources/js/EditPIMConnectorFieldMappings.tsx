/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {Toolbar, VerticalNavLayout} from '@liferay/site-cms-site-initializer';
import {fetch, navigate, sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import PIMFieldMappingRulesSection from './edit_field_mappings/PIMFieldMappingRulesSection';
import PIMFieldMappingsSection from './edit_field_mappings/PIMFieldMappingsSection';
import getObjectFields from './edit_field_mappings/getObjectFields';
import newPIMFieldMapping from './edit_field_mappings/newPIMFieldMapping';
import {
	IObjectDefinition,
	IPIMFieldMapping,
	IPIMFieldMappingRule,
	TYPE_DYNAMIC_VALUE,
	TYPE_FIXED_VALUE,
} from './edit_field_mappings/types';

const EMPTY_FIELD_MAPPING_RULE = {
	id: 0,
	stripHTMLTags: false,
	textCase: '',
	trimWhitespace: false,
	truncateLength: 0,
	urlEncode: false,
	useFirstNonemptySource: false,
};

interface IProps {
	apiURL: string;
	backURL: string;
	channelField: string;
	channelFieldAdditionalFieldMappingTypes: string[];
	channelFieldLabel: string;
	fieldMappingRule: IPIMFieldMappingRule | null;
	fieldMappingRuleAPIURL: string;
	fieldMappingRuleObjectRelationshipObjectFieldName: string;
	fieldMappings: IPIMFieldMapping[];
	objectDefinitions: IObjectDefinition[];
	objectEntryId: number;
	objectRelationshipObjectFieldName: string;
	spritemap: string;
	title: string;
	useFirstNonemptySourceSupported: boolean;
}

function isComplete({sourceFieldName, type, value}: IPIMFieldMapping) {
	if (type === TYPE_DYNAMIC_VALUE) {
		return Boolean(sourceFieldName);
	}

	if (type === TYPE_FIXED_VALUE) {
		return Boolean(value);
	}

	return true;
}

function isEmpty({
	stripHTMLTags,
	textCase,
	trimWhitespace,
	truncateLength,
	urlEncode,
	useFirstNonemptySource,
}: IPIMFieldMappingRule) {
	return (
		!stripHTMLTags &&
		!textCase &&
		!trimWhitespace &&
		!truncateLength &&
		!urlEncode &&
		!useFirstNonemptySource
	);
}

export default function EditPIMConnectorFieldMappings({
	apiURL,
	backURL,
	channelField,
	channelFieldAdditionalFieldMappingTypes = [],
	channelFieldLabel,
	fieldMappingRule: initialFieldMappingRule,
	fieldMappingRuleAPIURL,
	fieldMappingRuleObjectRelationshipObjectFieldName,
	fieldMappings: initialFieldMappings = [],
	objectDefinitions = [],
	objectEntryId,
	objectRelationshipObjectFieldName,
	spritemap,
	title,
	useFirstNonemptySourceSupported,
}: IProps) {
	const [fieldMappings, setFieldMappings] = useState<IPIMFieldMapping[]>(
		() => {
			const currentFieldMappings = initialFieldMappings
				.filter(
					(fieldMapping) =>
						fieldMapping.type !== TYPE_DYNAMIC_VALUE ||
						getObjectFields(
							objectDefinitions,
							fieldMapping.sourceClassName
						).some(
							({name}) => name === fieldMapping.sourceFieldName
						)
				)
				.map((fieldMapping) => ({
					...fieldMapping,
					key: `entry-${fieldMapping.id}`,
				}));

			if (currentFieldMappings.length) {
				return currentFieldMappings;
			}

			return [newPIMFieldMapping()];
		}
	);

	const [fieldMappingRule, setFieldMappingRule] =
		useState<IPIMFieldMappingRule>(
			initialFieldMappingRule ?? EMPTY_FIELD_MAPPING_RULE
		);

	useEffect(() => {
		if (!Number(objectEntryId) || !channelField) {
			navigate(backURL);
		}
	}, [backURL, channelField, objectEntryId]);

	const saveFieldMappingRule = () => {
		const {id, ...values} = fieldMappingRule;

		if (isEmpty(fieldMappingRule)) {
			if (!id) {
				return null;
			}

			return fetch(`${fieldMappingRuleAPIURL}/${id}`, {method: 'DELETE'});
		}

		const body: {[key: string]: unknown} = {
			...values,
			channelFieldName: channelField,
		};

		if (!id) {
			body[fieldMappingRuleObjectRelationshipObjectFieldName] =
				objectEntryId;
		}

		return fetch(
			id ? `${fieldMappingRuleAPIURL}/${id}` : fieldMappingRuleAPIURL,
			{
				body: JSON.stringify(body),
				headers: {
					'Content-Type': 'application/json',
				},
				method: id ? 'PATCH' : 'POST',
			}
		);
	};

	const handleSave = async () => {
		const nextFieldMappings = fieldMappings.filter(isComplete);

		if (!nextFieldMappings.length && !isEmpty(fieldMappingRule)) {
			Liferay.Util.openToast({
				message: Liferay.Language.get(
					'add-at-least-one-source-attribute-to-save-the-mapping-rules-or-turn-the-mapping-rules-off'
				),
				type: 'danger',
			});

			return;
		}

		const nextIds = new Set(
			nextFieldMappings.map(({id}) => id).filter(Boolean)
		);

		try {
			const deleteResponses = await Promise.all(
				initialFieldMappings
					.filter(({id}) => !nextIds.has(id))
					.map(({id}) => fetch(`${apiURL}/${id}`, {method: 'DELETE'}))
			);

			if (deleteResponses.some(({ok}) => !ok)) {
				throw new Error();
			}

			const saveResponses = await Promise.all(
				nextFieldMappings.map((fieldMapping, index) => {
					const body: {[key: string]: unknown} = {
						channelFieldName: channelField,
						priority: index,
						sourceClassName: fieldMapping.sourceClassName,
						sourceFieldName:
							fieldMapping.type === TYPE_DYNAMIC_VALUE
								? fieldMapping.sourceFieldName
								: '',
						type: fieldMapping.type,
						value:
							fieldMapping.type === TYPE_FIXED_VALUE
								? fieldMapping.value
								: '',
					};

					if (!fieldMapping.id) {
						body[objectRelationshipObjectFieldName] = objectEntryId;
					}

					return fetch(
						fieldMapping.id
							? `${apiURL}/${fieldMapping.id}`
							: apiURL,
						{
							body: JSON.stringify(body),
							headers: {
								'Content-Type': 'application/json',
							},
							method: fieldMapping.id ? 'PATCH' : 'POST',
						}
					);
				})
			);

			if (saveResponses.some(({ok}) => !ok)) {
				throw new Error();
			}

			const fieldMappingRuleResponse = await saveFieldMappingRule();

			if (fieldMappingRuleResponse && !fieldMappingRuleResponse.ok) {
				throw new Error();
			}

			Liferay.Util.openToast({
				message: sub(
					Liferay.Language.get('x-was-updated-successfully'),
					channelFieldLabel
				),
				type: 'success',
			});

			navigate(backURL);
		}
		catch (error) {
			Liferay.Util.openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});
		}
	};

	return (
		<>
			<Toolbar backURL={backURL} title={title}>
				<Toolbar.Item>
					<ClayButton
						displayType="secondary"
						onClick={() => navigate(backURL)}
						size="sm"
					>
						{Liferay.Language.get('cancel')}
					</ClayButton>

					<ClayButton
						className="inline-item-after"
						displayType="primary"
						onClick={handleSave}
						size="sm"
					>
						{Liferay.Language.get('save')}
					</ClayButton>
				</Toolbar.Item>
			</Toolbar>

			<VerticalNavLayout
				items={[
					{
						component: (
							<PIMFieldMappingsSection
								channelFieldAdditionalFieldMappingTypes={
									channelFieldAdditionalFieldMappingTypes
								}
								fieldMappings={fieldMappings}
								objectDefinitions={objectDefinitions}
								setFieldMappings={setFieldMappings}
								spritemap={spritemap}
							/>
						),
						id: 'destination',
						label: Liferay.Language.get('destination'),
					},
					{
						component: (
							<PIMFieldMappingRulesSection
								disabled={!initialFieldMappingRule}
								fieldMappingRule={fieldMappingRule}
								onChange={setFieldMappingRule}
								useFirstNonemptySourceSupported={
									useFirstNonemptySourceSupported
								}
							/>
						),
						id: 'mappingRules',
						label: Liferay.Language.get('mapping-rules'),
					},
				]}
			/>
		</>
	);
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {ClayInput, ClaySelect} from '@clayui/form';
import {RequiredMark} from '@liferay/site-cms-site-initializer';
import classNames from 'classnames';
import {sub} from 'frontend-js-web';
import React, {useEffect, useRef, useState} from 'react';
import {useDrag, useDrop} from 'react-dnd';

import getObjectFields from './getObjectFields';
import {
	IFieldMapping,
	IObjectDefinition,
	TYPE_DYNAMIC_VALUE,
	TYPE_FIXED_VALUE,
} from './types';

import '../../css/PIMFieldMappingRow.scss';

interface IProps {
	deletable: boolean;
	fieldMapping: IFieldMapping;
	index: number;
	objectDefinitions: IObjectDefinition[];
	onAdd: () => void;
	onChange: (index: number, fieldMapping: IFieldMapping) => void;
	onDelete: (index: number) => void;
	onMove: (index: number, targetIndex: number) => void;
	spritemap: string;
}

export default function FieldMappingRow({
	deletable,
	fieldMapping,
	index,
	objectDefinitions,
	onAdd,
	onChange,
	onDelete,
	onMove,
	spritemap,
}: IProps) {
	const dragButtonRef = useRef<HTMLButtonElement>(null);
	const rowRef = useRef<HTMLDivElement>(null);

	const [keyboardDragging, setKeyboardDragging] = useState(false);

	const [{dragging}, dragRef, previewRef] = useDrag({
		collect: (monitor) => ({
			dragging: monitor.isDragging(),
		}),
		item: {
			index,
			type: 'fieldMappingRow',
		},
	});

	const [, dropRef] = useDrop({
		accept: 'fieldMappingRow',
		hover(item: {index: number; type: string}, monitor) {
			const clientOffset = monitor.getClientOffset();

			if (!clientOffset || !rowRef.current || item.index === index) {
				return;
			}

			const {bottom, top} = rowRef.current.getBoundingClientRect();

			const middle = (bottom - top) / 2;

			if (
				(item.index < index && clientOffset.y - top < middle) ||
				(item.index > index && clientOffset.y - top > middle)
			) {
				return;
			}

			onMove(item.index, index);

			item.index = index;
		},
	});

	dragRef(dragButtonRef);
	previewRef(dropRef(rowRef));

	useEffect(() => {
		if (keyboardDragging) {
			dragButtonRef.current?.focus();
		}
	}, [index, keyboardDragging]);

	const fixedValue = fieldMapping.type === TYPE_FIXED_VALUE;

	const objectFields = getObjectFields(
		objectDefinitions,
		fieldMapping.sourceClassName
	);

	const handleSourceFieldNameChange = (value: string) => {
		const nextFixedValue = value === TYPE_FIXED_VALUE;

		onChange(index, {
			...fieldMapping,
			sourceFieldName: nextFixedValue ? '' : value,
			type: nextFixedValue ? TYPE_FIXED_VALUE : TYPE_DYNAMIC_VALUE,
			value: nextFixedValue ? fieldMapping.value : '',
		});
	};

	const handleKeyDown = (event: React.KeyboardEvent<HTMLButtonElement>) => {
		if (event.key === 'Enter') {
			event.preventDefault();

			setKeyboardDragging(!keyboardDragging);

			return;
		}

		if (!keyboardDragging) {
			return;
		}

		if (event.key === 'ArrowDown') {
			event.preventDefault();

			onMove(index, index + 1);
		}
		else if (event.key === 'ArrowUp') {
			event.preventDefault();

			onMove(index, index - 1);
		}
		else if (event.key === 'Escape') {
			setKeyboardDragging(false);
		}
	};

	return (
		<div
			className={classNames(
				'c-gap-4 d-flex flex-column pim-field-mapping-row',
				{dragging}
			)}
			ref={rowRef}
		>
			{keyboardDragging && (
				<span aria-live="assertive" className="sr-only">
					{Liferay.Language.get(
						'use-up-and-down-arrows-to-move-the-field-and-press-enter-to-place-it-in-desired-position'
					)}
				</span>
			)}

			<ClayInput.Group className="c-gap-3">
				<ClayInput.GroupItem className="c-gap-1">
					<label>
						{Liferay.Language.get('source')}

						<RequiredMark />
					</label>

					<ClaySelect
						aria-label={Liferay.Language.get('source')}
						onChange={(event) =>
							onChange(index, {
								...fieldMapping,
								sourceClassName: event.target.value,
								sourceFieldName: '',
							})
						}
						value={fieldMapping.sourceClassName}
					>
						<ClaySelect.Option
							label={Liferay.Language.get('all-structures')}
							value=""
						/>

						{objectDefinitions.map((objectDefinition) => (
							<ClaySelect.Option
								key={objectDefinition.className}
								label={objectDefinition.label}
								value={objectDefinition.className}
							/>
						))}
					</ClaySelect>
				</ClayInput.GroupItem>

				<ClayInput.GroupItem className="c-gap-1">
					<div className="align-items-center d-flex flex-fill">
						<label>
							{Liferay.Language.get('source-attribute')}

							<RequiredMark />
						</label>

						<div className="ml-auto">
							<ClayButtonWithIcon
								aria-label={sub(
									Liferay.Language.get('drag-x'),
									Liferay.Language.get('source')
								)}
								aria-pressed={keyboardDragging}
								className="rounded-circle"
								onBlur={() => setKeyboardDragging(false)}
								onKeyDown={handleKeyDown}
								ref={dragButtonRef}
								size="xs"
								spritemap={spritemap}
								symbol="drag"
							/>

							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('delete-row')}
								className="ml-1 rounded-circle"
								disabled={!deletable}
								onClick={() => onDelete(index)}
								size="xs"
								spritemap={spritemap}
								symbol="hr"
							/>

							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('add-row')}
								className="ml-1 rounded-circle"
								onClick={onAdd}
								size="xs"
								spritemap={spritemap}
								symbol="plus"
							/>
						</div>
					</div>

					<ClaySelect
						aria-label={Liferay.Language.get('source-attribute')}
						onChange={(event) =>
							handleSourceFieldNameChange(event.target.value)
						}
						value={
							fixedValue
								? TYPE_FIXED_VALUE
								: fieldMapping.sourceFieldName
						}
					>
						<ClaySelect.Option
							label={Liferay.Language.get('not-mapped')}
							value=""
						/>

						{objectFields.map((objectField) => (
							<ClaySelect.Option
								key={objectField.name}
								label={`${objectField.label} (${objectField.name})`}
								value={objectField.name}
							/>
						))}

						<ClaySelect.Option
							label={Liferay.Language.get('fixed-value')}
							value={TYPE_FIXED_VALUE}
						/>
					</ClaySelect>
				</ClayInput.GroupItem>
			</ClayInput.Group>

			{fixedValue && (
				<ClayInput.Group>
					<ClayInput.GroupItem className="c-gap-1">
						<label>
							{Liferay.Language.get('value')}

							<RequiredMark />
						</label>

						<ClayInput
							aria-label={Liferay.Language.get('value')}
							onChange={(event) =>
								onChange(index, {
									...fieldMapping,
									value: event.target.value,
								})
							}
							type="text"
							value={fieldMapping.value}
						/>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			)}
		</div>
	);
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useMemo} from 'react';
import {useNavigate} from 'react-router-dom';
import Table, {IRow} from '~/components/Table/Table';
import i18n from '~/utils/I18n';

import {ISVRow} from '../SVTable/SVTable';

import '../SVTable/SVTable.css';

export interface IColumn {
	columnKey: string;
	label: string;
}

const MobileContent = ({
	mobileAffectedVersions,
	row,
}: {
	mobileAffectedVersions: string;
	row: ISVRow;
}) => {
	return (
		<div>
			<div>
				<div className="d-flex justify-content-between">
					<div className="align-items-center d-flex">
						<div className="font-weight-bold sv-name sv-wrap-text">
							{row.cveIds}
						</div>

						<div
							className={`ml-2 px-2 sv-severity sv-severity-${row.severity?.toLowerCase()} text-center`}
						>
							{row.severity}
						</div>
					</div>

					<div>{row.published}</div>
				</div>

				<div className="sv-summary sv-wrap-text text-neutral-8">
					{row.summary}
				</div>
			</div>

			<div className="d-flex flex-column mt-2">
				<div>
					<span className="text-secondary">
						{i18n.translate('category')}:
					</span>{' '}
					{row.category}
				</div>

				<div>
					<span className="text-secondary">
						{i18n.translate('classification')}:
					</span>{' '}
					{row.issueClassification}
				</div>

				<div>
					<span className="text-secondary">
						{i18n.translate('affected-version')}:
					</span>{' '}
					{mobileAffectedVersions}
				</div>
			</div>
		</div>
	);
};

interface IProps {
	rows: ISVRow[];
}

const SVMobileTable = ({rows}: IProps) => {
	const navigate = useNavigate();

	const handleRowClick = (row: IRow) => {
		if (row.link) {
			navigate(row.link);
		}
	};

	const mobileColumns = [
		{
			className: 'sv-mobile-content-column',
			columnKey: 'content',
			label: '',
		},
	];

	const mobileRows = useMemo(
		() =>
			rows.map((row) => {
				const versions = row.affectedVersions ?? [];

				const remaining = versions.length - 1;

				const mobileAffectedVersions = `${versions
					.slice(0, 1)
					.join(
						', '
					)}${remaining > 0 ? ` +${remaining} others` : ''}`;

				return {
					content: (
						<MobileContent
							mobileAffectedVersions={mobileAffectedVersions}
							row={row}
						/>
					),
					link: row.link,
				};
			}),
		[rows]
	);

	return (
		<Table
			className="sv"
			columns={mobileColumns}
			onRowClick={handleRowClick}
			rows={mobileRows}
		/>
	);
};

export default SVMobileTable;

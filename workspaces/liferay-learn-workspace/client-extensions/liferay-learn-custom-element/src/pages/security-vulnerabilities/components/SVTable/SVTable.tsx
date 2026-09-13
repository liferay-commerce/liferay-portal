/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useMemo} from 'react';
import {useNavigate} from 'react-router-dom';
import Table, {IRow} from '~/components/Table/Table';
import i18n from '~/utils/I18n';

import SVAffectedVersions from './components/SVAffectedVersions/SVAffectedVersions';

import './SVTable.css';

export interface IColumn {
	className?: string;
	columnKey: string;
	label: string;
}

export interface ISVRow {
	affectedVersions: string[];
	category?: string;
	cveIds?: string;
	issueClassification?: string;
	link?: string;
	published?: string;
	severity?: string;
	summary?: string;
}

interface IProps {
	rows: ISVRow[];
}

const SVTable = ({rows}: IProps) => {
	const navigate = useNavigate();

	const columns: IColumn[] = [
		{
			className: 'sv-priority-summary-column',
			columnKey: 'prioritySummary',
			label: i18n.translate('priority-summary'),
		},
		{
			columnKey: 'severity',
			label: i18n.translate('severity'),
		},
		{
			columnKey: 'category',
			label: i18n.translate('category'),
		},
		{
			columnKey: 'issueClassification',
			label: i18n.translate('classification'),
		},
		{
			columnKey: 'affectedVersion',
			label: i18n.translate('affected-version'),
		},
		{
			columnKey: 'published',
			label: i18n.translate('published'),
		},
	];

	const tableRows = useMemo(
		() =>
			rows.map((row) => ({
				affectedVersion: (
					<div>
						<SVAffectedVersions
							affectedVersions={row.affectedVersions}
						/>
					</div>
				),
				category: row.category,
				issueClassification: row.issueClassification,
				link: row.link,
				prioritySummary: (
					<div>
						<div className="align-items-center d-flex">
							<div className="font-weight-bold sv-name sv-wrap-text">
								{row.cveIds}
							</div>
						</div>

						<div className="sv-summary sv-wrap-text text-neutral-8">
							{row.summary}
						</div>
					</div>
				),
				published: row.published,
				severity: (
					<div
						className={`font-weight-semi-bold sv-severity sv-severity-${row.severity?.toLowerCase()} text-center`}
					>
						{row.severity}
					</div>
				),
			})),
		[rows]
	);

	const handleRowClick = (row: IRow) => {
		if (row.link) {
			navigate(row.link);
		}
	};

	return (
		<Table
			className="sv"
			columns={columns}
			onRowClick={handleRowClick}
			rows={tableRows}
		/>
	);
};

export default SVTable;

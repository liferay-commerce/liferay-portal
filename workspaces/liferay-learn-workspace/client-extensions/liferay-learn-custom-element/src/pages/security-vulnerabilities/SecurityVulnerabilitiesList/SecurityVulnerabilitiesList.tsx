/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayPaginationBarWithBasicItems} from '@clayui/pagination-bar';
import {useMemo} from 'react';
import CloudLockIcon from '~/assets/CloudLock';
import {SVWaves} from '~/assets/SVWaves';
import i18n from '~/utils/I18n';
import {getFormattedDate} from '~/utils/getFormattedDate';

import {IJiraIssue} from '../../../hooks/useJiraIssue';
import useJiraSearch, {
	IProps as IJiraSearch,
} from '../../../hooks/useJiraSearch';
import useJiraVersions from '../../../hooks/useJiraVersions';
import {JiraEnum} from '../../../utils/constants/JiraEnum';
import {FILTER_OPTIONS} from '../../../utils/constants/filterOptions';
import {
	paginationDeltas,
	paginationLabels,
} from '../../../utils/constants/paginationOptions';
import {SORT_OPTIONS} from '../../../utils/constants/sortOptions';
import SVFilter from '../components/SVFilter/SVFilter';
import SVHeader from '../components/SVHeader/SVHeader';
import SVMobileTable from '../components/SVMobileTable/SVMobileTable';
import SVSearch from '../components/SVSearch/SVSearch';
import SVTable, {ISVRow} from '../components/SVTable/SVTable';

import './SecurityVulnerabilitiesList.css';

const SecurityVulnerabilitiesList = () => {
	const defaultParams: IJiraSearch = useMemo(
		() => ({
			[JiraEnum.PAGE]: 1,
			[JiraEnum.PAGE_SIZE]: 15,
		}),
		[]
	);

	const {jiraSearch, loading, searchParams, updateSearchParams} =
		useJiraSearch(defaultParams);

	const {jiraVersions} = useJiraVersions();

	const setPage = (page: number) => {
		updateSearchParams({
			[JiraEnum.PAGE]: page,
		});
	};

	const setPageSize = (pageSize: number) => {
		updateSearchParams({
			[JiraEnum.PAGE]: 1,
			[JiraEnum.PAGE_SIZE]: pageSize,
		});
	};

	const rows = useMemo<ISVRow[] | undefined>(() => {
		if (jiraSearch?.[JiraEnum.ISSUES]) {
			return jiraSearch?.[JiraEnum.ISSUES].map((issue: IJiraIssue) => {
				const fields = issue[JiraEnum.FIELDS];

				return {
					affectedVersions:
						fields?.[JiraEnum.AFFECTED_VERSIONS] ?? [],
					category: fields?.[JiraEnum.CATEGORIES]
						?.map(String)
						.join(', '),
					cveIds: fields?.[JiraEnum.CVE_IDS],
					issueClassification:
						fields?.[JiraEnum.ISSUE_CLASSIFICATION],
					link: `/${issue?.[JiraEnum.KEY]}`,
					published: getFormattedDate(
						fields?.[JiraEnum.PUBLISHED_DATE],
						'day2DMonthSYearN'
					),
					severity: fields?.[JiraEnum.SEVERITY],
					summary: fields?.[JiraEnum.SUMMARY],
				};
			});
		}
		else {
			return undefined;
		}
	}, [jiraSearch]);

	return (
		<>
			<div className="sv-list">
				<div className="align-items-start d-flex flex-column justify-content-center mb-5 sv-list-header text-left">
					<div className="container-fluid-max-xl sv-search-container">
						<SVHeader
							description="Welcome to the Liferay Security Advisories
							dashboard. Use this tool to search and monitor
							documented security vulnerabilities, CVE
							identifiers, and classification statuses across
							Liferay products, including DXP, PaaS, and SaaS. For
							detailed information regarding our triage protocols,
							patching timelines, or to responsibly report a
							security issue, please visit the official Liferay
							Security Statement"
							icon={<CloudLockIcon className="cloud-lock-icon" />}
							title="security-advisories"
						>
							<SVSearch
								keywords={
									searchParams.get(JiraEnum.KEYWORDS) || ''
								}
								onChange={(keywords) =>
									updateSearchParams({
										[JiraEnum.KEYWORDS]: keywords,
										[JiraEnum.PAGE]: 1,
									})
								}
							/>
						</SVHeader>
					</div>

					<div className="align-items-end d-flex justify-content-end position-absolute sv-gradient">
						<SVWaves />
					</div>
				</div>

				<div className="container-fluid container-fluid-max-xl">
					<div className="row sv-table-content">
						<div className="col-12 col-lg-3">
							<SVFilter
								filterOptions={{
									...FILTER_OPTIONS,
									[JiraEnum.AFFECTED_VERSIONS]: jiraVersions,
								}}
								onChange={(params) =>
									updateSearchParams({
										...params,
										[JiraEnum.PAGE]: 1,
									})
								}
								params={searchParams}
								sortOptions={SORT_OPTIONS}
							/>
						</div>

						<div className="col-12 col-lg-9">
							{loading ? (
								<span className="cp-spinner ml-2 spinner-border spinner-border-sm"></span>
							) : rows?.length ? (
								<>
									<div className="sv-desktop-table">
										<SVTable rows={rows} />
									</div>

									<div className="sv-mobile-table">
										<SVMobileTable rows={rows} />
									</div>

									<ClayPaginationBarWithBasicItems
										active={jiraSearch?.[JiraEnum.PAGE]}
										activeDelta={
											jiraSearch?.[JiraEnum.PAGE_SIZE]
										}
										deltas={paginationDeltas}
										labels={paginationLabels}
										onActiveChange={(value: number) =>
											setPage(value)
										}
										onDeltaChange={(value: number) =>
											setPageSize(value)
										}
										totalItems={
											jiraSearch?.[JiraEnum.TOTAL]!
										}
									/>
								</>
							) : (
								<div className="py-2">
									{i18n.translate(
										'the-requested-search-does-not-exist-in-our-database-please-try-again-with-different-criteria'
									)}
								</div>
							)}
						</div>
					</div>
				</div>
			</div>
		</>
	);
};

export default SecurityVulnerabilitiesList;

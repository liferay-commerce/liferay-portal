/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Link, useParams} from 'react-router-dom';
import {SVWaves} from '~/assets/SVWaves';
import i18n from '~/utils/I18n';

import useJiraIssue from '../../../hooks/useJiraIssue';
import {JiraEnum} from '../../../utils/constants/JiraEnum';

import './SecurityVulnerabilitiesItem.css';

const SecurityVulnerabilitiesItem = () => {
	const {id} = useParams();

	const {jiraIssue, loading: issueLoading} = useJiraIssue(id);

	if (!id) {
		return <div>{i18n.translate('sorry-there-are-no-results-found')}</div>;
	}

	if (issueLoading) {
		return (
			<span className="cp-spinner ml-2 spinner-border spinner-border-sm"></span>
		);
	}

	if (!jiraIssue) {
		return <div>{i18n.translate('sorry-there-are-no-results-found')}</div>;
	}

	return (
		<div className="container-fluid container-xl py-4 sv-item">
			<div className="d-flex flex-column justify-content-between mb-3 p-4 sv-item-header">
				<div className="sv-breadcrumbs">
					<Link to="/">{i18n.translate('all-security-reports')}</Link>

					<span className="mx-2">/</span>

					{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.CVE_IDS]}
				</div>

				<div className="align-items-center d-flex my-3">
					<h1 className="mb-0">
						{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.CVE_IDS]}
					</h1>

					<span
						className={`sv-severity sv-severity-${jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.SEVERITY]?.toLowerCase()} text-center`}
					>
						{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.SEVERITY]}
					</span>
				</div>

				{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.SUMMARY] && (
					<span>
						{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.SUMMARY]}
					</span>
				)}

				<div className="align-items-end d-flex justify-content-end position-absolute sv-gradient">
					<SVWaves />
				</div>
			</div>

			<div className="mb-3 row sv-issue-details">
				<div className="col-12 col-lg-9 col-md-8 col-xl-10">
					<div className="sv-item-description">
						<h5 className="text-neutral-10">
							{i18n.translate('summary')}
						</h5>

						<div
							className="sv-structured-data"
							dangerouslySetInnerHTML={{
								__html:
									jiraIssue[JiraEnum.FIELDS]?.[
										JiraEnum.DESCRIPTION
									] || '',
							}}
						/>
					</div>
				</div>

				<div className="col-12 col-lg-3 col-md-4 col-xl-2">
					<div className="sv-item-details">
						{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.AFFECTS] && (
							<div className="mb-4">
								<h5 className="text-neutral-10">
									{i18n.translate('affects')}
								</h5>

								<div className="sv-badge-list">
									{jiraIssue[JiraEnum.FIELDS]?.[
										JiraEnum.AFFECTS
									]
										.split(',')
										.map((affectedVersion) => (
											<span
												className="sv-badge"
												key={affectedVersion.trim()}
											>
												{affectedVersion.trim()}
											</span>
										))}
								</div>
							</div>
						)}

						{jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.CATEGORIES] &&
							jiraIssue[JiraEnum.FIELDS]?.[JiraEnum.CATEGORIES]
								.length > 0 && (
								<div className="mb-4">
									<h5 className="text-neutral-10">
										{i18n.translate('category')}
									</h5>

									<div className="sv-badge-list">
										{jiraIssue[JiraEnum.FIELDS]?.[
											JiraEnum.CATEGORIES
										]?.map((category) => (
											<span
												className="sv-badge"
												key={category}
											>
												{category}
											</span>
										))}
									</div>
								</div>
							)}

						{jiraIssue[JiraEnum.FIELDS]?.[
							JiraEnum.ISSUE_CLASSIFICATION
						] && (
							<div className="mb-4">
								<h5 className="text-neutral-10">
									{i18n.translate('classification')}
								</h5>

								<span className="sv-badge">
									{
										jiraIssue[JiraEnum.FIELDS]?.[
											JiraEnum.ISSUE_CLASSIFICATION
										]
									}
								</span>
							</div>
						)}
					</div>
				</div>
			</div>
		</div>
	);
};

export default SecurityVulnerabilitiesItem;

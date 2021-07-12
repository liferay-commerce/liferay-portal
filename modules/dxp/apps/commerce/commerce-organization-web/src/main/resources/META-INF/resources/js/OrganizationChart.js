/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import {ClayIconSpriteContext} from '@clayui/icon';
import classnames from 'classnames';
import PropTypes from 'prop-types';
import React, {useEffect, useLayoutEffect, useRef, useState} from 'react';

import ChartContext from './ChartContext';
import D3OrganizationChart from './D3OrganizationChart';
import ManagementBar from './ManagementBar/ManagementBar';
import {getOrganization} from './data/organizations';
import {VIEWS} from './utils/constants';

function OrganizationChartApp({rootOrganizationId, spritemap, templatesURL}) {
	const [currentView, updateCurrentView] = useState(VIEWS[0]);
	const [expanded, updateExpanded] = useState(false);
	const [rootData, updateRootData] = useState(null);
	const chartSVGRef = useRef(null);
	const chartInstanceRef = useRef(null);
	const zoomOutRef = useRef(null);
	const zoomInRef = useRef(null);

	useEffect(() => {
		getOrganization(rootOrganizationId).then(updateRootData);
	}, [rootOrganizationId]);

	useLayoutEffect(() => {
		if (rootData && chartSVGRef.current) {
			chartInstanceRef.current = new D3OrganizationChart(
				rootData,
				{
					svg: chartSVGRef.current,
					zoomIn: zoomInRef.current,
					zoomOut: zoomOutRef.current,
				},
				spritemap
			);
		}

		return () => chartInstanceRef.current?.cleanUp();
	}, [rootData, spritemap]);

	return (
		<ClayIconSpriteContext.Provider value={spritemap}>
			<ChartContext.Provider
				value={{
					chartInstanceRef,
					currentView,
					templatesURL,
					updateCurrentView,
				}}
			>
				<ManagementBar />
				<div className={classnames('org-chart-container', {expanded})}>
					<svg className="svg-chart" ref={chartSVGRef} />
					<div className="zoom-controls">
						<ClayButtonWithIcon
							displayType="secondary"
							onClick={() => updateExpanded(!expanded)}
							small
							symbol="expand"
						/>
						<ClayButton.Group className="ml-3">
							<ClayButtonWithIcon
								displayType="secondary"
								ref={zoomOutRef}
								small
								symbol="hr"
							/>
							<ClayButtonWithIcon
								displayType="secondary"
								ref={zoomInRef}
								small
								symbol="plus"
							/>
						</ClayButton.Group>
					</div>
				</div>
			</ChartContext.Provider>
		</ClayIconSpriteContext.Provider>
	);
}

OrganizationChartApp.propTypes = {
	rootOrganizationId: PropTypes.number.isRequired,
	spritemap: PropTypes.string.isRequired,
	templatesURL: PropTypes.shape({
		accountsDetailsPage: PropTypes.string.isRequired,
		organizationsDetailsPage: PropTypes.string.isRequired,
		usersDetailsPage: PropTypes.string.isRequired,
	}),
};

export default OrganizationChartApp;

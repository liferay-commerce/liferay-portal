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

let highlightedLinks;
let highlightedNodes;
let enabled = true;

export function enableHighlight() {
	enabled = true;
}

export function disableHighlight() {
	enabled = false;
}

export function highlight(selectedNode, root, nodesGroup, linksGroup) {
	if (!enabled) {
		return;
	}

	const nodeInstances = [];

	root.each((d) => {
		if (d.data.chartNodeId === selectedNode.data.chartNodeId) {
			nodeInstances.push(d);
		}
	});

	const chartIdsToBeHighlighted = nodeInstances.reduce(
		(chartIdsToBeHighlighted, nodeInstance) => {
			const ancestorsId = nodeInstance
				.ancestors()
				.map((d) => d.data.chartNodeId);

			return chartIdsToBeHighlighted.concat(ancestorsId);
		},
		[]
	);

	highlightedNodes = nodesGroup
		.selectAll('.chart-item')
		.filter((d) => chartIdsToBeHighlighted.includes(d.data.chartNodeId));

	highlightedLinks = linksGroup
		.selectAll('.chart-link')
		.filter((d) =>
			chartIdsToBeHighlighted.includes(d.target.data.chartNodeId)
		);

	highlightedLinks.raise();

	highlightedLinks.classed('highlighted', true);
	highlightedNodes.classed('highlighted', true);
}

export function removeHighlight() {
	if (highlightedLinks && highlightedNodes) {
		highlightedLinks.classed('highlighted', false);
		highlightedNodes.classed('highlighted', false);

		highlightedLinks = null;
		highlightedNodes = null;
	}
}

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

import {DRAGGING_THRESHOLD, ZOOM_EXTENT} from './constants';
import {disableHighlight, enableHighlight} from './highlight';

let mouseStartPositions = null;
let dragging = false;
let chartItems;
let dragHandle;

function hasPositionChanged(start, end) {
	if (!end) {
		return false;
	}

	const diff = Math.max(Math.abs(start.x - end.x), Math.abs(start.y - end.y));

	return diff > DRAGGING_THRESHOLD;
}

function handleMouseDown(event) {
	mouseStartPositions = {
		x: event.offsetX,
		y: event.offsetY,
	};
}

function createDragHandle(startingNode, itemsTotal, nodesWrapper) {
	dragHandle = nodesWrapper
		.append('g')
		.attr('transform', startingNode.attributes.transform.value)
		.attr('class', 'dnd-handle');

	const container = dragHandle
		.append('g')
		.attr('class', 'dnd-handle-content');

	const startingNodeClone = startingNode.cloneNode(true);
	startingNodeClone.classList.remove('highlighted');
	startingNodeClone.removeAttribute('transform');

	const itemsToBeAppended = Math.min(3, itemsTotal - 1);
	const rectPlaceholder = startingNodeClone.querySelector('rect');

	for (let i = itemsToBeAppended; i > 0; i--) {
		container
			.append(() => rectPlaceholder.cloneNode())
			.attr('x', i * 3)
			.attr('y', i * 3);
	}

	container.append(() => startingNodeClone);
}

function handleMouseMove(
	event,
	d,
	startingNode,
	selectedNodeIds,
	nodesGroup,
	currentScale,
	svgRef
) {
	if (!dragging) {
		if (
			hasPositionChanged(mouseStartPositions, {
				x: event.offsetX,
				y: event.offsetY,
			})
		) {
			disableHighlight();

			dragging = true;

			const targetsNotAllowed = new Map();

			/**
			 * Elements to be disabled while dragging:
			 * - dragged nodes
			 * - descendants of dragged nodes
			 * - all users and accounts nodes
			 * - all add buttons
			 * - direct parents
			 */

			chartItems = nodesGroup.selectAll('.chart-item');

			chartItems.each((d) => {
				if (['user', 'account', 'add'].includes(d.data.type)) {
					targetsNotAllowed.set(d.data.chartNodeId, d);
				}

				if (selectedNodeIds.has(d.data.chartNodeId)) {
					targetsNotAllowed.set(d.data.chartNodeId, d);

					if (d.parent) {
						targetsNotAllowed.set(
							d.parent.data.chartNodeId,
							d.parent
						);
					}

					const descendants = d.descendants();

					descendants.forEach((descendant) => {
						targetsNotAllowed.set(
							descendant.data.chartNodeId,
							descendant
						);
					});
				}
			});

			createDragHandle(startingNode, selectedNodeIds.size, nodesGroup);

			chartItems.each((d, index, nodeInstance) => {
				if (targetsNotAllowed.has(d.data.chartNodeId)) {
					nodeInstance[index].classList.add('drop-not-allowed');
				}
				else {
					nodeInstance[index].classList.add('drop-allowed');
				}
			});

			svgRef.classList.add('dragging');
		}

		return;
	}

	const handlerPosition = {
		x:
			(mouseStartPositions.x - event.offsetX) *
				(ZOOM_EXTENT[1] / currentScale) *
				-1 +
			d.y,
		y:
			(mouseStartPositions.y - event.offsetY) *
				(ZOOM_EXTENT[1] / currentScale) *
				-1 +
			d.x,
	};

	dragHandle
		.attr(
			'transform',
			`translate(${handlerPosition.x}, ${handlerPosition.y})`
		)
		.classed('dragging', true);
}

function handleMouseUp(mouseDownEvent, mouseUpEvent, d, svgRef, resolve) {
	if (!dragging) {
		return resolve({
			d,
			event: mouseDownEvent,
			type: 'click',
		});
	}

	mouseStartPositions = null;
	svgRef.classList.remove('dragging');
	dragging = false;
	enableHighlight();

	const target = mouseUpEvent.target.closest('.drop-allowed');

	chartItems.each((_d, index, nodeInstance) => {
		nodeInstance[index].classList.remove('drop-not-allowed');
		nodeInstance[index].classList.remove('drop-allowed');
	});

	dragHandle.remove();

	return resolve({
		d,
		event: mouseDownEvent,
		target: target?.__data__,
		type: 'drop',
	});
}

export default function handleDnd(
	initialEvent,
	d,
	selectedNodeIds,
	svgRef,
	nodesGroup,
	currentScale
) {
	return new Promise((resolve) => {
		handleMouseDown(initialEvent);

		const nodesToBeDragged = selectedNodeIds.has(d.data.chartNodeId)
			? selectedNodeIds
			: new Set([d.data.chartNodeId]);
		const startingNodeInstance = initialEvent.currentTarget;

		const _handleMouseMove = (event) =>
			handleMouseMove(
				event,
				d,
				startingNodeInstance,
				nodesToBeDragged,
				nodesGroup,
				currentScale,
				svgRef
			);

		svgRef.addEventListener('mousemove', _handleMouseMove);

		window.addEventListener(
			'mouseup',
			(event) => {
				svgRef.removeEventListener('mousemove', _handleMouseMove);
				handleMouseUp(initialEvent, event, d, svgRef, resolve);
			},
			{once: true}
		);
	});
}

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

import * as d3 from 'd3';
import {openToast} from 'frontend-js-web';

import {getAccount} from './data/accounts';
import {getOrganization} from './data/organizations';
import {
	DY,
	RECT_SIZES,
	TRANSITIONS_DISABLED,
	TRANSITION_TIME,
	ZOOM_EXTENT,
} from './utils/constants';
import {
	formatChild,
	formatItem,
	formatItemDescription,
	formatItemName,
	formatRootData,
	getEntityId,
	getMinWidth,
	hideChildren,
	insertChildrenIntoNode,
	showChildren,
	tree,
} from './utils/index';
import {fillEntityNode, getLinkDiagonal} from './utils/paint';
class D3OrganizationChart {
	constructor(
		rootData,
		refs,
		spritemap,
		modalActions,
		rootVisible = true
	) {
		this._spritemap = spritemap;
		this._refs = refs;
		this._handleZoomInClick = this._handleZoomInClick.bind(this);
		this._handleZoom = this._handleZoom.bind(this);
		this._handleZoomOutClick = this._handleZoomOutClick.bind(this);
		this._handleNodeClick = this._handleNodeClick.bind(this);
		this._handleNodeMouseDown = this._handleNodeMouseDown.bind(this);
		this._hideChildrenAndUpdate = this._hideChildrenAndUpdate.bind(this);
		this._currentScale = 1;
		this._modalActions = modalActions;
		this._selectedNodes = new Map();
		this._rootVisible = rootVisible;
		this._initialiseZoomListeners(this._refs);
		this._createChart();
		this._initializeData(formatRootData(rootData, this._rootVisible));
		this._update(this._root);
	}

	addNodes(children, type, parentData) {
		const parentId = getEntityId(parentData);

		const formattedChildren = children.map((child) =>
			formatChild(child, type)
		);

		let lastNodeAdded = null;

		this._root.each((d) => {
			if (getEntityId(d.data) === parentId) {
				const {children} = insertChildrenIntoNode(formattedChildren, d);

				lastNodeAdded = children[children.length - 1];
				this._update(d, false);
			}
		});

		if (lastNodeAdded) {
			this._recenterViewport(lastNodeAdded);
		}
	}

	deleteNodes(nodesToBeDeleted, allNodeInstances = true, forceUpdate = true) {
		const propertyToMatch = allNodeInstances
			? 'chartNodeId'
			: 'chartNodeNumber';

		const nodesToBeDeletedIds = new Set(
			nodesToBeDeleted.map((node) => node[propertyToMatch])
		);

		this._root.each((node) => {
			if (nodesToBeDeletedIds.has(node.data[propertyToMatch])) {
				if (!node.parent) {
					openToast({
						message: Liferay.Language.get(
							'root-organization-cant-be-deleted'
						),
						type: 'danger',
					});

					return;
				}

				if (node.parent.children.length === 1) {
					node.parent.children = null;
					node.parent.data.children = null;
				}
				else {
					const childIndex = node.parent.children.findIndex((d) =>
						nodesToBeDeletedIds.has(d.data[propertyToMatch])
					);

					node.parent.children.splice(childIndex, 1);
					node.parent.data.children = node.parent.children.map(
						(child) => child.data
					);
				}
			}
		});

		if (forceUpdate) {
			this._update(this._root, false, null, true);
		}
	}

	updateNodeContent(nodeData) {
		this._root.each((d) => {
			if (d.data.chartNodeId === nodeData.chartNodeId) {
				d.data = {
					...nodeData,
					chartNodeNumber: d.data.chartNodeNumber,
				};
			}
		});

		const chartItems = this._nodesGroup.selectAll('.chart-item');
		const nodesToBeUpdated = chartItems.filter(
			(chartItem) => chartItem.data.id === nodeData.id
		);

		nodesToBeUpdated.selectAll('.node-title').text(formatItemName);

		nodesToBeUpdated
			.selectAll('.node-description')
			.text(formatItemDescription);
	}

	collapseAllNodes() {
		this._currentScale = ZOOM_EXTENT[1];
		this._selectedNodes.clear();

		if (this._rootVisible) {
			hideChildren(this._root);
			this._update(this._root);

			if (this._root._children) {
				this._root._children.forEach((d) =>
					d.descendants().forEach(hideChildren)
				);
			}
		}
		else {
			const sourcesMap = new Map();

			this._root.children.forEach((child) => {
				const descendants = child.descendants();
				descendants.shift();

				descendants.forEach((descendant) => {
					sourcesMap.set(descendant.data.chartNodeNumber, child);
				});

				hideChildren(child);
			});

			this._update(this._root, true, sourcesMap);

			this._root.children.forEach((child) => {
				if (child._children) {
					child._children.forEach((d) =>
						d.descendants().forEach(hideChildren)
					);
				}
			});
		}
	}

	updateRoot(root) {
		this._initializeData(formatRootData(root, this._rootVisible));
	}

	_hideChildrenAndUpdate(d) {
		hideChildren(d);
		tree(d);
	}

	_initialiseZoomListeners() {
		this._refs.zoomIn.disabled = true;

		this._refs.zoomIn.addEventListener('click', this._handleZoomInClick);
		this._refs.zoomOut.addEventListener('click', this._handleZoomOutClick);
	}

	_handleZoom() {
		this._currentScale = d3.event.transform.k;

		this._handleZoomButtonsState();
		this._zoomHandler.attr('transform', d3.event.transform);
	}

	_handleZoomInClick() {
		this._currentScale = this._currentScale * 2;

		this._handleZoomButtonsState();
		this._animateZoom();
	}

	_handleZoomOutClick() {
		this._currentScale = this._currentScale * 0.5;

		this._handleZoomButtonsState();
		this._animateZoom();
	}

	_handleZoomButtonsState() {
		this._refs.zoomIn.disabled = Boolean(
			this._currentScale === ZOOM_EXTENT[1]
		);
		this._refs.zoomOut.disabled = Boolean(
			this._currentScale === ZOOM_EXTENT[0]
		);
	}

	_animateZoom() {
		this._createTransition();
		this._handleTransition(this.svg).call(
			this._zoom.scaleTo,
			this._currentScale
		);
	}

	_handleNodeClick(event, d) {
		event.stopPropagation();

		if (event.shiftKey && !d.data.selectable) {
			return;
		}

		if (d.data.type === 'user') {
			return this._recenterViewport(d);
		}

		let expanded = true;

		if (this._selectedNodes.has(d.data.chartNodeId)) {
			expanded = false;
		}

		if (!event.shiftKey) {
			this._selectedNodes.clear();
		}

		if (expanded) {
			this._selectedNodes.set(d.data.chartNodeId, d);
		}
		else {
			this._selectedNodes.delete(d.data.chartNodeId);
		}

		if (!d.data.fetched) {
			const getData =
				d.data.type === 'organization' ? getOrganization : getAccount;

			return getData(getEntityId(d.data))
				.then((rawData) => formatItem(rawData, d.data.type))
				.then((data) => insertChildrenIntoNode(data.children, d))
				.then(() => {
					d.data.fetched = true;

					this._update(d);
				});
		}

		if (expanded) {
			showChildren(d);
		}
		else {
			hideChildren(d);
		}

		this._update(d);
	}

	_createChart() {
		this._zoom = d3
			.zoom()
			.scaleExtent(ZOOM_EXTENT)
			.on('zoom', this._handleZoom);

		this.svg = d3
			.select(this._refs.svg)
			.call(this._zoom);

		this._zoomHandler = this.svg.append('g');

		const dataWrapper = this._zoomHandler
			.append('g')
			.attr('class', 'chart-data-wrapper');

		this._linksGroup = dataWrapper.append('g');
		this._nodesGroup = dataWrapper.append('g');
	}

	_initializeData(initialData) {
		this._root = d3.hierarchy(initialData, (d) => d.children);
		this._root.x0 = DY / 2;
		this._root.y0 = 0;
	}

	_handleNodeMouseDown(d) {
		d3.event.stopPropagation();

		if (d.data.type === 'user') {
			this._createTransition();

			return this._recenterViewport(d);
		}

		return this._handleNodeClick(d3.event, d);
	}

	_update(source, recenter = true, sourcesMap, showDeleteTransition) {
		tree(this._root);

		this._root.eachBefore((d) => {
			d.x0 = d.x;
			d.y0 = d.y;
		});

		this._createTransition();

		if (recenter) {
			this._recenterViewport(source);
		}
		this._updateLinks(source, showDeleteTransition);
		this._updateNodes(source, sourcesMap, showDeleteTransition);
	}

	_createTransition() {
		this.transition = this.svg
			.transition()
			.duration(TRANSITION_TIME)
			.tween(
				'resize',
				window.ResizeObserver
					? null
					: () => () => this.svg.dispatch('toggle')
			);
	}

	_recenterViewport(source) {
		const {height, width} = this._refs.svg.getBoundingClientRect();
		const k = this._currentScale;

		let y0;

		if (source.depth || this._rootVisible) {
			y0 = source.y0 + RECT_SIZES[source.data.type].width / 2;
		}
		else {
			y0 = source.children[0].y0 + getMinWidth(source.children) / 2;
		}

		const x = -y0 * k + width / 2;
		const y = -source.x0 * k + height / 2;

		this._handleTransition(this.svg).call(
			this._zoom.transform,
			d3.zoomIdentity.translate(x, y).scale(k)
		);
	}

	_handleTransition(selection) {
		return TRANSITIONS_DISABLED
			? selection
			: selection.transition(this.transition);
	}

	_updateLinks(source, showDeleteTransition) {
		const links = this._root.links();
		const filteredLinks = this._rootVisible
			? links
			: links.filter((d) => d.source.depth);

		const bindedLinks = this._linksGroup
			.selectAll('.chart-link')
			.data(filteredLinks, (d) => d.target.data.chartNodeNumber);

		this.bindedChartLink = bindedLinks
			.enter()
			.append('path')
			.attr('class', 'chart-link')
			.attr('d', (d) => getLinkDiagonal(d, source, 'enter'))
			.attr('opacity', 0);

		this._handleTransition(bindedLinks.merge(this.bindedChartLink))
			.attr('d', (d) => getLinkDiagonal(d, source))
			.attr('opacity', 1);

		this._handleTransition(bindedLinks.exit())
			.remove()
			.attr('d', (d) =>
				getLinkDiagonal(
					d,
					source,
					d.target.data.type === 'add' || showDeleteTransition
						? null
						: 'exit'
				)
			)
			.attr('opacity', 0);
	}

	_updateNodes(source, sourcesMap, showDeleteTransition) {
		const dataNodes = this._root.descendants().reverse();

		const filteredDataNodes = this._rootVisible
			? dataNodes
			: dataNodes.filter((d) => d.depth !== 0);

		this.bindedNodes = this._nodesGroup
			.selectAll('.chart-item')
			.data(filteredDataNodes, (d) => d.data.chartNodeNumber);

		const nodes = this.bindedNodes
			.enter()
			.append('g')
			.attr('opacity', 0)
			.attr('class', (d) => `chart-item chart-item-${d.data.type}`);

		const addButtons = nodes.filter((d) => d.data.type === 'add');

		addButtons.attr(
			'transform',
			(d) => `translate(${d.y},${d.x}) scale(0)`
		);

		const children = nodes.filter((d) => d.data.type !== 'add');

		children.attr('transform', `translate(${source.y0},${source.x0})`);
		fillEntityNode(children, this._spritemap);

		children
			.on('mousedown', this._handleNodeMouseDown);

		this._handleTransition(this.bindedNodes.merge(nodes))
			.attr('opacity', 1)
			.attr('transform', (d) =>
				d.data.type !== 'add'
					? `translate(${d.y},${d.x})`
					: `translate(${d.y},${d.x}) scale(1)`
			);

		this.bindedNodes.classed('selected', (d) =>
			this._selectedNodes.has(d.data.chartNodeId)
		);

		this._handleTransition(this.bindedNodes.exit())
			.remove()
			.attr('opacity', 0)
			.attr('transform', (d) => {
				if (showDeleteTransition || d.data.type == 'add') {
					return `translate(${d.y},${d.x}) scale(0)`;
				}

				const target = sourcesMap
					? sourcesMap.get(d.data.chartNodeNumber)
					: source;

				return `translate(${target.y},${target.x})`;
			});
	}
}

export default D3OrganizationChart;

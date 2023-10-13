/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayPanel from '@clayui/panel';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {sub} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

import {parseValue} from '../util/index';

function ItemInfoViewOptions({childItems, options}) {
	const [expanded, setExpanded] = useState(false);

	const OptionsRenderer = (
		<div className="child-items">
			{options.map((option, index) => {
				const {
					skuId,
					skuOptionName,
					skuOptionValueName,
					value,
				} = option;

				const childItem = (childItems || []).find(
					(childItem) => childItem.skuId === parseInt(skuId, 10)
				);

				const {name, quantity, skuUnitOfMeasure} = childItem || {};

				return name ? (
					<div className="item-info-extra pt-2" key={index}>
						<h6 className="item-name">{skuOptionName}</h6>

						<p className="item-sku">
							<span>
								<span>
									{parseValue(skuOptionValueName) ||
										parseValue(value)}
								</span>

								<span className="pl-2">
									({quantity} &times; {name}{' '}

									{skuUnitOfMeasure?.key || ''})
								</span>
							</span>
						</p>
					</div>
				) : (
					<div className="item-info-extra pt-2">
						<h6 className="item-name">{skuOptionName}</h6>

						<p className="item-sku">
							{parseValue(skuOptionValueName) ||
								parseValue(value)}
						</p>
					</div>
				);
			})}
		</div>
	);

	return options.length > 1 ? (
		<ClayPanel
			className="item-info-collapse mb-0"
			collapsable
			displayTitle={sub(
				Liferay.Language.get('x-options'),
				expanded
					? Liferay.Language.get('hide')
					: Liferay.Language.get('show')
			)}
			displayType="secondary"
			expanded={expanded}
			onExpandedChange={(expanded) => {
				setExpanded(expanded);
			}}
			showCollapseIcon
		>
			<ClayPanel.Body>{OptionsRenderer}</ClayPanel.Body>
		</ClayPanel>
	) : (
		OptionsRenderer
	);
}

function ItemInfoViewReplacement({replacedSku}) {
	return (
		<div className="item-info-replacement">
			<ClayLabel displayType="info">
				{Liferay.Language.get('replacement')}
			</ClayLabel>

			<ClayTooltipProvider>
				<span
					data-tooltip-align="left"
					title={sub(
						Liferay.Language.get('replacement-product-for-x'),
						replacedSku
					)}
				>
					<ClayIcon aria-label="Info" symbol="info-circle" />
				</span>
			</ClayTooltipProvider>
		</div>
	);
}

function ItemInfoViewBase({name, sku}) {
	return (
		<div className="item-info-base">
			<h5 className="item-name">{name}</h5>

			<p className="item-sku">{sku}</p>
		</div>
	);
}

function ItemInfoView({childItems = [], name, options = '', replacedSku, sku}) {
	const hasReplacement = !!replacedSku;

	return (
		<>
			<ItemInfoViewBase name={name} sku={sku} />

			{hasReplacement && (
				<ItemInfoViewReplacement replacedSku={replacedSku} />
			)}

			<ItemInfoViewOptions childItems={childItems} options={options} />
		</>
	);
}

ItemInfoView.propTypes = {
	childItems: PropTypes.array,
	name: PropTypes.string.isRequired,
	options: PropTypes.string,
	sku: PropTypes.string.isRequired,
};

export default ItemInfoView;

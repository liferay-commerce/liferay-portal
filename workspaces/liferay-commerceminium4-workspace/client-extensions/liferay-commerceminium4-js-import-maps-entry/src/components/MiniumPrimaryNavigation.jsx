/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { VerticalNav as ClayVerticalNav } from '@clayui/core';
import ClayIcon from '@clayui/icon';
import classnames from 'classnames';
import React from 'react';

const DEFAULT_LAYOUT_ICONS = {
    'account-management': 'briefcase',
    'catalog': 'tag',
    'dashboard': 'analytics',
    'orders': 'order-form',
    'pending-orders': '',
    'placed-orders': '',
    'quotes': 'order-pencil',
    'returns': 'document-pending',
    'shipments': 'truck',
};

function toItems(entries) {
    return entries.length
        ? entries.map((entry) => ({
            active: entry.active,
            href: entry?.href,
            icon: entry?.icon ?? DEFAULT_LAYOUT_ICONS[
                    entry.friendlyURL.replace('/', '')
                ],
            id: entry.id,
            initialExpanded: entry.active && !!entry?.items?.length,
            items: entry?.items?.length
                ? entry.items.map((item) => {
                   item.isChild = true;

                   return item;
                }) : [],
            label: entry.label,
        })) : {};
}

export default function MiniumPrimaryNavigation({
	entries,
	spritemap,
}) {
    return (
        <div className="minium-primary-navigation">
            <ClayVerticalNav
                items={toItems(entries)}
                spritemap={spritemap}
            >
                {item => (
                    <ClayVerticalNav.Item
                        active={item.active}
                        className={classnames({
                            'has-children': !!item?.items?.length,
                            'is-child': !!item?.isChild
                        })}
                        href={item.href}
                        initialExpanded={item.initialExpanded}
                        items={item.items}
                        key={item.id}
                    >
					<span className="align-items-center d-flex">
						{item?.icon && (
                            <span className={
                                classnames('mx-2', 'nav-item-icon', {
                                    'bg-primary': item.active,
                                })
                            }>
                                <ClayIcon
                                    spritemap={spritemap}
                                    symbol={item.icon}
                                />
                            </span>
                        )}

                        <span className="nav-item-label">
                            {item.label}
                        </span>
					</span>
                    </ClayVerticalNav.Item>
                )}
            </ClayVerticalNav>
        </div>
    );
}
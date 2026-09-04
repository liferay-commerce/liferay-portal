/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ReactNode} from 'react';
import i18n from '~/utils/I18n';

import './SVHeader.css';

type SVHeaderProps = {
	children: ReactNode;
	className?: string;
	description: string;
	icon?: JSX.Element;
	title: string;
};

const SVHeader = ({
	children,
	className,
	description,
	icon,
	title,
}: SVHeaderProps) => {
	return (
		<div className={`${className} d-flex flex-column sv-header`}>
			<span className="sv-header-pill">Updates</span>

			<div className="align-items-center d-flex m-0">
				<span className="align-items-center d-flex sv-header-icon">
					{icon}
				</span>

				<h1 className="m-0 sv-header-title text-neutral-0">
					{i18n.translate(title)}
				</h1>
			</div>

			<div className="sv-header-description">{description}</div>

			{children}
		</div>
	);
};

export default SVHeader;

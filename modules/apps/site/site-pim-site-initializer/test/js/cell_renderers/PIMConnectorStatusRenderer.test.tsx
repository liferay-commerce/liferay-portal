/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import React from 'react';

import PIMConnectorStatusRenderer from '../../../src/main/resources/META-INF/resources/js/cell_renderers/PIMConnectorStatusRenderer';

describe('PIMConnectorStatusRenderer', () => {
	it('labels an active connector as active', () => {
		render(<PIMConnectorStatusRenderer value={true} />);

		expect(screen.getByText('active')).toBeInTheDocument();
	});

	it('labels an inactive connector as inactive', () => {
		render(<PIMConnectorStatusRenderer value={false} />);

		expect(screen.getByText('inactive')).toBeInTheDocument();
	});
});

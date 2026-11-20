/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import React from 'react';

import PIMConnectorLastExecutionStatusRenderer from '../../../src/main/resources/META-INF/resources/js/cell_renderers/PIMConnectorLastExecutionStatusRenderer';

describe('PIMConnectorLastExecutionStatusRenderer', () => {
	it('labels a failed execution as failed', () => {
		render(<PIMConnectorLastExecutionStatusRenderer value="failed" />);

		expect(screen.getByText('failed').closest('.label')).toHaveClass(
			'label-danger'
		);
	});

	it('labels a connector that never ran as never executed', () => {
		render(
			<PIMConnectorLastExecutionStatusRenderer value="never-executed" />
		);

		expect(
			screen.getByText('never-executed').closest('.label')
		).toHaveClass('label-secondary');
	});

	it('labels a successful execution as success', () => {
		render(<PIMConnectorLastExecutionStatusRenderer value="success" />);

		expect(screen.getByText('success').closest('.label')).toHaveClass(
			'label-success'
		);
	});
});

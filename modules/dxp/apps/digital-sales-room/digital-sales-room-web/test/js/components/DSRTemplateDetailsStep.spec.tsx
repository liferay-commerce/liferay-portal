/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, renderHook, screen} from '@testing-library/react';
import React, {useState} from 'react';

import DSRTemplateDetailsStep from '../../../src/main/resources/META-INF/resources/js/components/DSRRoomDetailsStep';
import {DSRContext} from '../../../src/main/resources/META-INF/resources/js/components/DSRTemplateInitializer';
import {
	TDSRDataContext,
	TDSRRoomDetailsStepProps,
} from '../../../src/main/resources/META-INF/resources/js/components/DSRTypes';

let {result: useStateHookResult} = renderHook(() =>
	useState<TDSRDataContext>({
		banner: {},
		clientLogo: {},
		clientName: '',
		errors: {},
		friendlyURL: '',
		primaryColor: '0B5FFF',
		roomName: '',
		secondaryColor: 'FFFFFF',
	})
);

const component = ({
	numberOfSteps = 1,
	setHandleStepSubmit,
}: TDSRRoomDetailsStepProps) => {
	return (
		<DSRContext.Provider
			value={{
				dataContext: useStateHookResult.current[0],
				setDataContext: useStateHookResult.current[1],
			}}
		>
			<DSRTemplateDetailsStep
				numberOfSteps={numberOfSteps}
				setHandleStepSubmit={setHandleStepSubmit}
			/>
		</DSRContext.Provider>
	);
};

const renderComponent = ({
	numberOfSteps = 1,
	setHandleStepSubmit,
}: TDSRRoomDetailsStepProps) => {
	return render(component({numberOfSteps, setHandleStepSubmit}));
};

describe('DSRTemplateDetailsStep', () => {
	beforeEach(() => {
		({result: useStateHookResult} = renderHook(() =>
			useState({
				banner: {},
				clientLogo: {},
				clientName: '',
				errors: {},
				friendlyURL: '',
				primaryColor: '0B5FFF',
				roomName: '',
				secondaryColor: 'FFFFFF',
			})
		));
	});

	it('renders the correct UI elements', async () => {
		renderComponent({
			numberOfSteps: 1,
			setHandleStepSubmit: () => {},
		});

		expect(screen.getByTestId('bannerImage')).toBeInTheDocument();
		expect(screen.getByTestId('clientLogoSticker')).toBeInTheDocument();
		expect(screen.getByTestId('primaryColorInput')).toBeInTheDocument();
		expect(screen.getByTestId('secondaryColorInput')).toBeInTheDocument();
		expect(screen.getByTestId('stepLocator')).toBeInTheDocument();
		expect(screen.getByTestId('stepTitle')).toBeInTheDocument();
	});
});

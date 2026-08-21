/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ApiHelper from '../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper';
import {ObjectLayout} from '../../../../src/main/resources/META-INF/resources/js/common/types/ObjectDefinition';
import {Structure} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import buildObjectLayout from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectLayout';
import persistObjectLayout from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/persistObjectLayout';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper',
	() => ({
		__esModule: true,
		default: {
			delete: jest.fn(() => Promise.resolve({data: null, error: null})),
			get: jest.fn(() =>
				Promise.resolve({data: {items: []}, error: null})
			),
			post: jest.fn(() => Promise.resolve({data: {id: 1}, error: null})),
		},
	})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/buildObjectLayout'
);

const OBJECT_LAYOUTS_URL =
	'/o/object-admin/v1.0/object-definitions/by-external-reference-code/erc/object-layouts';

const LAYOUT = {
	defaultObjectLayout: true,
	name: {en_US: 'Product'},
	objectDefinitionExternalReferenceCode: 'erc',
	objectLayoutTabs: [],
} as ObjectLayout;

const STRUCTURE = {erc: 'erc', type: 'L_CMS_CONTENT_STRUCTURES'} as Structure;

describe('persistObjectLayout', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('reads the existing layouts of the object definition', async () => {
		(buildObjectLayout as jest.Mock).mockReturnValue(LAYOUT);

		await persistObjectLayout({erc: 'erc', structure: STRUCTURE});

		expect(ApiHelper.get).toHaveBeenCalledWith(
			`${OBJECT_LAYOUTS_URL}?pageSize=100`
		);
	});

	it('deletes the existing layout and posts the serialized layout', async () => {
		(buildObjectLayout as jest.Mock).mockReturnValue(LAYOUT);
		(ApiHelper.get as jest.Mock).mockResolvedValueOnce({
			data: {items: [{id: 42}]},
			error: null,
		});

		const {error} = await persistObjectLayout({
			erc: 'erc',
			structure: STRUCTURE,
		});

		expect(ApiHelper.delete).toHaveBeenCalledWith(
			'/o/object-admin/v1.0/object-layouts/42'
		);
		expect(ApiHelper.post).toHaveBeenCalledWith(OBJECT_LAYOUTS_URL, LAYOUT);
		expect(error).toBe(false);
	});

	it('deletes the existing layout without posting when the grouping is removed', async () => {
		(buildObjectLayout as jest.Mock).mockReturnValue(null);
		(ApiHelper.get as jest.Mock).mockResolvedValueOnce({
			data: {items: [{id: 42}]},
			error: null,
		});

		const {error} = await persistObjectLayout({
			erc: 'erc',
			structure: STRUCTURE,
		});

		expect(ApiHelper.delete).toHaveBeenCalledWith(
			'/o/object-admin/v1.0/object-layouts/42'
		);
		expect(ApiHelper.post).not.toHaveBeenCalled();
		expect(error).toBe(false);
	});

	it('reports an error when posting the layout fails', async () => {
		(buildObjectLayout as jest.Mock).mockReturnValue(LAYOUT);
		(ApiHelper.post as jest.Mock).mockResolvedValueOnce({
			data: null,
			error: 'unexpected',
		});

		const {error} = await persistObjectLayout({
			erc: 'erc',
			structure: STRUCTURE,
		});

		expect(error).toBe(true);
	});
});

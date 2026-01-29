/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {cleanup, render, screen, waitFor} from '@testing-library/react';

// @ts-ignore

import fetchMock from 'fetch-mock';
import React from 'react';

import DigitalSalesRoomService from '../../../src/main/resources/META-INF/resources/js/commons/DigitalSalesRoomService';
import DSRCommentsPanel from '../../../src/main/resources/META-INF/resources/js/components/DSRCommentsPanel';
import {setFieldValue} from '../utils';

const renderComponent = (roomId: number) => {
	return render(<DSRCommentsPanel roomId={roomId}></DSRCommentsPanel>);
};

describe('DSRCommentsPanel', () => {
	beforeAll(() => {
		global.Liferay = {
			...global.Liferay,
			ThemeDisplay: {
				...global.Liferay?.ThemeDisplay,
				getUserId: () => '102',
			},
		};
	});

	afterEach(() => {
		fetchMock.restore();
		jest.clearAllMocks();

		cleanup();
	});

	it('loads comments', async () => {
		const spyOnGetComments = jest.spyOn(
			DigitalSalesRoomService,
			'getComments'
		);

		fetchMock.get(
			/headless-digital-sales-room\/.*\/digital-sales-rooms\/.*\/comments.*/i,
			() => {
				return {
					items: [
						{
							creator: {
								id: 102,
								name: 'Author 1',
							},
							dateCreated: new Date(),
							id: 100,
							text: 'Comment 1',
						},
						{
							creator: {
								id: 100,
								name: 'Author 2',
							},
							dateCreated: new Date(),
							id: 101,
							text: 'Comment 2',
						},
					],
				};
			}
		);

		renderComponent(100);

		expect(spyOnGetComments).toBeCalledTimes(1);
		expect(spyOnGetComments).toBeCalledWith(100, 1);

		await waitFor(() => {
			expect(screen.getByText('Author 1')).toBeInTheDocument();
			expect(screen.getByText('Author 2')).toBeInTheDocument();
			expect(screen.getByText('Comment 1')).toBeInTheDocument();
			expect(screen.getByText('Comment 2')).toBeInTheDocument();
		});
	});

	it('Load more comments', async () => {
		const spyOnGetComments = jest.spyOn(
			DigitalSalesRoomService,
			'getComments'
		);

		fetchMock.get(
			/headless-digital-sales-room\/.*\/digital-sales-rooms\/.*\/comments.*/i,
			() => {
				return {
					items: [],
					lastPage: 2,
				};
			}
		);

		renderComponent(100);

		expect(spyOnGetComments).toBeCalledTimes(1);
		expect(spyOnGetComments).toBeCalledWith(100, 1);

		await waitFor(() => {
			expect(screen.getByTestId('loadMoreButton')).toBeInTheDocument();
		});

		await waitFor(() => {
			screen.getByTestId('loadMoreButton').click();
		});

		expect(spyOnGetComments).toBeCalledWith(100, 2);
		expect(screen.queryByTestId('loadMoreButton')).not.toBeInTheDocument();
	});

	it('Can post a comment', async () => {
		const spyOnPostComment = jest.spyOn(
			DigitalSalesRoomService,
			'postDigitalSalesRoomComment'
		);

		renderComponent(100);
		expect(screen.getByTestId('commentTextarea')).toBeInTheDocument();

		expect(screen.getByRole('button', {name: 'save'})).toBeDisabled();

		await setFieldValue(
			screen.getByTestId('commentTextarea'),
			'testComment'
		);

		await waitFor(() => {
			screen.getByRole('button', {name: 'save'}).click();
		});

		expect(spyOnPostComment).toBeCalledWith(100, 'testComment');
	});

	it('Can cancel adding comment', async () => {
		const spyOnPostComment = jest.spyOn(
			DigitalSalesRoomService,
			'postDigitalSalesRoomComment'
		);

		renderComponent(100);
		expect(screen.getByTestId('commentTextarea')).toBeInTheDocument();

		expect(screen.getByRole('button', {name: 'save'})).toBeDisabled();

		await setFieldValue(
			screen.getByTestId('commentTextarea'),
			'testComment'
		);

		await waitFor(() => {
			screen.getByRole('button', {name: 'cancel'}).click();
		});

		expect(spyOnPostComment).not.toHaveBeenCalled();
		await waitFor(() => {
			expect(screen.getByTestId('commentTextarea')).toHaveValue('');
		});
	});

	it('Can delete comment', async () => {
		const spyOnGetComments = jest.spyOn(
			DigitalSalesRoomService,
			'getComments'
		);
		const spyOnDeleteComment = jest.spyOn(
			DigitalSalesRoomService,
			'deleteDigitalSalesRoomComment'
		);

		fetchMock.get(
			/headless-digital-sales-room\/.*\/digital-sales-rooms\/.*\/comments.*/i,
			() => {
				return {
					items: [
						{
							creator: {
								id: 102,
								name: 'Author 1',
							},
							dateCreated: new Date(),
							id: 101,
							text: 'Comment 1',
						},
					],
					lastPage: 1,
				};
			}
		);

		renderComponent(100);

		expect(spyOnGetComments).toBeCalledTimes(1);
		expect(spyOnGetComments).toBeCalledWith(100, 1);

		await waitFor(() => {
			expect(screen.getByTestId('comment-actions')).toBeInTheDocument();
		});

		await waitFor(() => {
			screen.getByTestId('comment-actions').click();
		});

		await waitFor(() => {
			expect(
				screen.getByRole('menuitem', {name: /delete/i})
			).toBeInTheDocument();
		});

		await waitFor(() => {
			screen.getByRole('menuitem', {name: /delete/i}).click();
		});

		expect(spyOnDeleteComment).toBeCalledWith(100, 101);
		expect(spyOnGetComments).toBeCalledTimes(1);
	});

	it('can edit comment', async () => {
		const spyOnGetComments = jest.spyOn(
			DigitalSalesRoomService,
			'getComments'
		);
		const spyOnPatchComment = jest.spyOn(
			DigitalSalesRoomService,
			'patchDigitalSalesRoomComment'
		);

		fetchMock.get(
			/headless-digital-sales-room\/.*\/digital-sales-rooms\/.*\/comments.*/i,
			() => {
				return {
					items: [
						{
							creator: {
								id: 102,
								name: 'Author 1',
							},
							dateCreated: new Date(),
							id: 101,
							text: 'Comment 1',
						},
					],
					lastPage: 1,
				};
			}
		);
		fetchMock.patch(
			/headless-digital-sales-room\/.*\/digital-sales-rooms\/.*\/comments.*/i,
			(_: any, options: any) => {
				const body = JSON.parse(options?.body as string);

				if (body.text === 'edited') {
					return {
						creator: {
							id: 102,
							name: 'Author 1',
						},
						dateCreated: new Date().toISOString(),
						id: 101,
						text: 'edited',
					};
				}

				return {
					body: {error: 'Unexpected payload'},
					status: 400,
				};
			}
		);

		renderComponent(100);

		expect(spyOnGetComments).toBeCalledTimes(1);
		expect(spyOnGetComments).toBeCalledWith(100, 1);

		await waitFor(() => {
			expect(screen.getByTestId('comment-actions')).toBeInTheDocument();
		});

		await waitFor(() => {
			screen.getByTestId('comment-actions').click();
		});

		await waitFor(() => {
			screen.getByRole('menuitem', {name: /edit/i}).click();
		});

		await setFieldValue(screen.getByTestId('commentTextarea'), 'edited');

		await waitFor(() => {
			screen.getByRole('button', {name: 'save'}).click();
		});

		expect(spyOnPatchComment).toBeCalledWith(101, 100, 'edited');
		await waitFor(() => {
			expect(screen.getByTestId('commentTextarea')).toHaveValue('');
			expect(screen.getByText('edited')).toBeInTheDocument();
		});
	});
});

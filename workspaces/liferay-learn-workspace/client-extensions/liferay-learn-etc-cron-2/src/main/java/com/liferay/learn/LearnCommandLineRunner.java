/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import com.liferay.client.extension.util.spring.boot3.client.LiferayOAuth2AccessTokenManager;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Nilton Vieira
 */
@Component
public class LearnCommandLineRunner
	extends BaseRestController implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		_postP2S3ExamResultsSynchronization("certmanager");

		_postP2S3ExamResultsSynchronization("webassessor");
	}

	private void _postP2S3ExamResultsSynchronization(String source) {
		post(
			_liferayOAuth2AccessTokenManager.getAuthorization(
					_liferayOAuthApplicationExternalReferenceCodes),
			new JSONObject(
			).put(
					"source", source
			).put(
					"synchronizationStatus", "InProgress"
			).toString(),
			UriComponentsBuilder.fromPath(
					"/o/c/p2s3examresultssynchronizations"
			).build(
			).toUri());
	}

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

	@Value("${liferay.oauth.application.external.reference.codes}")
	private String _liferayOAuthApplicationExternalReferenceCodes;

}
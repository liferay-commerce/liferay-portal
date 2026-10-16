/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Danny Situ
 */
@ExtendedObjectClassDefinition(
	category = "digital-signature", featureFlagKey = "LPD-69290",
	scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	id = "com.liferay.digital.signature.configuration.DigitalSignatureSystemConfiguration",
	localization = "content/Language",
	name = "digital-signature-system-configuration-name"
)
@ProviderType
public interface DigitalSignatureSystemConfiguration {

	@Meta.AD(
		deflt = "https://account.docusign.com", name = "production-account-url",
		required = false
	)
	public String productionAccountURL();

	@Meta.AD(
		deflt = "https://apps.docusign.com", name = "production-app-origin-url",
		required = false
	)
	public String productionAppOriginURL();

	@Meta.AD(
		deflt = "https://js.docusign.com/bundle.js", name = "production-js-url",
		required = false
	)
	public String productionJSURL();

	@Meta.AD(
		deflt = "https://account-d.docusign.com", name = "sandbox-account-url",
		required = false
	)
	public String sandboxAccountURL();

	@Meta.AD(
		deflt = "https://apps-d.docusign.com", name = "sandbox-app-origin-url",
		required = false
	)
	public String sandboxAppOriginURL();

	@Meta.AD(
		deflt = "https://js-d.docusign.com/bundle.js", name = "sandbox-js-url",
		required = false
	)
	public String sandboxJSURL();

	@Meta.AD(
		deflt = "3",
		description = "signature-reminder-check-interval-description",
		min = "1", name = "signature-reminder-check-interval", required = false
	)
	public int signatureReminderCheckInterval();

}
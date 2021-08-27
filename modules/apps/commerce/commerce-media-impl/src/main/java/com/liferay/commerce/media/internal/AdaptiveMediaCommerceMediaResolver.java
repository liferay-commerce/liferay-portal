/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.media.internal;

import com.liferay.adaptive.media.AdaptiveMedia;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.AMImageConfigurationHelper;
import com.liferay.adaptive.media.image.finder.AMImageFinder;
import com.liferay.adaptive.media.image.finder.AMImageQueryBuilder;
import com.liferay.adaptive.media.image.processor.AMImageAttribute;
import com.liferay.adaptive.media.image.processor.AMImageProcessor;
import com.liferay.adaptive.media.image.url.AMImageURLFactory;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.commerce.media.CommerceMediaResolver;
import com.liferay.commerce.media.constants.CommerceMediaConstants;
import com.liferay.commerce.media.internal.configuration.CommerceMediaDefaultImageConfiguration;
import com.liferay.commerce.product.constants.CPAttachmentFileEntryConstants;
import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;
import com.liferay.portlet.asset.service.permission.AssetCategoryPermission;

import java.net.URI;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alec Sloan
 */
@Component(enabled = false, service = CommerceMediaResolver.class)
public class AdaptiveMediaCommerceMediaResolver
	implements CommerceMediaResolver {

	@Override
	public String getDefaultURL(long groupId) throws PortalException {
		CommerceMediaDefaultImageConfiguration
			commerceMediaDefaultImageConfiguration =
				ConfigurationProviderUtil.getConfiguration(
					CommerceMediaDefaultImageConfiguration.class,
					new GroupServiceSettingsLocator(
						groupId, CommerceMediaConstants.SERVICE_NAME));

		FileEntry fileEntry = null;

		try {
			fileEntry = _dlAppService.getFileEntry(
				commerceMediaDefaultImageConfiguration.defaultFileEntryId());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			Group group = _groupLocalService.fetchGroup(groupId);

			if (group == null) {
				return StringPool.BLANK;
			}

			Company company = _companyLocalService.fetchCompany(
				group.getCompanyId());

			fileEntry =
				_dlAppLocalService.fetchFileEntryByExternalReferenceCode(
					company.getGroupId(), PropsKeys.IMAGE_DEFAULT_COMPANY_LOGO);
		}

		return _getFileEntryAdaptiveMediaURL(fileEntry.getFileEntryId());
	}

	@Override
	public String getDownloadURL(
			long commerceAccountId, long cpAttachmentFileEntryId)
		throws PortalException {

		return getURL(commerceAccountId, cpAttachmentFileEntryId, true, false);
	}

	@Override
	public String getThumbnailURL(
			long commerceAccountId, long cpAttachmentFileEntryId)
		throws PortalException {

		return getURL(commerceAccountId, cpAttachmentFileEntryId, false, true);
	}

	@Override
	public String getURL(long commerceAccountId, long cpAttachmentFileEntryId)
		throws PortalException {

		return getURL(commerceAccountId, cpAttachmentFileEntryId, false, false);
	}

	@Override
	public String getURL(
			long commerceAccountId, long cpAttachmentFileEntryId,
			boolean download, boolean thumbnail)
		throws PortalException {

		return getURL(
			commerceAccountId, cpAttachmentFileEntryId, download, thumbnail,
			true);
	}

	@Override
	public String getURL(
			long commerceAccountId, long cpAttachmentFileEntryId,
			boolean download, boolean thumbnail, boolean secure)
		throws PortalException {

		Company company = _companyLocalService.getCompany(
			CompanyThreadLocal.getCompanyId());

		CPAttachmentFileEntry cpAttachmentFileEntry =
			_cpAttachmentFileEntryLocalService.fetchCPAttachmentFileEntry(
				cpAttachmentFileEntryId);

		if (cpAttachmentFileEntry == null) {
			return getDefaultURL(company.getGroupId());
		}

		if (secure) {
			String className = cpAttachmentFileEntry.getClassName();

			if (className.equals(AssetCategory.class.getName())) {
				AssetCategory assetCategory =
					_assetCategoryLocalService.fetchCategory(
						cpAttachmentFileEntry.getClassPK());

				AssetCategoryPermission.check(
					PermissionThreadLocal.getPermissionChecker(), assetCategory,
					ActionKeys.VIEW);
			}
			else if (className.equals(CPDefinition.class.getName())) {
				_commerceProductViewPermission.check(
					PermissionThreadLocal.getPermissionChecker(),
					commerceAccountId, cpAttachmentFileEntry.getClassPK());
			}
		}

		if (cpAttachmentFileEntry.isCDNEnabled()) {
			return cpAttachmentFileEntry.getCDNURL();
		}

		if (cpAttachmentFileEntry.getType() ==
				CPAttachmentFileEntryConstants.TYPE_IMAGE) {

			return _getFileEntryAdaptiveMediaURL(
				cpAttachmentFileEntry.getFileEntryId());
		}
		else if (cpAttachmentFileEntry.getType() ==
					CPAttachmentFileEntryConstants.TYPE_OTHER) {

			StringBundler sb = new StringBundler(9);

			sb.append(_portal.getPathModule());
			sb.append(StringPool.SLASH);
			sb.append(CommerceMediaConstants.SERVLET_PATH);
			sb.append("/accounts/");
			sb.append(commerceAccountId);
			sb.append("/attachments/");
			sb.append(cpAttachmentFileEntry.getCPAttachmentFileEntryId());
			sb.append("?download=");
			sb.append(download);
		}

		return getDefaultURL(company.getGroupId());
	}

	private String _getDefaultCompanyLogoURL(long companyId)
		throws PortalException {

		Company company = _companyLocalService.getCompany(companyId);

		return StringBundler.concat(
			_portal.getPathImage(), "/company_logo?img_id=",
			company.getLogoId(), "&t=",
			WebServerServletTokenUtil.getToken(company.getLogoId()));
	}

	private String _getFileEntryAdaptiveMediaURL(long fileEntryId)
		throws PortalException {

		FileEntry fileEntry = _dlAppService.getFileEntry(fileEntryId);

		FileVersion fileVersion = fileEntry.getFileVersion();

		Stream<AdaptiveMedia<AMImageProcessor>> adaptiveMediaStream =
			_amImageFinder.getAdaptiveMediaStream(
				amImageQueryBuilder -> amImageQueryBuilder.forFileVersion(
					fileVersion
				).orderBy(
					AMImageAttribute.AM_IMAGE_ATTRIBUTE_WIDTH,
					AMImageQueryBuilder.SortOrder.DESC
				).done());

		List<AdaptiveMedia<AMImageProcessor>> adaptiveMedias =
			adaptiveMediaStream.collect(Collectors.toList());

		if (!adaptiveMedias.isEmpty()) {
			AdaptiveMedia<AMImageProcessor> adaptiveMedia = adaptiveMedias.get(
				0);

			URI uri = adaptiveMedia.getURI();

			return uri.toString();
		}

		Collection<AMImageConfigurationEntry> amImageConfigurationEntries =
			_amImageConfigurationHelper.getAMImageConfigurationEntries(
				fileEntry.getCompanyId());

		if (amImageConfigurationEntries.isEmpty()) {
			return _getDefaultCompanyLogoURL(fileEntry.getCompanyId());
		}

		Stream<AMImageConfigurationEntry> amImageConfigurationEntryStream =
			amImageConfigurationEntries.stream();

		Optional<AMImageConfigurationEntry> amImageConfigurationEntryOptional =
			amImageConfigurationEntryStream.findFirst();

		if (!amImageConfigurationEntryOptional.isPresent()) {
			return _getDefaultCompanyLogoURL(fileEntry.getCompanyId());
		}

		URI uri = _amImageURLFactory.createFileEntryURL(
			fileEntry.getFileVersion(),
			amImageConfigurationEntryOptional.get());

		return uri.toString();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AdaptiveMediaCommerceMediaResolver.class);

	@Reference
	private AMImageConfigurationHelper _amImageConfigurationHelper;

	@Reference
	private AMImageFinder _amImageFinder;

	@Reference
	private AMImageURLFactory _amImageURLFactory;

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private CommerceProductViewPermission _commerceProductViewPermission;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

}
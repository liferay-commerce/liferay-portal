/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.testray;

import com.liferay.jenkins.results.parser.BuildDatabase;
import com.liferay.jenkins.results.parser.Environment;
import com.liferay.jenkins.results.parser.JenkinsMaster;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.Job;
import com.liferay.jenkins.results.parser.PluginsWorkspaceGitRepository;
import com.liferay.jenkins.results.parser.PortalFixpackRelease;
import com.liferay.jenkins.results.parser.PortalHotfixRelease;
import com.liferay.jenkins.results.parser.PortalRelease;
import com.liferay.jenkins.results.parser.PortalWorkspace;
import com.liferay.jenkins.results.parser.PortalWorkspaceGitRepository;
import com.liferay.jenkins.results.parser.PullRequest;
import com.liferay.jenkins.results.parser.Workspace;

import java.io.File;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Hashimoto
 */
public abstract class BaseTestrayTextReplacer implements TestrayTextReplacer {

	@Override
	public String replace(String string) {
		string = _replace(string);

		if (!JenkinsResultsParserUtil.isNullOrEmpty(string) &&
			(string.length() > 150)) {

			string = string.substring(string.length() - 150);
		}

		return string;
	}

	@Override
	public String replaceSlack(String string, TestrayBuild testrayBuild) {
		string = _replace(string);

		string = _replaceSlackTestrayInformation(string, testrayBuild);
		string = _replaceSlackTestrayImporter(string);

		return string;
	}

	protected BaseTestrayTextReplacer(BuildDatabase buildDatabase) {
		_portalFixpackReleases = buildDatabase.getPortalFixpackReleases();
		_portalHotfixReleases = buildDatabase.getPortalHotfixReleases();
		_portalReleases = buildDatabase.getPortalReleases();
		_pullRequests = buildDatabase.getPullRequests();
		_workspaces = buildDatabase.getWorkspaces();
	}

	protected abstract int getBuildNumber();

	protected abstract Map<String, String> getBuildParameters();

	protected abstract int getControllerBuildNumber();

	protected abstract JenkinsMaster getControllerJenkinsMaster();

	protected abstract String getControllerJobName();

	protected abstract Date getControllerStartDate();

	protected abstract JenkinsMaster getJenkinsMaster();

	protected abstract String getJobName();

	protected abstract Date getStartDate();

	protected abstract String getTestSuiteName();

	protected abstract boolean hasControllerBuild();

	private String _fixSlackString(String string) {
		string = string.replace("*", "&#42;");
		string = string.replace(">", "&gt;");
		string = string.replace("<", "&lt;");

		return string.replace("|", "&vert;");
	}

	private String _getBuildURL(
		int buildNumber, JenkinsMaster jenkinsMaster, String jobName) {

		return JenkinsResultsParserUtil.combine(
			"https://", jenkinsMaster.getName(), ".liferay.com/job/", jobName,
			"/", String.valueOf(buildNumber));
	}

	private String _getJenkinsReportURL() {
		JenkinsMaster jenkinsMaster = getJenkinsMaster();

		return JenkinsResultsParserUtil.combine(
			"https://", jenkinsMaster.getName(), ".liferay.com/",
			"userContent/jobs/", getJobName(), "/builds/",
			String.valueOf(getBuildNumber()), "/jenkins-report.html");
	}

	private String _getMajorPortalVersion() {
		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return "7.4";
		}

		File releasePropertiesFile = new File(
			portalWorkspaceGitRepository.getDirectory(), "release.properties");

		Properties releaseProperties = JenkinsResultsParserUtil.getProperties(
			releasePropertiesFile);

		String majorPortalVersion = JenkinsResultsParserUtil.getProperty(
			releaseProperties, "lp.version.major");

		if (JenkinsResultsParserUtil.isNullOrEmpty(majorPortalVersion)) {
			return "7.4";
		}

		return majorPortalVersion;
	}

	private PluginsWorkspaceGitRepository _getPluginsWorkspaceGitRepository() {
		for (Workspace workspace : _workspaces) {
			if (!(workspace instanceof PortalWorkspace)) {
				continue;
			}

			PortalWorkspace portalWorkspace = (PortalWorkspace)workspace;

			return portalWorkspace.getPluginsWorkspaceGitRepository();
		}

		return null;
	}

	private Job.BuildProfile _getPortalBuildProfile() {
		Map<String, String> buildParameters = getBuildParameters();

		Job.BuildProfile buildProfile = Job.BuildProfile.getByString(
			buildParameters.get("TEST_PORTAL_BUILD_PROFILE"));

		if (buildProfile != null) {
			return buildProfile;
		}

		return Job.BuildProfile.DXP;
	}

	private PortalFixpackRelease _getPortalFixpackRelease() {
		if (_portalFixpackReleases.isEmpty()) {
			return null;
		}

		return _portalFixpackReleases.get(0);
	}

	private PortalHotfixRelease _getPortalHotfixRelease() {
		if (_portalHotfixReleases.isEmpty()) {
			return null;
		}

		return _portalHotfixReleases.get(0);
	}

	private PortalRelease _getPortalRelease() {
		if (_portalReleases.isEmpty()) {
			return null;
		}

		return _portalReleases.get(0);
	}

	private PortalWorkspaceGitRepository _getPortalWorkspaceGitRepository() {
		for (Workspace workspace : _workspaces) {
			if (!(workspace instanceof PortalWorkspace)) {
				continue;
			}

			PortalWorkspace portalWorkspace = (PortalWorkspace)workspace;

			return portalWorkspace.getPortalWorkspaceGitRepository();
		}

		return null;
	}

	private PullRequest _getPullRequest() {
		if (_pullRequests.isEmpty()) {
			return null;
		}

		if (_pullRequests.size() == 1) {
			return _pullRequests.get(0);
		}

		Map<String, String> buildParameters = getBuildParameters();

		String githubReceiverUsername = buildParameters.get(
			"GITHUB_RECEIVER_USERNAME");

		String pullRequestNumber = buildParameters.get(
			"GITHUB_PULL_REQUEST_NUMBER");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(githubReceiverUsername) &&
			!JenkinsResultsParserUtil.isNullOrEmpty(pullRequestNumber)) {

			for (PullRequest pullRequest : _pullRequests) {
				if (Objects.equals(
						pullRequest.getReceiverUsername(),
						githubReceiverUsername) &&
					Objects.equals(
						pullRequest.getNumber(), pullRequestNumber)) {

					return pullRequest;
				}
			}
		}

		return _pullRequests.get(0);
	}

	private String _replace(String string) {
		string = _replaceControllerBuild(string);
		string = _replacePluginsBranchInformationBuild(string);
		string = _replacePluginsTopLevelBuild(string);
		string = _replacePortalAppReleaseTopLevelBuild(string);
		string = _replacePortalBranchInformationBuild(string);
		string = _replacePortalRelease(string);
		string = _replacePullRequestBuild(string);
		string = _replaceQAWebsitesTopLevelBuild(string);
		string = _replaceTopLevelBuild(string);

		String jobName = getJobName();

		if (jobName.contains("subrepository")) {
			string = _replaceSubrepository(string);
		}

		return string;
	}

	private String _replaceControllerBuild(String string) {
		if (!hasControllerBuild()) {
			return string;
		}

		JenkinsMaster controllerJenkinsMaster = getControllerJenkinsMaster();
		String controllerJobName = getControllerJobName();
		int controllerBuildNumber = getControllerBuildNumber();

		string = string.replace(
			"$(jenkins.controller.build.url)",
			_getBuildURL(
				controllerBuildNumber, controllerJenkinsMaster,
				controllerJobName));
		string = string.replace(
			"$(jenkins.controller.build.number)",
			String.valueOf(controllerBuildNumber));
		string = string.replace(
			"$(jenkins.controller.build.start)",
			JenkinsResultsParserUtil.toDateString(
				getControllerStartDate(), "yyyy-MM-dd HH:mm:ss",
				"America/Los_Angeles"));
		string = string.replace(
			"$(jenkins.controller.job.name)", controllerJobName);

		return string.replace(
			"$(jenkins.controller.master.hostname)",
			controllerJenkinsMaster.getName());
	}

	private String _replacePluginsBranchInformationBuild(String string) {
		PluginsWorkspaceGitRepository pluginsWorkspaceGitRepository =
			_getPluginsWorkspaceGitRepository();

		if (pluginsWorkspaceGitRepository == null) {
			return string;
		}

		string = string.replace(
			"$(plugins.branch.name)",
			pluginsWorkspaceGitRepository.getUpstreamBranchName());
		string = string.replace(
			"$(plugins.custom.branch.name)",
			pluginsWorkspaceGitRepository.getSenderBranchName());
		string = string.replace(
			"$(plugins.custom.branch.username)",
			pluginsWorkspaceGitRepository.getSenderBranchUsername());
		string = string.replace(
			"$(plugins.repository)", pluginsWorkspaceGitRepository.getName());

		return string.replace(
			"$(plugins.sha)",
			pluginsWorkspaceGitRepository.getSenderBranchSHA());
	}

	private String _replacePluginsTopLevelBuild(String string) {
		Map<String, String> buildParameters = getBuildParameters();

		String pluginName = buildParameters.get("TEST_PLUGIN_NAME");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(pluginName)) {
			string = string.replace("$(plugin.name)", pluginName);
		}

		return string;
	}

	private String _replacePortalAppReleaseTopLevelBuild(String string) {
		Map<String, String> buildParameters = getBuildParameters();

		String portalAppName = buildParameters.get("TEST_PORTAL_APP_NAME");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(portalAppName)) {
			string = string.replace("$(portal.app.name)", portalAppName);
		}

		return string;
	}

	private String _replacePortalBranchInformationBuild(String string) {
		Job.BuildProfile buildProfile = _getPortalBuildProfile();

		string = string.replace(
			"$(portal.profile)", buildProfile.toDisplayString());

		if (buildProfile == Job.BuildProfile.PORTAL) {
			string = string.replace("$(portal.type)", "CE");
		}
		else {
			string = string.replace("$(portal.type)", "EE");
		}

		String majorPortalVersion = _getMajorPortalVersion();

		string = string.replace("$(portal.version)", majorPortalVersion);

		string = string.replace(
			"$(portal.product.version)", majorPortalVersion + ".x");

		PortalWorkspaceGitRepository portalWorkspaceGitRepository =
			_getPortalWorkspaceGitRepository();

		if (portalWorkspaceGitRepository == null) {
			return string;
		}

		String portalUpstreamBranchName =
			portalWorkspaceGitRepository.getUpstreamBranchName();

		string = string.replace(
			"$(portal.branch.name)", portalUpstreamBranchName);

		Matcher releaseBranchMatcher = _releaseBranchPattern.matcher(
			portalUpstreamBranchName);

		if (releaseBranchMatcher.find()) {
			string = string.replace(
				"$(portal.branch.display.name)",
				JenkinsResultsParserUtil.combine(
					releaseBranchMatcher.group("year"), " Q",
					releaseBranchMatcher.group("quarter")));
		}
		else {
			string = string.replace(
				"$(portal.branch.display.name)", majorPortalVersion);
		}

		string = string.replace(
			"$(portal.repository)", portalWorkspaceGitRepository.getName());

		return string.replace(
			"$(portal.sha)", portalWorkspaceGitRepository.getSenderBranchSHA());
	}

	private String _replacePortalRelease(String string) {
		PortalRelease portalRelease = _getPortalRelease();

		if (portalRelease != null) {
			String portalBundleTomcatURLString = String.valueOf(
				portalRelease.getPortalBundleTomcatURL());

			string = string.replace(
				"$(portal.product.version)", portalRelease.getPortalVersion());
			string = string.replace(
				"$(portal.release.tomcat.url)", portalBundleTomcatURLString);
			string = string.replace(
				"$(portal.release.version)", portalRelease.getPortalVersion());

			Matcher matcher = _releaseArtifactURLPattern.matcher(
				portalBundleTomcatURLString);

			if (matcher.find()) {
				string = string.replace(
					"$(portal.release.tomcat.name)",
					matcher.group("releaseName"));
			}

			Map<String, String> buildParameters = getBuildParameters();

			String portalReleaseBuildVersion = buildParameters.get(
				"TEST_PORTAL_RELEASE_VERSION");

			if (!JenkinsResultsParserUtil.isNullOrEmpty(
					portalReleaseBuildVersion)) {

				string = string.replace(
					"$(portal.release.build.version)",
					portalReleaseBuildVersion);
			}
		}

		PortalFixpackRelease portalFixpackRelease = _getPortalFixpackRelease();

		if (portalFixpackRelease != null) {
			String portalFixpackURL = String.valueOf(
				portalFixpackRelease.getPortalFixpackURL());

			string = string.replace(
				"$(portal.fixpack.release.url)", portalFixpackURL);

			string = string.replace(
				"$(portal.fixpack.release.version)",
				portalFixpackRelease.getPortalFixpackVersion());

			Matcher matcher = _releaseArtifactURLPattern.matcher(
				portalFixpackURL);

			if (matcher.find()) {
				string = string.replace(
					"$(portal.fixpack.release.name)",
					matcher.group("releaseName"));
			}
		}

		PortalHotfixRelease portalHotfixRelease = _getPortalHotfixRelease();

		if (portalHotfixRelease != null) {
			String portalHotfixURL = String.valueOf(
				portalHotfixRelease.getPortalHotfixReleaseURL());

			string = string.replace(
				"$(portal.hotfix.release.url)", portalHotfixURL);

			string = string.replace(
				"$(portal.hotfix.release.version)",
				portalHotfixRelease.getPortalHotfixReleaseVersion());

			if (portalRelease != null) {
				string = string.replace(
					"$(portal.product.version)",
					portalRelease.getPortalVersion());
			}

			Matcher matcher = _releaseArtifactURLPattern.matcher(
				portalHotfixURL);

			if (matcher.find()) {
				string = string.replace(
					"$(portal.hotfix.release.name)",
					matcher.group("releaseName"));
			}
		}

		StringBuilder sb = new StringBuilder();

		if (portalRelease == null) {
			sb.append(_getMajorPortalVersion());
			sb.append(".x");

			string = string.replace("$(portal.product.version)", sb.toString());
		}
		else {
			sb.append(portalRelease.getPortalVersion());

			string = string.replace(
				"$(portal.product.version)", portalRelease.getPortalVersion());

			if (portalFixpackRelease != null) {
				sb.append(" FP");
				sb.append(portalFixpackRelease.getPortalFixpackVersion());
			}

			if (portalHotfixRelease != null) {
				sb.append(" HF");
				sb.append(portalHotfixRelease.getPortalHotfixReleaseVersion());
			}
		}

		return string.replace("$(portal.release.name)", sb.toString());
	}

	private String _replacePullRequestBuild(String string) {
		PullRequest pullRequest = _getPullRequest();

		if (pullRequest == null) {
			return string;
		}

		string = string.replace(
			"$(pull.request.number)", pullRequest.getNumber());
		string = string.replace(
			"$(pull.request.url)", pullRequest.getHtmlURL());
		string = string.replace(
			"$(pull.request.receiver.username)",
			pullRequest.getReceiverUsername());

		return string.replace(
			"$(pull.request.sender.username)", pullRequest.getSenderUsername());
	}

	private String _replaceQAWebsitesTopLevelBuild(String string) {
		Map<String, String> buildParameters = getBuildParameters();

		String projectNames = buildParameters.get("PROJECT_NAMES");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(projectNames)) {
			string = string.replace(
				"$(qa.websites.project.name)", projectNames);
		}

		return string;
	}

	private String _replaceSlackTestrayImporter(String string) {
		String buildNumber = Environment.get("BUILD_NUMBER");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(buildNumber)) {
			string = string.replace(
				"$(testray.importer.build.number)", buildNumber);
		}

		String buildURL = Environment.get("BUILD_URL");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(buildURL)) {
			string = string.replace("$(testray.importer.build.url)", buildURL);
		}

		String jobName = Environment.get("JOB_NAME");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(jobName)) {
			string = string.replace("$(testray.importer.job.name)", jobName);
		}

		return string;
	}

	private String _replaceSlackTestrayInformation(
		String string, TestrayBuild testrayBuild) {

		if (testrayBuild == null) {
			return string;
		}

		TestrayServer testrayServer = testrayBuild.getTestrayServer();

		if (testrayServer != null) {
			string = string.replace(
				"$(testray.server.url)",
				String.valueOf(testrayServer.getURL()));
		}

		TestrayProject testrayProject = testrayBuild.getTestrayProject();

		if (testrayProject != null) {
			string = string.replace(
				"$(testray.project.name)",
				_fixSlackString(testrayProject.getName()));

			string = string.replace(
				"$(testray.project.url)",
				String.valueOf(testrayProject.getURL()));
		}

		TestrayProductVersion testrayProductVersion =
			testrayBuild.getTestrayProductVersion();

		if (testrayProductVersion != null) {
			string = string.replace(
				"$(testray.product.version.name)",
				_fixSlackString(testrayProductVersion.getName()));
		}

		TestrayRoutine testrayRoutine = testrayBuild.getTestrayRoutine();

		if (testrayRoutine != null) {
			string = string.replace(
				"$(testray.routine.name)",
				_fixSlackString(testrayRoutine.getName()));
			string = string.replace(
				"$(testray.routine.url)",
				String.valueOf(testrayRoutine.getURL()));
		}

		string = string.replace(
			"$(testray.build.name)", _fixSlackString(testrayBuild.getName()));

		return string.replace(
			"$(testray.build.url)", String.valueOf(testrayBuild.getURL()));
	}

	private String _replaceSubrepository(String string) {
		Map<String, String> buildParameters = getBuildParameters();

		String githubUpstreamBranchName = buildParameters.get(
			"GITHUB_UPSTREAM_BRANCH_NAME");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(githubUpstreamBranchName)) {
			string = string.replace(
				"$(github.upstream.branch.name)", githubUpstreamBranchName);
		}

		String repositoryName = buildParameters.get("REPOSITORY_NAME");

		if (!JenkinsResultsParserUtil.isNullOrEmpty(repositoryName)) {
			string = string.replace("$(repository.name)", repositoryName);
		}

		return string;
	}

	private String _replaceTopLevelBuild(String string) {
		JenkinsMaster jenkinsMaster = getJenkinsMaster();
		String jobName = getJobName();
		int buildNumber = getBuildNumber();

		string = string.replace("$(ci.test.suite)", getTestSuiteName());
		string = string.replace(
			"$(jenkins.build.number)", String.valueOf(buildNumber));
		string = string.replace(
			"$(jenkins.build.start)",
			JenkinsResultsParserUtil.toDateString(
				getStartDate(), "yyyy-MM-dd[HH:mm:ss]", "America/Los_Angeles"));
		string = string.replace(
			"$(jenkins.build.url)",
			_getBuildURL(buildNumber, jenkinsMaster, jobName));
		string = string.replace("$(jenkins.job.name)", jobName);
		string = string.replace(
			"$(jenkins.master.hostname)", jenkinsMaster.getName());

		return string.replace("$(jenkins.report.url)", _getJenkinsReportURL());
	}

	private static final Pattern _releaseArtifactURLPattern = Pattern.compile(
		"https?://.+/(?<releaseName>[^/]+)(.7z|.tar.gz|.war|.zip)");
	private static final Pattern _releaseBranchPattern = Pattern.compile(
		"release-(?<year>\\d{4})\\.q(?<quarter>[1-4])");

	private final List<PortalFixpackRelease> _portalFixpackReleases;
	private final List<PortalHotfixRelease> _portalHotfixReleases;
	private final List<PortalRelease> _portalReleases;
	private final List<PullRequest> _pullRequests;
	private final List<Workspace> _workspaces;

}
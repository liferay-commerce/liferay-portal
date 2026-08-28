#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

_APPS_MARKDOWN_FILE_NAME="${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR:-}/docs/reference/latest/en/dxp/apps.md"

_ATTEMPT_FILE_NAME=/public_html/.learn-importer-attempt

_REMOTE_COMMIT=""

_SUCCESS_FILE_NAME=/public_html/.learn-importer-success

function check_environment {
	record_phase "preflight"

	local missing_names=()

	local name

	for name in \
		LIFERAY_LEARN_ETC_CRON_DXP_URL \
		LIFERAY_LEARN_ETC_CRON_GITHUB_DEPLOY_KEY \
		LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR \
		LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_ID \
		LIFERAY_LEARN_ETC_CRON_OAUTH_CLIENT_SECRET \
		LIFERAY_LEARN_RESOURCE_DOMAIN
	do
		if [ -z "${!name:-}" ]
		then
			missing_names+=("${name}")
		fi
	done

	if [[ "${#missing_names[@]}" -gt 0 ]]
	then
		echo "[cron-1] ERROR: missing required environment variables: ${missing_names[*]}"

		exit 1
	fi

	if ! touch /public_html/.learn-importer-write-test 2> /dev/null
	then
		echo "[cron-1] ERROR: /public_html is not writable"

		exit 1
	fi

	rm --force /public_html/.learn-importer-write-test

	local resource_url=${LIFERAY_LEARN_RESOURCE_DOMAIN}

	case "${resource_url}" in
		http://*|https://*)
			;;
		*)
			resource_url="https://${resource_url}"
			;;
	esac

	local resource_status

	resource_status=$( \
		curl \
			--max-time 30 \
			--output /dev/null \
			--silent \
			--write-out "%{http_code}" \
			"${resource_url}/" || true)

	if [ "${resource_status}" == "000" ]
	then
		echo "[cron-1] ERROR: the resource domain ${resource_url} is unreachable"

		exit 1
	fi

	echo "[cron-1] Environment verified, ${resource_url} answered ${resource_status}."
}

function check_for_changes {
	_REMOTE_COMMIT=$(git ls-remote git@github.com:"${LIFERAY_LEARN_ETC_CRON_GITHUB_USER:-liferay}"/liferay-learn.git refs/heads/"${LIFERAY_LEARN_ETC_CRON_GITHUB_BRANCH:-master}" | awk '{print $1}') || true

	if [ -z "${_REMOTE_COMMIT}" ]
	then
		_REMOTE_COMMIT="unknown"
	fi

	local attempt_commit attempt_force attempt_seconds

	attempt_commit=$(awk '{print $1}' "${_ATTEMPT_FILE_NAME}" 2> /dev/null) || true
	attempt_force=$(awk '{print $3}' "${_ATTEMPT_FILE_NAME}" 2> /dev/null) || true
	attempt_seconds=$(awk '{print $2}' "${_ATTEMPT_FILE_NAME}" 2> /dev/null) || true

	local force=${LIFERAY_LEARN_ETC_CRON_FORCE_RUN:-}

	if [ -n "${force}" ] && [ "${force}" != "false" ] &&
	   [ "${force}" != "${attempt_force}" ]
	then
		echo "[cron-1] A full run was forced by \"${force}\", change the value to force another one."

		return 0
	fi

	if [[ ! "${attempt_seconds}" =~ ^[0-9]+$ ]]
	then
		attempt_seconds=0
	fi

	if [ "${_REMOTE_COMMIT}" == "unknown" ] ||
	   [ "${_REMOTE_COMMIT}" != "${attempt_commit}" ]
	then
		echo "[cron-1] The remote branch is at ${_REMOTE_COMMIT} and the last attempt was ${attempt_commit:-none}, so there is work to do."

		return 0
	fi

	local attempt_hours

	attempt_hours=$(hours_since "${attempt_seconds}")

	if [[ "${attempt_hours}" -ge "${LIFERAY_LEARN_ETC_CRON_RETRY_HOURS:-6}" ]]
	then
		local success_seconds

		success_seconds=$(cat "${_SUCCESS_FILE_NAME}" 2> /dev/null) || true

		if [[ ! "${success_seconds}" =~ ^[0-9]+$ ]]
		then
			success_seconds=0
		fi

		local success_hours

		success_hours=$(hours_since "${success_seconds}")

		if [[ "${success_seconds}" -lt "${attempt_seconds}" ]]
		then
			echo "[cron-1] The last attempt never reported success, so it is being retried."

			return 0
		fi

		if [[ "${success_hours}" -ge "${LIFERAY_LEARN_ETC_CRON_RECONCILE_HOURS:-20}" ]]
		then
			echo "[cron-1] The last successful run was ${success_hours} hours ago, so the site is being reconciled."

			return 0
		fi
	fi

	echo "[cron-1] There is no new commit since ${attempt_commit} and the last attempt was ${attempt_hours} hours ago, so there is nothing to do."

	touch /tmp/liferay_jar_runner_skipped

	exit 0
}

function check_generated_site {
	local docs_dir="${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/docs"
	local site_dir="${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site"

	local examples=()
	local expected_count=0
	local missing_count=0
	local stale_count=0

	local html_file markdown_file

	while IFS= read -r markdown_file
	do
		expected_count=$((expected_count + 1))

		html_file="${site_dir}/${markdown_file%.md}.html"

		if [ ! -f "${html_file}" ]
		then
			missing_count=$((missing_count + 1))

			if [[ "${#examples[@]}" -lt 5 ]]
			then
				examples+=("never generated: ${html_file}")
			fi
		elif [ ! "${html_file}" -nt "${_GENERATION_START_MARKER}" ]
		then
			stale_count=$((stale_count + 1))

			if [[ "${#examples[@]}" -lt 5 ]]
			then
				examples+=("not regenerated in this run: ${html_file}")
			fi
		fi
	done < <(find "${docs_dir}" -path "*/en/*" -name "*.md" -not -path "*/_snippets/*" -not -path "*/resources/*" -printf "%P\n")

	if [[ "${missing_count}" -gt 0 ]] ||
	   [[ "${stale_count}" -gt 0 ]]
	then
		echo "[cron-1] ERROR: the generated site does not match the markdown sources: ${missing_count} missing and ${stale_count} not regenerated out of ${expected_count}, refusing to publish"

		printf "[cron-1]   %s\n" "${examples[@]}"

		exit 1
	fi

	echo "[cron-1] Generated site verified: ${expected_count} HTML files fresh from this run."

	local site_digest=$( \
		cd "${site_dir}" && \
		find . -name "*.html" -print0 | \
			sort --zero-terminated | \
			xargs --null md5sum | \
			md5sum | \
			awk '{print $1}')

	echo "[cron-1] Generated site digest: ${site_digest}"
}

function check_reference_cache {
	_REFERENCE_FINGERPRINT=""
	_SKIP_REFERENCE_COPY=""

	if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" == "true" ]
	then
		return 0
	fi

	local common_file="${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/_common.sh"

	local doc_file_name release

	doc_file_name=$(sed --expression "s/^readonly LIFERAY_LEARN_DXP_DOC_FILE_NAME=//p" --quiet "${common_file}")
	release=$(sed --expression "s/^readonly LIFERAY_LEARN_DXP_RELEASE_TOKEN_VALUE=//p" --quiet "${common_file}")

	if [ -z "${doc_file_name}" ] || [ -z "${release}" ]
	then
		echo "[cron-1] Reference cache: release tokens not found in _common.sh, forcing a full refresh."

		return 0
	fi

	local total_size

	total_size=$( \
		curl \
			--dump-header - \
			--fail \
			--header "Range: bytes=0-0" \
			--location \
			--output /dev/null \
			--silent \
			"https://releases-cdn.liferay.com/dxp/${release}/${doc_file_name}" | \
			tr --delete "\r" | \
			grep --ignore-case "^content-range:" | \
			tail --lines=1 | \
			sed --expression "s|.*/||")

	if [ -z "${total_size}" ]
	then
		echo "[cron-1] Reference cache: no fingerprint from the CDN, forcing a full refresh."

		return 0
	fi

	_REFERENCE_FINGERPRINT="${doc_file_name}|${total_size}"

	if [ "${LIFERAY_LEARN_ETC_CRON_FORCE_REFERENCE_DOCS:-}" == "true" ]
	then
		echo "[cron-1] Reference cache: forced refresh requested, ignoring the marker."

		return 0
	fi

	if [ -f /public_html/.learn-importer-reference-marker ] && [ "$(cat /public_html/.learn-importer-reference-marker)" == "${_REFERENCE_FINGERPRINT}" ]
	then
		if [ ! -f /public_html/.learn-importer-apps-md ]
		then
			echo "[cron-1] Reference cache: apps.md not cached yet, forcing a full refresh."

			return 0
		fi

		echo "[cron-1] Reference cache HIT (${_REFERENCE_FINGERPRINT}): skipping javadoc download, extraction and copy."

		_SKIP_REFERENCE_COPY="true"

		export LIFERAY_LEARN_ETC_CRON_SKIP_REFERENCE_DOCS="true"
	else
		echo "[cron-1] Reference cache MISS (${_REFERENCE_FINGERPRINT}): full javadoc refresh."
	fi
}

function clone_repository {
	record_phase "clone"

	rm --force --recursive "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}"

	git clone \
		--branch "${LIFERAY_LEARN_ETC_CRON_GITHUB_BRANCH:-master}" \
		--depth 1 \
		--single-branch \
		git@github.com:"${LIFERAY_LEARN_ETC_CRON_GITHUB_USER:-liferay}"/liferay-learn.git \
		"${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}"

	git \
		-C "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}" \
		log \
		--max-count=1 \
		--pretty="Cloned at commit: %H %aN %s"

	echo "commit $(git -C "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}" rev-parse --short=8 HEAD)" >> /tmp/liferay_learn_run_phases
}

function copy_resources {
	record_phase "copy"

	local delete_flag="--delete"

	if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" == "true" ]
	then
		echo "[cron-1] Partial mode: rsync without --delete."

		delete_flag=""
	fi

	local digest

	digest=$(resources_digest)

	if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" != "true" ] &&
	   [ -f /public_html/.learn-importer-resources-digest ] &&
	   [ "$(cat /public_html/.learn-importer-resources-digest)" == "${digest}" ]
	then
		echo "[cron-1] Resources digest HIT (${digest}): skipping the examples and images copy."
	else
		echo "[cron-1] Resources digest MISS (${digest}): copying the examples and images."

		rsync \
			--include="*.zip" \
			--include="*/" \
			--exclude="*" \
			--inplace \
			--prune-empty-dirs \
			--recursive \
			--whole-file \
			"${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site/" \
			/public_html

		rsync \
			${delete_flag} \
			--inplace \
			--prune-empty-dirs \
			--recursive \
			--whole-file \
			"${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site/examples" \
			/public_html

		copy_tree_sharded "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site/images" /public_html/images "${delete_flag}"

		if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" != "true" ]
		then
			echo "${digest}" > /public_html/.learn-importer-resources-digest
		fi
	fi

	if [ -z "${_SKIP_REFERENCE_COPY}" ]
	then
		copy_tree_sharded "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site/reference" /public_html/reference "${delete_flag}"

		if [ -n "${_REFERENCE_FINGERPRINT}" ]
		then
			cp "${_APPS_MARKDOWN_FILE_NAME}" /public_html/.learn-importer-apps-md

			echo "${_REFERENCE_FINGERPRINT}" > /public_html/.learn-importer-reference-marker

			echo "[cron-1] Reference marker and apps.md cache updated."
		fi
	fi
}

function copy_tree_sharded {
	local source_dir=${1} dest_dir=${2} delete_flag=${3}

	local work_dir

	work_dir=$(mktemp --directory)

	(cd "${source_dir}" && find . -type f) > "${work_dir}/files"

	if [ ! -s "${work_dir}/files" ]
	then
		echo "[cron-1] Nothing to copy from ${source_dir}."

		return 0
	fi

	split --number=l/16 "${work_dir}/files" "${work_dir}/chunk-"

	local pids=()

	local chunk

	for chunk in "${work_dir}"/chunk-*
	do
		rsync \
			--files-from="${chunk}" \
			--inplace \
			--whole-file \
			"${source_dir}/" \
			"${dest_dir}/" &

		pids+=("${!}")
	done

	local pid

	for pid in "${pids[@]}"
	do
		wait "${pid}"
	done

	if [ -n "${delete_flag}" ]
	then
		rsync \
			--delete \
			--existing \
			--ignore-existing \
			--prune-empty-dirs \
			--recursive \
			"${source_dir}/" \
			"${dest_dir}/"
	fi

	rm --force --recursive "${work_dir}"
}

function generate_docs {
	record_phase "generation"

	export LIFERAY_LEARN_SKIP_LOCALES=${LIFERAY_LEARN_ETC_CRON_SKIP_LOCALES_CONTENT:-}

	echo "[cron-1] Locales excluded from conversion: ${LIFERAY_LEARN_SKIP_LOCALES}"

	if [ -n "${_SKIP_REFERENCE_COPY}" ]
	then
		mkdir --parents "$(dirname "${_APPS_MARKDOWN_FILE_NAME}")"

		cp /public_html/.learn-importer-apps-md "${_APPS_MARKDOWN_FILE_NAME}"

		echo "[cron-1] Restored apps.md from the reference cache."
	fi

	_GENERATION_START_MARKER=$(mktemp)

	pushd "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}"

	env --unset=LIFERAY_LEARN_ETC_CRON_SLACK_CHANNEL --unset=LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT ./generate_docs.sh

	popd

	check_generated_site
}

function hours_since {
	local seconds=${1:-}

	if [ -z "${seconds}" ] || [ "${seconds}" == "0" ]
	then
		echo 99999

		return 0
	fi

	echo "$((($(date +%s) - seconds) / 3600))"
}

function log_url {
	echo "https://console.${LCP_INFRASTRUCTURE_DOMAIN:-}/projects/${LCP_PROJECT_ID:-}/logs?instanceId=${HOSTNAME:-}&logServiceId=${LCP_SERVICE_ID:-}"
}

function main {
	exec > >(tee /tmp/liferay_learn_run.log) 2>&1

	rm --force /tmp/liferay_jar_runner_skipped /tmp/liferay_learn_run_phases

	set_up_ssh

	check_for_changes

	record_attempt

	notify_slack ":rocket: *liferay-learn-etc-cron-1* run started\\nBranch: ${LIFERAY_LEARN_ETC_CRON_GITHUB_BRANCH:-master} · Source: ${_REMOTE_COMMIT:0:8} · <$(log_url)|console log>"

	check_environment

	clone_repository

	run_preflight

	check_reference_cache

	generate_docs

	notify_sources_ready

	copy_resources

	if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" != "true" ]
	then
		write_manifest
	else
		echo "[cron-1] Partial mode: manifest not written (it only describes full runs)."
	fi

	notify_resources_published

	echo "phase import $(date +%s)" >> /tmp/liferay_learn_run_phases

	touch /tmp/liferay_jar_runner_set_up_ok

	echo "[cron-1] Setup completed, handing over to the importer."
}

function notify_resources_published {
	local detail

	detail="copy $(span copy now)"

	if [ "${LIFERAY_LEARN_ETC_CRON_PARTIAL:-}" != "true" ]
	then
		detail="copy $(span copy manifest) · manifest $(span manifest now)"
	fi

	notify_slack ":truck: *Resources published* · $(span copy now)\\n${detail}"
}

function notify_slack {
	local text=${1}

	if [ -z "${LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT:-}" ]
	then
		return 0
	fi

	local payload_file

	payload_file=$(mktemp)

	printf '{"channel":"%s","icon_emoji":":robot_face:","text":"%s","username":"learn-importer"}' \
		"${LIFERAY_LEARN_ETC_CRON_SLACK_CHANNEL:-}" \
		"${text}" > "${payload_file}"

	curl \
		--data "@${payload_file}" \
		--header "Content-Type: application/json" \
		--max-time 30 \
		--silent \
		"${LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT}" > /dev/null || true

	rm --force "${payload_file}"
}

function notify_sources_ready {
	local commit

	commit=$(source_commit)

	local source_link="unknown"

	if [ -n "${commit}" ]
	then
		source_link="<https://github.com/${LIFERAY_LEARN_ETC_CRON_GITHUB_USER:-liferay}/liferay-learn/commit/${commit}|${commit}>"
	fi

	notify_slack ":hammer: *Sources ready* · $(span preflight now)\\nSource: ${source_link} · clone $(span clone preflight-dxp) · preflight-dxp $(span preflight-dxp generation) · generation $(span generation now)$(preflight_warnings)"
}

function phase_seconds {
	local name=${1}

	if [ ! -f /tmp/liferay_learn_run_phases ]
	then
		return 0
	fi

	grep "^phase ${name} " /tmp/liferay_learn_run_phases | \
		tail --lines=1 | \
		awk '{print $3}' || true
}

function preflight_warnings {
	local warnings

	warnings=$( \
		grep "\[preflight\] Warning: " /tmp/liferay_learn_run.log | \
			sed \
				--expression "s|.*\[preflight\] Warning: ||" \
				--expression "s|${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR:-}/||" \
				--expression "s|\\\\|\\\\\\\\|g" \
				--expression "s|\"|\\\\\"|g") || true

	if [ -z "${warnings}" ]
	then
		return 0
	fi

	local count

	count=$(echo "${warnings}" | wc --lines)

	local message="\\n:warning: ${count} preflight warnings:"

	local warning

	while read -r warning
	do
		message="${message}\\n> ${warning}"
	done < <(echo "${warnings}" | head --lines=5)

	if [[ "${count}" -gt 5 ]]
	then
		message="${message}\\n> and $((count - 5)) more, see the console log"
	fi

	echo "${message}"
}

function record_attempt {
	if ! echo "${_REMOTE_COMMIT} $(date +%s) ${LIFERAY_LEARN_ETC_CRON_FORCE_RUN:-none}" > "${_ATTEMPT_FILE_NAME}"
	then
		echo "[cron-1] ERROR: ${_ATTEMPT_FILE_NAME} is not writable"

		exit 1
	fi

	echo "[cron-1] The attempt was recorded for commit ${_REMOTE_COMMIT}."
}

function record_phase {
	local name=${1}

	echo "[cron-1] Phase: ${name}"

	echo "phase ${name} $(date +%s)" >> /tmp/liferay_learn_run_phases
}

function resources_digest {
	local docs_dir="${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/docs"

	if [ ! -d "${docs_dir}" ]
	then
		echo "absent"

		return 0
	fi

	( \
		cd "${docs_dir}" && \
		find . -type f \( -path "*/images/*" -o -path "*.zip/*" \) -print0 | \
			sort --zero-terminated | \
			xargs --null md5sum | \
			md5sum | \
			awk '{print $1}')
}

function run_preflight {
	record_phase "preflight-dxp"

	local java_options=(${LIFERAY_JAR_RUNNER_JAVA_OPTS:-})

	java "${java_options[@]}" -jar /opt/liferay/jar-runner.jar --preflight
}

function set_up_ssh {
	if [ -z "${LIFERAY_LEARN_ETC_CRON_GITHUB_DEPLOY_KEY:-}" ]
	then
		echo "[cron-1] ERROR: missing required environment variable: LIFERAY_LEARN_ETC_CRON_GITHUB_DEPLOY_KEY"

		exit 1
	fi

	eval "$(ssh-agent -s)"

	echo -e "-----BEGIN OPENSSH PRIVATE KEY-----\n${LIFERAY_LEARN_ETC_CRON_GITHUB_DEPLOY_KEY}\n-----END OPENSSH PRIVATE KEY-----" | ssh-add -

	export GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=accept-new -q"
}

function source_commit {
	if [ ! -f /tmp/liferay_learn_run_phases ]
	then
		return 0
	fi

	grep "^commit " /tmp/liferay_learn_run_phases | \
		tail --lines=1 | \
		awk '{print $2}' || true
}

function span {
	local seconds

	seconds=$(span_seconds "${1}" "${2}")

	if [ -z "${seconds}" ]
	then
		printf "n/a"

		return 0
	fi

	to_duration "${seconds}"
}

function span_seconds {
	local from_name=${1} to_name=${2}

	local from_seconds to_seconds

	from_seconds=$(phase_seconds "${from_name}")

	if [ "${to_name}" == "now" ]
	then
		to_seconds=$(date +%s)
	else
		to_seconds=$(phase_seconds "${to_name}")
	fi

	if [ -z "${from_seconds}" ] || [ -z "${to_seconds}" ]
	then
		return 0
	fi

	echo "$((to_seconds - from_seconds))"
}

function to_duration {
	local seconds=${1}

	if [[ "${seconds}" -lt 60 ]]
	then
		printf "%ds" "${seconds}"
	else
		printf "%dm%ds" "$((seconds / 60))" "$((seconds % 60))"
	fi
}

function write_manifest {
	record_phase "manifest"

	local commit

	commit=$(git -C "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}" rev-parse HEAD)

	local zips_json

	zips_json=$( \
		cd "${LIFERAY_LEARN_ETC_CRON_GIT_REPOSITORY_DIR}/site" && \
		find . -name "*.zip" -type f | \
			sed --expression "s|^\./||" | \
			sort | \
			awk '{printf "%s\"%s\"", separator, $0; separator = ", "}')

	cat > /public_html/.learn-importer-manifest.json <<EOF
{
	"generatedAt": "$(date --iso-8601=seconds --utc)",
	"managedRoots": ["examples", "images", "reference"],
	"managedZips": [${zips_json}],
	"sourceCommit": "${commit}"
}
EOF

	echo "[cron-1] Manifest written to /public_html/.learn-importer-manifest.json."
}

main "${@}"
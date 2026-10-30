#!/bin/bash

function log_tail {
	if [ ! -f /tmp/liferay_learn_run.log ]
	then
		return 0
	fi

	tail --lines=25 /tmp/liferay_learn_run.log | \
		sed \
			--expression "s/\\\\/\\\\\\\\/g" \
			--expression "s/\"/\\\\\"/g" \
			--expression "s/\t/    /g" | \
		awk '{printf "%s\\n", $0}'
}

function main {
	if [ -f /tmp/liferay_jar_runner_skipped ]
	then
		echo "[cron-1] Job completed: there was nothing to do."

		return 0
	fi

	if [ -f /tmp/liferay_jar_runner_main_ok ]
	then
		echo "[cron-1] Job completed successfully."

		return 0
	fi

	echo "[cron-1] Job failed: the importer did not complete successfully."

	notify_slack

	exit 1
}

function notify_slack {
	if [ -f /tmp/liferay_learn_slack_notified ]
	then
		return 0
	fi

	if [ -z "${LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT:-}" ]
	then
		return 0
	fi

	local last_phase="none"

	if [ -f /tmp/liferay_learn_run_phases ]
	then
		last_phase=$( \
			grep "^phase " /tmp/liferay_learn_run_phases | \
				tail --lines=1 | \
				awk '{print $2}')
	fi

	local text=":rotating_light: *liferay-learn-etc-cron-1* run failed before the importer could report. Last phase reached: ${last_phase}."

	local tail_lines

	tail_lines=$(log_tail)

	if [ -n "${tail_lines}" ]
	then
		text="${text}\\n\\nLast lines of the run:\\n\`\`\`\\n${tail_lines}\`\`\`"
	fi

	local log_url="https://console.${LCP_INFRASTRUCTURE_DOMAIN:-}/projects/${LCP_PROJECT_ID:-}/logs?instanceId=${HOSTNAME:-}&logServiceId=${LCP_SERVICE_ID:-}"

	local payload_file

	payload_file=$(mktemp)

	printf '{"channel":"%s","icon_emoji":":robot_face:","text":"%s <%s|console log>","username":"learn-importer"}' \
		"${LIFERAY_LEARN_ETC_CRON_SLACK_CHANNEL:-}" \
		"${text}" \
		"${log_url}" > "${payload_file}"

	curl \
		--data "@${payload_file}" \
		--header "Content-Type: application/json" \
		--max-time 30 \
		--silent \
		"${LIFERAY_LEARN_ETC_CRON_SLACK_ENDPOINT}" > /dev/null || true

	rm --force "${payload_file}"
}

main
#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})
TEST_SMTP_SERVER_DIR="/tmp"
TEST_SMTP_SERVER_URL="https://repository-cdn.liferay.com/nexus/content/repositories/third-party/com/liferay/com.mockmock/1.4.0/com.mockmock-1.4.0.jar"

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}
echo TEST_SMTP_SERVER_DIR=${TEST_SMTP_SERVER_DIR}
echo TEST_SMTP_SERVER_URL=${TEST_SMTP_SERVER_URL}

source ${CURRENT_DIR_NAME}/../../../../env/common.sh

function check_mockmock_start {
local sleep_duration=60
local sleep_interval=2
local total_duration=0

while ! curl --output /dev/null --silent --head --fail http://localhost:8282
do
	if [ ${total_duration} -ge ${sleep_duration} ]; then
	  echo "Unable to start MockMock smtp server."
	  exit 1
	fi
	  sleep ${sleep_interval}
	  total_duration=$((total_duration + sleep_interval))
done

echo "Started MockMock smtp server."
}

function download_mockmock {
USER_AGENT="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
max_attempts=5
attempt=1

while ! wget --user-agent="${USER_AGENT}" "${TEST_SMTP_SERVER_URL}" -O "${TEST_SMTP_SERVER_DIR}/MockMock.jar";
do
if [ $attempt -ge $max_attempts ]; then
	echo "Failed to download MockMock.jar after $max_attempts attempts."
	exit 1
fi
echo "Download failed. Retrying in 20 seconds... (Attempt $((attempt+1))/$max_attempts)"
attempt=$((attempt+1))
sleep 20
done

echo "Download MockMock successful."
}

function main {
default_set_up

download_mockmock

java --add-opens java.base/java.lang=ALL-UNNAMED -jar ${TEST_SMTP_SERVER_DIR}/MockMock.jar -p 25000 &

check_mockmock_start
}

main "${@}"
}
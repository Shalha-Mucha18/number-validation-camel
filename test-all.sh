#!/usr/bin/env bash
# Sends every sample request to the running demo and prints the response.
# Start the app first:  mvn spring-boot:run
BASE=http://localhost:9091/cxf/api/v1/demo
cd "$(dirname "$0")/requests"

call() {   # $1 = title, $2 = endpoint, $3 = file, $4 = channel header (optional)
  echo "=================== $1"
  if [ -n "$4" ]; then
    curl -s -X POST "$BASE/$2" -H "Content-Type: application/xml" -H "channel: $4" --data-binary @"$3"
  else
    curl -s -X POST "$BASE/$2" -H "Content-Type: application/xml" --data-binary @"$3"
  fi
  echo; echo
}

call "1. Success"                 mnpValidation 01-success.xml        MYAPP
call "2. Empty SIM -> 039"        mnpValidation 02-empty-sim.xml      MYAPP
call "3. Blocked SIM -> 040"      mnpValidation 03-blocked-sim.xml    MYAPP
call "4. Missing bsCode -> EX002" mnpValidation 04-missing-bscode.xml MYAPP
call "5. Inactive number -> 041"  mnpValidation 05-inactive-number.xml MYAPP
call "6. RMS down -> RMS500"      mnpValidation 06-rms-down.xml       MYAPP
call "7. No channel header -> EX002" mnpValidation 01-success.xml
call "8. Split/aggregate"         channelStatus 07-channel-list.xml   MYAPP
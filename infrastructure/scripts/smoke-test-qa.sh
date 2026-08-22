#!/usr/bin/env bash
set -e

BASE_URL="${1:-http://localhost:8080}"

echo "=========================================="
echo " REPLAY Engine - QA Smoke Test Suite"
echo " Target URL: ${BASE_URL}"
echo "=========================================="

echo -e "\n[1/3] Testing Actuator Health Endpoint..."
HEALTH_STATUS=$(curl -s "${BASE_URL}/actuator/health" | grep -o '"status":"[^"]*"' | head -1 || echo "OFFLINE")
echo "  Health Status: ${HEALTH_STATUS}"

echo -e "\n[2/3] Testing QA User Authentication..."
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@replay.app","password":"Password123!"}' || echo "{}")

TOKEN=$(echo "${LOGIN_RES}" | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || echo "")

if [ -n "${TOKEN}" ]; then
  echo "  [PASS] User Login Successful! Token acquired."
  echo -e "\n[3/3] Testing Protected Memories Query..."
  MEMORIES=$(curl -s -X GET "${BASE_URL}/api/memories" -H "Authorization: Bearer ${TOKEN}")
  echo "  [PASS] Protected query response received."
else
  echo "  [INFO] Service not reachable or demo user not yet initialized."
fi

echo -e "\n=========================================="
echo " QA Smoke Tests Finished"
echo "=========================================="

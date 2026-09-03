#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCK_FILE="${ROOT_DIR}/third_party/llama.cpp.lock"
DEST_DIR="${ROOT_DIR}/third_party/llama.cpp"

repo="$(awk -F= '$1 == "repository" { print substr($0, index($0, "=") + 1) }' "${LOCK_FILE}")"
commit="$(awk -F= '$1 == "commit" { print $2 }' "${LOCK_FILE}")"

if [[ -z "${repo}" || -z "${commit}" ]]; then
    echo "Invalid lock file: ${LOCK_FILE}" >&2
    exit 1
fi

if [[ ! -d "${DEST_DIR}/.git" ]]; then
    rm -rf "${DEST_DIR}"
    git clone --filter=blob:none --no-checkout "${repo}" "${DEST_DIR}"
fi

git -C "${DEST_DIR}" fetch --depth=1 origin "${commit}"
git -C "${DEST_DIR}" checkout --detach "${commit}"

actual="$(git -C "${DEST_DIR}" rev-parse HEAD)"
if [[ "${actual}" != "${commit}" ]]; then
    echo "llama.cpp revision mismatch: expected ${commit}, got ${actual}" >&2
    exit 1
fi

echo "llama.cpp synced at ${actual}"

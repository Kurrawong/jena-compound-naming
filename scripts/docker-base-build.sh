#!/usr/bin/env bash
set -euo pipefail

repo_url="${FUSEKI_CONTAINER_IMAGE_REPO:?FUSEKI_CONTAINER_IMAGE_REPO is required}"
repo_dir="${FUSEKI_CONTAINER_IMAGE_DIR:?FUSEKI_CONTAINER_IMAGE_DIR is required}"
pr_ref="${FUSEKI_CONTAINER_IMAGE_PR_REF:?FUSEKI_CONTAINER_IMAGE_PR_REF is required}"
image_tag="${FUSEKI_BASE_IMAGE_TAG:?FUSEKI_BASE_IMAGE_TAG is required}"

echo "[docker:base:build] Preparing repo at ${repo_dir}"
if [ ! -d "${repo_dir}/.git" ]; then
  git clone --filter=blob:none "${repo_url}" "${repo_dir}"
fi

echo "[docker:base:build] Fetching ${pr_ref}"
git -C "${repo_dir}" fetch --prune origin "${pr_ref}"

commit="$(git -C "${repo_dir}" rev-parse --short=12 FETCH_HEAD)"
git -C "${repo_dir}" checkout --detach FETCH_HEAD >/dev/null 2>&1

echo "[docker:base:build] Building ${image_tag} from fuseki-container-image@${commit}"
if [ "${CI:-}" = "true" ]; then
  docker build -q -t "${image_tag}" -f "${repo_dir}/docker/Dockerfile" "${repo_dir}/docker" >/dev/null
else
  docker build -t "${image_tag}" -f "${repo_dir}/docker/Dockerfile" "${repo_dir}/docker"
fi
echo "[docker:base:build] Built ${image_tag} from fuseki-container-image@${commit}"

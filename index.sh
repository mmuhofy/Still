#!/usr/bin/env bash

set -euo pipefail

GITHUB_USER="mmuhofy"
REPO_NAME="Still"
BRANCH="main"

OUTPUT_FILE="index.txt"

declare -A groups

while IFS= read -r file
do
    relative="${file#./}"

    # Gereksiz dosya/klasörleri atla
    case "$relative" in
        .git/*|.gradle/*|.idea/*|build/*|app/build/*)
            continue
            ;;
    esac

    if [[ "$relative" == */* ]]; then
        category="${relative%%/*}"
    else
        category="root"
    fi

    groups["$category"]+="$relative"$'\n'

done < <(find . -type f | sort)

{
    echo "# Still Project Index"
    echo

    for category in $(printf '%s\n' "${!groups[@]}" | sort)
    do
        echo "## ${category}/"
        echo

        while IFS= read -r path
        do
            [[ -z "$path" ]] && continue

            echo "https://raw.githubusercontent.com/${GITHUB_USER}/${REPO_NAME}/${BRANCH}/${path}"
        done <<< "${groups[$category]}"

        echo
    done
} > "$OUTPUT_FILE"

echo "Generated: $OUTPUT_FILE"

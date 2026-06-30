#!/usr/bin/env python3
import re
import sys


def main():
    input_lines = [line.strip() for line in sys.stdin if line.strip()]

    if not input_lines or len(input_lines) == 0:
        print("No commits to check.")
        sys.exit(0)

    print(f"Found {len(input_lines)} commit(s).")

    pattern = re.compile(
        r"^(feat|fix|docs|style|refactor|test|chore)(\([a-zA-Z0-9_-]+\))?: .+$"
    )
    has_errors = False
    separator = "|"

    for line in input_lines:
        if separator not in line:
            continue

        commit_sha, commit_msg = line.split(separator, 1)

        if commit_msg.startswith("Merge ") or ("merge pull request" in commit_msg.lower()):
            continue

        if (len(commit_msg) > 72):
            print(f"Commit message [{commit_sha[:8]}]: \"{commit_msg}\" too long: ({len(commit_msg)}/72 characters).")
            has_errors = True
            continue

        if not pattern.match(commit_msg):
            print(f"Commit message [{commit_sha[:8]}]: \"{commit_msg}\" does not follow Conventional Commits format.")
            has_errors = True
        else:
            print(f"Commit message [{commit_sha[:8]}]: \"{commit_msg}\" passed.")

    if has_errors:
        sys.exit(1)
    else:
        print("\nAll commits are properly formatted.")
        sys.exit(0)


if __name__ == "__main__":
    main()

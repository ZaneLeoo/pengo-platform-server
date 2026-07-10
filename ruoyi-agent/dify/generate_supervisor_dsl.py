#!/usr/bin/env python3
"""根据当前 Dify 租户的工具 provider ID 生成可导入 Supervisor DSL。"""

from __future__ import annotations

import argparse
import re
import uuid
from pathlib import Path


DEFAULT_AGENT_PLUGIN = (
    "langgenius/agent:0.0.40@"
    "d00a70e81bfb28fadd52cba7fd7a1f7c344c67b051e4a2e356a06c690736a7c4"
)
PLACEHOLDER_PATTERN = re.compile(r"__[A-Z0-9_]+__")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="生成企业 Agent V2 Dify DSL")
    parser.add_argument("--tool-provider-id", required=True, help="导入 OpenAPI 后 Dify 分配的 provider UUID")
    parser.add_argument("--model-provider", default="langgenius/deepseek/deepseek")
    parser.add_argument("--model-name", default="deepseek-chat")
    parser.add_argument("--agent-plugin-identifier", default=DEFAULT_AGENT_PLUGIN)
    parser.add_argument(
        "--output",
        type=Path,
        default=Path(__file__).with_name("agent-v2-supervisor.generated.yml"),
    )
    return parser.parse_args()


def require_safe_scalar(name: str, value: str) -> str:
    value = value.strip()
    if not value or not re.fullmatch(r"[A-Za-z0-9._/@:+-]+", value):
        raise ValueError(f"{name} 包含不安全字符")
    return value


def require_provider_id(value: str) -> str:
    try:
        return str(uuid.UUID(value.strip()))
    except ValueError as error:
        raise ValueError("tool provider id 必须是 Dify 分配的 UUID") from error


def main() -> None:
    args = parse_args()
    values = {
        "__TOOL_PROVIDER_ID__": require_provider_id(args.tool_provider_id),
        "__MODEL_PROVIDER__": require_safe_scalar("model provider", args.model_provider),
        "__MODEL_NAME__": require_safe_scalar("model name", args.model_name),
        "__AGENT_PLUGIN_IDENTIFIER__": require_safe_scalar(
            "agent plugin identifier", args.agent_plugin_identifier
        ),
    }
    template_path = Path(__file__).with_name("agent-v2-supervisor.template.yml")
    content = template_path.read_text(encoding="utf-8")
    for placeholder, value in values.items():
        content = content.replace(placeholder, value)
    unresolved = sorted(set(PLACEHOLDER_PATTERN.findall(content)))
    if unresolved:
        raise ValueError(f"存在未替换占位符：{', '.join(unresolved)}")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(content, encoding="utf-8", newline="\n")
    print(f"已生成：{args.output.resolve()}")


if __name__ == "__main__":
    main()

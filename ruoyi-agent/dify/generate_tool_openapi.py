#!/usr/bin/env python3
"""为当前环境生成包含真实 server URL 的 Dify 自定义工具 OpenAPI。"""

from __future__ import annotations

import argparse
from pathlib import Path
from urllib.parse import urlparse


PLACEHOLDER = "https://YOUR_PUBLIC_RUOYI_HOST"


def main() -> None:
    parser = argparse.ArgumentParser(description="生成 Agent V2 工具 OpenAPI")
    parser.add_argument("--server-url", required=True, help="Dify 可访问的 RuoYi 根地址")
    parser.add_argument(
        "--output",
        type=Path,
        default=Path(__file__).with_name("agent-v2-tools.generated.openapi.yaml"),
    )
    args = parser.parse_args()
    server_url = args.server_url.strip().rstrip("/")
    parsed = urlparse(server_url)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc or parsed.query or parsed.fragment:
        raise ValueError("server-url 必须是无 query/fragment 的 HTTP/HTTPS 地址")
    if parsed.scheme == "http" and parsed.hostname not in {"localhost", "127.0.0.1", "host.docker.internal"}:
        raise ValueError("HTTP server-url 仅允许本地开发地址；公网环境请使用 HTTPS")

    source = Path(__file__).parents[1] / "docs" / "agent-v2-tools.openapi.yaml"
    content = source.read_text(encoding="utf-8")
    if PLACEHOLDER not in content:
        raise ValueError("OpenAPI 模板缺少 server URL 占位符")
    content = content.replace(PLACEHOLDER, server_url)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(content, encoding="utf-8", newline="\n")
    print(f"已生成：{args.output.resolve()}")


if __name__ == "__main__":
    main()

param(
    [Parameter(Mandatory = $true)] [string] $BaseUrl,
    [Parameter(Mandatory = $true)] [string] $Username,
    [Parameter(Mandatory = $true)] [securestring] $Password,
    [string] $Query = '请统计物料分类数量，完成基础分析并生成柱状图',
    [int] $TimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
$base = $BaseUrl.TrimEnd('/')
$plainPassword = [System.Net.NetworkCredential]::new('', $Password).Password

try {
    $captcha = Invoke-RestMethod -Uri "$base/captchaImage" -Method Get -TimeoutSec 15
    if ($captcha.captchaEnabled) {
        throw '当前环境启用了验证码，请为自动化验收准备免验证码测试账号或临时关闭验证码。'
    }
    $loginBody = @{ username = $Username; password = $plainPassword } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$base/login" -Method Post -ContentType 'application/json' `
        -Body $loginBody -TimeoutSec 15
    if ($login.code -ne 200 -or [string]::IsNullOrWhiteSpace($login.token)) {
        throw "登录失败：$($login.msg)"
    }
    $headers = @{ Authorization = "Bearer $($login.token)" }
    $runBody = @{
        query = $Query
        inputs = @{}
        contextRefs = @{}
        attachmentIds = @()
    } | ConvertTo-Json -Depth 8
    $created = Invoke-RestMethod -Uri "$base/agent/v2/runs" -Method Post -Headers $headers `
        -ContentType 'application/json' -Body $runBody -TimeoutSec 15
    if ($created.code -ne 200 -or -not $created.data.runId) {
        throw "创建 Agent Run 失败：$($created.msg)"
    }
    $runId = [long] $created.data.runId
    $stream = Invoke-WebRequest -Uri "$base/agent/v2/runs/$runId/events?afterSequence=0" `
        -Headers $headers -UseBasicParsing -TimeoutSec $TimeoutSeconds

    $events = [regex]::Matches($stream.Content, '(?m)^data:(.+)$') | ForEach-Object {
        $_.Groups[1].Value.Trim() | ConvertFrom-Json
    }
    $eventNames = @($events | ForEach-Object { $_.event })
    $toolCodes = @($events | Where-Object { $_.event -eq 'tool.completed' } |
        ForEach-Object { $_.data.toolCode })
    $requiredTools = @('query_business_data', 'analyze_dataset', 'render_chart')
    $missingTools = @($requiredTools | Where-Object { $_ -notin $toolCodes })

    if ('run.completed' -notin $eventNames) {
        $failure = $events | Where-Object { $_.event -eq 'run.failed' } | Select-Object -Last 1
        throw "Run 未完成：$($failure.data.message)"
    }
    if ('artifact.created' -notin $eventNames) {
        throw 'Run 已完成，但没有收到 artifact.created。'
    }
    if ($missingTools.Count -gt 0) {
        throw "缺少预期工具调用：$($missingTools -join ', ')"
    }
    $sequences = @($events | ForEach-Object { [long] $_.sequence })
    for ($index = 1; $index -lt $sequences.Count; $index++) {
        if ($sequences[$index] -le $sequences[$index - 1]) {
            throw 'SSE sequence 不是严格递增。'
        }
    }

    Write-Output 'AGENT_V2_ACCEPTED=True'
    Write-Output "RUN_ID=$runId"
    Write-Output "CONVERSATION_ID=$($created.data.conversationId)"
    Write-Output "TOOLS=$($toolCodes -join ',')"
    Write-Output "EVENT_COUNT=$($events.Count)"
}
finally {
    $plainPassword = $null
}

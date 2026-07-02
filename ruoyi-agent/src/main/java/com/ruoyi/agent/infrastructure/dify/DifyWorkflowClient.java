package com.ruoyi.agent.infrastructure.dify;

import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import java.io.IOException;

/** Dify 工作流客户端。 */
public interface DifyWorkflowClient
{
    /** 上传本地文件，返回 Dify 文件 ID。 */
    DifyFileUploadResult uploadFile(DifyClientSettings settings, DifyFileUploadRequest request)
        throws IOException, InterruptedException;

    /** 以阻塞模式执行默认发布工作流。 */
    DifyWorkflowRunResult runBlocking(DifyClientSettings settings, DifyWorkflowRunRequest request)
        throws IOException, InterruptedException;

    /** 以流式模式执行默认发布工作流，并返回 workflow_finished 的最终结果。 */
    DifyWorkflowRunResult runStreaming(DifyClientSettings settings, DifyWorkflowRunRequest request)
        throws IOException, InterruptedException;
}

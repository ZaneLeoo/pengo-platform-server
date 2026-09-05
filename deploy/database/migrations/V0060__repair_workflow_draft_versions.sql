-- 同一流程只允许一个可编辑草稿。
-- 历史上重复编辑可能产生多个 DRAFT；保留最新逻辑版本，其余标记为 SUPERSEDED。
UPDATE pm_workflow_definition_version old_version
JOIN (
    SELECT definition_id, MAX(version_no) AS latest_draft_no
    FROM pm_workflow_definition_version
    WHERE version_status = 'DRAFT'
    GROUP BY definition_id
) latest_draft
  ON latest_draft.definition_id = old_version.definition_id
SET old_version.version_status = 'SUPERSEDED'
WHERE old_version.version_status = 'DRAFT'
  AND old_version.version_no < latest_draft.latest_draft_no;

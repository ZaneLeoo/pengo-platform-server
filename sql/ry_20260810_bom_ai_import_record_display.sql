-- AI 导入记录补充识别出的独立 BOM 数量，并回填已有追溯记录。
alter table bom_ai_import_trace
    add column recognized_bom_count int not null default 0 comment '识别出的独立 BOM 数量' after file_count;

update bom_ai_import_trace
set recognized_bom_count = json_length(json_extract(preview_payload, '$.documents'))
where preview_payload is not null
  and json_valid(preview_payload)
  and json_type(json_extract(preview_payload, '$.documents')) = 'ARRAY';

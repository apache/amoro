-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDataSize}', (summary::jsonb) -> 'newFileSize');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDataFileCnt}', (summary::jsonb) -> 'newFileCnt');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{positionDeleteSize}', (summary::jsonb) -> 'positionalDeleteSize');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{rewritePosDataFileCnt}', (summary::jsonb) -> 'reRowDeletedDataFileCnt');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDataRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDeleteSize}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDeleteFileCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{newDeleteRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{rewriteDataRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{rewritePosDataRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{eqDeleteRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = jsonb_set(summary::jsonb, '{posDeleteRecordCnt}', '0');
UPDATE table_optimizing_process SET summary = summary::jsonb - 'newFileSize';
UPDATE table_optimizing_process SET summary = summary::jsonb - 'newFileCnt';
UPDATE table_optimizing_process SET summary = summary::jsonb - 'positionalDeleteSize';
UPDATE table_optimizing_process SET summary = summary::jsonb - 'reRowDeletedDataFileCnt';

UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDataSize}', (metrics_summary::jsonb) -> 'newFileSize');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDataFileCnt}', (metrics_summary::jsonb) -> 'newFileCnt');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{positionDeleteSize}', (metrics_summary::jsonb) -> 'positionalDeleteSize');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{rewritePosDataFileCnt}', (metrics_summary::jsonb) -> 'reRowDeletedDataFileCnt');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDataRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDeleteSize}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDeleteFileCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{newDeleteRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{rewriteDataRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{rewritePosDataRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{eqDeleteRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = jsonb_set(metrics_summary::jsonb, '{posDeleteRecordCnt}', '0');
UPDATE task_runtime SET metrics_summary = metrics_summary::jsonb - 'newFileSize';
UPDATE task_runtime SET metrics_summary = metrics_summary::jsonb - 'newFileCnt';
UPDATE task_runtime SET metrics_summary = metrics_summary::jsonb - 'positionalDeleteSize';
UPDATE task_runtime SET metrics_summary = metrics_summary::jsonb - 'reRowDeletedDataFileCnt';

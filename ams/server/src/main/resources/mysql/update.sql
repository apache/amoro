-- If you have any changes to the AMS database, please record them in this file.
-- We will confirm the corresponding version of these upgrade scripts when releasing.

UPDATE table_optimizing_process
SET summary = JSON_SET(summary, '$.newDataSize', JSON_EXTRACT(summary, '$.newFileSize')),
    summary = JSON_SET(summary, '$.newDataFileCnt', JSON_EXTRACT(summary, '$.newFileCnt')),
    summary = JSON_SET(summary, '$.positionDeleteSize',
                       JSON_EXTRACT(summary, '$.positionalDeleteSize')),
    summary = JSON_SET(summary, '$.rewritePosDataFileCnt',
                       JSON_EXTRACT(summary, '$.reRowDeletedDataFileCnt')),
    summary = JSON_SET(summary, '$.newDataRecordCnt', 0),
    summary = JSON_SET(summary, '$.newDeleteSize', 0),
    summary = JSON_SET(summary, '$.newDeleteFileCnt', 0),
    summary = JSON_SET(summary, '$.newDeleteRecordCnt', 0),
    summary = JSON_SET(summary, '$.rewriteDataRecordCnt', 0),
    summary = JSON_SET(summary, '$.rewritePosDataRecordCnt', 0),
    summary = JSON_SET(summary, '$.eqDeleteRecordCnt', 0),
    summary = JSON_SET(summary, '$.posDeleteRecordCnt', 0),
    summary = JSON_REMOVE(summary, '$.newFileSize'),
    summary = JSON_REMOVE(summary, '$.newFileCnt'),
    summary = JSON_REMOVE(summary, '$.positionalDeleteSize'),
    summary = JSON_REMOVE(summary, '$.reRowDeletedDataFileCnt');

UPDATE task_runtime
SET metrics_summary = JSON_SET(metrics_summary, '$.newDataSize', JSON_EXTRACT(metrics_summary, '$.newFileSize')),
    metrics_summary = JSON_SET(metrics_summary, '$.newDataFileCnt', JSON_EXTRACT(metrics_summary, '$.newFileCnt')),
    metrics_summary = JSON_SET(metrics_summary, '$.positionDeleteSize',
                               JSON_EXTRACT(metrics_summary, '$.positionalDeleteSize')),
    metrics_summary = JSON_SET(metrics_summary, '$.rewritePosDataFileCnt',
                               JSON_EXTRACT(metrics_summary, '$.reRowDeletedDataFileCnt')),
    metrics_summary = JSON_SET(metrics_summary, '$.newDataRecordCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.newDeleteSize', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.newDeleteFileCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.newDeleteRecordCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.rewriteDataRecordCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.rewritePosDataRecordCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.eqDeleteRecordCnt', 0),
    metrics_summary = JSON_SET(metrics_summary, '$.posDeleteRecordCnt', 0),
    metrics_summary = JSON_REMOVE(metrics_summary, '$.newFileSize'),
    metrics_summary = JSON_REMOVE(metrics_summary, '$.newFileCnt'),
    metrics_summary = JSON_REMOVE(metrics_summary, '$.positionalDeleteSize'),
    metrics_summary = JSON_REMOVE(metrics_summary, '$.reRowDeletedDataFileCnt');

use texera_db;

drop table if exists operator_runtime_statistics;

alter table workflow_executions add column runtime_stats_uri text default null;

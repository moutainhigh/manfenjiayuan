#添加新的列
ALTER TABLE table_name ADD column_name datatype

ALTER TABLE database_name.table_name ADD COLUMN column_def...;
ALTER TABLE tb_pos_procuct_v1 ADD COLUMN textStr VARCHAR;
ALTER TABLE table_name DROP COLUMN column_name
ALTER TABLE tb_pos_procuct_v1 RENAME COLUMN textStr TO haha;
#重命名表
ALTER TABLE database_name.table_name RENAME TO new_table_name;
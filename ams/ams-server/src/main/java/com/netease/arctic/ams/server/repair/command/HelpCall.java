package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;

public class HelpCall implements CallCommand {
  @Override
  public String call(Context context) {
    return
        "Usage:\n" +
            "\n" +
            "SHOW [ CATALOGS | DATABASES | TABLES ]                      -- Show catalogs, databases or tables --\n" +
            "\n" +
            "USE [ ${catalog_name} | ${database_name} ]                  -- Use catalog or database --\n" +
            "\n" +
            "ANALYZE ${table_name}                                       -- Analyze your table which will" +
            " list the lost files and provide you with repair options --\n" +
            "\n" +
            "REPAIR ${table_name} THROUGH FIND_BACK                      -- Retrieve your lost files from trash --\n" +
            "REPAIR ${table_name} THROUGH SYNC_METADATA                  -- Sync metadata which may" +
            " delete non-existent files in the metadata --\n" +
            "REPAIR ${table_name} THROUGH ROLLBACK ${snapshot_id}        -- Roll back to a snapshot" +
            " without lost files --\n" +
            "\n" +
            "OPTIMIZE [ STOP | START ] ${table_name}                     -- Start or stop optimizing tasks" +
            " for the table --\n" +
            "\n" +
            "REFRESH FILE_CACHE ${table_name}                            -- Flush the file cache for the table --\n" +
            "\n" +
            "QUIT                                                        -- Exit --"
        ;
  }
}

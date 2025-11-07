package org.apache.amoro.formats.paimon.info;

import org.apache.amoro.table.descriptor.StatisticsBaseInfo;
import org.apache.paimon.stats.ColStats;

import java.util.Map;

public class PaimonStatisticInfo extends StatisticsBaseInfo {
    private Map<String, ColStats<?>> colStas;

    public PaimonStatisticInfo(){}

    public PaimonStatisticInfo(Long snapshotId, Long schemaId, Long mergedRecordCount, Long mergedRecordSize, Map<String, ColStats<?>> colStas) {
        super(snapshotId, schemaId, mergedRecordCount, mergedRecordSize);
        this.colStas = colStas;
    }

}

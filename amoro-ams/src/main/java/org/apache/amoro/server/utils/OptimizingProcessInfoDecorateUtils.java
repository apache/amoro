package org.apache.amoro.server.utils;

import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class OptimizingProcessInfoDecorateUtils {

    final static String[] UNITS = {"d", "h", "min", "s", "ms"};
    final static long[] THRESHOLDS = {24 * 3600 * 1000L, 3600 * 1000L, 60 * 1000L, 1000L, 1L};

    public static void decorate(List<OptimizingProcessInfo> optimizingProcessInfos) {
         if (CollectionUtils.isEmpty(optimizingProcessInfos)) {
             return;
         }
        optimizingProcessInfos.forEach(optimizingProcessInfo -> {
            long duration = optimizingProcessInfo.getDuration();
            optimizingProcessInfo.setDurationDescriptor(formatCostTime(duration));
        });


    }

    private static String formatCostTime(long milliseconds){
        // Convert milliseconds to hours, minutes, seconds, and milliseconds

        // Calculate values for each time unit
        long[] values = new long[UNITS.length];
        for (int i = 0; i < THRESHOLDS.length; i++) {
            values[i] = milliseconds / THRESHOLDS[i];
            milliseconds %= THRESHOLDS[i];
        }

        // Generate output with at most two non-zero time units
        StringBuilder result = new StringBuilder();
        int addedUnits = 0;

        for (int i = 0; i < UNITS.length && addedUnits < 2; i++) {
            if (values[i] > 0) {
                if (addedUnits > 0) {
                    result.append(" ");
                }
                result.append(values[i]).append(" ").append(UNITS[i]);
                addedUnits++;
            }
        }
        return result.toString();
    }
}

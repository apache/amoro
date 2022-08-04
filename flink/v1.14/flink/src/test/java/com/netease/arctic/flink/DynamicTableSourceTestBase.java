package com.netease.arctic.flink;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.connector.source.abilities.SupportsWatermarkPushDown;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.planner.factories.TableFactoryHarness;

import java.io.Serializable;

public abstract class DynamicTableSourceTestBase extends TableFactoryHarness.ScanSourceBase implements
    SupportsWatermarkPushDown, Serializable {

  public static final long serialVersionUID = 1L;
  private WatermarkStrategy<RowData> watermarkStrategy;

  @Override
  public ChangelogMode getChangelogMode() {
    return ChangelogMode.all();
  }

  @Override
  public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
    init();
    return SourceFunctionProvider.of(
        new SourceFunction<RowData>() {
          @Override
          public void run(SourceContext<RowData> ctx) {
            WatermarkGenerator<RowData> generator =
                watermarkStrategy.createWatermarkGenerator(() -> null);
            WatermarkOutput output = new TestWatermarkOutput(ctx);
            doRun(generator, output, ctx);
          }

          @Override
          public void cancel() {
          }
        },
        false);
  }
  public void init() {};

  public abstract void doRun(WatermarkGenerator<RowData> generator, WatermarkOutput output,
                             SourceFunction.SourceContext<RowData> ctx);

  @Override
  public void applyWatermark(WatermarkStrategy<RowData> watermarkStrategy) {
    this.watermarkStrategy = watermarkStrategy;
  }

  public class TestWatermarkOutput implements WatermarkOutput, Serializable {
    public static final long serialVersionUID = 1L;
    public SourceFunction.SourceContext<RowData> ctx;

    public TestWatermarkOutput(SourceFunction.SourceContext<RowData> ctx) {
      this.ctx = ctx;
    }

    @Override
    public void emitWatermark(Watermark watermark) {
      ctx.emitWatermark(
          new org.apache.flink.streaming.api.watermark.Watermark(
              watermark.getTimestamp()));
    }

    @Override
    public void markIdle() {
    }

    @Override
    public void markActive() {
    }
  }
}

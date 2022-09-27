package com.netease.arctic.spark.optimize;

public class ClusterDistributionImpl implements ClusteredDistribution{

  private Expression[] clusterExprs;

  public ClusterDistributionImpl(Expression[] clusterExprs) {
    this.clusterExprs = clusterExprs;
  }

  @Override
  public Expression[] clustering() {
    return clusterExprs;
  }
}

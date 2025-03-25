package org.apache.amoro.resource;

public interface InternalResourceContainer extends ResourceContainer {

  /**
   * Start a new optimizer.
   *
   * @param resource resource information to start the optimizer
   */
  void requestResource(Resource resource);

  /**
   * Release a optimizer
   *
   * @param resource resource information to release the optimizer
   */
  void releaseOptimizer(Resource resource);
}

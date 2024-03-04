package com.netease.arctic.ams.api;

import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.TableState;

import java.util.List;

/**
 * TableRuntime is the key interface for the AMS framework to interact with the table.
 * Typically, it is used to get the table's configuration, process states, and table identifier.
 * The key usage is {@link com.netease.arctic.ams.api.process.ProcessFactory} to create and recover Process.
 */
public interface TableRuntime {

  /**
   * Get the list of optimizing process states.
   * Normally, the list contains one default optimizing state.
   * There could be more than one states if customized optimizing process factory is used.
   *
   * @return the list of optimizing process states
   */
  List<OptimizingState> getOptimizingStates();

  /**
   * Get the list of arbitrary process states.
   * One arbitrary state belongs to one arbitrary process related to one
   * {@link com.netease.arctic.ams.api.Action#ARBITRARY_ACTIONS}
   * There could be more than one arbitrary states depending on scheduler implementation.
   *
   * @return the list of arbitrary process states
   */
  List<TableState> getArbitraryStates();

  /**
   * Get the table identifier containing server side id and table format.
   *
   * @return the table identifier
   */
  ServerTableIdentifier getTableIdentifier();

  /**
   * Get the table configuration.
   *
   * @return the table configuration
   */
  TableConfiguration getTableConfiguration();
}

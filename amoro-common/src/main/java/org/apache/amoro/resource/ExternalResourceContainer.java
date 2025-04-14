package org.apache.amoro.resource;

import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;

public interface ExternalResourceContainer {

  Resource submit(TableProcess<TableProcessState> process);

  void release(String resourceId);
}

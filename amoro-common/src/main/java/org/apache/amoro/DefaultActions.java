package org.apache.amoro;

import java.util.Map;
import java.util.Optional;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;

public class DefaultActions {

  public static final Action MINOR_OPTIMIZING = new DefaultAction(1,10,"minor");
  public static final Action MAJOR_OPTIMIZING = new DefaultAction(2, 11, "major");
  public static final Action FULL_OPTIMIZING = new DefaultAction(3, 12, "full");
  // expire all metadata and data files necessarily.
  public static final Action EXPIRE_DATA = new DefaultAction(4, 1, "expire-data");
  public static final Action DELETE_ORPHAN_FILES = new DefaultAction(5, 2, "delete-orphans");
  public static final Action SYNC_HIVE_COMMIT = new DefaultAction(6, 3, "sync-hive");

  public static final Map<Integer, Action> ACTIONS = ImmutableMap.<Integer, Action>builder()
      .put(MINOR_OPTIMIZING.getCode(), MINOR_OPTIMIZING)
      .put(MAJOR_OPTIMIZING.getCode(), MAJOR_OPTIMIZING)
      .put(EXPIRE_DATA.getCode(), EXPIRE_DATA)
      .put(DELETE_ORPHAN_FILES.getCode(), DELETE_ORPHAN_FILES)
      .put(SYNC_HIVE_COMMIT.getCode(), SYNC_HIVE_COMMIT)
      .put(FULL_OPTIMIZING.getCode(), FULL_OPTIMIZING)
      .build();

  public static Action of(int code) {
    return Optional.ofNullable(ACTIONS.get(code))
        .orElseThrow(() -> new IllegalArgumentException("No action with code: " + code));
  }
}

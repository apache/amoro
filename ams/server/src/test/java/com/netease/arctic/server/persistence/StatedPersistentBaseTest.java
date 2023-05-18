package com.netease.arctic.server.persistence;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatedPersistentBaseTest {

  @Test
  public void testModifyStateFailed() {
    ChildStatedPersistentBase objectUnderTest = new ChildStatedPersistentBase();
    AtomicInteger state = new AtomicInteger(0);

    // Modify the state of the object safely
    objectUnderTest.modifyStateSafely(() -> {
      state.incrementAndGet();
      assertEquals(1, state.get(), "State should be modified safely within the lock");
      objectUnderTest.setIntField(42);
      objectUnderTest.setStringField("modified");
      assertEquals(42, objectUnderTest.getIntField(),
          "Int field should be modified safely within the lock");
      assertEquals("modified", objectUnderTest.getStringField(),
          "String field should be modified safely within the lock");
      throw new RuntimeException("Test exception");
    });

    // Verify that the state of the object was restored after the exception
    assertEquals(1, state.get(), "State should be restored after an exception");
    assertEquals(0, objectUnderTest.getIntField(),
        "Int field should be restored after an exception");
    assertEquals("initial", objectUnderTest.getStringField(),
        "String field should be restored after an exception");
  }

  @Test
  public void testModifyStateSuccess() {
    ChildStatedPersistentBase objectUnderTest = new ChildStatedPersistentBase();
    AtomicInteger state = new AtomicInteger(0);

    // Modify the state of the object safely
    objectUnderTest.modifyStateSafely(() -> {
      state.incrementAndGet();
      assertEquals(1, state.get(), "State should be modified safely within the lock");
      objectUnderTest.setIntField(42);
      objectUnderTest.setStringField("modified");
      assertEquals(42, objectUnderTest.getIntField(),
          "Int field should be modified safely within the lock");
      assertEquals("modified", objectUnderTest.getStringField(),
          "String field should be modified safely within the lock");
      throw new RuntimeException("Test exception");
    });

    // Verify that the state of the object was restored after the exception
    assertEquals(1, state.get(), "State should be restored after an exception");
    assertEquals(0, objectUnderTest.getIntField(), "Int field should be restored after an exception");
    assertEquals("initial", objectUnderTest.getStringField(), "String field should be restored after an exception");
  }

  static class ChildStatedPersistentBase extends StatedPersistentBase {
    private int intField = 0;
    private String stringField = "initial";

    public int getIntField() {
      return intField;
    }

    public void setIntField(int intField) {
      this.intField = intField;
    }

    public String getStringField() {
      return stringField;
    }

    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    public void modifyState() {

    }
  }
}
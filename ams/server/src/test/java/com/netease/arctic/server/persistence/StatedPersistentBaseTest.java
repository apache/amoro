package com.netease.arctic.server.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatedPersistentProxyTest {

  private MyPersistent myPersistent;
  private MyPersistent myPersistentProxy;

  @BeforeEach
  void setUp() {
    myPersistent = new MyPersistent();
    myPersistentProxy = StatedPersistentProxy.get(MyPersistent.class, myPersistent);
  }

  @Test
  void testInvokeWithoutStateConsistency() {
    myPersistentProxy.setCounter(1);
    assertEquals(1, myPersistentProxy.getCounter());
  }

  @Test
  void testInvokeWithStateConsistency() {
    myPersistentProxy.setCounter(1);
    assertThrows(RuntimeException.class, () -> myPersistentProxy.incrementAndThrow());
    assertEquals(1, myPersistentProxy.getCounter());
  }

  @Test
  void testInvokeWithStateConsistencyAndDifferentFieldTypes() {
    myPersistentProxy.setCounter(1);
    myPersistentProxy.setStateFieldString("Initial");
    myPersistentProxy.setNonStateFieldString("Initial");

    assertThrows(RuntimeException.class, () -> myPersistentProxy.incrementAndThrow());

    assertEquals(1, myPersistentProxy.getCounter());
    assertEquals("Initial", myPersistentProxy.getStateFieldString());
    assertEquals("Changed", myPersistentProxy.getNonStateFieldString());
  }

  static class MyPersistent extends PersistentBase {

    @StatedPersistentProxy.StateField
    private int counter;

    @StatedPersistentProxy.StateField
    private String stateFieldString;

    private String nonStateFieldString;

    public int getCounter() {
      return counter;
    }

    public void setCounter(int counter) {
      this.counter = counter;
    }

    public String getStateFieldString() {
      return stateFieldString;
    }

    public void setStateFieldString(String stateFieldString) {
      this.stateFieldString = stateFieldString;
    }

    public String getNonStateFieldString() {
      return nonStateFieldString;
    }

    public void setNonStateFieldString(String nonStateFieldString) {
      this.nonStateFieldString = nonStateFieldString;
    }

    @StatedPersistentProxy.StateConsistency
    public void incrementAndThrow() {
      counter++;
      stateFieldString = "Changed";
      nonStateFieldString = "Changed";
      throw new RuntimeException("Exception occurred");
    }
  }
}
/**
 * Autogenerated by Thrift Compiler (0.20.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.amoro.api;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.20.0)", date = "2024-05-30")
public enum CommitMetaProducer implements org.apache.thrift.TEnum {
  OPTIMIZE(0),
  INGESTION(1),
  DATA_EXPIRATION(2);

  private final int value;

  private CommitMetaProducer(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  @Override
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static CommitMetaProducer findByValue(int value) { 
    switch (value) {
      case 0:
        return OPTIMIZE;
      case 1:
        return INGESTION;
      case 2:
        return DATA_EXPIRATION;
      default:
        return null;
    }
  }
}

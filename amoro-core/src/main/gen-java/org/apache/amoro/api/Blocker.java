/**
 * Autogenerated by Thrift Compiler (0.20.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.amoro.api;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.20.0)", date = "2024-08-02")
public class Blocker implements org.apache.amoro.shade.thrift.org.apache.thrift.TBase<Blocker, Blocker._Fields>, java.io.Serializable, Cloneable, Comparable<Blocker> {
  private static final org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TStruct("Blocker");

  private static final org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField BLOCKER_ID_FIELD_DESC = new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField("blockerId", org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField OPERATIONS_FIELD_DESC = new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField("operations", org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField("properties", org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.MAP, (short)3);

  private static final org.apache.amoro.shade.thrift.org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new BlockerStandardSchemeFactory();
  private static final org.apache.amoro.shade.thrift.org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new BlockerTupleSchemeFactory();

  public @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String blockerId; // required
  public @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.util.List<BlockableOperation> operations; // required
  public @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> properties; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.amoro.shade.thrift.org.apache.thrift.TFieldIdEnum {
    BLOCKER_ID((short)1, "blockerId"),
    OPERATIONS((short)2, "operations"),
    PROPERTIES((short)3, "properties");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // BLOCKER_ID
          return BLOCKER_ID;
        case 2: // OPERATIONS
          return OPERATIONS;
        case 3: // PROPERTIES
          return PROPERTIES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    @Override
    public short getThriftFieldId() {
      return _thriftId;
    }

    @Override
    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final java.util.Map<_Fields, org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.BLOCKER_ID, new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData("blockerId", org.apache.amoro.shade.thrift.org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldValueMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OPERATIONS, new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData("operations", org.apache.amoro.shade.thrift.org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.ListMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.LIST, 
            new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.EnumMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.ENUM, BlockableOperation.class))));
    tmpMap.put(_Fields.PROPERTIES, new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData("properties", org.apache.amoro.shade.thrift.org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.MapMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.MAP, 
            new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldValueMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING), 
            new org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldValueMetaData(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.amoro.shade.thrift.org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Blocker.class, metaDataMap);
  }

  public Blocker() {
  }

  public Blocker(
    java.lang.String blockerId,
    java.util.List<BlockableOperation> operations,
    java.util.Map<java.lang.String,java.lang.String> properties)
  {
    this();
    this.blockerId = blockerId;
    this.operations = operations;
    this.properties = properties;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Blocker(Blocker other) {
    if (other.isSetBlockerId()) {
      this.blockerId = other.blockerId;
    }
    if (other.isSetOperations()) {
      java.util.List<BlockableOperation> __this__operations = new java.util.ArrayList<BlockableOperation>(other.operations.size());
      for (BlockableOperation other_element : other.operations) {
        __this__operations.add(other_element);
      }
      this.operations = __this__operations;
    }
    if (other.isSetProperties()) {
      java.util.Map<java.lang.String,java.lang.String> __this__properties = new java.util.HashMap<java.lang.String,java.lang.String>(other.properties);
      this.properties = __this__properties;
    }
  }

  @Override
  public Blocker deepCopy() {
    return new Blocker(this);
  }

  @Override
  public void clear() {
    this.blockerId = null;
    this.operations = null;
    this.properties = null;
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  public java.lang.String getBlockerId() {
    return this.blockerId;
  }

  public Blocker setBlockerId(@org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String blockerId) {
    this.blockerId = blockerId;
    return this;
  }

  public void unsetBlockerId() {
    this.blockerId = null;
  }

  /** Returns true if field blockerId is set (has been assigned a value) and false otherwise */
  public boolean isSetBlockerId() {
    return this.blockerId != null;
  }

  public void setBlockerIdIsSet(boolean value) {
    if (!value) {
      this.blockerId = null;
    }
  }

  public int getOperationsSize() {
    return (this.operations == null) ? 0 : this.operations.size();
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  public java.util.Iterator<BlockableOperation> getOperationsIterator() {
    return (this.operations == null) ? null : this.operations.iterator();
  }

  public void addToOperations(BlockableOperation elem) {
    if (this.operations == null) {
      this.operations = new java.util.ArrayList<BlockableOperation>();
    }
    this.operations.add(elem);
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  public java.util.List<BlockableOperation> getOperations() {
    return this.operations;
  }

  public Blocker setOperations(@org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.util.List<BlockableOperation> operations) {
    this.operations = operations;
    return this;
  }

  public void unsetOperations() {
    this.operations = null;
  }

  /** Returns true if field operations is set (has been assigned a value) and false otherwise */
  public boolean isSetOperations() {
    return this.operations != null;
  }

  public void setOperationsIsSet(boolean value) {
    if (!value) {
      this.operations = null;
    }
  }

  public int getPropertiesSize() {
    return (this.properties == null) ? 0 : this.properties.size();
  }

  public void putToProperties(java.lang.String key, java.lang.String val) {
    if (this.properties == null) {
      this.properties = new java.util.HashMap<java.lang.String,java.lang.String>();
    }
    this.properties.put(key, val);
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  public java.util.Map<java.lang.String,java.lang.String> getProperties() {
    return this.properties;
  }

  public Blocker setProperties(@org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String,java.lang.String> properties) {
    this.properties = properties;
    return this;
  }

  public void unsetProperties() {
    this.properties = null;
  }

  /** Returns true if field properties is set (has been assigned a value) and false otherwise */
  public boolean isSetProperties() {
    return this.properties != null;
  }

  public void setPropertiesIsSet(boolean value) {
    if (!value) {
      this.properties = null;
    }
  }

  @Override
  public void setFieldValue(_Fields field, @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case BLOCKER_ID:
      if (value == null) {
        unsetBlockerId();
      } else {
        setBlockerId((java.lang.String)value);
      }
      break;

    case OPERATIONS:
      if (value == null) {
        unsetOperations();
      } else {
        setOperations((java.util.List<BlockableOperation>)value);
      }
      break;

    case PROPERTIES:
      if (value == null) {
        unsetProperties();
      } else {
        setProperties((java.util.Map<java.lang.String,java.lang.String>)value);
      }
      break;

    }
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  @Override
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case BLOCKER_ID:
      return getBlockerId();

    case OPERATIONS:
      return getOperations();

    case PROPERTIES:
      return getProperties();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  @Override
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case BLOCKER_ID:
      return isSetBlockerId();
    case OPERATIONS:
      return isSetOperations();
    case PROPERTIES:
      return isSetProperties();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that instanceof Blocker)
      return this.equals((Blocker)that);
    return false;
  }

  public boolean equals(Blocker that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_blockerId = true && this.isSetBlockerId();
    boolean that_present_blockerId = true && that.isSetBlockerId();
    if (this_present_blockerId || that_present_blockerId) {
      if (!(this_present_blockerId && that_present_blockerId))
        return false;
      if (!this.blockerId.equals(that.blockerId))
        return false;
    }

    boolean this_present_operations = true && this.isSetOperations();
    boolean that_present_operations = true && that.isSetOperations();
    if (this_present_operations || that_present_operations) {
      if (!(this_present_operations && that_present_operations))
        return false;
      if (!this.operations.equals(that.operations))
        return false;
    }

    boolean this_present_properties = true && this.isSetProperties();
    boolean that_present_properties = true && that.isSetProperties();
    if (this_present_properties || that_present_properties) {
      if (!(this_present_properties && that_present_properties))
        return false;
      if (!this.properties.equals(that.properties))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetBlockerId()) ? 131071 : 524287);
    if (isSetBlockerId())
      hashCode = hashCode * 8191 + blockerId.hashCode();

    hashCode = hashCode * 8191 + ((isSetOperations()) ? 131071 : 524287);
    if (isSetOperations())
      hashCode = hashCode * 8191 + operations.hashCode();

    hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
    if (isSetProperties())
      hashCode = hashCode * 8191 + properties.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(Blocker other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.compare(isSetBlockerId(), other.isSetBlockerId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBlockerId()) {
      lastComparison = org.apache.amoro.shade.thrift.org.apache.thrift.TBaseHelper.compareTo(this.blockerId, other.blockerId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetOperations(), other.isSetOperations());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOperations()) {
      lastComparison = org.apache.amoro.shade.thrift.org.apache.thrift.TBaseHelper.compareTo(this.operations, other.operations);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetProperties(), other.isSetProperties());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetProperties()) {
      lastComparison = org.apache.amoro.shade.thrift.org.apache.thrift.TBaseHelper.compareTo(this.properties, other.properties);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable
  @Override
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  @Override
  public void read(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol iprot) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  @Override
  public void write(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol oprot) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("Blocker(");
    boolean first = true;

    sb.append("blockerId:");
    if (this.blockerId == null) {
      sb.append("null");
    } else {
      sb.append(this.blockerId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("operations:");
    if (this.operations == null) {
      sb.append("null");
    } else {
      sb.append(this.operations);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("properties:");
    if (this.properties == null) {
      sb.append("null");
    } else {
      sb.append(this.properties);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TCompactProtocol(new org.apache.amoro.shade.thrift.org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.amoro.shade.thrift.org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TCompactProtocol(new org.apache.amoro.shade.thrift.org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.amoro.shade.thrift.org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class BlockerStandardSchemeFactory implements org.apache.amoro.shade.thrift.org.apache.thrift.scheme.SchemeFactory {
    @Override
    public BlockerStandardScheme getScheme() {
      return new BlockerStandardScheme();
    }
  }

  private static class BlockerStandardScheme extends org.apache.amoro.shade.thrift.org.apache.thrift.scheme.StandardScheme<Blocker> {

    @Override
    public void read(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol iprot, Blocker struct) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
      org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // BLOCKER_ID
            if (schemeField.type == org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING) {
              struct.blockerId = iprot.readString();
              struct.setBlockerIdIsSet(true);
            } else { 
              org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // OPERATIONS
            if (schemeField.type == org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TList _list118 = iprot.readListBegin();
                struct.operations = new java.util.ArrayList<BlockableOperation>(_list118.size);
                @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable BlockableOperation _elem119;
                for (int _i120 = 0; _i120 < _list118.size; ++_i120)
                {
                  _elem119 = org.apache.amoro.api.BlockableOperation.findByValue(iprot.readI32());
                  if (_elem119 != null)
                  {
                    struct.operations.add(_elem119);
                  }
                }
                iprot.readListEnd();
              }
              struct.setOperationsIsSet(true);
            } else { 
              org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PROPERTIES
            if (schemeField.type == org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TMap _map121 = iprot.readMapBegin();
                struct.properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map121.size);
                @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String _key122;
                @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String _val123;
                for (int _i124 = 0; _i124 < _map121.size; ++_i124)
                {
                  _key122 = iprot.readString();
                  _val123 = iprot.readString();
                  struct.properties.put(_key122, _val123);
                }
                iprot.readMapEnd();
              }
              struct.setPropertiesIsSet(true);
            } else { 
              org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    @Override
    public void write(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol oprot, Blocker struct) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.blockerId != null) {
        oprot.writeFieldBegin(BLOCKER_ID_FIELD_DESC);
        oprot.writeString(struct.blockerId);
        oprot.writeFieldEnd();
      }
      if (struct.operations != null) {
        oprot.writeFieldBegin(OPERATIONS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TList(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.I32, struct.operations.size()));
          for (BlockableOperation _iter125 : struct.operations)
          {
            oprot.writeI32(_iter125.getValue());
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.properties != null) {
        oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TMap(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING, org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING, struct.properties.size()));
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter126 : struct.properties.entrySet())
          {
            oprot.writeString(_iter126.getKey());
            oprot.writeString(_iter126.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class BlockerTupleSchemeFactory implements org.apache.amoro.shade.thrift.org.apache.thrift.scheme.SchemeFactory {
    @Override
    public BlockerTupleScheme getScheme() {
      return new BlockerTupleScheme();
    }
  }

  private static class BlockerTupleScheme extends org.apache.amoro.shade.thrift.org.apache.thrift.scheme.TupleScheme<Blocker> {

    @Override
    public void write(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol prot, Blocker struct) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
      org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetBlockerId()) {
        optionals.set(0);
      }
      if (struct.isSetOperations()) {
        optionals.set(1);
      }
      if (struct.isSetProperties()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetBlockerId()) {
        oprot.writeString(struct.blockerId);
      }
      if (struct.isSetOperations()) {
        {
          oprot.writeI32(struct.operations.size());
          for (BlockableOperation _iter127 : struct.operations)
          {
            oprot.writeI32(_iter127.getValue());
          }
        }
      }
      if (struct.isSetProperties()) {
        {
          oprot.writeI32(struct.properties.size());
          for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter128 : struct.properties.entrySet())
          {
            oprot.writeString(_iter128.getKey());
            oprot.writeString(_iter128.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol prot, Blocker struct) throws org.apache.amoro.shade.thrift.org.apache.thrift.TException {
      org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.blockerId = iprot.readString();
        struct.setBlockerIdIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TList _list129 = iprot.readListBegin(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.I32);
          struct.operations = new java.util.ArrayList<BlockableOperation>(_list129.size);
          @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable BlockableOperation _elem130;
          for (int _i131 = 0; _i131 < _list129.size; ++_i131)
          {
            _elem130 = org.apache.amoro.api.BlockableOperation.findByValue(iprot.readI32());
            if (_elem130 != null)
            {
              struct.operations.add(_elem130);
            }
          }
        }
        struct.setOperationsIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TMap _map132 = iprot.readMapBegin(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING, org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TType.STRING); 
          struct.properties = new java.util.HashMap<java.lang.String,java.lang.String>(2*_map132.size);
          @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String _key133;
          @org.apache.amoro.shade.thrift.org.apache.thrift.annotation.Nullable java.lang.String _val134;
          for (int _i135 = 0; _i135 < _map132.size; ++_i135)
          {
            _key133 = iprot.readString();
            _val134 = iprot.readString();
            struct.properties.put(_key133, _val134);
          }
        }
        struct.setPropertiesIsSet(true);
      }
    }
  }

  private static <S extends org.apache.amoro.shade.thrift.org.apache.thrift.scheme.IScheme> S scheme(org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.amoro.shade.thrift.org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


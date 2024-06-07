package crux.ast.types;

import java.lang.reflect.Array;

/**
 * This Type will Array data type base is the base of the array, could be int, bool, or char for
 * cruxlang This should implement the equivalent methods Two arrays are equivalent if their bases
 * are equivalent and have same extend
 */
public final class ArrayType extends Type implements java.io.Serializable {
  static final long serialVersionUID = 12022L;
  private final Type base;
  private final long extent;

  public ArrayType(long extent, Type base) {
    this.extent = extent;
    this.base = base;
  }

  public Type getBase() {
    return base;
  }

  public long getExtent() {
    return extent;
  }

  @Override
  public String toString() {
    return String.format("array[%d,%s]", extent, base);
  }

  @Override
  Type index(Type that) {
    if (that.equivalent(new IntType())) {
      return getBase();
    }
    return super.index(that);
  }

  public boolean equivalent(Type that) {
    if (this.toString().equals(that.toString()) &&
       ((ArrayType) that).getBase().toString().equals(this.getBase().toString()) &&
       (((ArrayType) that).getExtent() == this.getExtent()) ) {
      return true;
    }
    return false;
  }
}


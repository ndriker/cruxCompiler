package crux.ast.types;

/**
 * Types for Integers values This should implement the equivalent methods along with add,sub, mul,
 * div, and compare equivalent will check if the param is instance of IntType
 */
public final class IntType extends Type implements java.io.Serializable {
  static final long serialVersionUID = 12022L;

  @Override
  public String toString() {
    return "int";
  }

  @Override
  Type add(Type that) {
    if (equivalent(that)) {
      return new IntType();
    }
    return super.add(that);
  }

  @Override
  Type sub(Type that) {
    if (equivalent(that)) {
      return new IntType();
    }
    return super.sub(that);
  }

  @Override
  Type mul(Type that) {
    if (equivalent(that)) {
      return new IntType();
    }
    return super.mul(that);
  }

  @Override
  Type div(Type that) {
    if (equivalent(that)) {
      return new IntType();
    }
    return super.div(that);
  }

  @Override
  Type compare(Type that) {
    if (equivalent(that)) {
      return new BoolType();
    }
    return super.compare(that);
  }

  @Override
  Type assign(Type source) {
    if (equivalent(source)) {
      return new VoidType();
    }
    return super.assign(source);
  }

  public boolean equivalent(Type that) {
    if (this.toString().equals(that.toString())) {
      return true;
    }
    return false;
  }

}

package crux.ast.types;

/**
 * FuncType for functions args is a TypeList to create a type for each param and push it to the list
 * ret is the type of the function return type, could be int,bool,void Two functions are equivalent
 * if their args and ret are also equivalent This Class should implement Call method
 */
public final class FuncType extends Type implements java.io.Serializable {
  static final long serialVersionUID = 12022L;

  private TypeList args;
  private Type ret;

  public FuncType(TypeList args, Type returnType) {
    this.args = args;
    this.ret = returnType;
  }

  public Type getRet() {
    return ret;
  }

  public TypeList getArgs() {
    return args;
  }

  @Override
  Type call(Type args) {

    if (this.getArgs().equivalent(args)){
      return this.ret;
    }
    return super.call(args);
  }


  @Override
  public String toString() {
    return "func(" + args + "):" + ret;
  }

  public boolean equivalent(Type that) {
    // TODO not correct/done yet
    // ERROR IN STAGE 4 NOT STAGE 3
    if (that.toString().startsWith("func")) {
      if (this.getRet().equivalent(((FuncType) that).getRet())) {
        if (this.getArgs().equivalent(((FuncType) that).getArgs())) {
          return true;
        }
      }
    }
    return false;
  }
}

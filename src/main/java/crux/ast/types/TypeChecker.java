package crux.ast.types;

import crux.ast.Symbol;
import crux.ast.*;
import crux.ast.traversal.NullNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// This class will associate types with the AST nodes from Stage 2
public final class TypeChecker {
  private final ArrayList<String> errors = new ArrayList<>();

  public ArrayList<String> getErrors() {
    return errors;
  }

  public void check(DeclarationList ast) {
    var inferenceVisitor = new TypeInferenceVisitor();
    inferenceVisitor.visit(ast);
  }


  // Helper function, should be used to add error into the errors array
  private void addTypeError(Node n, String message) {
    errors.add(String.format("TypeError%s[%s]", n.getPosition(), message));
  }


  // Helper function, should be used to record Types if the Type is an ErrorType then it will call
  // addTypeError
  private void setNodeType(Node n, Type ty) {
    ((BaseNode) n).setType(ty);
    if (ty.getClass() == ErrorType.class) {
      var error = (ErrorType) ty;
      addTypeError(n, error.getMessage());
    }
  }


  // Helper to retrieve Type from the map
  public Type getType(Node n) {
    return ((BaseNode) n).getType();
  }



  // This calls will visit each AST node and try to resolve it's type with the help of the
  // symbolTable.
  private final class TypeInferenceVisitor extends NullNodeVisitor<Void> {
    private Symbol currentFunctionSymbol;
    private Type currentFunctionReturnType;

    private boolean lastStatementReturns;
    private boolean hasBreak;

    @Override
    public Void visit(VarAccess vaccess) {


      Type vType = vaccess.getSymbol().getType();
      setNodeType(vaccess, vType);
      return null;
    }

    @Override
    public Void visit(ArrayDeclaration arrayDeclaration) {


      boolean baseTypeInvalid = true;
      Type baseType = ( (ArrayType) arrayDeclaration.getSymbol().getType()).getBase();
      Type iType = new IntType();
      Type bType = new BoolType();

      if (baseType.equivalent(iType) ) {
        baseTypeInvalid = false;

      }
      if (baseType.equivalent(bType)) {
        baseTypeInvalid = false;
      }
      if (baseTypeInvalid) {
        addTypeError(arrayDeclaration, "Base type is not an Integer or a Boolean");

      }

      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(Assignment assignment) {
      assignment.getLocation().accept(this);
      assignment.getValue().accept(this);
      Type locType = getType(assignment.getLocation());
      Type valueType = getType(assignment.getValue());
      setNodeType(assignment, locType.assign(valueType));
      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(Break brk) {
      hasBreak = true;
      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(Call call) {

      TypeList argList = new TypeList();


      for (Expression i : call.getArguments()) {
        i.accept(this);
        argList.append(getType(i));
      }
      FuncType funcType = new FuncType(argList, call.getCallee().getType());

      setNodeType(call, funcType.call(argList));
      lastStatementReturns = false;


      return null;
    }

    @Override
    public Void visit(Continue cont) {
      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(DeclarationList declarationList) {
      for (Node i : declarationList.getChildren()) {
        i.accept(this);
      }
      return null;
    }

    @Override
    public Void visit(FunctionDefinition functionDefinition) {
      currentFunctionSymbol = functionDefinition.getSymbol();
      lastStatementReturns = false;

      int index = functionDefinition.getSymbol().getType().toString().indexOf(":") + 1;
      String typeName = functionDefinition.getSymbol().getType().toString().substring(index);
      switch(typeName) {
        case "void": currentFunctionReturnType = new VoidType(); break;
        case "bool": currentFunctionReturnType = new BoolType(); break;
        case "int" : currentFunctionReturnType = new IntType(); break;
      }
      if (currentFunctionSymbol.getName().equals("main") ) {
        if (functionDefinition.getParameters().size() != 0) {
          if (!currentFunctionReturnType.equivalent(new VoidType())){
            addTypeError(functionDefinition, "Function main() is incorrectly structured");

          }
        }
      }

      Type iType = new IntType();
      Type bType = new BoolType();
      Type vType = new VoidType();

      for (Symbol i : functionDefinition.getParameters()) {
        boolean hasInvalidType = true;
        if ( i.getType().equivalent(iType)) {
          hasInvalidType = false;
        }
        if (i.getType().equivalent(bType)) {
          hasInvalidType = false;

        }
        if (hasInvalidType) {
          addTypeError(functionDefinition, "Only Integers and Booleans are allowed for function parameters");
        }
      }
      functionDefinition.getStatements().accept(this);

      if (!currentFunctionReturnType.equivalent(vType)) {
        if (!lastStatementReturns){
          addTypeError(functionDefinition, "Non-void Functions must return");
        }
      }
      return null;
    }

    @Override
    public Void visit(IfElseBranch ifElseBranch) {

      ifElseBranch.getCondition().accept(this);
      Type condType = getType(ifElseBranch.getCondition());
      if (!condType.equivalent(new BoolType())) {
        addTypeError(ifElseBranch, "Condition must resolve to a Boolean");
      }
      boolean afterThen = false;
      lastStatementReturns = false;

      ifElseBranch.getThenBlock().accept(this);
      if (lastStatementReturns) {
        afterThen = true;
      }

      boolean afterElse = false;
      lastStatementReturns = false;

      if (ifElseBranch.getElseBlock() != null) {
        ifElseBranch.getElseBlock().accept(this);
        if (lastStatementReturns) {
          afterElse = true;
        }
      }

      lastStatementReturns = afterThen && afterElse;

      return null;
    }

    @Override
    public Void visit(ArrayAccess access) {

      Expression bracketOpExpr = access.getIndex();
      bracketOpExpr.accept(this);

      setNodeType(access, access.getBase().getType().index(getType(bracketOpExpr)));

      return null;
    }

    @Override
    public Void visit(LiteralBool literalBool) {
      setNodeType(literalBool, new BoolType());
      return null;
    }

    @Override
    public Void visit(LiteralInt literalInt) {
      setNodeType(literalInt, new IntType());
      return null;
    }

    @Override
    public Void visit(Loop loop) {
      boolean hasBreakPrevVal = hasBreak;
      loop.getBody().accept(this);
      if (!hasBreak) {
        lastStatementReturns = true;
      } else {
        lastStatementReturns = false;
      }
      hasBreak = hasBreakPrevVal;

      return null;
    }

    @Override
    public Void visit(OpExpr op) {
      Expression leftExpr = op.getLeft();
      Expression rightExpr = op.getRight();
      Type rightType = null;

      leftExpr.accept(this);
      Type leftType = getType(leftExpr);


      if (rightExpr != null) {
        rightExpr.accept(this);


        rightType = getType(rightExpr);

        boolean funcCheckRight = rightType.toString().startsWith("func(");
        if (funcCheckRight) {
          rightType = ((FuncType) rightType).getRet();
        }
      }
      boolean funcCheckLeft = leftType.toString().startsWith("func(");

      if (funcCheckLeft) {
        leftType = ((FuncType) leftType).getRet();
      }

      Type resultType;
      switch(op.getOp()) {
        case ADD: resultType = leftType.add(rightType); break;
        case SUB: resultType = leftType.sub(rightType); break;
        case MULT: resultType = leftType.mul(rightType); break;
        case DIV: resultType = leftType.div(rightType); break;
        case GE:
        case LE:
        case NE:
        case EQ:
        case GT:
        case LT: resultType = leftType.compare(rightType); break;
        case LOGIC_AND: resultType = leftType.and(rightType); break;
        case LOGIC_OR: resultType = leftType.or(rightType); break;
        case LOGIC_NOT: resultType = leftType.not(); break;
        default: resultType = leftType; break; // just silencing warning

      }

      setNodeType(op, resultType);
      return null;
    }

    @Override
    public Void visit(Return ret) {
      Expression retExpr = ret.getValue();

      retExpr.accept(this);
      if (!getType(retExpr).equivalent(currentFunctionReturnType)) {
        addTypeError(ret, "Return type does not match function return type");
      }
      lastStatementReturns = true;

      return null;
    }

    @Override
    public Void visit(StatementList statementList) {
      for (Node i: statementList.getChildren()) {


        if (lastStatementReturns) {
          addTypeError(statementList, "You have unreachable code!");
          break;
        }
        i.accept(this);
      }
      return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDeclaration) {
      boolean baseTypeInvalid = true;
      if (variableDeclaration.getSymbol().getType().equivalent(new IntType())) {
        baseTypeInvalid = false;
      }

      if (variableDeclaration.getSymbol().getType().equivalent(new BoolType())) {
        baseTypeInvalid = false;
      }

      if (baseTypeInvalid) {
        addTypeError(variableDeclaration, "Var Dec: Base type is not an Integer or a Boolean");
      }
      lastStatementReturns = false;

      return null;
    }
  }
}

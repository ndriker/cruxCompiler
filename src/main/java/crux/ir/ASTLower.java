package crux.ir;

import crux.ast.Symbol;
import crux.ast.*;
import crux.ast.OpExpr.Operation;
import crux.ast.traversal.NodeVisitor;
import crux.ast.types.*;
import crux.ir.insts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class InstPair {
  // InstPair takes a Value instead of just LocalVar, because it can store Constants and Vars
  // check types using instance of operator
  Instruction start;
  Instruction end;
  Value value;

  InstPair(Instruction startInstr, Instruction endInstr, Value val) {
    start = startInstr;
    end = endInstr;
    value = val;
  }

  InstPair(Instruction both, Value val) {
    start = both;
    end = both;
    value = val;
  }

  InstPair(Instruction startInstr, Instruction endInstr) {
    start = startInstr;
    end = endInstr;
    value = null;
  }

  InstPair(Instruction both) {
    // both is used for start and end
    start = both;
    end = both;
    value = null;
  }

  Instruction getStart() {
    return start;
  }

  Instruction getEnd() {
    return end;
  }

  Value getValue() {return value;}

}


/**
 * Convert AST to IR and build the CFG
 */
public final class ASTLower implements NodeVisitor<InstPair> {
  private Program mCurrentProgram;
  private Function mCurrentFunction;

  private Map<Symbol, Variable> mCurrentLocalVarMap;


  private NopInst globalBreak;
  private NopInst globalHead;

  /**
   * A constructor to initialize member variables
   */
  public ASTLower() {
    mCurrentProgram = null;
    mCurrentFunction = null;
    mCurrentLocalVarMap = null;
    globalBreak = new NopInst();
    globalHead = new NopInst();
  }

  public Program lower(DeclarationList ast) {
    visit(ast);
    return mCurrentProgram;
  }

  @Override
  public InstPair visit(DeclarationList declarationList) {
    mCurrentProgram = new Program();
    for (var dec: declarationList.getChildren()) {
      dec.accept(this);
    }
    System.out.println("END OF DECLARATION LIST");
    return new InstPair(new NopInst(), new NopInst());
  }

  /**
   * This visitor should create a Function instance for the functionDefinition node add parameters to
   * the localVarMap add the function to the program init the function start Instruction
   */
  @Override
  public InstPair visit(FunctionDefinition functionDefinition) {
    mCurrentFunction = new Function(functionDefinition.getSymbol().getName(), (FuncType) functionDefinition.getSymbol().getType());
    mCurrentLocalVarMap = new HashMap<Symbol, Variable>();
    List<LocalVar> lvList = new ArrayList<LocalVar>();
    for (var i : functionDefinition.getParameters() ){
      LocalVar lv = mCurrentFunction.getTempVar(i.getType());
      mCurrentLocalVarMap.put(i, lv);
      lvList.add(lv);
    }

    mCurrentFunction.setArguments(lvList);
    mCurrentProgram.addFunction(mCurrentFunction);
    StatementList functionBody = functionDefinition.getStatements();
    InstPair funcInstPair = functionBody.accept(this);

    mCurrentFunction.setStart(funcInstPair.getStart());
    mCurrentFunction = null;
    mCurrentLocalVarMap = null;
    return new InstPair(new NopInst());
  }

  @Override
  public InstPair visit(StatementList statementList) {
    // create a nop
    NopInst nopInst = new NopInst();
    Instruction currentInstruction = nopInst;
    for (var statement : statementList.getChildren()) {
      InstPair sPair = statement.accept(this);
      currentInstruction.setNext(0, sPair.getStart());
      currentInstruction = sPair.getEnd();
    }

    InstPair finalInstPair = new InstPair(nopInst, currentInstruction);
    return finalInstPair;
  }

  /**
   * Declarations, could be either local or Global
   */
  @Override
  public InstPair visit(VariableDeclaration variableDeclaration) {
    Symbol varSymbol = variableDeclaration.getSymbol();

    if (mCurrentFunction == null) {
      // this is a global variable
      GlobalDecl gd = new GlobalDecl(varSymbol, IntegerConstant.get(mCurrentProgram, 1));
      mCurrentProgram.addGlobalVar(gd);
    } else {
      // this is a local variable
      LocalVar lv = mCurrentFunction.getTempVar(varSymbol.getType());
      mCurrentLocalVarMap.put(varSymbol, lv);
    }
    return new InstPair(new NopInst());
  }

  /**
   * Create a declaration for array and connected it to the CFG
   */
  @Override
  public InstPair visit(ArrayDeclaration arrayDeclaration) {
    Symbol varSymbol = arrayDeclaration.getSymbol();
    long arrLength = ((ArrayType) varSymbol.getType()).getExtent();
    GlobalDecl gd = new GlobalDecl(varSymbol, IntegerConstant.get(mCurrentProgram, arrLength));
    mCurrentProgram.addGlobalVar(gd);
    return new InstPair(new NopInst());
  }

  /**
   * LookUp the name in the map(s) Throw an exception if the name not found
   */
  @Override
  public InstPair visit(VarAccess name) {

    if (mCurrentLocalVarMap.containsKey(name.getSymbol())) {
      return new InstPair(new NopInst(), mCurrentLocalVarMap.get(name.getSymbol()));
    } else {
      AddressVar addressVar = mCurrentFunction.getTempAddressVar(name.getSymbol().getType());
      AddressAt addressAt = new AddressAt(addressVar, name.getSymbol());
      InstPair addressPair = new InstPair(addressAt, addressVar);
      return addressPair;
    }
  }


  /**
   * Check if the Location is LocalVar to copy the value or if the location is addressVar to
   * store the value
   */
  @Override
  public InstPair visit(Assignment assignment) {
    InstPair lhs = assignment.getLocation().accept(this);
    InstPair rhs = assignment.getValue().accept(this);
    Instruction resultInstruction;

//    Local = Local
//    Local = Global
//    Global = Local
//    Global = Global
    lhs.getEnd().setNext(0, rhs.getStart());

    if (lhs.getValue() instanceof LocalVar && rhs.getValue() instanceof LocalVar) {

      LocalVar leftLocal = (LocalVar) lhs.getValue();
      LocalVar rightLocal = (LocalVar) rhs.getValue();
      CopyInst copyInst = new CopyInst(leftLocal, rightLocal);
      rhs.getEnd().setNext(0, copyInst);
      resultInstruction = copyInst;

    } else if (lhs.getValue() instanceof LocalVar && rhs.getValue() instanceof AddressVar) {

      LocalVar leftLocal = (LocalVar) lhs.getValue();
      LocalVar rightLocal = mCurrentFunction.getTempVar( ( (AddressVar) rhs.getValue()).getType());
      LoadInst loadInst = new LoadInst(rightLocal, (AddressVar) rhs.getValue());
      CopyInst copyInst = new CopyInst(leftLocal, rightLocal);
      rhs.getEnd().setNext(0, loadInst);
      loadInst.setNext(0, copyInst);
      resultInstruction = copyInst;

    } else if (lhs.getValue() instanceof AddressVar && rhs.getValue() instanceof LocalVar) {

      AddressVar addressVar = (AddressVar) lhs.getValue();
      LocalVar localVar = (LocalVar) rhs.getValue();
      StoreInst storeInst = new StoreInst(localVar, addressVar);
      rhs.getEnd().setNext(0, storeInst);
      resultInstruction = storeInst;

    } else { // both are address vars

      AddressVar addressVar = (AddressVar) lhs.getValue();
      LocalVar rightLocal = mCurrentFunction.getTempVar( rhs.getValue().getType());
      LoadInst loadInst = new LoadInst(rightLocal, (AddressVar) rhs.getValue());
      StoreInst storeInst = new StoreInst(rightLocal, addressVar);

      rhs.getEnd().setNext(0, loadInst);
      loadInst.setNext(0, storeInst);
      resultInstruction = storeInst;
    }

    InstPair resInstPair = new InstPair(lhs.getStart(), resultInstruction);
    return resInstPair;
  }

  /**
   * Create a callInst
   */
  @Override
  public InstPair visit(Call call) {
    List<LocalVar> vals = new ArrayList<LocalVar>();
    List<Expression> argList = call.getArguments();

    Instruction topInst = new NopInst();
    Instruction nextInstruction = new NopInst();
    if (argList.size() > 0) {
      int index = 0;
      for (var arg : argList) {
        Instruction condInst = new NopInst();
        InstPair argInstPair = arg.accept(this);

        if (index == 0) {
          topInst.setNext(0, argInstPair.getStart());
        } else {
          nextInstruction.setNext(0, argInstPair.getStart());
        }

        if (argInstPair.getValue() instanceof LocalVar) {
          vals.add((LocalVar) argInstPair.getValue());
          NopInst nop = new NopInst();
          argInstPair.getEnd().setNext(0, nop);
          condInst = nop;
        } else if (argInstPair.getValue() instanceof Constant) {
          LocalVar localConstant = mCurrentFunction.getTempVar(argInstPair.getValue().getType());
          CopyInst copyInst = new CopyInst(localConstant, argInstPair.getValue());
          argInstPair.getEnd().setNext(0, copyInst);
          condInst = copyInst;
          vals.add(localConstant);
        } else {
          // address var
          LocalVar localAddressVar = mCurrentFunction.getTempVar(argInstPair.getValue().getType());
          LoadInst loadInst = new LoadInst(localAddressVar, (AddressVar) argInstPair.getValue());
          argInstPair.getEnd().setNext(0, loadInst);
          condInst = loadInst;
          vals.add(localAddressVar);
        }
        nextInstruction = condInst;
        index++;
      }
    } else {
      topInst.setNext(0, nextInstruction);
    }

    CallInst callInstruction;
    InstPair finalPair;

    if (!call.getCallee().getType().toString().endsWith("void")) {
      // non void func
      LocalVar returnTempVar = mCurrentFunction.getTempVar(call.getCallee().getType());

      callInstruction = new CallInst(returnTempVar, call.getCallee(), vals);
      nextInstruction.setNext(0, callInstruction);
      finalPair = new InstPair(topInst, callInstruction, returnTempVar);

    } else {
      // void func
      callInstruction = new CallInst(call.getCallee(), vals);
      nextInstruction.setNext(0, callInstruction);
      finalPair = new InstPair(topInst, callInstruction);

    }

    return finalPair;

  }

  /**
   * to Handle Operations like Arithmetics and Comparisons Also to handle logical operations (and,
   * or, not)
   */

  public InstPair arithmeticHelper(OpExpr operation) {
    BinaryOperator bOp;

    Expression lhs = operation.getLeft();
    Expression rhs = operation.getRight();
    InstPair lhsPair = lhs.accept(this);
    InstPair rhsPair = rhs.accept(this);


    LocalVar lhsValue = mCurrentFunction.getTempVar(lhsPair.getValue().getType());
    LocalVar rhsValue = mCurrentFunction.getTempVar(rhsPair.getValue().getType());

    Instruction lhsInst = new NopInst();
    if (lhsPair.getValue() instanceof LocalVar) {
      lhsValue = (LocalVar) lhsPair.getValue();
    } else if (lhsPair.getValue() instanceof Constant) {
      CopyInst copyInst = new CopyInst(lhsValue, lhsPair.getValue());
      lhsInst = copyInst;
    } else if (lhsPair.getValue() instanceof AddressVar) {
      LoadInst loadInst = new LoadInst(lhsValue, (AddressVar) lhsPair.getValue());
      lhsInst = loadInst;
    }
    lhsPair.getEnd().setNext(0, lhsInst);
    lhsInst.setNext(0, rhsPair.getStart());

    Instruction rhsInst = new NopInst();
    if (rhsPair.getValue() instanceof LocalVar) {
      rhsValue = (LocalVar) rhsPair.getValue();
    } else if (rhsPair.getValue() instanceof Constant) {
      CopyInst copyInst = new CopyInst(rhsValue, rhsPair.getValue());
      rhsInst = copyInst;
    } else if (rhsPair.getValue() instanceof AddressVar) {
      LoadInst loadInst = new LoadInst(rhsValue, (AddressVar) rhsPair.getValue());
      rhsInst = loadInst;
    }

    rhsPair.getEnd().setNext(0, rhsInst);

    LocalVar res = mCurrentFunction.getTempVar(new IntType());

    switch(operation.getOp()) {
      case ADD:
        bOp = new BinaryOperator(BinaryOperator.Op.Add, res, lhsValue, rhsValue); break;
      case SUB:
        bOp = new BinaryOperator(BinaryOperator.Op.Sub, res, lhsValue, rhsValue); break;
      case MULT:
        bOp = new BinaryOperator(BinaryOperator.Op.Mul, res, lhsValue, rhsValue); break;
      case DIV:
        bOp = new BinaryOperator(BinaryOperator.Op.Div, res, lhsValue, rhsValue); break;
      default:
        bOp = null; break; // silencing warning
    }

    rhsInst.setNext(0, bOp);
    InstPair finalInstPair = new InstPair(lhsPair.getStart(), bOp, res);
    return finalInstPair;

  }
  public InstPair compareHelper(OpExpr operation) {
    CompareInst cInst;

    Expression lhs = operation.getLeft();
    Expression rhs = operation.getRight();
    InstPair lhsPair = lhs.accept(this);
    InstPair rhsPair = rhs.accept(this);

    LocalVar lhsValue = mCurrentFunction.getTempVar(lhsPair.getValue().getType());
    LocalVar rhsValue = mCurrentFunction.getTempVar(rhsPair.getValue().getType());

    Instruction lhsInst = new NopInst();
    if (lhsPair.getValue() instanceof LocalVar) {
      lhsValue = (LocalVar) lhsPair.getValue();
    } else if (lhsPair.getValue() instanceof Constant) {
      CopyInst copyInst = new CopyInst(lhsValue, lhsPair.getValue());
      lhsInst = copyInst;
    } else if (lhsPair.getValue() instanceof AddressVar) {
      LoadInst loadInst = new LoadInst(lhsValue, (AddressVar) lhsPair.getValue());
      lhsInst = loadInst;
    }
    lhsPair.getEnd().setNext(0, lhsInst);
    lhsInst.setNext(0, rhsPair.getStart());

    Instruction rhsInst = new NopInst();
    if (rhsPair.getValue() instanceof LocalVar) {
      rhsValue = (LocalVar) rhsPair.getValue();
    } else if (rhsPair.getValue() instanceof Constant) {
      CopyInst copyInst = new CopyInst(rhsValue, rhsPair.getValue());
      rhsInst = copyInst;
    } else if (rhsPair.getValue() instanceof AddressVar) {
      LoadInst loadInst = new LoadInst(rhsValue, (AddressVar) rhsPair.getValue());
      rhsInst = loadInst;
    }

    rhsPair.getEnd().setNext(0, rhsInst);

    LocalVar res = mCurrentFunction.getTempVar(new IntType());
    switch(operation.getOp()) {
      case GE:
        cInst = new CompareInst(res, CompareInst.Predicate.GE, lhsValue, rhsValue); break;
      case GT:
        cInst = new CompareInst(res, CompareInst.Predicate.GT, lhsValue, rhsValue); break;
      case LE:
        cInst = new CompareInst(res, CompareInst.Predicate.LE, lhsValue, rhsValue); break;
      case LT:
        cInst = new CompareInst(res, CompareInst.Predicate.LT, lhsValue, rhsValue); break;
      case EQ:
        cInst = new CompareInst(res, CompareInst.Predicate.EQ, lhsValue, rhsValue); break;
      case NE:
        cInst = new CompareInst(res, CompareInst.Predicate.NE, lhsValue, rhsValue); break;
      default:
        cInst = null; break; // silencing warning
    }
    rhsInst.setNext(0, cInst);
    InstPair finalInstPair = new InstPair(lhsPair.getStart(), cInst, res);
    return finalInstPair;
  }

  public InstPair notHelper(OpExpr operation) {
    Expression lhs = operation.getLeft();
    InstPair lhsPair = lhs.accept(this);
    LocalVar notRes = mCurrentFunction.getTempVar(new BoolType());

    Instruction convertInst = new NopInst();
    if (lhsPair.getValue() instanceof BooleanConstant) {
      CopyInst copyInst = new CopyInst(notRes, lhsPair.getValue());
      convertInst = copyInst;
    } else if (lhsPair.getValue() instanceof AddressVar) {
      LoadInst loadInst = new LoadInst(notRes, (AddressVar) lhsPair.getValue());
      convertInst = loadInst;
    } else {
      // local var
      notRes = (LocalVar) lhsPair.getValue();
    }

    lhsPair.getEnd().setNext(0, convertInst);
    UnaryNotInst notInst = new UnaryNotInst(notRes, (LocalVar) lhsPair.getValue());
    convertInst.setNext(0, notInst);
    InstPair finalInstPair = new InstPair(lhsPair.getStart(), notInst, notRes);
    return finalInstPair;
  }

  public InstPair orHelper(OpExpr operation) {
    Expression lhs = operation.getLeft();
    Expression rhs = operation.getRight();
    InstPair lhsPair = lhs.accept(this);
    InstPair rhsPair = rhs.accept(this);

    LocalVar predicate = (LocalVar) lhsPair.getValue();
    JumpInst jInst = new JumpInst(predicate);
    lhsPair.getEnd().setNext(0, jInst);
    LocalVar destVar = mCurrentFunction.getTempVar(new BoolType());
    BooleanConstant bc = BooleanConstant.get(mCurrentProgram, true);

    CopyInst trueCInst = new CopyInst(destVar, bc);
    jInst.setNext(1, trueCInst);
    jInst.setNext(0, rhsPair.getStart());

    CopyInst falseCInst = new CopyInst(destVar, rhsPair.getValue());
    rhsPair.getEnd().setNext(0, falseCInst);
    NopInst nopInst = new NopInst();
    falseCInst.setNext(0, nopInst);
    trueCInst.setNext(0, nopInst);

    InstPair finalInstPair = new InstPair(lhsPair.getStart(), nopInst, destVar);
    return finalInstPair;
  }

  public InstPair andHelper(OpExpr operation) {
    Expression lhs = operation.getLeft();
    Expression rhs = operation.getRight();
    InstPair lhsPair = lhs.accept(this);
    InstPair rhsPair = rhs.accept(this);

    LocalVar predicate = (LocalVar) lhsPair.getValue();
    JumpInst jInst = new JumpInst(predicate);
    lhsPair.getEnd().setNext(0, jInst);
    LocalVar destVar = mCurrentFunction.getTempVar(new BoolType());
    BooleanConstant bc = BooleanConstant.get(mCurrentProgram, false);
    CopyInst falseCInst = new CopyInst(destVar, bc);

    jInst.setNext(0, falseCInst);

    jInst.setNext(1, rhsPair.getStart());
    CopyInst trueCInst = new CopyInst(destVar, rhsPair.getValue());
    rhsPair.getEnd().setNext(0, trueCInst);

    NopInst nopInst = new NopInst();

    falseCInst.setNext(0, nopInst);
    trueCInst.setNext(0, nopInst);

    InstPair finalInstPair = new InstPair(lhsPair.getStart(), nopInst, destVar);
    return finalInstPair;

  }
  @Override
  public InstPair visit(OpExpr operation) {
    InstPair resInstPair;
    switch(operation.getOp()) {
      case ADD:
      case SUB:
      case MULT:
      case DIV:
        resInstPair = arithmeticHelper(operation); break;
      case GE:
      case GT:
      case LE:
      case LT:
      case EQ:
      case NE:
        resInstPair = compareHelper(operation); break;
      case LOGIC_NOT:
        resInstPair = notHelper(operation); break;
      case LOGIC_OR:
        resInstPair = orHelper(operation); break;
      case LOGIC_AND:
        resInstPair = andHelper(operation); break;
      default:
        resInstPair = new InstPair(new NopInst()); break; // silencing warning
    }
    return resInstPair;
  }

  /**
   * It should reach the array base, and build the offset expression
   */
  @Override
  public InstPair visit(ArrayAccess access) {
    //  public AddressAt(Variable destVar, Symbol base, LocalVar offset) {

    Expression arrayExpression = access.getIndex();
    InstPair indexInstPair = arrayExpression.accept(this);
    AddressVar destAddressVar = mCurrentFunction.getTempAddressVar( ( (ArrayType) access.getBase().getType() ).getBase() );

    LocalVar offset = mCurrentFunction.getTempVar(new IntType());
    Instruction resultInstruction;

    if (indexInstPair.getValue() instanceof AddressVar) {

      LoadInst loadInst = new LoadInst(offset, (AddressVar) indexInstPair.getValue());
      resultInstruction = loadInst;

    } else if (indexInstPair.getValue() instanceof IntegerConstant) {

      CopyInst copyInst = new CopyInst(offset, indexInstPair.getValue());
      resultInstruction = copyInst;

    } else {

      // local var
      CopyInst copyInst = new CopyInst(offset, indexInstPair.getValue());
      resultInstruction = copyInst;

    }

    AddressAt addressAt = new AddressAt(destAddressVar, access.getBase(), offset);
    indexInstPair.getEnd().setNext(0, resultInstruction);
    resultInstruction.setNext(0, addressAt);

    InstPair addressInstPair = new InstPair(indexInstPair.getStart(), addressAt, destAddressVar);
    return addressInstPair;

  }

  /**
   * Copy the literal into a tempVar
   */
  @Override
  public InstPair visit(LiteralBool literalBool) {
    BooleanConstant bc = BooleanConstant.get(mCurrentProgram, literalBool.getValue());
    LocalVar lv = mCurrentFunction.getTempVar(new BoolType());
    NopInst startInst = new NopInst();
    CopyInst copied = new CopyInst(lv, bc);
    startInst.setNext(0, copied);
    NopInst endInst = new NopInst();
    copied.setNext(0, endInst);
    InstPair bcInstPair = new InstPair(startInst, endInst, lv);
    return bcInstPair;
  }

  /**
   * Copy the literal into a tempVar
   */
  @Override
  public InstPair visit(LiteralInt literalInt) {
    IntegerConstant ic = IntegerConstant.get(mCurrentProgram, literalInt.getValue());
    LocalVar lv = mCurrentFunction.getTempVar(new IntType());
    NopInst startInst = new NopInst();
    CopyInst copied = new CopyInst(lv, ic);
    startInst.setNext(0, copied);
    NopInst endInst = new NopInst();
    copied.setNext(0, endInst);
    InstPair icInstPair = new InstPair(startInst, endInst, lv);
    return icInstPair;
  }

  /**
   * create a ReturnInst
   */
  @Override
  public InstPair visit(Return ret) {
    Expression retExpr = ret.getValue();
    InstPair retPair = retExpr.accept(this);
    ReturnInst returnInst = new ReturnInst((LocalVar) retPair.getValue());
    retPair.getEnd().setNext(0, returnInst);
    InstPair finalInstPair = new InstPair(retPair.getStart(), returnInst);
    return finalInstPair;
  }

  /**
   * Break Node
   */
  @Override
  public InstPair visit(Break brk) {

    NopInst dummy = new NopInst();
    InstPair finalInstPair = new InstPair(globalBreak, dummy);
    return finalInstPair;
  }

  /**
   * Continue Node
   */
  @Override
  public InstPair visit(Continue cnt) {

    NopInst dummy = new NopInst();
    InstPair finalInstPair = new InstPair(globalHead, dummy);
    return finalInstPair;
  }

  /**
   *  Implement If Then Else statements.
   */
  @Override
  public InstPair visit(IfElseBranch ifElseBranch) {
    Expression cond = ifElseBranch.getCondition();
    InstPair condPair = cond.accept(this);

    JumpInst jInst = new JumpInst((LocalVar) condPair.getValue());
    condPair.getEnd().setNext(0, jInst);
    StatementList thenStatementList = ifElseBranch.getThenBlock();
    StatementList elseStatementList = ifElseBranch.getElseBlock();

    InstPair thenInstPair = thenStatementList.accept(this);
    InstPair elseInstPair = elseStatementList.accept(this);

    jInst.setNext(0, elseInstPair.getStart());
    jInst.setNext(1, thenInstPair.getStart());

    NopInst nopInst = new NopInst();

    thenInstPair.getEnd().setNext(0, nopInst);
    elseInstPair.getEnd().setNext(0, nopInst);

    InstPair finalInstPair = new InstPair(condPair.getStart(), nopInst);
    System.out.println("END OF IF");
    return finalInstPair;
  }

  /**
   *  Implement loops.
   */
  @Override
  public InstPair visit (Loop loop) {
    NopInst localBreak = new NopInst();
    NopInst localHead = new NopInst();

    NopInst previousBreak = globalBreak;
    NopInst previousHead = globalHead;

    globalBreak = localBreak;
    globalHead = localHead;

    InstPair bodyInstPair = loop.getBody().accept(this);

    localHead.setNext(0, bodyInstPair.getStart());
    bodyInstPair.getEnd().setNext(0, bodyInstPair.getStart());

    globalBreak = previousBreak;
    globalHead = previousHead;


    return new InstPair(localHead, localBreak);

  }
}

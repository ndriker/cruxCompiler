package crux.backend;

import crux.ast.Symbol;
import crux.ast.types.ArrayType;
import crux.ast.types.FuncType;
import crux.ast.types.VoidType;
import crux.ir.*;
import crux.ir.insts.*;
import crux.printing.IRValueFormatter;

import java.util.*;

/**
 * Convert the CFG into Assembly Instructions
 */
public final class CodeGen extends InstVisitor {
  private final Program p;
  private final CodePrinter out;
  private final Set<Instruction> visitedInsts;
  private HashMap<Instruction, String> labelMap;
  private HashMap<Variable, Integer> varIndexMap;
  private int varIndex;


  public CodeGen(Program p) {
    this.p = p;
    // Do not change the file name that is outputted or it will
    // break the grader!
    visitedInsts = new HashSet<Instruction>();
    labelMap = new HashMap<Instruction, String>();
    varIndexMap = new HashMap<Variable, Integer>();
    varIndex = 0;

    out = new CodePrinter("a.s");
  }



  public int checkVarMap(Variable v) {
    // takes variable as input, returns offset (its position relative to rbp on the stack)
    // check if variable is in varIndexMap
    //  if it is:
    //    it is already on the stack
    //    copy it to a temporary variable
    //  if it is not:
    //    it is not on the stack
    //    place the variable into the varIndexMap
    //    add the variable to the stack
    if (varIndexMap.containsKey(v)) {
      int index = varIndexMap.get(v);
      return -8*index;
      // out.bufferCode("movq " + (-8*index) + "(%rbp), %r10");
    } else {
      varIndex += 1;
      varIndexMap.put(v, varIndex);
      return -8*varIndex;
    }
  }


  /**
   * It should allocate space for globals call genCode for each Function
   */
  public void genCode() {
    //TODO
    for (Iterator<GlobalDecl> glob_it = p.getGlobals(); glob_it.hasNext();) {
      GlobalDecl g = glob_it.next();
      Symbol gSymbol = g.getSymbol();
      String name = gSymbol.getName();
      IntegerConstant numElements = (IntegerConstant) g.getNumElement();
      long size = numElements.getValue() * 8;
      out.printCode(".comm " + name + ", " + size + ", 8");
    }
    int count[] = new int[1];
    for (Iterator<Function> func_it = p.getFunctions(); func_it.hasNext();) {
      Function f = func_it.next();
      genCode(f, count);
    }

    out.close();
  }

  // traverse the graph
  // buffer the code from the graph
  // figure out the number of slots
  // print the prologue with the number of slots
  // flush and print the body code


//  public void genCodeTraversalUtil(Instruction i, HashSet<Instruction> visited) {
//    visited.add(i);
//    Instruction zeroInst = i.getNext(0);
//    if (zeroInst != null) {
//      zeroInst.accept(this);
//      if (!visited.contains(zeroInst)) {
//        genCodeTraversalUtil(zeroInst, visited);
//      }
//    }
//    Instruction oneInst = i.getNext(1);
//    if (oneInst != null) {
//      oneInst.accept(this);
//      if (!visited.contains(oneInst)) {
//        genCodeTraversalUtil(oneInst, visited);
//      }
//    }
//  }
//
//  public void genCodeTraversal(Instruction i) {
//    // DFS traversal of the CFG
//    HashSet<Instruction> visited = new HashSet<Instruction>();
//    genCodeTraversalUtil(i, visited);
//  }

  public void genCode(Function f, int[] count) {
    labelMap = f.assignLabels(count); // unsure about setting it to labelmap
    out.printCode(".globl " + f.getName());
    out.printLabel(f.getName() + ":");

    List<LocalVar> args = f.getArguments();
    int numArgs = args.size();
    String[] argRegs = {"rdi", "rsi", "rdx", "rcx", "r8", "r9"}; // this should be made into a global variable cuz will be used later on
    for (int i = 0; i < numArgs; i++) {
      // add arguments to the varIndexMap
      LocalVar currentArg = args.get(i);
      int offset = checkVarMap(currentArg);
      int n = i + 1;
      // first instr should be movq %rdi, -8(%rbp)
      if (i <= 5) {
        out.bufferCode("movq %" + argRegs[i] + ", " + offset + "(%rbp)"); // arg1
      } else {
        // more than 6 args, handle args 7-n
        out.bufferCode("movq " + (8*n - 40) + "(%rbp), %r10");
        out.bufferCode("movq %r10, " + offset + "(%rbp)");
      }
    }
    System.out.println(f.getStart());
    f.getStart().accept(this);
    int size = varIndexMap.size();// + numArgs;
    if (size % 2 != 0) {
      size += 1;
    }
    out.printCode("enter $(8*" + size + "), $0");
    out.outputBuffer();
    varIndexMap.clear();
  }


  public void labelChecker(Instruction i) {
    if (labelMap.containsKey(i)) {
      String label = labelMap.get(i);
      out.bufferLabel(label + ":");
    }
    visitedInsts.add(i);
  }

  public void asmGenHelper(Instruction i) {
    // if instruction is key in label map, print label
    // printing should occur at the start of every visit method



//    if (visitedInsts.contains(i)) {
//      // if the instruction has already been visited, jmp
//      // to its label instead
//      String labelToJmpTo = labelMap.get(i);
//      //String asmInst = String.format("jmp %s", labelToJmpTo);
//      out.bufferCode("jmp " + labelToJmpTo); // unconditional jump at the end of the helper
//
//    }


    // i.getNext(1) when i is jump
    // if i is jmp inst, i.getNext(1) is starting inst of then block
    // something like the if below
    // start of then block is in labelmap
    // je inst points to start of then block, jmp goes to the merge block
    // if the inst is visited, it is merge block


//    if (i instanceof JumpInst) { // something else might need to go here (not CallInst)
//      // if the instruction needs a label, buffer the label first
//      String labelToJmpTo = labelMap.get(i);
//      System.out.println("labelToJmpTo " + labelToJmpTo);
//
//      out.bufferLabel(labelToJmpTo);
//    }

//    if ( ( i.getNext(0) != null) &&
//         (!visitedInsts.contains(i.getNext(0)))) {
//
//    } else {
//
//    }
    if (i.getNext(0) != null) {
      if (visitedInsts.contains(i.getNext(0))) {
        String label = labelMap.get(i.getNext(0));
        out.bufferCode("jmp " + label);
        return;
      }
      i.getNext(0).accept(this);
    }
    else {
      // otherwise buffer leave and ret
      out.bufferCode("leave");
      out.bufferCode("ret");
    }
    if (i.getNext(1) != null) {
      if (visitedInsts.contains(i.getNext(1))) {
        String label = labelMap.get(i.getNext(1));
        out.bufferCode("jmp " + label);
        return;
      }
      i.getNext(1).accept(this);
    }


  }



  public void visit(AddressAt i) {
    // does AddressAt have any variables that need to go onto the stack?
    // move value global var address to temp register
    // copy temp register address to dest var
    labelChecker(i);
    AddressVar destVar = i.getDst();
    int destOffset = checkVarMap(destVar);
    Symbol base = i.getBase();
    if (i.getOffset() == null) {
      out.bufferCode("movq " + base.getName() + "@GOTPCREL(%rip), %r11");
      out.bufferCode("movq %r11, " + destOffset + "(%rbp)");

    } else {
      LocalVar offsetVar = i.getOffset();
      int offset = checkVarMap(offsetVar);

      out.bufferCode("movq " + base.getName() + "@GOTPCREL(%rip), %r11");
      out.bufferCode("movq " + offset + "(%rbp), %r10");
      out.bufferCode("imulq $8, %r10");
      out.bufferCode("addq %r10, %r11");
      out.bufferCode("movq %r11, " + destOffset + "(%rbp)");

    }
    asmGenHelper(i);

  }

  public void visit(BinaryOperator i) {

    // will we ever have to do a copy here? Or do we just check the varIndexMap
    labelChecker(i);
    LocalVar left = i.getLeftOperand();
    LocalVar right = i.getRightOperand();
    LocalVar dest = i.getDst();

    int leftOffset = checkVarMap(left);
    int rightOffset = checkVarMap(right);
    int destOffset = checkVarMap(dest);

    if (i.getOperator() == BinaryOperator.Op.Add) {
      out.bufferCode("movq " + leftOffset + "(%rbp), %r10");
      out.bufferCode("addq " + rightOffset + "(%rbp), %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (i.getOperator() == BinaryOperator.Op.Sub) {
      out.bufferCode("movq " + leftOffset + "(%rbp), %r10");
      out.bufferCode("subq " + rightOffset + "(%rbp), %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (i.getOperator() == BinaryOperator.Op.Mul) {
      out.bufferCode("movq " + leftOffset + "(%rbp), %r10");
      out.bufferCode("imulq " + rightOffset + "(%rbp), %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else {
      // division
      out.bufferCode("movq " + leftOffset + "(%rbp), %rax");
      out.bufferCode("cqto");
      out.bufferCode("idivq " + rightOffset + "(%rbp)");
      out.bufferCode("movq %rax, " + destOffset + "(%rbp)");
    }
    asmGenHelper(i);

  }


  public void visit(CompareInst i) {
    labelChecker(i);

    // then is getNext(
    // else is getNext(

    // jump to label of the instruction from i.getNext(1)
//    Instruction nextInst = i.getNext(1);
//    String nextLabel = labelMap.get(nextInst);

    LocalVar left = i.getLeftOperand();
    LocalVar right = i.getRightOperand();
    LocalVar destVar = i.getDst();

    int leftOffset = checkVarMap(left);
    int rightOffset = checkVarMap(right);
    int destOffset = checkVarMap(destVar);

    out.bufferCode("movq $0, %r10");
    out.bufferCode("movq $1, %rax");
    out.bufferCode("movq " + leftOffset + "(%rbp), %r11");
    out.bufferCode("cmp " + rightOffset + "(%rbp), %r11");


    CompareInst.Predicate cmpOp = i.getPredicate();

    // after comparing call conditional movq
    if (cmpOp == CompareInst.Predicate.EQ) {
      out.bufferCode("cmove %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (cmpOp == CompareInst.Predicate.NE) {
      out.bufferCode("cmovne %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (cmpOp == CompareInst.Predicate.GT) {
      out.bufferCode("cmovg %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (cmpOp == CompareInst.Predicate.GE) {
      out.bufferCode("cmovge %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (cmpOp == CompareInst.Predicate.LT) {
      out.bufferCode("cmovl %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    } else if (cmpOp == CompareInst.Predicate.LE) {
      out.bufferCode("cmovle %rax, %r10");
      out.bufferCode("movq %r10, " + destOffset + "(%rbp)");
    }
  asmGenHelper(i);

  }

  public void visit(CopyInst i) {
    labelChecker(i);

    // example for x = 5
    // movq $5, %r10
    // movq %r10, -16(%rbp)

    LocalVar destVar = i.getDstVar();
    Value srcVal = i.getSrcValue();

    int offset = checkVarMap(destVar);

    if (srcVal instanceof IntegerConstant) {
      long intConstVal = ((IntegerConstant) srcVal).getValue();
      out.bufferCode("movq $" + intConstVal + ", %r10");
      out.bufferCode("movq %r10, " + offset + "(%rbp)");

    } else if (srcVal instanceof BooleanConstant) {
      if (((BooleanConstant) srcVal).getValue()) {
        // srcVal is true
        out.bufferCode("movq $1, %r10");
        out.bufferCode("movq %r10, " + offset + "(%rbp)");

      } else {
        // srcVal is false
        out.bufferCode("movq $0, %r10");
        out.bufferCode("movq %r10, " + offset + "(%rbp)");
      }
    } else {
      // srcVal instanceof LocalVar
      int lvOffset = checkVarMap((LocalVar) srcVal); // we always know that this srcVal is on the stack
      out.bufferCode("movq " + lvOffset + "(%rbp), %r10");
      out.bufferCode("movq %r10, " + offset + "(%rbp)");
    }
    asmGenHelper(i);

  }

  public void visit(JumpInst i) {
    labelChecker(i);
    LocalVar predicateVar = i.getPredicate();
    int predicateOffset = checkVarMap(predicateVar);
    String label = labelMap.get(i.getNext(1));
    out.bufferCode("movq " + predicateOffset + "(%rbp), %r10");
    out.bufferCode("cmp $1, %r10");
    out.bufferCode("je " + label);
    asmGenHelper(i);

  }



  public void visit(LoadInst i) {
    labelChecker(i);

    AddressVar srcAddress = i.getSrcAddress();
    LocalVar destVar = i.getDst();

    int srcOffset = checkVarMap(srcAddress);
    int destOffset = checkVarMap(destVar);

    out.bufferCode("movq " + srcOffset + "(%rbp), %r10");
    out.bufferCode("movq 0(%r10), %r11");
    out.bufferCode("movq %r11, " + destOffset + "(%rbp)");
    asmGenHelper(i);

  }

  public void visit(NopInst i) {
    labelChecker(i);
    asmGenHelper(i);
  }

  public void visit(StoreInst i) {
    labelChecker(i);

    // src is a local var
    // dest is an address var
    LocalVar srcVar = i.getSrcValue();
    AddressVar destAddress = i.getDestAddress();

    int srcOffset = checkVarMap(srcVar);
    int destOffset = checkVarMap(destAddress);

    out.bufferCode("movq " + srcOffset + "(%rbp), %r10");
    out.bufferCode("movq " + destOffset + "(%rbp), %r11"); // now r11 stores the destination address
    out.bufferCode("movq %r10, 0(%r11)"); // check this
    asmGenHelper(i);

  }

  public void visit(ReturnInst i) {
    labelChecker(i);

    LocalVar retValue = i.getReturnValue();
    if (retValue != null) {
      int retOffset = checkVarMap(retValue);
      out.bufferCode("movq " + retOffset + "(%rbp), %rax");
    }
    out.bufferCode("leave");
    out.bufferCode("ret");
    asmGenHelper(i);

  }

  public void visit(CallInst i) {
    labelChecker(i);

    String[] argRegs = {"rdi", "rsi", "rdx", "rcx", "r8", "r9"}; // this should be made into a global variable cuz will be used later on


    List<Value> params = i.getParams();
    //checkVarMap((Variable) params.get(0));
    int numParams = params.size();
    for (int j = 0; j < numParams; j++) {
      Value currentParam = params.get(j);
      LocalVar var = (LocalVar) currentParam;
      int offset = checkVarMap(var);

      if (j <= 5) {
        out.bufferCode("movq " + offset + "(%rbp), %" + argRegs[j]);
      } else {
        int x = 8*(j - 6);
        out.bufferCode("movq " + offset + "(%rbp), " + "%r10");
        out.bufferCode("movq %r10, " + x + "(%rsp)");
      }
    }


    FuncType callee = (FuncType) i.getCallee().getType();
    String calleeName = i.getCallee().getName();
    out.bufferCode("call " + calleeName);
    if (! (callee.getRet() instanceof VoidType)) {
      LocalVar destVar = i.getDst();
      int destOffset = checkVarMap(destVar);
      out.bufferCode("movq %rax, " + destOffset + "(%rbp)");
    }
    asmGenHelper(i);

  }

  public void visit(UnaryNotInst i) {
    labelChecker(i);

    LocalVar inner = i.getInner();
    LocalVar dest = i.getDst();
    int innerOffset = checkVarMap(inner);
    int destOffset = checkVarMap(dest);

    out.bufferCode("movq " + innerOffset + "(%rbp), %r10");
    out.bufferCode("movq $1, %r11");
    out.bufferCode("subq %r10, %r11");
    out.bufferCode("movq %r11, " + destOffset + "(%rbp)");
    asmGenHelper(i);
  }
}

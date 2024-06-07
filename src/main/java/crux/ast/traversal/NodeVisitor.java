package crux.ast.traversal;

import crux.ast.*;

public interface NodeVisitor<T> {
  T visit(ArrayAccess arrayAccess);

  T visit(ArrayDeclaration arrayDeclaration);

  T visit(Assignment assignment);

  T visit(Break brk);

  T visit(Call call);

  T visit(Continue cnt);

  T visit(DeclarationList declarationList);

  T visit(FunctionDefinition functionDefinition);

  T visit(IfElseBranch ifElseBranch);

  T visit(LiteralBool literalBool);

  T visit(LiteralInt literalInt);

  T visit(OpExpr operation);

  T visit(Return ret);

  T visit(StatementList statementList);

  T visit(VarAccess vaccess);

  T visit(VariableDeclaration variableDeclaration);

  T visit(Loop loop);
}

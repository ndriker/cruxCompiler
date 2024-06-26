package crux.ast;

import crux.ast.*;
import crux.ast.OpExpr.Operation;
import crux.pt.CruxBaseVisitor;
import crux.pt.CruxParser;
import crux.pt.CruxParser.ArrayDeclarationContext;
import crux.pt.CruxParser.DeclarationContext;
import crux.pt.CruxParser.Op0Context;
import crux.pt.CruxParser.Op1Context;
import crux.pt.CruxParser.Op2Context;
import crux.ast.types.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.spi.CurrencyNameProvider;
import java.util.stream.Collectors;

/**
 * This class will convert the parse tree generated by ANTLR to AST It follows the visitor pattern
 * where declarations will be by DeclarationVisitor Class Statements will be resolved by
 * StatementVisitor Class Expressions will be resolved by ExpressionVisitor Class
 */

public final class ParseTreeLower {
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor();
    private final StatementVisitor statementVisitor = new StatementVisitor();
    private final ExpressionVisitor expressionVisitor = new ExpressionVisitor();

    private final SymbolTable symTab;

    public ParseTreeLower(PrintStream err) {
        symTab = new SymbolTable(err);
    }

    private static Position makePosition(ParserRuleContext ctx) {
        var start = ctx.start;
        return new Position(start.getLine());
    }

    // return True if any errors
    public boolean hasEncounteredError() {
        return symTab.hasEncounteredError();
    }

    /**
     * Lower top-level parse tree to AST
     *
     * @return a {@link DeclarationList} object representing the top-level AST.
     */

    public DeclarationList lower(CruxParser.ProgramContext program) {

        int i = 0;
        List<Declaration> decList = new ArrayList<Declaration>();
        while (program.declarationList().declaration(i) != null) {
            Declaration currentDec = program.declarationList().declaration(i).accept(declarationVisitor);
            decList.add(currentDec);
            i = i + 1;

        }
        DeclarationList declarationList = new DeclarationList(makePosition(program), decList);
        return declarationList;

    }

    // Lower statement list by lower individual statement into AST
    private StatementList lower(CruxParser.StatementListContext statementList) {
        List<Statement> statements = new ArrayList<Statement>();
        int i = 0;
        while (statementList.statement(i) != null) {
            Statement currentStatement = statementList.statement(i).accept(statementVisitor);
            statements.add(currentStatement);
            i = i + 1;
        }

        StatementList stateList = new StatementList(makePosition(statementList), statements);
        return stateList;
    }



    // Similar to the lower() function above, but handles symbol tables as well
    // The StatementBlock implementation is used for scoping within IF/ELSE block
    // The StatementList implementation is used for scoping with function definitions
    // (need the param list to be added into same scope as func body)
    private StatementList lower(CruxParser.StatementBlockContext statementBlock) {
        List<Statement> statements = new ArrayList<Statement>();
        int i = 0;
        symTab.enter();
        while (statementBlock.statementList().statement(i) != null) {
            Statement currentStatement = statementBlock.statementList().statement(i).accept(statementVisitor);
            statements.add(currentStatement);
            i = i + 1;
        }
        symTab.exit();
        StatementList stateList = new StatementList(makePosition(statementBlock), statements);
        return stateList;
    }

    private Type extractType(String ctxString) {
        if (ctxString.startsWith("bool")) {
            return new BoolType();
        } else if (ctxString.startsWith("int")) {
            return new IntType();
        } else if (ctxString.startsWith("void")){
            return new VoidType();
        } else {
            return null;
        }
    }

    // a parse tree visitor to create AST nodes derives from Declaration
    private final class DeclarationVisitor extends CruxBaseVisitor<Declaration> {

        // visit a parse tree variable declaration and create an AST
        @Override
        public VariableDeclaration visitVariableDeclaration(CruxParser.VariableDeclarationContext ctx) {
            // TODO get type of variable
            Position pos = makePosition(ctx);

            Symbol newSymbol = symTab.add(pos, ctx.IDENTIFIER().getText(), extractType(ctx.getText()));


            return new VariableDeclaration(pos, newSymbol);
        }


        // visit a parse tree array declaration and create an AST
        @Override
        public Declaration visitArrayDeclaration(CruxParser.ArrayDeclarationContext ctx) {

            Position pos = makePosition(ctx);
            int leftInd = ctx.getText().indexOf('[') + 1;
            int rightInd = ctx.getText().indexOf(']');
            long extent = Long.parseLong(ctx.getText().substring(leftInd, rightInd));
            System.out.println("GET TEXT " + ctx.getText());
            System.out.println("EXTRACT TYPE " + extractType(ctx.getText()));
            ArrayType arrType = new ArrayType(extent, extractType(ctx.getText()));

            Symbol newSymbol = symTab.add(pos, ctx.IDENTIFIER().getText(), arrType);
            System.out.println("SYMBOL " + newSymbol);
            return new ArrayDeclaration(pos, newSymbol);

        }


        // visit a parse tree function definition and create an AST
        @Override
        public Declaration visitFunctionDefinition(CruxParser.FunctionDefinitionContext ctx) {
            Position pos = makePosition(ctx);
            Type returnType = extractType(ctx.getText());


            int j = 0;
            List<Symbol> paramList = new ArrayList<Symbol>();
            TypeList argList = new TypeList();
            symTab.enter();

            while (ctx.parameterList().parameter(j) != null) {
                String currentSymbol = ctx.parameterList().parameter(j).getText();
                Type symType;
                String symName;
                Symbol newSym;
                if (currentSymbol.startsWith("int")) {
                    symType = new IntType();
                    symName = currentSymbol.substring(3);

                } else if (currentSymbol.startsWith("bool")) {
                    symType = new BoolType();
                    symName = currentSymbol.substring(4);

                } else {
                    symType = new VoidType();
                    symName = currentSymbol;
                }
                newSym = symTab.add(pos, symName, symType);//new Symbol(symName, symType);
                argList.append(symType);
                paramList.add(newSym);

                j = j + 1;
            }

            FuncType fType = new FuncType(argList, returnType);
            Symbol fSymbol = symTab.add(pos, ctx.IDENTIFIER().getText(), fType);
            symTab.enter();
            for (Symbol i : paramList) {
                symTab.add(pos, i.getName(), i.getType());
            }
            StatementList statementList = lower(ctx.statementBlock().statementList());
            symTab.exit();


            return new FunctionDefinition(pos, fSymbol, paramList, statementList);
        }

    }

    // a parse tree visitor to create AST nodes from Statement
    private final class StatementVisitor extends CruxBaseVisitor<Statement> {

        // visit a parse tree variable declaration and create an AST
        // variable declaration is both a Declaration and a Statement
        @Override
        public Statement visitVariableDeclaration(CruxParser.VariableDeclarationContext ctx) {
            Position pos = makePosition(ctx);
            Symbol newSymbol = symTab.add(pos, ctx.IDENTIFIER().getText(), extractType(ctx.getText()));
            return new VariableDeclaration(pos, newSymbol);
        }


        // visit a parse tree assignment statement and create an AST
        @Override
        public Statement visitAssignmentStatement(CruxParser.AssignmentStatementContext ctx) {
            return new Assignment(makePosition(ctx), ctx.designator().accept(expressionVisitor), ctx.expression0().accept(expressionVisitor));
        }


        // visit a parse tree call statement and create an AST
        // call is both an Expression and a Statement
        @Override
        public Statement visitCallStatement(CruxParser.CallStatementContext ctx) {
            Position pos = makePosition(ctx);
            Symbol callee;

            callee = symTab.lookup(pos, ctx.callExpression().IDENTIFIER().getText());
            List<Expression> exprList = new ArrayList<Expression>();
            for (int i = 0; i < ctx.callExpression().expressionList().expression0().size(); i++) {
                Expression newExpr = ctx.callExpression().expressionList().expression0(i).accept(expressionVisitor);
                exprList.add(newExpr);
            }
            return new Call(pos, callee, exprList);
        }



        // visit a parse tree if-else branch and create an AST
        @Override
        public Statement visitIfStatement(CruxParser.IfStatementContext ctx) {

            Position pos = makePosition(ctx);
            if (ctx.ELSE() == null) {

                StatementList ifList = lower(ctx.statementBlock(0));
                List<Statement> elseStatements = new ArrayList<Statement>();
                StatementList elseList = new StatementList(pos, elseStatements);
                return new IfElseBranch(pos, ctx.expression0().accept(expressionVisitor), ifList, elseList);

            }

            StatementList ifList = lower(ctx.statementBlock(0));
            StatementList elseList = lower(ctx.statementBlock(1));

            return new IfElseBranch(pos, ctx.expression0().accept(expressionVisitor), ifList, elseList);

        }


        // visit a parse tree while loop and create an AST
        @Override
        public Statement visitLoopStatement(CruxParser.LoopStatementContext ctx) {
            List<Statement> stateList = new ArrayList<Statement>();
            StatementList statementList = lower(ctx.statementBlock());
            return new Loop(makePosition(ctx), statementList);
        }


        // visit a parse tree return statement and create an AST
        @Override
        public Statement visitReturnStatement(CruxParser.ReturnStatementContext ctx) {
            return new Return(makePosition(ctx), ctx.expression0().accept(expressionVisitor));
        }

        // create a break node
        @Override
        public Statement visitBreakStatement(CruxParser.BreakStatementContext ctx) {
            return new Break(makePosition(ctx));
        }


        // create a continue node
        @Override
        public Statement visitContinueStatement(CruxParser.ContinueStatementContext ctx) {
            return new Continue(makePosition(ctx));
        }

    }

    private final class ExpressionVisitor extends CruxBaseVisitor<Expression> {

        // Parse Expression0 to OpExpr Node Parsing the expression should be exactly as described in the
        // grammar
        @Override
        public Expression visitExpression0(CruxParser.Expression0Context ctx) {

            if (ctx.op0() == null) {
                return ctx.expression1(0).accept(expressionVisitor);
            }

            Expression lhs = ctx.expression1(0).accept(expressionVisitor);
            Expression rhs = ctx.expression1(1).accept(expressionVisitor);
            Op0Context op0 = ctx.op0();
            Operation op;

            String opString = op0.getText();
            switch (opString) {
                case ">=":
                    op = Operation.GE;
                    break;
                case "<=":
                    op = Operation.LE;
                    break;
                case "!=":
                    op = Operation.NE;
                    break;
                case "==":
                    op = Operation.EQ;
                    break;
                case ">":
                    op = Operation.GT;
                    break;
                case "<":
                    op = Operation.LT;
                    break;
                default:
                    op = null; // never gets here, just silencing warning on return
            }

            return new OpExpr(makePosition(ctx), op, lhs, rhs);


        }


        // Parse Expression1 to OpExpr Node Parsing the expression should be exactly as described in the
        //grammar
        @Override
        public Expression visitExpression1(CruxParser.Expression1Context ctx) {
            if (ctx.op1() == null) {
                return ctx.expression2().accept(expressionVisitor);
            }

            Expression lhs = ctx.expression1().accept(expressionVisitor);
            Expression rhs = ctx.expression2().accept(expressionVisitor);
            Op1Context op1 = ctx.op1();
            Operation op;

            String opString = op1.getText();
            switch (opString) {
                case "+":
                    op = Operation.ADD;
                    break;
                case "-":
                    op = Operation.SUB;
                    break;
                case "||":
                    op = Operation.LOGIC_OR;
                    break;
                default:
                    op = null; // never gets here, just silencing warning on return

            }

            return new OpExpr(makePosition(ctx), op, lhs, rhs);
        }


        // Parse Expression2 to OpExpr Node Parsing the expression should be exactly as described in the
        // grammar
        @Override
        public Expression visitExpression2(CruxParser.Expression2Context ctx) {
            if (ctx.op2() == null) {
                return ctx.expression3().accept(expressionVisitor);
            }

            Expression lhs = ctx.expression2().accept(expressionVisitor);
            Expression rhs = ctx.expression3().accept(expressionVisitor);
            Op2Context op2 = ctx.op2();
            Operation op;

            String opString = op2.getText();
            switch (opString) {
                case "*":
                    op = Operation.MULT;
                    break;
                case "/":
                    op = Operation.DIV;
                    break;
                case "&&":
                    op = Operation.LOGIC_AND;
                    break;
                default:
                    op = null;
            }

            return new OpExpr(makePosition(ctx), op, lhs, rhs);
        }

        // parse expression3 to OpExpr node, parsing the expression should be exactly as described in the grammar
        @Override
        public Expression visitExpression3(CruxParser.Expression3Context ctx) {
            if (ctx.NOT() != null) {
                Expression rhs = ctx.expression3().accept(expressionVisitor);
                // put null on the rhs
                return new OpExpr(makePosition(ctx), Operation.LOGIC_NOT, rhs, null);
            }
            if ((ctx.OPEN_PAREN() != null) && (ctx.CLOSE_PAREN() != null)) {
                return ctx.expression0().accept(expressionVisitor);
            }
            if (ctx.designator() != null) {
                return ctx.designator().accept(expressionVisitor);
            }
            if (ctx.callExpression() != null) {
                return ctx.callExpression().accept(expressionVisitor);
            }

            return ctx.literal().accept(expressionVisitor);
        }


        // Create a Call Node
        @Override
        public Call visitCallExpression(CruxParser.CallExpressionContext ctx) {
            Position pos = makePosition(ctx);
            List<Expression> expressions = new ArrayList<Expression>();
            int i = 0;
            while (ctx.expressionList().expression0(i) != null) {
                Expression currentExpression = ctx.expressionList().expression0(i).accept(expressionVisitor);
                expressions.add(currentExpression);
                i = i + 1;
            }


            Symbol newSymbol = symTab.lookup(pos, ctx.IDENTIFIER().getText());

            return new Call(pos, newSymbol, expressions);
        }


        // visitDesignator will check for a name or ArrayAccess (it should account for the case when the designator was de-referenced)
        @Override
        public Expression visitDesignator(CruxParser.DesignatorContext ctx) {
            // return either var access or array access (depending on whether expression 0 is present or not)
            Position pos = makePosition(ctx);
            String name = ctx.IDENTIFIER().getText();
            Symbol symbol = symTab.lookup(pos, name);
            if (ctx.expression0() == null) {
                return new VarAccess(pos, symbol);
            } else {
                //Symbol base = new Symbol(symbol.getName(), ((ArrayType) symbol.getType()).getBase());
                //return new ArrayAccess(pos, base, ctx.expression0().accept(expressionVisitor));
                return new ArrayAccess(pos, symbol, ctx.expression0().accept(expressionVisitor));

            }
        }


        // create a literal node
        @Override
        public Expression visitLiteral(CruxParser.LiteralContext ctx) {
            Position pos = makePosition(ctx);
            String data = ctx.getText();
            switch (data) {
                case "true":
                    return new LiteralBool(pos, true);
                case "false":
                    return new LiteralBool(pos, false);
                default:
                    return new LiteralInt(pos, Long.parseLong(data));
            }
        }

    }
}

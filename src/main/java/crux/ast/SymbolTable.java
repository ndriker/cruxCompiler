package crux.ast;

import crux.ast.Position;
import crux.ast.types.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Symbol table will map each symbol from Crux source code to its declaration or appearance in the
 * source. the symbol table is made up of scopes, each scope is a map which maps an identifier to
 * it's symbol Scopes are inserted to the table Starting from the first scope (Global Scope). The
 * Global scope is the first scope in each Crux program and it contains all the built in functions
 * and names. The symbol table is an ArrayList of scops.
 */

final class SymbolTable {
  private final PrintStream err;
  private final ArrayList<Map<String, Symbol>> symbolScopes = new ArrayList<>();

  private boolean encounteredError = false;

  SymbolTable(PrintStream err) {
    this.err = err;
    symbolScopes.add(new HashMap<>());
    add(null, "readInt", new IntType());
    add(null, "readChar", new IntType());
    add(null, "printBool", new VoidType());
    add(null, "printInt", new VoidType());
    add(null, "printChar", new VoidType());
    add(null, "println", new VoidType());
  }

  boolean hasEncounteredError() {
    return encounteredError;
  }


  // Called to tell symbol table we entered a new scope.
  void enter() {
    // create new scope in symbol table
    symbolScopes.add(new HashMap<>());
  }


  // Called to tell symbol table we are exiting a scope.
  void exit() {
    // pop latest scope from symbol table
    symbolScopes.remove(symbolScopes.size() - 1);
  }


  // Insert a symbol to the table at the most recent scope. if the name already exists in the
  // current scope that's a declaration error.
   Symbol add(Position pos, String name, Type type) {
    if (symbolScopes.get(symbolScopes.size() - 1).containsKey(name) ) {
      encounteredError = true;
      err.printf("DeclarationSymbolError%s[Already exists %s.]%n", pos, name);
      return new Symbol(name, "DeclarationSymbolError");
    }

    Symbol newSymbol = new Symbol(name, type);
    symbolScopes.get(symbolScopes.size() - 1).put(name, newSymbol);
    return newSymbol;

  }



  // lookup a name in the SymbolTable, if the name not found in the table it should encounter an
  // error and return a symbol with ResolveSymbolError error. if the symbol is found then return it.
  Symbol lookup(Position pos, String name) {
    var symbol = find(name);
    if (symbol == null) {
      err.printf("ResolveSymbolError%s[Could not find %s.]%n", pos, name);
      encounteredError = true;
      return new Symbol(name, "ResolveSymbolError");
    } else {
      return symbol;
    }
  }

  // Try to find a symbol in the table starting from the most recent scope.
  private Symbol find(String name) {

    for (int i = symbolScopes.size() - 1 ; i >= 0; i--){
      Symbol symbol = symbolScopes.get(i).get(name);
      if (symbol != null) {
        return symbol;
      }
    }
    return null;
  }
}

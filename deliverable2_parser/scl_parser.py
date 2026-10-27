"""
scl_parser.py
CS 4308 – SCL Interpreter Project, Deliverable 2

Recursive-descent parser for our SCL subset.

The parser is initialised with a source file path.  It calls the scanner
internally, then walks the token stream according to grammar/scl_subset.txt.

Grammar extended for this deliverable:
    <statement>  ::= <set_stmt> | <display_stmt> | <input_stmt>
                   | <if_stmt>  | <exit_stmt>    | <return_stmt>
    <return_stmt> ::= "return" <expression>

Public API (three required functions):
    getNextToken()              – next non-comment token
    identifierExists(name)      – True if name was declared
    begin()                     – start the parse

Error format (from the team plan):
    Syntax error (line N): expected X, found Y
    
Usage:
    python scl_parser.py <source_file.scl>
"""

import sys
import os

# ---------------------------------------------------------------------------
# Path setup: allow imports from the scanner package sitting one level up
# ---------------------------------------------------------------------------
_HERE = os.path.dirname(os.path.abspath(__file__))
_SCANNER_DIR = os.path.join(_HERE, "..", "deliverable1_scanner")
if _SCANNER_DIR not in sys.path:
    sys.path.insert(0, _SCANNER_DIR)
 
import scl_scanner
from scl_scanner import scan_source, IdentifierTable
from scl_token import (
    KEYWORD, OPERATOR, IDENTIFIER, INTEGER_CONST, REAL_CONST, STRING, UNKNOWN,
    KEYWORDS, OPERATORS,
)

# ---------------------------------------------------------------------------
# Token-type constants (purely for readability)
# ---------------------------------------------------------------------------

_KW_IMPORT   = KEYWORDS["import"]           # 1
_KW_IMPL     = KEYWORDS["implementations"]  # 2
_KW_FUNCTION = KEYWORDS["function"]         # 3
_KW_MAIN     = KEYWORDS["main"]             # 4
_KW_RETURN   = KEYWORDS["return"]           # 5
_KW_TYPE     = KEYWORDS["type"]             # 6
_KW_IS       = KEYWORDS["is"]               # 7
_KW_VARS     = KEYWORDS["variables"]        # 8
_KW_BEGIN    = KEYWORDS["begin"]            # 9
_KW_ENDFUN   = KEYWORDS["endfun"]           # 10
_KW_DEFINE   = KEYWORDS["define"]           # 11
_KW_OF       = KEYWORDS["of"]               # 12
_KW_INTEGER  = KEYWORDS["integer"]          # 13
_KW_REAL     = KEYWORDS["real"]             # 14
_KW_SET      = KEYWORDS["set"]              # 15
_KW_DISPLAY  = KEYWORDS["display"]          # 16
_KW_INPUT    = KEYWORDS["input"]            # 17
_KW_EXIT     = KEYWORDS["exit"]             # 18
_KW_IF       = KEYWORDS["if"]               # 19
_KW_THEN     = KEYWORDS["then"]             # 20
_KW_ELSE     = KEYWORDS["else"]             # 21
_KW_ENDIF    = KEYWORDS["endif"]            # 22
 
_OP_ASSIGN = OPERATORS["="]   # 101
_OP_PLUS   = OPERATORS["+"]   # 102
_OP_MINUS  = OPERATORS["-"]   # 103
_OP_MUL    = OPERATORS["*"]   # 104
_OP_DIV    = OPERATORS["/"]   # 105
_OP_GT     = OPERATORS[">"]   # 106
_OP_LT     = OPERATORS["<"]   # 107
_OP_EQ     = OPERATORS["=="]  # 108
_OP_COMMA  = OPERATORS[","]   # 109
 
# Keywords that end a block (not consumed by _actions)
_TERMINATORS = {_KW_ENDFUN, _KW_ELSE, _KW_ENDIF}
 
# Keywords that can start a statement
_STATEMENT_STARTS = {_KW_SET, _KW_DISPLAY, _KW_INPUT,
                     _KW_RETURN, _KW_EXIT, _KW_IF}
 
# Safe places to resume parsing after a syntax error
_SYNC_POINTS = _STATEMENT_STARTS | _TERMINATORS
 
DEFAULT_OUTPUT = "ParserOutput.txt"
 
 
# ===========================================================================
# SCLParser
# ===========================================================================
class SCLParser:
    """Recursive-descent parser for the SCL subset.

    Initialise with the path to an SCL source file, then call begin().
    """

     def __init__(self, source_path, output_path=DEFAULT_OUTPUT):
        self._source_path = source_path
        self._output_path = output_path
 
        # Read source and run the scanner
        with open(source_path, "r", encoding="utf-8") as fh:
            source_text = fh.read()
 
        self._id_table = IdentifierTable()
        all_tokens = scan_source(source_text, self._id_table)
 
        self._tokens = [t for t in all_tokens if t.type != UNKNOWN]
        self._pos = 0
        self._last_line = self._tokens[-1].line if self._tokens else None
 
        self._declared = set()
 
        self._errors = []
        self._output = []   # everything printed: statements and errors

    # =======================================================================
    # PUBLIC INTERFACE  (three required functions)
    # =======================================================================

    def getNextToken(self):
        """Return the next token and advance, or None at end of input.
 
        Comments are removed by the scanner and UNKNOWN tokens are filtered
        out in __init__, so every token in the stream is a real token.
        """
        if self._pos < len(self._tokens):
            token = self._tokens[self._pos]
            self._pos += 1
            return token
        return None
 
    def identifierExists(self, identifier):
        """Return True if *identifier* was declared with 'define'."""
        return identifier in self._declared
 
    def begin(self):
        """Public entry point: initialise and start the parse.
 
        Returns the number of syntax errors found.
        """
        self._pos = 0
        self._declared.clear()
        self._errors.clear()
        self._output.clear()
        self._start()
        self._finish()
        return len(self._errors)

    # =======================================================================
    # INTERNAL HELPERS
    # =======================================================================

    def _current(self):
        """Peek at the current token without consuming it."""
        if self._pos < len(self._tokens):
            return self._tokens[self._pos]
        return None

    def _consume(self):
        """Consume and return the current token."""
        tok = self._tokens[self._pos]
        self._pos += 1
        return tok

    def _expect_keyword(self, kw_id: int) -> bool:
        """Consume the current token if it is keyword *kw_id*, else error."""
        tok = self._current()
        kw_name = _kw_name(kw_id)
        if tok and tok.type == KEYWORD and tok.id == kw_id:
            self._consume()
            return True
        got = _tok_desc(tok)
        self._parse_error(f"expected keyword '{kw_name}', got {got}",
                          tok.line if tok else None)
        return False

    def _expect_operator(self, op_id: int) -> bool:
        """Consume the current token if it is operator *op_id*, else error."""
        tok = self._current()
        op_sym = _op_sym(op_id)
        if tok and tok.type == OPERATOR and tok.id == op_id:
            self._consume()
            return True
        got = _tok_desc(tok)
        self._parse_error(f"expected operator '{op_sym}', got {got}",
                          tok.line if tok else None)
        return False

    def _expect_identifier(self):
        """Consume and return an IDENTIFIER token, or error and return None."""
        tok = self._current()
        if tok and tok.type == IDENTIFIER:
            self._consume()
            return tok
        got = _tok_desc(tok)
        self._parse_error(f"expected identifier, got {got}",
                          tok.line if tok else None)
        return None

    def _expect_string(self):
        """Consume and return a STRING token, or error and return None."""
        tok = self._current()
        if tok and tok.type == STRING:
            self._consume()
            return tok
        got = _tok_desc(tok)
        self._parse_error(f"expected string literal, got {got}",
                          tok.line if tok else None)
        return None

    def _parse_error(self, message: str, line=None):
        loc = f"line {line}: " if line else ""
        full = f"PARSE ERROR – {loc}{message}"
        self._errors.append(full)
        print(full)

    def _log(self, message: str):
        """Record and print a recognised-statement message."""
        self._output.append(message)
        print(message)

    def _finish(self):
        """Print the final parse summary."""
        print()
        if self._errors:
            print(f"Parse finished with {len(self._errors)} error(s).")
        else:
            print("Parse completed successfully – no errors.")

    # =======================================================================
    # STRUCTURAL PARSE METHODS  
    # =======================================================================

    def _start(self):
        """<program> ::= <import> 'implementations' <function>"""
        self._import_stmt()
        self._expect_keyword(_KW_IMPL)
        self._function_def()

    def _import_stmt(self):
        """<import> ::= 'import' STRING"""
        self._expect_keyword(_KW_IMPORT)
        tok = self._expect_string()
        if tok:
            self._log(f"Statement recognized: import \"{tok.value}\"")

    def _function_def(self):
        """<function> ::= 'function' 'main' 'return' 'type' <type>
                          'is' 'variables' <declarations>
                          'begin' <statements> 'endfun' 'main'"""
        self._expect_keyword(_KW_FUNCTION)
        self._expect_keyword(_KW_MAIN)
        self._expect_keyword(_KW_RETURN)
        self._expect_keyword(_KW_TYPE)
        self._type()
        self._expect_keyword(_KW_IS)
        self._expect_keyword(_KW_VARS)
        self._declarations()
        self._expect_keyword(_KW_BEGIN)
        self._actions()          # ← YOUR section starts here
        self._expect_keyword(_KW_ENDFUN)
        self._expect_keyword(_KW_MAIN)

    def _declarations(self):
        """<declarations> ::= <declaration> { <declaration> }"""
        self._declaration()
        while (self._current() and
               self._current().type == KEYWORD and
               self._current().id == _KW_DEFINE):
            self._declaration()

    def _declaration(self):
        """<declaration> ::= 'define' IDENTIFIER 'of' 'type' <type>"""
        self._expect_keyword(_KW_DEFINE)
        tok = self._expect_identifier()
        self._expect_keyword(_KW_OF)
        self._expect_keyword(_KW_TYPE)
        type_name = self._type()
        if tok:
            self._log(f"Statement recognized: define {tok.value} of type {type_name}")

    def _type(self) -> str:
        """<type> ::= 'integer' | 'real'"""
        tok = self._current()
        if tok and tok.type == KEYWORD and tok.id in (_KW_INTEGER, _KW_REAL):
            self._consume()
            return tok.value
        got = _tok_desc(tok)
        self._parse_error(f"expected type ('integer' or 'real'), got {got}",
                          tok.line if tok else None)
        return "unknown"

    # =======================================================================
    # actions() and all statement parsers
    # =======================================================================

    def _actions(self):
        """Read and parse statements until 'endfun', 'else', or 'endif'.

        <actions> ::= { <statement> }

        Stops (without consuming) when the next token is one of the
        block-terminator keywords: endfun, else, endif.
        """
        _TERMINATORS = {_KW_ENDFUN, _KW_ELSE, _KW_ENDIF}

        while True:
            tok = self._current()

            # End of token stream
            if tok is None:
                break

            # Block terminator – stop but do NOT consume
            if tok.type == KEYWORD and tok.id in _TERMINATORS:
                break

            # Dispatch on the leading keyword
            if tok.type == KEYWORD:
                if tok.id == _KW_SET:
                    self._set_stmt()
                elif tok.id == _KW_DISPLAY:
                    self._display_stmt()
                elif tok.id == _KW_INPUT:
                    self._input_stmt()
                elif tok.id == _KW_RETURN:
                    self._return_stmt()
                elif tok.id == _KW_EXIT:
                    self._exit_stmt()
                elif tok.id == _KW_IF:
                    self._if_stmt()
                else:
                    # Unexpected keyword inside an action block
                    self._parse_error(
                        f"unexpected keyword '{tok.value}' in action block",
                        tok.line,
                    )
                    self._consume()   # skip it and keep going
            else:
                # Non-keyword token with no leading statement keyword
                self._parse_error(
                    f"expected a statement keyword, got {_tok_desc(tok)}",
                    tok.line,
                )
                self._consume()

    # -----------------------------------------------------------------------
    # Individual statement parsers
    # -----------------------------------------------------------------------

    def _set_stmt(self):
        """<set_stmt> ::= 'set' IDENTIFIER '=' <expression>

        Verifies that the target variable was declared before use.
        """
        line = self._current().line
        self._expect_keyword(_KW_SET)

        name_tok = self._expect_identifier()
        if name_tok and not self.identifierExists(name_tok.value):
            self._parse_error(
                f"undeclared variable '{name_tok.value}'", name_tok.line
            )

        self._expect_operator(_OP_ASSIGN)
        val = self._expression()

        if name_tok:
            self._log(
                f"Statement recognized (line {line}): "
                f"set {name_tok.value} = {val}"
            )

    def _display_stmt(self):
        """<display_stmt> ::= 'display' <display_item> { ',' <display_item> }

        <display_item> ::= STRING | <expression>
        """
        line = self._current().line
        self._expect_keyword(_KW_DISPLAY)

        items = [self._display_item()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id == _OP_COMMA):
            self._consume()  # eat ','
            items.append(self._display_item())

        self._log(
            f"Statement recognized (line {line}): "
            f"display {', '.join(str(i) for i in items)}"
        )

    def _display_item(self) -> str:
        """Parse and return a display item (STRING or expression)."""
        tok = self._current()
        if tok and tok.type == STRING:
            self._consume()
            return f'"{tok.value}"'
        return str(self._expression())

    def _input_stmt(self):
        """<input_stmt> ::= 'input' STRING ',' IDENTIFIER

        The prompt string is displayed to the user and the value is stored
        in the named variable (which must have been declared).
        """
        line = self._current().line
        self._expect_keyword(_KW_INPUT)

        prompt_tok = self._expect_string()
        self._expect_operator(_OP_COMMA)
        name_tok = self._expect_identifier()

        if name_tok and not self.identifierExists(name_tok.value):
            self._parse_error(
                f"undeclared variable '{name_tok.value}'", name_tok.line
            )

        if prompt_tok and name_tok:
            self._log(
                f"Statement recognized (line {line}): "
                f"input \"{prompt_tok.value}\", {name_tok.value}"
            )

    def _return_stmt(self):
        """<return_stmt> ::= 'return' <expression>

        Returns a value from the current function.
        """
        line = self._current().line
        self._expect_keyword(_KW_RETURN)
        val = self._expression()
        self._log(
            f"Statement recognized (line {line}): return {val}"
        )

    def _exit_stmt(self):
        """<exit_stmt> ::= 'exit'

        Terminates the program immediately.
        """
        line = self._current().line
        self._expect_keyword(_KW_EXIT)
        self._log(f"Statement recognized (line {line}): exit")

    def _if_stmt(self):
        """<if_stmt> ::= 'if' <condition> 'then' <actions>
                         [ 'else' <actions> ]
                         'endif'

        Supports nesting to any depth because _actions() calls _if_stmt()
        recursively when it encounters another 'if'.
        """
        line = self._current().line
        self._expect_keyword(_KW_IF)
        cond = self._condition()
        self._expect_keyword(_KW_THEN)

        self._log(f"Statement recognized (line {line}): if {cond} then")
        self._actions()  # 'then' branch – recurses for nested ifs

        tok = self._current()
        if tok and tok.type == KEYWORD and tok.id == _KW_ELSE:
            self._consume()
            self._log(f"  else (line {tok.line})")
            self._actions()  # 'else' branch

        self._expect_keyword(_KW_ENDIF)
        self._log(f"  endif (closing if at line {line})")

    # =======================================================================
    # EXPRESSION / CONDITION  (called by the statement parsers above)
    # =======================================================================

    def _expression(self) -> str:
        """<expression> ::= <term> { ( '+' | '-' ) <term> }

        Returns a human-readable string representation of the expression
        (used for logging).  Until a full evaluator is wired up, this acts
        as a plain-constant / identifier expression validator.
        """
        parts = [self._term()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id in (_OP_PLUS, _OP_MINUS)):
            op_tok = self._consume()
            parts.append(op_tok.value)
            parts.append(self._term())
        return " ".join(parts)

    def _term(self) -> str:
        """<term> ::= <factor> { ( '*' | '/' ) <factor> }"""
        parts = [self._factor()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id in (_OP_MUL, _OP_DIV)):
            op_tok = self._consume()
            parts.append(op_tok.value)
            parts.append(self._factor())
        return " ".join(parts)

    def _factor(self) -> str:
        """<factor> ::= IDENTIFIER | INTEGER_CONST | REAL_CONST

        Verifies that an IDENTIFIER was declared before it is used.
        """
        tok = self._current()
        if tok is None:
            self._parse_error("unexpected end of file in expression")
            return "<eof>"

        if tok.type == IDENTIFIER:
            self._consume()
            if not self.identifierExists(tok.value):
                self._parse_error(
                    f"undeclared variable '{tok.value}'", tok.line
                )
            return tok.value

        if tok.type in (INTEGER_CONST, REAL_CONST):
            self._consume()
            return tok.value

        self._parse_error(
            f"expected identifier or number in expression, got {_tok_desc(tok)}",
            tok.line,
        )
        return "<error>"

    def _condition(self) -> str:
        """<condition> ::= <expression> <rel_op> <expression>

        <rel_op> ::= '>' | '<' | '=='
        """
        left = self._expression()
        tok = self._current()

        if tok and tok.type == OPERATOR and tok.id in (_OP_GT, _OP_LT, _OP_EQ):
            op_sym = self._consume().value
        else:
            got = _tok_desc(tok)
            self._parse_error(
                f"expected relational operator ('>', '<', '=='), got {got}",
                tok.line if tok else None,
            )
            op_sym = "?"

        right = self._expression()
        return f"{left} {op_sym} {right}"


# ===========================================================================
# Module-level helpers (private to this file)
# ===========================================================================

def _kw_name(kw_id: int) -> str:
    """Return the keyword string for a given keyword ID."""
    for name, kid in KEYWORDS.items():
        if kid == kw_id:
            return name
    return f"<kw:{kw_id}>"


def _op_sym(op_id: int) -> str:
    """Return the operator string for a given operator ID."""
    for sym, oid in OPERATORS.items():
        if oid == op_id:
            return sym
    return f"<op:{op_id}>"


def _tok_desc(tok) -> str:
    """Return a short, printable description of a token (or 'end of file')."""
    if tok is None:
        return "end of file"
    return f"{tok.type} '{tok.value}' (id {tok.id})"


# ===========================================================================
# Entry point
# ===========================================================================

def main():
    if len(sys.argv) != 2:
        print("Usage: python scl_parser.py <source_file.scl>")
        sys.exit(2)

    path = sys.argv[1]
    if not os.path.isfile(path):
        print(f"Error: file not found: '{path}'")
        sys.exit(2)

    parser = SCLParser(path)
    parser.begin()


if __name__ == "__main__":
    main()

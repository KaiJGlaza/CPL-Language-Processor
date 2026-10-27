"""
scl_parser.py
CS 4308 - SCL Interpreter Project, Deliverable 2

Recursive-descent parser for our SCL subset.

The parser is initialised with a source file path.  It calls the scanner
internally, then walks the token stream according to grammar/scl_subset.txt.

Grammar extended for this deliverable:
    <statement>  ::= <set_stmt> | <display_stmt> | <input_stmt>
                   | <if_stmt>  | <exit_stmt>    | <return_stmt>
    <return_stmt> ::= "return" <expression>

Public API (three required functions):
    getNextToken()              - next non-comment token
    identifierExists(name)      - True if name was declared
    begin()                     - start the parse

Error format (from the team plan):
    Syntax error (line N): expected X, found Y

Usage:
    python scl_parser.py <source_file.scl> [output.txt]
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
# Token-id constants (purely for readability)
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

        # Filter out UNKNOWN tokens (the scanner already reported them) so
        # the parser never sees them, then store the clean stream.
        self._tokens = [t for t in all_tokens if t.type != UNKNOWN]
        self._pos = 0
        self._last_line = self._tokens[-1].line if self._tokens else None

        # Names declared by 'define' (NOT the scanner's table, which holds
        # every identifier seen anywhere, declared or not).
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
        """Consume and return the current token (via getNextToken)."""
        return self.getNextToken()

    def _synchronize(self):
        """Skip tokens until a statement keyword or block terminator.

        Called after a syntax error so one mistake does not trigger a
        pile of follow-on errors.  Does not consume the sync token.
        """
        while True:
            tok = self._current()
            if tok is None:
                return
            if tok.type == KEYWORD and tok.id in _SYNC_POINTS:
                return
            self.getNextToken()

    def error(self, expected, found):
        """Report: expected <expected>, found <found>.

        *found* is the offending token, or None at end of file.  The error
        is printed, counted, and logged; _actions() then resynchronizes at
        the next statement.
        """
        self._parse_error(f"expected {expected}, found {_tok_desc(found)}",
                          found.line if found else None)

    def match(self, expected):
        """Consume the current token if it fits *expected*, else error().

        *expected* is one of:
            a keyword such as "set" or "endif"
            an operator such as "=" or ","
            IDENTIFIER or STRING (a token type from scl_token.py)

        Returns the consumed token, or None if it did not fit.  Every
        part of the parser goes through this method.
        """
        tok = self._current()

        if expected in (IDENTIFIER, STRING):
            fits = tok is not None and tok.type == expected
            label = "identifier" if expected == IDENTIFIER else "string literal"
        elif expected in KEYWORDS:
            fits = (tok is not None and tok.type == KEYWORD
                    and tok.value == expected)
            label = f"'{expected}'"
        elif expected in OPERATORS:
            fits = (tok is not None and tok.type == OPERATOR
                    and tok.value == expected)
            label = f"'{expected}'"
        else:
            raise ValueError(f"match(): unknown expected token {expected!r}")

        if fits:
            return self._consume()
        self.error(label, tok)
        return None

    def _parse_error(self, message, line=None):
        """Record and print one syntax error, then keep parsing."""
        if line is None:
            line = self._last_line
        loc = f" (line {line})" if line else ""
        full = f"Syntax error{loc}: {message}"
        self._errors.append(full)
        self._output.append(full)
        print(full)

    def _log(self, message):
        """Record and print a recognised-statement message."""
        self._output.append(message)
        print(message)

    def _check_declared(self, tok):
        """Report an undeclared variable use."""
        if tok and not self.identifierExists(tok.value):
            self._parse_error(f"undeclared variable '{tok.value}'", tok.line)

    def _finish(self):
        """Print the final summary and write the output file."""
        if self._errors:
            summary = f"Parse finished with {len(self._errors)} error(s)."
        else:
            summary = "Parse completed successfully - no errors."
        print()
        print(summary)
        self._output.append("")
        self._output.append(summary)

        try:
            with open(self._output_path, "w", encoding="utf-8") as out:
                out.write("\n".join(self._output) + "\n")
            print(f"Parser output saved to {self._output_path}")
        except OSError as problem:
            print(f"Could not write '{self._output_path}': {problem}")

    # =======================================================================
    # STRUCTURAL PARSE METHODS
    # =======================================================================

    def _start(self):
        """<program> ::= <import> 'implementations' <function>"""
        self._import_stmt()
        self.match("implementations")
        self._function_def()

        # Nothing may follow 'endfun main'
        extra = self._current()
        if extra is not None:
            self.error("end of file after 'endfun main'", extra)

    def _import_stmt(self):
        """<import> ::= 'import' STRING"""
        self.match("import")
        tok = self.match(STRING)
        if tok:
            self._log(f"Statement recognized: import \"{tok.value}\"")

    def _function_def(self):
        """<function> ::= 'function' 'main' 'return' 'type' <type>
                          'is' 'variables' <declarations>
                          'begin' <statements> 'endfun' 'main'"""
        self.match("function")
        self.match("main")
        self.match("return")
        self.match("type")
        self._type()
        self.match("is")
        self.match("variables")
        self._declarations()
        self.match("begin")
        self._actions("'begin' block")
        self.match("endfun")
        self.match("main")

    def _declarations(self):
        """<declarations> ::= <declaration> { <declaration> }"""
        self._declaration()
        while (self._current() and
               self._current().type == KEYWORD and
               self._current().id == _KW_DEFINE):
            self._declaration()

    def _declaration(self):
        """<declaration> ::= 'define' IDENTIFIER 'of' 'type' <type>"""
        self.match("define")
        tok = self.match(IDENTIFIER)
        self.match("of")
        self.match("type")
        type_name = self._type()
        if tok:
            if tok.value in self._declared:
                self._parse_error(
                    f"duplicate declaration of '{tok.value}'", tok.line
                )
            else:
                self._declared.add(tok.value)
                self._log(f"Statement recognized: "
                          f"define {tok.value} of type {type_name}")

    def _type(self):
        """<type> ::= 'integer' | 'real'"""
        tok = self._current()
        if tok and tok.type == KEYWORD and tok.id in (_KW_INTEGER, _KW_REAL):
            self._consume()
            return tok.value
        self.error("type ('integer' or 'real')", tok)
        return "unknown"

    # =======================================================================
    # actions() and all statement parsers
    # =======================================================================

    def _actions(self, block_name="block"):
        """Read and parse statements until 'endfun', 'else', or 'endif'.

        <statements> ::= <statement> { <statement> }

        Stops (without consuming) at a block terminator.  The grammar
        requires at least one statement, so an empty block is an error.
        After any error the parser resynchronizes at the next statement
        keyword or terminator.
        """
        attempted = False

        while True:
            tok = self._current()

            # End of token stream
            if tok is None:
                break

            # Block terminator - stop but do NOT consume
            if tok.type == KEYWORD and tok.id in _TERMINATORS:
                break

            attempted = True

            if tok.type == KEYWORD and tok.id in _STATEMENT_STARTS:
                errors_before = len(self._errors)

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
                else:
                    self._if_stmt()

                # The statement had an error: skip to a safe place
                if len(self._errors) > errors_before:
                    self._synchronize()
            else:
                # Stray token that cannot start a statement
                self.error("a statement", tok)
                self._consume()
                self._synchronize()

        if not attempted:
            self.error(f"at least one statement in {block_name}",
                           self._current())

    # -----------------------------------------------------------------------
    # Individual statement parsers
    # -----------------------------------------------------------------------

    def _set_stmt(self):
        """<set_stmt> ::= 'set' IDENTIFIER '=' <expression>"""
        line = self._current().line
        self.match("set")

        name_tok = self.match(IDENTIFIER)
        self._check_declared(name_tok)

        self.match("=")
        val = self._expression()

        if name_tok:
            self._log(f"Statement recognized (line {line}): "
                      f"set {name_tok.value} = {val}")

    def _display_stmt(self):
        """<display_stmt> ::= 'display' <display_item> { ',' <display_item> }"""
        line = self._current().line
        self.match("display")

        items = [self._display_item()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id == _OP_COMMA):
            self._consume()  # eat ','
            items.append(self._display_item())

        self._log(f"Statement recognized (line {line}): "
                  f"display {', '.join(items)}")

    def _display_item(self):
        """<display_item> ::= STRING | <expression>"""
        tok = self._current()
        if tok and tok.type == STRING:
            self._consume()
            return f'"{tok.value}"'
        return self._expression()

    def _input_stmt(self):
        """<input_stmt> ::= 'input' STRING ',' IDENTIFIER"""
        line = self._current().line
        self.match("input")

        prompt_tok = self.match(STRING)
        self.match(",")
        name_tok = self.match(IDENTIFIER)
        self._check_declared(name_tok)

        if prompt_tok and name_tok:
            self._log(f"Statement recognized (line {line}): "
                      f"input \"{prompt_tok.value}\", {name_tok.value}")

    def _return_stmt(self):
        """<return_stmt> ::= 'return' <expression>"""
        line = self._current().line
        self.match("return")
        val = self._expression()
        self._log(f"Statement recognized (line {line}): return {val}")

    def _exit_stmt(self):
        """<exit_stmt> ::= 'exit'"""
        line = self._current().line
        self.match("exit")
        self._log(f"Statement recognized (line {line}): exit")

    def _if_stmt(self):
        """<if_stmt> ::= 'if' <condition> 'then' <statements>
                         [ 'else' <statements> ]
                         'endif'

        Nests to any depth because _actions() calls _if_stmt() again
        whenever it sees another 'if'.
        """
        line = self._current().line
        self.match("if")
        cond = self._condition()
        self.match("then")

        self._log(f"Statement recognized (line {line}): if {cond} then")
        self._actions("'then' branch")

        tok = self._current()
        if tok and tok.type == KEYWORD and tok.id == _KW_ELSE:
            self._consume()
            self._log(f"  else (line {tok.line})")
            self._actions("'else' branch")

        self.match("endif")
        self._log(f"  endif (closing if at line {line})")

    # =======================================================================
    # EXPRESSION / CONDITION
    # =======================================================================

    def _expression(self):
        """<expression> ::= <term> { ( '+' | '-' ) <term> }

        Returns a readable string (used for logging).  A tree return value
        for Deliverable 3 can replace this later.
        """
        parts = [self._term()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id in (_OP_PLUS, _OP_MINUS)):
            op_tok = self._consume()
            parts.append(op_tok.value)
            parts.append(self._term())
        return " ".join(parts)

    def _term(self):
        """<term> ::= <factor> { ( '*' | '/' ) <factor> }"""
        parts = [self._factor()]
        while (self._current() and
               self._current().type == OPERATOR and
               self._current().id in (_OP_MUL, _OP_DIV)):
            op_tok = self._consume()
            parts.append(op_tok.value)
            parts.append(self._factor())
        return " ".join(parts)

    def _factor(self):
        """<factor> ::= IDENTIFIER | INTEGER_CONST | REAL_CONST"""
        tok = self._current()

        if tok is not None and tok.type == IDENTIFIER:
            self._consume()
            self._check_declared(tok)
            return tok.value

        if tok is not None and tok.type in (INTEGER_CONST, REAL_CONST):
            self._consume()
            return tok.value

        self.error("identifier or number", tok)
        return "<error>"

    def _condition(self):
        """<condition> ::= <expression> <rel_op> <expression>

        <rel_op> ::= '>' | '<' | '=='
        """
        left = self._expression()
        tok = self._current()

        if tok and tok.type == OPERATOR and tok.id in (_OP_GT, _OP_LT, _OP_EQ):
            op_sym = self._consume().value
        else:
            self.error("relational operator ('>', '<', '==')", tok)
            op_sym = "?"

        right = self._expression()
        return f"{left} {op_sym} {right}"


# ===========================================================================
# Module-level helpers (private to this file)
# ===========================================================================

def _tok_desc(tok):
    """Return a short, printable description of a token (or 'end of file')."""
    if tok is None:
        return "end of file"
    return f"'{tok.value}'"


# ===========================================================================
# Entry point
# ===========================================================================

def main():
    if len(sys.argv) not in (2, 3):
        print("Usage: python scl_parser.py <source_file.scl> [output.txt]")
        sys.exit(2)

    path = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) == 3 else DEFAULT_OUTPUT

    if not os.path.isfile(path):
        print(f"Error: file not found: '{path}'")
        sys.exit(2)

    parser = SCLParser(path, output_path)
    parse_errors = parser.begin()

    # Non-zero exit if the parser OR the scanner found problems
    sys.exit(1 if (parse_errors or scl_scanner.errors) else 0)


if __name__ == "__main__":
    main()
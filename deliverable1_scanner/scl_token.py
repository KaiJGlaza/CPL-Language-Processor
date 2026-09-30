"""
scl_token.py
CS 4308 - SCL Interpreter Project, Deliverable 1

Defines the tokens for our SCL subset:
  - KEYWORDS:  the 22 keywords in our subset, each mapped to an ID number
  - OPERATORS: the operators and special symbols, each mapped to an ID number
  - Token IDs for the other token types (identifiers, numbers, strings)
  - The Token class, which stores one scanned token

These lists must match grammar/scl_subset.txt exactly.
"""

# Keywords (IDs 1-22), grouped the same way as in our grammar file
KEYWORDS = {
    # Structure
    "import": 1,
    "implementations": 2,
    "function": 3,
    "main": 4,
    "return": 5,
    "type": 6,
    "is": 7,
    "variables": 8,
    "begin": 9,
    "endfun": 10,
    # Declarations
    "define": 11,
    "of": 12,
    "integer": 13,
    "real": 14,
    # Actions
    "set": 15,
    "display": 16,
    "input": 17,
    "exit": 18,
    # Conditionals
    "if": 19,
    "then": 20,
    "else": 21,
    "endif": 22,
}

# Operators and special symbols (IDs 101-109)
OPERATORS = {
    "=": 101,    # assignment
    "+": 102,    # addition
    "-": 103,    # subtraction
    "*": 104,    # multiplication
    "/": 105,    # division
    ">": 106,    # greater than
    "<": 107,    # less than
    "==": 108,   # equal to
    ",": 109,    # separator (used in display and input)
}

# Token type names and IDs for everything that is not a keyword/operator
KEYWORD = "KEYWORD"
OPERATOR = "OPERATOR"
IDENTIFIER = "IDENTIFIER"
INTEGER_CONST = "INTEGER_CONST"
REAL_CONST = "REAL_CONST"
STRING = "STRING"
UNKNOWN = "UNKNOWN"

IDENTIFIER_ID = 200
INTEGER_CONST_ID = 300
REAL_CONST_ID = 301
STRING_ID = 400
UNKNOWN_ID = 999


class Token:
    """Stores one token found by the scanner."""

    def __init__(self, token_type, token_id, value, line):
        self.type = token_type   # KEYWORD, IDENTIFIER, STRING, etc.
        self.id = token_id       # ID number from the tables above
        self.value = value       # the actual text
        self.line = line         # line number in the SCL file

    def to_dict(self):
        """Return the token as a dictionary so it can be saved to JSON."""
        return {
            "type": self.type,
            "id": self.id,
            "value": self.value,
            "line": self.line,
        }

    def __str__(self):
        """Return a readable version of the token for printing."""
        return f"Line {self.line}: {self.type} (id {self.id}) -> {self.value}"
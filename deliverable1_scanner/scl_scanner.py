"""
scl_scanner.py
CS 4308 - SCL Interpreter Project, Deliverable 1

Scanner for our SCL subset. Pipeline:
    read file -> remove comments -> split into lines -> scan each line
    -> print tokens -> save JSON -> print error summary

Grammar: grammar/scl_subset.txt
Tokens:  scl_token.py

Usage:
    python scl_scanner.py <source_file.scl> [output.json]

"""

import json
import sys

from scl_token import (
    KEYWORDS, OPERATORS,
    KEYWORD, OPERATOR, IDENTIFIER, INTEGER_CONST, REAL_CONST, STRING, UNKNOWN,
    IDENTIFIER_ID, INTEGER_CONST_ID, REAL_CONST_ID, STRING_ID, UNKNOWN_ID,
    Token,
)


# ---------------------------------------------------------------------------
# Errors module
# ---------------------------------------------------------------------------
errors = []  # list of (line_number, message)


def report_error(line, message):
    """Record a scanning error and print it.
    Policy: report, skip the bad piece, keep scanning.
	
	Arguments:
		line: line number in the SCL file where the error was found
		message: short description, ex: "unterminated string"
	
	"""
    errors.append((line, message))
	print(f"Line {line}: ERROR - {message}")
    


# ---------------------------------------------------------------------------
# Token helper (scl_token.py)
# ---------------------------------------------------------------------------
def make_token(token_type, token_id, value, line):
    """Build and return a Token object.
	
	Token creation happens here.
	
	Arguments:
	 token_type: one of KEYWORD, OPERATOR, IDENTIFIER, INTEGER_CONST,
                    REAL_CONST, STRING, UNKNOWN (constants from scl_token.py)
        token_id:   the ID number for this token (from KEYWORDS, OPERATORS,
                    or one of the *_ID constants)
        value:      the actual text of the token (for STRING, the text
                    between the quotes)
        line:       the line number in the SCL file where it was found
	
	"""
    return Token(token_type, token_id, value, line)
    


# ---------------------------------------------------------------------------
# Identifier table module
# ---------------------------------------------------------------------------
class IdentifierTable:
    """Remembers each identifier the first time it is seen."""

    def __init__(self):
        self.table = {}  # name -> first line number

    def add(self, name, line):
        """Add name if it is not already in the table.
			Returns True if it was new, False if it was already there.
		"""
        if name in self.table:
            return False
        self.table[name] = line
        return True
        
	def exists(self, name):
        """Return True if name is already in the table."""
        return name in self.table	

    def to_dict(self):
        """Return the table as a dictionary for JSON output."""
        return dict(self.table)


# ---------------------------------------------------------------------------
# Comment remover module (Person 1)
# ---------------------------------------------------------------------------
def _is_word_char(ch):
    """True if ch can be part of an identifier (letter, digit, underscore)."""
    return ch.isalnum() or ch == "_"
 
 
def remove_comments(text):
    """Strip comments from the whole source text.
 
    Comment forms (from scl_subset.txt):
        //            to the end of the line
        description   up to and including */
 
    Rules:
      - Walk the text character by character so that '//' or 'description'
        inside a "string" is NOT treated as a comment. A string cannot span
        lines, so a newline ends string mode (an unterminated string is
        reported later by read_string).
      - 'description' only starts a comment when it is a whole word, so
        identifiers like 'descriptions' or 'my_description' are untouched.
        It is lowercase only, like every other keyword.
      - Comments do not nest: once inside one, only '*/' (or, for '//', the
        end of the line) matters.
      - Every newline is kept, even inside a removed comment, so line
        numbers stay correct for later error messages.
      - Each removed comment is replaced by one space so the tokens on
        either side of it cannot run together.
      - An unterminated 'description' comment is reported at the line where
        it started, and the rest of the file is treated as comment.
 
    Returns the cleaned text.
    """
    result = []
    n = len(text)
    i = 0
    line = 1
    in_string = False
 
    while i < n:
        ch = text[i]
 
        # Inside a string: copy everything; a quote or newline ends it.
        if in_string:
            result.append(ch)
            if ch == '"':
                in_string = False
            elif ch == "\n":
                in_string = False
                line += 1
            i += 1
            continue
 
        # Start of a string.
        if ch == '"':
            in_string = True
            result.append(ch)
            i += 1
            continue
 
        # '//' comment: skip to the end of the line (keep the newline).
        if text.startswith("//", i):
            while i < n and text[i] != "\n":
                i += 1
            result.append(" ")
            continue
 
        # 'description' comment: only when it is a whole word.
        if text.startswith("description", i):
            after = i + len("description")
            before_ok = i == 0 or not _is_word_char(text[i - 1])
            after_ok = after >= n or not _is_word_char(text[after])
            if before_ok and after_ok:
                close = text.find("*/", after)
                if close == -1:
                    report_error(line, "unterminated comment "
                                       "(started with 'description')")
                    close = n
                else:
                    close += 2
                skipped_newlines = text.count("\n", i, close)
                result.append(" " + "\n" * skipped_newlines)
                line += skipped_newlines
                i = close
                continue
 
        # Ordinary character.
        if ch == "\n":
            line += 1
        result.append(ch)
        i += 1
 
    return "".join(result)
# ---------------------------------------------------------------------------
# Line splitting module
# ---------------------------------------------------------------------------
def split_lines(text):
    """Return a list of (line_number, line_text) pairs, numbered from 1."""
    # TODO
    pass


# ---------------------------------------------------------------------------
# Token readers (each returns (token, next_index))
# ---------------------------------------------------------------------------
def read_word(line_text, start, line_number, id_table):
    """Read letter {letter | digit | '_'}.
    If the word is in KEYWORDS -> KEYWORD token (keywords are lowercase only).
    Otherwise -> IDENTIFIER token, and add it to id_table."""
    # TODO
    pass


def read_number(line_text, start, line_number):
    """Read digit {digit}, optionally followed by '.' digit {digit}.
    INTEGER_CONST (300) or REAL_CONST (301).
    A '.' with no digit after it (e.g. '3.') is an error."""
    # TODO
    pass


def read_string(line_text, start, line_number):
    """Read from the opening '"' to the closing '"'.
    Token value = the text between the quotes.
    If the line ends first -> report 'unterminated string'."""
    # TODO
    pass


def read_operator(line_text, start, line_number):
    """Match operators from OPERATORS.
    Look ahead one character so '==' (108) wins over '=' (101)."""
    # TODO
    pass


# ---------------------------------------------------------------------------
# Core scanning loop
# ---------------------------------------------------------------------------
def scan_line(line_text, line_number, id_table):
    """Scan one line and return a list of Tokens.

    Dispatch on the current character:
      whitespace        -> skip
      letter            -> read_word
      digit             -> read_number
      '"'               -> read_string
      operator character -> read_operator
      anything else     -> report_error, make an UNKNOWN token, skip the
                           character, keep scanning
    """
    # TODO
    pass


def scan_source(text, id_table):
    """Remove comments, split into lines, scan every line.
    Returns the full list of Tokens."""
    # TODO
    pass


# ---------------------------------------------------------------------------
# Output
# ---------------------------------------------------------------------------
def print_tokens(tokens):
    """Print each token to the console using str(token)."""
    # TODO
    pass


def save_json(tokens, id_table, path):
    """Write the tokens (and identifier table) to a JSON file.
    Tokens: [t.to_dict() for t in tokens]"""
    # TODO
    pass


def print_error_summary():
    """Print the total error count and each error, or 'No errors'."""
    # TODO
    pass


# ---------------------------------------------------------------------------
# Driver
# ---------------------------------------------------------------------------
def read_source(path):
    """Return the file contents as a string.
    If the file is missing/unreadable, print a clean message and exit(2)."""
    # TODO
    pass


def main():
    """
    1. Check sys.argv (source file required, JSON path optional,
       default 'OutputTokens.json').
    2. text = read_source(path)
    3. id_table = IdentifierTable()
    4. tokens = scan_source(text, id_table)
    5. print_tokens(tokens)
    6. save_json(tokens, id_table, json_path)
    7. print_error_summary()
    8. Exit with a non-zero code if any errors were reported.
    """
    # TODO
    pass


if __name__ == "__main__":
    main()
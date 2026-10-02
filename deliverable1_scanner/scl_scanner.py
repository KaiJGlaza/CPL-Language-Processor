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
    """Return a list of (line_number, line_text) pairs, numbered from 1.
 
    Rules:
      - Split only on '\\n', the same character remove_comments counts, so
        the line numbers of the two steps always agree. (str.splitlines()
        also splits on other characters and could throw numbering off.)
      - A trailing '\\r' (Windows line endings) is removed from each line.
      - Blank lines are kept so numbering stays correct; scan_line simply
        produces no tokens for them.
      - A final newline at the end of the file does not create an extra
        empty line.
    """
    pieces = text.split("\n")
    if pieces and pieces[-1] == "":
        pieces.pop()
    return [(number, piece.rstrip("\r"))
            for number, piece in enumerate(pieces, start=1)]

# ---------------------------------------------------------------------------
# Token readers (each returns (token, next_index))
# ---------------------------------------------------------------------------
def read_word(line_text, start, line_number, id_table):
    """Read letter {letter | digit | '_'}.
    If the word is in KEYWORDS -> KEYWORD token (keywords are lowercase only).
    Otherwise -> IDENTIFIER token, and add it to id_table."""
    end = start + 1
    while end < len(line_text) and _is_word_char(line_text[end]):
        end += 1
    word = line_text[start:end]
 
    if word in KEYWORDS:
        return make_token(KEYWORD, KEYWORDS[word], word, line_number), end
 
    id_table.add(word, line_number)
    return make_token(IDENTIFIER, IDENTIFIER_ID, word, line_number), end


def read_number(line_text, start, line_number):
    """Read digit {digit}, optionally followed by '.' digit {digit}.
    INTEGER_CONST (300) or REAL_CONST (301).
    A '.' with no digit after it (e.g. '3.') is an error."""
     n = len(line_text)
    end = start
    while end < n and _is_digit(line_text[end]):
        end += 1
 
    is_real = False
    if end < n and line_text[end] == ".":
        if end + 1 < n and _is_digit(line_text[end + 1]):
            end += 1
            while end < n and _is_digit(line_text[end]):
                end += 1
            is_real = True
        else:
            report_error(line_number,
                         f"malformed real constant '{line_text[start:end + 1]}'"
                         " (digits required after '.')")
            return None, end + 1
 
    if end < n and _is_word_char(line_text[end]):
        while end < n and _is_word_char(line_text[end]):
            end += 1
        report_error(line_number,
                     f"invalid token '{line_text[start:end]}'"
                     " (identifiers must start with a letter)")
        return None, end
 
    text = line_text[start:end]
    if is_real:
        return make_token(REAL_CONST, REAL_CONST_ID, text, line_number), end
    return make_token(INTEGER_CONST, INTEGER_CONST_ID, text, line_number), end


def read_string(line_text, start, line_number):
    """Read from the opening '"' to the closing '"'.
    Token value = the text between the quotes.
    If the line ends first -> report 'unterminated string'."""
     close = line_text.find('"', start + 1)
    if close == -1:
        report_error(line_number, "unterminated string")
        return None, len(line_text)
    value = line_text[start + 1:close]
    return make_token(STRING, STRING_ID, value, line_number), close + 1


def read_operator(line_text, start, line_number):
    """Match operators from OPERATORS.
    Look ahead one character so '==' (108) wins over '=' (101)."""
   two_chars = line_text[start:start + 2]
    if two_chars in OPERATORS:
        return (make_token(OPERATOR, OPERATORS[two_chars], two_chars,
                           line_number), start + 2)
 
    one_char = line_text[start:start + 1]
    if one_char in OPERATORS:
        return (make_token(OPERATOR, OPERATORS[one_char], one_char,
                           line_number), start + 1)
 
    return None, start


# ---------------------------------------------------------------------------
# Core scanning loop
# ---------------------------------------------------------------------------
def scan_line(line_text, line_number, id_table):
    """Scan one line and return a list of Tokens.

      whitespace        -> skip
      letter            -> read_word
      digit             -> read_number
      '"'               -> read_string
      operator character -> read_operator
      anything else     -> report_error, make an UNKNOWN token, skip the
                           character, keep scanning
    """
     tokens = []
    i = 0
    n = len(line_text)
 
    while i < n:
        ch = line_text[i]
 
        if ch.isspace():
            i += 1
            continue
 
        if _is_letter(ch):
            token, next_i = read_word(line_text, i, line_number, id_table)
        elif _is_digit(ch):
            token, next_i = read_number(line_text, i, line_number)
        elif ch == '"':
            token, next_i = read_string(line_text, i, line_number)
        elif ch in OPERATOR_START_CHARS:
            token, next_i = read_operator(line_text, i, line_number)
        else:
            report_error(line_number, f"unknown character '{ch}'")
            token = make_token(UNKNOWN, UNKNOWN_ID, ch, line_number)
            next_i = i + 1
 
        if token is not None:
            tokens.append(token)
 
        i = next_i if next_i > i else i + 1
 
    return tokens


def scan_source(text, id_table):
     cleaned = remove_comments(text)
    tokens = []
    for line_number, line_text in split_lines(cleaned):
        tokens.extend(scan_line(line_text, line_number, id_table))
    return tokens


# ---------------------------------------------------------------------------
# Output
# ---------------------------------------------------------------------------
def print_tokens(tokens):
    for token in tokens:
        print(token)
    print(f"Total tokens: {len(tokens)}")


def print_identifier_table(id_table):
    print("Identifiers (first line seen):")
    if not id_table.table:
        print("  (none)")
        return
    for name, line in id_table.to_dict().items():
        print(f"  {name}: line {line}")

def save_json(tokens, id_table, path):
   try:
        with open(path, "w") as out_file:
            json.dump([token.to_dict() for token in tokens], out_file, indent=2)
        return True
    except OSError as problem:
        print(f"Could not write '{path}': {problem}")
        return False


def print_error_summary():
    """Print the total error count and each error, or 'No errors'."""
    if not errors:
        print("No errors.")
        return
    print(f"{len(errors)} error(s) found:")
    for line, message in sorted(errors, key=lambda e: e[0]):
        print(f"  Line {line}: {message}")


# ---------------------------------------------------------------------------
# Driver
# ---------------------------------------------------------------------------
def read_source(path):
    try:
        with open(path, "r", encoding="utf-8") as source_file:
            return source_file.read()
    except FileNotFoundError:
        print(f"Error: no such file: '{path}'")
    except UnicodeDecodeError:
        print(f"Error: '{path}' is not a readable text file (UTF-8 expected)")
    except OSError as problem:
        print(f"Error: could not read '{path}': {problem}")
    sys.exit(2)


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
    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print("Usage: python scl_scanner.py <source_file.scl> [output.json]")
        sys.exit(2)
 
    source_path = sys.argv[1]
    json_path = sys.argv[2] if len(sys.argv) == 3 else "OutputTokens.json"
 
    text = read_source(source_path)
    id_table = IdentifierTable()
    tokens = scan_source(text, id_table)
 
    print_tokens(tokens)
    print_identifier_table(id_table)
    json_saved = save_json(tokens, json_path)
    if json_saved:
        print(f"Token list saved to {json_path}")
    print_error_summary()
 
    sys.exit(1 if (errors or not json_saved) else 0)


if __name__ == "__main__":
    main()
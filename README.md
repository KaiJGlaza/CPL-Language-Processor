# SCL Interpreter

**Course:** CS 4308 – Concepts of Programming Languages, Section W02
**Institution:** Kennesaw State University
**Team:** Kai Glaza, Logan Nelson, Vlad Kuskov, David Ripley
## Overview

This project is an interpreter for a subset of **SCL**, an experimental
systems programming language created by Dr. Jose Garrido. It is built in
three stages:

| Deliverable | Component   | Language | Status      |
|-------------|-------------|----------|-------------|
| 1           | Scanner     | Python   | Submitted   |
| 2           | Parser      | Python   | In progress |
| 3           | Interpreter | TBD      | Not started |

## How It Works

```
SCL program (.scl) -> Scanner -> Parser -> Interpreter
                      tokens     syntax     executes
                      (JSON)     check      the program
```

- **Scanner:** reads an SCL source file and produces a list of tokens.
- **Parser:** checks the tokens against the grammar and reports syntax errors.
- **Interpreter:** executes the program and reports runtime errors.

## Project Structure

```
grammar/                  BNF/EBNF grammar of the SCL subset
examples/                 SCL test programs from Deliverable 1
tests/                    SCL test programs for the parser
deliverable1_scanner/     Scanner (Python)
deliverable2_parser/      Parser (Python)
deliverable3_interpreter/ Interpreter
reports/                  Written reports for each deliverable
screenshots/              Screenshots of program runs
```

## How to Run

**Scanner**
```
python deliverable1_scanner/scl_scanner.py examples/examplefile.scl
```
Prints the tokens to the console and writes them to a JSON file

**Parser**
```
python deliverable2_parser/scl_parser.py tests/valid_declarations.scl [output.txt]
```
Prints each recognized statement and every syntax error, then saves the
same lines to the output file (ParserOutput.txt if none is given).

**Interpreter**
Instructions will be added with Deliverable 3.

## Supported SCL Subset

See `grammar/scl_subset.txt` for the full grammar: an import, `function main`,
`define` with integer or real, `set`, `display`, `input`, `return`, `exit`,
and nested `if`/`else`/`endif`.

## Requirements

- Python 3.x
- Deliverable 3 must use a language not used in Deliverables 1 and 2 (not chosen yet)

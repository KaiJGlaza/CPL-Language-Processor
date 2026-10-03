# SCL Interpreter

**Course:** CS 4308 – Concepts of Programming Languages, Section [XX]
**Institution:** Kennesaw State University
**Team:** Kai Glaza, Logan Nelson, Vlad Kuskov, David Ripley
## Overview

This project is an interpreter for a subset of **SCL**, an experimental
systems programming language created by Dr. Jose Garrido. It is built in
three stages:

| Deliverable | Component   | Language | Status      |
|-------------|-------------|----------|-------------|
| 1           | Scanner     | Python   | In progress |
| 2           | Parser      | Python   | Not started |
| 3           | Interpreter | [Java]???| Not started |

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
examples/                 SCL test programs (inputs)
deliverable1_scanner/     Scanner (Python)
deliverable2_parser/      Parser (Python)
deliverable3_interpreter/ Interpreter ([Java])
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
python deliverable2_parser/scl_parser.py examples/examplefile.scl
```

**Interpreter**
Instructions will be added with Deliverable 3.

## Supported SCL Subset

See `grammar/scl_subset.txt` for the full grammar. Currently planned:
imports, function declarations, variable definitions, `set`, `display`,
`input`, `exit`, and [if / while].

## Requirements

- Python 3.x
- [Java JDK, for Deliverable 3] ???
package io.github.styler.parser;

/**
 * High-performance Java lexer implementing tokenization for JDK 25 features.
 *
 * This is a simplified implementation focusing on the core architecture.
 * A full implementation would include:
 * - Complete Unicode support
 * - All JDK 25 language features
 * - Comprehensive error recovery
 * - String template processing
 *
 * The design prioritizes performance through:
 * - Single-pass scanning
 * - Minimal object allocation
 * - Efficient character classification
 */
public class JavaLexer {
    private final String source;
    private final int length;
    private int position = 0;
    private int line = 1;
    private int column = 1;

    public JavaLexer(String source) {
        this.source = source;
        this.length = source.length();
    }

    /**
     * Returns the next token from the input stream.
     */
    public TokenInfo nextToken() {
        long startTime = METRICS_ENABLED ? System.nanoTime() : 0;

        skipWhitespace();

        if (position >= length) {
            return createToken(TokenType.EOF, position, 0, "");
        }

        int tokenStart = position;
        char ch = source.charAt(position);

        TokenInfo token = switch (ch) {
            case '(' -> singleCharToken(TokenType.LPAREN);
            case ')' -> singleCharToken(TokenType.RPAREN);
            case '{' -> singleCharToken(TokenType.LBRACE);
            case '}' -> singleCharToken(TokenType.RBRACE);
            case '[' -> singleCharToken(TokenType.LBRACKET);
            case ']' -> singleCharToken(TokenType.RBRACKET);
            case ';' -> singleCharToken(TokenType.SEMICOLON);
            case ',' -> singleCharToken(TokenType.COMMA);
            case '@' -> singleCharToken(TokenType.AT);
            case '?' -> singleCharToken(TokenType.QUESTION);
            case ':' -> singleCharToken(TokenType.COLON);
            case '~' -> singleCharToken(TokenType.BITWISE_NOT);

            // Multi-character operators and punctuation
            case '.' -> scanDotOrEllipsis();
            case '+' -> scanPlus();
            case '-' -> scanMinus();
            case '*' -> scanStar();
            case '/' -> scanSlashOrComment();
            case '%' -> scanPercent();
            case '=' -> scanEquals();
            case '!' -> scanExclamation();
            case '<' -> scanLessThan();
            case '>' -> scanGreaterThan();
            case '&' -> scanAmpersand();
            case '|' -> scanPipe();
            case '^' -> scanCaret();

            // String literals
            case '"' -> scanStringLiteral();
            case '\'' -> scanCharacterLiteral();

            // Numbers
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> scanNumber();

            // Identifiers and keywords
            default -> {
                if (Character.isJavaIdentifierStart(ch)) {
                    yield scanIdentifierOrKeyword();
                } else {
                    // Unknown character - create error token
                    ParseMetrics.recordParseError(false);
                    yield singleCharToken(TokenType.ERROR);
                }
            }
        };

        if (METRICS_ENABLED && startTime > 0) {
            ParseMetrics.recordTokenizationTime(System.nanoTime() - startTime, 1);
        }

        return token;
    }

    private void skipWhitespace() {
        while (position < length && Character.isWhitespace(source.charAt(position))) {
            if (source.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }

    private TokenInfo singleCharToken(TokenType type) {
        char ch = source.charAt(position);
        position++;
        column++;
        return createToken(type, position - 1, 1, String.valueOf(ch));
    }

    private TokenInfo scanDotOrEllipsis() {
        int start = position;
        position++; // consume '.'
        column++;

        if (position < length - 1 &&
            source.charAt(position) == '.' &&
            source.charAt(position + 1) == '.') {
            position += 2; // consume '..'
            column += 2;
            return createToken(TokenType.ELLIPSIS, start, 3, "...");
        }

        return createToken(TokenType.DOT, start, 1, ".");
    }

    private TokenInfo scanPlus() {
        int start = position;
        position++; // consume '+'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '+') {
                position++;
                column++;
                return createToken(TokenType.INCREMENT, start, 2, "++");
            } else if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.PLUS_ASSIGN, start, 2, "+=");
            }
        }

        return createToken(TokenType.PLUS, start, 1, "+");
    }

    private TokenInfo scanMinus() {
        int start = position;
        position++; // consume '-'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '-') {
                position++;
                column++;
                return createToken(TokenType.DECREMENT, start, 2, "--");
            } else if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.MINUS_ASSIGN, start, 2, "-=");
            } else if (next == '>') {
                position++;
                column++;
                return createToken(TokenType.ARROW, start, 2, "->");
            }
        }

        return createToken(TokenType.MINUS, start, 1, "-");
    }

    private TokenInfo scanStar() {
        int start = position;
        position++; // consume '*'
        column++;

        if (position < length && source.charAt(position) == '=') {
            position++;
            column++;
            return createToken(TokenType.MULT_ASSIGN, start, 2, "*=");
        }

        return createToken(TokenType.MULT, start, 1, "*");
    }

    private TokenInfo scanSlashOrComment() {
        int start = position;
        position++; // consume '/'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '/') {
                return scanLineComment(start);
            } else if (next == '*') {
                return scanBlockComment(start);
            } else if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.DIV_ASSIGN, start, 2, "/=");
            }
        }

        return createToken(TokenType.DIV, start, 1, "/");
    }

    private TokenInfo scanLineComment(int start) {
        // Skip until end of line
        while (position < length && source.charAt(position) != '\n') {
            position++;
            column++;
        }

        String text = source.substring(start, position);
        return createToken(TokenType.LINE_COMMENT, start, text.length(), text);
    }

    private TokenInfo scanBlockComment(int start) {
        position++; // consume '*'
        column++;

        boolean isJavadoc = position < length && source.charAt(position) == '*';

        // Scan until */
        while (position < length - 1) {
            if (source.charAt(position) == '*' && source.charAt(position + 1) == '/') {
                position += 2; // consume '*/'
                column += 2;
                break;
            }

            if (source.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }

        String text = source.substring(start, position);
        TokenType type = isJavadoc ? TokenType.JAVADOC_COMMENT : TokenType.BLOCK_COMMENT;
        return createToken(type, start, text.length(), text);
    }

    private TokenInfo scanPercent() {
        int start = position;
        position++; // consume '%'
        column++;

        if (position < length && source.charAt(position) == '=') {
            position++;
            column++;
            return createToken(TokenType.MOD_ASSIGN, start, 2, "%=");
        }

        return createToken(TokenType.MOD, start, 1, "%");
    }

    private TokenInfo scanEquals() {
        int start = position;
        position++; // consume '='
        column++;

        if (position < length && source.charAt(position) == '=') {
            position++;
            column++;
            return createToken(TokenType.EQ, start, 2, "==");
        }

        return createToken(TokenType.ASSIGN, start, 1, "=");
    }

    private TokenInfo scanExclamation() {
        int start = position;
        position++; // consume '!'
        column++;

        if (position < length && source.charAt(position) == '=') {
            position++;
            column++;
            return createToken(TokenType.NE, start, 2, "!=");
        }

        return createToken(TokenType.LOGICAL_NOT, start, 1, "!");
    }

    private TokenInfo scanLessThan() {
        int start = position;
        position++; // consume '<'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.LE, start, 2, "<=");
            } else if (next == '<') {
                position++;
                column++;
                if (position < length && source.charAt(position) == '=') {
                    position++;
                    column++;
                    return createToken(TokenType.LSHIFT_ASSIGN, start, 3, "<<=");
                }
                return createToken(TokenType.LSHIFT, start, 2, "<<");
            }
        }

        return createToken(TokenType.LT, start, 1, "<");
    }

    private TokenInfo scanGreaterThan() {
        int start = position;
        position++; // consume '>'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.GE, start, 2, ">=");
            } else if (next == '>') {
                position++;
                column++;
                if (position < length) {
                    char next2 = source.charAt(position);
                    if (next2 == '>') {
                        position++;
                        column++;
                        if (position < length && source.charAt(position) == '=') {
                            position++;
                            column++;
                            return createToken(TokenType.URSHIFT_ASSIGN, start, 4, ">>>=");
                        }
                        return createToken(TokenType.URSHIFT, start, 3, ">>>");
                    } else if (next2 == '=') {
                        position++;
                        column++;
                        return createToken(TokenType.RSHIFT_ASSIGN, start, 3, ">>=");
                    }
                }
                return createToken(TokenType.RSHIFT, start, 2, ">>");
            }
        }

        return createToken(TokenType.GT, start, 1, ">");
    }

    private TokenInfo scanAmpersand() {
        int start = position;
        position++; // consume '&'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '&') {
                position++;
                column++;
                return createToken(TokenType.LOGICAL_AND, start, 2, "&&");
            } else if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.AND_ASSIGN, start, 2, "&=");
            }
        }

        return createToken(TokenType.BITWISE_AND, start, 1, "&");
    }

    private TokenInfo scanPipe() {
        int start = position;
        position++; // consume '|'
        column++;

        if (position < length) {
            char next = source.charAt(position);
            if (next == '|') {
                position++;
                column++;
                return createToken(TokenType.LOGICAL_OR, start, 2, "||");
            } else if (next == '=') {
                position++;
                column++;
                return createToken(TokenType.OR_ASSIGN, start, 2, "|=");
            }
        }

        return createToken(TokenType.BITWISE_OR, start, 1, "|");
    }

    private TokenInfo scanCaret() {
        int start = position;
        position++; // consume '^'
        column++;

        if (position < length && source.charAt(position) == '=') {
            position++;
            column++;
            return createToken(TokenType.XOR_ASSIGN, start, 2, "^=");
        }

        return createToken(TokenType.BITWISE_XOR, start, 1, "^");
    }

    private TokenInfo scanStringLiteral() {
        int start = position;
        position++; // consume opening quote
        column++;

        StringBuilder sb = new StringBuilder();
        sb.append('"');

        while (position < length) {
            char ch = source.charAt(position);

            if (ch == '"') {
                position++; // consume closing quote
                column++;
                sb.append('"');
                break;
            } else if (ch == '\\') {
                // Handle escape sequences
                sb.append(ch);
                position++;
                column++;
                if (position < length) {
                    sb.append(source.charAt(position));
                    position++;
                    column++;
                }
            } else {
                sb.append(ch);
                if (ch == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                position++;
            }
        }

        return createToken(TokenType.STRING_LITERAL, start, sb.length(), sb.toString());
    }

    private TokenInfo scanCharacterLiteral() {
        int start = position;
        position++; // consume opening quote
        column++;

        StringBuilder sb = new StringBuilder();
        sb.append('\'');

        while (position < length && source.charAt(position) != '\'') {
            char ch = source.charAt(position);
            sb.append(ch);

            if (ch == '\\' && position + 1 < length) {
                position++;
                column++;
                sb.append(source.charAt(position));
            }

            position++;
            column++;
        }

        if (position < length) {
            position++; // consume closing quote
            column++;
            sb.append('\'');
        }

        return createToken(TokenType.CHARACTER_LITERAL, start, sb.length(), sb.toString());
    }

    private TokenInfo scanNumber() {
        int start = position;

        // Simple number scanning (full implementation would handle all Java number formats)
        while (position < length && Character.isDigit(source.charAt(position))) {
            position++;
            column++;
        }

        // Check for long suffix
        if (position < length && (source.charAt(position) == 'L' || source.charAt(position) == 'l')) {
            position++;
            column++;
            String text = source.substring(start, position);
            return createToken(TokenType.LONG_LITERAL, start, text.length(), text);
        }

        String text = source.substring(start, position);
        return createToken(TokenType.INTEGER_LITERAL, start, text.length(), text);
    }

    private TokenInfo scanIdentifierOrKeyword() {
        int start = position;

        // Scan identifier characters
        while (position < length && Character.isJavaIdentifierPart(source.charAt(position))) {
            position++;
            column++;
        }

        String text = source.substring(start, position);
        TokenType type = getKeywordType(text);

        return createToken(type, start, text.length(), text);
    }

    private TokenType getKeywordType(String text) {
        return switch (text) {
            case "abstract" -> TokenType.ABSTRACT;
            case "assert" -> TokenType.ASSERT;
            case "boolean" -> TokenType.BOOLEAN;
            case "break" -> TokenType.BREAK;
            case "byte" -> TokenType.BYTE;
            case "case" -> TokenType.CASE;
            case "catch" -> TokenType.CATCH;
            case "char" -> TokenType.CHAR;
            case "class" -> TokenType.CLASS;
            case "const" -> TokenType.CONST;
            case "continue" -> TokenType.CONTINUE;
            case "default" -> TokenType.DEFAULT;
            case "do" -> TokenType.DO;
            case "double" -> TokenType.DOUBLE;
            case "else" -> TokenType.ELSE;
            case "enum" -> TokenType.ENUM;
            case "extends" -> TokenType.EXTENDS;
            case "final" -> TokenType.FINAL;
            case "finally" -> TokenType.FINALLY;
            case "float" -> TokenType.FLOAT;
            case "for" -> TokenType.FOR;
            case "goto" -> TokenType.GOTO;
            case "if" -> TokenType.IF;
            case "implements" -> TokenType.IMPLEMENTS;
            case "import" -> TokenType.IMPORT;
            case "instanceof" -> TokenType.INSTANCEOF;
            case "int" -> TokenType.INT;
            case "interface" -> TokenType.INTERFACE;
            case "long" -> TokenType.LONG;
            case "native" -> TokenType.NATIVE;
            case "new" -> TokenType.NEW;
            case "package" -> TokenType.PACKAGE;
            case "private" -> TokenType.PRIVATE;
            case "protected" -> TokenType.PROTECTED;
            case "public" -> TokenType.PUBLIC;
            case "return" -> TokenType.RETURN;
            case "short" -> TokenType.SHORT;
            case "static" -> TokenType.STATIC;
            case "strictfp" -> TokenType.STRICTFP;
            case "super" -> TokenType.SUPER;
            case "switch" -> TokenType.SWITCH;
            case "synchronized" -> TokenType.SYNCHRONIZED;
            case "this" -> TokenType.THIS;
            case "throw" -> TokenType.THROW;
            case "throws" -> TokenType.THROWS;
            case "transient" -> TokenType.TRANSIENT;
            case "try" -> TokenType.TRY;
            case "void" -> TokenType.VOID;
            case "volatile" -> TokenType.VOLATILE;
            case "while" -> TokenType.WHILE;

            // JDK 9+ module keywords
            case "module" -> TokenType.MODULE;
            case "requires" -> TokenType.REQUIRES;
            case "exports" -> TokenType.EXPORTS;
            case "opens" -> TokenType.OPENS;
            case "to" -> TokenType.TO;
            case "uses" -> TokenType.USES;
            case "provides" -> TokenType.PROVIDES;
            case "with" -> TokenType.WITH;
            case "transitive" -> TokenType.TRANSITIVE;

            // JDK 10+ var (contextual keyword)
            case "var" -> TokenType.VAR;

            // JDK 14+ yield
            case "yield" -> TokenType.YIELD;

            // JDK 16+ record
            case "record" -> TokenType.RECORD;

            // JDK 17+ sealed classes
            case "sealed" -> TokenType.SEALED;
            case "non-sealed" -> TokenType.NON_SEALED;
            case "permits" -> TokenType.PERMITS;

            // JDK 21+ when (pattern matching)
            case "when" -> TokenType.WHEN;

            // Literals
            case "true", "false" -> TokenType.BOOLEAN_LITERAL;
            case "null" -> TokenType.NULL_LITERAL;

            default -> TokenType.IDENTIFIER;
        };
    }

    private TokenInfo createToken(TokenType type, int start, int length, String text) {
        return new TokenInfo(type, start, length, text);
    }

    private static final boolean METRICS_ENABLED =
        Boolean.parseBoolean(System.getProperty("styler.metrics.enabled", "false"));
}
package com.ruoyi.mes.base.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单四则运算表达式求值器。 支持 + - * / 和括号，纯 BigDecimal 运算。 不支持负数、科学计数法、函数调用。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
public class ArithmeticEvaluator {

    private static final MathContext INTERNAL_CONTEXT = MathContext.DECIMAL128;

    /**
     * 对外唯一入口：接收纯数字表达式，返回计算结果。
     *
     * @param expr 如 "0.5 * 1.2 * 100" 或 "(1.0 - 0.02) * 0.5 * 0.15"
     */
    public BigDecimal evaluate(String expr) {
        if (expr == null || expr.isBlank()) {
            return BigDecimal.ZERO;
        }
        List<Token> tokens = tokenize(expr.trim());
        if (tokens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return parseExpression(new TokenStream(tokens));
    }

    // ---- lexer ----

    private List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char ch = expr.charAt(i);
            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '(' || ch == ')') {
                tokens.add(new Token(Kind.OP, String.valueOf(ch)));
                i++;
            } else if (Character.isDigit(ch) || ch == '.') {
                int start = i;
                while (i < expr.length()
                        && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(new Token(Kind.NUM, expr.substring(start, i)));
            } else {
                throw new IllegalArgumentException("表达式包含非法字符: '" + ch + "' at position " + i);
            }
        }
        tokens.add(new Token(Kind.EOF, ""));
        return tokens;
    }

    // ---- recursive-descent parser (additive > multiplicative > primary) ----

    private BigDecimal parseExpression(TokenStream ts) {
        BigDecimal left = parseTerm(ts);
        while (ts.peek().is("+", "-")) {
            String op = ts.next().value;
            BigDecimal right = parseTerm(ts);
            left =
                    op.equals("+")
                            ? left.add(right, INTERNAL_CONTEXT)
                            : left.subtract(right, INTERNAL_CONTEXT);
        }
        return left;
    }

    private BigDecimal parseTerm(TokenStream ts) {
        BigDecimal left = parseFactor(ts);
        while (ts.peek().is("*", "/")) {
            String op = ts.next().value;
            BigDecimal right = parseFactor(ts);
            if (op.equals("*")) {
                left = left.multiply(right, INTERNAL_CONTEXT);
            } else {
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("除以零");
                }
                left = left.divide(right, INTERNAL_CONTEXT);
            }
        }
        return left;
    }

    private BigDecimal parseFactor(TokenStream ts) {
        if (ts.peek().is("(")) {
            ts.next(); // skip '('
            BigDecimal val = parseExpression(ts);
            ts.expect(")");
            return val;
        }
        ts.expect(Kind.NUM);
        return new BigDecimal(ts.next().value);
    }

    // ---- internal types ----

    enum Kind {
        NUM,
        OP,
        EOF
    }

    static class Token {
        Kind kind;
        String value;

        Token(Kind kind, String value) {
            this.kind = kind;
            this.value = value;
        }

        boolean is(String... vals) {
            if (kind != Kind.OP && kind != Kind.EOF) return false;
            for (String v : vals) if (value.equals(v)) return true;
            return false;
        }
    }

    static class TokenStream {
        List<Token> tokens;
        int pos;

        TokenStream(List<Token> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        Token peek() {
            return tokens.get(pos);
        }

        Token next() {
            return tokens.get(pos++);
        }

        void expect(String op) {
            if (!peek().is(op))
                throw new IllegalArgumentException("期望 '" + op + "', 实际: " + peek().value);
            next();
        }

        void expect(Kind kind) {
            if (peek().kind != kind)
                throw new IllegalArgumentException("期望数字, 实际: " + peek().value);
        }
    }
}

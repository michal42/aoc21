package com.company;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Day18 extends Puzzle {
    public Day18(List<String> lines) {
        super(lines);
    }

    private static class SnailFishNumber {
        private static class Token {
            public static final int LEFT_BRACKET = 1;
            public static final int RIGTH_BRACKET = 2;
            public static final int COMMA = 3;
            public static final int LITERAL = 4;

            public Token(int type) {
                if (type == LITERAL) {
                    throw new IllegalArgumentException();
                }
                this.type = type;
                this.value = 0;
            }

            public Token(int type, int value) {
                if (type != LITERAL) {
                    throw new IllegalArgumentException();
                }
                this.type = type;
                this.value = value;
            }

            public void add(Token rhs) {
                value += rhs.value;
            }

            private final int type;
            private int value;

            public int getType() {
                return type;
            }

            public int getValue() {
                return value;
            }

            public static Token fromChar(int c) {
                if (c == '[') {
                    return new Token(LEFT_BRACKET);
                } else if (c == ']') {
                    return new Token(RIGTH_BRACKET);
                } else if (c == ',') {
                    return new Token(COMMA);
                } else if (c >= '0' && c <= '9') {
                    return new Token(LITERAL, c - '0');
                }
                throw new IllegalArgumentException();
            }

            @Override
            public String toString() {
                return switch (type) {
                    case LEFT_BRACKET -> "[";
                    case RIGTH_BRACKET -> "]";
                    case COMMA -> ",";
                    case LITERAL -> String.valueOf(value);
                    default -> "?";
                };
            }
        }

        private static class Node {
            public long value;
            public List<Node> children;

            public static Node fromToken(Token t) {
                Node res = new Node();
                if (t.getType() == Token.LEFT_BRACKET) {
                    res.children = new ArrayList<>();
                    res.value = 0;
                } else if (t.getType() == Token.LITERAL) {
                    res.children = Collections.emptyList();
                    res.value = t.getValue();
                } else {
                    throw new IllegalArgumentException();
                }
                return res;
            }
        }

        public static final SnailFishNumber ZERO = new SnailFishNumber();

        public SnailFishNumber() {
            tokens = new LinkedList<>();
        }

        public SnailFishNumber(String str) {
            tokens = str.chars().mapToObj(Token::fromChar).collect(Collectors.toCollection(LinkedList::new));
        }

        @Override
        public String toString() {
            return tokens.stream().map(Token::toString).collect(Collectors.joining());
        }

        public SnailFishNumber add(SnailFishNumber rhs) {
            if (tokens.isEmpty() || rhs.tokens.isEmpty()) {
                tokens.addAll(rhs.tokens);
            } else {
                var leftTokens = tokens;
                tokens = new LinkedList<>();
                tokens.add(new Token(Token.LEFT_BRACKET));
                tokens.addAll(leftTokens);
                tokens.add(new Token(Token.COMMA));
                tokens.addAll(rhs.tokens);
                tokens.add(new Token(Token.RIGTH_BRACKET));
                reduce();
            }
            return this;
        }

        public long getMagnitude() {
            var root = buildTree();
            return getMagnitude(root);
        }

        private static long getMagnitude(Node n) {
            if (n.children.isEmpty()) {
                return n.value;
            }
            return 3 * getMagnitude(n.children.get(0)) + 2 * getMagnitude(n.children.get(1));
        }

        private Node buildTree() {
            Deque<Node> stack = new ArrayDeque<>();
            var it = tokens.listIterator();
            Node root = Node.fromToken(it.next());
            stack.addFirst(root);
            while (it.hasNext()) {
                var cur = it.next();
                var type = cur.getType();
                if (type == Token.LEFT_BRACKET || type == Token.LITERAL) {
                    var n = Node.fromToken(cur);
                    stack.getFirst().children.add(n);
                    if (type == Token.LEFT_BRACKET) {
                        stack.addFirst(n);
                    }
                } else if (type == Token.RIGTH_BRACKET) {
                    stack.removeFirst();
                }
            }
            assert stack.isEmpty();
            return root;
        }

        private void reduce() {
            while (true) {
                while (doOneExplosion()) {}
                if (!doOneSplit()) {
                    return;
                }
            }
        }

        private boolean doOneExplosion() {
            var it = tokens.listIterator();
            Token lastLiteral = null;
            Token carryOver = null;
            int depth = 0;
            while (it.hasNext()) {
                var cur = it.next();
                if (cur.getType() == Token.LEFT_BRACKET) {
                    // Only to a single explosion at once
                    if (++depth > 4 && carryOver == null) {
                        // [ -> 0
                        it.remove();
                        it.add(new Token(Token.LITERAL, 0));
                        // Left number
                        cur = it.next();
                        assert cur.getType() == Token.LITERAL;
                        if (lastLiteral != null) {
                            lastLiteral.add(cur);
                        }
                        it.remove();
                        // ,
                        cur = it.next();
                        assert cur.getType() == Token.COMMA;
                        it.remove();
                        // Right number
                        carryOver = it.next();
                        assert carryOver.getType() == Token.LITERAL;
                        it.remove();
                        // ]
                        cur = it.next();
                        assert cur.getType() == Token.RIGTH_BRACKET;
                        it.remove();
                        depth--;
                    }
                } else if (cur.getType() == Token.RIGTH_BRACKET) {
                    depth--;
                } else if (cur.getType() == Token.LITERAL) {
                    if (carryOver != null) {
                        cur.add(carryOver);
                        return true;
                    } else {
                        lastLiteral = cur;
                    }
                }
            }
            return carryOver != null;
        }

        private boolean doOneSplit() {
            var it = tokens.listIterator();
            while (it.hasNext()) {
                var cur = it.next();
                if (cur.getType() != Token.LITERAL || cur.getValue() < 10) {
                    continue;
                }
                int left = cur.getValue() / 2;
                int right = cur.getValue() - left;
                it.remove();
                it.add(new Token(Token.LEFT_BRACKET));
                it.add(new Token(Token.LITERAL, left));
                it.add(new Token(Token.COMMA));
                it.add(new Token(Token.LITERAL, right));
                it.add(new Token(Token.RIGTH_BRACKET));
                return true;
            }
            return false;
        }

        private List<Token> tokens;
    }

    @Override
    public long runPartOne() {
        return lines.stream().map(SnailFishNumber::new).reduce(SnailFishNumber.ZERO, SnailFishNumber::add).getMagnitude();
    }

    @Override
    public long runPartTwo() {
        return IntStream.range(0, lines.size()).mapToLong(a ->
            IntStream.range(0, lines.size()).filter(b -> a != b).mapToLong(b ->
                // XXX: Need to parse the lines each time, since SnailFishNumber.add is not creating an independent clone
                new SnailFishNumber(lines.get(a)).add(new SnailFishNumber(lines.get(b))).getMagnitude()
            ).max().getAsLong()
        ).max().getAsLong();
    }
}

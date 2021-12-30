package com.company;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.ToLongBiFunction;
import java.util.stream.Collectors;

public class Day16 extends Puzzle {
    public Day16(List<String> lines) {
        super(lines);
    }

    private static class Packet {
        public static final int LENGTH_TYPE_FIXED = 0;
        public static final int LENGTH_TYPE_PACKETS = 1;

        public static final int TYPE_SUM = 0;
        public static final int TYPE_PRODUCT = 1;
        public static final int TYPE_MIN = 2;
        public static final int TYPE_MAX = 3;
        public static final int TYPE_LITERAL = 4;
        public static final int TYPE_GT = 5;
        public static final int TYPE_LT = 6;
        public static final int TYPE_EQ = 7;

        public static final int VERSION_BITS = 3;
        public static final int TYPE_BITS = 3;
        public static final int LENGTH_FIXED_BITS = 15;
        public static final int LENGTH_PACKETS_BITS = 11;
        public static final int INT_GROUP_BITS = 5;
        public static final int INT_GROUP_VALUE_BITS = INT_GROUP_BITS - 1;
        public static final int INT_GROUP_VALUE_MASK = (1 << INT_GROUP_VALUE_BITS) - 1;
        public static final int INT_GROUP_CONTINUE = 1 << INT_GROUP_VALUE_BITS;

        public int version;
        public int type;
        public int lengthType;
        // TYPE_LITERAL: Literal value
        // Other types: length
        //   LENGTH_TYPE_FIXED: Cursor in the bitstream to stop at (exclusive)
        //   LENGTH_TYPE_PACKETS: Number of packets to read
        public long value;
        List<Packet> operands;
    }

    private static Packet parsePackets(String hex) {
        var bs = new BitStream(hex);
        Deque<Packet> stack = new ArrayDeque<>();

        var root = parseOnePacket(bs);
        if (root.type == Packet.TYPE_LITERAL) {
            return root;
        }
        stack.addFirst(root);
        while (true) {
            var parent = stack.getFirst();
            var packet = parseOnePacket(bs);
            parent.operands.add(packet);
            if (packet.type == Packet.TYPE_LITERAL) {
                while (parent.lengthType == Packet.LENGTH_TYPE_FIXED && parent.value == bs.getBitCursor() ||
                       parent.lengthType == Packet.LENGTH_TYPE_PACKETS && parent.value == parent.operands.size()) {
                    stack.removeFirst();
                    if (stack.isEmpty()) {
                        return root;
                    }
                    parent = stack.getFirst();
                }
            } else {
                stack.addFirst(packet);
            }
        }
    }

    private static Packet parseOnePacket(BitStream bs) {
        var p = new Packet();
        p.version = bs.getBits(Packet.VERSION_BITS);
        p.type = bs.getBits(Packet.TYPE_BITS);
        if (p.type == Packet.TYPE_LITERAL) {
            long group;
            do {
                group = bs.getBits(Packet.INT_GROUP_BITS);
                p.value <<= Packet.INT_GROUP_VALUE_BITS;
                p.value |= group & Packet.INT_GROUP_VALUE_MASK;
            } while ((group & Packet.INT_GROUP_CONTINUE) > 0);
            return p;
        }
        p.lengthType = bs.getBits(1);
        if (p.lengthType == Packet.LENGTH_TYPE_FIXED) {
            p.value = bs.getBits(Packet.LENGTH_FIXED_BITS);
            p.value += bs.getBitCursor();
        } else {
            p.value = bs.getBits(Packet.LENGTH_PACKETS_BITS);
        }
        p.operands = new ArrayList<>();
        return p;
    }

    private static class BitStream {
        public BitStream(String hex) {
            this.hex = hex;
        }

        public int getBits(int bits) {
            int res = 0;
            while (bits > 0) {
                if (bitsAvail == 0) {
                    nextByte();
                }
                int step = Math.min(bits, bitsAvail);
                res <<= step;
                // step = 2, bitsAvail = 6: 01234567  ->  0123 & 3 -> 23
                //                            ^^
                res |= (curByte >> (bitsAvail - step)) & ((1 << step) - 1);
                bitsAvail -= step;
                bits -= step;
                //System.out.println("curbyte: " + Integer.toBinaryString(curByte) + " step: " + step + " -> " + Long.toBinaryString(res));
            }
            return res;
        }

        public int getBitCursor() {
            return hexCursor * 4 - bitsAvail;
        }

        private void nextByte() {
            curByte = Integer.parseInt(hex.substring(hexCursor, hexCursor + 2), 16);
            bitsAvail = 8;
            hexCursor += 2;
        }
        private int hexCursor;
        private int bitsAvail;
        private int curByte;
        private final String hex;
    }

    private static void testHex(String hex, List<Integer> steps) {
        System.out.println(hex);
        var bs = new BitStream(hex);
        for (var s : steps) {
            long b = bs.getBits(s);
            System.out.println(s + ": " + b + " / " + String.format("%" + s + "s", Long.toBinaryString(b)).replace(' ', '0'));
        }
        var p = parseOnePacket(new BitStream(hex));
        System.out.println("packet: " + p.version + " / " + p.type + " / " + p.value);
    }

    private static long evaluatePackets(Packet packet, ToLongBiFunction<Packet, List<Long>> calculator) {
        if (packet.operands == null) {
            return calculator.applyAsLong(packet, Collections.EMPTY_LIST);
        } else {
            return calculator.applyAsLong(packet, packet.operands.stream().mapToLong(p -> evaluatePackets(p, calculator)).boxed().collect(Collectors.toList()));
        }
    }

    private static long printPackets(Packet packet, List<Long> operands) {
        System.out.println("version: " + packet.version + " type: " + packet.type + " value: " + packet.value);
        return 0;
    }

    private static long sumVersions(Packet packet, List<Long> operands) {
        return packet.version + operands.stream().mapToLong(Long::longValue).sum();
    }

    private static long calculateExpression(Packet packet, List<Long> operands) {
        return switch (packet.type) {
            case Packet.TYPE_LITERAL -> packet.value;
            case Packet.TYPE_GT -> operands.get(0) > operands.get(1) ? 1 : 0;
            case Packet.TYPE_LT -> operands.get(0) < operands.get(1) ? 1 : 0;
            case Packet.TYPE_EQ -> operands.get(0).equals(operands.get(1)) ? 1 : 0;
            default -> {
                var stream = operands.stream().mapToLong(Long::longValue);
                yield switch (packet.type) {
                    case Packet.TYPE_SUM -> stream.sum();
                    case Packet.TYPE_PRODUCT -> stream.reduce(1, Math::multiplyExact);
                    case Packet.TYPE_MIN -> stream.min().getAsLong();
                    case Packet.TYPE_MAX -> stream.max().getAsLong();
                    default -> throw new IllegalArgumentException("Unknown packet type");
                };
            }
        };
    }

    @Override
    public long runPartOne() {
        var root = parsePackets(lines.get(0));
        return evaluatePackets(root, Day16::sumVersions);
    }

    @Override
    public long runPartTwo() {
        var root = parsePackets(lines.get(0));
        return evaluatePackets(root, Day16::calculateExpression);
    }
}

package P2PBC.Chord;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class represent either a {@link Node} in the Chord network or a key,
 * and provides the basic modular arithmetic operations that will be used in
 * the Chord routing algorithm.
 */
public class Identifier implements Comparable<Identifier> {
    private static int bits = 8;
    private static BigInteger base = BigInteger.ONE.shiftLeft(bits);
    private static MessageDigest digest;
    private final BigInteger id;

    static {
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ignore) {}
    }

    /**
     * Sets the number of bits of each {@link Identifier}. Every sequent
     * arithmetical operation will be done in modulo 2^{@code length}.
     *
     * @param length the new bit length.
     */
    public static void setBitLength(int length) {
        bits = length;
        base = BigInteger.ONE.shiftLeft(bits);
    }

    /**
     * Returns the current bit length.
     *
     * @return the current bit length.
     */
    public static int getBitLength() {
        return bits;
    }

    /**
     * Creates a new {@link Identifier} by the given {@link BigInteger}.
     *
     * @param bigInteger the value of the {@link Identifier}, eventually modulo
     *                   2^{@link Identifier#getBitLength()}.
     */
    public Identifier(BigInteger bigInteger) {
        id = bigInteger.mod(base);
    }

    /**
     * Generates a new {@link Identifier} by hashing the given {@code byte}
     * array using the SHA algorithm.
     *
     * @param bytes the byte array to be hashed.
     */
    public Identifier(byte[] bytes) {
        id = new BigInteger(1, digest.digest(bytes)).shiftRight(8*digest.getDigestLength() - bits);
    }

    /**
     * Performs a modular addition with the given {@link BigInteger}.
     *
     * @param bigInteger the operand of the modular addition.
     * @return a new {@link Identifier} with value {@code this} +
     * {@code bigInteger} mod 2^{@link Identifier#getBitLength()}.
     */
    public Identifier add(BigInteger bigInteger) {
        return new Identifier(id.add(bigInteger));
    }

    /**
     * Performs a modular addition with the given {@link Identifier}.
     *
     * @param identifier the operand of the modular addition.
     * @return a new {@link Identifier} with value {@code this} +
     * {@code identifier} mod 2^{@link Identifier#getBitLength()}.
     */
    public Identifier add(Identifier identifier) {
        return add(identifier.id);
    }

    /**
     * Performs a modular subtraction with the given {@link BigInteger}.
     *
     * @param bigInteger the operand of the modular subtraction.
     * @return a new {@link Identifier} with value {@code this} -
     * {@code bigInteger} mod 2^{@link Identifier#getBitLength()}.
     */
    public Identifier subtract(BigInteger bigInteger) {
        return new Identifier(id.subtract(bigInteger));
    }

    /**
     * Performs a modular subtraction with the given {@link Identifier}.
     *
     * @param identifier the operand of the modular subtraction.
     * @return a new {@link Identifier} with value {@code this} -
     * {@code identifier} mod 2^{@link Identifier#getBitLength()}.
     */
    public Identifier subtract(Identifier identifier) {
        return subtract(identifier.id);
    }

    /**
     * Converts the {@link Identifier} to {@code int}. If this
     * {@link Identifier} is too big to fit in an int, only the low-order 32
     * bits are returned.
     *
     * @return the {@link Identifier} converted to {@code int}.
     */
    public int getValue() {
        return id.intValue();
    }

    /**
     * Checks if this {@link Identifier} is between {@code left} (excluded) and
     * {@code right} (included), i.e., if {@code 0 < left -  this <= left -
     * right mod 2^}{@link Identifier#getBitLength()}.
     *
     * @param left the left bound of the interval, excluded.
     * @param right the right bound of the interval, included.
     * @return {@code true} if {@code 0 < left - this <= left -  right mod 2^}
     * {@link Identifier#getBitLength()}, {@code false}
     * otherwise.
     */
    public boolean isBetween(Identifier left, Identifier right) {
        Identifier idGap = this.subtract(left);
        Identifier rightGap = right.subtract(left);

        return idGap.id.compareTo(BigInteger.ZERO) > 0 && idGap.compareTo(rightGap) <= 0;
    }

    /**
     * Returns the hashcode of the  {@link Identifier}.
     *
     * @return the computed hashcode.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Test whether two {@link Identifier}s are equal.
     *
     * @param obj the {@link Identifier} to be compared.
     * @return {@code true} if {@code obj} is an {@link Identifier} and has the
     * same value of {@code this}, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identifier)
            return id.equals(((Identifier) obj).id);

        return false;
    }

    /**
     * Compares this {@link Identifier} with {@code identifier}.
     *
     * @param identifier the {@link Identifier} to be compared.
     * @return a negative integer, zero, or a positive integer as this
     * {@link Identifier} is less than, equal to, or greater than the specified
     * {@code identifier}.
     */
    @Override
    public int compareTo(Identifier identifier) {
        return id.compareTo(identifier.id);
    }

    /**
     * Returns a string representation of the {@link Identifier}.
     *
     * @return a string representation of the {@link Identifier}.
     */
    @Override
    public String toString() {
        return id.toString();
    }
}

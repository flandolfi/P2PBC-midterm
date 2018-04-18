package P2PBC.Chord;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static void setBitLength(int length) {
        bits = length;
        base = BigInteger.ONE.shiftLeft(bits);
    }

    public static int getBitLength() {
        return bits;
    }

    public Identifier(BigInteger bigInteger) {
        id = bigInteger.mod(base);
    }

    public Identifier(byte[] bytes) {
        id = new BigInteger(1, digest.digest(bytes)).shiftRight(8*digest.getDigestLength() - bits);
    }

    public Identifier add(BigInteger bigInteger) {
        return new Identifier(id.add(bigInteger));
    }

    public Identifier add(Identifier identifier) {
        return add(identifier.id);
    }

    public Identifier subtract(BigInteger bigInteger) {
        return new Identifier(id.subtract(bigInteger));
    }

    public Identifier subtract(Identifier identifier) {
        return subtract(identifier.id);
    }

    public int getValue() {
        return id.intValue();
    }

    public boolean isBetween(Identifier left, Identifier right) {
        if (left.id.equals(right.id))
            return false;

        if (left.id.compareTo(right.id) < 0) {
            return left.id.compareTo(id) < 0 && id.compareTo(right.id) <= 0;
        }

        return isBetween(left, new Identifier(base.subtract(BigInteger.ONE)))
                || isBetween(new Identifier(BigInteger.ZERO), right);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identifier)
            return id.equals(((Identifier) obj).id);

        return false;
    }

    @Override
    public int compareTo(Identifier identifier) {
        return id.compareTo(identifier.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}

package Main;
import java.util.Arrays;

public record ByteArrayWrapper(byte[] data) {
    public ByteArrayWrapper {
        if (data == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArrayWrapper)) {
            return false;
        }
        return Arrays.equals(data, ((ByteArrayWrapper) other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}

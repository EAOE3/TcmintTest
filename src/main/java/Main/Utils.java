package Main;

import org.bouncycastle.util.encoders.Hex;

public class Utils {

    public static byte[] hexStringToByteArray(String s) {
        // Remove the "0x" prefix if it exists
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }

        return Hex.decode(s);
    }

}

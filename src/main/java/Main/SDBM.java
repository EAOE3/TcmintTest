package Main;

import Database.AES256;
import Database.FM;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SDBM {

    private static final String rootPath = "staticDatabase/";
    private static final String rootEncryptionKey = "kjughfkjwer" + Settings.haha;

    public static boolean store(String valueName, Object value) {
        if(value instanceof Number) {
            store(valueName, (Number) value);
        }
        else if(value instanceof String) {
            store(valueName, value.toString());
        }
        else if (value instanceof byte[]){
        	store(valueName, (byte[]) value);
        }

        return true;
    }

    public static boolean store(String valueName, byte[] value) {
        String path = rootPath + valueName;
        String encryptionKey = rootEncryptionKey + valueName;

        try {
            byte[] encryptedData = AES256.encrypt(value, encryptionKey);
            FM.write(path, encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
    public static boolean store(String valueName, String value) {
        return store(valueName, value.getBytes());
    }

    public static boolean store(String valueName, Number value) {
    	return store(valueName, getByteArrayValue(value));
    }

    public static boolean store(String valueName, Boolean value) {
    	return store(valueName, new byte[] { value ? (byte)1 : (byte)0 });
    }

    public static byte[] load(String valueName) {
        String path = rootPath + valueName;
        String encryptionKey = rootEncryptionKey + valueName;

        try {
            byte[] encryptedData = FM.readBytes(path);
            byte[] decryptedData = AES256.decrypt(encryptedData, encryptionKey);

            return decryptedData;
        } catch (Exception e) {
           // e.printStackTrace();
            return null;
        }
    }

    public static String loadString(String valueName) {
        byte[] value = load(valueName);
        if(value == null) return null;
        
        return new String(value);
    }

    public static Short loadShort(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else
            return (short) getNumberValue(value);
    }

    public static Integer loadInt(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else {
            return (Integer) getNumberValue(value);
        }
    }

    public static double loadDouble(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else {
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getDouble(); // convert byte array to double
        }
    }

    public static boolean loadBoolean(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return false;
        else
            return value[0] != 0;
    }

    public static Long loadLong(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return Long.valueOf(0);

        return (long) getNumberValue(value);
    }

    public static BigInteger loadBigInt(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return BigInteger.ZERO;

        return new BigInteger(1, value); // convert byte array to BigInteger
        //The signum parameter is set to 1 to indicate that the BigInteger should be positive
    }

    public static BigDecimal loadBigDec(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return BigDecimal.ZERO;

        return (BigDecimal) getNumberValue(value);
    }

    // Returns creation time of the folder holding the data of this class/The
    // creation time of the class
    public static long getCreationTime() {
        return FM.getCreationTime(rootPath);
    }

    public static void delete(String valueName) {
        String path = rootPath + valueName;
        File file = new File(path);

        file.delete();
    }

    public static byte[] getByteArrayValue(Number value) {
        if (value instanceof Integer) {
            return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt((int)value).array();
        } else if (value instanceof Short) {
            return ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                    .putShort((short)value).array();
        } else if (value instanceof Long) {
            return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                    .putLong((long)value).array();
        } else if (value instanceof BigInteger) {
            byte[] unscaledByteArray = ((BigInteger)value).toByteArray();
            return ByteBuffer.allocate(unscaledByteArray.length + 4).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(0)
                    .put(unscaledByteArray)
                    .array();
        } else if (value instanceof BigDecimal) {
            BigInteger unscaledValue = ((BigDecimal)value).unscaledValue();
            byte[] unscaledByteArray = unscaledValue.toByteArray();
            return ByteBuffer.allocate(unscaledByteArray.length + 4).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(((BigDecimal)value).scale())
                    .put(unscaledByteArray)
                    .array();
        } else if (value instanceof Double) {
            return ByteBuffer.allocate(Double.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                    .putDouble((double)value).array();
        } else {
            throw new IllegalArgumentException("Unsupported numeric type: " + value.getClass());
        }
    }

    public static Number getNumberValue(byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);
        if (byteArray.length == Integer.BYTES) {
            return buffer.getInt();
        } else if (byteArray.length == Short.BYTES) {
            return buffer.getShort();
        } else if (byteArray.length == Long.BYTES) {
            return buffer.getLong();
        } else if (byteArray.length > 4) {
            int scale = buffer.getInt();
            byte[] unscaledByteArray = Arrays.copyOfRange(byteArray, 4, byteArray.length);
            BigInteger unscaledValue = new BigInteger(1, unscaledByteArray);
            return new BigDecimal(unscaledValue, scale);
        } else if (byteArray.length == Double.BYTES) {
            return buffer.getDouble();
        } else {
            throw new IllegalArgumentException("Invalid byte array length: " + byteArray.length);
        }
    }
}

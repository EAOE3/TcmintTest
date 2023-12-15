package Database;

import java.io.*;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;

import Database.Webhook.Sender;
import Main.Settings;

import java.nio.ByteOrder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class DBM {

    public final String id;
    public final String rootPath;
    private final String rootEncryptionKey;
    private final boolean encrypt;
    private final boolean backup;

    // If a class doesn't want a superName, it uses this constructor
    public DBM(String id, boolean encrypt, boolean backup) {
        this.id = id;
        this.encrypt = encrypt;
        this.backup = backup;

        rootPath = "database/" + this.getClass().getSimpleName() + "/" + id + "/";

        if(encrypt) {
            rootEncryptionKey = id + "/" + this.getClass().getSimpleName() + Settings.haha;
        } else {
            rootEncryptionKey = null;
        }
    }

    public boolean store(String valueName, Object value) {
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

    public boolean store(String valueName, byte[] value) {
        String path = rootPath + valueName;

        File file = new File(path);
        file.getParentFile().mkdirs();

        try {
            if (encrypt) {
                String encryptionKey = rootEncryptionKey + valueName;
                value = AES256.encrypt(value, encryptionKey);
            }

            Files.write(file.toPath(), value); // using Files.write for simplicity

            if(backup) {
                Sender.backupBytes(path, value);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean store(String valueName, String value) {
    	if(value == null) return true;
        return store(valueName, value.getBytes());
    }

    public boolean store(String valueName, Number value) {
    	if(value == null) return true;
    	return store(valueName, getByteArrayValue(value));
    }

    public boolean store(String valueName, Boolean value) {
    	if(value == null) return true;
    	return store(valueName, new byte[] { value ? (byte)1 : (byte)0 });
    }

    public byte[] load(String valueName) {
        String path = rootPath + valueName;
        File file = new File(path);
        if(!file.exists()) return null;

        try {
            byte[] data = readByteArrayFromFile(file.toPath());
            if(data == null) return null;

            if(encrypt) {
                String encryptionKey = rootEncryptionKey + valueName;
                data = AES256.decrypt(data, encryptionKey);
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public String loadString(String valueName) {
        byte[] value = load(valueName);
        if(value == null) return null;
        
        return new String(value);
    }

    public Short loadShort(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else
            return (short) getNumberValue(value);
    }

    public Integer loadInt(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else {
            return (Integer) getNumberValue(value);
        }
    }

    public double loadDouble(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return 0;
        else {
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getDouble(); // convert byte array to double
        }
    }

    public boolean loadBoolean(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return false;
        else
            return value[0] != 0;
    }

    public Long loadLong(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return Long.valueOf(0);

        return (long) getNumberValue(value);
    }

    public BigInteger loadBigInt(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return BigInteger.ZERO;

        return new BigInteger(1, value); // convert byte array to BigInteger
        //The signum parameter is set to 1 to indicate that the BigInteger should be positive
    }

    public BigDecimal loadBigDec(String valueName) {
        byte[] value = load(valueName);

        if (value == null)
            return BigDecimal.ZERO;

        return (BigDecimal) getNumberValue(value);
    }

    // Returns creation time of the folder holding the data of this class/The
    // creation time of the class
    public long getCreationTime() {
        Path path = Paths.get(rootPath);

        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.creationTime().toInstant().getEpochSecond();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void delete(String valueName) {
        String path = rootPath + valueName;
        File file = new File(path);

        file.delete();
    }

    public void deleteAll() {

        File folder = new File(rootPath);

        for (File file : folder.listFiles()) {
            file.delete();
        }

        folder.delete();
    }

    public static <T> void loadAllObjectsFromDatabase(Class<T> c) throws NoSuchMethodException, SecurityException {

        // Get the constructor object for the Person class that takes a String and an
        // int as arguments
        Constructor<T> constructor = c.getConstructor(String.class);

        String dirPath = ("database/" + c.getSimpleName());
        File dir = new File(dirPath);

        if (!dir.exists()) {
            System.out.println("No dir found for " + c.getSimpleName());
            return;
        }

        for (String key : dir.list()) {
            try {
                System.out.println("key: " + key);
                constructor.newInstance(key);
                System.out.println("Loaded " + c.getSimpleName() + " Object");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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

    public static byte[] readByteArrayFromFile(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
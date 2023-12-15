package Database.Webhook;

import Main.OHTTP;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Sender {
    private static final String backupServerUrl = "http://104.248.32.91:8080";

    public static void backupShort(String path, short value) {
        try {
            String url = backupServerUrl + "/storeShort/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void backupInt(String path, int value) {
        try {
            String url = backupServerUrl + "/storeInt/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupLong(String path, long value) {
        try {
            String url = backupServerUrl + "/storeLong/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupDouble(String path, double value) {
        try {
            String url = backupServerUrl + "/storeDouble/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupBigInt(String path, BigInteger value) {
        try {
            String url = backupServerUrl + "/storeBigInt/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupBigDec(String path, BigDecimal value) {
        try {
            String url = backupServerUrl + "/storeBigDec/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupBoolean(String path, Boolean value) {
        try {
            String url = backupServerUrl + "/storeBoolean/?path=" + path + "&value=" + value.toString();
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupString(String path, String value) {
        try {
            String url = backupServerUrl + "/storeString/?path=" + path + "&value=" + value;
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupBytes(String path, byte[] value) {
        try {
            String url = backupServerUrl + "/storeBytes/?path=" + path + "&value=" + Hex.toHexString(value);
            new Thread(() -> {
                try {
                    OHTTP.sendPostRequest(url, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

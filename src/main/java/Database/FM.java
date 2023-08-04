package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Scanner;

public class FM { // Files Manager

    public static void write(Object filePath, Object Content) throws Exception {
        generateRoute(filePath.toString());

        File file = new File(filePath.toString());
        PrintWriter writer = new PrintWriter(file);
        writer.print(Content.toString());
        writer.close();

    }

    public static void write(Object filePath, byte[] data) throws Exception {
        generateRoute(filePath.toString());

        Files.write(Paths.get(filePath.toString()), data);
    }

    public static byte[] readBytes(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();
        return bytes;
    }

    // Write any object to a file
    public static void writeObjectToFile(Object obj, String filename) {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
            objOut.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read an object from a file and return it
    public static Object readObjectFromFile(String filename) {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
            return objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void generateRoute(String file) throws Exception {
        File fileObject = new File(file);

        if (fileObject.exists()) return;

        String[] path = file.split("/");
        //System.out.println(path.length);
        int routeSize = path.length - 1;

        if (routeSize <= 0) {
            return;
        }

        String route = path[0];

        File directory = new File(route);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new Exception("Failed to create directory " + route);
            }
        }

        for (int t = 1; t < routeSize; ++t) {
            route += "/" + path[t];
            directory = new File(route);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new Exception("Failed to create directory " + route);
                }
            }
        }

    }

    public static void rename(File oldFile, File newFile) {

        oldFile.renameTo(newFile);
    }

    public static String readAll(File File) throws FileNotFoundException {

        StringBuffer sb = new StringBuffer();

        Scanner Reader = new Scanner(File);

        while (Reader.hasNext()) {
            sb.append(" " + Reader.next());
        }

        Reader.close();

        return sb.toString();

    }

    public static String readAll(String filePath) throws Exception {
        File file = new File(filePath);
        StringBuffer sb = new StringBuffer();

        Scanner Reader = new Scanner(file);

        while (Reader.hasNext()) {
            sb.append(" " + Reader.next());
        }

        Reader.close();

        return sb.toString();

    }

    public static long getCreationTime(String file) {
        Path filePath = Path.of(file);

        // Get the creation time attribute of the file
        BasicFileAttributes fileAttributes;
        try {
            fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
        } catch (IOException e) {
            return 0;
        }
        Instant creationTime = fileAttributes.creationTime().toInstant();
        return creationTime.getEpochSecond();
    }

}

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static final String SERVER = "192.168.5.137";
    private static final int PORT = 5000;
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(SERVER, PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String localFilePath = "/home/dominic/Downloads/firmware.bin";
            String remoteDirectory = "/switch/melonds";
            String fileName = "firmware.bin";

            File localFile = new File(localFilePath);
            FileInputStream inputStream = new FileInputStream(localFile);

            boolean uploaded = ftpClient.storeFile(remoteDirectory + "/" + fileName, inputStream);
            inputStream.close();

            if (uploaded) {
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("Failed to upload the file.");
            }

            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

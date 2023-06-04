import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FTPClientJ {

    public static void FTPClient(String sendFile, String RemoteDir, Boolean send) {
        String server = "192.168.5.118";
        int port = 21;
        String username = "dominichann";
        String password = "Hidom@123";
        String localFilePath = "C:\\users\\dooli\\Downloads\\rename.bat";
        String remoteDirectory = "/home/dominichann/";

        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(server, port);
            boolean loggedIn = ftpClient.login(username, password);

            if (loggedIn) {
                System.out.println("Logged in to FTP server.");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

                File localFile = new File(localFilePath);
                FileInputStream inputStream = new FileInputStream(localFile);

                boolean uploaded = ftpClient.storeFile(remoteDirectory + localFile.getName(), inputStream);
                inputStream.close();

                if (uploaded) {
                    System.out.println("File uploaded successfully.");
                } else {
                    System.out.println("Failed to upload the file.");
                }

                ftpClient.logout();
                ftpClient.disconnect();
            } else {
                System.out.println("Failed to log in to FTP server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

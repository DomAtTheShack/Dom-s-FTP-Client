import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FTPClientJ {
    private static FTPClient ftpClient = new FTPClient();


    public static boolean isConnected(){
        if(ftpClient.isConnected()){
            return true;
        }else {
            return false;
        }
    }
    public static boolean connect(String server,String user, String pass,int port){
        return true;
    }
    public static boolean FTPUpFile(String sendFile, String remoteDir) {
        String server = "domserver.xyz";
        int port = 21;
        String username = "dominichann";
        String password = "Hidom@123";
        String localFilePath = "C:\\Users\\dooli\\Downloads\\FileBrowserGUI.java";
        String remoteFilePath = "/home/dominichann/";

        FileInputStream fileInputStream = null;

        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                Main.loadingBar.addConsoleText("FTP server refused connection.");
                return false;
            }

            boolean loggedIn = ftpClient.login(username, password);
            if (!loggedIn) {
                System.out.println("Could not log in to the FTP server.");
                return false;
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
          //  ftpClient.enterLocalPassiveMode();

            File localFile = new File(localFilePath);
            long fileSize = localFile.length();
            fileInputStream = new FileInputStream(localFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesUploaded = 0;

            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                ftpClient.storeFile(remoteFilePath, bufferedInputStream);
                totalBytesUploaded += bytesRead;

                int percentComplete = (int) ((totalBytesUploaded * 100) / fileSize);
                Main.loadingBar.setLoadingBar(percentComplete);
            }
            Main.loadingBar.setLoadingBarText("Done!");

            bufferedInputStream.close();
            fileInputStream.close();
            ftpClient.logout();
            ftpClient.disconnect();

            System.out.println("File upload completed successfully.");
            return true;
        } catch (Exception e) {
            Main.loadingBar.addConsoleText(e.toString());
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
        public static void FTPUpFolder(String[] args) {
            String server = "ftp.example.com";
            int port = 21;
            String username = "your-username";
            String password = "your-password";
            String localFolderPath = "path/to/local/folder";
            String remoteFolderPath = "/path/to/remote/folder";

            FTPClient ftpClient = new FTPClient();

            try {
                ftpClient.connect(server, port);
                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    System.out.println("FTP server refused connection.");
                    return;
                }

                boolean loggedIn = ftpClient.login(username, password);
                if (!loggedIn) {
                    System.out.println("Could not log in to the FTP server.");
                    return;
                }

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                long totalBytes = getFolderSize(new File(localFolderPath));
                long uploadedBytes = 0;

                uploadFolder(ftpClient, localFolderPath, remoteFolderPath, totalBytes, uploadedBytes);

                ftpClient.logout();
                ftpClient.disconnect();

                System.out.println("Folder upload completed successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void uploadFolder(FTPClient ftpClient, String localFolderPath, String remoteFolderPath, long totalBytes, long uploadedBytes) throws IOException {
            File localFolder = new File(localFolderPath);
            File[] files = localFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    String remoteFilePath = remoteFolderPath + "/" + file.getName();

                    if (file.isFile()) {
                        uploadFile(ftpClient, file.getAbsolutePath(), remoteFilePath, totalBytes, uploadedBytes);
                    } else if (file.isDirectory()) {
                        ftpClient.makeDirectory(remoteFilePath);
                        ftpClient.changeWorkingDirectory(remoteFilePath);
                        uploadFolder(ftpClient, file.getAbsolutePath(), remoteFilePath, totalBytes, uploadedBytes);
                        ftpClient.changeToParentDirectory();
                    }
                }
            }
        }

        private static void uploadFile(FTPClient ftpClient, String localFilePath, String remoteFilePath, long totalBytes, long uploadedBytes) throws IOException {
            File localFile = new File(localFilePath);
            FileInputStream fileInputStream = new FileInputStream(localFile);

            ftpClient.storeFile(remoteFilePath, fileInputStream);

            uploadedBytes += localFile.length();
            int percentComplete = (int) ((uploadedBytes * 100) / totalBytes);
            System.out.println("Progress: " + percentComplete + "%");

            fileInputStream.close();
        }

        private static long getFolderSize(File folder) {
            long size = 0;

            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            size += file.length();
                        } else if (file.isDirectory()) {
                            size += getFolderSize(file);
                        }
                    }
                }
            } else {
                size = folder.length();
            }

            return size;
        }
}

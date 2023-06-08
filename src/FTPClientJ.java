import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTP;

import javax.swing.*;
import java.io.*;

public class FTPClientJ {
    private static FTPClient ftpClient = new FTPClient();
    private static SwingWorker<Boolean, Integer> uploadTask;


    public static boolean isConnected(){
        if(ftpClient.isConnected()){
            return true;
        }else {
            return false;
        }
    }
    public static boolean connect(String server, String user, String pass, int port) throws IOException {
        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                Main.gui.addConsoleText("FTP server refused connection.");
                return false;
            }
            boolean loggedIn = ftpClient.login(user, pass);
            if (!loggedIn) {
                Main.gui.addConsoleText("Could not log in to the FTP server.");
                return false;
            }

            // Add protocol command listener to print out the commands and responses
            PrintWriter commandWriter = new PrintWriter(System.out);

            // Add protocol command listener to the custom PrintWriter
            ftpClient.addProtocolCommandListener(new PrintCommandListener(commandWriter, true));

            return true;
        } catch (Exception e) {
            Main.gui.addConsoleText(e.toString());
            return false;
        }
    }
    public static void disconnect() throws IOException {
        try {
            ftpClient.disconnect();
        }catch(Exception e){
            Main.gui.addConsoleText(e.toString());
        }
    }
    public static boolean FTPUpFile(String sendFile, String remoteDir ) {

        FileInputStream inputStream;
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            File localFile = new File(sendFile);
            String remoteFileName = localFile.getName();
            ftpClient.changeWorkingDirectory("/dominichann/");

            // Create and start the upload task
            uploadTask = createUploadTask(localFile, remoteFileName);
            uploadTask.execute();
            return true;
        } catch (Exception ex) {
            Main.gui.addConsoleText("Error: " + ex.getMessage());
            return false;
        }
    }
    private static SwingWorker<Boolean, Integer> createUploadTask(File localFile, String remoteFileName) {
        return new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                FileInputStream inputStream = new FileInputStream(localFile);
                long fileSize = localFile.length();
                long uploadedBytes = 0;
                int percentCompleted = 0;
                byte[] buffer = new byte[4096];

                final int POLL_INTERVAL = 500; // Polling interval in milliseconds
                long lastProgressUpdateTime = System.currentTimeMillis();

                try (OutputStream outputStream = ftpClient.storeFileStream(remoteFileName)) {
                    if (outputStream == null) {
                        Main.gui.addConsoleText("Failed to obtain the output stream for file transfer.");
                        return false;
                    }

                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;
                        int newPercentCompleted = (int) ((uploadedBytes * 100) / fileSize);
                        if (newPercentCompleted > percentCompleted) {
                            percentCompleted = newPercentCompleted;
                            publish(percentCompleted);
                        }

                        // Check if enough time has passed since the last progress update
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastProgressUpdateTime >= POLL_INTERVAL) {
                            lastProgressUpdateTime = currentTime;
                            publish(percentCompleted);
                        }
                    }
                }

                inputStream.close();

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    Main.gui.addConsoleText("File uploaded successfully.");
                    return true;
                } else {
                    Main.gui.addConsoleText("Failed to upload the file.");
                    return false;
                }
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                // Update the GUI loading bar with the latest progress value
                int progress = chunks.get(chunks.size() - 1);
                Main.gui.setLoadingBar(progress);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        Main.gui.addConsoleText("File upload completed.");
                    } else {
                        Main.gui.addConsoleText("File upload failed.");
                    }
                } catch (Exception ex) {
                    Main.gui.addConsoleText("Error: " + ex.getMessage());
                }
            }
        };
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

    public static FTPFile[] getDir() throws IOException {
        try {
            return ftpClient.listFiles();
        } catch (Exception e) {
            Main.gui.addConsoleText(e.toString());
            return null;
        }
    }
}

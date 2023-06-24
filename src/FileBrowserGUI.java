import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileBrowserGUI extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JLabel selectedFileLabel;
    public JProgressBar loadingBar;
    private JTextField textField1;
    private JTextField textField2;
    private JPasswordField textField3;
    private JTextField textField4;
    public JTextArea console;

    private JList<String> ftpDirectoryList;

    private DefaultListModel<String> FTPListModel;

    private File FTPcurrentDirectory;
    private File currentDirectory;

    public FileBrowserGUI() throws IOException {
        setTitle("FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        Image icon = Toolkit.getDefaultToolkit().getImage("/icons/ftp.png");
        setIconImage(icon);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        setContentPane(contentPane);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);

        JScrollPane scrollPane = new JScrollPane(fileList);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        console = new JTextArea(5, 5); // Create console component
        console.setEditable(false);
        selectedFileLabel = new JLabel();
        selectedFileLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel topPanel = new JPanel(new BorderLayout()); // Create a top panel for console
        topPanel.setPreferredSize(new Dimension(10, 120)); // Set preferred size for the top panel
        console = new JTextArea(5, 10); // Create console component
        console.setEditable(false);
        JScrollPane consoleScrollPane = new JScrollPane(console);
        topPanel.add(consoleScrollPane, BorderLayout.SOUTH); // Add console to the top panel
        topPanel.add(selectedFileLabel,BorderLayout.PAGE_START);

        contentPane.add(topPanel, BorderLayout.NORTH); // Add top panel to the north region

        JPanel connectPanel = new JPanel(); // Panel for connect and disconnect buttons
        connectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        disconnectButton.setOpaque(true); // Ensure button is opaque
        disconnectButton.setBackground(UIManager.getColor("Button.background")); // Set background color to default
        disconnectButton.setForeground(UIManager.getColor("Button.foreground")); // Set text color to default
        disconnectButton.setBorderPainted(true); // Show button border

        connectPanel.add(connectButton);
        connectPanel.add(disconnectButton);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        JLabel label1 = new JLabel("Server IP");
        textField1 = new JTextField(10);
        JLabel label2 = new JLabel("Username");
        textField2 = new JTextField(20);
        JLabel label3 = new JLabel("Password");
        textField3 = new JPasswordField(20);
        JLabel label4 = new JLabel("Port");
        textField4 = new JTextField(5);

        inputPanel.add(label1);
        inputPanel.add(textField1);
        inputPanel.add(label2);
        inputPanel.add(textField2);
        inputPanel.add(label3);
        inputPanel.add(textField3);
        inputPanel.add(label4);
        inputPanel.add(textField4);
        textField4.setText("21");

        inputPanel.add(connectPanel); // Add connectPanel to inputPanel

        contentPane.add(inputPanel, BorderLayout.SOUTH);
        FTPListModel = new DefaultListModel<>();
        ftpDirectoryList = new JList<>(FTPListModel);


        JPanel rightPanel = new JPanel(new BorderLayout()); // Panel for the right side components

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3)); // Panel for buttons
        JButton button1 = new JButton("Send File/Folder");
        JButton button2 = new JButton("Receive File/Folder");
        JButton button3 = new JButton("Button 3");

        buttonsPanel.add(button3);
        buttonsPanel.add(button2);
        buttonsPanel.add(button1);

        JPanel directoryPanel = new JPanel(); // Panel for FTP directory
        directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.Y_AXIS));

        JLabel directoryLabel = new JLabel("FTP Directory");
        directoryLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        ftpDirectoryList = new JList<>(new DefaultListModel<>());
        JScrollPane ftpDirectoryScrollPane = new JScrollPane(ftpDirectoryList);

        directoryPanel.add(directoryLabel);
        directoryPanel.add(ftpDirectoryScrollPane);

        loadingBar = new JProgressBar();
        loadingBar.setStringPainted(true);

        rightPanel.add(buttonsPanel, BorderLayout.PAGE_START); // Add buttons to the center
        rightPanel.add(directoryPanel, BorderLayout.LINE_START); // Add directory panel to the left
        rightPanel.add(loadingBar, BorderLayout.PAGE_END); // Add loading bar to the bottom

        contentPane.add(rightPanel, BorderLayout.EAST); // Add right panel to the center

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedIndex = fileList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedFile = listModel.getElementAt(selectedIndex);
                        File file = new File(currentDirectory, selectedFile);
                        if (selectedFile.equals("..")) {
                            File parentDirectory = currentDirectory.getParentFile();
                            if (parentDirectory != null) {
                                try {
                                    navigateToDirectory(parentDirectory);
                                } catch (IOException ex) {
                                    addConsoleText(ex.toString());
                                }
                            }
                        } else if (file.isDirectory()) {
                            try {
                                navigateToDirectory(file);
                            } catch (IOException ex) {
                                addConsoleText(ex.toString());
                            }
                        }
                    }
                }
            }
        });

        ftpDirectoryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                if (e.getClickCount() == 2) {
                    int selectedIndex = ftpDirectoryList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedFile = FTPListModel.getElementAt(selectedIndex);
                        File file = new File(FTPcurrentDirectory, selectedFile);
                        if (selectedFile.equals("..")) {
                            File parentDirectory = FTPcurrentDirectory.getParentFile();
                            if (parentDirectory != null) {
                                try {
                                    navigateToFTPDirectory(parentDirectory);
                                } catch (IOException ex) {
                                    addConsoleText(ex.toString());
                                }
                            }
                        } else if (file.isDirectory()) {
                            try {
                                navigateToFTPDirectory(file);
                            } catch (IOException ex) {
                                addConsoleText(ex.toString());
                            }
                        }
                    }
                }
            }
        });

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = fileList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedFile = listModel.getElementAt(selectedIndex);
                    selectedFileLabel.setText(currentDirectory.getAbsolutePath() + File.separator + selectedFile);
                } else {
                    selectedFileLabel.setText("");
                }
            }
        });

        navigateToDirectory(new File(System.getProperty("user.home")));
        selectedFileLabel.setText(currentDirectory.getPath());
        setVisible(true);
        connectButton.addActionListener(e -> {
            try {
                String pass = new String(textField3.getPassword());
                if(textField1.getText().equals("")||(textField4.getText()).equals("")){
                    addConsoleText("Invalid Server Info Input");
                }else {
                    if(FTPClientJ.connect(textField1.getText(), textField2.getText(), pass, Integer.parseInt(textField4.getText()))){
                        addConsoleText("Connected!");
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                            updateFTPFileList(FTPClientJ.getList());
                            if(FTPClientJ.getReply()==500){
                                updateFTPFileList(FTPClientJ.getDir());
                            }
                            FTPcurrentDirectory = new File(FTPClientJ.getWorkingDir());
                    }else{
                        addConsoleText("Unable to Connect");
                    }
                }
                } catch (IOException ex) {
                addConsoleText(ex.toString());
            }
        });

        disconnectButton.addActionListener(e -> {
            try {
                FTPClientJ.disconnect();
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                ((DefaultListModel<String>) ftpDirectoryList.getModel()).clear();
                addConsoleText("Disconnected!");
            } catch (IOException ex) {
                addConsoleText(ex.toString());
            }
        });

        button1.addActionListener(e -> {
            setLoadingBar(0);
            int selectedFileIndex = fileList.getSelectedIndex();
            if(selectedFileIndex==-1){
                addConsoleText("No File Selected");
                return;
            }
            File[] files = currentDirectory.listFiles();
            String selectedFile = listModel.getElementAt(selectedFileIndex);
            File file = new File(currentDirectory, selectedFile);
            if(file.isDirectory()&&selectedFileIndex!=0){
                //FTPClientJ.FTPUpFolder();
            }else if(selectedFileIndex!=0){
                FTPClientJ.FTPUpFile(file.getAbsolutePath(), selectedFile);
            }else {

            }
            if(!FTPClientJ.isConnected()){
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                addConsoleText("Disconnected from "+ textField1.getText());
            }
        });

        button2.addActionListener(e -> {
            ((DefaultListModel<String>) ftpDirectoryList.getModel()).clear();
            File[] files = currentDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        ((DefaultListModel<String>) ftpDirectoryList.getModel()).addElement(file.getName());
                    }else {
                        ((DefaultListModel<String>) ftpDirectoryList.getModel()).addElement(file.getName()+ File.separator);
                    }
                }
            }
        });

        button3.addActionListener(e -> {
            try {
                    navigateToFTPDirectory(new File("/dominichann"));
            } catch (IOException ex) {
                addConsoleText(ex.toString());
            }
        });
    }

    private void navigateToDirectory(File directory) throws IOException {
        try {
            if (directory != null) {
                updateFileList(directory);
                currentDirectory = directory;

                if (currentDirectory.getParentFile() != null) {
                    selectedFileLabel.setText(currentDirectory.getAbsolutePath());
                } else {
                    selectedFileLabel.setText("");
                }
            }
        }catch (Exception e){
            addConsoleText(e.toString());
        }
    }
    private void navigateToFTPDirectory(File directory) throws IOException {
        try {
            if (directory != null) {
                FTPClientJ.changeDir(directory);
                FTPFile[] curDir = FTPClientJ.getDir();
                if(curDir==null) {
                    updateFTPFileList(FTPClientJ.getList());
                }else {
                    updateFTPFileList(curDir);
                }
                FTPcurrentDirectory = directory;

                if (FTPcurrentDirectory.getParentFile() != null) {
                  //  selectedFileLabel.setText(currentDirectory.getAbsolutePath());
                } else {
                  //  selectedFileLabel.setText("");
                }
            }
        }catch (Exception e){
            addConsoleText(e.toString());
        }
    }
    private void updateFTPFileList(FTPFile[] files) throws IOException {
        if(FTPClientJ.ftpClient.isConnected()) {
            try {
                ((DefaultListModel<String>) ftpDirectoryList.getModel()).clear();
                if (ftpDirectoryList.getParent() != null && !"/".equals(FTPClientJ.ftpClient.printWorkingDirectory())) {
                    ((DefaultListModel<String>) ftpDirectoryList.getModel()).addElement("..");
                }
                if (files != null) {
                    for (FTPFile file : files) {
                        if (file.isDirectory()) {
                            ((DefaultListModel<String>) ftpDirectoryList.getModel()).addElement(file.getName() + File.separator);
                        } else {
                            ((DefaultListModel<String>) ftpDirectoryList.getModel()).addElement(file.getName());
                        }
                    }
                }
            } catch (Exception ex) {
                addConsoleText(ex.toString());
            }
        }
    }


    private void updateFileList(File directory) throws IOException {
        try {
            listModel.clear();
            if (directory.getParentFile() != null ) {
                listModel.addElement("..");
            }
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        listModel.addElement(file.getName() + File.separator);
                    } else {
                        listModel.addElement(file.getName());
                    }
                }
            }
        }catch(Exception e){
            addConsoleText(e.toString());
        }
    }

    public void setLoadingBar(int x) {
        loadingBar.setValue(x);
    }

    public void setLoadingBarText(String str) {
        loadingBar.setString(str);
    }
    public void addConsoleText(String text) {
        console.append(text + "\n");
    }
    public void appendTextToConsole(String text) {
        addConsoleText(text);
    }
}

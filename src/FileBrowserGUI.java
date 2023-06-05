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
import java.net.URL;
import java.util.Stack;
import org.apache.commons.net.ftp.FTPClient;

public class FileBrowserGUI extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JLabel selectedFileLabel;
    private JProgressBar loadingBar;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextArea console; // Added console component

    private File currentDirectory;

    public FileBrowserGUI() {
        setTitle("FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        setContentPane(contentPane);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);

        JScrollPane scrollPane = new JScrollPane(fileList);
        contentPane.add(scrollPane, BorderLayout.CENTER);


        selectedFileLabel = new JLabel();
        selectedFileLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(selectedFileLabel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.NORTH);

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
        //setVisible(true);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        JLabel label1 = new JLabel("Server IP");
        textField1 = new JTextField(10);
        JLabel label2 = new JLabel("Username");
        textField2 = new JTextField(20);
        JLabel label3 = new JLabel("Password");
        textField3 = new JTextField(20);
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

        inputPanel.add(connectPanel); // Add connectPanel to inputPanel

        contentPane.add(inputPanel, BorderLayout.SOUTH);

        console = new JTextArea(5, 10); // Create console component
        console.setEditable(false);
        JScrollPane consoleScrollPane = new JScrollPane(console);
        JPanel rightPanel = new JPanel(new BorderLayout()); // Panel for the right side components

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3)); // Panel for buttons
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");

        buttonsPanel.add(button3);
        buttonsPanel.add(button2);
        buttonsPanel.add(button1);

        JPanel spacePanel = new JPanel(); // Panel for white space
        spacePanel.setPreferredSize(new Dimension(10, 50)); // Adjust the preferred height as needed

        loadingBar = new JProgressBar();
        loadingBar.setStringPainted(true);

        rightPanel.add(consoleScrollPane, BorderLayout.CENTER); // Add console to the center
        rightPanel.add(buttonsPanel, BorderLayout.PAGE_START); // Add buttons to the center
        rightPanel.add(loadingBar, BorderLayout.PAGE_END); // Add loading bar to the bottom

        contentPane.add(rightPanel, BorderLayout.EAST);


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
                                navigateToDirectory(parentDirectory);
                            }
                        } else if (file.isDirectory()) {
                            navigateToDirectory(file);
                        }
                    }
                }
            }
        });

        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = fileList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedFile = listModel.getElementAt(selectedIndex);
                        selectedFileLabel.setText(currentDirectory.getAbsolutePath() + File.separator + selectedFile);
                    } else {
                        selectedFileLabel.setText("");
                    }
                }
            }
        });




        navigateToDirectory(new File(System.getProperty("user.home")));
        selectedFileLabel.setText(currentDirectory.getPath());
        setVisible(true);
    }

    private void navigateToDirectory(File directory) {
        if (directory != null) {
            updateFileList(directory);
            currentDirectory = directory;

            if (currentDirectory.getParentFile() != null) {
                selectedFileLabel.setText(currentDirectory.getAbsolutePath());
            } else {
                selectedFileLabel.setText("");
            }
        }
    }

    private void updateFileList(File directory) {
        listModel.clear();
        if (directory.getParentFile() != null) {
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
    }


    public void setLoadingBar(int x) {
        loadingBar.setValue(x);
    }

    public void setLoadingBarText(String str) {
        loadingBar.setString(str);
    }
}
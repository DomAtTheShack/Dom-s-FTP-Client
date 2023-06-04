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

public class FileBrowserGUI extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JLabel selectedFileLabel;
    private JButton backButton;
    private JButton forwardButton;
    private JProgressBar loadingBar; // Added loading bar component

    private Stack<File> backStack;
    private Stack<File> forwardStack;
    private File currentDirectory;

    public FileBrowserGUI() {
        setTitle("FTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);

        JScrollPane scrollPane = new JScrollPane(fileList);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        backButton = new JButton(getIcon("arrow_left.png"));
        backButton.setEnabled(false);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateBack();
            }
        });

        forwardButton = new JButton(getIcon("arrow_right.png"));
        forwardButton.setEnabled(false);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateForward();
            }
        });

        selectedFileLabel = new JLabel();
        selectedFileLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(backButton, BorderLayout.WEST);
        buttonPanel.add(selectedFileLabel, BorderLayout.CENTER);
        buttonPanel.add(forwardButton, BorderLayout.EAST);
        contentPane.add(buttonPanel, BorderLayout.NORTH);

        JPanel buttonsSection = new JPanel();
        buttonsSection.setLayout(new GridLayout(4, 1)); // Changed to 4 rows

        JButton button1 = new JButton("Button 1");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Button 1 clicked");
            }
        });
        buttonsSection.add(button1);

        JButton button2 = new JButton("Button 2");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Button 2 clicked");
            }
        });
        buttonsSection.add(button2);

        JButton button3 = new JButton("Button 3");
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Button 3 clicked");
            }
        });
        buttonsSection.add(button3);

        loadingBar = new JProgressBar();
        loadingBar.setStringPainted(true); // Display the percentage
        buttonsSection.add(loadingBar); // Added loading bar to the layout

        contentPane.add(buttonsSection, BorderLayout.EAST); // Aligned buttons section to the right

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == fileList && e.getClickCount() == 2) {
                    int selectedIndex = fileList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedFile = listModel.getElementAt(selectedIndex);
                        File file = new File(currentDirectory, selectedFile);
                        if (file.isDirectory()) {
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
                        File file = new File(currentDirectory, selectedFile);
                        selectedFileLabel.setText(file.getAbsolutePath());
                    } else {
                        selectedFileLabel.setText("");
                    }
                }
            }
        });

        backStack = new Stack<>();
        forwardStack = new Stack<>();
        navigateToDirectory(new File(System.getProperty("user.home")));

        setVisible(true);
    }

    private ImageIcon getIcon(String filename) {
        URL iconURL = getClass().getResource("icons/" + filename);
        if (iconURL != null) {
            return new ImageIcon(iconURL);
        } else {
            System.err.println("Icon not found: " + filename);
            return null;
        }
    }

    private void navigateToDirectory(File directory) {
        if (currentDirectory != null) {
            backStack.push(currentDirectory);
            backButton.setEnabled(true);
        }
        forwardButton.setEnabled(false);

        updateFileList(directory);
        currentDirectory = directory;
        selectedFileLabel.setText(currentDirectory.getAbsolutePath());
    }

    private void navigateBack() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentDirectory);
            forwardButton.setEnabled(true);

            currentDirectory = backStack.pop();
            updateFileList(currentDirectory);

            if (backStack.isEmpty()) {
                backButton.setEnabled(false);
            }
        } else {
            // Navigate to the parent directory
            File parentDirectory = currentDirectory.getParentFile();
            if (parentDirectory != null) {
                forwardStack.push(currentDirectory);
                forwardButton.setEnabled(true);

                currentDirectory = parentDirectory;
                updateFileList(currentDirectory);

                backButton.setEnabled(true);
            }
        }

        selectedFileLabel.setText(currentDirectory.getAbsolutePath());
    }


    private void navigateForward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentDirectory);
            backButton.setEnabled(true);

            currentDirectory = forwardStack.pop();
            updateFileList(currentDirectory);

            if (forwardStack.isEmpty()) {
                forwardButton.setEnabled(false);
            }
        }
        selectedFileLabel.setText(currentDirectory.getAbsolutePath());
    }

    private void updateFileList(File directory) {
        listModel.clear();
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

    public static void main(String[] args) {
        // Set native look and feel for better integration with the operating system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileBrowserGUI();
            }
        });
    }
}
import javax.swing.*;
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

    private Stack<File> backStack;
    private Stack<File> forwardStack;
    private File currentDirectory;

    public FileBrowserGUI() {
        setTitle("File Browser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);

        JScrollPane scrollPane = new JScrollPane(fileList);
        add(scrollPane, BorderLayout.CENTER);

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
        add(buttonPanel, BorderLayout.NORTH);

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == fileList && e.getClickCount() == 2) {
                    int selectedIndex = fileList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedFile = listModel.getElementAt(selectedIndex);
                        File file = new File(selectedFile);
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
                        File file = new File(selectedFile);
                        selectedFileLabel.setText(file.getName());
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
    }

    private void navigateBack() {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentDirectory);
            forwardButton.setEnabled(true);

            File directory = backStack.pop();
            updateFileList(directory);
            currentDirectory = directory;

            if (backStack.isEmpty()) {
                backButton.setEnabled(false);
            }
        }
    }

    private void navigateForward() {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentDirectory);
            backButton.setEnabled(true);

            File directory = forwardStack.pop();
            updateFileList(directory);
            currentDirectory = directory;

            if (forwardStack.isEmpty()) {
                forwardButton.setEnabled(false);
            }
        }
    }

    private void updateFileList(File directory) {
        listModel.clear();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                listModel.addElement(file.getAbsolutePath());
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

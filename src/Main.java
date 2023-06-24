import javax.swing.*;
import java.io.IOException;

public class Main {
    public static FileBrowserGUI gui;

    static {
        try {
            gui = new FileBrowserGUI();
        } catch (IOException e) {
            gui.addConsoleText(e.toString());
        }
    }

    public static void main(String[] args){
        // Set native look and feel for better integration with the operating system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

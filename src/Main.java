
import javax.swing.*;

public class Main {
    public static FileBrowserGUI loadingBar;

    static {
        loadingBar = new FileBrowserGUI();
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

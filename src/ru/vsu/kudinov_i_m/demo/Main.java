package ru.vsu.kudinov_i_m.demo;

import java.awt.EventQueue;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.UIManager;

import ru.vsu.kudinov_i_m.utils.SwingUtils;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Locale.setDefault(Locale.ROOT);
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtils.setDefaultFont("Arial", 20);

        EventQueue.invokeLater(() ->
        {
            try
            {
                JFrame mainFrame = new GraphDemoFrame();
                mainFrame.setVisible(true);
                mainFrame.setSize(1190, 900);
                mainFrame.setLocationRelativeTo(null);
            } catch (Exception ex)
            {
                SwingUtils.showErrorMessageBox(ex);
            }
        });
    }
}

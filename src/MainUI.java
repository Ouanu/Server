import utils.CmdTask;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.io.IOException;
import java.util.ArrayList;

public class MainUI {
    private JPanel panel;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JList list1;
    private JTextArea port;
    private ListModel<String> listModel;


    public static void main(String[] args) {
        JFrame frame = new JFrame("MainUI");
        MainUI mainUI = new MainUI();
        frame.setSize(800, 800);
        frame.setContentPane(mainUI.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        try {
            Server server = new Server();
            mainUI.textArea1.setText("IP:" + server.getIPAddress());
            mainUI.textArea2.setText("端口：9250");
            ArrayList<String> list = server.getList();
            mainUI.listModel = new AbstractListModel<String>() {
                @Override
                public int getSize() {
                    return list.size();
                }

                @Override
                public String getElementAt(int index) {
                    return list.get(index);
                }
            };
            mainUI.list1.setModel(mainUI.listModel);
            server.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

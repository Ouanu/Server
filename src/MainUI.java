import utils.CmdTask;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
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
        frame.setSize(500, 600);
        frame.setContentPane(mainUI.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
            mainUI.list1.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
//                    super.mouseClicked(e);
                    if (mainUI.list1.getSelectedIndex() != -1) {
                        if (e.getClickCount() == 1) {
                            System.out.println("选中" + list.get(mainUI.list1.getSelectedIndex()));
                        } else if (e.getClickCount() == 2) {
                            System.out.println("打开文件" + list.get(mainUI.list1.getSelectedIndex()));
                            try {
                                Desktop.getDesktop().open(new File("C:\\Users\\Linkdamo\\Desktop\\server\\" + list.get(mainUI.list1.getSelectedIndex())));
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
            });
            server.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

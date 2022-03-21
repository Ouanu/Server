import utils.DirAndFileUtil;
import utils.SQLiteHelper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;


public class Server extends Thread {
    private ServerSocket serverSocket;
    private int port = 9250;
    private static Object lock = new Object();
    private static SQLiteHelper sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");


    public Server() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    private void execute() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Task(socket)).start();
        }
    }

    private static class Task implements Runnable {
        private DirAndFileUtil util = new DirAndFileUtil(1);
        private Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            synchronized (lock) {
                try {
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    // 链接检查
                    String s = inputStream.readUTF();
                    System.out.println(s);
                    outputStream.writeUTF("Hi !");
                    // 选择要做的事
                    int i = inputStream.readInt();
                    System.out.println("Code:" + i);
                    outputStream.writeUTF("Server is ready.");
                    switch (i) {
                        case 12345:
                            util.synchronizeFiles(inputStream, outputStream);
                            break;
                        case 23456:
                            //同步文件至服务器
                            util.downloadFiles(outputStream, inputStream);
                            break;
                        case 34567:
                            //同步检查
                            if (sqLiteHelper.getDatabaseStatement()) {
                                System.out.println("有数据库");
                                outputStream.writeLong(1000l);
                                boolean isChange = false;
                                int cnt = inputStream.readInt(); // 需要处理的数据数量
                                for (int j = 0; j < cnt; j++) {
                                    long uid = inputStream.readLong();
                                    long newDate;
                                    if (sqLiteHelper.isExist(uid)) {
                                        newDate = inputStream.readLong();
                                        if(sqLiteHelper.getUpdateDate(uid) != newDate) {
                                            //判断修改时间是否不同，若有改变则更新文件
                                            outputStream.writeBoolean(true);
                                            util.receiveFiles(outputStream, inputStream);
                                            isChange = true;
                                        } else {
                                            outputStream.writeBoolean(false);
                                            continue;
                                        }
                                    }
                                }
                                if (isChange) {
                                    System.out.println("数据库需要更新");
                                    sqLiteHelper.shutdownSQL();
                                    outputStream.writeUTF("数据库关闭");
                                    util.getSQL(outputStream, inputStream);
                                    sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
                                } else {
                                    System.out.println("数据库不需要更新");
                                }
                            } else {
                                outputStream.writeLong(1111l);
                                // 没有数据库，同步所有文件(包括数据库)
                                util.downloadFiles(outputStream, inputStream);
                                // 重新读取数据库
                                outputStream.writeUTF("数据库传输完成");
                                sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
                            }
                            break;
                        default:
                            break;
                    }
                    s = inputStream.readUTF(); //接收完成指令
                    System.out.println(s);
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

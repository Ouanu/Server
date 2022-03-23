package utils;

import java.io.IOException;

public class CmdTask implements Runnable{

    // 需要执行的命令
    private String command;

    public CmdTask(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        Process process = null;
        int exitVal = 0;
        try {
            process = Runtime.getRuntime().exec(command);

            exitVal = process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (exitVal != 0) {
            throw new RuntimeException("cmd 执行失败");
        }
    }
}

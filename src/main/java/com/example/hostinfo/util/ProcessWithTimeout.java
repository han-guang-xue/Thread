package com.example.hostinfo.util;

/**
 * 进程超时时间和正常时间结束运行
 */
public class ProcessWithTimeout extends Thread {
    private Process process;
    private int exitCode = Integer.MIN_VALUE;

    public ProcessWithTimeout(Process process) {
        this.process = process;
    }

    public int waitForProcess(int milliseconds) {
        this.start();

        try {
            this.join(milliseconds);
        } catch (InterruptedException e) {
            this.interrupt();
            Thread.currentThread().interrupt();
        } finally {
            process.destroy();
        }
        return exitCode;
    }

    @Override
    public void run() {
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ignore) {
            // Do nothing
        } catch (Exception ex) {
            // Unexpected exception
        }
    }
}

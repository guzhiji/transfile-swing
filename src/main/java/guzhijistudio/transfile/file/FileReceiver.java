package guzhijistudio.transfile.file;

import guzhijistudio.transfile.utils.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver extends Thread {

    public interface FileReceiverListener {

        void onFileReceived(File file);

        void onFile(File file);

        void onMsg(String msg);

        void onError(String msg);

        void onProgress(File file, long received, long total, long speed, long secs);
    }

    private final ServerSocket socket;
    private final File dir;
    private final FileReceiverListener listener;
    private boolean running = false;

    public FileReceiver(int port, File dir, FileReceiverListener listener) throws IOException {
        this.socket = new ServerSocket(port);
        this.dir = dir;
        this.listener = listener;
    }

    @Override
    public void start() {
        running = true;
        super.start();
    }

    public void shutdown() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                final Socket s = socket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buf = new byte[10240];
                        InputStream is = null;
                        try {
                            is = s.getInputStream();
                            while (true) {
                                String cmd = SocketUtils.readString(is, buf);
                                if (!cmd.isEmpty()) {
                                    if ("file".equalsIgnoreCase(cmd)) {
                                        String f = SocketUtils.readFile(is, buf, dir, new SocketUtils.Progress() {
                                            @Override
                                            public void onStart(File file) {
                                                listener.onFile(file);
                                            }

                                            @Override
                                            public void onFinish(File file) {
                                                listener.onFileReceived(file);
                                            }

                                            @Override
                                            public void onProgress(File file, long progress, long total, long speed, long secs) {
                                                listener.onProgress(file, progress, total, speed, secs);
                                            }
                                        });
                                        if (f == null) {
                                            listener.onError("文件接收失败");
                                        }
                                    } else if ("msg".equalsIgnoreCase(cmd)) {
                                        String m = SocketUtils.readString(is, buf);
                                        if (!m.isEmpty()) {
                                            listener.onMsg(m);
                                        }
                                    } else if ("close".equalsIgnoreCase(cmd)) {
                                        is.close();
                                        s.close();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException ignored) {
                                }
                            }
                            try {
                                s.close();
                            } catch (IOException ignored) {
                            }
                            listener.onError(e.getMessage());
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package guzhijistudio.transfile.file;

import guzhijistudio.transfile.utils.SocketUtils;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileSender extends Thread {

    public interface FileSenderListener {

        void onStart(File file);

        void onFileSent(File file);

        void onError(File file, String msg);

        void onProgress(File file, long sent, long total, long speed, long secs);
    }

    private final String ip;
    private final int port;
    private final String filename;
    private final FileSenderListener listener;

    public FileSender(String ip, int port, String filename, FileSenderListener listener) {
        this.ip = ip;
        this.port = port;
        this.filename = filename;
        this.listener = listener;
    }

    @Override
    public void run() {
        byte[] buf = new byte[10240];
        File f = new File(filename);
        if (!f.canRead()) {
            listener.onError(null, "cannot read " + f.getName());
            return;
        }
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port));
            try {
                OutputStream os = socket.getOutputStream();
                try {
                    SocketUtils.writeString(os, "file");
                    SocketUtils.writeFile(os, buf, filename, new SocketUtils.Progress() {
                        @Override
                        public void onStart(File file) {
                            listener.onStart(file);
                        }

                        @Override
                        public void onFinish(File file) {
                            listener.onFileSent(file);
                        }

                        @Override
                        public void onProgress(File file, long progress, long total, long speed, long secs) {
                            listener.onProgress(file, progress, total, speed, secs);
                        }
                    });
                    SocketUtils.writeString(os, "close");
                } finally {
                    os.close();
                }
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(f, e.getMessage());
        }
    }
}

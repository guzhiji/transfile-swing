package guzhijistudio.transfile.swing;

import guzhijistudio.transfile.file.FileReceiver;
import guzhijistudio.transfile.file.FileSender;
import guzhijistudio.transfile.identity.Broadcaster;
import guzhijistudio.transfile.utils.Config;
import guzhijistudio.transfile.utils.Constants;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainFrame extends javax.swing.JFrame {

    private final String transFileReceiving, transFileSending, transFileInList;
    private Broadcaster broadcaster;
    private FileReceiver fileReceiver;
    private final ExecutorService fileSenders = Executors.newFixedThreadPool(2);
    private final FileReceiver.FileReceiverListener frListener = new FileReceiver.FileReceiverListener() {

        @Override
        public void onFileReceived(File file) {
            FileItemPanel fileItem = findFileItemPanel(jPanelFilesReceived, file);
            if (fileItem != null) {
                fileItem.setDone(true);
            }
        }

        @Override
        public void onFile(File file) {
            FileItemPanel fileItem = findFileItemPanel(jPanelFilesReceived, file);
            if (fileItem == null) {
                final FileItemPanel panel = new FileItemPanel(file);
                panel.setRemoveAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (panel.isProgressing()) {
                            JOptionPane.showMessageDialog(MainFrame.this, transFileReceiving);
                        } else {
                            jPanelFilesReceived.remove(panel);
                            jPanelFilesReceived.repaint();
                        }
                    }
                });
                jPanelFilesReceived.add(panel);
            }
        }

        @Override
        public void onMsg(String msg) {
        }

        @Override
        public void onError(String msg) {
        }

        @Override
        public void onProgress(File file, long received, long total, long speed, long secs) {
            FileItemPanel fileItem = findFileItemPanel(jPanelFilesReceived, file);
            if (fileItem != null) {
                fileItem.setProgress(received, total, speed, secs);
            }
        }
    };
    private final FileSender.FileSenderListener fsListener = new FileSender.FileSenderListener() {
        @Override
        public void onStart(File file) {
            FileItemPanel fileItem = findFileItemPanel(jPanelSendingFiles, file);
            if (fileItem != null) {
                fileItem.setError(null);
            }
        }

        @Override
        public void onFileSent(File file) {
            FileItemPanel fileItem = findFileItemPanel(jPanelSendingFiles, file);
            if (fileItem != null) {
                fileItem.setDone(true);
            }
        }

        @Override
        public void onError(File file, String msg) {
            FileItemPanel fileItem = findFileItemPanel(jPanelSendingFiles, file);
            if (fileItem != null) {
                fileItem.setError(msg);
            }
        }

        @Override
        public void onProgress(File file, long sent, long total, long speed, long secs) {
            FileItemPanel fileItem = findFileItemPanel(jPanelSendingFiles, file);
            if (fileItem != null) {
                fileItem.setProgress(sent, total, speed, secs);
            }
        }
    };

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("guzhijistudio/transfile/swing/Bundle");
        transFileReceiving = bundle.getString("MainFrame.Message.FileReceiving");
        transFileSending = bundle.getString("MainFrame.Message.FileSending");
        transFileInList = bundle.getString("MainFrame.Message.FileInList");

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/guzhijistudio/transfile/swing/icon.png")));

        if (Config.LOADED) {
            startBroadcaster();
            startFileReceiver();
        } else {
            showConfigDialog(true);
        }

    }

    private void startBroadcaster() {
        SocketAddress groupAddr = new InetSocketAddress(
                Config.GROUP_ADDR,
                Constants.IDENTITY_SERVER_PORT);
        broadcaster = new Broadcaster(Config.DEVICE_NAME, groupAddr);
        broadcaster.start();
    }

    private void startFileReceiver() {
        try {
            File dir = new File(Config.DIR);
            fileReceiver = new FileReceiver(Constants.FILE_SERVER_PORT, dir, frListener);
            fileReceiver.start();
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage());
            System.exit(1);
        }
    }

    private void showConfigDialog(boolean quitOnCancel) {
        ConfigDialog dialog = new ConfigDialog();
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            if (fileReceiver != null) {
                fileReceiver.shutdown();
            }
            if (broadcaster != null) {
                broadcaster.shutdown();
            }
            try {
                if (fileReceiver != null) {
                    fileReceiver.join();
                }
                startFileReceiver();
                if (broadcaster != null) {
                    broadcaster.join();
                }
                startBroadcaster();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, ex.getMessage());
                System.exit(1);
            }
        } else if (quitOnCancel) {
            System.exit(0);
        }
    }

    private void showFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File file : chooser.getSelectedFiles()) {
                if (findFileItemPanel(jPanelSendingFiles, file) == null) { // file not listed
                    // create panel
                    final FileItemPanel panel = new FileItemPanel(file);
                    panel.setResendAction(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            if (panel.isProgressing()) {
                                JOptionPane.showMessageDialog(MainFrame.this, transFileSending);
                            } else {
                                String ip = panel.getDestIp();
                                if (ip == null) { // choose an IP if not sent before
                                    DeviceListDialog dialog = new DeviceListDialog();
                                    dialog.setVisible(true);
                                    if (dialog.isIpSelected()) {
                                        ip = dialog.getSelectedIp();
                                        panel.setDestIp(ip);
                                    }
                                }
                                if (ip != null) {
                                    fileSenders.submit(new FileSender(
                                            ip,
                                            Constants.FILE_SERVER_PORT,
                                            panel.getFile().getAbsolutePath(),
                                            fsListener));
                                }
                            }
                        }
                    });
                    panel.setRemoveAction(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            if (panel.isProgressing()) {
                                JOptionPane.showMessageDialog(MainFrame.this, transFileSending);
                            } else {
                                jPanelSendingFiles.remove(panel);
                                jPanelSendingFiles.repaint();
                            }
                        }
                    });
                    // add it to the list
                    jPanelSendingFiles.add(panel);
                } else { // already listed
                    JOptionPane.showMessageDialog(this, String.format(transFileInList, file.getName()));
                }
            }
        }
    }

    private void showDeviceChooser() {
        DeviceListDialog dialog = new DeviceListDialog();
        dialog.setVisible(true);
        if (dialog.isIpSelected()) {
            for (Component comp : jPanelSendingFiles.getComponents()) {
                if (comp instanceof FileItemPanel) {
                    FileItemPanel fileItem = (FileItemPanel) comp;
                    if (!fileItem.isProgressing() && !fileItem.isDone()) {
                        fileItem.setDestIp(dialog.getSelectedIp());
                        fileSenders.submit(new FileSender(
                                dialog.getSelectedIp(),
                                Constants.FILE_SERVER_PORT,
                                fileItem.getFile().getAbsolutePath(),
                                fsListener));
                    }
                }
            }
        }
    }

    private FileItemPanel findFileItemPanel(JPanel list, File file) {
        for (Component comp : list.getComponents()) {
            if (comp instanceof FileItemPanel) {
                FileItemPanel fileItem = (FileItemPanel) comp;
                if (fileItem.getFile().equals(file)) {
                    return fileItem;
                }
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        if (fileReceiver != null) {
            fileReceiver.shutdown();
        }
        if (broadcaster != null) {
            broadcaster.shutdown();
        }
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelSend = new javax.swing.JPanel();
        jPanelSendToolBar = new javax.swing.JPanel();
        jButtonAddFile = new javax.swing.JButton();
        jButtonSendAll = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelSendingFiles = new javax.swing.JPanel();
        jPanelReceive = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanelFilesReceived = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuSend = new javax.swing.JMenu();
        jMenuItemAddFile = new javax.swing.JMenuItem();
        jMenuItemSendAll = new javax.swing.JMenuItem();
        jMenuConfig = new javax.swing.JMenu();
        jMenuItemConfig = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("guzhijistudio/transfile/swing/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N
        setLocationByPlatform(true);
        setMaximumSize(new java.awt.Dimension(350, 2147483647));
        setMinimumSize(new java.awt.Dimension(350, 500));
        setPreferredSize(new java.awt.Dimension(350, 500));
        setResizable(false);

        jPanelSend.setLayout(new javax.swing.BoxLayout(jPanelSend, javax.swing.BoxLayout.PAGE_AXIS));

        jPanelSendToolBar.setLayout(new javax.swing.BoxLayout(jPanelSendToolBar, javax.swing.BoxLayout.LINE_AXIS));

        jButtonAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/addfile.png"))); // NOI18N
        jButtonAddFile.setText(bundle.getString("MainFrame.jButtonAddFile.text")); // NOI18N
        jButtonAddFile.setFocusable(false);
        jButtonAddFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonAddFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFileActionPerformed(evt);
            }
        });
        jPanelSendToolBar.add(jButtonAddFile);

        jButtonSendAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/sendall.png"))); // NOI18N
        jButtonSendAll.setText(bundle.getString("MainFrame.jButtonSendAll.text")); // NOI18N
        jButtonSendAll.setFocusable(false);
        jButtonSendAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSendAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSendAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendAllActionPerformed(evt);
            }
        });
        jPanelSendToolBar.add(jButtonSendAll);

        jPanelSend.add(jPanelSendToolBar);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanelSendingFiles.setBackground(java.awt.SystemColor.window);
        jPanelSendingFiles.setLayout(new javax.swing.BoxLayout(jPanelSendingFiles, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(jPanelSendingFiles);

        jPanelSend.add(jScrollPane1);

        jTabbedPane1.addTab(bundle.getString("MainFrame.jPanelSend.TabConstraints.tabTitle"), jPanelSend); // NOI18N

        jPanelReceive.setLayout(new javax.swing.BoxLayout(jPanelReceive, javax.swing.BoxLayout.PAGE_AXIS));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanelFilesReceived.setBackground(java.awt.SystemColor.window);
        jPanelFilesReceived.setLayout(new javax.swing.BoxLayout(jPanelFilesReceived, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane2.setViewportView(jPanelFilesReceived);

        jPanelReceive.add(jScrollPane2);

        jTabbedPane1.addTab(bundle.getString("MainFrame.jPanelReceive.TabConstraints.tabTitle"), jPanelReceive); // NOI18N

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jMenuSend.setText(bundle.getString("MainFrame.jMenuSend.text")); // NOI18N

        jMenuItemAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/addfile.png"))); // NOI18N
        jMenuItemAddFile.setText(bundle.getString("MainFrame.jMenuItemAddFile.text")); // NOI18N
        jMenuItemAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddFileActionPerformed(evt);
            }
        });
        jMenuSend.add(jMenuItemAddFile);

        jMenuItemSendAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/sendall.png"))); // NOI18N
        jMenuItemSendAll.setText(bundle.getString("MainFrame.jMenuItemSendAll.text")); // NOI18N
        jMenuItemSendAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSendAllActionPerformed(evt);
            }
        });
        jMenuSend.add(jMenuItemSendAll);

        jMenuBar1.add(jMenuSend);

        jMenuConfig.setText(bundle.getString("MainFrame.jMenuConfig.text")); // NOI18N

        jMenuItemConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/config.png"))); // NOI18N
        jMenuItemConfig.setText(bundle.getString("MainFrame.jMenuItemConfig.text")); // NOI18N
        jMenuItemConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConfigActionPerformed(evt);
            }
        });
        jMenuConfig.add(jMenuItemConfig);

        jMenuBar1.add(jMenuConfig);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfigActionPerformed

        showConfigDialog(false);

    }//GEN-LAST:event_jMenuItemConfigActionPerformed

    private void jButtonSendAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendAllActionPerformed

        showDeviceChooser();

    }//GEN-LAST:event_jButtonSendAllActionPerformed

    private void jMenuItemAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddFileActionPerformed

        showFileChooser();

    }//GEN-LAST:event_jMenuItemAddFileActionPerformed

    private void jMenuItemSendAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSendAllActionPerformed

        showDeviceChooser();

    }//GEN-LAST:event_jMenuItemSendAllActionPerformed

    private void jButtonAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFileActionPerformed

        showFileChooser();

    }//GEN-LAST:event_jButtonAddFileActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonSendAll;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuConfig;
    private javax.swing.JMenuItem jMenuItemAddFile;
    private javax.swing.JMenuItem jMenuItemConfig;
    private javax.swing.JMenuItem jMenuItemSendAll;
    private javax.swing.JMenu jMenuSend;
    private javax.swing.JPanel jPanelFilesReceived;
    private javax.swing.JPanel jPanelReceive;
    private javax.swing.JPanel jPanelSend;
    private javax.swing.JPanel jPanelSendToolBar;
    private javax.swing.JPanel jPanelSendingFiles;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}

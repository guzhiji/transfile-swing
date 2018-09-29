package guzhijistudio.transfile.swing;

import guzhijistudio.transfile.identity.UdpServer;
import guzhijistudio.transfile.utils.Config;
import guzhijistudio.transfile.utils.Constants;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;

public class DeviceListDialog extends javax.swing.JDialog {

    private static class DeviceItem {

        private String ip;
        private String name;

        public DeviceItem(String ip, String name) {
            this.ip = ip;
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return ip == null ? 0 : ip.hashCode();
        }

        @Override
        public boolean equals(Object another) {
            String ip2;
            if (another instanceof DeviceItem) {
                ip2 = ((DeviceItem) another).ip;
            } else if (another instanceof String) {
                ip2 = (String) another;
            } else {
                return false;
            }
            return ip != null && ip.equals(ip2);
        }
    }

    private static class DeviceListModel extends AbstractListModel<String> {

        private final List<DeviceItem> devices = new ArrayList<>();

        @Override
        public int getSize() {
            return devices.size();
        }

        @Override
        public String getElementAt(int index) {
            DeviceItem device = devices.get(index);
            return device.getName() + " - " + device.getIp();
        }

        public DeviceItem get(int index) {
            return devices.get(index);
        }

        public void add(DeviceItem device) {
            devices.add(device);
            int index = devices.size() - 1;
            fireIntervalAdded(this, index, index);
        }

        public void remove(String ip) {
            DeviceItem item = new DeviceItem(ip, null);
            int index = devices.indexOf(item);
            if (index > -1) {
                devices.remove(index);
                fireIntervalRemoved(this, index, index);
            }
        }
    }

    private UdpServer server;
    private String selectedIp;
    private final DeviceListModel deviceListModel = new DeviceListModel();
    private final UdpServer.UdpServerListener usListener = new UdpServer.UdpServerListener() {
        @Override
        public void onEnter(String ip, String name) {
            System.out.println("enter:" + ip);
            deviceListModel.add(new DeviceItem(ip, name));
        }

        @Override
        public void onQuit(String ip) {
            System.out.println("quit:" + ip);
            deviceListModel.remove(ip);
        }
    };

    /**
     * Creates new form DeviceListDialog
     */
    public DeviceListDialog() {
        initComponents();

        jListDevices.setModel(deviceListModel);

        try {
            // SocketAddress addr = new InetSocketAddress("192.168.191.1", Constants.IDENTITY_SERVER_PORT);
            SocketAddress addr = new InetSocketAddress("0.0.0.0", Constants.IDENTITY_SERVER_PORT);
            InetAddress group = InetAddress.getByName(Config.GROUP_ADDR);
            server = new UdpServer(addr, group, usListener);
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(DeviceListDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        selectedIp = null;
    }

    public boolean isIpSelected() {
        return selectedIp != null;
    }

    public String getSelectedIp() {
        return selectedIp;
    }

    @Override
    public void dispose() {
        if (server != null) {
            server.shutdown();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jListDevices = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("guzhijistudio/transfile/swing/Bundle"); // NOI18N
        setTitle(bundle.getString("DeviceListDialog.title")); // NOI18N
        setLocationByPlatform(true);
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jListDevices.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListDevices.setName(""); // NOI18N
        jScrollPane1.setViewportView(jListDevices);

        getContentPane().add(jScrollPane1);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jButtonOk.setText(bundle.getString("DeviceListDialog.jButtonOk.text")); // NOI18N
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonOk);

        jButtonCancel.setText(bundle.getString("DeviceListDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonCancel);

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed

        int index = jListDevices.getSelectedIndex();
        if (index > -1) {
            selectedIp = deviceListModel.get(index).getIp();
            dispose();
        }

    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed

        dispose();

    }//GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JList<String> jListDevices;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

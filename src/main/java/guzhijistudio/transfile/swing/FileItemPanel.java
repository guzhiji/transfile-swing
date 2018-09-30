package guzhijistudio.transfile.swing;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class FileItemPanel extends javax.swing.JPanel {

    private File file = null;
    private boolean progressing = false;
    private boolean done = false;
    private String destIp = null;
    private final String transProgress, transUnitSec, transUnitMin, transUnitHr, transUnitUnknown;
    private final static Map<String, String> fileExtensions = new HashMap<>();

    static {
        fileExtensions.put(".apk", "apk");
        fileExtensions.put(".xls", "excel");
        fileExtensions.put(".xlsx", "excel");
        fileExtensions.put(".htm", "html");
        fileExtensions.put(".html", "html");
        fileExtensions.put(".mpg", "media");
        fileExtensions.put(".mpeg", "media");
        fileExtensions.put(".ogg", "media");
        fileExtensions.put(".flv", "media");
        fileExtensions.put(".mov", "media");
        fileExtensions.put(".avi", "media");
        fileExtensions.put(".mp4", "media");
        fileExtensions.put(".mp3", "music");
        fileExtensions.put(".wav", "music");
        fileExtensions.put(".mid", "music");
        fileExtensions.put(".flac", "music");
        fileExtensions.put(".pdf", "pdf");
        fileExtensions.put(".jpg", "picture");
        fileExtensions.put(".jpeg", "picture");
        fileExtensions.put(".bmp", "picture");
        fileExtensions.put(".png", "picture");
        fileExtensions.put(".gif", "picture");
        fileExtensions.put(".ppt", "ppt");
        fileExtensions.put(".pptx", "ppt");
        fileExtensions.put(".txt", "text");
        fileExtensions.put(".doc", "word");
        fileExtensions.put(".docx", "word");
        fileExtensions.put(".zip", "zip");
        fileExtensions.put(".7z", "zip");
        fileExtensions.put(".tar", "zip");
        fileExtensions.put(".gz", "zip");
        fileExtensions.put(".rar", "zip");
    }

    /**
     * Creates new form FileItemPanel
     */
    public FileItemPanel() {
        initComponents();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("guzhijistudio/transfile/swing/Bundle");
        transProgress = bundle.getString("FileItemPanel.progress");
        transUnitSec = bundle.getString("FileItemPanel.unit.sec");
        transUnitMin = bundle.getString("FileItemPanel.unit.min");
        transUnitHr = bundle.getString("FileItemPanel.unit.hr");
        transUnitUnknown = bundle.getString("FileItemPanel.unit.unknown");

        jPopupMenu1.setVisible(false);
        jMenuItemResend.setVisible(false);
        jMenuItemRemove.setVisible(false);
    }

    public FileItemPanel(File file) {
        this();
        setFile(file);
    }

    public final void setResendAction(ActionListener actionListener) {
        jPopupMenu1.setVisible(true);
        jMenuItemResend.setVisible(true);
        jMenuItemResend.addActionListener(actionListener);
    }

    public final void setRemoveAction(ActionListener actionListener) {
        jPopupMenu1.setVisible(true);
        jMenuItemRemove.setVisible(true);
        jMenuItemRemove.addActionListener(actionListener);
    }

    private ImageIcon getIcon(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        String ext = pos > -1 ? name.substring(pos).toLowerCase() : "";
        String type = fileExtensions.get(ext);
        if (type == null) {
            type = "unknown";
        }
        return new ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/format_" + type + ".png"));
    }

    public final void setFile(File file) {
        this.file = file;
        jLabelFileName.setText(file.getName());
        jLabelFileSize.setText(formatSize(file.length()));
        jLabelIcon.setIcon(getIcon(file));
    }

    public final File getFile() {
        return file;
    }

    public final void setProgress(long progress, long total, long speed, long secs) {
        jProgressBar1.setMaximum(10000);
        jProgressBar1.setValue((int) (10000.0 * progress / total));
        jLabelFileSize.setText(String.format(
                transProgress,
                formatSize(progress),
                formatSize(total),
                formatSize(speed),
                formatTime(secs)));
        progressing = true;
    }

    public final void setProgressing(boolean progressing) {
        this.progressing = progressing;
        if (progressing) {
            done = false;
        }
    }

    public final boolean isProgressing() {
        return progressing;
    }

    public final void setError(String msg) {
        if (msg == null) {
            setToolTipText(null);
        } else {
            setToolTipText(msg);
            setBackground(Color.PINK);
            progressing = false;
        }
    }

    public final void setDone(boolean done) {
        this.done = done;
        if (done) {
            this.progressing = false;
            jLabelFileSize.setText(formatSize(file.length()));
        }
    }

    public final boolean isDone() {
        return done;
    }

    public final void setDestIp(String ip) {
        destIp = ip;
    }

    public final String getDestIp() {
        return destIp;
    }

    private static String formatSize(long size) {
        float s = size;
        if (s < 1024) {
            return Math.round(s * 100) / 100.0 + " bytes";
        }
        s /= 1024;
        if (s < 1024) {
            return Math.round(s * 100) / 100.0 + " Kb";
        }
        s /= 1024;
        if (s < 1024) {
            return Math.round(s * 100) / 100.0 + " Mb";
        }
        s /= 1024;
        return Math.round(s * 100) / 100.0 + " Gb";
    }

    private String formatTime(long secs) {
        float t = secs;
        if (t < 0) {
            return transUnitUnknown;
        }
        if (t <= 60) {
            return (int) t + transUnitSec;
        }
        t /= 60;
        if (t <= 60) {
            return Math.round(t * 100) / 100.0 + transUnitMin;
        }
        t /= 60;
        return Math.round(t * 100) / 100.0 + transUnitHr;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItemResend = new javax.swing.JMenuItem();
        jMenuItemRemove = new javax.swing.JMenuItem();
        jLabelIcon = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelFileName = new javax.swing.JLabel();
        jLabelFileSize = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("guzhijistudio/transfile/swing/Bundle"); // NOI18N
        jMenuItemResend.setText(bundle.getString("FileItemPanel.jMenuItemResend.text")); // NOI18N
        jPopupMenu1.add(jMenuItemResend);

        jMenuItemRemove.setText(bundle.getString("FileItemPanel.jMenuItemRemove.text")); // NOI18N
        jPopupMenu1.add(jMenuItemRemove);

        setBackground(java.awt.SystemColor.window);
        setMaximumSize(new java.awt.Dimension(310, 72));
        setMinimumSize(new java.awt.Dimension(310, 72));
        setPreferredSize(new java.awt.Dimension(310, 72));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
        });
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabelIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/guzhijistudio/transfile/swing/format_unkown.png"))); // NOI18N
        add(jLabelIcon);

        jPanel1.setMaximumSize(new java.awt.Dimension(260, 32767));
        jPanel1.setMinimumSize(new java.awt.Dimension(260, 87));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(260, 87));
        jPanel1.setLayout(new java.awt.GridLayout(3, 1));

        jLabelFileName.setFont(new java.awt.Font("宋体", 0, 24)); // NOI18N
        jLabelFileName.setText(bundle.getString("FileItemPanel.jLabelFileName.text")); // NOI18N
        jPanel1.add(jLabelFileName);

        jLabelFileSize.setText(bundle.getString("FileItemPanel.jLabelFileSize.text")); // NOI18N
        jPanel1.add(jLabelFileSize);
        jPanel1.add(jProgressBar1);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered

        setBackground(java.awt.SystemColor.control);

    }//GEN-LAST:event_formMouseEntered

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited

        setBackground(java.awt.SystemColor.window);

    }//GEN-LAST:event_formMouseExited

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked

        jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());

    }//GEN-LAST:event_formMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JLabel jLabelFileSize;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JMenuItem jMenuItemRemove;
    private javax.swing.JMenuItem jMenuItemResend;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}

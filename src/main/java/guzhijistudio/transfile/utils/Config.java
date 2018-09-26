package guzhijistudio.transfile.utils;

import guzhijistudio.transfile.swing.ConfigDialog;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;

public final class Config {

    public final static String CONFIG_FILE = "./transfile.ini";
    public static boolean LOADED = false;
    public static String DEVICE_NAME = "";
    public static String GROUP_ADDR = "";
    public static String DIR = "";

    static {
        File configFile = new File(CONFIG_FILE);
        if (configFile.canRead()) {
            try {
                Ini config = new Ini(configFile);
                Ini.Section section = config.get("transfile");
                DEVICE_NAME = section.get("device_name");
                GROUP_ADDR = section.get("group_addr");
                DIR = section.get("dir");
                LOADED = true;
            } catch (IOException ex) {
                LOADED = false;
            }
        } else {
            LOADED = false;
        }
    }

    public static boolean save() {
        File configFile = new File(CONFIG_FILE);
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            Ini config = new Ini(configFile);
            Ini.Section section = config.get("transfile");
            if (section == null) {
                section = config.add("transfile");
            }
            section.put("device_name", DEVICE_NAME);
            section.put("group_addr", GROUP_ADDR);
            section.put("dir", DIR);
            config.store();
            LOADED = true;
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ConfigDialog.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}

package org.example.util;

import org.example.properties.IniUtil;

import java.io.File;

public class PropertiesLoader {

    private static class SingleTon{
        private static PropertiesLoader instance = new PropertiesLoader();
    }

    public static PropertiesLoader getInstance(){
        return SingleTon.instance;
    }

    //    private String GON_HOME = System.getenv("GON_HOME");
    private String GON_HOME = "/public";
    private String CONF_PATH = GON_HOME+"/conf";
    private String DATA_PATH = GON_HOME+"/data";
    private IniUtil _gonutil = null;

    public PropertiesLoader() {
        try {
            this._gonutil = null;
            String targetPath = CONF_PATH+"/gon.properties";
            if (new File(targetPath).exists()) {
                System.out.println(targetPath+" is exist");
                this._gonutil = new IniUtil(targetPath);
            } else {
                System.out.println(targetPath+" is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IniUtil getProperties() {
        try {
            this._gonutil.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this._gonutil;
    }
}

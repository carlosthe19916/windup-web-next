package org.jboss.windup.web.services;

import java.io.InputStream;

public class Carlos {
    private final static String UI_ZIP_FILENAME = "windup-ui.zip";

    public static void main(String[] args) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(UI_ZIP_FILENAME);
        InputStream aa = Carlos.class.getResourceAsStream(UI_ZIP_FILENAME);
        InputStream bb = Carlos.class.getClassLoader().getResourceAsStream(UI_ZIP_FILENAME);

        InputStream zz = Thread.currentThread().getContextClassLoader().getResourceAsStream("openubl-realm.json");
        InputStream xx = Carlos.class.getResourceAsStream("openubl-realm.json");
        InputStream yy = Carlos.class.getClassLoader().getResourceAsStream("openubl-realm.json");
        System.out.println("");
    }
}

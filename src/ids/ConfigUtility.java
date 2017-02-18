/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ids;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
 
/**
 * A utility class that reads/saves SMTP settings from/to a properties file.
 * @author www.codejava.net
 *
 */
public class ConfigUtility {
    private File configFile = new File("smtp.properties");
    private Properties configProps;
     
    public Properties loadProperties() throws IOException {
        Properties defaultProps = new Properties();
        // sets default properties
        defaultProps.setProperty("mail.smtp.host", "smtp.gmail.com");
        defaultProps.setProperty("mail.smtp.port", "465");
        defaultProps.setProperty("mail.user", "ceochallengenigeria1@gmail.com");
        defaultProps.setProperty("mail.password", "Covenant2009.");
        defaultProps.setProperty("mail.smtp.starttls.enable", "true");
        defaultProps.setProperty("mail.smtp.auth", "true");
         
        configProps = new Properties(defaultProps);
         
        // loads properties from file
        if (configFile.exists()) {
            InputStream inputStream = new FileInputStream(configFile);
            configProps.load(inputStream);
            inputStream.close();
        }
         
        return configProps;
    }
     
    public void saveProperties(String host, String port, String user, String pass) throws IOException {
        configProps.setProperty("mail.smtp.host", host);
        configProps.setProperty("mail.smtp.port", port);
        configProps.setProperty("mail.user", user);
        configProps.setProperty("mail.password", pass);
        configProps.setProperty("mail.smtp.starttls.enable", "true");
        configProps.setProperty("mail.smtp.auth", "true");
         
        OutputStream outputStream = new FileOutputStream(configFile);
        configProps.store(outputStream, "host setttings");
        outputStream.close();
    }  
}
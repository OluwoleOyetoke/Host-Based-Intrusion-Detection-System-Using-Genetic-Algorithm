/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import jpcap.*;
import jpcap.packet.Packet;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;


/**
 *
 * @author olu
 */
public class IDS {

    static Controllers control = new Controllers();
    static CapAndPrint print = new CapAndPrint();
    
    static QueryDB query = new QueryDB();
    static Messages msg = new Messages();

    static Connection Connect;
    static Statement Statement;
    static ResultSet Result;
    static PreparedStatement Prepare;

    static java.util.Date UtilDate;
    static java.sql.Timestamp SQLTime;
    static Packet PacketToClone;

    static byte[][] MyMacAddresses;
    static NetworkInterface[] Interfaces;
    static File InputFile = new File("settings.xml");
    static SAXBuilder saxBuilder = new SAXBuilder();
    static XMLOutputter xmlout = new XMLOutputter();
    static WriteToFile writer = new WriteToFile();
    static ReadFile reader = new ReadFile();

    static volatile int InterfaceToOpen;
    static int TotalNumberOfInterfaces, TrafficTableCounter, SpoofedTableCounter;
    static boolean LearnAndDetect, AutoAdminAlert, SaveOnCapture, ActivateProtocolFilter, ActivatePortFilter, ClonedProcessRuning;
    static String ProtoFilterString, PortFilterString;

    static String[] ElementNames = {"ADMIN_EMAIL", "TRIGGER_VALUE", "PORT_FILTER",
        "ARP_FILTER", "ICMP_FILTER", "IGMP_FILTER", "IP_FILTER", "TCP_FILTER", 
        "UDP_FILTER", "ALLPROTOCOL_FILTER"};
    
    static String[] ToSave;
    static String TableToView = "";
    static String ActivationCode = "";
    static String Key = "";
    static String Version = "";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Imrove Look and Feel
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            msg.ErrorMessages("Look And Feel Error:" + ex);
        }

        //Connect to Lite Database
        try {
            Class.forName("org.sqlite.JDBC");
            Connect = DriverManager.getConnection("jdbc:sqlite:IDS_DB.db");
            Statement = Connect.createStatement();

            String Query = "delete from TRAFFIC_TABLE";
            Statement.executeUpdate(Query);
             String Query2 = "delete from SPOOFED_TABLE";
            Statement.executeUpdate(Query2);

        } catch (Exception e) {
            msg.ErrorMessages(e.getClass().getName() + ": " + e.getMessage());
           // System.out.println("Error: " + e);
            System.exit(0);
        }
        
          Version = "FULL";
          InitializeParametres();

    }

    public static void InitializeParametres() {
        TotalNumberOfInterfaces = 0;
        TrafficTableCounter = 0;
        SpoofedTableCounter = 0;
        LearnAndDetect = false;
        SaveOnCapture = false;
        AutoAdminAlert = false;
        ActivateProtocolFilter = false;
        ActivatePortFilter = false;
        ProtoFilterString = "";
        PortFilterString = "";
        ClonedProcessRuning = false;
        IDS.ToSave = query.ProbeXML(ElementNames);
        IDS.query.GenerateFilterParameter();
        MainFrame main = new MainFrame();
        main.setVisible(true);
    }

}

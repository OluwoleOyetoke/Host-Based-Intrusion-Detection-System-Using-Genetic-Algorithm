/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * S
 *
 * @author olu
 */
public class QueryDB {

    synchronized void InsertIntoTable(String TableName, String... parametres) { //synchronizing the method and the classes object. Double security against deadlocks
        String Columns[];

        try {

            IDS.Statement = IDS.Connect.createStatement();

            String Query1 = "select * from " + TableName + "";

            IDS.Result = IDS.Statement.executeQuery(Query1);

            Columns = TableDetails(IDS.Result);

            String ConcatenatednatedColumnString = Columns[0] + ", ";

            String ConcatenatednatedparametreString = "'" + parametres[0] + "' , '";

            //Code will be structured such that number of parametres will always be equal to the number of table colums
            for (int i = 1; i < Columns.length; i++) {

                if (i == Columns.length - 1) {

                    ConcatenatednatedColumnString = ConcatenatednatedColumnString + "" + Columns[i] + "";

                } else {
                    ConcatenatednatedColumnString = ConcatenatednatedColumnString + "" + Columns[i] + ", ";

                }

            }
            // System.out.print(ConcatenatednatedColumnString);
            for (int i = 1; i < parametres.length; i++) {
                if (i == parametres.length - 1) {
                    ConcatenatednatedparametreString = ConcatenatednatedparametreString + "" + parametres[i] + "'";
                } else {
                    ConcatenatednatedparametreString = ConcatenatednatedparametreString + "" + parametres[i] + "' , '";
                }
            }

            String Query = "insert into " + TableName + " (" + ConcatenatednatedColumnString + ") values(" + ConcatenatednatedparametreString + ");";
            //System.out.println(Query);

            IDS.Statement.executeUpdate(Query);

        } catch (Exception ex) {

            IDS.msg.ErrorMessages("DB Record Error: " + ex);
        }

    }

    public String[][] RetreiveAllInformation(String DatabaseName) {

        String[] Columns, Columns2;
        int Counter = 0, TotalRowNumber = 0, ColumnsLength = 0;

        try {

            String Query = "select * from " + DatabaseName + "";

            IDS.Statement = IDS.Connect.createStatement();
            IDS.Result = IDS.Statement.executeQuery(Query);
            Columns = TableDetails(IDS.Result);
            ColumnsLength = Columns.length;
            while (IDS.Result.next()) {
                Counter = Counter + 1;
            }
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("Retreive All Table Information Error 1" + ex);
        }

        TotalRowNumber = Counter;
        Columns2 = new String[ColumnsLength];
        String[][] ResultStorage = new String[TotalRowNumber][ColumnsLength];
        Counter = 0;

        try {

            String Query = "select * from " + DatabaseName + "";
            IDS.Result = IDS.Statement.executeQuery(Query);
            Columns = TableDetails(IDS.Result);
            String Query2 = "select * from " + DatabaseName + "";
            IDS.Result = IDS.Statement.executeQuery(Query2);

            while (IDS.Result.next()) {

                for (int j = 0; j < ColumnsLength; j++) {
                    ResultStorage[Counter][j] = IDS.Result.getString(Columns[j]);
                    ResultStorage[Counter][j] = ResultStorage[Counter][j].trim(); //Neccessary
                    //   System.out.println(ResultStorage[Counter][j]);
                }
                Counter = Counter + 1;
            }
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("Retreive All Table Information Error 2 " + ex);
        }
        String[][] ResultStorage2 = new String[TotalRowNumber][ColumnsLength];

        return ResultStorage;
    }

    public synchronized String[] RetreiveSpecificColumnofInformation1(String TableName, String ColumnName, String Column1, String Parametre1, String Column2, String Parametre2, String Column3, String Parametre3) throws SQLException { //When 2 parametres are used

        String[] Columns;
        // Statement Statement;
        //ResultSet Result;
        int Counter = 0, TotalRowNumber = 0;
        IDS.Statement = IDS.Connect.createStatement();
        try {

            // String Query = "select * from "+TableName+"";
            String Query = "select * from " + TableName + " where " + Column1 + "= '" + Parametre1 + "' and " + Column2 + "='" + Parametre2 + "' and " + Column3 + "='" + Parametre3 + "'order by " + ColumnName + " desc";
//System.out.println(Query);   

            IDS.Result = IDS.Statement.executeQuery(Query);

            Columns = TableDetails(IDS.Result);

            while (IDS.Result.next() && Counter <= 10) {
                Counter = Counter + 1;
            }

            if (Counter == 0) {

                return null;

            }

        } catch (Exception ex) {
            IDS.msg.ErrorMessages("There are No columns to deal with" + ex);
        }
        TotalRowNumber = Counter;
        String[] ResultStorage = new String[TotalRowNumber];

        Counter = 0;
        try {
            String Query = "select * from " + TableName + " where " + Column1 + "='" + Parametre1 + "' and " + Column2 + "='" + Parametre2 + "' and " + Column3 + "='" + Parametre3 + "' order by " + ColumnName + " desc";

            //System.out.println(Query);
            IDS.Result = IDS.Statement.executeQuery(Query);

            while (IDS.Result.next() && Counter <= 10) {

                ResultStorage[Counter] = IDS.Result.getString(ColumnName);
                ResultStorage[Counter] = ResultStorage[Counter].trim(); //Neccessary
                // System.out.println(ResultStorage[Counter]);

                Counter = Counter + 1;
            }
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("Column Reading Error 1: " + ex);
        }
        // System.out.println("Result length At Query: "+ResultStorage.length);
        return ResultStorage;

    }

    public synchronized String[] RetreiveSpecificColumnofInformation2(String TableName, String ColumnName, String Column1, String Parametre1, String Column2, String Parametre2, String Column3, String Parametre3) throws SQLException { //When 3 parametres are used

        String[] Columns;
        //Statement Statement;
        //ResultSet Result;
        int Counter = 0, TotalRowNumber = 0;
        IDS.Statement = IDS.Connect.createStatement();
        try {

            // String Query = "select * from "+TableName+"";
            String Query = "select * from " + TableName + " where " + Column1 + "= '" + Parametre1 + "' and " + Column2 + "='" + Parametre2 + "' and " + Column3 + "='" + Parametre3 + "'order by INSTANT_FITNESS desc";

//System.out.println(Query);   
            IDS.Result = IDS.Statement.executeQuery(Query);

            Columns = TableDetails(IDS.Result);
            while (IDS.Result.next() && Counter <= 10) {
                Counter = Counter + 1;
            }
            if (Counter == 0) {

                return null;

            }
        } catch (Exception ex) {
            IDS.msg.ErrorMessages("There are No columns to deal with" + ex);
        }
        TotalRowNumber = Counter;
        String[] ResultStorage = new String[TotalRowNumber];
        Counter = 0;
        try {
            String Query = "select * from " + TableName + " where " + Column1 + "= '" + Parametre1 + "' and " + Column2 + "='" + Parametre2 + "' and " + Column3 + "='" + Parametre3 + "'order by INSTANT_FITNESS desc";

//System.out.println(Query);
            IDS.Result = IDS.Statement.executeQuery(Query);

            while (IDS.Result.next() && Counter <= 10) { //Picks 10

                ResultStorage[Counter] = IDS.Result.getString(ColumnName);
                ResultStorage[Counter] = ResultStorage[Counter].trim(); //Neccessary
                //System.out.println(ResultStorage[Counter]);

                Counter = Counter + 1;
            }
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("Column Reading Error 2: " + ex);
        }
        //System.out.println("Result length At Query: "+ResultStorage.length);
        return ResultStorage;
        //  return null;
    }

    public int NumberOfRows(String TableName, String Column1, String Column2, String Column3, String Column4, String Parametre1, String Parametre2, String Parametre3, String Parametre4) { //When three check conditions are present

        int Counter = 0, TotalRowNumber = 0;
        String[] Columns;
        try {

            String Query = "select * from " + TableName + " where " + Column1 + "='" + Parametre1 + "' and " + Column2 + "='" + Parametre2 + "' and " + Column3 + "='" + Parametre3 + "' and " + Column4 + "='" + Parametre4 + "' order by " + Column1 + " asc";;

            IDS.Statement = IDS.Connect.createStatement();
            IDS.Result = IDS.Statement.executeQuery(Query);
            Columns = TableDetails(IDS.Result);

            while (IDS.Result.next()) {
                Counter = Counter + 1;
            }
        } catch (Exception ex) {
            IDS.msg.ErrorMessages("" + ex);
        }
        TotalRowNumber = Counter;

        return TotalRowNumber;
    }

    public String[] TableDetails(ResultSet rs) throws SQLException {

        boolean toadd = true;
        ResultSetMetaData rsMeta = rs.getMetaData();
        int numberofcolumns = rsMeta.getColumnCount();
        String[] ColumnNames = new String[numberofcolumns];
        //column index starts from 1
        for (int i = 0; i < numberofcolumns; i++) {
            ColumnNames[i] = rsMeta.getColumnName(i + 1); //Get Table's Column Names
            // System.out.print(","+ColumnNames[i]+",]");
        }

        return ColumnNames;
    }

    public String[] TableDetails2(String TableName) throws SQLException {
        String Query = "select * from " + TableName + "";

        IDS.Statement = IDS.Connect.createStatement();
        IDS.Result = IDS.Statement.executeQuery(Query);
        boolean toadd = true;
        ResultSetMetaData rsMeta = IDS.Result.getMetaData();
        int numberofcolumns = rsMeta.getColumnCount();
        String[] ColumnNames = new String[numberofcolumns];
        //column index starts from 1
        for (int i = 0; i < numberofcolumns; i++) {
            ColumnNames[i] = rsMeta.getColumnName(i + 1); //Get Table's Column Names
            //  System.out.println(ColumnNames[i]+",");
        }
        return ColumnNames;
    }

    public void UpdateTable(String TableName, String[] Locationstoupdate, String[] ToAdd, String LocationOfReference, String Condition) {

        if (Locationstoupdate.length != ToAdd.length) {
            IDS.msg.ErrorMessages("Unequal number of parametres sent");
            return;
        }

        String[] ResultStorage = new String[ToAdd.length];
        String ConcatenatednatedparametreString;
        if (ToAdd.length > 1) {
            ConcatenatednatedparametreString = Locationstoupdate[0] + "=? ,";
            for (int i = 1; i < ToAdd.length; i++) {
                if (i == ToAdd.length - 1) {
                    ConcatenatednatedparametreString = ConcatenatednatedparametreString + "" + Locationstoupdate[i] + "=?";
                } else {
                    ConcatenatednatedparametreString = ConcatenatednatedparametreString + "" + Locationstoupdate[i] + "=? , ";
                }
            }
        } else {
            ConcatenatednatedparametreString = Locationstoupdate[0] + "=? ";
        }

        int counter = 0;
        try {
            String Query = "update " + TableName + " set " + ConcatenatednatedparametreString + " where " + LocationOfReference + "=?";
            //   System.out.println(Query);
            int c = 1;
            IDS.Prepare = IDS.Connect.prepareStatement(Query);
            for (int i = 0; i < ToAdd.length; i++) {
                IDS.Prepare.setString(i + 1, ToAdd[i]);
                c = c + 1;
            }
            IDS.Prepare.setString(c, Condition);
            IDS.Prepare.executeUpdate();

            //  IDS.msg.InformationMessages("Action successfull");
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("" + ex);
        }

    }

    public ResultSet GetTableResultSet(String TableName) {

        try {
            IDS.Statement = IDS.Connect.createStatement();

            String Query = "select * from " + TableName + "";
            IDS.Result = IDS.Statement.executeQuery(Query);
        } catch (Exception ex) {

            IDS.msg.ErrorMessages("" + ex);

        }

        return IDS.Result;
    }

    public String[] ProbeXML(String[] ElementNames) {
        String[] ToSave = {"test@gmail.com", "50", "0", "false", "false", "false", "false", "false", "false", "true"};
        try {

            File inputFile = new File("settings.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

//Get All Child nodes with the name preferences
            NodeList ChildNodeLists = doc.getElementsByTagName("settings");
            NodeList AvailableNodes = doc.getChildNodes();
            Node NodesInThem1 = AvailableNodes.item(0);
            Element ElementsofNodesInThem = (Element) NodesInThem1;
            for (int temp = 0; temp < 10; temp++) {
                NodeList ElementsThemselves = ElementsofNodesInThem.getElementsByTagName(ElementNames[temp]);
                Node ToExtract = ElementsThemselves.item(0);
                Element ValInElementForm = (Element) ToExtract;
                ToSave[temp] = (String) ValInElementForm.getTextContent();
            }

        } catch (Exception ex) {
            IDS.msg.ErrorMessages("Error: " + ex);
        }

        return ToSave;
    }

    public void GenerateFilterParameter() {

        String[] Ports = {"FILTER ALL", "HTTP - port 80", "SSL - port 443", "FTP - port 21", "SSH - port 22", "TELNET - port 23", "SMTP - port 25", "POP3 - port 110", "IMAP - port 143", "IMAP3 - port 993", "DNS - port 53", "NETBIOS - port 139", "SAMBA - port 137", "AD - port 445", "LDAP - port 389", "SQL - port 156"};
//Generate port filter string
        if (Integer.parseInt(IDS.ToSave[2]) == 0) {
            IDS.ActivatePortFilter = false;
        } else {
            IDS.ActivatePortFilter = true;
            String[] get = Ports[Integer.parseInt(IDS.ToSave[2])].split(" ");
            IDS.PortFilterString = get[2] + " " + get[3];

        }

        //Generate Protocol Filter String
        String[] Protocols = {"arp", "icmp", "igmp", "ip", "tcp", "udp"};
        String ProtoFilterString = "";
        if (Boolean.parseBoolean(IDS.ToSave[9]) == true) {
            IDS.ActivateProtocolFilter = false;
        } else {
            IDS.ActivateProtocolFilter = true;
            for (int i = 3; i < 9; i++) {

                switch (IDS.ToSave[i]) {
                    case "true":
                        ProtoFilterString = ProtoFilterString + Protocols[i - 3] + " or ";
                        break;
                    case "false":

                        break;
                }
            }
            IDS.ProtoFilterString = ProtoFilterString.substring(0, ProtoFilterString.length() - 4);

        }

    }

}

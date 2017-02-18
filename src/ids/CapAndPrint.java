/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.sql.SQLException;
import jpcap.*;
import jpcap.packet.Packet;

/**
 *
 * @author olu
 */
public class CapAndPrint implements PacketReceiver {

    CapAndPrint() {

    }

    @Override
    public synchronized void receivePacket(Packet packet) {

        int OctetCounter = 0;
        String ProtocolType = "OUT OF SCOPE";
        String PacketHeader = "";
//Get Packet Header Details
        for (byte b : packet.header) {
            PacketHeader = PacketHeader + Integer.toHexString(b & 0xff) + ":";
            //System.out.print( Integer.toHexString(b&0xff)+":");  
            OctetCounter = OctetCounter + 1;
            if (OctetCounter == 24) {
                ProtocolType = Integer.toHexString(b & 0xff);
            }
        }

        //Print Packet Details
        //  System.out.println(PacketHeader.substring(0, PacketHeader.length()-1)); 
        if (IDS.LearnAndDetect == true) {

            TextAreaAppend(packet.toString() + "\n");
        } else {
            TextAreaAppend(packet.toString() + "\n\n");
        }

        if (IDS.LearnAndDetect == true) {
            try {
                IDS.control.AnalyzePacket(ProtocolType, packet);

            } catch (SQLException ex) {
                IDS.msg.ErrorMessages("Printer Class Encountered Error: " + ex);
            }

        }
    }

    public synchronized void TextAreaAppend(String Message) {

        System.out.print(Message);
        // MainFrame.TextArea.append(Message);
    }

}

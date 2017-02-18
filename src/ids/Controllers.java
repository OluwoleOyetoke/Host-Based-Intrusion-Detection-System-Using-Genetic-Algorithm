/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;
import jpcap.*;
import jpcap.packet.ARPPacket;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

/**
 *
 * @author olu
 */
public class Controllers {

    CapAndPrint print = new CapAndPrint();
    QueryDB query = new QueryDB();

    public synchronized void GetAvailableInterfaces() {

//Obtain List of Network Interfaces
        IDS.Interfaces = JpcapCaptor.getDeviceList();
        IDS.TotalNumberOfInterfaces = IDS.Interfaces.length;
        IDS.MyMacAddresses = new byte[IDS.TotalNumberOfInterfaces][];
        Vector list = new Vector();

        list.clear();
        //   JListModel listmodel =  MainFrame.InterfacesList.getModel();
        MainFrame.TextArea.setText("");
        int counter = 0;
        for (int i = 0; i < IDS.Interfaces.length; i++) {
            counter = counter + 1;

            print.TextAreaAppend("-------------------------INFORMATION ON NETWORK INTERFACE " + counter + "-------------------------");

            print.TextAreaAppend("\nName: " + IDS.Interfaces[i].name);
            print.TextAreaAppend("\nDataLink Name: " + IDS.Interfaces[i].datalink_name);

            print.TextAreaAppend("\nDataLink Description: " + IDS.Interfaces[i].datalink_description);
            print.TextAreaAppend("\nGeneral Description: " + IDS.Interfaces[i].description);
            list.add("INTERFACE " + counter + ": " + IDS.Interfaces[i].description);
            print.TextAreaAppend("\nLoop Back: " + IDS.Interfaces[i].loopback);
            print.TextAreaAppend("\nIP Address: ");
            for (NetworkInterfaceAddress c : IDS.Interfaces[i].addresses) {

                print.TextAreaAppend(c.address.toString());

            }

            //  NetworkInterfaceAddress[] b = IDS.Interfaces[i].addresses;
            //   print.TextAreaAppend(b[0].address.toString());
            print.TextAreaAppend("\nMAC Address: ");
            for (byte c : IDS.Interfaces[i].mac_address) {
                print.TextAreaAppend(Integer.toHexString(c & 0xff) + ":");
            }
            //  IDS.MyMacAddresses[i][0] =  IDS.Interfaces[i].mac_address; 
            print.TextAreaAppend("\n");
            print.TextAreaAppend("\n");
        }

        MainFrame.InterfacesList.setListData(list);

    }

    public void AnalyzePacket(String Value, Packet packet) throws SQLException {
        Controllers control = new Controllers();

        //DECIFER ICMP DETAILS
        if (Value.equals("01")) {
            ICMPPacket icmp;
            icmp = (ICMPPacket) packet;

            DatalinkPacket DLayer = icmp.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;

            // Get the src and dest IP addresses from the IP layer
            InetAddress destIp = icmp.dst_ip;
            InetAddress srcIp = icmp.src_ip;

            //Get ICMP Packet ID and Initial TTL
            int PacketID = icmp.ident;
            short TTL = icmp.hop_limit;

            //Get packet hop count
            int HopCount = GetHopCount(TTL);

            String Protocol = "ICMP";

            print.TextAreaAppend("ICMP Source IP Address: " + srcIp.toString().replace("/", "") + "\n");
            print.TextAreaAppend("Source MAC Address Address: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC Address: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Initial Time To Live: " + TTL + "\n");
            print.TextAreaAppend("Hop Count: " + HopCount + "\n");
            print.TextAreaAppend("Packet ID: " + PacketID + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n");

            //Develop Packet Chromosome
            String Chromosome = GenerateChromosomes(srcIp, Ether, HopCount, TTL, Protocol, PacketID);
            double Fitness = GetInstantFitness(Ether, srcIp, Chromosome, Protocol);
            int SpoofDetect = DiscoverToAlert(Fitness, Protocol, srcIp.toString().replace("/", ""), Ether.getSourceAddress());

            //Get Current Time
            IDS.UtilDate = new java.util.Date();
            IDS.SQLTime = new java.sql.Timestamp(IDS.UtilDate.getTime());

//Insert Into Database
            synchronized (IDS.query) {
                if (SpoofDetect == 0) {
                    IDS.TrafficTableCounter = IDS.TrafficTableCounter + 1;
                    IDS.query.InsertIntoTable("TRAFFIC_TABLE", String.valueOf(IDS.TrafficTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), destIp.toString().replace("/", ""), Ether.getDestinationAddress(),
                            String.valueOf(TTL), String.valueOf(HopCount), String.valueOf(PacketID), IDS.SQLTime.toString(), Chromosome, Protocol, String.valueOf(Fitness));

                } else if (SpoofDetect == 1) {
                    IDS.SpoofedTableCounter = IDS.SpoofedTableCounter + 1;
                    IDS.query.InsertIntoTable("SPOOFED_TABLE", String.valueOf(IDS.SpoofedTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), Ether.getDestinationAddress(), String.valueOf(PacketID), Protocol, Chromosome, String.valueOf(Fitness), IDS.SQLTime.toString());

                } else {

                }
            }

            //Check To Alert
            if (IDS.AutoAdminAlert == true) {
                String Message = "<font face='cambria' color='black'>Dear Admin,<p>My computer has just received a suspicious packet which I belive has a spoofed IP address. Please find the details below</p><br />"
                        + "<p><u><b>SUSPICIOUS PACKET'S DETAILS</b></u></p>"
                        + "<p><ul><li><b>Source IP Address: </b>" + srcIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Source MAC Address: </b>" + Ether.getSourceAddress() + "</li>"
                        + "<li><b>Destination MAC Address: </b>" + Ether.getDestinationAddress() + "</li>"
                        + "<li><b>Initial Time To Live: </b>" + TTL + "</li>"
                        + "<li><b>Hop Count: </b>" + HopCount + "</li>"
                        + "<li><b>Packet ID: </b>" + PacketID + "</li>"
                        + "<li><b>Protocol: </b>" + Protocol + "</li>"
                        + "<li><b>Time of Occurence: </b>" + IDS.SQLTime.toString() + "</li></ul>"
                        + "<p>Also, find my computer's details below</p>"
                        + "<u><b>MY COMPUTER'S DETAILS</u></b>"
                        + "<p><ul><li><b>My IP Address: </b>" + destIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Receiving Intrface's MAC Address: </b>" + Ether.getDestinationAddress() + "</li></ul><br />"
                        + "<p>Thank you very much as you help look into it</p>"
                        + "<p><b> Your Client</b></p></font>";
                AlertAdmin(Message, Ether.getDestinationAddress());

            }

            print.TextAreaAppend("Time Stamp: " + IDS.SQLTime.toString() + "\n\n\n");

        }

        //DECIFER IPv4 or IPv6 DETAILS
        if (Value.equals("04") || Value.equals("29")) {
            IPPacket ip;
            ip = (IPPacket) packet;
            DatalinkPacket DLayer = ip.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;

// Get the src and dest IP addresses from the IP layer
            InetAddress destIp = ip.dst_ip;
            InetAddress srcIp = ip.src_ip;

            short TTL = ip.hop_limit;
            int PacketID = ip.ident;

            //Get packet hop count
            int HopCount = GetHopCount(TTL);

            String Protocol = "IP";

            print.TextAreaAppend("IP Source IP Address: " + srcIp.toString().replace("/", "") + "\n");

            print.TextAreaAppend("Source MAC Address Address: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC Address: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Initial Time To Live: " + TTL + "\n");
            print.TextAreaAppend("Hop Count: " + HopCount + "\n");
            print.TextAreaAppend("Packet ID: " + PacketID + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n");

            //Develop Packet Chromosome
            String Chromosome = GenerateChromosomes(srcIp, Ether, HopCount, TTL, Protocol, PacketID);
            double Fitness = GetInstantFitness(Ether, srcIp, Chromosome, Protocol);
            int SpoofDetect = DiscoverToAlert(Fitness, Protocol, srcIp.toString().replace("/", ""), Ether.getSourceAddress());

            //Get Current Time
            IDS.UtilDate = new java.util.Date();
            IDS.SQLTime = new java.sql.Timestamp(IDS.UtilDate.getTime());

//Insert Into Database
            synchronized (IDS.query) {
                if (SpoofDetect == 0) {
                    IDS.TrafficTableCounter = IDS.TrafficTableCounter + 1;
                    IDS.query.InsertIntoTable("TRAFFIC_TABLE", String.valueOf(IDS.TrafficTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), destIp.toString().replace("/", ""), Ether.getDestinationAddress(),
                            String.valueOf(TTL), String.valueOf(HopCount), String.valueOf(PacketID), IDS.SQLTime.toString(), Chromosome, Protocol, String.valueOf(Fitness));

                } else if (SpoofDetect == 1) {
                    IDS.SpoofedTableCounter = IDS.SpoofedTableCounter + 1;
                    IDS.query.InsertIntoTable("SPOOFED_TABLE", String.valueOf(IDS.SpoofedTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), Ether.getDestinationAddress(), String.valueOf(PacketID), Protocol, Chromosome, String.valueOf(Fitness), IDS.SQLTime.toString());

                } else {

                }
            }

            //Check To Alert
            if (IDS.AutoAdminAlert == true) {
                String Message = "<font face='cambria' color='black'>Dear Admin,<p>My computer has just received a suspicious packet which I belive has a spoofed IP address. Please find the details below</p><br />"
                        + "<p><u><b>SUSPICIOUS PACKET'S DETAILS</b></u></p>"
                        + "<p><ul><li><b>Source IP Address: </b>" + srcIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Source MAC Address: </b>" + Ether.getSourceAddress() + "</li>"
                        + "<li><b>Destination MAC Address: </b>" + Ether.getDestinationAddress() + "</li>"
                        + "<li><b>Initial Time To Live: </b>" + TTL + "</li>"
                        + "<li><b>Hop Count: </b>" + HopCount + "</li>"
                        + "<li><b>Packet ID: </b>" + PacketID + "</li>"
                        + "<li><b>Protocol: </b>" + Protocol + "</li>"
                        + "<li><b>Time of Occurence: </b>" + IDS.SQLTime.toString() + "</li></ul>"
                        + "<p>Also, find my computer's details below</p>"
                        + "<u><b>MY COMPUTER'S DETAILS</u></b>"
                        + "<p><ul><li><b>My IP Address: </b>" + destIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Receiving Intrface's MAC Address: </b>" + Ether.getDestinationAddress() + "</li></ul><br />"
                        + "<p>Thank you very much as you help look into it</p>"
                        + "<p><b> Your Client</b></p></font>";
                AlertAdmin(Message, Ether.getDestinationAddress());

            }

            print.TextAreaAppend("Time Stamp: " + IDS.SQLTime.toString() + "\n\n\n");

        } // DECIFER UDP PACKET SOURCE ADDRESS 
        // else if(Value.equals("11")){
        else if (packet instanceof UDPPacket) {
            UDPPacket udp;
            udp = (UDPPacket) packet;
            DatalinkPacket DLayer = udp.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;

            // Get the tcp src and dest ports
            int destPort = udp.dst_port;
            int srcPort = udp.src_port;
// Get the src and dest IP addresses from the IP layer
            InetAddress destIp = udp.dst_ip;
            InetAddress srcIp = udp.src_ip;

            short TTL = udp.hop_limit;
//System.out.println("Ether MAC"+Ether.getSourceAddress().getBytes());
//Get Actual Hop Count by subtracting final value from the least possible value
//This is because hop count reduces as it scales throuh the network
            int HopCount = GetHopCount(TTL);

            String Protocol = "UDP";
            int PacketID = udp.ident;
            print.TextAreaAppend("UDP Source IP Address: " + srcIp.toString().replace("/", "") + "\n");
            print.TextAreaAppend("Destination Port: " + destPort + "\n");
            print.TextAreaAppend("Source MAC Address Address: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC Address: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Initial Time To Live: " + TTL + "\n");
            print.TextAreaAppend("Hop Count: " + HopCount + "\n");
            print.TextAreaAppend("Packet ID: " + PacketID + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n");
            // System.out.println("Current Thread Name: " + Thread.currentThread().getName());

            //Develop Packet Chromosome
            String Chromosome = GenerateChromosomes(srcIp, Ether, HopCount, TTL, Protocol, PacketID);
            double Fitness = GetInstantFitness(Ether, srcIp, Chromosome, Protocol);
            int SpoofDetect = DiscoverToAlert(Fitness, Protocol, srcIp.toString().replace("/", ""), Ether.getSourceAddress());

            //Get Current Time
            IDS.UtilDate = new java.util.Date();
            IDS.SQLTime = new java.sql.Timestamp(IDS.UtilDate.getTime());

//Insert Into Database
            synchronized (IDS.query) {
                if (SpoofDetect == 0) {
                    IDS.TrafficTableCounter = IDS.TrafficTableCounter + 1;
                    IDS.query.InsertIntoTable("TRAFFIC_TABLE", String.valueOf(IDS.TrafficTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), destIp.toString().replace("/", ""), Ether.getDestinationAddress(),
                            String.valueOf(TTL), String.valueOf(HopCount), String.valueOf(PacketID), IDS.SQLTime.toString(), Chromosome, Protocol, String.valueOf(Fitness));

                } else if (SpoofDetect == 1) {
                    IDS.SpoofedTableCounter = IDS.SpoofedTableCounter + 1;
                    IDS.query.InsertIntoTable("SPOOFED_TABLE", String.valueOf(IDS.SpoofedTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), Ether.getDestinationAddress(), String.valueOf(PacketID), Protocol, Chromosome, String.valueOf(Fitness), IDS.SQLTime.toString());

                } else {

                }
            }

            //Check To Alert
            if (IDS.AutoAdminAlert == true) {
                String Message = "<font face='cambria' color='black'>Dear Admin,<p>My computer has just received a suspicious packet which I belive has a spoofed IP address. Please find the details below</p><br />"
                        + "<p><u><b>SUSPICIOUS PACKET'S DETAILS</b></u></p>"
                        + "<p><ul><li><b>Source IP Address: </b>" + srcIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Source MAC Address: </b>" + Ether.getSourceAddress() + "</li>"
                        + "<li><b>Destination MAC Address: </b>" + Ether.getDestinationAddress() + "</li>"
                        + "<li><b>Initial Time To Live: </b>" + TTL + "</li>"
                        + "<li><b>Hop Count: </b>" + HopCount + "</li>"
                        + "<li><b>Packet ID: </b>" + PacketID + "</li>"
                        + "<li><b>Protocol: </b>" + Protocol + "</li>"
                        + "<li><b>Time of Occurence: </b>" + IDS.SQLTime.toString() + "</li></ul>"
                        + "<p>Also, find my computer's details below</p>"
                        + "<u><b>MY COMPUTER'S DETAILS</u></b>"
                        + "<p><ul><li><b>My IP Address: </b>" + destIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Receiving Intrface's MAC Address: </b>" + Ether.getDestinationAddress() + "</li></ul><br />"
                        + "<p>Thank you very much as you help look into it</p>"
                        + "<p><b> Your Client</b></p></font>";
                AlertAdmin(Message, Ether.getDestinationAddress());

            }

            print.TextAreaAppend("Time Stamp: " + IDS.SQLTime.toString() + "\n\n\n");

        } //Decipher TCP Packet Details
        // else if(Value.equals("6")){
        else if (packet instanceof TCPPacket) {
            TCPPacket tcp;
            tcp = (TCPPacket) packet;

            DatalinkPacket DLayer = tcp.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;
            // Get the tcp src and dest ports
            int destPort = tcp.dst_port;
            int srcPort = tcp.src_port;
// Get the src and dest IP addresses from the IP layer
            InetAddress destIp = tcp.dst_ip;
            InetAddress srcIp = tcp.src_ip;
            short TTL = tcp.hop_limit;
            int PacketID = tcp.ident;

            int HopCount = GetHopCount(TTL);

            String Protocol = "TCP";

            print.TextAreaAppend("TCP Source IP Address: " + srcIp.toString().replace("/", "") + "\n");
            print.TextAreaAppend("Destination Port: " + destPort + "\n");
            print.TextAreaAppend("Source MAC Address Address: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC Address: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Initial Time To Live: " + TTL + "\n");
            print.TextAreaAppend("Hop Count: " + HopCount + "\n");
            print.TextAreaAppend("Packet ID: " + PacketID + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n");

            //Develop Packet Chromosome
            String Chromosome = GenerateChromosomes(srcIp, Ether, HopCount, TTL, Protocol, PacketID);
            double Fitness = GetInstantFitness(Ether, srcIp, Chromosome, Protocol);
            int SpoofDetect = DiscoverToAlert(Fitness, Protocol, srcIp.toString().replace("/", ""), Ether.getSourceAddress());

            //Get Current Time
            IDS.UtilDate = new java.util.Date();
            IDS.SQLTime = new java.sql.Timestamp(IDS.UtilDate.getTime());

//Insert Into Database
            synchronized (IDS.query) {
                if (SpoofDetect == 0) {
                    IDS.TrafficTableCounter = IDS.TrafficTableCounter + 1;
                    IDS.query.InsertIntoTable("TRAFFIC_TABLE", String.valueOf(IDS.TrafficTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), destIp.toString().replace("/", ""), Ether.getDestinationAddress(),
                            String.valueOf(TTL), String.valueOf(HopCount), String.valueOf(PacketID), IDS.SQLTime.toString(), Chromosome, Protocol, String.valueOf(Fitness));

                } else if (SpoofDetect == 1) {
                    IDS.SpoofedTableCounter = IDS.SpoofedTableCounter + 1;
                    IDS.query.InsertIntoTable("SPOOFED_TABLE", String.valueOf(IDS.SpoofedTableCounter), srcIp.toString().replace("/", ""), Ether.getSourceAddress(), Ether.getDestinationAddress(), String.valueOf(PacketID), Protocol, Chromosome, String.valueOf(Fitness), IDS.SQLTime.toString());

                } else {

                }
            }

            //Check To Alert
            if (IDS.AutoAdminAlert == true) {
                String Message = "<font face='cambria' color='black'>Dear Admin,<p>My computer has just received a suspicious packet which I belive has a spoofed IP address. Please find the details below</p><br />"
                        + "<p><u><b>SUSPICIOUS PACKET'S DETAILS</b></u></p>"
                        + "<p><ul><li><b>Source IP Address: </b>" + srcIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Source MAC Address: </b>" + Ether.getSourceAddress() + "</li>"
                        + "<li><b>Destination MAC Address: </b>" + Ether.getDestinationAddress() + "</li>"
                        + "<li><b>Initial Time To Live: </b>" + TTL + "</li>"
                        + "<li><b>Hop Count: </b>" + HopCount + "</li>"
                        + "<li><b>Packet ID: </b>" + PacketID + "</li>"
                        + "<li><b>Protocol: </b>" + Protocol + "</li>"
                        + "<li><b>Time of Occurence: </b>" + IDS.SQLTime.toString() + "</li></ul>"
                        + "<p>Also, find my computer's details below</p>"
                        + "<u><b>MY COMPUTER'S DETAILS</u></b>"
                        + "<p><ul><li><b>My IP Address: </b>" + destIp.toString().replace("/", "") + "</li>"
                        + "<li><b>Receiving Intrface's MAC Address: </b>" + Ether.getDestinationAddress() + "</li></ul><br />"
                        + "<p>Thank you very much as you help look into it</p>"
                        + "<p><b> Your Client</b></p></font>";
                AlertAdmin(Message, Ether.getDestinationAddress());

            }

            print.TextAreaAppend("Time Stamp: " + IDS.SQLTime.toString() + "\n\n\n");

        } //DECIFER ARP REQUEST 
        else if (packet instanceof ARPPacket) {
            ARPPacket arpp = (ARPPacket) packet;
            Object str = arpp.getSenderHardwareAddress();
            print.TextAreaAppend("GOT AN ARP REQUEST FROM: " + str.toString() + "\n\n");

            DatalinkPacket DLayer = arpp.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;

            String Protocol = "ARP";
            int PacketID = 0;
            print.TextAreaAppend("ARP Source IP Address: " + arpp.getSenderProtocolAddress().toString() + "\n");
            print.TextAreaAppend("Source MAC Address Address: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC Address: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n\n\n");

        } else if (Value.equals("2")) {
            String Protocol = "IGMP";

            DatalinkPacket DLayer = packet.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;
            print.TextAreaAppend("Frame Type: " + Ether.frametype + "\n");
            print.TextAreaAppend("Source MAC: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Protocol: " + Protocol + "\n\n\n");
        } else {

            DatalinkPacket DLayer = packet.datalink;
            EthernetPacket Ether = (EthernetPacket) DLayer;
            print.TextAreaAppend("Frame Type: " + Ether.frametype + "\n");
            print.TextAreaAppend("Source MAC: " + Ether.getSourceAddress() + "\n");
            print.TextAreaAppend("Destination MAC: " + Ether.getDestinationAddress() + "\n");
            print.TextAreaAppend("Protocol: Others\n\n\n");
        }
    }

    public int GetHopCount(int InitialTTL) {
        int HopCount = 0;
        if (InitialTTL <= 32) {
            HopCount = 32 - InitialTTL;
        } else if (InitialTTL <= 64) {
            HopCount = 64 - InitialTTL;
        } else if (InitialTTL <= 128) {
            HopCount = 128 - InitialTTL;
        } else if (InitialTTL <= 255) {
            HopCount = 255 - InitialTTL;
        }

        return HopCount;
    }

    public String GenerateChromosomes(InetAddress srcIp, EthernetPacket Ether, int TTL, int HopCount, String Protocol, int PacketID) {
        Hex2Decimal conv = new Hex2Decimal();
        Messages msg = new Messages();
        //Convert IP Address to Binary   
        String data_in = "";
        String data_out = "";
        byte[] bytes = srcIp.getAddress();
        data_out = new BigInteger(1, bytes).toString(2);
        //System.out.println("IP In Binary: " + data_out);

           //Convert Source MAC Address to Binary
        String[] SourceMAC = Ether.getSourceAddress().split(":");
        String[] DestinationMAC = Ether.getDestinationAddress().split(":");
        int[] IntSourceMAC = new int[SourceMAC.length];
        int[] IntDestinationMAC = new int[DestinationMAC.length];
        String Collector = "";
        String SourceMACAddressInBinary = "";
        String DestinationMACAddressInBinary = "";
        for (int i = 0; i < SourceMAC.length; i++) {
            IntSourceMAC[i] = conv.hex2decimal(SourceMAC[i]);
            Collector = Padder(conv.decimal2binary(IntSourceMAC[i])); //Would pad all outputs to 8 bits, as each of the 
            //6 octets of the MAC addressi is 8-bit long
            SourceMACAddressInBinary = SourceMACAddressInBinary + "" + Collector;
        }
        for (int i = 0; i < DestinationMAC.length; i++) {
            IntDestinationMAC[i] = conv.hex2decimal(DestinationMAC[i]);
            Collector = Padder(conv.decimal2binary(IntDestinationMAC[i]));
            DestinationMACAddressInBinary = DestinationMACAddressInBinary + "" + Collector;
        }

//Convert Initial TTL to Binary
        String TTLInBinary = Padder(conv.decimal2binary(TTL)); //Pad to 8 bits length, as maximum ttl value is 255

//Convert HopCount toBinary
        String HopCountInBinary = Padder(conv.decimal2binary(HopCount)); //Pad to 8 bits length, as maximum hop count value is 255

//Search DB for Packet ID values from previous instances
        String[] PacketIds;
        String PacketIDValidity = "0";

        try {
            synchronized (IDS.query) {

                PacketIds = IDS.query.RetreiveSpecificColumnofInformation1("TRAFFIC_TABLE", "PACKET_ID", "SOURCE_MAC", Ether.getSourceAddress(), "DESTINATION_MAC", Ether.getDestinationAddress(), "PROTOCOL", Protocol);

//The new PacketID should be greater at all instance
                if (PacketIds != null) {

                    for (int i = 0; i < PacketIds.length; i++) {
                        if (PacketID > Integer.parseInt(PacketIds[i])) {
                            PacketIDValidity = "1";

                        } else {
                            PacketIDValidity = "0";
                        }
                        if (i > 10) {
                            break;
                        }
                    }
                } else {
                    PacketIDValidity = "1";
                }
            }
        } catch (Exception ex) {
            IDS.msg.ErrorMessages("Chromosome Generation Error: " + ex);
        }

        //Concatenate Genes to make Chromosome
        String Chromosome = SourceMACAddressInBinary + "" + DestinationMACAddressInBinary + "" + TTLInBinary + "" + HopCountInBinary + "" + PacketIDValidity;

        byte[] ByteChromosome = Chromosome.getBytes();
        print.TextAreaAppend("Genetic Chromosome: " + Chromosome + "\n");
        return Chromosome;
    }

    public double GetInstantFitness(EthernetPacket Ether, InetAddress srcIp, String Chromosome, String Protocol) throws SQLException {
        int FitnessLevel = 0;
        double FitnessPercentage = 0;
        synchronized (IDS.query) {
            String[] Benchmarks = IDS.query.RetreiveSpecificColumnofInformation2("TRAFFIC_TABLE", "CHROMOSOME", "SOURCE_IP", srcIp.toString().replace("/", ""), "DESTINATION_MAC", Ether.getDestinationAddress(), "PROTOCOL", Protocol);
            if (Benchmarks == null) {
                FitnessPercentage = 100;
                print.TextAreaAppend("Chromosome Fitness: " + FitnessPercentage + "%\n");
            } else {
              // print.TextAreaAppend("Benchmark Length: "+Benchmarks[0].length()+" Chromosome Lenght: "+Chromosome.length());
                for (int i = 0; i < Benchmarks[0].length(); i++) { //Comparing against the chromosome with the highest strenth in the db
                    if (Benchmarks[0].substring(i, i + 1).equals(Chromosome.substring(i, i + 1))) {
                        FitnessLevel = FitnessLevel + 1;
                        //System.out.println("Fitness Level: "+FitnessLevel);
                    }
                }
                FitnessPercentage = (double) ((100 * FitnessLevel) / (Benchmarks[0].length()));
                print.TextAreaAppend("Chromosome Fitness: " + FitnessPercentage + "%\n");
            }
        }

        return FitnessPercentage;
    }

    public int DiscoverToAlert(double FitnessValue, String ProtocolType, String SourceIP, String SourceMAC) {

        int SpoofIndicator = 0;
        Packet Packet2Use;
        if (FitnessValue <= Integer.parseInt(IDS.ToSave[1])) {
            print.TextAreaAppend("SPOOFED IP DETECTED FROM " + SourceIP + " ->" + SourceMAC + " ON A " + ProtocolType + " PACKET\n");
            IDS.msg.InformationMessages("SPOOFED IP DETECTED FROM " + SourceIP + " ->" + SourceMAC + " ON A " + ProtocolType + " PACKET");
            SpoofIndicator = 1;
        }
        return SpoofIndicator;
    }

    public void AlertAdmin(String Message, String MACAddress) {

        ConfigUtility configUtil = new ConfigUtility();

        String toAddress = IDS.ToSave[0];
        String subject = "SPOOFED IP DETECTED BY " + MACAddress;
        String message = Message;

        File[] attachFiles = null;

        try {
            Properties smtpProperties = configUtil.loadProperties();
            EmailUtility.sendEmail(smtpProperties, toAddress, subject, message, attachFiles);

            print.TextAreaAppend("ADMIN HAS BEEN ALERTED\n");

        } catch (Exception ex) {
            print.TextAreaAppend("Error while trying to send email alert to admin: " + ex.getMessage());
        }

    }
    
    public String Padder(String ToPad) {
        if (ToPad.length()<8){
            int deficit = 8 - ToPad.length();
            for (int i=0; i<deficit; i++){
         ToPad = "0"+ToPad;   
        }
        }
      
        return ToPad;

    }
}

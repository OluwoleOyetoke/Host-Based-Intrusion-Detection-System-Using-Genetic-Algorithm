/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.io.IOException;
import jpcap.*;
import jpcap.packet.Packet;

/**
 *
 * @author olu
 */
public class InterfaceThread implements Runnable {
 ThreadLocal<Integer> MyInterfaceNumber = new ThreadLocal<>();
   int Global;
  

    InterfaceThread() {

    }

    InterfaceThread(int InterfaceNumber) {
  Global = InterfaceNumber;
    }

 
    
    @Override
    public void run() {
      
 MyInterfaceNumber.set(Global);
        Integer InterfaceToPrint = MyInterfaceNumber.get();
        MainFrame.ThreadFireSeparator=true;
      
        CapAndPrint print = new CapAndPrint();
  
        try {

//Open Selected Interface     
            JpcapCaptor captor = JpcapCaptor.openDevice(IDS.Interfaces[InterfaceToPrint], 65535, true, 5000);
       
            
              if(IDS.ActivateProtocolFilter==true){
            captor.setFilter(IDS.ProtoFilterString, true);
            System.out.println("Protocol Filtering ON: "+IDS.ProtoFilterString);
            }
            if(IDS.ActivatePortFilter==true){
                  captor.setFilter(IDS.PortFilterString, true);
                  System.out.println("Port Filtering ON: "+IDS.PortFilterString);
            }
            JpcapWriter writer=JpcapWriter.openDumpFile(captor,"DumpFile"+InterfaceToPrint+".txt");
            print.TextAreaAppend("Traffic Through "+IDS.Interfaces[InterfaceToPrint].description+" Interface Now Being Sniffed\n");
           
            Packet Pack;
           
            while (MainFrame.StopSniffing == false) {
             // captor.processPacket(-1, new CapAndPrint());
                Pack = captor.getPacket();
                if(Pack!=null){
                print.receivePacket(Pack);
                if(IDS.SaveOnCapture==true){
                writer.writePacket(Pack);
                }
                }
                }
             //captor.loopPacket(-1, new CapAndPrint());
       
           // writer.close();  //Closing the writer kept generating errors
          
            captor.close();
            
           
            

        } catch (IOException ex) {
            IDS.msg.ErrorMessages("Interface " + IDS.Interfaces[InterfaceToPrint].description + " Encountered Error: " + ex);
        }

    }

    public void Start(int InterfaceNumber) {

        InterfaceThread interfaceT = new InterfaceThread(InterfaceNumber);

        Thread thread = new Thread(interfaceT);

        thread.start();

    }

}

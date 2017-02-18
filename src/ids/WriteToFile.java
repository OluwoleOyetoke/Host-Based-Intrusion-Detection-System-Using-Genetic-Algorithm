/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author olu
 */
public class WriteToFile {

    Messages msg = new Messages();

    public void Write(String Value, String FileName) {
        try {
            
             
            BufferedWriter write = new BufferedWriter(new FileWriter("C:\\IDS\\FileBank\\"+FileName, true));
      //  File file = new File("C:\\IDS\\FileBank\\ports.txt");
       // PrintWriter printer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
       write.newLine();
       write.append(Value);
       write.close();
           // printer.append(Value);
            //printer.close();
        } catch (Exception ex) {
            msg.ErrorMessages("Unable To Write To File: " + ex);
        }

    }
    
    
    public void OverWrite(ArrayList Value, String FileName) {
        try {
            
             
          
        File file = new File("C:\\IDS\\FileBank\\"+FileName);
        PrintWriter printer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
      for (int i=0; i<Value.size(); i++){
         printer.println(Value.get(i).toString());
      }
         printer.close();
        } catch (Exception ex) {
            msg.ErrorMessages("Unable To Write To File: " + ex);
        }

    }
}

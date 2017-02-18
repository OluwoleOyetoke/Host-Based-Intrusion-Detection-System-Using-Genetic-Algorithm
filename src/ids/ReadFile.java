/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ids;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author olu
 */
public class ReadFile {
    
    public ArrayList Read(String filename){
    ArrayList<String> readvalues = new ArrayList<String>();
        try{
    FileReader file = new FileReader("C:\\IDS\\FileBank\\"+filename+"");
    BufferedReader buffer = new BufferedReader(file);
    String justread=null;
    while((justread=buffer.readLine())!=null){
        readvalues.add(justread);
    }
    buffer.close();
    }
    catch(Exception ex){
       IDS.msg.ErrorMessages(""+ex);
    }
    return readvalues;
    }
    
}

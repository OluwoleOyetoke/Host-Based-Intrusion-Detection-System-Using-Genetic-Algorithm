/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import java.awt.Image;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author olu
 */
public class Messages {

   
    public synchronized void ErrorMessages(String Message) {
       
        JOptionPane.showMessageDialog(null, Message, "ERROR", JOptionPane.ERROR_MESSAGE);

    }

    public synchronized void InformationMessages(String Message) {
      
        JOptionPane.showMessageDialog(null, Message, "INFORMATION", JOptionPane.INFORMATION_MESSAGE);

    }

  

 

}

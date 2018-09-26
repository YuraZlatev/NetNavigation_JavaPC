package client_navigation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server_Navigation 
{
    private final static String configurationPath = "Configuration.cfg";
    private static TreeMap<String, String> conf = new TreeMap<String, String>();
    
    public static String getServerIP(){return conf.get("server_ip");}
    public static String getServerPort(){return conf.get("server_port");}
    
    /**
     * прочитать из файла "NetAddress.txt"
     */
    private static InetAddress readSocket()
    {
        File confFile = new File(configurationPath);
        if(!confFile.exists())
        {
            try 
            {          
                confFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configurationPath), "UTF8"));
                bw.write("server_ip:127.0.0.1\r\n");
                bw.write("server_port:5555\r\n");
                bw.close();
            } 
            catch (IOException ex) { }
        }
        
        try 
        {
            String ip = "";
            String port = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configurationPath), "UTF8"));
            ip = br.readLine();
            port = br.readLine();
            br.close();
            
            int indexIP = ip.indexOf(":");
            conf.put(ip.substring(0, indexIP), ip.substring(indexIP + 1));
            int indexPort = port.indexOf(":");
            conf.put(port.substring(0, indexPort), port.substring(indexPort + 1));
            
            InetAddress net = InetAddress.getByName(conf.get("server_ip"));
            if(net == null)
                throw new Exception();
            
            return net;
        } 
        catch (Exception ex) 
        {      
            if(confFile.exists())
                confFile.delete();
            
            return null;
        }
    }
    
    public static ServerThread runServer() 
    {
        InetAddress curAddress = readSocket();

        if(curAddress == null)
        {
            System.out.println("Ошибка при чтении Файла конфигурации!");
            return null;
        }
        
        System.out.println("\tServer");
        System.out.println("IP: "+conf.get("server_ip"));
        System.out.println("Port: "+conf.get("server_port"));
        System.out.println("-----------------");
        
        try
        {
            int port = Integer.parseInt(conf.get("server_port"));
            client_navigation.ServerThread st = new client_navigation.ServerThread(curAddress, port);
            st.setDaemon(true);
            st.start();
            
            return st;
        }
        catch (Exception ex)
        {
            Logger.getLogger(Server_Navigation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }   
}

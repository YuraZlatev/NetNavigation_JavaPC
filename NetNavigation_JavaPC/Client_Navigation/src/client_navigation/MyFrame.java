package client_navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class MyFrame extends JFrame implements ActionListener
{
    private Socket server; //server - сервер компьютера к которому мы подключаемся
    private ServerThread localServer; //localServer - наш сервер
      
    protected JPanel mainPanel;
    protected JPopupMenu menu;
    protected JPopupMenu tableMenu;

    //-------------Local Computer-------
    protected JTable table;  
    protected NNTableModel tableModel;
    //---
    protected final String nameDir = "Local: ";
    protected JButton btParent;
    protected JTree tree;   
    protected File[] roots;
    protected TreePath curDir = null;
    protected JLabel curPath;
    protected String copyFromPath = "";
    protected String copyTo = "";
    //-------------Server---------------
    protected JTable stable;
    protected NNTableModel stableModel;
    //---
    protected final JLabel serverStatus = new JLabel("Нет подключенного сервера");
    protected JSplitPane serverSplit;
    protected JPanel serverPanel;
    //before connect (create)--
    protected JPanel pServer;
    protected JTextField ip;
    protected JTextField port;
    protected JButton connect;
    protected JButton disconnect;
    //after connect (create)---
    protected String snameDir = "";
    protected JButton sbtParent;
    protected JTree stree;
    protected TreePath scurDir = null;
    protected JLabel scurPath;
    protected String scopyFromPath = "";
    protected String scopyTo = "";
    
    public MyFrame()
    {  
        this.serverStatus.setHorizontalAlignment(JLabel.CENTER);
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.setContentPane(this.mainPanel);
        //-------------------------------
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(MyFrame.this.server != null)
                {
                    if(MyFrame.this.server.isConnected())
                    {
                        try { MyFrame.this.server.close(); }
                        catch (IOException ex) { }
                    }
                }
                
                if(MyFrame.this.localServer != null)
                    MyFrame.this.localServer.closeServer();
                
                System.exit(0);
            }
        });
        //-------------------------------
        this.setTitle("Проводник");
        this.setSize(new Dimension(800, 600));
        this.setMinimumSize(new Dimension(800, 600));     
        //----Button "Back"----------------------------
        this.btParent = new JButton("Назад");
        this.btParent.setName("back");
        this.btParent.addActionListener(this);
        //---Tree Local Computer-----------------------------
        this.roots = File.listRoots();//список дисков
        FillTree();    
        //---Create Trees & JTables
        UtilNetNavigation.createTreeAndTable(this);
        //-------Запуск себя как сервера------------
        this.localServer = Server_Navigation.runServer();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        if(this.localServer == null)
            System.out.println("Не удалось запустить приложение как сервер!");
        else
        {
            JLabel lb1 = new JLabel("Local Server_IP : "+Server_Navigation.getServerIP());
            JLabel lb2 = new JLabel("Local Server_Port : "+Server_Navigation.getServerPort());
            panel.add(lb1);
            panel.add(lb2);
        }
        //---------------------------------
        createTopPanel(panel);
        //----Create PopupMenu--------
        UtilNetNavigation.createPopupMenu(this);
        this.tree.setComponentPopupMenu(this.menu);
        //---
        UtilNetNavigation.createTablePopupMenu(this);
        this.table.setComponentPopupMenu(this.tableMenu);     
    }
    
    /**
     * заполнение панели для подключения
     */
    private void createTopPanel(JPanel infoServer)
    {
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
    
        this.ip = new JTextField();
        this.ip.setToolTipText("IP сервера");
        this.ip.setPreferredSize(new Dimension(100, 25));
        
        this.port = new JTextField();
        this.port.setToolTipText("Порт для подключения к серверу");
        this.port.setPreferredSize(new Dimension(60, 25));
        
        p1.add(this.ip);
        p1.add(this.port);
        //-----------
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        //---connect---------------
        this.connect = new JButton("Подключится");
        this.connect.setFont(new Font("Arial", Font.ITALIC, 12));
        this.connect.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                try
                {
                    String address = MyFrame.this.ip.getText().trim();
                    if(address.isEmpty())
                        throw new Exception("adr");
                        
                    int port = Integer.parseInt(MyFrame.this.port.getText().trim());

                    try 
                    {
                        MyFrame.this.server = new Socket(InetAddress.getByName(address), port);
                        UtilNetNavigation.Enabled(MyFrame.this, false);
                        ServerFillPanel();
                        MyFrame.this.snameDir = MyFrame.this.ip.getText()+": ";
                        MyFrame.this.stree.setComponentPopupMenu(MyFrame.this.menu);
                        MyFrame.this.stable.setComponentPopupMenu(MyFrame.this.tableMenu);
                        MyFrame.this.scurPath.setText(MyFrame.this.snameDir+"[root]");
                    } catch (IOException ex) 
                    {
                        JOptionPane.showMessageDialog(rootPane, "Не удается подключится к серверу! Возможно неверно введен IP или Port.", "Error", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
                catch(Exception ex)
                {
                    if(ex.getMessage().contentEquals("adr"))
                        JOptionPane.showMessageDialog(rootPane, "Неверный IP!", "Error", JOptionPane.ERROR_MESSAGE); 
                    else
                        JOptionPane.showMessageDialog(rootPane, "Неверно указан Port!", "Error", JOptionPane.ERROR_MESSAGE); 
                }
            }      
        });
        //---disconnect------------
        this.disconnect = new JButton("Отключится");
        this.disconnect.setFont(new Font("Arial", Font.ITALIC, 12));
        this.disconnect.setEnabled(false);
        this.disconnect.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                if(MyFrame.this.server == null)
                    return;
                
                if(MyFrame.this.server.isConnected())
                {
                    try { MyFrame.this.server.close(); } 
                    catch (IOException ex) {}
                    
                    MyFrame.this.server = null;
                    UtilNetNavigation.Enabled(MyFrame.this, true);
                    MyFrame.this.pServer.updateUI();
                    
                    if(MyFrame.this.curDir != null)
                        MyFrame.this.curPath.setText(MyFrame.this.nameDir + MyFrame.this.curDir.toString());
                    else
                        MyFrame.this.curPath.setText(MyFrame.this.nameDir + "[root]");                      
                }
            }        
        });
        
        p2.add(this.connect);
        p2.add(this.disconnect);
        //-----------------------------
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setMinimumSize(new Dimension(430, 0));
        panel.add(p1, BorderLayout.WEST);
        panel.add(p2, BorderLayout.EAST);
        
        JPanel jp = new JPanel();//ip, port, conn...
        jp.setLayout(new BorderLayout());
        if(infoServer.getComponentCount() > 0)
            jp.add(infoServer, BorderLayout.WEST);        
        jp.add(panel, BorderLayout.EAST);
        //------------------------------
        this.mainPanel.add(jp, BorderLayout.NORTH);
    }
    
    /**
     * отображение дисков сервера
     */
    private void ServerFillPanel()
    {
        try 
        {
            String command = "GET_ROOTS";
            //запрос на получение дисков-----            
            byte[] data = UtilServer.sizeFromString(command);
            MyFrame.this.server.getOutputStream().write(data, 0, data.length); 
            data = command.getBytes();
            MyFrame.this.server.getOutputStream().write(data, 0, data.length);           
            //---ожидание ответа от сервера----------
            while(MyFrame.this.server.getInputStream().available() == 0){}           
            data = UtilServer.getDataFromServer(this.server);
            if(data == null) throw new Exception();
            //команда и данные---
            String line = new String(data, 0, data.length);
            //---Правильная ли команда пришла--------
            command = line.substring(0, line.indexOf("|"));
            if(!command.contentEquals("GET_ROOTS"))
            {               
                try { MyFrame.this.server.close(); } catch(IOException ex) {}                 
                MyFrame.this.server = null;
                UtilNetNavigation.Enabled(MyFrame.this, true);
                JOptionPane.showMessageDialog(rootPane, "Ошибка при чтении данных.", command, JOptionPane.ERROR_MESSAGE);
                return;
            }
            //-----------------------------------------          
            String sroot = line.substring(line.indexOf("|")+1);
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            UtilNetNavigation.addNodes(root, sroot);
            
            this.stree = new JTree(root);
            this.stree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            this.stree.setName("serverTree");
            this.stree.setRootVisible(false);
            this.stree.addTreeExpansionListener(new TreeExpansionListener()
            {
                private void RequestServerNodes(TreePath tp)
                {
                    String path = pathByTreeNode(((DefaultMutableTreeNode)tp.getLastPathComponent()).getPath());                   
                    String command = "GET_NODES@"+path;
                    byte[] a = command.getBytes();
                    try 
                    {
                        byte[] data = UtilServer.sizeFromString(command);
                        MyFrame.this.server.getOutputStream().write(data, 0, data.length); 
                        data = command.getBytes();
                        MyFrame.this.server.getOutputStream().write(data, 0, data.length);  
                        //ожидание ответа от сервера-----------------------
                        while(MyFrame.this.server.getInputStream().available() == 0){}
                        //---получение nodes-------------------------------                
                        data = UtilServer.getDataFromServer(MyFrame.this.server);
                        if(data == null) throw new Exception();
                        //команда и данные---
                        String line = new String(data, 0, data.length);
                        command = line.substring(0, line.indexOf("|"));
                        if(!command.contentEquals("GET_NODES"))
                        {                  
                            try { MyFrame.this.server.close(); } catch(IOException ex) {}                 
                            MyFrame.this.server = null;
                            UtilNetNavigation.Enabled(MyFrame.this, true);
                            JOptionPane.showMessageDialog(rootPane, "Ошибка при чтении данных.", command, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        //-------------------------------------------------
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
                        node.removeAllChildren();
                        MyFrame.this.stree.clearSelection();
                        String answer = line.substring(line.indexOf("|")+1);
                        if(answer.contentEquals("empty"))
                        {                     
                            MyFrame.this.scurDir = tp.getParentPath();
                            JOptionPane.showMessageDialog(rootPane, "Каталог пуст или его уже не существует", "Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {                           
                            UtilNetNavigation.addNodes(node, answer);
                            MyFrame.this.scurDir = tp;
                            
                            //-----заполнение тиблицы----------------------
                            UtilNetNavigation.infoByPath(MyFrame.this, answer);
                        }
                        //-------------------------------------------------
                        MyFrame.this.scurPath.setText(MyFrame.this.snameDir + MyFrame.this.scurDir.toString());
                        ((DefaultTreeModel)MyFrame.this.stree.getModel()).reload(node);
                        MyFrame.this.stree.expandPath(MyFrame.this.scurDir);                       
                    } 
                    catch (Exception ex) 
                    {                 
                        try { MyFrame.this.server.close(); } catch(IOException e) {}                 
                        MyFrame.this.server = null;
                        UtilNetNavigation.Enabled(MyFrame.this, true);
                        JOptionPane.showMessageDialog(rootPane, "Сервер не доступен.", "Message", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
                
                @Override
                public void treeExpanded(TreeExpansionEvent event) 
                {
                    TreePath tp = event.getPath();
                    RequestServerNodes(tp);
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent event)
                {
                    MyFrame.this.scurDir = event.getPath().getParentPath();
                    MyFrame.this.scurPath.setText(MyFrame.this.snameDir + MyFrame.this.scurDir.toString());
                    if(MyFrame.this.scurDir == null || MyFrame.this.scurDir.toString().contentEquals("[root]"))
                    {
                        MyFrame.this.stableModel.clearRows();
                        MyFrame.this.stable.updateUI();
                    }
                    else
                        RequestServerNodes(MyFrame.this.scurDir);                   
                }
            });
            
            this.stree.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() == 3)
                    {
                        MyFrame.this.menu.setInvoker(MyFrame.this.stree);
                        UtilNetNavigation.checkMenuBeforeShow(MyFrame.this, MyFrame.this.menu);
                        MyFrame.this.menu.show(MyFrame.this.menu.getInvoker(), e.getX(), e.getY());
                    }
                }
            });
            
            this.pServer.removeAll();
            
            this.sbtParent = new JButton("Назад");
            this.sbtParent.setName("server_back");
            this.sbtParent.addActionListener(this);
            
            this.pServer.add(this.sbtParent, BorderLayout.SOUTH);
            JScrollPane scroll = new JScrollPane(this.stree);
            this.pServer.add(scroll);
            
            this.pServer.updateUI();
        } 
        catch (Exception ex) 
        { 
            try { MyFrame.this.server.close(); } catch(IOException e) {}                 
            MyFrame.this.server = null;
            UtilNetNavigation.Enabled(MyFrame.this, true);
            JOptionPane.showMessageDialog(rootPane, "main catch", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }
    
    public String pathByTreeNode(TreeNode[] nodes)
    {      
        String path = "";
        for(int i=1; i<nodes.length; i++)
        {
            if(path.isEmpty())
                path += nodes[i].toString();
            else
                path += "\\" + nodes[i].toString();
        }
        return path;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if(e.getSource() instanceof JMenuItem)
        {
            actionPerformedMenu((JMenuItem)e.getSource());
            return;
        }
        
        JButton bt = (JButton)e.getSource();
        switch(bt.getName())
        {
            case "back":
            {
                if(this.curDir == null || this.curDir.toString().contentEquals("[root]"))
                {
                    JOptionPane.showMessageDialog(rootPane, "Дальше некуда!", "Local computer", JOptionPane.ERROR_MESSAGE);
                    break;
                }   
                //----если папка после распахивания стала файлом---------------------
                boolean flag = false;
                if(this.tree.isCollapsed(this.curDir))
                {
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode();
                    ((DefaultMutableTreeNode)this.curDir.getLastPathComponent()).add(tn);
                    this.tree.updateUI();
                    flag = true;
                }
                
                this.tree.collapsePath(this.curDir);              
                this.curPath.setText(this.nameDir + this.curDir.toString());
                this.tree.clearSelection();     
                
                if(flag)
                {
                    this.curDir = this.curDir.getParentPath();
                    this.curPath.setText(this.nameDir + this.curDir.toString());
                }
                this.table.clearSelection();
                break;
            }
            case "server_back":
            {
                if(this.scurDir == null || this.scurDir.toString().contentEquals("[root]"))
                {
                    JOptionPane.showMessageDialog(rootPane, "Дальше некуда!", this.ip.getText(), JOptionPane.ERROR_MESSAGE);
                    break;
                }   
                //----если папка после распахивания стала файлом---------------------           
                boolean flag = false;
                if(this.stree.isCollapsed(this.scurDir))
                {
                    DefaultMutableTreeNode tn = new DefaultMutableTreeNode();
                    ((DefaultMutableTreeNode)this.scurDir.getLastPathComponent()).add(tn);
                    this.stree.updateUI();
                    flag = true;
                }

                this.stree.collapsePath(this.scurDir);  
                if(!UtilNetNavigation.isClosed)
                {
                    this.scurPath.setText(this.snameDir + this.scurDir.toString());
                    this.stree.clearSelection();

                    if(flag)
                    {
                        this.scurDir = this.scurDir.getParentPath();
                        this.scurPath.setText(this.snameDir + this.scurDir.toString());
                    }
                    this.stable.clearSelection();
                }
                break;
            }
        }
    }
    
    /**
     * обработчик кликов для JMenuItem (JTree, JTable)
     */
    public void actionPerformedMenu(JMenuItem m)
    {
        JPopupMenu mn = (JPopupMenu)m.getParent();    
        String parent = mn.getInvoker().getName();
        
        switch(m.getName())
        {
            //---------TREE----------------------------------
            case "m1": 
            { 
                if(parent.contentEquals("localTree"))
                    NNFileHelper.createDirTree(this, m.getText());
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.createDirTree(this.server, this, m.getText());
                break; 
            }
            case "m2": 
            {
                if(parent.contentEquals("localTree"))
                    NNFileHelper.createFileTree(this, m.getText()); 
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.createFileTree(this.server, this, m.getText());
                break; 
            }
            case "m3": 
            {
                if(parent.contentEquals("localTree"))
                    NNFileHelper.renameDirTree(this, m.getText()); 
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.renameDirTree(this.server, this, m.getText());
                break; 
            }
            case "m4": 
            { 
                if(parent.contentEquals("localTree"))
                    NNFileHelper.deleteDirTree(this, m.getText());
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.deleteDirTree(this.server, this, m.getText());
                break; 
            }
            case "m5": 
            {
                if(parent.contentEquals("localTree"))
                    NNFileHelper.copyDirTree(this, m.getText());
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.copyDirTree(this, m.getText());
                break;
            } 
            case "m6": 
            {
                if(parent.contentEquals("localTree"))
                    NNFileHelper.pasteDirTree(this.server, this, m.getText());
                else if(parent.contentEquals("serverTree"))
                    NNServerFileHelper.pasteDirTree(this.server, this, m.getText());
                break;
            } 
            //-----------TABLE---------------------------------
            case "mt1"://сортировка по имени
            {                          
                if(parent.contentEquals("table"))
                    UtilNetNavigation.tableComparator(this.table, this.tableModel, NNTableModel.NNSort.Name, true);
                else if(parent.contentEquals("stable"))
                    UtilNetNavigation.tableComparator(this.stable, this.stableModel, NNTableModel.NNSort.Name, true);               
                break;
            }
            case "mt2"://сортировка по размеру
            {               
                if(parent.contentEquals("table"))
                    UtilNetNavigation.tableComparator(this.table, this.tableModel, NNTableModel.NNSort.Size, true);
                else if(parent.contentEquals("stable"))
                    UtilNetNavigation.tableComparator(this.stable, this.stableModel, NNTableModel.NNSort.Size, true);             
                break;
            }
            case "mt3"://сортировка по дате
            {              
                if(parent.contentEquals("table"))
                    UtilNetNavigation.tableComparator(this.table, this.tableModel, NNTableModel.NNSort.Date, true);
                else if(parent.contentEquals("stable"))
                    UtilNetNavigation.tableComparator(this.stable, this.stableModel, NNTableModel.NNSort.Date, true);               
                break;
            }
            case "mt4": //create Dir (Table)
            {                       
                if(parent.contentEquals("table"))
                {
                    selectPathFromTable(this.table);
                    NNFileHelper.createDirTree(this, m.getText());
                }
                else if(parent.contentEquals("stable"))
                {
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.createDirTree(this.server, this, m.getText());
                }
                break; 
            }
            case "mt5": //create File (Table)
            {
                if(parent.contentEquals("table"))
                {
                    selectPathFromTable(this.table);
                    NNFileHelper.createFileTree(this, m.getText()); 
                }
                else if(parent.contentEquals("stable"))
                {
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.createFileTree(this.server, this, m.getText());
                }
                break; 
            }
            case "mt6": 
            {
                if(parent.contentEquals("table"))
                {
                    selectPathFromTable(this.table);
                    NNFileHelper.renameDirTree(this, m.getText()); 
                }
                else if(parent.contentEquals("stable"))
                {
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.renameDirTree(this.server, this, m.getText());
                }
                break; 
            }
            case "mt7": 
            {
                if(parent.contentEquals("table"))
                {
                    selectPathFromTable(this.table);
                    NNFileHelper.deleteDirTree(this, m.getText()); 
                }
                else if(parent.contentEquals("stable"))
                {
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.deleteDirTree(this.server, this, m.getText());
                }
                break; 
            }
            case "mt8": 
            {      
                if(parent.contentEquals("table"))
                {
                    selectPathFromTable(this.table);
                    NNFileHelper.copyDirTree(this, m.getText());
                }
                else if(parent.contentEquals("stable"))
                {
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.copyDirTree(this, m.getText());
                }
                break; 
            }
            case "mt9": 
            {
                if(parent.contentEquals("table"))
                {
                    if(this.table.getSelectedRow() != -1)
                    {
                        String dir = this.tableModel.getValueAt(this.table.getSelectedRow(), 0).toString();
                        if(dir.indexOf("[f] ") != -1)
                        {
                            JOptionPane.showMessageDialog(this, "Нельзя копировать в файл!", "Message", JOptionPane.ERROR_MESSAGE);
                            this.table.clearSelection();
                            return;
                        }
                    }
                    selectPathFromTable(this.table);
                    NNFileHelper.pasteDirTree(this.server, this, m.getText());
                }
                else if(parent.contentEquals("stable"))
                {
                    if(this.stable.getSelectedRow() != -1)
                    {
                        String dir = this.stableModel.getValueAt(this.stable.getSelectedRow(), 0).toString();
                        if(dir.indexOf("[f] ") != -1)
                        {
                            JOptionPane.showMessageDialog(this, "Нельзя копировать в файл!", "Message", JOptionPane.ERROR_MESSAGE);
                            this.stable.clearSelection();
                            return;
                        }
                    }
                    selectPathFromTable(this.stable);
                    NNServerFileHelper.pasteDirTree(this.server, this, m.getText());
                }
                break; 
            }
        }
    }
    
    private void selectPathFromTable(JTable table)
    {          
        if(table.getName().contentEquals("table"))
        {
            if(this.table.getSelectedRow() != -1)
            {       
                String dir = this.tableModel.getValueAt(this.table.getSelectedRow(), 0).toString();
                dir = dir.substring(4);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir);
                TreePath p = this.curDir;
                TreePath t = p.pathByAddingChild(node);
                this.tree.setSelectionPath(t);
            }
            else
                this.tree.setSelectionPath(this.curDir);
            this.table.clearSelection();
        }
        else if(table.getName().contentEquals("stable"))
        {
            if(this.stable.getSelectedRow() != -1)
            {          
                String dir = this.stableModel.getValueAt(this.stable.getSelectedRow(), 0).toString();
                dir = dir.substring(4);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir);
                TreePath p = this.scurDir;
                TreePath t = p.pathByAddingChild(node);
                this.stree.setSelectionPath(t);
            }
            else
                this.stree.setSelectionPath(this.scurDir);
            this.stable.clearSelection();
        }
    }
    
    //------Local Tree----------------------
    private void FillTree()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (File root1 : this.roots) 
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(root1.getAbsolutePath());
            root.add(node);
            CheckNextNode(node, root1.getAbsoluteFile());
        }
        this.tree = new JTree(root);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.setName("localTree");
        this.tree.setRootVisible(false);
        this.tree.addTreeExpansionListener(new TreeExpansionListener(){
            @Override
            public void treeExpanded(TreeExpansionEvent event) 
            {
                MyFrame.this.curDir = event.getPath();
                MyFrame.this.AddNodes(MyFrame.this.curDir);
                MyFrame.this.curPath.setText(MyFrame.this.nameDir + MyFrame.this.curDir.toString());
                ((DefaultTreeModel)MyFrame.this.tree.getModel()).reload((DefaultMutableTreeNode)event.getPath().getLastPathComponent()); 
                MyFrame.this.tree.expandPath(MyFrame.this.curDir);
                MyFrame.this.tree.clearSelection();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event)
            {
                MyFrame.this.curDir = event.getPath().getParentPath();
                MyFrame.this.curPath.setText(MyFrame.this.nameDir + MyFrame.this.curDir.toString()); 
                if(MyFrame.this.curDir == null || MyFrame.this.curDir.toString().contentEquals("[root]"))
                {
                    MyFrame.this.tableModel.clearRows();
                    MyFrame.this.table.updateUI();
                }
                else
                    MyFrame.this.AddNodes(MyFrame.this.curDir);
                ((DefaultTreeModel)MyFrame.this.tree.getModel()).reload((DefaultMutableTreeNode)event.getPath().getParentPath().getLastPathComponent()); 
            }
        });
        
        this.tree.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == 3)
                {
                    MyFrame.this.menu.setInvoker(MyFrame.this.tree);
                    UtilNetNavigation.checkMenuBeforeShow(MyFrame.this, MyFrame.this.menu);
                    MyFrame.this.menu.show(MyFrame.this.menu.getInvoker(), e.getX(), e.getY());
                }
            }          
        });
    }  
    protected void AddNodes(TreePath tp)
    {      
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
        node.removeAllChildren();
        TreeNode[] tps = node.getPath();
        String str = "";
        for(int i=1; i<tps.length; i++)
            str += tps[i].toString() + "\\";
        
        File f = new File(str);
        File[] files = f.listFiles();
        if(files == null)
            return;
        
        for (File file : files) 
        {
            if(file.isDirectory())
            {
                DefaultMutableTreeNode nd = new DefaultMutableTreeNode(file.getName());
                node.add(nd);
                CheckNextNode(nd, file.getAbsoluteFile()); 
            }
        }
        
        this.tableModel.clearRows();
        for(int i=0; i<files.length; i++)
        {
            File file = files[i].getAbsoluteFile();          
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            if(files[i].isDirectory())
                this.tableModel.addRow(new String[]{"[D] "+file.getName(), String.valueOf(file.length()), sdf.format(file.lastModified())});      
            else
                this.tableModel.addRow(new String[]{"[f] "+file.getName(), String.valueOf(file.length()), sdf.format(file.lastModified())});   
        }
        UtilNetNavigation.tableComparator(this.table, this.tableModel, this.tableModel.getCurSort(), false);
        this.table.updateUI();
    }
    private void CheckNextNode(DefaultMutableTreeNode node, File file)
    {            
        File[] files = file.listFiles();
        if(files == null)
            return;
        
        for(int i=0; i<files.length; i++)
        {
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode(files[i].getName());
            node.add(nd); 
        }
    }
    
}

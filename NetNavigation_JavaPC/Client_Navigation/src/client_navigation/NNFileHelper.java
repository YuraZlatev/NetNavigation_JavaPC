package client_navigation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class NNFileHelper 
{
    private static String dir = "Путь: ";
    
    /**
     * возвращается строка для операций с файлами если выделана папка в дереве, если нет возвращается путь curDir
     */
    private static String getCurDir(JTree tree)
    {
        String normal = "";
        Object[] ob;
        
        ob = tree.getSelectionPath().getPath();
        
        for(int i = 1; i < ob.length; i++)
            normal += ob[i] + "\\";
        
        return normal;
    }
    
    //---запрещено удалять накопители---
    private static String forRoot(JTree tree)
    {
        String normal = "";
        Object[] ob;
        
        ob = tree.getSelectionPath().getPath();
        
        for(int i = 1; i < ob.length; i++)
            normal += ob[i];
        
        return normal;
    }   
    private static String checkRoot(File[] roots, JTree tree)
    {
        String abs = forRoot(tree);
        for(File f : roots)
            if(f.getAbsolutePath().contentEquals(abs))
                return "";
        
        return abs;
    }
    
    //---Local computer--------------------------------------
    public static void createDirTree(MyFrame mf, String title)
    {        
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете каталог !", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        
        String toPath = getCurDir(mf.tree); 
                
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel lb1 = new JLabel(dir);
        lb1.setPreferredSize(new Dimension(100, 25));
        JLabel lb2 = new JLabel(toPath);
        lb2.setPreferredSize(new Dimension(200, 25));
        lb2.setToolTipText(toPath);
        
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new FlowLayout());
        fPanel.add(lb1);
        fPanel.add(lb2);
        
        JLabel lb3 = new JLabel("Имя папки: ");
        lb3.setPreferredSize(new Dimension(100, 25));
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(200, 25));
        
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new FlowLayout());
        sPanel.add(lb3);
        sPanel.add(t);
        
        panel.add(fPanel);
        panel.add(sPanel);
        //-----------------
        do
        {
            int ans = JOptionPane.showConfirmDialog(mf, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch(ans)
            {
                case JOptionPane.OK_OPTION:
                {
                    String nameDir = t.getText().trim();
                    if(nameDir.isEmpty())
                    {
                        JOptionPane.showMessageDialog(mf, "Имя каталога не может быть пустым !", title, JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    
                    File newDir = new File(toPath + nameDir);
                    synchronized(newDir)
                    {
                        if(!newDir.exists())
                        {
                            if(!newDir.mkdir()) 
                                JOptionPane.showMessageDialog(mf, "Не удалось создать папку!", title, JOptionPane.ERROR_MESSAGE);
                            else
                            {              
                                JOptionPane.showMessageDialog(mf, "Готово", title, JOptionPane.INFORMATION_MESSAGE);                      
                            /*    TreePath tp = mf.curDir;
                                mf.tree.collapsePath(tp);
                                mf.tree.expandPath(mf.curDir);*/
                            }
                        }
                        else
                            JOptionPane.showMessageDialog(mf, "Каталог с таким именем уже существует !", title, JOptionPane.INFORMATION_MESSAGE);
                    }
                    mf.tree.clearSelection();
                    return;
                }
                default: mf.tree.clearSelection(); return;
            }
        }while(true);
    }
    
    public static void createFileTree(MyFrame mf, String title)
    {
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете каталог !", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        
        String toPath = getCurDir(mf.tree); 
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel lb1 = new JLabel(dir);
        lb1.setPreferredSize(new Dimension(100, 25));
        JLabel lb2 = new JLabel(toPath);
        lb2.setPreferredSize(new Dimension(200, 25));
        lb2.setToolTipText(toPath);
        
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new FlowLayout());
        fPanel.add(lb1);
        fPanel.add(lb2);
        
        JLabel lb3 = new JLabel("Имя файла: ");
        lb3.setPreferredSize(new Dimension(100, 25));
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(200, 25));
        
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new FlowLayout());
        sPanel.add(lb3);
        sPanel.add(t);
        
        panel.add(fPanel);
        panel.add(sPanel);
        //-----------------
        do
        {
            int ans = JOptionPane.showConfirmDialog(mf, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch(ans)
            {
                case JOptionPane.OK_OPTION:
                {
                    String nameDir = t.getText().trim();
                    if(nameDir.isEmpty())
                    {
                        JOptionPane.showMessageDialog(mf, "Имя файла не может быть пустым !", title, JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    
                    File newFile = new File(toPath + nameDir);
                    synchronized(newFile)
                    {
                        if(!newFile.exists())
                        {
                            try
                            { 
                                if(!newFile.createNewFile())
                                   JOptionPane.showMessageDialog(mf, "Не удалось создать файл !", title, JOptionPane.ERROR_MESSAGE); 
                                else
                                {
                                    JOptionPane.showMessageDialog(mf, "Готово", title, JOptionPane.INFORMATION_MESSAGE);
                                /*    TreePath tp = mf.curDir;
                                    mf.tree.collapsePath(tp);
                                    mf.tree.expandPath(tp);*/
                                }
                            }
                            catch(Exception ex){}
                        }    
                        else
                            JOptionPane.showMessageDialog(mf, "Файл с таким именем уже существует !", title, JOptionPane.INFORMATION_MESSAGE);
                    }
                    mf.tree.clearSelection();
                    return;
                }
                default: mf.tree.clearSelection(); return;
            }
        }while(true);
    }
    
    public static void renameDirTree(MyFrame mf, String title)
    {
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете каталог или файл!", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        
        String fromPath = checkRoot(mf.roots, mf.tree);
        if(fromPath.isEmpty())
        {
            JOptionPane.showMessageDialog(mf, "С данным каталогом / файлом операция невозможна !", title, JOptionPane.ERROR_MESSAGE);
            mf.tree.clearSelection();
            return;
        }   
        fromPath = getCurDir(mf.tree);
        File file = new File(fromPath);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel lb1 = new JLabel("Текущее имя: ");
        lb1.setPreferredSize(new Dimension(100, 25));
        JLabel lb2 = new JLabel(file.getName());
        lb2.setPreferredSize(new Dimension(200, 25));
        lb2.setToolTipText(fromPath);
        
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new FlowLayout());
        fPanel.add(lb1);
        fPanel.add(lb2);
        
        JLabel lb3 = new JLabel("Новое имя: ");
        lb3.setPreferredSize(new Dimension(100, 25));
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(200, 25));
        
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new FlowLayout());
        sPanel.add(lb3);
        sPanel.add(t);
        
        panel.add(fPanel);
        panel.add(sPanel);
        //-----------------
        do
        {
            int ans = JOptionPane.showConfirmDialog(mf, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch(ans)
            {
                case JOptionPane.OK_OPTION:
                {
                    String nameDir = t.getText().trim();
                    if(nameDir.isEmpty())
                    {
                        JOptionPane.showMessageDialog(mf, "Имя каталога / файла не может быть пустым !", title, JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    file = file.getAbsoluteFile();                             
                    String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("\\") + 1);
                    File newFile = new File(path+nameDir);
                    synchronized(file)
                    {
                        if(file.exists())
                        {                       
                            if(file.renameTo(newFile))
                            {
                                JOptionPane.showMessageDialog(mf, "Готово", title, JOptionPane.INFORMATION_MESSAGE);
                            /*    TreePath tp = mf.curDir;
                                mf.tree.collapsePath(tp);
                                mf.tree.expandPath(tp);*/
                            }
                            else
                                JOptionPane.showMessageDialog(mf, "Не удалось переименовать!", title, JOptionPane.ERROR_MESSAGE);
                        }
                        else
                            JOptionPane.showMessageDialog(mf, "Каталог / файл уже не существует !", title, JOptionPane.INFORMATION_MESSAGE);
                    }
                    mf.tree.clearSelection();
                    return;
                }
                default: mf.tree.clearSelection(); return;
            }
        }while(true);
    }
    
    public static void deleteDirTree(MyFrame mf, String title)
    {
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете каталог или файл!", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        
        String fromPath = checkRoot(mf.roots, mf.tree); 
        if(fromPath.isEmpty())
        {
            JOptionPane.showMessageDialog(mf, "С данным каталогом / файлом операция невозможна !", title, JOptionPane.ERROR_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        fromPath = getCurDir(mf.tree);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel lb1 = new JLabel(dir);
        lb1.setPreferredSize(new Dimension(100, 25));
        JLabel lb2 = new JLabel(fromPath);
        lb2.setPreferredSize(new Dimension(200, 25));
        lb2.setToolTipText(fromPath);
        
        JPanel fPanel = new JPanel();
        fPanel.setLayout(new FlowLayout());
        fPanel.add(lb1);
        fPanel.add(lb2);
        
        panel.add(fPanel);
        //-----------------
        do
        {
            int ans = JOptionPane.showConfirmDialog(mf, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch(ans)
            {
                case JOptionPane.OK_OPTION:
                {
                    File oldFile = new File(fromPath);
                    synchronized(oldFile)
                    {
                        if(oldFile.exists())
                        {                      
                            DeleteDirBodys(oldFile);
                            if(!oldFile.delete())
                                JOptionPane.showMessageDialog(mf, "Не удалось удалить! Возможно его уже не существует.", title, JOptionPane.ERROR_MESSAGE);
                            else
                            {
                                JOptionPane.showMessageDialog(mf, "Готово", title, JOptionPane.INFORMATION_MESSAGE);  

                            /*    TreePath tp = mf.curDir;
                                mf.tree.collapsePath(tp);
                                mf.tree.expandPath(tp);*/

                            }
                        }                  
                        else
                            JOptionPane.showMessageDialog(mf, "Каталог / файл уже не существует !", title, JOptionPane.INFORMATION_MESSAGE);
                    }
                    mf.tree.clearSelection();
                    return;
                }
                default: mf.tree.clearSelection(); return;
            }
        }while(true);
    }
    
    /**
     * рекурсивно удаляет каталог (если исходный файл - каталог)
     * @param f - обьект File
     */
    private static void DeleteDirBodys(File f)
    {
        File[] files = f.listFiles();
        if(files == null)
            return;
        
        for(File file : files)
        {
            try
            { 
                if(file.isFile())
                    file.delete();
                else
                {
                    DeleteDirBodys(file);
                    file.delete();
                }
            }catch(Exception ex){ }
        }
    }
    
    public static void copyDirTree(MyFrame mf, String title)
    {
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете каталог или файл!", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        
        String fromPath = checkRoot(mf.roots, mf.tree); 
        if(fromPath.isEmpty())
        {
            JOptionPane.showMessageDialog(mf, "С данным каталогом / файлом операция невозможна !", title, JOptionPane.ERROR_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        fromPath = getCurDir(mf.tree);
        mf.copyFromPath = fromPath;  
        mf.scopyFromPath = "";
        mf.tree.clearSelection();
    }
    
    public static void pasteDirTree(Socket server, MyFrame mf, String title)
    {
        if(mf.tree.getSelectionPath() == null)
        {
            JOptionPane.showMessageDialog(mf, "Выбирете путь!", title, JOptionPane.INFORMATION_MESSAGE);
            mf.tree.clearSelection();
            return;
        }
        String toPath = getCurDir(mf.tree);
        mf.copyTo = toPath;
        String cmd = UtilServer.serverCommand(mf);
        
        if(cmd == null)
        {
            File fileFrom = new File(mf.copyFromPath);
            File fileTo = new File(toPath);
            Path newPath = Paths.get(fileTo.toString());
            Thread t = new Thread()
            {
                MyFrame m = mf;
                String from = fileFrom.getName();
                @Override
                public void run()
                {
                    synchronized(fileTo)
                    {
                        if(fileFrom.isDirectory())
                        {
                            File dir = new File(fileTo.getAbsolutePath()+"\\"+fileFrom.getName());
                            dir.mkdir();
                            Path newPath = Paths.get(dir.toString());
                            copyDirAndFiles(fileFrom, dir, newPath);
                            JOptionPane.showMessageDialog(m, "Готово", "Копирование: [ "+from+" ]", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {
                            try
                            {
                                Files.copy(fileFrom.toPath(), newPath.resolve(fileFrom.getName()), StandardCopyOption.REPLACE_EXISTING);
                                JOptionPane.showMessageDialog(m, "Готово", "Копирование: [ "+from+" ]", JOptionPane.INFORMATION_MESSAGE);
                            }catch(Exception ex){}                 
                        }   
                    }
                }
                private void copyDirAndFiles(File from, File fileTo, Path to)
                {
                    File[] files = from.listFiles();
                    if(files == null)
                        return;

                    try 
                    {
                        for(File f : files)
                        {
                            if(f.isFile())
                                Files.copy(f.toPath(), to.resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
                            else
                            {
                                File dir = new File(fileTo.getAbsolutePath()+"\\"+f.getName());
                                dir.mkdir();
                                Path newPath = Paths.get(dir.toString());
                                copyDirAndFiles(f, dir, newPath);
                            }
                        }
                    } 
                    catch (IOException ex) { }
                }
            };
            t.setDaemon(true);
            t.start();  
        }
        else
        {
            Thread t = null;
            if(cmd.substring(0, cmd.indexOf("|")).contentEquals("COPY_SERVER_TO_LOCAL"))
                t = new CopyServerToLocalThread(server, mf, cmd, toPath);

            if(t != null)
            {
                t.setDaemon(true);      
                t.start();
            }
        }
        
        mf.copyFromPath = "";
        mf.copyTo = "";
        mf.scopyFromPath = "";
        mf.scopyTo = "";
        mf.tree.clearSelection(); 
    }
    
}
package client_navigation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class NNCellRenderer implements TableCellRenderer
{ 
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        JLabel lb = new JLabel(value.toString());
        
        if(value.toString().indexOf("[D]") != -1)    
            lb.setToolTipText(value.toString().substring(value.toString().indexOf("[D]") + 4));  
        else if(value.toString().indexOf("[f]") != -1)
            lb.setToolTipText(value.toString().substring(value.toString().indexOf("[f]") + 4)); 
        else
            lb.setToolTipText(value.toString()); 

        if(!isSelected)
        {
            if(table.getName().contentEquals("stable"))
            {
                lb.setBackground(Color.black);
                lb.setForeground(Color.cyan);
            }
            else
            {
                lb.setBackground(Color.black);
                lb.setForeground(Color.green);
            }
        }
        else
        {
            if(table.getName().contentEquals("stable"))
            {
                lb.setBackground(Color.cyan);
                lb.setForeground(Color.black);
            }
            else
            {
                lb.setBackground(Color.green);
                lb.setForeground(Color.black);
            }
        }
        
        switch(column)
        {
            case 1:
            {
                lb.setFont(new Font("Verdana", Font.PLAIN, 12));
                lb.setHorizontalAlignment(JLabel.CENTER);
                break;
            }
            case 2:
            {
                lb.setFont(new Font("Verdana", Font.PLAIN, 12));
                lb.setHorizontalAlignment(JLabel.CENTER);
                break;
            }
            default:
            {        
                lb.setFont(new Font("Verdana", Font.BOLD, 14));
                break;
            }
        }
        lb.setOpaque(true);
        return lb;
    }
}

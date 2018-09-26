package client_navigation;

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * NetNavigationTableModel
 */
public class NNTableModel implements TableModel
{
    public static enum NNSort{Name, Size, Date};
    
    private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private ArrayList<String[]> rows = new ArrayList<String[]>();
    
    private boolean sortName = false;
    private boolean sortSize = false;
    private boolean sortDate = false;
    
    private NNSort curSort = NNSort.Name;
    
    public NNSort getCurSort(){return this.curSort;}
    
    public void chengeSort(NNSort s)
    {
        switch(s)
        {
            case Name: this.sortName = !this.sortName; this.curSort = NNSort.Name; break;
            case Size: this.sortSize = !this.sortSize; this.curSort = NNSort.Size; break;
            case Date: this.sortDate = !this.sortDate; this.curSort = NNSort.Date; break;
        }
    }
    
    public boolean getSort(NNSort sort)
    {
        switch(sort)
        {
            case Size: return this.sortSize;
            case Date: return this.sortDate;
            default: return this.sortName;
        }
    }

    public ArrayList<String[]> getRows()
    {
        return rows;
    }
    
    public void addRow(String[] row)
    {
        this.rows.add(row);
        
        TableModelEvent ev = new TableModelEvent
        (
            this,
            this.rows.size()-1,
            this.rows.size()-1,
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.INSERT
        );
        for(TableModelListener l : this.listeners)
            l.tableChanged(ev);
    }
    
    public void clearRows()
    {
        this.rows.clear();
        
        TableModelEvent ev = new TableModelEvent
        (
            this,
            this.rows.size(),
            this.rows.size(),
            TableModelEvent.ALL_COLUMNS,
            TableModelEvent.DELETE
        );
        for(TableModelListener l : this.listeners)
            l.tableChanged(ev);
    }
    
    @Override
    public int getRowCount() {
        return this.rows.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex)
        {
            case 1: return "Размер (byte)";
            case 2: return "Дата изминения"; 
            default: return "Имя";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return TableColumn.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.rows.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        this.listeners.add(l);    
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.listeners.remove(l);    
    }
    
}

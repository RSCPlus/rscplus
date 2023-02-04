package Chat;

import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class ChatWindowChatTable extends JTable {
  private Color backgroundColor = Color.decode("#282828");
  //    private ChatWindowChatTableCellRenderer cellRenderer;
  private ChatWindowChatTableCellRenderer cellRenderer;

  public ChatWindowChatTable() {
    super();

    // Create cell renderer
    //        cellRenderer = new ChatWindowChatTableCellRenderer();
    cellRenderer = new ChatWindowChatTableCellRenderer();

    // Setup table look/behavior
    setCellSelectionEnabled(false);
    setRowSelectionAllowed(false);
    setShowHorizontalLines(false);
    setTableHeader(null);
    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    addHierarchyBoundsListener(
        new HierarchyBoundsAdapter() {
          @Override
          public void ancestorResized(HierarchyEvent e) {
            super.ancestorResized(e);

            //                resizeColumns();
          }
        });

    SwingUtilities.invokeLater(this::resizeColumns);

    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    return cellRenderer;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  public void resizeColumns() {
    autoFitColumnToContent(0, 30);
    autoSizeColumn(1);
  }

  private void autoSizeColumn(int columnIndex) {
    int cumulativeWidth = 0;
    int parentWidth = (int) getParent().getSize().getWidth();

    TableColumn targetColumn = columnModel.getColumn(columnIndex);
    for (int i = 0; i < getColumnCount(); i++) {
      if (i == columnIndex) continue;
      TableColumn tempColumn = columnModel.getColumn(i);
      int tempColumnWidth = tempColumn.getPreferredWidth() + getIntercellSpacing().width;
      cumulativeWidth += tempColumnWidth;
    }

    int newWidth = parentWidth - cumulativeWidth;
    if (targetColumn.getPreferredWidth() != newWidth) {
      targetColumn.setPreferredWidth(newWidth);
      SwingUtilities.invokeLater(this::updateRowHeights);
    }
  }

  private void autoFitColumnToContent(int columnIndex, int padding) {
    TableColumn tableColumn = getColumnModel().getColumn(columnIndex);
    int preferredWidth = tableColumn.getMinWidth();
    int maxWidth = tableColumn.getMaxWidth();

    for (int row = 0; row < getRowCount(); row++) {
      TableCellRenderer cellRenderer = getCellRenderer(row, columnIndex);
      Component c = prepareRenderer(cellRenderer, row, columnIndex);
      int width = c.getPreferredSize().width + getIntercellSpacing().width;
      preferredWidth = Math.max(preferredWidth, width);

      //  We've exceeded the maximum width, no need to check other rows
      if (preferredWidth >= maxWidth) {
        preferredWidth = maxWidth;
        break;
      }
    }

    int newWidth = preferredWidth + padding;
    if (tableColumn.getPreferredWidth() != newWidth) {
      tableColumn.setPreferredWidth(newWidth);
    }
  }

  private void updateRowHeights() {
    for (int row = 0; row < getRowCount(); row++) {
      cellRenderer.adjustRowHeight(this, row, 1);
    }
  }
}

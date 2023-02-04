package Chat;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class ChatWindowChatTable extends JTable {

  private Color backgroundColor = Color.decode("#282828");
  private ChatWindowChatTableCellRenderer cellRenderer;

  public ChatWindowChatTable() {
    super();

    // Create cell renderer
    cellRenderer = new ChatWindowChatTableCellRenderer();

    // Setup table look/behavior
    setShowHorizontalLines(false);
    setTableHeader(null);

    addMouseMotionListener(
        new MouseAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);

            Point point = e.getPoint();
            int columnIndex = columnAtPoint(point);
            int rowIndex = rowAtPoint(point);
            ChatWindowChatTableCellRenderer renderer =
                getPreparedCellRenderer(rowIndex, columnIndex);

            int cursorType = Cursor.DEFAULT_CURSOR;
            if (columnIndex == 1 && !renderer.getText().equals("")) {
              cursorType = Cursor.TEXT_CURSOR;
            }

            setCursor(Cursor.getPredefinedCursor(cursorType));
            //                System.out.format("[row: %d, column: %d] \n", rowIndex, columnIndex);
          }
        });

    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    SwingUtilities.invokeLater(this::resizeColumns);
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    return cellRenderer;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  @Override
  public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    if (columnIndex == 1) {
      ChatWindowChatTableCellRenderer renderer = getPreparedCellRenderer(rowIndex, columnIndex);
      Point mousePosition = MouseInfo.getPointerInfo().getLocation();
      Point componentPosition = renderer.getLocation();
      Point relativeMousePosition =
          new Point(mousePosition.x - componentPosition.x, mousePosition.y - componentPosition.y);
      System.out.println("changing selection: " + relativeMousePosition.toString());
      renderer.selectAll();
      System.out.format(
          "[row: %d, column: %d] text: %s\n", rowIndex, columnIndex, renderer.getText());
    }
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
      if (i == columnIndex) {
        continue;
      }
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
      ChatWindowChatTableCellRenderer c = getPreparedCellRenderer(row, columnIndex);
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

  private ChatWindowChatTableCellRenderer getPreparedCellRenderer(int rowIndex, int columnIndex) {
    return (ChatWindowChatTableCellRenderer)
        prepareRenderer(getCellRenderer(rowIndex, columnIndex), rowIndex, columnIndex);
  }
}

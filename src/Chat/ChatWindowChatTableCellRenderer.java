package Chat;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;

public class ChatWindowChatTableCellRenderer extends JTextPane implements TableCellRenderer {
  private Color backgroundColor = Color.decode("#282828");

  public ChatWindowChatTableCellRenderer() {
    Border eb = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    //        setEditable(false);
    setBorder(eb);
    setBackground(backgroundColor);
    setOpaque(false);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String text = value != null ? value.toString() : "";

    StyledDocument document = getStyledDocument();

    try {
      // Clear existing content in the cell
      document.remove(0, document.getLength());
    } catch (BadLocationException e) {
      throw new RuntimeException(e);
    }

    if (column == 0) {
      // Write username
      SimpleAttributeSet attribs = new SimpleAttributeSet();
      StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_RIGHT);
      StyleConstants.setBold(attribs, true);

      int offset = document.getLength();
      try {
        document.insertString(offset, text, attribs);
        document.setParagraphAttributes(offset, text.length(), attribs, false);
      } catch (BadLocationException e) {
        throw new RuntimeException(e);
      }
    } else if (column == 1) {
      // Write message
      SimpleAttributeSet attribs = new SimpleAttributeSet();
      StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_LEFT);
      StyleConstants.setBold(attribs, false);

      int offset = document.getLength();
      try {
        document.insertString(offset, text, attribs);
        document.setParagraphAttributes(offset, text.length(), attribs, false);
      } catch (BadLocationException e) {
        throw new RuntimeException(e);
      }
    }

    return this;
  }

  public void adjustRowHeight(JTable table, int row, int column) {
    // Set the component width to match the width of its table cell
    // and make the height arbitrarily large to accomodate all the contents
    int currentWidth = table.getColumnModel().getColumn(column).getWidth();
    int tempRowHeight = Short.MAX_VALUE;
    setSize(currentWidth, tempRowHeight);

    // Get the current table row height
    int currentRowHeight = table.getRowHeight(row);

    // Prepare cell renderer and get the new preferred height
    Component prepared = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
    int newHeight = prepared.getPreferredSize().height;

    // Set table row height to fitted height.
    // Important to check if this has been done already
    // to prevent a never-ending loop.
    if (newHeight != currentRowHeight) {
      table.setRowHeight(newHeight);
    }
  }
}

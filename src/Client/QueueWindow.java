/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/OrN/rscplus>
 */
package Client;

import static javax.swing.JComponent.WHEN_FOCUSED;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
//import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import Game.Replay;
import Game.ReplayQueue;

/**
 * GUI designed for the RSCPlus client that manages the replay playlist queue
 */
public class QueueWindow {
  static JTable playlistTable = new JTable(new PlaylistModel());
  static PlaylistModel model = (PlaylistModel) playlistTable.getModel();
  static JLabel replayCountLabel = new JLabel("0 replays");
  static private JFrame frame;
  static private JButton button;
	static private Font controlsFont;
  static private boolean reorderIsPointless = true; //helper bool to stop copyTableToQueue if nothing in table has changed

  public QueueWindow() {
    try {
      // Set System L&F as a fall-back option.
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
          laf.getDefaults().put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 11));
          laf.getDefaults().put("Table.alternateRowColor", new Color(230,230,255));
          break;
        }
      }
    } catch (UnsupportedLookAndFeelException e) {
      Logger.Error("Unable to set L&F: Unsupported look and feel");
    } catch (ClassNotFoundException e) {
      Logger.Error("Unable to set L&F: Class not found");
    } catch (InstantiationException e) {
      Logger.Error("Unable to set L&F: Class object cannot be instantiated");
    } catch (IllegalAccessException e) {
      Logger.Error("Unable to set L&F: Illegal access exception");
    }
    initialize();
  }

  public void showQueueWindow() {
    copyQueueToTable();
    frame.setVisible(true);
  }

  public void hideQueueWindow() {
    frame.setVisible(false);
  }

  /** Initialize the contents of the frame. */
  private static void initialize() {
    Logger.Info("Creating queue window");
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the replay queue window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the replay queue window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }
	
	private static void controlsFontInit() {
		String uppermostChar = "\uD83D\uDD00";
		String currentFontName = "";
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
		if (font.canDisplayUpTo(uppermostChar) < 0) {
			controlsFont = font;
			return;
		}

		// current font is not suitable, using our fallback
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		InputStream is = Settings.getResourceAsStream("/assets/Symbola_Hinted.ttf");
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
			font = new Font("Symbola", Font.PLAIN, 18);
			controlsFont = font;
			if (font.canDisplayUpTo(uppermostChar) < 0) {
				controlsFont = font;
				return;
			} else {
				controlsFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
			}
		} catch (FontFormatException | IOException e) {
			controlsFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
		}
	}

  private static void runInit() {
		controlsFontInit();
		
    //ToolTipManager.sharedInstance().setInitialDelay(0);
    //ToolTipManager.sharedInstance().setDismissDelay(500);
    frame = new JFrame();
    frame.setTitle("ıllıllı [ Replay Queue ] ıllıllı");
    frame.setBounds(100, 100, 800, 580);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout(0, 0));
    URL iconURL = Settings.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }

    // Container declarations
    JPanel navigationPanel = new JPanel();
    JPanel playlistPanel = new JPanel(new GridLayout());
    JPanel musicPlayerPanel = new JPanel();

    playlistTable.setFillsViewportHeight(true);

    // enable clicking header to sort it
    playlistTable.setAutoCreateRowSorter(true);

    // disable rearranging columns
    playlistTable.getTableHeader().setReorderingAllowed(false);

    // rearrange rows by dragging and dropping them
    playlistTable.setDragEnabled(true);
    playlistTable.setDropMode(DropMode.INSERT_ROWS);
    playlistTable.setTransferHandler(new TableRowTransferHandler(playlistTable));

    // assign TableColumn vars
    TableColumn currentlyPlayingIndicatorCol = playlistTable.getColumnModel().getColumn(0);
    TableColumn numberInQueueCol = playlistTable.getColumnModel().getColumn(1);
    TableColumn folderPathCol = playlistTable.getColumnModel().getColumn(2);
    TableColumn replayNameCol = playlistTable.getColumnModel().getColumn(3);
    TableColumn replayLengthCol = playlistTable.getColumnModel().getColumn(4);
    TableColumn dateModifiedCol = playlistTable.getColumnModel().getColumn(5);
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          setHorizontalAlignment(JLabel.CENTER);
          return this;
      }
    };
    DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.CENTER);
        try {
          if (Settings.PREFERRED_DATE_FORMAT.get(Settings.currentProfile).trim().length() > 0) {
            this.setText(new SimpleDateFormat(Settings.PREFERRED_DATE_FORMAT.get(Settings.currentProfile)).format(value));
          } else {
            this.setText(new SimpleDateFormat(Settings.PREFERRED_DATE_FORMAT.get("default")).format(value));
          }
        } catch (IllegalArgumentException e) {
          this.setText(new SimpleDateFormat(Settings.PREFERRED_DATE_FORMAT.get("default")).format(value));
          Logger.Warn("The date string you provided in the Replay Settings tab is invalid: ");
          Logger.Warn(e.toString());
        }
        return this;
      }
    };
    DefaultTableCellRenderer timesliceRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.CENTER);

        int total_centiseconds = (int)value*2; //50fps * 2; converts to hundreths of a second
        int leftover_centiseconds = total_centiseconds % 100;
        int total_seconds = (total_centiseconds - leftover_centiseconds) / 100;
        int leftover_seconds = total_seconds % 60;
        int total_minutes = (total_seconds - leftover_seconds) / 60;
        int leftover_minutes = total_minutes % 60;
        int total_hours = (total_minutes - leftover_minutes) / 60;

        if (total_hours > 0) {
          this.setText(String.format("%d:%02d:%02d", total_hours, leftover_minutes, leftover_seconds));
        } else if (leftover_minutes > 0) {
          this.setText(String.format("%d:%02d", leftover_minutes, leftover_seconds));
        } else {
          this.setText(String.format("%d.%02d", leftover_seconds, leftover_centiseconds));
        }

        return this;
      }
    };
    DefaultTableCellRenderer cutoffLeftRenderer = new CutoffLeftRenderer();

    // center table headers
    JTableHeader header = playlistTable.getTableHeader();
    header.setDefaultRenderer(new HeaderRenderer(playlistTable));

    // set column widths & renderer
    currentlyPlayingIndicatorCol.setMinWidth(20);
    currentlyPlayingIndicatorCol.setMaxWidth(20);
    currentlyPlayingIndicatorCol.setCellRenderer(centerRenderer);
    numberInQueueCol.setMinWidth(20);
    numberInQueueCol.setMaxWidth(80);
    numberInQueueCol.setPreferredWidth(50);
    numberInQueueCol.setCellRenderer(centerRenderer);
    folderPathCol.setCellRenderer(cutoffLeftRenderer);
    replayLengthCol.setMinWidth(60);
    replayLengthCol.setMaxWidth(150);
    replayLengthCol.setPreferredWidth(60);
    replayLengthCol.setCellRenderer(timesliceRenderer);
    dateModifiedCol.setMinWidth(140);
    dateModifiedCol.setMaxWidth(250);
    dateModifiedCol.setPreferredWidth(140);
    dateModifiedCol.setCellRenderer(dateRenderer);

    copyQueueToTable();

    // Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(playlistTable);
    playlistPanel.add(scrollPane);

    // <add navpanel buttons>
    // =====================
    navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));
    addButton("Reorder", navigationPanel, Component.LEFT_ALIGNMENT)
            .addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        copyTableToQueue();
                        ReplayQueue.skipped = false;
                        reorderIsPointless = true;
                      }
                    });


    addButton("Clear Queue", navigationPanel, Component.LEFT_ALIGNMENT)
            .addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        ReplayQueue.clearQueue();
                        QueueWindow.copyQueueToTable();
                        model.fireTableDataChanged();
                      }
                    });

    navigationPanel.add(Box.createHorizontalGlue());

    JLabel windowHeader = new JLabel("ıllıllı [ Replay Queue ] ıllıllı");
    navigationPanel.add(windowHeader);
    windowHeader.setAlignmentY(Component.CENTER_ALIGNMENT);
    windowHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
    windowHeader.setFont(new Font("",Font.TRUETYPE_FONT,24));
    windowHeader.setBorder(new EmptyBorder(0, 2, 0, 2));

    navigationPanel.add(Box.createHorizontalGlue());

    addButton("Clear Sort", navigationPanel, Component.LEFT_ALIGNMENT)
            .addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        reorderIsPointless = true;
                        clearSort();
                      }
                    });

    addButton("Exit", navigationPanel, Component.RIGHT_ALIGNMENT)
            .addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        Launcher.getQueueWindow().hideQueueWindow();
                      }
                    });
    // </add navpanel buttons>
    // <add musicplayerpanel buttons>
    // ⏮ ⏪ ⏯ ⏩ ⏭ ◼ ======================= 🔀
    // TODO: 🔂 🔁
    musicPlayerPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    c.weightx = 0;

    formatButton("⏮");
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                ReplayQueue.skipped = true;
                ReplayQueue.previousReplay();
              }
            });
    c.gridx = 0;
    musicPlayerPanel.add(button, c);

    formatButton("⏪");
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                Replay.controlPlayback("ff_minus");
              }
            });
    c.gridx = 1;
    musicPlayerPanel.add(button, c);

    formatButton("⏯");
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                Replay.togglePause();
              }
            });
    c.gridx = 2;
    musicPlayerPanel.add(button, c);

    formatButton("⏩");
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                Replay.controlPlayback("ff_plus");
              }
            });
    c.gridx = 3;
    musicPlayerPanel.add(button, c);

    formatButton("⏭");
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                ReplayQueue.skipped = true;
                ReplayQueue.nextReplay();
              }
            });
    c.gridx = 4;
    musicPlayerPanel.add(button, c);


    c.anchor = GridBagConstraints.LINE_END;
    formatButton("\uD83D\uDD00"); //🔀
    button.addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                int size = ReplayQueue.queue.size();
                if (size > 0) {
                  Random rand = new Random(); //random enough
                  ReplayQueue.skipToReplay(rand.nextInt(size));
                }
              }
            });
    c.gridx = 10;
    musicPlayerPanel.add(button, c);

    c.gridx = 11;
    replayCountLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
    musicPlayerPanel.add(replayCountLabel, c);

    // tell the panel to add a bunch of blank space in the middle
    c.gridx = 5;
    c.weightx = 1; // give spacing priority to this element
    JLabel blank = new JLabel("");
    musicPlayerPanel.add(blank, c);
    // </add musicplayerpanel buttons>



    // padding for aesthetics
    navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    playlistPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    musicPlayerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    frame.getContentPane().add(navigationPanel, BorderLayout.PAGE_START);
    frame.getContentPane().add(playlistPanel, BorderLayout.CENTER);
    frame.getContentPane().add(musicPlayerPanel, BorderLayout.PAGE_END);

    // add double-click to switch to replay functionality
    playlistTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent mouseEvent) {
        int row = playlistTable.rowAtPoint(mouseEvent.getPoint());
        if (mouseEvent.getClickCount() == 2 && playlistTable.getSelectedRow() != -1) {
          ReplayQueue.skipToReplay(playlistTable.getRowSorter().convertRowIndexToModel(row));
        }
      }
    });

    // listener for updating label of how many rows are selected
    playlistTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateReplayCountLabel();
      }
    });

    // add functionality to delete replays with the delete key
    InputMap inputMap = playlistTable.getInputMap(WHEN_FOCUSED);
    ActionMap actionMap = playlistTable.getActionMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
    actionMap.put("delete", new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        int[] selectedRows = playlistTable.getSelectedRows();
        int[] selectedRowsConverted = new int[selectedRows.length];

        if (reorderIsPointless) {
          // this is just for speed, convertRowIndexToModel would still work
          selectedRowsConverted = selectedRows;
        } else {
          for (int i = 0; i < selectedRows.length; i++) {
            selectedRowsConverted[i] = playlistTable.getRowSorter().convertRowIndexToModel(selectedRows[i]);
          }
          Arrays.sort(selectedRowsConverted);
        }
        for (int i = selectedRowsConverted.length - 1; i >= 0; i--) {
          ReplayQueue.removeReplay((int)(model.getRow(selectedRowsConverted[i])[1]) - 1);
          model.removeRow(selectedRowsConverted[i]);
        }
        if (selectedRows.length > 0) {
          // update second column
          for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(new Integer(i+1),i,1);
          }
        }
      }
    });

    // detect when rows are sorted
    addTheRowSorterListener();

    /*
    presetsScrollPane.setViewportView(presetsPanel);
    presetsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setScrollSpeed(presetsScrollPane, 20, 15);
    */
  }

  /**
   * Sets the scroll speed of a JScrollPane
   *
   * @param scrollPane The JScrollPane to modify
   * @param horizontalInc The horizontal increment value
   * @param verticalInc The vertical increment value
   */
  private static void setScrollSpeed(JScrollPane scrollPane, int horizontalInc, int verticalInc) {
    scrollPane.getVerticalScrollBar().setUnitIncrement(verticalInc);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(horizontalInc);
  }

  public static void copyQueueToTable() {
    Logger.Debug("copyQueueToTable called");
    model.getDataVector().removeAllElements();
    for (int i=0; i < ReplayQueue.queue.size(); i++) {
      String replayFolder = ReplayQueue.queue.get(i).getAbsolutePath();
      Object[] metadata = Replay.readMetadata(replayFolder);

      model.addRow(new Object[] {
              ReplayQueue.currentIndex - 1 == i ? "▶" : "",
              new Integer(i+1),
              new File(replayFolder).getParent(),
              ReplayQueue.queue.get(i).getName(),
              new Integer((int)metadata[0]),
              new Date((long)metadata[1])
      });
    }
    updateReplayCountLabel();
  }

  public static void copyTableToQueue() {
    Logger.Debug("copyTableToQueue called");
    // abort if user tried to copyTableToQueue for no reason
    if (reorderIsPointless) {
      Logger.Debug("Skipping reorder button press");
      return;
    }

    // copy table data to queue in correct order
    int size = model.getRowCount();
    ReplayQueue.clearQueue();
    for (int i = 0; i < size; i++) {
      int row = playlistTable.getRowSorter().convertRowIndexToModel(i);
      ReplayQueue.queue.add(new File((String)model.getRow(row)[2],(String)model.getRow(row)[3]));
    }

    // display newly reordered data in table
    clearSort();
    copyQueueToTable();
    Logger.Debug("Wrote sorted order to queue");
  }

  public static void updatePlaying() {
    if (model.getRowCount() >= ReplayQueue.lastIndex &&
            ReplayQueue.lastIndex >=1) {
      model.setValueAt("", ReplayQueue.lastIndex - 1, 0);
    }
    if (model.getRowCount() >= ReplayQueue.currentIndex &&
            ReplayQueue.currentIndex >= 1) {
      if (Replay.isPlaying) {
        model.setValueAt("▶", ReplayQueue.currentIndex - 1, 0);
      } else {
        model.setValueAt("", ReplayQueue.currentIndex - 1, 0);
      }
    }
  }

  public static void disposeJFrame() {
    frame.dispose();
  }


  public static interface Reorderable {
    public void reorder(int toIndex, int[] selectedRows);
  }

  static class PlaylistModel extends DefaultTableModel implements Reorderable {

    // Handle Columns
    private String[] columnNames = {"▶", //is the currently selected replay or not
                                    "#", //position the replay is in the replay queue
                                    "Folder Path", //folder containing replay folder
                                    "Replay Name", //replay folder name
                                    "Length", //how long the replay plays for
                                    "Date Modified"}; //date modified of keys.bin
    public int getColumnCount() {
      return columnNames.length;
    }
    public String getColumnName(int col) {
      return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
      return false;
    }

    /*
    public String getToolTipText(MouseEvent e) { //TODO generate tooltips somehow
      String tip = "";
      try {
        java.awt.Point p = e.getPoint();
        int rowIndex = playlistTable.rowAtPoint(p);
        int colIndex = playlistTable.columnAtPoint(p);
        tip = getValueAt(rowIndex, colIndex).toString();
      } catch (RuntimeException e1) {
        // catch null pointer exception if mouse is over an empty line
      }
      playlistTable.createToolTip().setTipText(tip);
      return tip;
    }
    */

    // Handle Rows
    public void addRow(Object[] rowData) {
      Vector<Object> rowVector = new Vector<>();
      for (int i=0; i <= 5; i++) {
        rowVector.add(rowData[i]);
      }
      super.addRow(rowVector);
    }

    public void insertRow(int i, Object[] rowData) {
      Vector<Object> rowVector = new Vector<>();
      for (int j=0; j <= 5; j++)
        rowVector.add(rowData[j]);
      super.insertRow(i, rowVector);
    }

    public Object[] getRow(int i) {
      if (getRowCount() >= i) {
        return new Object[]{getValueAt(i, 0), getValueAt(i, 1), getValueAt(i, 2), getValueAt(i, 3), getValueAt(i, 4), getValueAt(i, 5)};
      }
      return new Object[] {"","","","","",""};
    }

    // required for columns to not always sort as string (30 < 4)
    public Class getColumnClass(int c) {
      // specify each column manually b/c when table is empty, it dies
      if (c == 1 || c == 4) {
        return Integer.class;
      } else if (c == 5) {
        return Date.class;
      }
      return String.class;
    }

    @Override
    public void reorder(int toIndex, int[] rowFroms) {
      playlistTable.clearSelection();

      //check to see if the drop is a valid move or not
      for (int i = 0; i < rowFroms.length; i++) {
        if (rowFroms[i] == toIndex) {
          Logger.Debug("Refusing to move rows into themselves");
          return;
        }
      }
      if (!reorderIsPointless) {
        Logger.Debug("Refusing to reorder rows that are already being sorted");
        return;
      }

      //move the rows
      for (int i = 0; i < rowFroms.length; i++) {
        if (rowFroms[i] < toIndex) {
          //row moving forward, nothing needs to change b/c reference always shifting
          insertRow(toIndex, getRow(rowFroms[0]));
          removeRow(rowFroms[0]);
        } else {
          //row moving backward, rowFrom moved
          insertRow(toIndex + i, getRow(rowFroms[i]));
          removeRow(rowFroms[i] + 1);
        }
      }
      reorderIsPointless = false;
      copyTableToQueue();
      ReplayQueue.skipped = false;
      reorderIsPointless = true;
    }

  }

  // necessary to be able to center the headers with the same look and feel
  private static class HeaderRenderer implements TableCellRenderer {
    DefaultTableCellRenderer renderer;

    public HeaderRenderer(JTable table) {
      renderer = (DefaultTableCellRenderer)
      table.getTableHeader().getDefaultRenderer();
      renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
    }
  }

  // handles drag & drop row reordering
  public static class TableRowTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "application/x-java-Integer;class=java.lang.Integer", "Integer Row Index");
    private JTable           table             = null;

    public TableRowTransferHandler(JTable table) {
      this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
      assert (c == table);
      return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
      boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
      table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
      return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
      return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
      JTable target = (JTable) info.getComponent();
      JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
      int index = dl.getRow();
      int max = table.getModel().getRowCount();
      if (index < 0 || index > max)
        index = max;
      target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      try {
        Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
        if (rowFrom != -1 && rowFrom != index) {
          ((Reorderable)table.getModel()).reorder(index, table.getSelectedRows());
          if (index > rowFrom)
            index--;
          target.getSelectionModel().addSelectionInterval(index, index);
          return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
      if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }

  }

  // needed to be able to cut off left side instead of right side of Folder Path
  public static class CutoffLeftRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
      super.getTableCellRendererComponent(table, value,
              isSelected, hasFocus, row, column);
      // setHorizontalAlignment(JLabel.RIGHT); // TODO possibly offer this as an option

      int availableWidth = table.getColumnModel().getColumn(column).getWidth();
      availableWidth -= table.getIntercellSpacing().getWidth();
      Insets borderInsets = getBorder().getBorderInsets((Component)this);
      availableWidth -= (borderInsets.left + borderInsets.right);

      String cellText = getText();
      FontMetrics fm = getFontMetrics( getFont() );
      String cellTextReplaced = cellText.replace(Settings.REPLAY_BASE_PATH.get("custom"), "");

      if (cellText.equals(cellTextReplaced)) {
        if (System.getProperty("os.name").contains("Windows")) {
          cellText += "\\";
        } else {
          cellText += "/";
        }
      } else {
        if (System.getProperty("os.name").contains("Windows")) {
          cellText = String.format(".\\%s\\", cellTextReplaced);
        } else {
          cellText = String.format("./%s/", cellTextReplaced);
        }
      }

      if (fm.stringWidth(cellText) > availableWidth)
      {
        String dots = "...";
        int textWidth = fm.stringWidth( dots );
        int nChars = cellText.length() - 1;
        for (; nChars > 0; nChars--)
        {
          textWidth += fm.charWidth(cellText.charAt(nChars));

          if (textWidth > availableWidth)
          {
            break;
          }
        }

        setText(dots + cellText.substring(nChars + 1));

      } else {
        setText(cellText); // to preserve string replacement
      }

      return this;
    }
  }

  private static JButton addButton(String text, Container container, float alignment) {
    JButton button = new JButton(text);
    button.setAlignmentX(alignment);
    container.add(button);
    return button;
  }

  private static void formatButton(String text) {
    button = new JButton(text);
		button.setFont(controlsFont);
    button.setMargin(new Insets(-5,-7,-2,-7));
  }

  private static void updateReplayCountLabel() {
    replayCountLabel.setText(String.format("<html><body>%d replays<br>%d selected</body></html>", ReplayQueue.queue.size(), playlistTable.getSelectedRowCount()));
  }

  private static void clearSort() {
    playlistTable.setAutoCreateRowSorter(false);
    playlistTable.setAutoCreateRowSorter(true);
    playlistTable.getTableHeader().repaint();
    addTheRowSorterListener();
  }

  private static void addTheRowSorterListener() {
    playlistTable.getRowSorter().addRowSorterListener(new RowSorterListener() {
      @Override
      public void sorterChanged(RowSorterEvent rowSorterEvent) {
        reorderIsPointless = false;
      }
    });
  }
}

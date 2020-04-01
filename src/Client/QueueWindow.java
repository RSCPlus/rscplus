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
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
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
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
//import javax.swing.ToolTipManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.*;
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
  static private String editValue = "@:/@";
  static private boolean editingEnabled = false;
  static private boolean reorderIsPointless = true; //helper bool to stop copyTableToQueue if nothing in table has changed
  static TableColumn serverCol;
  static TableColumn converterSettingsCol;
  static TableColumn userFieldCol;

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
    serverCol = playlistTable.getColumnModel().getColumn(6);
    converterSettingsCol = playlistTable.getColumnModel().getColumn(7);
    userFieldCol = playlistTable.getColumnModel().getColumn(8);
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

        this.setText(Util.formatTimeLongShort((int)value));

        return this;
      }
    };
    DefaultTableCellRenderer cutoffLeftRenderer = new CutoffLeftRenderer();

    //get "previous" value of cell being edited
    playlistTable.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          Object ob = evt.getNewValue();
          if (evt.getPropertyName().equals("tableCellEditor") && ob != null)  {
            if (ob instanceof DefaultCellEditor) {
              editValue = ((JTextField)((DefaultCellEditor)ob).getComponent()).getText();
            }
          }
        }
      }
    );

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
    folderPathCol.setMinWidth(85);
    folderPathCol.setPreferredWidth(250);
    replayNameCol.setMinWidth(100);
    replayNameCol.setPreferredWidth(250);
    replayLengthCol.setMinWidth(67);
    replayLengthCol.setMaxWidth(150);
    replayLengthCol.setPreferredWidth(60);
    replayLengthCol.setCellRenderer(timesliceRenderer);
    dateModifiedCol.setMinWidth(140);
    dateModifiedCol.setMaxWidth(250);
    dateModifiedCol.setPreferredWidth(140);
    dateModifiedCol.setCellRenderer(dateRenderer);

    serverCol.setMinWidth(60);
    serverCol.setMaxWidth(200);
    serverCol.setCellRenderer(centerRenderer);

    converterSettingsCol.setMinWidth(40);
    converterSettingsCol.setMaxWidth(200);
    converterSettingsCol.setCellRenderer(centerRenderer);

    userFieldCol.setMinWidth(40);
    userFieldCol.setMaxWidth(200);
    userFieldCol.setCellRenderer(centerRenderer);

    if (!Settings.SHOW_WORLD_COLUMN.get(Settings.currentProfile))
      playlistTable.removeColumn(serverCol);
    if (!Settings.SHOW_CONVERSION_COLUMN.get(Settings.currentProfile))
      playlistTable.removeColumn(converterSettingsCol);
    if (!Settings.SHOW_USERFIELD_COLUMN.get(Settings.currentProfile))
      playlistTable.removeColumn(userFieldCol);

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

    addButton("Rename", navigationPanel, Component.LEFT_ALIGNMENT)
            .addActionListener(
                    new ActionListener() {
                      @Override
                      public void actionPerformed(ActionEvent e) {
                        editingEnabled = !editingEnabled;
                        if (editingEnabled) {
                          Logger.Info("@|green Toggled On:|@ Replays can now be renamed by double clicking the \"Replay Name\" column or by single clicking that column and pressing F2.");
                        } else {
                          Logger.Info("@|green Toggled Off:|@ You can now double click anywhere in each row to switch to that replay");
                        }
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
        int column = playlistTable.columnAtPoint(mouseEvent.getPoint());
        if (mouseEvent.getClickCount() == 2 && playlistTable.getSelectedRow() != -1 && !playlistTable.isCellEditable(row, column)) {
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
          ReplayQueue.removeReplay((int)model.getValueAt(selectedRowsConverted[i], 1) - 1);
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

    // add functionality to go back to unsorted mode by clicking three times on the header
    playlistTable.setRowSorter(new TableRowSorter(model));

    // Add MouseListener for onClick event
    playlistTable.getTableHeader().addMouseListener(new MouseAdapter() {
      private SortOrder currentOrder = SortOrder.UNSORTED;
      private int lastCol = -1;

      @Override
      public void mouseClicked(MouseEvent e) {
        int column = playlistTable.getTableHeader().columnAtPoint(e.getPoint());
        RowSorter sorter = playlistTable.getRowSorter();

        if (column != lastCol) {
          currentOrder = SortOrder.UNSORTED;
          lastCol = column;
        }

        for (RowSorter.SortKey sortKey : playlistTable.getRowSorter().getSortKeys()) {
          if (sortKey.getColumn() != column) {
            currentOrder = SortOrder.UNSORTED;
          }
          break;
        }
        if (reorderIsPointless) {
          currentOrder = SortOrder.UNSORTED;
        }

        List sortKeys = new ArrayList();
        if (e.getButton() == MouseEvent.BUTTON1) {
          switch (currentOrder) {
            case UNSORTED:
              reorderIsPointless = false;
              sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.ASCENDING));
              break;
            case ASCENDING:
              reorderIsPointless = false;
              sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.DESCENDING));
              break;
            case DESCENDING:
              reorderIsPointless = true;
              sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.UNSORTED));
              break;
          }
          sorter.setSortKeys(sortKeys);
        }
      }
    });

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

  public static void syncColumnsWithSettings() {
    int serverColView = playlistTable.convertColumnIndexToView(6);
    int conversionColView = playlistTable.convertColumnIndexToView(7);
    int userFieldColView = playlistTable.convertColumnIndexToView(8);

    if (Settings.SHOW_WORLD_COLUMN.get(Settings.currentProfile)) {
      if (serverColView == -1) {
        // Must remove columns ahead, because addColumn only supports adding to the end without destroying existing TableColumn
        if (conversionColView != -1) {
          playlistTable.removeColumn(converterSettingsCol);
        }
        if (userFieldColView != -1) {
          playlistTable.removeColumn(userFieldCol);
        }

        // add column
        playlistTable.addColumn(serverCol);

        // add back columns removed
        if (conversionColView != -1) {
          playlistTable.addColumn(converterSettingsCol);
        }
        if (userFieldColView != -1) {
          playlistTable.addColumn(userFieldCol);
        }
      }
    } else {
      playlistTable.removeColumn(serverCol);
    }

    if (Settings.SHOW_CONVERSION_COLUMN.get(Settings.currentProfile)) {
      if (playlistTable.convertColumnIndexToView(7) == -1) {
        if (userFieldColView != -1) {
          playlistTable.removeColumn(userFieldCol);
        }
        playlistTable.addColumn(converterSettingsCol);
        if (userFieldColView != -1) {
          playlistTable.addColumn(userFieldCol);
        }
      }
    } else {
      playlistTable.removeColumn(converterSettingsCol);
    }

    if (Settings.SHOW_USERFIELD_COLUMN.get(Settings.currentProfile)) {
      if (playlistTable.convertColumnIndexToView(8) == -1) {
        playlistTable.addColumn(userFieldCol);
      }
    } else {
      playlistTable.removeColumn(userFieldCol);
    }
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
              new Date((long)metadata[1]),
              metadata[2],
              new Integer((byte)metadata[3]),
              new Integer((int)metadata[4])
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
      ReplayQueue.queue.add(new File((String)model.getValueAt(row, 2),(String)model.getValueAt(row, 3)));
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
            "Date Modified", //date modified of keys.bin
            "World",
            "Conversion Settings",
            "User Field"};

    public int getColumnCount() {
      return columnNames.length;
    }
    public String getColumnName(int col) {
      return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
      if (col == 3 && editingEnabled) { //Replay Name Column
        return true;
      }
      return false;
    }

    @Override
    public void fireTableCellUpdated(int row, int col) {
      if (col == 3) {
        String afterEditValue = (String) model.getValueAt(row, col);

        //rename folder
        if (afterEditValue.indexOf('/') == -1) {
          if (!editValue.equals(afterEditValue) && !editValue.equals("@:/@")) {
            File renamedFile = new File(ReplayQueue.queue.get(row).getParent(), afterEditValue);
            Logger.Debug("We'd like to rename to: " + renamedFile.getAbsolutePath());
            if (!ReplayQueue.queue.get(row).renameTo(renamedFile)) {
              Logger.Warn("@|red Failed to rename row: |@" + ReplayQueue.queue.get(row).getAbsolutePath());
              if (System.getProperty("os.name").contains("Windows")) {
                if (afterEditValue.matches(".*[?%*:|\"<>]")) {
                  Logger.Warn(String.format("@|yellow You're on Windows and you tried to use a restricted character in your desired filename: |@@|red %s|@", afterEditValue));
                } else {
                  Logger.Warn("@|yellow Probably this is because you're |@@|red using Windows |@@|yellow and Windows locks the replay files while they are in use. There are workarounds, but my advice is to |@@|green use Debian!|@");
                  Logger.Warn("@|yellow You can also try just advancing to the next replay, in order to name the replay you're currently watching, if you would like to stop watching this replay at this time.|@");
                }
              }
              copyQueueToTable();
            } else {
              //Instances of the File class are immutable, so after calling renameTo, we must update the pathname to the new one
              ReplayQueue.queue.set(row, renamedFile);
              Logger.Info(String.format("Renamed @|green %s|@ to @|cyan %s|@", editValue, afterEditValue));
            }
          }
        } else {
          Logger.Warn(String.format("@|yellow RSC+ is not programmed to rename folders into subdirectories. Your offending filename: |@@|red %s|@",afterEditValue));
          copyQueueToTable();
        }
        editValue = "@:/@";
      }

      super.fireTableCellUpdated(row, col);
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
      for (int i=0; i < getColumnCount(); i++) {
        rowVector.add(rowData[i]);
      }
      super.addRow(rowVector);
    }

    public void insertRow(int i, Object[] rowData) {
      Vector<Object> rowVector = new Vector<>();
      for (int j=0; j < getColumnCount(); j++)
        rowVector.add(rowData[j]);
      super.insertRow(i, rowVector);
    }

    public Object[] getRow(int i) {
      Object[] thisRow = new Object[getRowCount()];

      for (int j=0; j < getColumnCount(); j++) {
        if (getRowCount() >= i) {
          thisRow[i] = getValueAt(i, j);
        } else {
          thisRow[i] = "";
        }
      }
      return thisRow;
    }

    // required for columns to not always sort as string (30 < 4)
    public Class getColumnClass(int c) {
      // specify each column manually b/c when table is empty, it dies
      if (c == 1 || c == 4 || c == 7 || c == 8) {
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
    if (Settings.DEBUG.get(Settings.currentProfile)) {
      updateSelectedLengthLabel();
    }
  }

  private static void updateSelectedLengthLabel() {
    int[] selectedRows = playlistTable.getSelectedRows();
    int timeSum = 0;
    for (int i=0; i < selectedRows.length; i++) {
      if (selectedRows[i] < playlistTable.getRowCount() && selectedRows[i] >= 0)
        try {
          timeSum += (int) playlistTable.getValueAt(selectedRows[i], 4);
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }
    Logger.Info(String.format("Total Selected Time: %s", Util.formatTimeLongShort(timeSum)));
  }

  private static void clearSort() {
    playlistTable.setAutoCreateRowSorter(false);
    playlistTable.setAutoCreateRowSorter(true);
    playlistTable.getTableHeader().repaint();
  }

}

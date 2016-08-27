package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Game.Game;

public class NotificationsHandler {
		
	static JFrame notificationFrame;
	static JLabel iconLabel;
	static JLabel notificationTitle;
	static JLabel notificationLabel;
	static JPanel mainContentPanel;
	
	/**
	 * @wbp.parser.entryPoint
	 * Initializes the Notification JFrame and prepares it to receive notifications
	 */
	public static void initialize() {
		
		NotifsShowGameMouseListener mouseManager = new NotifsShowGameMouseListener();
		
		notificationFrame = new JFrame();
		JPanel contentPanel = new JPanel();
		notificationFrame.setContentPane(contentPanel);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		notificationFrame.setBounds(width-446, height-154, 449, 104);
		notificationFrame.setUndecorated(true);
		notificationFrame.setShape(new RoundRectangle2D.Double(0, 0, notificationFrame.getWidth(), notificationFrame.getHeight(), 16, 16));
		notificationFrame.setBackground(new Color(0,0,0,0));
		notificationFrame.setAutoRequestFocus(false);
		notificationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		notificationFrame.setType(Window.Type.UTILITY);
		notificationFrame.setAlwaysOnTop(true);	
		
		contentPanel.setLayout(null);
		contentPanel.setBackground(new Color(0,0,0,0));
		
		mainContentPanel = new JPanel();
		mainContentPanel.setBounds(13, 13, 423, 79);
		mainContentPanel.setLayout(null);
		mainContentPanel.setBackground(new Color(0,0,0,0));
		mainContentPanel.addMouseListener(mouseManager);
		contentPanel.add(mainContentPanel);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0,0,0,0));
		panel.setBounds(0, 0, 79, 79);
		panel.setLayout(new BorderLayout(0, 0));
		mainContentPanel.add(panel);

		
		iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(Settings.getResource("/assets/icon.png")));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(iconLabel, BorderLayout.CENTER);
					
		notificationTitle = new JLabel();
		notificationTitle.setBounds(91, 3, 326, 26);
		notificationTitle.setForeground(Color.BLACK);
		mainContentPanel.add(notificationTitle);
		
		notificationLabel = new JLabel();
		notificationLabel.setBounds(91, 30, 326, 43);
		mainContentPanel.add(notificationLabel);
		
		JButton closeButton = new JButton("");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setNotificationWindowVisible(false);
			}
		});
		closeButton.setBounds(400, 5, 17, 17);
		closeButton.setIcon(new ImageIcon(Settings.getResource("/assets/notification_close.png")));
		closeButton.setSelectedIcon(new ImageIcon(Settings.getResource("/assets/notification_close_highlighted.png")));
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		mainContentPanel.add(closeButton);
		
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Settings.getResource("/assets/notification_background.png")));
		lblNewLabel.setBounds(0, 0, 442, 104);
		lblNewLabel.setBackground(new Color(0,0,0,0));
		lblNewLabel.setForeground(new Color(0,0,0,0));
		lblNewLabel.setOpaque(false);
		contentPanel.add(lblNewLabel);
		
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, Settings.getResourceAsStream("/assets/OpenSans-Regular.ttf"));
			Font boldFont = Font.createFont(Font.TRUETYPE_FONT, Settings.getResourceAsStream("/assets/OpenSans-Bold.ttf"));
			
			notificationTitle.setFont(boldFont.deriveFont(Font.BOLD, 18f));
			notificationLabel.setFont(font.deriveFont(Font.PLAIN, 16f));
		} catch (FontFormatException | IOException e) {
			Logger.Error("Error while setting up notifications font:" + e.getMessage());
			e.printStackTrace();
		}
		
		loadNotificationSound();
		notificationFrame.repaint();
	}
	
	/**
	 * Displays a notification, playing sound if it is enabled
	 * @param title - The title of the notification
	 * @param text - Text message of the notification
	 */
	public static void displayNotification(String title, String text) {
		setNotificationWindowVisible(true);
		notificationTitle.setText(title);
		notificationLabel.setText("<html>" + text + "</html>");
		notificationFrame.repaint();
		if (Settings.NOTIFICATION_SOUNDS) {
			playNotificationSound();
		}
	}
	
	/**
	 * Sets visibility of the notification window
	 * @param isVisible - Whether the window should be visible
	 */
	public static void setNotificationWindowVisible(boolean isVisible) {
		notificationFrame.setVisible(isVisible);
	}
	
	private static URL urlToNotificationSound;
	private static AudioInputStream notificationAudioIn;
	private static Clip notificationSoundClip;
	
	public static void loadNotificationSound() {
		try {
			urlToNotificationSound = new File("assets/notification.wav").toURI().toURL();
			notificationAudioIn = AudioSystem.getAudioInputStream(urlToNotificationSound);
			notificationSoundClip = AudioSystem.getClip();
			notificationSoundClip.open(notificationAudioIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public static void playNotificationSound() {
		notificationSoundClip.setMicrosecondPosition(0);
		notificationSoundClip.start();
	}
	
	public static void closeNotificationSoundClip() {
		notificationSoundClip.close();
	}
	
	public static void disposeNotificationHandler() {
		notificationFrame.dispose();
	}
	
}

class NotifsShowGameMouseListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent arg0) {
		Game.getInstance().toFront();
		NotificationsHandler.setNotificationWindowVisible(false);
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
	
}

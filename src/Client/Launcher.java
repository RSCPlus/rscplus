package Client;

import Game.Game;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Launcher extends JFrame implements Runnable
{
	public Launcher()
	{
		instance = this;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("rscplus Launcher");
		setResizable(false);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setLayout(new FlowLayout());

		// Set size
		getContentPane().setPreferredSize(new Dimension(300, 120));
		pack();
		setMinimumSize(getSize());

		// Add components
		JLabel label = new JLabel("World: ");
		label.setForeground(Color.GRAY.brighter());
		getContentPane().add(label);

		m_worldSelector = new JComboBox();
		for(int i = 0; i < Settings.WORLD_LIST.length; i++)
			m_worldSelector.addItem(Settings.WORLD_LIST[i]);
		m_worldSelector.setSelectedIndex(Settings.WORLD - 1);
		m_worldSelector.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.WORLD = m_worldSelector.getSelectedIndex() + 1;
			}
		});

		getContentPane().add(m_worldSelector);

		label = new JLabel("Resolution: ");
		label.setForeground(Color.GRAY.brighter());
		getContentPane().add(label);
		m_resolutionWidth = new JTextField(3);
		m_resolutionWidth.setText("" + Settings.RESOLUTION.width);
		getContentPane().add(m_resolutionWidth);
		label = new JLabel("x");
		label.setForeground(Color.GRAY.brighter());
		getContentPane().add(label);
		m_resolutionHeight = new JTextField(3);
		m_resolutionHeight.setText("" + Settings.RESOLUTION.height);
		getContentPane().add(m_resolutionHeight);

		m_progressBar = new JProgressBar();
		m_progressBar.setStringPainted(true);
		m_progressBar.setBorderPainted(false);
		m_progressBar.setForeground(Color.GRAY.brighter());
		m_progressBar.setBackground(Color.BLACK);
		m_progressBar.setString("Waiting for game to be launched...");
		getContentPane().add(m_progressBar);

		m_launchButton = new JButton("Launch");
		m_launchButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				m_worldSelector.setEnabled(false);
				m_resolutionWidth.setEnabled(false);
				m_resolutionHeight.setEnabled(false);
				m_launchButton.setEnabled(false);
				new Thread(Launcher.instance).start();
			}
		});
		getContentPane().add(m_launchButton);

		// Set window icon
		URL iconURL = getClass().getResource("/assets/icon.png");
		if(iconURL != null)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void run()
	{
		try
		{
			Settings.RESOLUTION.width = Integer.parseInt(m_resolutionWidth.getText());
			Settings.RESOLUTION.height = Integer.parseInt(m_resolutionHeight.getText());

			if(Settings.RESOLUTION.width < 512)
				Settings.RESOLUTION.width = 512;
			if(Settings.RESOLUTION.height < 346)
				Settings.RESOLUTION.height = 346;
		}
		catch(Exception e) {}

		Settings.Save();

		setStatus("Loading JConfig...");
		JConfig config = new JConfig();
		if(!config.fetch(Util.MakeWorldURL(Settings.WORLD)))
		{
			error("Unable to fetch JConfig");
			return;
		}

		if(!config.isSupported())
		{
			error("JConfig outdated");
			return;
		}

		JClassLoader classLoader = new JClassLoader();
		if(!classLoader.fetch(this, config.getJarURL()))
		{
			error("Unable to fetch Jar");
		}

		setStatus("Launching game...");
		Game game = new Game();
		try
		{
			Class<?> client = classLoader.loadClass(config.getJarClass());
			game.applet = (Applet)client.newInstance();
			game.applet.setStub(game);
			game.config = config;
		}
		catch(Exception e)
		{
			error("Unable to launch game");
			e.printStackTrace();
			return;
		}
		setVisible(false);
		dispose();
		game.start();
	}

	public void error(String text)
	{
		setStatus("Error: " + text);
		setProgress(0, 0);
		m_worldSelector.setEnabled(true);
		m_resolutionWidth.setEnabled(true);
		m_resolutionHeight.setEnabled(true);
		m_launchButton.setEnabled(true);
	}

	public void setStatus(String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				m_progressBar.setString(text);
			}
		});
	}

	public void setProgress(int value, int total)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(total == 0)
				{
					m_progressBar.setValue(0);
					return;
				}

				m_progressBar.setValue(value * 100 / total);
			}
		});
	}

	public static void main(String args[])
	{
		Settings.Load();
		new Launcher();
	}

	public static Launcher instance;

	private JComboBox m_worldSelector;
	private JTextField m_resolutionWidth;
	private JTextField m_resolutionHeight;
	private JProgressBar m_progressBar;
	private JButton m_launchButton;
}

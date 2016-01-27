/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Client;

import Game.Game;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
		getContentPane().setBackground(Color.BLACK);

		// Set window icon
		URL iconURL = Settings.getResource("/assets/icon.png");
		if(iconURL != null)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}

		// Set size
		getContentPane().setPreferredSize(new Dimension(280, 120));
		getContentPane().setLayout(null);
		setResizable(false);
		setTitle("rscplus Launcher");
		pack();
		pack();
		setLocationRelativeTo(null);

		// Add components
		JLabel label = new JLabel("World: ");
		label.setForeground(Color.GRAY.brighter());
		label.setSize(64, 12);
		label.setLocation(42, 16);
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
		m_worldSelector.setSize(150, 20);
		m_worldSelector.setLocation(label.getX() + label.getWidth(), label.getY() - 4);
		getContentPane().add(m_worldSelector);

		label = new JLabel("Resolution: ");
		label.setForeground(Color.GRAY.brighter());
		label.setSize(128, 12);
		label.setLocation(8, 40);
		getContentPane().add(label);
		m_resolutionWidth = new JTextField(3);
		m_resolutionWidth.setText("" + Settings.RESOLUTION.width);
		m_resolutionWidth.setSize(40, 20);
		m_resolutionWidth.setLocation(m_worldSelector.getX(), label.getY() - 4);
		getContentPane().add(m_resolutionWidth);
		label = new JLabel("x");
		label.setSize(128, 12);
		label.setLocation(m_worldSelector.getX() + m_resolutionWidth.getWidth() + 8, m_resolutionWidth.getY() + 3);
		label.setForeground(Color.GRAY.brighter());
		getContentPane().add(label);
		m_resolutionHeight = new JTextField(3);
		m_resolutionHeight.setText("" + Settings.RESOLUTION.height);
		m_resolutionHeight.setSize(40, 20);
		m_resolutionHeight.setLocation(label.getX() + 15, m_resolutionWidth.getY());
		getContentPane().add(m_resolutionHeight);

		m_progressBar = new JProgressBar();
		m_progressBar.setStringPainted(true);
		m_progressBar.setBorderPainted(true);
		m_progressBar.setForeground(Color.GRAY.brighter());
		m_progressBar.setBackground(Color.BLACK);
		m_progressBar.setString("Waiting for game to be launched...");
		m_progressBar.setSize(getContentPane().getWidth() - 16, 20);
		m_progressBar.setLocation(8, m_resolutionWidth.getY() + 30);
		getContentPane().add(m_progressBar);

		//pack();
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
		m_launchButton.setSize(100, 20);
		m_launchButton.setLocation((getContentPane().getWidth() / 2) - (m_launchButton.getWidth() / 2),
					   getContentPane().getHeight() - m_launchButton.getHeight() - 8);
		getContentPane().add(m_launchButton);

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

		classLoader = new JClassLoader();
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

	public void setStatus(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				m_progressBar.setString(text);
			}
		});
	}

	public void setProgress(final int value, final int total)
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

	public JClassLoader classLoader;

	private JComboBox m_worldSelector;
	private JTextField m_resolutionWidth;
	private JTextField m_resolutionHeight;
	private JProgressBar m_progressBar;
	private JButton m_launchButton;
}

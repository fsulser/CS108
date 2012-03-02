package client.lobby;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import shared.Log;
import shared.ServerAddress;
import client.net.DiscoveryClient;

public class SelectServer extends JPanel{
	private static final long serialVersionUID = 1L;
	private Vector<ServerAddress> vs_Servers;
	private JList jl_Dialog;
	private JButton bt_Join;
	private String[] sa_NoServers={"suchen ...","bitte haben Sie Geduld"};
	private int i_ScreenX;
	private int i_ScreenY;

	public SelectServer()
	{
		Log.DebugLog("Choose a server");

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel lbl_Dialog = new JLabel();
		lbl_Dialog.setText("Wählen Sie ihren Server:");
		lbl_Dialog.setBackground(new Color (255,255,255));
		lbl_Dialog.setOpaque(true);
		lbl_Dialog.setForeground(new Color (50,50,50));
		c.fill = GridBagConstraints.LINE_END;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		this.add(lbl_Dialog, c);
		
		jl_Dialog = new JList(new DefaultListModel());
		jl_Dialog.setVisibleRowCount(5);
		jl_Dialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jl_Dialog.setLayoutOrientation(JList.VERTICAL);
		
		vs_Servers = new Vector<ServerAddress>();
		
		jl_Dialog.setListData(sa_NoServers);
		jl_Dialog.setEnabled(false); //because no server found yet

		//suche regelmässig nach servern
		Timer timer = new Timer();
		int i_Delay = 1000;   
		int i_Period = 6000;
		
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				DiscoveryClient s = new DiscoveryClient();
				Thread t = new Thread(s);
				t.start();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// not important
					e.printStackTrace();
				}
				
				vs_Servers.clear();
				for(ServerAddress a : s.GetList())
				{
					vs_Servers.add(a);
				}
				
				if(vs_Servers.isEmpty()) //no server active
				{
					jl_Dialog.clearSelection();
					jl_Dialog.setEnabled(false);
					jl_Dialog.setListData(sa_NoServers);
					bt_Join.setEnabled(false);
				}else
				{	
					jl_Dialog.setListData(vs_Servers);
					jl_Dialog.setEnabled(true);
				}
				jl_Dialog.repaint();
				t.interrupt();
			}
		}, i_Delay, i_Period);
		
		jl_Dialog.addListSelectionListener(new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		          Log.DebugLog("Choosen "+vs_Servers.elementAt(evt.getFirstIndex())+" as Server");
		          bt_Join.setEnabled(true);
		        }
		});
		
		JScrollPane jsp_ServerPane = new JScrollPane(jl_Dialog);
		jsp_ServerPane.setPreferredSize(new Dimension(250, 80));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 40;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		c.insets=new Insets(0,0,10,0);
		this.add(jsp_ServerPane, c);
		
		
		JLabel lbl_User = new JLabel();
		lbl_User.setText("Wählen Sie ihren Benutzernamen:");
		lbl_User.setBackground(new Color (255,255,255));
		lbl_User.setOpaque(true);
		lbl_User.setForeground(new Color (50,50,50));
		c.fill = GridBagConstraints.LINE_END;
		c.ipady = -1;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 3;
		c.insets=new Insets(10,5,2,5);
		this.add(lbl_User, c);
		
		JFormattedTextField jft_UserName = new JFormattedTextField();
		jft_UserName.setColumns(10);
		jft_UserName.setText(System.getProperty("user.name"));
		c.fill = GridBagConstraints.LINE_END;
		c.ipady = -1;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 4;
		c.insets=new Insets(0,0,10,5);
		this.add(jft_UserName, c);
		
		
		bt_Join = new JButton("Server beitreten");
		bt_Join.setEnabled(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 20;      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 5;
		this.add(bt_Join, c);
		

		this.setPreferredSize(getMinimumSize());
	}
	public void paintComponent(Graphics g)
	{
		BufferedImage img = null;
		try 
		{
			img = ImageIO.read(new File("lobby_bg.jpg"));
			g.drawImage(img, 0, 0, this.i_ScreenX, this.i_ScreenY, 0, 0, img.getWidth(), img.getHeight(), new Color(0,0,0), null);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();

		}
	}
	public void setScreen(int x, int y) {
		this.i_ScreenX=x;
		this.i_ScreenY=y;
	}
}

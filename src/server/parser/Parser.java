package server.parser;

import java.util.NoSuchElementException;
import java.util.UUID;

import server.MainServer;
import server.Server;
import server.net.PlayerSocket;
import server.players.Player;
import shared.Log;
import shared.Protocol;

public class Parser 
{

	public Parser()
	{
		
	}
	
	/**
	 * Parses the given Message and sends the corresponding answers
	 * @param s_MSG the Message the Server socket received
	 * @return What the Socket should answer
	 */
	public void Parse(String s_MSG, PlayerSocket ps_sock)
	{
		String s_ParseMSG;
		
		if(s_MSG.length() >= 5)
		{
			s_ParseMSG = s_MSG.substring(0, 5).toUpperCase();
			if(ps_sock.getPlayer() == null && s_ParseMSG.compareTo(Protocol.CON_AUTH.toString()) != 0)
			{
				Log.ErrorLog("Critical: A player wasn't connected propperly");
				return;
			}
		}
		else
		{
			Log.WarningLog("Received a too short command");
			return;
		}

		Protocol command = Protocol.fromString(s_ParseMSG);
		
		switch(command)
		{
		case CON_AUTH://tested & works ~frank
			if(s_MSG.length()>7)
			{
				String s_PlayerID = s_MSG.substring(6, s_MSG.length());
				Player p = MainServer.getPlayerManager().findUUID(s_PlayerID);
				if(p != null)
				{
					MainServer.printInformation("The Player "+p.getNick()+" just reconnected");
					ps_sock.sendData(Protocol.CON_HASH.str()+s_PlayerID);
					p.reconnect(ps_sock);
					ps_sock.setPlayer(p);
				}
				else
				{
					ps_sock.sendData(Protocol.CON_ERROR.str() + "Unknown UUID, you're not allowed to reconnect");
					break;
				}
			}
			else
			{
				String uuid = UUID.randomUUID().toString();
				try
				{
					ps_sock.setPlayer(new Player(uuid, ps_sock, MainServer.getPlayerManager().reserveID()));
					ps_sock.sendData(Protocol.CON_HASH.str() + uuid);
				}
				catch(NoSuchElementException e)
				{
					ps_sock.sendData(Protocol.CON_ERROR.str() + "All seats taken - server is full!");
					ps_sock.sendData(Protocol.CON_EXIT.toString());
					ps_sock.close();
				}
			}
			break;
			
		case CON_PING://tested & works ~frank
			ps_sock.sendData(Protocol.CON_PONG.toString());
			break;
			
		case CON_NICK://tested & works ~frank
			handleNick(s_MSG, ps_sock);
			break;
			
		case CON_EXIT://tested & working ~frank
			ps_sock.close();
			break;
			
		case CON_MY_ID://tested & working ~frank
			ps_sock.sendData(Protocol.CON_MY_ID.str() + ps_sock.getPlayer().getID());
			break;
			
		case GAME_MAKE://tested & works ~frank
			handleMakeGame(s_MSG, ps_sock);
			break;
			
		case GAME_JOIN://tested & works ~frank
			handleGameJoin(s_MSG, ps_sock);			
			break;
			
		case GAME_QUIT://tested & works ~frank
			handleGameQuit(ps_sock);
			break;
			
		case CHAT_MESSAGE://tested & works ~Frank
			handleChat(s_MSG, ps_sock);
			break;
			
		default:
			Log.InformationLog("Received: \'"+s_MSG+"\'" );
			ps_sock.sendData(Protocol.CON_ERROR.str() + "not implemented yet");	
			break;
		}		
	}

	/**
	 * @param ps_sock
	 */
	private void handleGameQuit(PlayerSocket ps_sock) {
		if(ps_sock.getPlayer().getServer() != null)
		{
			ps_sock.getPlayer().getServer().removePlayer(ps_sock.getPlayer());
			ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "*now chatting in the lobby*");
			return;
		}
		ps_sock.sendData(Protocol.CON_ERROR.str() + "you can't leave a server you're not in");
	}

	/**
	 * @param s_MSG
	 * @param ps_sock
	 */
	private void handleGameJoin(String s_MSG, PlayerSocket ps_sock) {
		if(ps_sock.getPlayer().getServer() != null)
		{
			ps_sock.sendData(Protocol.CON_ERROR.str() + "ALready on a server!");
			return;
		}
		
		
		if(s_MSG.length() > 6)
		{
			s_MSG = s_MSG.substring(6, s_MSG.length());
			try
			{
				int id = Integer.parseInt(s_MSG);
				Server serv = MainServer.getServerManager().findServer(id);
				if(serv != null)
				{
					if(serv.getPlayerAmount() >= 5)
					{
						ps_sock.sendData(Protocol.CON_ERROR.str() + "this server is full!");
					}
					else
					{
						serv.addPlayer(ps_sock.getPlayer());
						ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "*now chatting in the server \'"+serv.getServername()+"\'*");
					}
					return;
				}
			}
			catch(NumberFormatException e)
			{
			}
		}
		
		ps_sock.sendData(Protocol.CON_ERROR.str() + "the specified server was not found");
	}

	/**
	 * @param s_MSG
	 * @param ps_sock
	 */
	private void handleMakeGame(String s_MSG, PlayerSocket ps_sock) {
		if(ps_sock.getPlayer().getServer() != null)
		{
			ps_sock.sendData(Protocol.CON_ERROR.str() + "already on a server, leave this one to create another one");
			return;
		}
		try
		{
			if(s_MSG.length() > 7)
			{
				s_MSG = s_MSG.substring(6, s_MSG.length());
				if(s_MSG.length() > 15)
					s_MSG = s_MSG.substring(0, 15);
			}
			else
			{
				s_MSG = "UnknownGame";
			}
			
			if(s_MSG.length() < 4)
				s_MSG = "UnknownGame";

			Server serv = new Server(s_MSG ,MainServer.getServerManager().reserveID());
			serv.addPlayer(ps_sock.getPlayer());
			ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "*now chatting in the server \'"+serv.getServername()+"\'*");
		}
		catch(NoSuchElementException e)
		{
			ps_sock.sendData(Protocol.CON_ERROR.str() + "Maximum amount of Servers reached, please join an existing one.");
		}
	}

	/**
	 * Handles incoming chat messages.
	 * @param s_MSG the message
	 * @param ps_sock the player socket
	 */
	private void handleChat(String s_MSG, PlayerSocket ps_sock) {
		if(s_MSG.length() < 7)
		{
			ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "[SERVER]\tYou're a quiet person, are you?");
			return;
		}
		s_MSG = s_MSG.substring(6);
		
		if (s_MSG.toUpperCase().startsWith(Protocol.CHAT_PREF_PRIVATE.toString()))
		{
			s_MSG = s_MSG.substring(5, s_MSG.length());
			//Split by one or more whitespaces
			String[] s = s_MSG.split("\\s+");
			Player p_player = MainServer.getPlayerManager().findPlayer(s[0]);
			if(p_player != null)
			{
				s_MSG = s_MSG.substring(s[0].length(), s_MSG.length());
				if(p_player == ps_sock.getPlayer())
				{
					ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "[SERVER]\tLast time I checked, you were sane... player.IsTalkingWithOneself = true;");
					return;
				}
				else
				{
					ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "[to "+p_player.getNick()+"]\t"+s_MSG);
					p_player.sendData(Protocol.CHAT_MESSAGE.str() + "[from "+ps_sock.getPlayer().getNick()+"]\t"+s_MSG);
					return;
				}
			}
			else
			{
				ps_sock.sendData(Protocol.CHAT_MESSAGE.str() + "[SERVER]\t Player \'"+s[0]+"\' isn't playing on this server...");
				return;
			}
		}
		else
		{
			MainServer.getPlayerManager().broadcastMessage(Protocol.CHAT_MESSAGE.str() + "<"+ps_sock.getPlayer().getNick()+">\t"+s_MSG,	ps_sock.getPlayer());
		}
	}

	/**
	 * Assigns a nick to a player.
	 * @param s_MSG the message
	 * @param ps_sock the player socket
	 */
	private void handleNick(String s_MSG, PlayerSocket ps_sock) {
		if(s_MSG.length() < 7)
		{
			s_MSG = "anon";
		}
		else
		{
			s_MSG = s_MSG.substring(6, s_MSG.length());
			//remove anything that isn't a-z or 0-9
			s_MSG = s_MSG.replaceAll("[^a-zA-Z0-9]", "");
			if(s_MSG.length() > 15)
				s_MSG = s_MSG.substring(0, 15);
		}
		
		if(s_MSG.length() < 4)
			s_MSG = "anon";
		
		// make sure no nicks are used twice
		Player p = MainServer.getPlayerManager().findPlayer(s_MSG);
		if(p != null)
		{
			int i = 0;
			while(p != null)
			{
				i++;
				p = MainServer.getPlayerManager().findPlayer(s_MSG+i);
			}
			s_MSG = s_MSG+i;
		}
		
		ps_sock.getPlayer().setNick(s_MSG);
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.CON_NICK.str()+ps_sock.getPlayer().getID()+" "+s_MSG);
	}

}

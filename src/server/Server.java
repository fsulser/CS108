package server;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import server.players.*;
import shared.Protocol;
public class Server 
implements Comparable<Server>
{
	private List<Player> l_players = new Vector<Player>();
	private List<Player> l_locked;
	private String s_servername;
	private int i_ServerID;
	
	public Server(String s_Servername, int i_ID)
	
	{
		// TODO implement the whole server a user can start when he's in the lobby
		s_servername = s_Servername;
		l_locked = Collections.unmodifiableList(l_players);
		i_ServerID = i_ID+200;
		MainServer.getServerManager().addServer(this);
		MainServer.printInformation("New server '"+s_servername+"' created");
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.GAME_BROADCAST.str()+this.getID()+" "+this.getPlayerAmount()+"  "+this.getServername());
	}
	
	
	@Override
	public int compareTo(Server o)
	{
		return o.i_ServerID - this.i_ServerID;
	}

	/**
	 * Returns all players on the current Server
	 * @return a list of players
	 */
	public List<Player> getPlayers()
	{
		return l_locked;
	}
	
	/**
	 * Add a Player to the Server.
	 * @param p_Player the player
	 */
	public void addPlayer(Player p_Player)
	{	
		p_Player.setServer(this);
		process(p_Player, true);
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.GAME_JOIN.str()+this.i_ServerID+" "+p_Player.getID()+" "+p_Player.getNick());
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.LOBBY_QUIT.str()+p_Player.getID());
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.GAME_BROADCAST.str()+this.getID()+" "+this.getPlayerAmount()+"  "+this.getServername());
	}
	
	/**
	 * Returns the amount of players connected to this Server/Game
	 * @return the amount of players
	 */
	public int getPlayerAmount()
	{
		return this.l_locked.size();
	}
	
	
	/**
	 * Manually remove a player on quit
	 * @param p_player the player to remove
	 */
	public void removePlayer(Player p_player)
	{
		p_player.setServer(null);
		process(p_player, false);
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.LOBBY_JOIN.str()+p_player.getID());
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.GAME_QUIT.str()+this.i_ServerID+" "+p_player.getID()+" "+p_player.getNick());
		if(this.getPlayerAmount() == 0)
		{
			MainServer.getServerManager().removeServer(this);
			MainServer.printInformation("Server '"+s_servername+"' closed");
		}			
		MainServer.getPlayerManager().broadcastMessage_everyone(Protocol.GAME_BROADCAST.str()+this.getID()+" "+this.getPlayerAmount()+"  "+this.getServername());
	}
	
	private synchronized void process(Player p_player, boolean b_add)
	{
		if(b_add)
		{
			this.l_players.add(p_player);
			MainServer.printInformation("added "+p_player.getNick()+" to "+this.s_servername);
		}
		else
		{
			Iterator<Player> i_players = this.l_players.iterator();
			while(i_players.hasNext())
			{
				Player p = i_players.next();
				if(p == p_player)
				{
					i_players.remove();
					//TODO close the server if it was the last guy and broadcast that
					MainServer.printInformation("Removed "+p.getNick()+" from "+this.s_servername);					
				}
			}
		}
	}
	
	/**
	 * Returns the server's name
	 * @return the server's name
	 */
	public String getServername()
	{
		return this.s_servername;
	}
	
	/**
	 * pauses the server. it's called when a player loses the connection
	 */
	public void pause()
	{
		//TODO implement
	}
	
	/**
	 * resumes the server. it is called when a player reconnects or times out
	 */
	public void resume()
	{
		//TODO implement
	}


	/**
	 * returns the serverID
	 * @return the serverID
	 */
	public int getID()
	{
		return this.i_ServerID;
	}
}

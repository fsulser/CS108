package server.players;

import java.util.*;
import shared.*;
import server.MainServer;


public class PlayerManager 
{
	private List<Player> l_players = new Vector<Player>();
	private List<Player> l_locked;
	private Queue<Integer> qi_AvailableIDs = new LinkedList<Integer>();
	
	public PlayerManager()
	{
		for(int i = 1; i != 100; i++)
		{
			qi_AvailableIDs.offer(i);
		}
		l_locked = Collections.unmodifiableList(l_players);
		Log.DebugLog("PLayerManager is up & running");
	}
	
	/**
	 * Broadcast a message to all players in the same lobby or server
	 * @param s_MSG the message
	 * @param p_player the initiator of the broadcast
	 */
	public void broadcastMessage(String s_MSG, Player p_player)
	{
		if(p_player.getServer() == null)
		{
			//send to the whole lobby
			for(Player p : l_players)
			{
				if(p.getServer() == null)				
				{
					p.sendData(s_MSG);				
				}			
			}	
		}
		else
		{
			for(Player p : p_player.getServer().getPlayers())
			{
				p.sendData(s_MSG);
			}
		}
			
			
	}
	
	/**
	 * checks whether a certain player is connected to the server and if so return the name
	 * <p>
	 * returns null if none was found
	 * @param s_name the pleayer's name you're looking for
	 * @return the player or null if no such player was found
	 */
	public Player findPlayer(String s_name)
	{
		for(Player p : this.l_players)
		{
			if(s_name.compareToIgnoreCase(p.getNick()) == 0)
				return p;
		}
		return null;
	}


	/**
	 * Returns a !!READ ONLY!! list of all players on the server
	 * @return the list of all connected players
	 */
	public List<Player> getPlayers()
	{
		return l_locked;
	}
	
	/**
	 * Add a Player to the PlayerManager. This is automatically done when you create a new Player!
	 * @param p_Player the player
	 */
	public void addPlayer(Player p_Player)
	{
		process(p_Player, true);
	}
	
	
	/**
	 * Manually remove a player on quit
	 * @param p_player the player to remove
	 */
	public void removePlayer(Player p_player)
	{
		process(p_player, false);
	}
	
	private synchronized void process(Player p_player, boolean b_add)
	{
		if(b_add)
		{
			this.l_players.add(p_player);
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
					p.disconnect();
					qi_AvailableIDs.offer(p.getID());
					MainServer.printInformation("Removed "+p.getNick()+" from the List of active Players");					
				}
			}
		}
	}
	
	/**
	 * checks whether a player with a certain UUID can be found. returns null if nothing was found.
	 * @param s_PlayerID the player UUID
	 * @return the corresponding player
	 */
	public Player findUUID(String s_PlayerID) 
	{
		for(Player p : this.l_players)
		{
			if(s_PlayerID.compareTo(p.getToken()) == 0)
				return p;
		}
		return null;
	}
	
	/**
	 * Returns a PlayerID that remains allocated until the player quits
	 * @return the playerID
	 * @throws NoSuchElementException if the server is full
	 */
	public int reserveID()
	throws NoSuchElementException
	{
		return this.qi_AvailableIDs.remove();
	}

	/**
	 * Broadcasts the message to everyone, no matter whether they're int he lobby or a server
	 * @param s_MSG the message to broadcast
	 */
	public void broadcastMessage_everyone(String s_MSG)
	{
		for(Player p : l_players)
		{
			p.sendData(s_MSG);
		}		
	}
}

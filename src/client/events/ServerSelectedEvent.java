package client.events;

import java.util.EventObject;

import shared.ServerAddress;

/**
 *  
 *  Event which is fired when a server is selected
 * @author oliver
 *
 */
public class ServerSelectedEvent extends EventObject 
{
	/**serial.*/
	private static final long serialVersionUID = 1L;
	/**holds the Address of the selected Server.*/
	private ServerAddress a;
	/**holds the desired Username.*/
	private String u;
	/**Constructs the Event.
	 * @param source Eventsource
	 * @param server Server which is selected.
	 * @param user desired Username.
	 * */
	public ServerSelectedEvent(final Object source, final ServerAddress server, final String user) 
	{
		super(source);
		this.a =  server;
		this.u = user;
	}
	/**Return the ServerAddress.
	 * @return a the selected Server
	 * */
	public final ServerAddress getServer()
	{
		return a;
	}
	/**return the desired Username.
	 * @return u the desired Username.
	 * */
	public final String getUsername()
	{
		return this.u;
	}
}

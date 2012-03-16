package server.parser;

import java.util.UUID;

import shared.Log;

public class Parser 
{
	
	/**
	 * Parses the given Message and sends the corresponding answers
	 * @param s_MSG the Message the Server socket received
	 * @return What the Socket should answer
	 */
	public String Parse(String s_MSG)
	{
		Log.InformationLog("Received: \'"+s_MSG+"\'" );
		
		switch(s_MSG.substring(0, 4).toUpperCase())
		{
		case "VAUTH":
			if(s_MSG.length()>6)
			{
				String s_PlayerID = s_MSG.substring(5, s_MSG.length()-1);
				//TODO reconnect the player
				return s_PlayerID;
			}
			else
			{
				//TODO store the UUID in the right place
				String uuid = UUID.randomUUID().toString();
				
				return uuid;
			}
		case "VPING":
			return "VPONG";
		case "CCHAT":
			return s_MSG;
		}
		
		return "not implemented yet";
	}

}

package shared;

@SuppressWarnings("serial")
public class MCSException 
extends Exception
{
	public MCSException()
	{
		super();
	}
	public MCSException(String s_ErrorMessage)
	{
		super(s_ErrorMessage);
	}
}

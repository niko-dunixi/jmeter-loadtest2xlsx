package application;

public class JMeterHandlerParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2418751016429637729L;

	public JMeterHandlerParseException(int errorCode) {
		super("JMeter could not output the graph. Error code: " + errorCode);
	}

}

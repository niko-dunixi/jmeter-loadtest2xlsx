package application;

public class JMeterHandlerSetupException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5152104484380287746L;

	public JMeterHandlerSetupException(String message) {
		super("JMeter could not properly setup it's environment. Error message: " + message);
	}
}

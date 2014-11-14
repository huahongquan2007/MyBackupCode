package robotbase.action.kobuki;

public enum KobukiCommand {
	FORWARD("FORWARD"),
	BACKWARD("BACKWARD"),
	LEFT("LEFT"),
	RIGHT("RIGHT");
	
	private String Command;
	
	private KobukiCommand(String command) {
		this.setCommand(command);
	}

	public String getCommand() {
		return Command;
	}

	public void setCommand(String command) {
		Command = command;
	}
}

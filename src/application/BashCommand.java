package application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

public class BashCommand extends Task<List<String>> {

	private String[] _command;
	
	public BashCommand(String[] command) {
		_command = command;
	}
	
	@Override
	protected List<String> call() throws Exception {
		return runBashCommand(_command);
	}

	// A static method used to pass bash commands into the shell and return the stdout
	public static List<String> runBashCommand(String[] command) {
		List<String> output = new ArrayList<String>();
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			Process process = builder.start();
			InputStream stdout = process.getInputStream();
			BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
			String line = null;
			while ((line = stdoutBuffered.readLine()) != null ) {
				output.add(line);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}

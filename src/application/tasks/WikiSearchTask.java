package application.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.concurrent.Task;

public class WikiSearchTask extends Task<String>{

	private String _searchTerm;
	
	public WikiSearchTask(String searchTerm) {
		_searchTerm = searchTerm;
	}
	
	@Override
	protected String call() throws Exception {
		String searchResult = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", "wikit " + _searchTerm});
			Process process = builder.start();
			
			InputStream stdout = process.getInputStream();
			BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
			
			String line = null;
			while ((line = stdoutBuffered.readLine()) != null ) {
				searchResult += line;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return searchResult.trim();
	}
}

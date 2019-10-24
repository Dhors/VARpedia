package application.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class CreateCreationTask extends Task<Void>{

	private ObservableList<String> _chunksList;
	private String _searchTerm;
	
	public CreateCreationTask(ObservableList<String> chunksListAsString, String searchTerm) {
		_chunksList = chunksListAsString;
		_searchTerm = searchTerm;
	}
	
	@Override
	protected Void call() throws Exception {
		try {
			// Convert the ObservableList of chunks into a single string, with each element separated by a space
			String chunksListAsString = "";
			int numChunksSelected = _chunksList.size();
			for (int i = 0; i < numChunksSelected; i++) {
				chunksListAsString += "chunks/" + _chunksList.get(i) + ".wav ";
			}
			// Last element should not have a space after it
			chunksListAsString = chunksListAsString.substring(0, chunksListAsString.length()-1);
			
			// Combines the chunks supplied in the args into a single .wav file
			String command = "sox " + chunksListAsString + " creations/" + _searchTerm + "/" + _searchTerm + ".wav";
			ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", command});
			Process process = builder.start();
			
			process.waitFor();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

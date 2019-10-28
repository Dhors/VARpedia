package application.tasks;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import javafx.concurrent.Task;

public class SaveTextTask extends Task<String> {
	private String _voiceChoice;
	private String _chunk;
	
	public SaveTextTask(String voiceChoice, String chunk) {
		_voiceChoice = voiceChoice;
		_chunk = chunk;
	}

	@Override
	protected String call() throws Exception {
		String creationName = null;
		try {
			String voice = null;
			if (_voiceChoice == "Default") {
				voice="kal_diphone";
			} else if (_voiceChoice == "NZ-Man") {
				voice="akl_nz_jdt_diphone";
			} else if (_voiceChoice == "NZ-Woman") {
				voice="akl_nz_cw_cg_cg";
			}
			
			int i = 1;
			File creationDir;

			String[] chunkAsWordArrary = _chunk.split("\\s+");
			
			// Find unique name for the chunk based on the first five words
			// Use less than five words if the chunk does not have five valid words
			// Keep increasing the suffix until there is no existing chunk with that name
			do {
				creationName = "";
				for (int j = 0; j < 5 && j < chunkAsWordArrary.length; j++) {
						creationName += chunkAsWordArrary[j] + "-";
				}
				creationName += i + ".wav";
				
				creationDir = new File(System.getProperty("user.dir")+"/chunks/"+creationName);
				
				i++;
			} while (creationDir.exists());
			
			String command = "text2wave -o chunks/" + creationName + " -eval '(voice_" + voice + ")'";
			ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", command});
			Process process = builder.start();
			
			OutputStream in = process.getOutputStream();
			PrintWriter stdin = new PrintWriter(in);
			stdin.println(_chunk);
			stdin.close();
			
			process.waitFor();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return creationName;
	}
}

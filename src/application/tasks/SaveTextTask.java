package application.tasks;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import javafx.concurrent.Task;

public class SaveTextTask extends Task<String> {
	private String _voiceChoice;
	private String _chunk;
	private String temp;
	public SaveTextTask(String voiceChoice, String chunk) {
		_voiceChoice = voiceChoice;
		_chunk = chunk;
	}

	@Override
	protected String call() throws Exception {
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
			String fileName;
			File fileDir;
			
			String[] array = _chunk.split("\\s+");
			
			// Find unique name for the chunk based on the first five words
			do {
				fileName = "";
				for (int j = 0; j < 5; j++) {
					fileName += array[j] + "-";
				}

				fileName += i + ".wav";
				
				fileDir = new File(System.getProperty("user.dir")+"/chunks/"+fileName);
				
				i++;
			} while (fileDir.exists());
			String command = "text2wave -o chunks/" + fileName + " -eval '(voice_" + voice + ")'";
			ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", command});
			Process process = builder.start();
			
			OutputStream in = process.getOutputStream();
			PrintWriter stdin = new PrintWriter(in);
			stdin.println(_chunk);
			stdin.close();
			
			process.waitFor();
			temp = fileName;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
}

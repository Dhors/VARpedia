package application.tasks;

import java.io.OutputStream;
import java.io.PrintWriter;

import javafx.concurrent.Task;

public class PreviewTextTask extends Task<Void> {
    private String _chunk;

    public PreviewTextTask(String chunk) {
        _chunk = chunk;
    }

    @Override
    protected Void call() throws Exception {
        try {
            ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", "festival --tts"});
            Process process = builder.start();

            OutputStream in = process.getOutputStream();
            PrintWriter stdin = new PrintWriter(in);
            stdin.println(_chunk);
            stdin.close();

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

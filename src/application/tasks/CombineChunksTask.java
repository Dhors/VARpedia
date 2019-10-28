package application.tasks;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class CombineChunksTask extends Task<Void> {

    private ObservableList<String> _chunksList;
    private String _searchTerm;

    public CombineChunksTask(ObservableList<String> chunksList, String searchTerm) {
        _chunksList = chunksList;
        _searchTerm = searchTerm;
    }

    @Override
    protected Void call() throws Exception {
        try {
            // Convert the ObservableList of chunks into a single string, with each element separated by a space
            String chunksListAsString = "";
            for (String chunk : _chunksList) {
                chunksListAsString += "chunks/" + chunk + ".wav ";
            }
            // Remove the space after the last chunk
            chunksListAsString = chunksListAsString.substring(0, chunksListAsString.length() - 1);

            // Combines the chunks supplied in the args into a single .wav file
            String newCreationDir = "creations/" + _searchTerm + "/" + _searchTerm + ".wav";
            String command = "sox " + chunksListAsString + " " + newCreationDir;
            ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", command});
            Process process = builder.start();

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

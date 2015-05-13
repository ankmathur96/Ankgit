import java.util.HashMap;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Commit implements Serializable {
    
    private static final long serialVersionUID = 1L;
    protected String commitMessage;
    protected String commitID;
    protected Commit parent;
    protected String dateCommitted;
    /* Maps fileNames to commit IDs. */
    protected HashMap<String, String> fileHistory;
    
    public Commit(String id, String userMessage, Commit givenParent, 
            HashMap<String, String> history) {
        commitID = id;
        commitMessage = userMessage;
        parent = givenParent;
        if (history == null) {
            fileHistory = new HashMap<String, String>();
        } else {
            fileHistory = history;
        }
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        dateCommitted = date.format(new Date());
    }
    
    /* Almost never will you want to inherit the parent of the old commit, 
     * so set it to null by default. */
    public Commit(Commit c) {
        commitMessage = c.commitMessage;
        parent = null;
        fileHistory = c.fileHistory;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        dateCommitted = date.format(new Date());
    }
    
    /**
     * @param fileName
     * @return true if commit contains this file.
     */
    public boolean containsFile(String fileName) {
        return fileHistory.containsKey(fileName);
    }
    
    /**
     * @param fileName
     * @return commitID of last commit where a file was edited.
     */
    public String getFileLastLocation(String fileName) {
        return fileHistory.get(fileName);
    }
}

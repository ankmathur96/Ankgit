import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class CommitTree implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String currentBranch;
    protected int lastcommitID;
    
    /* Maps commit IDs to Commit Objects */
    private HashMap<String, Commit> commitTree;
    /* Maps branch names to Commit Objects */
    private HashMap<String, Commit> branchHeads;
    /* Maps commit messages to commit IDs */
    private HashMap<String, ArrayList<String>> commitMessagesToID;
    
    public CommitTree() {
        commitTree = new HashMap<String, Commit>();
        commitTree.put("0", new Commit("0", "initial commit", null, null));
        branchHeads = new HashMap<String, Commit>();
        branchHeads.put("master", commitTree.get("0"));
        currentBranch = "master";
        lastcommitID = 0;
        commitMessagesToID = new HashMap<String, ArrayList<String>>();
        ArrayList<String> matchingIDs = new ArrayList<String>();
        matchingIDs.add("0");
        commitMessagesToID.put("initial commit", matchingIDs);
    }
    
    /**A series of utility methods to return access to private instance variables
     * From a design standpoint, it made sense to have these variables be private, since
     * the commitTree really doesn't need to expose how it works on the inside.
     */
    
    /**Commit management (method names explain functionality.) */
    public boolean containsCommit(String commitID) {
        return commitTree.containsKey(commitID);
    }
    
    /** Returns head commit of current branch */ 
    public Commit getHeadCommit() {
        return branchHeads.get(currentBranch);
    }
    
    /** Gets commit with ID string matching parameter. */
    public Commit getCommitWithID(String id) {
        if (!commitTree.containsKey(id)) {
            return null;
        } else {
            return commitTree.get(id);
        }
    }
    
    /** adds a commit by adding it to the tree. Some other bookkeeping for other data structures. */
    public void addCommit(Commit c) {
        commitTree.put(c.commitID, c);
        addCommitMessage(c.commitMessage, c.commitID);
        branchHeads.put(currentBranch, c);
    }
    
    /** Returns all commits in a collection form */
    public Collection<Commit> getAllCommits() {
        return commitTree.values();
    }
    
    /** Branch management methods */ 
        
    /** Check whether such a branch exists */
    public boolean containsBranch(String branchName) {
        if (branchHeads.containsKey(branchName)) {
            return true;
        }
        return false;
    }
    
    /** Adds a branch */
    public void addBranch(String branchName) {
        branchHeads.put(branchName, getHeadCommit());
    }
    
    /** Returns head commit of a branch. */
    public Commit getBranchHead(String branch) {
        return branchHeads.get(branch);
    }
    
    /** Returns name of current branch */
    public String getCurrentBranch() {
        return currentBranch;
    }

    /** Returns a Collection form of all branches. */
    public Collection<String> getAllBranches() {
        return branchHeads.keySet();
    }
    
    /** Sets current branch to some new branch name */
    public void setCurrentBranch(String currentBranch) {
        this.currentBranch = currentBranch;
    }
    
    /** Changes the head commit of a branch to the given commit */
    public void setBranchHead(String branch, String commitID) {
        if (commitTree.containsKey(commitID)) {
            branchHeads.put(branch, commitTree.get(commitID));
        }
    }
    
    /** Removes an added branch */
    public void removeBranch(String branchName) {
        branchHeads.remove(branchName);
    }
    
    /** commit Message to ID Map Utility */
    public boolean containsCommitMessage(String message) {
        return commitMessagesToID.containsKey(message);
    }
    
    /** Adds a commit message and updates list of IDs with that message */
    public void addCommitMessage(String message, String commitID) {
        if (!commitMessagesToID.containsKey(message)) {
            ArrayList<String> matchingIDs = new ArrayList<String>();
            commitMessagesToID.put(message, matchingIDs);
        } else {
            commitMessagesToID.get(message).add(commitID);
        }
    }
    
    /** Returns list of commit IDs that have a given commit message. */
    public ArrayList<String> getMatchingIDs(String message) {
        return commitMessagesToID.get(message);
    }
    
    /**
     * Locates the split points between two branches by walking backwards along both branches
     * and putting all the commit IDs that are covered in a HashSet. The first time that a 
     * commit ID is shared is the earliest ancestor (a.k.a the split point).
     * @param branchName1
     * @param branchName2
     * @return Commit object that is the split point between these two branches.
     */
    
    public Commit findSplitPoint(String branchName1, String branchName2) {
        HashSet<String> history1 = new HashSet<String>();
        HashSet<String> history2 = new HashSet<String>();
        Commit c1 = getBranchHead(branchName1);
        Commit c2 = getBranchHead(branchName2);
        boolean end = false;
        while (!end) {
            if (c1.commitID.equals(c2.commitID)) {
                return c1;
            }
            if (history2.contains(c1.commitID)) {
                return c1;
            } else if (history1.contains(c2.commitID)) {
                return c2;
            } else {
                history1.add(c1.commitID);
                history2.add(c2.commitID);
                if (c1.parent == null && c2.parent == null) {
                    end = true;
                }
                if (c1.parent != null) {
                    c1 = c1.parent;
                }
                if (c2.parent != null) {
                    c2 = c2.parent;
                }
            }
        }
        return null;
    }
}

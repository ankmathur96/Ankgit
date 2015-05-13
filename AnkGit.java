import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AnkGit {
    /**
     * 
     */
    private CommitTree commitTree;
    private final String ADDED_PATH = ".ankgit/currentlyAdded.ser";
    private final String REMOVE_PATH = ".ankgit/toRemove.ser";
    private final String COMMIT_HISTORY = ".ankgit/commitHistory/";
    private static final String COMMIT_TREE_PATH = ".ankgit/commitTree.ser";
    
    public AnkGit() {
        commitTree = null;
    }
    /** FILE IO */
    /**
     * Takes a path and an object as parameters and writes the 
     * object to the path.
     * @param path
     * @param o
     */
    private static void writeObject(String path, Object o) {
        try {
            FileOutputStream fOut = new FileOutputStream(getAbsolutePath(path), false);
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            oOut.writeObject(o);
            fOut.close();
            oOut.close();
        } catch (IOException e) {
            System.out.println("File write failure when trying to write: " + path);
        }
    }
    
    /**
     * Takes the path as a parameter, reads object from serialized 
     * file, and returns it.
     * @param path
     * @return serialized Object
     */
    private static Object readObject(String path) {
        Object o = null;
        try {
            FileInputStream fIn = new FileInputStream(getAbsolutePath(path));
            ObjectInputStream oIn = new ObjectInputStream(fIn);
            o = oIn.readObject();
            oIn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("File input failure when trying to read: " + path);
        } catch (IOException ioe) {
            System.out.println("File input failure when trying to read: " + path);
        }
        return o;
    }
    
    /**
     * Creates a new AnkGit version control system in the current directory. 
     * This system starts with one empty commit (a commit that contains no 
     * files and has the commit message initial commit.) 
     * If there is already a AnkGit version control system
     * in the current directory, it will abort.
     */
    private void initializeDirectory() {
        File gitLetInit = new File(getAbsolutePath(".ankgit/"));
        if (!gitLetInit.exists()) {
            gitLetInit.mkdir();
            CommitTree gitletTree = new CommitTree();
            this.commitTree = gitletTree;
        } else {
            System.out.println("A AnkGit version control system already "
                    + "exists in the current directory.");
        }
    }
    
    /**
     * Reads a Serializable ArrayList from the given String path. 
     * Useful extension of readObject.
     * @param path
     * @return Arraylist read in from serialized object.
     */
    private static ArrayList<String> getArrayListFile(String path) {
        File currentlyAdded = new File(getAbsolutePath(path));
        ArrayList<String> staged = null;
        if (!currentlyAdded.exists()) {
            staged = new ArrayList<String>();
            writeObject(path, staged);
        } else {
            staged = (ArrayList<String>) readObject(path);
        }
        return staged;
    }
    
    private static String getAbsolutePath(String fileName) {
        return System.getProperty("user.dir") + "/" + fileName;
    }
    
    /**
     * Checks whether a file exists. If it does and it does not 
     * exist in the latest commit, then it is added to a list of files 
     * to be added to the next commit. If it exists but also exists in the
     * latest commit, then the version of the file from the latest commit 
     * and the working directory version are bytewise compared and 
     * if there's a difference, the file is added.
     * @param fileName
     * @throws IOException
     */
    private void addFile(String fileName) throws IOException {
        File added = new File(getAbsolutePath(fileName));
        if (!added.exists()) {
            System.out.println("File does not exist.");
            return;
        } else if (commitTree.getHeadCommit().containsFile(fileName)) {
            String oldID = commitTree.getHeadCommit().getFileLastLocation(fileName);
            Path curPath = Paths.get(getAbsolutePath(fileName));
            byte[] newData = Files.readAllBytes(curPath);
            Path oldPath = Paths.get(COMMIT_HISTORY + oldID + "/" + fileName);
            byte[] oldData = Files.readAllBytes(oldPath);
            if (Arrays.equals(oldData, newData)) {
                System.out.println("File has not been modified since the last commit.");
                return;
            }
        }
        ArrayList<String> staged = getArrayListFile(ADDED_PATH);
        staged.add(fileName);
        writeObject(ADDED_PATH, staged);
    }
    
    /**
     * Checks if a file is staged, and if it is, it removes 
     * it from the staged list. Otherwise, checks to see whether
     * the latest commit has the file, and, if it does,
     * the file is marked for removal in the next commit.
     * @param fileName
     */
    private void remove(String fileName) {
        ArrayList<String> staged = getArrayListFile(ADDED_PATH);
        ArrayList<String> removalStage = getArrayListFile(REMOVE_PATH);
        if (staged.contains(fileName)) {
            staged.remove(fileName);
            removalStage.add(fileName);
        } else if (commitTree.getHeadCommit().containsFile(fileName)) {
            removalStage.add(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }
        writeObject(ADDED_PATH, staged);
        writeObject(REMOVE_PATH, removalStage);
    }
    
    /**
     * Helper method for makeCommit. Identifies the folder paths 
     * inside file names to create directories.
     * @param s
     * @return String of the file name with only the additional folder path. 
     * ("test/test1/t.txt" -> "test/test1/")
     */
    private String extractFolderPath(String s) {
        int n = s.length() - 1;
        for (int i = n; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == '/') {
                return s.substring(0, i + 1);    
            }
        }
        return "";
    }
    
    /**
     * Creates a new commit, adding in the working directory's versions  
     * of the added files.Removes the files staged for removal from the 
     * inherited file list of the commit. Versions of these added  
     * files are put into ".ankgit/commitHistory/COMMITID/*" Lots of file 
     * system bookkeeping to ensure that folders work.
     * @param message
     */
    private void makeCommit(String message) {
        if (message == null || message.equals("") || message.equals(" ")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        ArrayList<String> staged = getArrayListFile(ADDED_PATH);
        ArrayList<String> removalStage = getArrayListFile(REMOVE_PATH);
        if (staged.size() == 0 && removalStage.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        commitTree.lastcommitID += 1;
        String commitID = Integer.toString(commitTree.lastcommitID);
        HashMap<String, String> contained = new HashMap<String, String>();
        contained.putAll(commitTree.getHeadCommit().fileHistory);
        for (String s: staged) {
            contained.put(s, commitID);
        }
        for (String s: removalStage) {
            contained.remove(s);
        }
        new File(getAbsolutePath(COMMIT_HISTORY + commitID + "/")).mkdirs();
        for (String fileName : staged) {
            String deepPath = extractFolderPath(fileName);
            if (!deepPath.equals("")) {
                new File(getAbsolutePath(COMMIT_HISTORY + commitID + "/" + deepPath)).mkdirs();
            }
            Path current = Paths.get(getAbsolutePath(fileName));
            Path newCommit = Paths.get(getAbsolutePath(COMMIT_HISTORY + commitID + "/" + fileName));
            try {
                Files.copy(current, newCommit);
            } catch (IOException e) {
                System.out.println(current.toString() + " " + newCommit.toString());
            }
        }
        staged.clear();
        removalStage.clear();
        writeObject(ADDED_PATH, staged);
        writeObject(REMOVE_PATH, removalStage);
        Commit newCommit = new Commit(commitID, message, commitTree.getHeadCommit(), contained);
        commitTree.addCommit(newCommit);
    }
    
    /**
     * Starting at the current head pointer, displays information about each commit backwards 
     * along the commit tree until the initial commit.
     * For every node in this history, the information it should display is the commit id, the 
     * time the commit was made, and the commit message. 
     * @return String containing normal log.
     */
    private String log() {
        Commit c = commitTree.getHeadCommit();
        StringBuilder log = new StringBuilder();
        while (c != null) {
            log.append("====\nCommit ");
            log.append(c.commitID + ".\n");
            log.append(c.dateCommitted + "\n");
            log.append(c.commitMessage + "\n\n");
            c = c.parent;
        }
        return log.toString();
    }
    
    /**
     * Same functionality as log, but for all commits throughout the history 
     * of the version control system.
     * @return String containing global log.
     */
    private String globalLog() {
        StringBuilder log = new StringBuilder();
        for (Commit c : commitTree.getAllCommits()) {
            log.append("====\nCommit ");
            log.append(c.commitID + ".\n");
            log.append(c.dateCommitted + "\n");
            log.append(c.commitMessage + "\n\n");
            c = c.parent;
        }
        return log.toString();
    }
    
    /**
     * Displays the branches in the version control system, along with files that are 
     * currently staged or marked for removal.
     * @return String containing status.
     */
    private String status() {
        StringBuilder status = new StringBuilder();
        status.append("=== Branches ===\n");
        for (String branchName : commitTree.getAllBranches()) {
            if (branchName.equals(commitTree.getCurrentBranch())) {
                status.append("*" + branchName + "\n");
            } else {
                status.append(branchName + "\n");
            }
        }
        status.append("=== Staged Files ===\n");
        ArrayList<String> added = getArrayListFile(ADDED_PATH);
        for (String fileName : added) {
            status.append(fileName + "\n");
        }
        status.append("=== Files Marked for Removal ===\n");
        ArrayList<String> removed = getArrayListFile(REMOVE_PATH);
        for (String fileName : removed) {
            status.append(fileName + "\n");
        }
        status.deleteCharAt(status.length() - 1);
        return status.toString();
    }

    /**
     * Prints all commit IDs that have this commit Message.
     * @param commitMessage
     * @return String containing all the IDs.
     */
    private void find(String commitMessage) {
        if (commitTree.containsCommitMessage(commitMessage)) {
            for (String id : commitTree.getMatchingIDs(commitMessage)) {
                System.out.println(id);
            }
        } else {
            System.out.println("Found no commit with that message.");
        }
        
    }
    
    /**
     * Case 1: checking out a branch - for all files in a given branch, copies the versions 
     * from the given branch's head into the working directory.
     * Case 2: checking out a file - checks whether the file exists in the head commit. 
     * If it does, copies version from that commit into working directory
     * @param fileOrBranch
     */
    private void checkout(String fileOrBranch) {
        if (commitTree.containsBranch(fileOrBranch)) {
            if (commitTree.getCurrentBranch().equals(fileOrBranch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            Commit branchHead = commitTree.getBranchHead(fileOrBranch);
            for (String fileName : branchHead.fileHistory.keySet()) {
                String commitID = branchHead.fileHistory.get(fileName);
                checkout(commitID, fileName);
            }
            commitTree.setCurrentBranch(fileOrBranch);
        } else if (commitTree.getHeadCommit().containsFile(fileOrBranch)) {
            checkout(commitTree.getHeadCommit().commitID, fileOrBranch);
        } else {
            System.out.println("File does not exist in the most recent commit"
                      + ", or no such branch exists.");
        }
    }
    
    /**
     * Checks whether the file exists in the given commit. If it does, copies version from that 
     * commit into working directory.
     * @param commitID
     * @param fileName
     */
    private void checkout(String commitID, String fileName) {
        Commit c = commitTree.getCommitWithID(commitID);
        if (c == null) {
            System.out.println("No commit with that id exists.");
        } else if (!c.containsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
        } else {
            Path source = Paths.get(getAbsolutePath(".ankgit/commitHistory/"  
                         + c.fileHistory.get(fileName) + "/" + fileName));
            Path target = Paths.get(getAbsolutePath(fileName));
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Copy error");
            }
        }
        
    }
    
    /**
     * Creates a new branch with the given branchName. This involves the creation of a 
     * new Head pointer and updates in the commitTree, which are abstracted
     * away to that class.
     * @param branchName
     */
    private void branch(String branchName) {
        if (commitTree.containsBranch(branchName)) {
            System.out.println("A branch with that name already exists.");
        }
        commitTree.addBranch(branchName);
    }
    
    /**
     * Removes a branch with the given branchName, unless it doesn't exist or is the current branch.
     * @param branchName
     */
    private void rmBranch(String branchName) {
        if (!commitTree.containsBranch(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchName.equals(commitTree.getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            commitTree.removeBranch(branchName);
        }
    }
    
    /**
     * Restores all files to their versions in the commit with the given id. Also moves the current 
     * branch's head to that commit node.
     * @param resetID
     */
    private void reset(String resetID) {
        if (commitTree.containsCommit(resetID)) {
            Commit resetC = commitTree.getCommitWithID(resetID);
            for (String fileName : resetC.fileHistory.keySet()) {
                checkout(resetID, fileName);
            }
            commitTree.setBranchHead(commitTree.getCurrentBranch(), resetID);
        } else {
            System.out.println("No commit with that id exists.");
        }
    }
    
    /**
     * Merges files from the head of the given branch into the head of the current branch. 
     * 1. Files that are modified in the given branch but not in the current branch since 
     * the split point are changed to their versions in the given branch.
     * 2. files that have been modified in both branches since the split point should stay 
     * as they are in the current branch. However, a conflicted copy from the given branch
     * is added to this commit.
     * @param branchName
     */
    private void merge(String branchName) {
        if (branchName.equals(commitTree.getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
        } else if (!commitTree.containsBranch(branchName)) {
            System.out.println("A branch with that name does not exist.");
        }
        Commit otherHead = commitTree.getBranchHead(branchName);
        Commit thisHead = commitTree.getHeadCommit();
        Commit splitPoint = commitTree.findSplitPoint(commitTree.getCurrentBranch(), branchName);
        int splitID = Integer.parseInt(splitPoint.commitID);
        for (String file : otherHead.fileHistory.keySet()) {
            int cID = Integer.parseInt(otherHead.fileHistory.get(file));
            if (cID > splitID) {
                Path source = Paths.get(".ankgit/commitHistory/" 
                      + otherHead.commitID + "/" + file);
                Path target;
                if (thisHead.fileHistory.keySet().contains(file) 
                          && Integer.parseInt(thisHead.fileHistory.get(file)) > splitID) {
                    target = Paths.get(file + ".conflicted");
                } else {
                    target = Paths.get(file);
                }
                try {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /** Propogates changes from one head to another for usage with */
    private void propogateChanges(Commit changeFrom, Commit changeTo, String splitPoint) {
        for (String fileName : changeTo.fileHistory.keySet()) {
            String lastEditedID = commitTree.getHeadCommit().fileHistory.get(fileName);
            if (Integer.parseInt(lastEditedID) <= Integer.parseInt(splitPoint)) {
                if (changeFrom.containsFile(fileName)) {
                    String lastEdited = changeFrom.fileHistory.get(fileName);
                    if (Integer.parseInt(lastEdited) > Integer.parseInt(splitPoint)) {
                        commitTree.getHeadCommit().fileHistory.put(fileName, lastEdited);
                        checkout(lastEdited, fileName);
                    }
                }
            }
        }
    }
    
    /**
     * Rebase finds the split point, and then makes defensive copies of the current branch
     * and then attaches it to the end of the given branch, while propogating updated copies
     * of files from the given branch forward.
     * @param branchName
     */
    private void reBase(String branchName) {
        if (!commitTree.containsBranch(branchName)) { 
            System.out.println("A branch with that name does not exist");
            return;
        } else if (commitTree.getCurrentBranch().equals(branchName)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        } else {
            Commit split = commitTree.findSplitPoint(commitTree.getCurrentBranch(), branchName);
            Commit otherHead = commitTree.getBranchHead(branchName);
            if (split.commitID.equals(otherHead.commitID)) {
                System.out.println("Already up-to-date.");
                return;
            } else if (split.commitID.equals(commitTree.getHeadCommit().commitID)) {
                commitTree.setBranchHead(commitTree.getCurrentBranch(), otherHead.commitID);
                return;
            }
            /* Create a copy of the branch with new commit IDs. */
            Commit oldHeadC = commitTree.getHeadCommit();
            Commit replayedC = new Commit(oldHeadC);
            ArrayList<Commit> replayed = new ArrayList<Commit>();
            replayed.add(0, replayedC);
            while (oldHeadC.parent != null && !oldHeadC.parent.commitID.equals(split.commitID)) {
                Commit prev = replayedC;
                oldHeadC = oldHeadC.parent;
                replayedC = new Commit(oldHeadC);
                replayed.add(0, replayedC);
                prev.parent = replayedC;
            }
            for (Commit c : replayed) {
                commitTree.lastcommitID += 1;
                c.commitID = Integer.toString(commitTree.lastcommitID);
                propogateChanges(otherHead, c, split.commitID);
                commitTree.addCommit(c);
            }
            String headID = replayed.get(replayed.size() - 1).commitID;
            commitTree.setBranchHead(commitTree.getCurrentBranch(), headID);
            replayedC.parent = otherHead;
        }
    }

    /**
     * Rebase, except users are given the option to edit the contents of the 
     * commits (message name through flag 'm', skip a node through flag 's').
     * @param branchName
     * @param in
     */
    private void iReBase(String branchName, Scanner in) {
        reBase(branchName);
        Commit current = commitTree.getHeadCommit();
        Commit last = commitTree.getBranchHead(branchName);
        ArrayList<Commit> replayed = new ArrayList<Commit>();
        
        while (current != null && !current.commitID.equals(last.commitID)) {
            replayed.add(0, current);
            current = current.parent;
        }
        boolean end = false;
        int count = 0;
        while (!end) {
            Commit c = replayed.get(count);
            System.out.println("Currently replaying:");
            System.out.println("\nCommit " + c.commitID + ".\n");
            System.out.println(c.dateCommitted + "\n" + c.commitMessage + "\n");
            String response = interActiveIO(in, "Would you like to (c)ontinue, (s)kip "
                    + "this commit, or change this commit's (m)essage?");
            if (response.equals("s")) {
                if (count == 0 || count == replayed.size() - 1) {
                    continue;
                } else {
                    replayed.get(count + 1).parent = replayed.get(count - 1);
                }
            } else if (response.equals("m")) {
                c.commitMessage = interActiveIO(in, "Please enter a new message for this commit.");
            }
            count += 1;
            if (count >= replayed.size()) {
                end = true;
            }
        }
    }
    
    /**USER IO */
    
    /**
     * Prints out a message and gets response.
     * @param message
     * @return
     */
    private static String interActiveIO(Scanner in, String message) {
        System.out.println(message);
        String response = in.nextLine();
        return response;
    }
    
    /**
     * Special case of interactive IO that has yes/no logic embedded to prevent 
     * reusing the same if several times. Prompts the user that a command may be dangerous.
     * @return true if user confirms yes.
     */
    private static boolean dangerousPromptResponse(Scanner in) {
        String yn = interActiveIO(in, "Warning: The command you entered may alter the files "
                + "in your working directory. Uncommitted changes may be lost. "
                + "Are you sure you want to continue? (yes/no)");
        if (yn.equals("yes")) {
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
        AnkGit git = new AnkGit();
        if (new File(getAbsolutePath(".ankgit/")).exists()) {
            git.commitTree = (CommitTree) AnkGit.readObject(COMMIT_TREE_PATH);
        }
        Scanner in = new Scanner(System.in);
        if (args.length >= 1) {
            try {
                switch (args[0]) {
                    case "init": 
                        git.initializeDirectory();
                        break;
                    case "add":
                        git.addFile(args[1]);
                        break;
                    case "commit":
                        git.makeCommit(args[1]);
                        break;
                    case "rm":
                        git.remove(args[1]);
                        break;
                    case "log":
                        System.out.println(git.log());
                        break;
                    case "global-log": 
                        System.out.println(git.globalLog());
                        break;
                    case "status" : 
                        System.out.println(git.status());
                        break;
                    case "branch": 
                        git.branch(args[1]);
                        break;
                    case "rm-branch": 
                        git.rmBranch(args[1]);
                        break;
                    case "find": 
                        git.find(args[1]);
                        break;
                    case "checkout":
                        if (dangerousPromptResponse(in)) {
                            if (args.length > 2) {
                                git.checkout(args[1], args[2]);
                            } else {
                                git.checkout(args[1]);
                            }
                        }
                        break;
                    case "merge":
                        if (dangerousPromptResponse(in)) {
                            git.merge(args[1]);
                        }
                        break;
                    case "reset":
                        if (dangerousPromptResponse(in)) {
                            git.reset(args[1]);
                        }
                        break;
                    case "rebase":
                        if (dangerousPromptResponse(in)) {
                            git.reBase(args[1]);
                        }
                        break;
                    case "i-rebase":
                        if (dangerousPromptResponse(in)) {
                            git.iReBase(args[1], in);
                        }
                        break;
                    default:
                        System.out.println("Please enter a valid command.");
                }
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Please enter a valid command.");
            }
        } else {
            System.out.println("Please enter a command.");
        }
        in.close();
        writeObject(COMMIT_TREE_PATH, git.commitTree);
    }
}

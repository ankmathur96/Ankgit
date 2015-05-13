AnkGit
======
Fully functioning version control system with all of Git's local commands (remote coming soon!)

Supported Commands
------------------

<b>1. add </b>

Usage: `java Ankgit add [file name]`

Indicates you want the file to be included in the upcoming commit as having been changed. Same as staging the file If the file had been marked for removal, instead just unmark it

<b> 2. commit </b>

Usage: `java Ankgit commit [message]`

Saves a snapshot of certain files that can be viewed or restored at a later time. The files in a commit's snapshot come from two sources: files that were newly added to this commit (staged prior to the commit), and files that were inherited from the previous commit.

<b> 3. rm </b>

Usage: `java Ankgit rm [file name]`

Mark the file for removal; this means it will not be inherited as an old file in the next commit. If the file had been staged, instead unstage it.

<b> 4. log</b>

Usage: `java Ankgit log`

Starting at the current head pointer, display information about each commit backwards along the commit tree until the initial commit. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.

<b>5. global-log</b>

Usage: `java Ankgit global-log`

Like log, except displays information about all commits ever made.

<b> 6. status</b>

Usage: `java Ankgit status`

Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged or marked for removal. 

<b> 7. branch </b>

Usage: `java Ankgit branch [branch name]`

Creates a new branch with the given name. Note: Does NOT immediately switch to the newly created branch.

<b> 8. rm-branch </b>

Usage: `java Ankgit rm-branch [branch-name]`

Deletes the branch with the given name.

<b> 9. find </b>

Usage: `java Ankgit find [commit message]`

Prints out the id of the commit that has the given commit message. If there are multiple such commits, it prints the ids out on separate lines.

<b> 10. checkout </b>

Usage: `java Ankgit checkout [file name]` - Restores the given file in the working directory to its state at the commit at the head of the current branch.

`java Ankgit checkout [commit id] [file name]` - Restores the given file in the working directory to its state at the given commit.

`java Ankgit checkout [branch name]` - Restores all files in the working directory to their versions in the commit at the head of the given branch. Considers the given branch to now be the current branch.

<b> 11. merge </b>

Usage: `java Ankgit merge [branch name]`

Merges files from the head of the given branch into the head of the current branch. 

<b> 12. reset </b>

Usage: `java Ankgit reset [commit id]`

Restores all files to their versions in the commit with the given id. Also moves the current branch's head to that commit node.

<b> 13. rebase </b>

Usage: `java Ankgit rebase [branch name]`

What rebase does is find the split point of the current branch and the given branch, then snaps off the current branch at this point and reattaches the current branch to the head of the given branch. 

<b> 14. i-rebase </b>

Usage: `java Ankgit i-rebase [branch name]`

Same as normal rebasing, except interative. For each node it replays, it allows the user to change the commit’s message or skip replaying the commit. This means the command needs to pause and prompt the user for text input before continuing with each commit.

Example Usage
----------------------------------------------------------------------
(assuming all files have already been compiled, from the directory where the class files exist, these commands can be executed)

``` bash
java Ankgit init

echo "**information of critical importance**" > test.txt

java Ankgit add test.txt

java Ankgit commit "initial commit"

rm test.txt

// oops, I didn't want that!

java Ankgit checkout 1 test.txt

cat test.txt

**information of critical importance**

// phew.
```

package mapreduce;

public class Block extends MRData {
    private int userID;

    public Block(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }
}

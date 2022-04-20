package mapreduce;

public class Block extends MRData {
    private int userID;
    private int blockID;

    public Block(int userID, int blockID) {
        this.userID = userID;
        this.blockID = blockID;
    }

    public int getUserID() {
        return userID;
    }

    public int getBlockID() { return blockID; }
}

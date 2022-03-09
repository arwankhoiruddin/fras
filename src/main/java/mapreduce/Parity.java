package mapreduce;

public class Parity extends MRData {
    private int userID;

    public Parity(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }
}

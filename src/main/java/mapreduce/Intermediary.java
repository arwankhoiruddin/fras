package mapreduce;

public class Intermediary extends MRData {
    private int userID;
    private double size;

    public Intermediary(int userID, double size) {
        this.userID = userID;
        this.size = size;
    }

    public int getUserID() {
        return this.userID;
    }

    public double getSize() {
        return this.size;
    }
}

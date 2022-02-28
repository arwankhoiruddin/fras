package cluster;

import java.util.LinkedList;

public class Disk {
    private SataType sataType;
    private double diskSpace;
    private LinkedList<Block> blocks;
    private LinkedList<Parity> parities;

    public Disk(SataType sataType, double diskSpace) {
        this.sataType = sataType;
        this.diskSpace = diskSpace;
        blocks = new LinkedList<Block>();
        parities = new LinkedList<Parity>();
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public void addParity(Parity parity) {
        parities.add(parity);
    }

    public void removeBlock(Block block) {
        blocks.remove(block);
    }

    public void removeParity(Parity parity) {
        parities.remove(parity);
    }

    public LinkedList<Block> getBlocks() {
        return blocks;
    }

    public LinkedList<Parity> getParity() {
        return parities;
    }

    public double getDiskSpace() {
        return diskSpace;
    }

    public SataType getSataType() {
        return sataType;
    }

    public void setDiskSpace(double diskSpace) {
        this.diskSpace = diskSpace;
    }

    public double getDiskSpeed() {
        double speed = 0;
        switch (sataType) {
            case SATA1 -> speed = 150;
            case SATA2 -> speed = 300;
            case SATA3 -> speed = 600;
        }
        return speed;
    }
}

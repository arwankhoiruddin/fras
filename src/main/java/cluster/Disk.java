package cluster;

import common.MRConfigs;
import mapreduce.Block;
import mapreduce.Intermediary;
import mapreduce.MRData;
import mapreduce.Parity;

import java.util.LinkedList;

public class Disk {
    private SataType sataType;
    private double diskSpace; // disk in MB
    private LinkedList<MRData> data;

    /*
    diskSpace in init is in GB
    diskSpace in this class is in MB
     */
    public Disk(SataType sataType, double diskSpace) {
        this.sataType = sataType;
        this.diskSpace = diskSpace * 1024;
        data = new LinkedList<MRData>();
    }

    public void addBlock(Block block) {
        // add block into disk data
        data.add(block);
        // adjust the disk space
        this.diskSpace -= MRConfigs.blockSize;
    }

    public void addParity(Parity parity) {
        // add parity into disk data
        data.add(parity);
        // adjust the disk space
        this.diskSpace -= (MRConfigs.blockSize / 2);
    }

    public void removeBlock(Block block) {
        if (data.contains(block)) {
            data.remove(block);
        }
    }

    public void removeParity(Parity parity) {
        if (data.contains(parity))
            data.remove(parity);
    }

    public LinkedList<Block> getBlocks() {
        LinkedList<Block> blocks = new LinkedList<>();

        for (int i=0; i<data.size(); i++) {
            if (data.get(i) instanceof Block)
                blocks.add((Block) data.get(i));
        }
        return blocks;
    }

    public LinkedList<Parity> getParity() {
        LinkedList<Parity> parities = new LinkedList<>();

        for (int i=0; i<data.size(); i++) {
            if (data.get(i) instanceof Parity)
                parities.add((Parity) data.get(i));
        }
        return parities;
    }

    public LinkedList<Intermediary> getIntermediary() {
        LinkedList<Intermediary> intermediaries = new LinkedList<>();

        for (int i=0; i<data.size(); i++) {
            if (data.get(i) instanceof Intermediary)
                intermediaries.add((Intermediary) data.get(i));
        }
        return intermediaries;
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
            case SATA1: speed = 150; break;
            case SATA2: speed = 300; break;
            case SATA3: speed = 600; break;
        }
        return speed;
    }
}

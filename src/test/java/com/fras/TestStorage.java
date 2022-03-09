package com.fras;

import cluster.Cluster;
import cluster.Disk;
import cluster.SataType;
import mapreduce.Block;
import mapreduce.Parity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class TestStorage {

    @BeforeEach
    public void setUp() {
        Cluster cluster = new Cluster();
    }

    @Test
    public void testAddBlock() {
        Block block = new Block(1);
        Cluster.nodes[0].addBlock(block);
        assert Cluster.nodes[0].getDisk().getBlocks().getFirst().getUserID() == 1;
    }

    @Test
    public void testDiskInit() {
        Disk disk = new Disk(SataType.SATA1, 100);
        assert disk.getDiskSpeed() == 150;
        assert disk.getDiskSpace() == 102400;
    }

    @Test
    public void testGetParities() {
        Block block1 = new Block(1);
        Block block2 = new Block(2);
        Parity parity = new Parity(2);
        Disk disk = new Disk(SataType.SATA1, 10);
        disk.addBlock(block1);
        disk.addBlock(block2);
        disk.addParity(parity);

        // get all the blocks
        LinkedList<Parity> parities = disk.getParity();
        assert parities.size() == 1;

    }

    @Test
    public void testGetBlocks() {
        Block block1 = new Block(1);
        Block block2 = new Block(2);
        Parity parity = new Parity(2);
        Disk disk = new Disk(SataType.SATA1, 10);
        disk.addBlock(block1);
        disk.addBlock(block2);
        disk.addParity(parity);

        // get all the blocks
        LinkedList<Block> blocks = disk.getBlocks();
        assert blocks.size() == 2;
    }

    @Test
    public void testSpaceAfterBlockAdded() {
        Block block = new Block(1);
        Disk disk = new Disk(SataType.SATA1, 1);
        disk.addBlock(block);
        assert disk.getDiskSpace() == 960; // 1024 - 64 (block size)
    }
}

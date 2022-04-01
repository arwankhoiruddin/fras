package cluster;

import java.util.LinkedList;

public class Switch {
    private Link link;
    private int switchID;

    public LinkedList<Node> nodes = new LinkedList<>();
    public Switch parentSwitch;
    public LinkedList<Switch> switches = new LinkedList<>();

    public Switch(int switchID, LinkType linkType) {
        this.switchID = switchID;
        this.link = new Link(linkType);
    }

    public int getSwitchID() {
        return this.switchID;
    }

    public void connectNode(Node node) {
        this.nodes.add(node);
    }

    public void connectSwitch(Switch aSwitch) {
        this.switches.add(aSwitch);
    }

    public void connectParentSwitch(Switch parentSwitch) {
        this.parentSwitch = parentSwitch;
        parentSwitch.connectSwitch(this);
    }

    public boolean isConnected(Node node) {
        if (nodes.contains(node))
            return true;
        else
            return false;
    }

    public double getLinkSpeed() {
        return this.link.getLinkSpeed();
    }
}

package cluster;

public class Link {
    private LinkType linkType;

    public Link(LinkType linkType) {
        this.linkType = linkType;
    }

    public double getLinkSpeed() {
        double linkSpeed = 0;
        switch (linkType) {
            case GIGABIT: linkSpeed = 1000; break;
            case TENGIGABIT: linkSpeed = 10000; break;
            case TWENTYFIVEGIGABIT: linkSpeed = 25000; break;
            case FIVEGIGABIT: linkSpeed = 5000; break;
            default:
                throw new IllegalArgumentException("Unknown link type");
        }
        return linkSpeed;
    }
}

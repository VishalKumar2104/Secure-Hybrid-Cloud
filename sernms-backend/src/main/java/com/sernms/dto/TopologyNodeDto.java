package com.sernms.dto;

public class TopologyNodeDto {
    private String id;
    private String label;
    private String title;
    private String group; // ROUTER, SWITCH, FIREWALL, SERVER, AWS_VPC, AWS_EC2, AWS_RDS, ENDPOINT
    private String status; // ONLINE, OFFLINE, WARNING
    private String ip;
    private int level; // Hierarchical layout level (1=Internet, 2=Router/Edge, 3=Firewall, 4=Core, 5=Access, 6=VLANs/Hosts)
    private String shape;
    private String color;

    public TopologyNodeDto() {}

    public TopologyNodeDto(String id, String label, String title, String group, String status, String ip, int level, String shape, String color) {
        this.id = id;
        this.label = label;
        this.title = title;
        this.group = group;
        this.status = status;
        this.ip = ip;
        this.level = level;
        this.shape = shape;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}

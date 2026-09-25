package com.sernms.dto;

public class TopologyEdgeDto {
    private String from;
    private String to;
    private String label;
    private String type; // FIBER, TRUNK, ACCESS, VPN_TUNNEL
    private String status; // ACTIVE, DOWN, WARNING
    private String color;
    private boolean dashes;
    private int width;

    public TopologyEdgeDto() {}

    public TopologyEdgeDto(String from, String to, String label, String type, String status, String color, boolean dashes, int width) {
        this.from = from;
        this.to = to;
        this.label = label;
        this.type = type;
        this.status = status;
        this.color = color;
        this.dashes = dashes;
        this.width = width;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isDashes() { return dashes; }
    public void setDashes(boolean dashes) { this.dashes = dashes; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
}

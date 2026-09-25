package com.sernms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aws_metrics")
public class AwsMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false, length = 100)
    private String resourceId;

    @Column(name = "metric_name", nullable = false, length = 50)
    private String metricName; // CPUUtilization, NetworkIn, NetworkOut, FreeableMemory

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(length = 20)
    private String unit = "Percent";

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AwsMetric() {}

    public AwsMetric(String resourceId, String metricName, Double metricValue, String unit) {
        this.resourceId = resourceId;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.unit = unit;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

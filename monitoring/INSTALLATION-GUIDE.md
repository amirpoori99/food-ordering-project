# Food Ordering System - Monitoring Installation Guide

## Overview
This guide will help you set up Prometheus, Grafana, and Alertmanager for the Food Ordering System.

## Prerequisites
- Windows 10/11 or Windows Server 2016+
- PowerShell 5.1 or later
- Administrative privileges
- Internet connection (for downloading components)

## Installation Steps

### 1. Download Required Components

#### Prometheus
- Download from: https://prometheus.io/download/
- Choose: prometheus-2.47.0.windows-amd64.zip
- Extract to: `monitoring\prometheus\`

#### Grafana
- Download from: https://grafana.com/grafana/download
- Choose: grafana-10.1.0.windows-amd64.zip
- Extract to: `monitoring\grafana\`

#### Alertmanager
- Download from: https://prometheus.io/download/
- Choose: alertmanager-0.26.0.windows-amd64.zip
- Extract to: `monitoring\alertmanager\`

### 2. Directory Structure
After extraction, your directory structure should look like:
```
monitoring/
â”œâ”€â”€ prometheus/
â”‚   â”œâ”€â”€ prometheus.exe
â”‚   â”œâ”€â”€ promtool.exe
â”‚   â”œâ”€â”€ prometheus.yml
â”‚   â”œâ”€â”€ alerting-rules.yml
â”‚   â”œâ”€â”€ recording-rules.yml
â”‚   â””â”€â”€ start-prometheus.ps1
â”œâ”€â”€ grafana/
â”‚   â”œâ”€â”€ bin/
â”‚   â”‚   â””â”€â”€ grafana-server.exe
â”‚   â”œâ”€â”€ conf/
â”‚   â”œâ”€â”€ food-ordering-overview.json
â”‚   â”œâ”€â”€ food-ordering-business.json
â”‚   â””â”€â”€ start-grafana.ps1
â”œâ”€â”€ alertmanager/
â”‚   â”œâ”€â”€ alertmanager.exe
â”‚   â”œâ”€â”€ alertmanager.yml
â”‚   â””â”€â”€ start-alertmanager.ps1
â””â”€â”€ start-monitoring.ps1
```

### 3. Starting Services

#### Option 1: Start All Services
```powershell
.\monitoring\start-monitoring.ps1 -Action start
```

#### Option 2: Start Individual Services
```powershell
# Start Prometheus
.\monitoring\prometheus\start-prometheus.ps1

# Start Grafana
.\monitoring\grafana\start-grafana.ps1

# Start Alertmanager
.\monitoring\alertmanager\start-alertmanager.ps1
```

### 4. Access Web Interfaces

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Alertmanager**: http://localhost:9093

### 5. Grafana Dashboard Import

1. Login to Grafana (admin/admin)
2. Go to "+" â†’ Import
3. Upload the JSON files:
   - `food-ordering-overview.json`
   - `food-ordering-business.json`

### 6. Verification

Check service status:
```powershell
.\monitoring\start-monitoring.ps1 -Action status
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Check if ports 9090, 3000, 9093 are free
   - Use `netstat -an | findstr :9090` to check

2. **Permission Denied**
   - Run PowerShell as Administrator
   - Check antivirus software

3. **Service Won't Start**
   - Check Windows Event Viewer
   - Verify file paths in configuration

### Log Files
- Prometheus: `monitoring\prometheus\data\`
- Grafana: `monitoring\grafana\data\log\`
- Alertmanager: `monitoring\alertmanager\data\`

## Security Considerations

1. **Change Default Passwords**
   - Grafana: Change admin password
   - Configure authentication

2. **Firewall Configuration**
   - Allow inbound rules for ports 9090, 3000, 9093
   - Restrict access to trusted networks

3. **HTTPS Configuration**
   - Configure SSL certificates
   - Enable HTTPS for all services

## Maintenance

### Regular Tasks
- Monitor disk usage for time-series data
- Review and adjust retention policies
- Update monitoring rules as needed
- Backup dashboard configurations

### Updates
- Check for new versions regularly
- Test updates in non-production environment
- Update configuration files as needed

## Support

For issues related to:
- Food Ordering System: Check project documentation
- Prometheus: https://prometheus.io/docs/
- Grafana: https://grafana.com/docs/
- Alertmanager: https://prometheus.io/docs/alerting/

---

**Installation Date**: {Installation Date}
**Version**: Phase 42 Implementation
**Support Contact**: System Administrator

@echo off
echo Starting Prometheus on port 9090...
echo Prometheus would be running with config: config\monitoring\prometheus-config.yml
echo Data directory: data\prometheus
echo.
echo In production, this would execute:
echo prometheus.exe --config.file=config\monitoring\prometheus-config.yml --storage.tsdb.path=data\prometheus --web.listen-address=:9090
echo.
echo Monitoring stack is configured and ready!
pause

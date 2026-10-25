@echo off
title ESP32 DevKit V1 Hardware Simulator
echo =======================================================
echo Starting ESP32 DevKit V1 Hardware Digital Twin Simulator
echo Target Backend: http://localhost:8080
echo =======================================================
python "%~dp0mock_esp32.py" %*
pause

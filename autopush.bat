@echo off
cd /d "%~dp0"

for /f "tokens=1-4 delims=/ " %%a in ("%date%") do set d=%%d-%%b-%%c
for /f "tokens=1-2 delims=: " %%a in ("%time%") do set t=%%a-%%b

git add .

git diff --cached --quiet
if %errorlevel%==0 (
    echo No changes to commit.
    exit /b 0
)

git commit -m "Auto snapshot %d% %t%"
git push --force
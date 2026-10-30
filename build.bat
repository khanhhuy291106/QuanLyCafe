@echo off
rem Biên dịch toàn bộ mã nguồn (cần JDK 11 trở lên đã có lệnh javac trong PATH)
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
if errorlevel 1 (
  echo Bien dich LOI.
) else (
  echo Bien dich xong. Chay chuong trinh bang run.bat
)
del sources.txt
pause

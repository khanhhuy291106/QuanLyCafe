@echo off
rem Chạy chương trình (nhớ đứng ở thư mục gốc của project để đọc được config\config.properties)
java -Dfile.encoding=UTF-8 -cp "bin;lib/*" gui.Main
pause

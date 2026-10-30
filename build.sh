#!/bin/bash
# Biên dịch toàn bộ mã nguồn (cần JDK 11 trở lên)
mkdir -p bin
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src -name "*.java") && echo "Biên dịch xong. Chạy bằng ./run.sh"

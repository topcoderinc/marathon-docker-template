rm -rf ./bin
mkdir ./bin
find -name "*.java" > sources.txt
javac -d ./bin @sources.txt
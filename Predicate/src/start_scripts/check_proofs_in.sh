#!/usr/bin/env bash
GREEN='\033[0;32m'
NC='\033[0m'
printf "All proofs from the directory ${GREEN}src/proofs_in${NC} will be checked now\n"
read -n 1 -s -p "Press any key to start"
printf "\n"
cd ~/ITMO/MathLogic/Predicate/
javac -d out/production/Predicate src/*.java
cd src/
for file in proofs_in/*
do
	name=${file##*/}
	java  -cp ~/ITMO/MathLogic/Predicate/out/production/Predicate Main "$file" "proofs_out/"${name/.in/.out}
done
#!/usr/bin/env bash
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
NC='\033[0m'
ARG=$1
cd ~/ITMO/MathLogic/Predicate/
javac -d out/production/Predicate src/*.java
cd src/

if [ -d "${ARG}" ]; then
    printf "All proofs ${PURPLE}<filename>.*${NC} from the directory ${GREEN}"${ARG}"${NC} will be checked now\n"
    read -n 1 -s -p "Press any key to start"
    printf "\n"
    mkdir -p ${ARG}/"results"
    printf "Processed proofs ${PURPLE}<filename>.out${NC} are written into the folder ${GREEN}results${NC}\n"
    for file in ${ARG}/*.*
        do
	        name=${file##*/}
	        java -cp ~/ITMO/MathLogic/Predicate/out/production/Predicate Main "${ARG}/${name}" "${ARG}/results/"${name/.*/.out}
        done
else
    if [ -f "${ARG}" ]; then
        dir="${ARG%/*}"
        name=${ARG##*/}
        printf "The proof from ${GREEN}"${name}"${NC} will be checked now\n"
        read -n 1 -s -p "Press any key to start"
        printf "\n"
        java -cp ~/ITMO/MathLogic/Predicate/out/production/Predicate Main "${ARG}" "${dir}/"${name/.*/.out}
    else
        echo "Error: ${ARG} is not found";
        exit 1
    fi
fi
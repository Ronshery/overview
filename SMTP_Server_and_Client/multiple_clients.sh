#!/usr/bin/env bash
for (( n=1; n<=$@; n++ ))
do
	java -jar SMTPClient.jar 127.0.0.1 6332 &
done
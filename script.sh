#!/bin/bash

CREATIONS_DIR=./creations
CHUNKS_DIR=./chunks

AUDIO_DIR=$TMP_DIR/audio.wav

case $1 in
	create)
		creationName=$2
		chunksList=""
		argNum=0
		for i in $@
		do
			#
			if [ $argNum -gt 1 ]
			then
				chunksList="$chunksList $CHUNKS_DIR/$i.wav"
			fi
			argNum=$(($argNum + 1))
		done
		
		# Combines the chunks supplied in the args into a single .wav file
		# Note: chunksList starts with a space
		sox${chunksList} $CREATIONS_DIR/${creationName}/${creationName}.wav
		;;
	*)
		echo "Invalid menu choice." >&2
		exit 1
		;;
esac

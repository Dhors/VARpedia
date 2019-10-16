#!/bin/bash

save() {
	voiceChoice=$1	
	name=$2

	# Make directory to store chunks if necessary
	if [ ! -d "$CHUNKS_DIR" ]
	then
		mkdir "$CHUNKS_DIR"
	fi
	
	voice=""
	case $voiceChoice in
		Default)
			voice="kal_diphone"
			;;
		NZ-Man)
			voice="akl_nz_jdt_diphone"
			;;
		NZ-Woman)
			voice="akl_nz_cw_cg_cg"
			;;
		*)
			echo "Invalid selection." >&2
			return 1
			;;
	esac

	extension=1
	while [ -e "$CHUNKS_DIR/${name}${extension}.wav" ]
	do
		extension=$(($extension + 1))
	done
	name="${name}${extension}"

	text2wave -o chunks/$name.wav -eval "(voice_$voice)" input.txt
	rm -f input.txt
}

CREATIONS_DIR=./creations
CHUNKS_DIR=./chunks

AUDIO_DIR=$TMP_DIR/audio.wav

case $1 in
	preview)
		rm -f input.txt
		for i in $@
		do
			if [ "$i" != "./script.sh" ] && [ "$i" != "preview" ]
			then
				echo "$i " >> input.txt
			fi
		done
		festival --tts "input.txt"
		rm -f input.txt
		;;
	save)
		argNum=0
		name=""
		rm -f input.txt

		for i in $@
		do
			# Only store words from the "chunk" input
			if [ $argNum -gt 1 ]
			then
				echo "$i " >> input.txt
				if [ $argNum -lt 7 ]
				then
					name="${name}$i-"
				fi
			fi
			argNum=$(($argNum + 1))
		done

		voiceChoice=$2
		save $voiceChoice $name
		;;
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

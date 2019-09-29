#!/bin/bash

search() {
	# Create a fresh temporary directory (in case there was an error in previous functioning)
#	if [ -d "$TMP_DIR" ]
#	then
#		rm -rf $TMP_DIR
#	fi
#	mkdir $TMP_DIR	

	search_term=$1
	search_result=`wikit $search_term`
	
	# If a search is invalid it returns "[search term] not found :^("
	echo $search_result | grep "not found :^(" &> /dev/null
	# If there was an instance of the above then $? is 0
	search_is_invalid=$?

	if	[ "$search_is_invalid" -eq 0 ]
	then
		echo "(Term not found)"
	else
		# Separate each sentence into a new line
#		echo $search_result | sed 's/\. /.\n/g' > $FULL_SEARCH_DIR
	
		# Count and print the number of lines
#		total_sentences=`cat $FULL_SEARCH_DIR | wc -l`
#		echo $total_sentences
		
		# Print the search result with lines numbered
#		cat -n $FULL_SEARCH_DIR
		
		echo $search_result
	fi
}

save() {
	voiceChoice=$1	
	name=$2

	# Make directory to store chunks if necessary
	if [ ! -d "./chunks" ]
	then
		mkdir "chunks"
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
	while [ -e "chunks/${name}${extension}.wav" ]
	do
		extension=$(($extension + 1))
	done
	name="${name}${extension}"

	text2wave -o chunks/$name.wav -eval "(voice_$voice)" input.txt
	rm -f input.txt
}

list() {
	# If there is no creations folder then there are no creations
	if [ ! -d "$CREATIONS_DIR" ]
	then
		num_of_creations=0	
		echo $num_of_creations	
	else
		num_of_creations=`ls $CREATIONS_DIR | wc -l`
		echo $num_of_creations	
		# Only list creations if there are any		
		if [ "$num_of_creations" -ne 0 ]
		then
			# List the creations and remove the file extensions
			ls $CREATIONS_DIR | sed 's/\(.*\)\..*/\1/'
		fi
	fi
}

play() {
	selected_creation_filename=$1
	selected_creation_dir=$CREATIONS_DIR/$selected_creation_filename.mp4
	
	ffplay -autoexit $selected_creation_dir &> /dev/null
	
	if [ "$?" -ne 0 ]
	then
		echo "Error playing video." >&2
	fi
}

delete() {
	selected_creation_filename=$1
	selected_creation_dir=$CREATIONS_DIR/$selected_creation_filename.mp4
	
	rm -f $selected_creation_dir
}
create() {
	search_term=$1
	included_sentences=$2
	creation_name=$3
	
	current_creation_dir=$CREATIONS_DIR"/"$creation_name.mp4
	
	# Save the specified range of sentences into a file to be used in text2wave
	head -n $included_sentences $FULL_SEARCH_DIR > $PARTIAL_SEARCH_DIR

	# Convert the sentences into a .wav format using festival
	text2wave -o $AUDIO_DIR $PARTIAL_SEARCH_DIR
	
	if [ ! -d "$CREATIONS_DIR" ]
	then
		mkdir $CREATIONS_DIR
	fi

	# Create the .mp4 creation using the .wav sound file and search term
	ffmpeg -f lavfi -i color=c=blue:s=320x240:d=5 -i $AUDIO_DIR -strict -2 -vf "drawtext=fontfile=$FONT_DIR:fontsize=30: fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='$search_term'" $current_creation_dir &> /dev/null
	
	if [ "$?" -eq 0 ]
	then
		echo "Creation was successfully created."
	else
		echo "Creation was not successfully created." >&2
	fi

	# Delete the temporary text and .wav files
	rm -rf $TMP_DIR
}

CREATIONS_DIR=./creations
TMP_DIR=./tmp
FONT_DIR=./BodoniFLF-Roman.ttf

FULL_SEARCH_DIR=$TMP_DIR/full_search.txt
PARTIAL_SEARCH_DIR=$TMP_DIR/part_search.txt
AUDIO_DIR=$TMP_DIR/audio.wav

case $1 in
	l)
		list
		;;
	p)
		selected_creation=$2		
		play $selected_creation
		;;
	d)
		selected_creation=$2
		delete $selected_creation
		;;
	search)
		search_term=$2
		search $search_term
		;;
	c)
		search_term=$2
		included_sentences=$3
		creation_name=$4
		create $search_term $included_sentences $creation_name
		;;
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
		argNum=0
		chunksList=""

		for i in $@
		do
			#
			if [ $argNum -gt 0 ]
			then
				chunksList="$chunksList./chunks/$i.wav "
			fi
			argNum=$(($argNum + 1))
		done
		
		# Currently only saves creations as test.wav, can use text field here
		creationName="test"

		sox ${chunksList}./creations/${creationName}.wav
		;;
	*)
		echo "Invalid selection." >&2
		exit 1
		;;
esac

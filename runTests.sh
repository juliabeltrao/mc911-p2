
if [ -z $1 ]; then
  path=test/smaller/*
  #path=test/bigger/*
else
  path=$1
fi

FILES=$path

make
count=0
correct=0
echo "" > output.txt
echo "" > error.txt
for f in $FILES
do
  echo "------------------------------" >> output.txt
  echo "| Processing $f" >> output.txt
  echo "------------------------------" >> output.txt
  echo "------------------------------" >> error.txt
  echo "| Processing $f" >> error.txt
  echo "------------------------------" >> error.txt
  filename="${f%.*}"
  out=out/$filename.s
  RES=0
  make run INPUT=$f OUTPUT=$out >> output.txt 2>> error.txt &&
  lli $out >> output.txt 2>> error.txt &&
  RES=1 && ((correct+=1))
  if [ $RES == 0 ]; then
	echo "TEST $f FAILED"
  fi
  # take action on each file. $f store current file name
  # cat $f
  ((count+=1))
  #if [ $count -ge 3 ]; then
  #  break
  #fi
done

echo "Correct = $correct/$count"

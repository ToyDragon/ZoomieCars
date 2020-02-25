stty -icanon min 1
stty -echo
java -cp "out/production/ZoomieCars" me.frogtown.zoomiecars.Main
stty echo

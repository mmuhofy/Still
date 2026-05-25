SOURCE_IMG="icon.png"
RES_DIR="../app/src/main/res"

mkdir -p $RES_DIR/mipmap-mdpi
mkdir -p $RES_DIR/mipmap-hdpi
mkdir -p $RES_DIR/mipmap-xhdpi
mkdir -p $RES_DIR/mipmap-xxhdpi
mkdir -p $RES_DIR/mipmap-xxxhdpi

cp $SOURCE_IMG $RES_DIR/mipmap-xxxhdpi/ic_launcher.png
cp $SOURCE_IMG $RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png

cp $SOURCE_IMG $RES_DIR/mipmap-xxhdpi/ic_launcher.png
cp $SOURCE_IMG $RES_DIR/mipmap-xxhdpi/ic_launcher_round.png


PARENT_DIR=$(cd $(dirname $0);cd ..; pwd)
echo $PARENT_DIR
TARGET_DIR=$(pwd)
cp $PARENT_DIR/dist/target/arctic-0.4.0-SNAPSHOT-bin.zip $TARGET_DIR/
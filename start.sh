PREV=$(pwd)

VERSION=0.4
LOCATION=/var/www/www-root/data/www/service/aggregator
cd $LOCATION

PID=$(head -1 pid)
echo attempt to kill $PID

pkill -F pid java

java -Dserver.port=8000 -Xms64m -Xmx64m -Xss1m -jar aggregator-$VERSION.jar > /dev/null &
echo $! > pid
echo started process $!

cd $PREV

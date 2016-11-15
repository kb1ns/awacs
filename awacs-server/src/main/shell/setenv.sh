export JAVA_OPTS="-XX:+DisableExplicitGC \
	-XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
	-XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m \
	-XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly \
	-XX:CMSInitiatingOccupancyFraction=70"


release: java -Dspring.profiles.active=migrate -jar build/libs/app.jar
web: java -Dserver.port=3001 $JAVA_OPTS -jar build/libs/app.jar & cd netplix-frontend && PORT=$PORT node_modules/.bin/next start -p $PORT

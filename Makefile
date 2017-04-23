test: app-test server-test

app-test:
	cd app && yarn headless

server-test:
	cd server/node && yarn test

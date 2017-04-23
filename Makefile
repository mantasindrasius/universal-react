test: app-test server-test

app-test:
	cd app && yarn install && yarn headless

server-test:
	cd server/node && yarn install && yarn test

#!/bin/bash

for build in app server/node server/java
do
  echo 'Building '$build
  (cd $build; yarn install; yarn setup; yarn build; yarn test)
done

#find . -name package.json -not -path "**/node_modules/**" -exec echo 'Building {}' \; -execdir yarn install \; -execdir yarn test \;

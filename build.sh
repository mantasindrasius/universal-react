#!/bin/bash

for build in app server/node server/java
do
  echo 'Building '$build
  (cd $build; yarn install; yarn setup; yarn test)
done

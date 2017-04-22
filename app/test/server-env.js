'use strict';

module.exports = {
  node: nodeEnv
}

function nodeEnv({ port }) {
  const nodeServerEnv = require('../../server/node/test/node-server-env');

  return {
    start: () => nodeServerEnv({ port }).start()
  };
}
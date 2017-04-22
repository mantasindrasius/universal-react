'use strict';

const server = require('../src/server');

module.exports = function nodeServerEnv({ port }) {
  return {
    start: () => server({ port })
  }
};
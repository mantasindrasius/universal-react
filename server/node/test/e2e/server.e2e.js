'use strict';

const nodeServerEnv = require('../node-server-env');
const fetch = require('node-fetch');
const chai = require('chai');
const expect = chai.expect;

describe('Server', function() {
  var server;

  before(() => {
    server = nodeServerEnv({ port: 9901 })
      .start();
  });

  after(() => server.close());

  it('should respond with HTML greeting', async () => {
    let response = await fetch('http://localhost:9901/hello');
    let body = await response.text();

    expect(body).to.include('Welcome');
  });
});
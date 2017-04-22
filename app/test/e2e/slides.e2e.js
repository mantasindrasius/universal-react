'use strict';

const webdriver = require('selenium-webdriver'),
      By = webdriver.By,
      until = webdriver.until,
      chrome = require('selenium-webdriver/chrome'),
      options = new chrome.Options(),
      serverEnv = require('../server-env'),
      expect = require('chai').expect;

describe('Slides', function() {
  const serverUrl = 'http://localhost:9901';

  var driver, server;

  before(() => {
    driver = new webdriver.Builder()
      .forBrowser('chrome')
      //.setChromeOptions(new chrome.Options().addArguments('--headless'))
      .build();

    server = serverEnv.node({ port: 9901 })
      .start();
  });

  after(() => driver.quit()
    .then(() => server.close()));

  it('should run through slides', async() => {
    driver.get(`${serverUrl}/hello`);
    
    let element = await driver.findElement(By.id('welcome-message'));
    let welcomeMessage = await element.getText();
    
    return expect(welcomeMessage).to.equal('Welcome!');
  });
});
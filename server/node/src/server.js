'use strict';

const express = require('express');
const app = express();

module.exports = function nodeServer({ port }) {
  app.get('/hello', (req, res) => {
    res.send('<div id="welcome">Welcome!</div>');
  });

  let server = app.listen(port, () => {
    console.log(`Listening on port ${port}!`);
  });

  return {
    close: () => server.close()
  };
}
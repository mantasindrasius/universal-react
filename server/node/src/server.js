'use strict';

const express = require('express');
const app = express();
const reactViews = require('express-react-views');

module.exports = function nodeServer({ port }) {
  const viewsDir = __dirname + '/views';

  app.set('views', viewsDir);
  app.set('view engine', 'jsx');
  app.engine('jsx', reactViews.createEngine());

  app.get('/hello', (req, res) => {
    res.render('index', { welcomeMessage: 'Welcome' });
    //res.send('<div id="welcome">Welcome!</div>');
  });

  let server = app.listen(port, () => {
    console.log(`Listening on port ${port}!`);
  });

  return {
    close: () => server.close()
  };
}
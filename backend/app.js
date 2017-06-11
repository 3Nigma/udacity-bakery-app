const express = require('express');
const path = require('path');
const logger = require('morgan');
const bodyParser = require('body-parser');

const recipes = require('./routes/recipes');

const app = express();

app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use('/recipes', recipes);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  const err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // render the error page
  res.status(err.status || 500);
});

module.exports = app;

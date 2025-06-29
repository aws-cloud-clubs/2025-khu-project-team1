const express = require('express');
const followRoutes = require('./routes/follow');

const app = express();

app.use(express.json());

app.use((req, res, next) => {
    console.log("Request URL:", req.url);
    console.log("Request Method:", req.method);
    next();
});

app.use('/follow', followRoutes);

module.exports = app;

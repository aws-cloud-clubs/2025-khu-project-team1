const AWS = require('aws-sdk');

const db = new AWS.DynamoDB.DocumentClient({
  region: process.env.AWS_REGION || 'ap-northeast-2'
});

module.exports = db;

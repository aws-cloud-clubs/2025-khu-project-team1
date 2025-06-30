const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const AWS = require('aws-sdk');

// DynamoDB
AWS.config.update({ region: 'ap-northeast-2' });
const dynamoDb = new AWS.DynamoDB.DocumentClient();
const FOLLOW_TABLE_NAME = process.env.FOLLOW_TABLE_NAME || 'Follows';

// Follow
router.post('/', async (req, res) => {
    const { followerId, followeeId } = req.body;
    const followId = uuidv4();

    const params = {
        TableName: FOLLOW_TABLE_NAME,
        Item: {
            followId,
            followerId,
            followeeId,
            createdAt: new Date().toISOString()
        }
    };

    try {
        await dynamoDb.put(params).promise();
        res.status(201).json({ message: 'Followed successfully', followId });
    } catch (error) {
        console.error("DynamoDB put error:", error);
        res.status(500).json({ error: error.message });
    }
});

// Unfollow
router.post('/unfollow', async (req, res) => {
    const { followId } = req.body;

    const params = {
        TableName: FOLLOW_TABLE_NAME,
        Key: { followId }
    };

    try {
        await dynamoDb.delete(params).promise();
        res.status(200).json({ message: 'Unfollowed successfully' });
    } catch (error) {
        console.error("DynamoDB delete error:", error);
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;

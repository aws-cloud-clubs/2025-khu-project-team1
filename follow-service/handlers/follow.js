const { getUserIdFromToken } = require('../utils/auth');
const db = require('../utils/db');
const { getAsync, setAsync } = require('../utils/cache');

const TABLE_NAME = process.env.FOLLOW_TABLE;

module.exports.followUser = async (event) => {
  try {
    const token = event.headers.Authorization?.split(' ')[1] || event.headers.authorization?.split(' ')[1];
    const userId = getUserIdFromToken(token);
    const targetId = event.pathParameters.user_id;

    if (userId === targetId) {
      return {
        statusCode: 400,
        body: JSON.stringify({ detail: '자기 자신을 팔로우할 수 없습니다.' }),
      };
    }

    await db.put({
      TableName: TABLE_NAME,
      Item: {
        followerId: userId,
        followeeId: targetId,
        followedAt: new Date().toISOString()
      },
      ConditionExpression: 'attribute_not_exists(followerId) AND attribute_not_exists(followeeId)'
    }).promise();

    return {
      statusCode: 200,
      body: JSON.stringify({ message: 'Successfully followed user' }),
    };
  } catch (error) {
    console.error(error);
    return {
      statusCode: 500,
      body: JSON.stringify({ detail: '서버 에러' }),
    };
  }
};

module.exports.unfollowUser = async (event) => {
  try {
    const token = event.headers.Authorization?.split(' ')[1];
    const userId = getUserIdFromToken(token);
    const targetId = event.pathParameters.user_id;

    await db.delete({
      TableName: TABLE_NAME,
      Key: {
        followerId: userId,
        followeeId: targetId
      }
    }).promise();

    return {
      statusCode: 200,
      body: JSON.stringify({ message: 'Successfully unfollowed user' }),
    };
  } catch (error) {
    console.error(error);
    return {
      statusCode: 500,
      body: JSON.stringify({ detail: '서버 에러' }),
    };
  }
};

module.exports.getFollowing = async (event) => {
  try {
    const token = event.headers.Authorization?.split(' ')[1];
    const userId = getUserIdFromToken(token);
    const cacheKey = `following:${userId}`;

    const cached = await getAsync(cacheKey);
    if (cached) {
      return {
        statusCode: 200,
        body: JSON.stringify(cached),
      };
    }

    const result = await db.query({
      TableName: TABLE_NAME,
      KeyConditionExpression: 'followerId = :fid',
      ExpressionAttributeValues: {
        ':fid': userId
      }
    }).promise();

    const following = result.Items.map(item => ({
      userId: item.followeeId,
      followedAt: item.followedAt
    }));

    await setAsync(cacheKey, following, 60);

    return {
      statusCode: 200,
      body: JSON.stringify(following),
    };
  } catch (error) {
    console.error('getFollowing error:', error);
    return {
      statusCode: 500,
      body: JSON.stringify({ detail: '서버 에러' }),
    };
  }
};

module.exports.getFollowers = async (event) => {
  try {
    const token = event.headers.Authorization?.split(' ')[1];
    const userId = getUserIdFromToken(token);
    const cacheKey = `followers:${userId}`;

    const cached = await getAsync(cacheKey);
    if (cached) {
      return {
        statusCode: 200,
        body: JSON.stringify(cached),
      };
    }

    const result = await db.query({
      TableName: TABLE_NAME,
      IndexName: 'FolloweeIndex',
      KeyConditionExpression: 'followeeId = :fid',
      ExpressionAttributeValues: {
        ':fid': userId
      }
    }).promise();

    const followers = result.Items.map(item => ({
      userId: item.followerId,
      followedAt: item.followedAt
    }));

    await setAsync(cacheKey, followers, 60);

    return {
      statusCode: 200,
      body: JSON.stringify(followers),
    };
  } catch (error) {
    console.error('getFollowers error:', error);
    return {
      statusCode: 500,
      body: JSON.stringify({ detail: '서버 에러' }),
    };
  }
};

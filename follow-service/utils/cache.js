const redis = require('redis');

const client = redis.createClient({
  socket: {
    host: process.env.REDIS_HOST,
    port: Number(process.env.REDIS_PORT),
  },
  password: process.env.REDIS_PASSWORD || undefined,
});

client.connect()
  .then(() => console.log(' Redis connected'))
  .catch(err => console.error(' Redis connection failed:', err));

async function getAsync(key) {
  try {
    const data = await client.get(key);
    return data ? JSON.parse(data) : null;
  } catch (err) {
    console.error('Redis GET error:', err);
    return null;
  }
}

async function setAsync(key, value, ttl = 60) {
  try {
    await client.setEx(key, ttl, JSON.stringify(value));
  } catch (err) {
    console.error('Redis SET error:', err);
  }
}

module.exports = { getAsync, setAsync };

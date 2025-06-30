const jwt = require('jsonwebtoken');
const secret = process.env.JWT_SECRET;

function getUserIdFromToken(token) {
  try {
    const decoded = jwt.verify(token, secret);
    return decoded.user_id || decoded.sub; 
  } catch (err) {
    console.error('JWT verification failed:', err);
    return null;
  }
}

module.exports = { getUserIdFromToken };

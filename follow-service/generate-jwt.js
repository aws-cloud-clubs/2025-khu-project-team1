const jwt = require('jsonwebtoken');
require('dotenv').config();

const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
  console.error('JWT_SECRET is not defined in .env');
  process.exit(1);
}

const payload = {
  sub: 'user123',       
};

const token = jwt.sign(payload, JWT_SECRET, {
  expiresIn: '1h',
});

console.log('JWT token generated:\n');
console.log(token);

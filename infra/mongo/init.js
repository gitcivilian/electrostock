const password = process.env.MONGO_APP_PASSWORD;
if (!password || password === 'replace-with-random-app-password') {
  throw new Error('Generate a real application password before initializing MongoDB.');
}

const electrostock = db.getSiblingDB('electrostock');
if (!electrostock.getUser('electrostock_app')) {
  electrostock.createUser({
    user: 'electrostock_app',
    pwd: password,
    roles: [{ role: 'readWrite', db: 'electrostock' }],
  });
}
electrostock.getCollection('_setup').updateOne(
  { _id: 'initialized' },
  { $setOnInsert: { initializedAt: new Date() } },
  { upsert: true },
);

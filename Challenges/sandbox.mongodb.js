const gadgetStore = db.getSiblingDB('gadgetStore');

gadgetStore.createCollection('products', {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["name", "price", "inStock"],
            properties: {
                name: {
                    bsonType: "string"
                },
                price: {
                    bsonType: ["int", "double"]
                },
                inStock: {
                    bsonType: "bool"
                }
            }
        }
    },
    validationAction: "error"
});

// Insert products
gadgetStore.products.insertMany([
    {
        name: "Wireless Mouse",
        price: 25,
        inStock: true,
        specs: { brand: "Logitech" }
    },
    {
        name: "Mechanical Keyboard",
        price: 80,
        inStock: true,
        specs: { brand: "Keychron" }
    },
    {
        name: "Gaming Monitor",
        price: 249.99,
        inStock: false,
        specs: { brand: "Samsung" }
    }
]);

gadgetStore.products.insertOne({
    name: "Gaming Headset",
    inStock: true,
    specs: { brand: "HyperX" }
});

const mouse = gadgetStore.products.findOne({ name: "Wireless Mouse" });

gadgetStore.products.updateOne(
    { _id: mouse._id },
    {
        $set: { category: "Accessories" },
        $inc: { price: 15 },
        $push: { tags: "wireless" }
    }
);

gadgetStore.products.updateOne(
    { _id: mouse._id },
    {
        $push: { tags: "bestseller" }
    }
);

gadgetStore.products.updateOne(
    { _id: mouse._id },
    {
        $pull: { tags: "wireless" }
    }
);

gadgetStore.products.find({
    price: { $gte: 100 }
});

gadgetStore.products.find({
    "specs.brand": "Logitech"
});

gadgetStore.products.find({
    category: { $in: ["Accessories", "Electronics"] }
});

gadgetStore.createCollection("orders");

gadgetStore.orders.insertOne({
    productId: mouse._id,
    quantity: 2
});

gadgetStore.orders.aggregate([
    {
        $lookup: {
            from: "products",
            localField: "productId",
            foreignField: "_id",
            as: "product"
        }
    },
    {
        $unwind: "$product"
    },
    {
        $project: {
            _id: 0,
            productName: "$product.name",
            quantity: 1
        }
    }
]);
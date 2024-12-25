const Sequelize = require("sequelize");
const db = require("../config/database.js");
const { DataTypes } = Sequelize;

const Reply = db.define('Replies', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    idKP: {
        type: DataTypes.INTEGER,
        allowNull: false
    },
    responseLetterUrl: {
        type: DataTypes.STRING,
        allowNull: false
    }
}, {
    freezeTableName: true
});

module.exports = Reply;
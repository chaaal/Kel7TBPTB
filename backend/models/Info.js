const Sequelize = require("sequelize");
const db = require("../config/database.js");
const { DataTypes } = Sequelize;

const Info = db.define('Infos', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    title: {
        type: DataTypes.STRING,
        allowNull: false
    },
    description: {
        type: DataTypes.TEXT,
        allowNull: false
    }
}, {
    freezeTableName: true
});

module.exports = Info;
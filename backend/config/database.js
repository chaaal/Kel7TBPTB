const Sequelize = require('sequelize');

const db = new Sequelize('db_kp','root','',{
    host:"localhost",
    dialect:"mysql",
    logging: false 

});

module.exports = db; 

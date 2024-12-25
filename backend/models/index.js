const Member = require('./Member');
const Group = require('./Group');
const GroupMember = require('./GroupMember');
const KPRequest = require('./KPRequest');
const Reply = require('./Reply');
const Info = require('./Info');

// Define relationships
Member.belongsToMany(Group, { through: GroupMember });
Group.belongsToMany(Member, { through: GroupMember });

Group.hasMany(KPRequest, { foreignKey: 'groupId' });
KPRequest.belongsTo(Group, { foreignKey: 'groupId' });

KPRequest.hasMany(Reply, { foreignKey: 'idKP' });
Reply.belongsTo(KPRequest, { foreignKey: 'idKP' });

module.exports = {
    Member,
    Group,
    GroupMember,
    KPRequest,
    Reply,
    Info
};